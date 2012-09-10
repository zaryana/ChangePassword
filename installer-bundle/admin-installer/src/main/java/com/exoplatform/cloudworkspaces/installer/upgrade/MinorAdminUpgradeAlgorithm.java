/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.exoplatform.cloudworkspaces.installer.upgrade;

import com.exoplatform.cloudworkspaces.installer.InstallerException;
import com.exoplatform.cloudworkspaces.installer.configuration.AdminConfigurationManager;
import com.exoplatform.cloudworkspaces.installer.configuration.AdminDirectories;
import com.exoplatform.cloudworkspaces.installer.configuration.CurrentAdmin;
import com.exoplatform.cloudworkspaces.installer.configuration.PreviousAdmin;
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;
import com.exoplatform.cloudworkspaces.installer.rest.AdminException;
import com.exoplatform.cloudworkspaces.installer.rest.CloudAdminServices;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;

/**
 * Just start AS with new image id wait while it became ONLINE and stop previous
 * servers
 */
public abstract class MinorAdminUpgradeAlgorithm extends AdminUpgradeAlgorithm {

  protected final Logger                    logger = new Logger();

  protected final PreviousAdmin             prevAdmin;

  protected final CurrentAdmin              currAdmin;

  protected final InteractionManager        interaction;

  protected final AnswersManager            answers;

  protected final AdminConfigurationManager configurationManager;

  public MinorAdminUpgradeAlgorithm(PreviousAdmin prevAdmin,
                                    CurrentAdmin currAdmin,
                                    InteractionManager interaction,
                                    AnswersManager answers,
                                    AdminConfigurationManager configurationManager) {
    this.prevAdmin = prevAdmin;
    this.currAdmin = currAdmin;
    this.interaction = interaction;
    this.answers = answers;
    this.configurationManager = configurationManager;
  }

  @Override
  public void upgrade(AdminDirectories toDirs, boolean isClearTenants) throws InstallerException {
    logger.timePrintln("Start configuring admin...");
    configurationManager.configure();

    configurationGenerated(prevAdmin, currAdmin);

    updateStarted(currAdmin);

    logger.timePrintln("Stopping admin tomcat...");
    prevAdmin.getAdminTomcatWrapper().stopTomcat();
    logger.timePrintln("Replacing old admin with new bundle...");

    currAdmin.getAdminDirectories().moveTo(toDirs);

    tomcatStopped(currAdmin);

    logger.timePrintln("Starting admin tomcat...");
    currAdmin.getAdminTomcatWrapper().startTomcat();

    tomcatStarted(currAdmin);

    CloudAdminServices cloudAdminServices = currAdmin.getCloudAdminServices();

    boolean z = false;
    while (!z) {
      try {
        cloudAdminServices.serverStates();
        z = true;
      } catch (AdminException e) {
        z = false;
      }
    }
    logger.timePrintln("Tomcat started. Starting new AS...");

    cloudAdminServices.blockAutoscaling();
    String newAlias = cloudAdminServices.serverStart(answers.getAnswer("application.default.type"));

    String state = cloudAdminServices.serverState(newAlias);
    while (state != null && !state.equals("ONLINE")) {
      try {
        Thread.sleep(1 * 60 * 1000);
      } catch (InterruptedException e) {
        throw new InstallerException(e);
      }
      state = cloudAdminServices.serverState(newAlias);
    }
    if (state == null) {
      throw new InstallerException("Instance with new application server didn't start in time");
    }
    logger.timePrintln("New AS ready. Stopping previous AS...");
    newAsReady(currAdmin);
    for (String alias : cloudAdminServices.serverStates().keySet()) {
      if (!alias.equals(newAlias)) {
        logger.print("   ");
        logger.timePrint("stopping " + alias + "...   ");
        try {
          cloudAdminServices.serverStop(alias);
        } catch (AdminException e) {
          String serverState = cloudAdminServices.serverStates().get(alias);
          if (serverState == null || !serverState.equals("STOPPING"))
            throw e;
        }
        String serverState = cloudAdminServices.serverStates().get(alias);
        while (serverState != null && serverState.equals("STOPPING")) {
          try {
            Thread.sleep(1 * 10 * 1000);
          } catch (InterruptedException e) {
            throw new InstallerException(e);
          }
          serverState = cloudAdminServices.serverStates().get(alias);
        }
        if (serverState != null) {
          throw new AdminException("Error while stopping application server. Server still exists and has status "
              + serverState);
        }
        logger.println("stopped");
      }
    }
    cloudAdminServices.allowAutoscaling();

    if (isClearTenants) {
      logger.println();
      logger.print("Are you sure you want to delete all tenants? (yes or no): ");
      boolean mustDelete = false;
      while (true) {
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.next();
        if (answer.equals("yes")) {
          mustDelete = true;
          break;
        }
        if (answer.equals("no")) {
          mustDelete = false;
          break;
        }
        logger.print("Please, print yes or no: ");
      }
      if (mustDelete) {
        logger.timePrintln("Clearing all tenants");
        for (String tenant : cloudAdminServices.tenantList()) {
          logger.print("   ");
          logger.timePrint("deleting tenant " + tenant + "...    ");
          try {
            removeTenant(tenant, cloudAdminServices);
            logger.println("successfull");
          } catch (AdminException e) {
            logger.println("failed");
          }
        }
      }
    }

    updateFinished(currAdmin);
    logger.timePrintln("Admin successfully upgraded to " + getVersion());
  }

  protected void removeTenant(String tenant, CloudAdminServices cloudAdminServices) throws AdminException {
    Map<String, String> status = cloudAdminServices.tenantStatus(tenant);
    if (!status.isEmpty()) {
      if (status.get("state").equals("ONLINE"))
        cloudAdminServices.tenantStop(tenant);
      cloudAdminServices.tenantRemove(tenant);
    }
  }

  /*
   * @Override public void install(File confDir, File tomcatDir, File dataDir)
   * throws InstallerException { File bundleZip = new
   * File(tomcatDir.getParentFile(), "admin-bundle-" + getVersion() + ".zip");
   * if (bundleZip.exists()) { if (!bundleZip.delete()) { throw new
   * InstallerException("Could not delete file " + bundleZip.getAbsolutePath());
   * } } bundleDownloader.downloadAdminTo(bundleZip);
   * configurationManager.bindTo(confDir, tomcatDir, bundleZip);
   * configurationManager.configure();
   * cloudAdminServices.bindTo(answers.getAnswer("tenant.masterhost"),
   * "cloudadmin", answers.getAnswer("tomcat.users.admin.password"));
   * updateStarted(confDir, tomcatDir, dataDir); tomcat.stopTomcat();
   * configurationManager.update(); tomcat.startTomcat(); tomcatStarted(confDir,
   * tomcatDir, dataDir); cloudAdminServices.blockAutoscaling(); String newAlias
   * =
   * cloudAdminServices.serverStart(answers.getAnswer("application.default.type"
   * )); String state = cloudAdminServices.serverState(newAlias); while (state
   * != null && !state.equals("ONLINE")) { try { Thread.sleep(1 * 60 * 1000); }
   * catch (InterruptedException e) { throw new InstallerException(e); } state =
   * cloudAdminServices.serverState(newAlias); } if (state == null) { throw new
   * InstallerException
   * ("Instance with new application server didn't start in time"); }
   * newAsReady(confDir, tomcatDir, dataDir);
   * cloudAdminServices.allowAutoscaling(); updateFinished(confDir, tomcatDir,
   * dataDir); System.out.println("Admin with version " + getVersion() +
   * " successfully installed"); }
   */

  public abstract String getVersion();

  public abstract void configurationGenerated(PreviousAdmin prevAdmin, CurrentAdmin currAdmin) throws InstallerException;

  public abstract void updateStarted(CurrentAdmin currAdmin) throws InstallerException;

  public abstract void tomcatStopped(CurrentAdmin currAdmin) throws InstallerException;

  public abstract void tomcatStarted(CurrentAdmin currAdmin) throws InstallerException;

  public abstract void newAsReady(CurrentAdmin currAdmin) throws InstallerException;

  public abstract void updateFinished(CurrentAdmin currAdmin) throws InstallerException;

  static class Logger {
    public void print(String message) {
      System.out.print(message);
    }

    public void println() {
    }

    public void println(String message) {
      System.out.println(message);
    }

    public void timePrint() {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      Date date = new Date();
      System.out.print(dateFormat.format(date));
      System.out.print(" ");
    }

    public void timePrint(String message) {
      timePrint();
      System.out.print(message);
    }

    public void timePrintln(String message) {
      timePrint();
      System.out.println(message);
    }
  }

}
