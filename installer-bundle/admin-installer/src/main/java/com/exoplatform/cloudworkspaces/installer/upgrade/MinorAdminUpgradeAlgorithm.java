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
import com.exoplatform.cloudworkspaces.installer.upgrade.Hook.BeforeConfigurationGeneratingHook;
import com.exoplatform.cloudworkspaces.installer.upgrade.Hook.ConfigurationGeneratedHook;
import com.exoplatform.cloudworkspaces.installer.upgrade.Hook.NewAsReadyHook;
import com.exoplatform.cloudworkspaces.installer.upgrade.Hook.TomcatStartedHook;
import com.exoplatform.cloudworkspaces.installer.upgrade.Hook.TomcatStoppedHook;
import com.exoplatform.cloudworkspaces.installer.upgrade.Hook.UpdateFinishedHook;
import com.exoplatform.cloudworkspaces.installer.upgrade.Hook.UpdateStartedHook;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Just start AS with new image id wait while it became ONLINE and stop previous
 * servers
 */
public class MinorAdminUpgradeAlgorithm extends AdminUpgradeAlgorithm {

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
    Logger.timePrintln("Start configuring admin...");

    beforeConfigurationGenerating(prevAdmin, currAdmin);

    configurationManager.configure();

    configurationGenerated(prevAdmin, currAdmin);

    updateStarted(currAdmin);

    Logger.timePrintln("Stopping admin tomcat...");
    prevAdmin.getAdminTomcatWrapper().stopTomcat();
    Logger.timePrintln("Replacing old admin with new bundle...");

    currAdmin.getAdminDirectories().moveTo(toDirs);

    tomcatStopped(currAdmin);

    Logger.timePrintln("Starting admin tomcat...");
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
    Logger.timePrintln("Tomcat started. Starting new AS...");

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
    Logger.timePrintln("New AS ready. Stopping previous AS...");
    newAsReady(currAdmin);
    for (String alias : cloudAdminServices.serverStates().keySet()) {
      if (!alias.equals(newAlias)) {
        Logger.print("   ");
        Logger.timePrint("stopping " + alias + "...   ");
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
        Logger.println("stopped");
      }
    }
    cloudAdminServices.allowAutoscaling();

    if (isClearTenants) {
      Logger.println();
      Logger.print("Are you sure you want to delete all tenants? (yes or no): ");
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
        Logger.print("Please, print yes or no: ");
      }
      if (mustDelete) {
        Logger.timePrintln("Clearing all tenants");
        for (String tenant : cloudAdminServices.tenantList()) {
          Logger.print("   ");
          Logger.timePrint("deleting tenant " + tenant + "...    ");
          try {
            removeTenant(tenant, cloudAdminServices);
            Logger.println("successfull");
          } catch (AdminException e) {
            Logger.println("failed");
          }
        }
      }
    }

    updateFinished(currAdmin);
    Logger.timePrintln("Admin successfully upgraded to " + currAdmin.getVersionEntry().getVersion());
  }

  protected void removeTenant(String tenant, CloudAdminServices cloudAdminServices) throws AdminException {
    Map<String, String> status = cloudAdminServices.tenantStatus(tenant);
    if (!status.isEmpty()) {
      if (status.get("state").equals("ONLINE"))
        cloudAdminServices.tenantStop(tenant);
      cloudAdminServices.tenantRemove(tenant);
    }
  }

  @Override
  public void install(AdminDirectories toDirs) throws InstallerException {
    Logger.timePrintln("Start configuring admin...");

    beforeConfigurationGenerating(prevAdmin, currAdmin);

    configurationManager.configure();

    configurationGenerated(prevAdmin, currAdmin);

    updateStarted(currAdmin);

    currAdmin.getAdminDirectories().moveTo(toDirs);

    tomcatStopped(currAdmin);

    Logger.timePrintln("Starting admin tomcat...");
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
    Logger.timePrintln("Tomcat started. Starting new AS...");

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
    Logger.timePrintln("New AS ready. Stopping previous AS...");
    newAsReady(currAdmin);
    for (String alias : cloudAdminServices.serverStates().keySet()) {
      if (!alias.equals(newAlias)) {
        Logger.print("   ");
        Logger.timePrint("stopping " + alias + "...   ");
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
        Logger.println("stopped");
      }
    }
    cloudAdminServices.allowAutoscaling();

    updateFinished(currAdmin);
    Logger.timePrintln("Admin successfully installed with version "
        + currAdmin.getVersionEntry().getVersion());
  }

  public void beforeConfigurationGenerating(PreviousAdmin prevAdmin, CurrentAdmin currAdmin) throws InstallerException {
    List<Object> hooks = currAdmin.getVersionEntry()
                                  .getUpdationAlgorithmConfiguration()
                                  .getHooks("before-configuration-generating");
    for (Object hook : hooks) {
      ((BeforeConfigurationGeneratingHook) hook).beforeConfigurationGenerating(prevAdmin,
                                                                               currAdmin,
                                                                               interaction);
    }
  }

  public void configurationGenerated(PreviousAdmin prevAdmin, CurrentAdmin currAdmin) throws InstallerException {
    List<Object> hooks = currAdmin.getVersionEntry()
                                  .getUpdationAlgorithmConfiguration()
                                  .getHooks("configuration-generated");
    for (Object hook : hooks) {
      ((ConfigurationGeneratedHook) hook).configurationGenerated(prevAdmin, currAdmin);
    }
  }

  public void updateStarted(CurrentAdmin currAdmin) throws InstallerException {
    List<Object> hooks = currAdmin.getVersionEntry()
                                  .getUpdationAlgorithmConfiguration()
                                  .getHooks("update-started");
    for (Object hook : hooks) {
      ((UpdateStartedHook) hook).updateStarted(currAdmin);
    }
  }

  public void tomcatStopped(CurrentAdmin currAdmin) throws InstallerException {
    List<Object> hooks = currAdmin.getVersionEntry()
                                  .getUpdationAlgorithmConfiguration()
                                  .getHooks("tomcat-stopped");
    for (Object hook : hooks) {
      ((TomcatStoppedHook) hook).tomcatStopped(currAdmin);
    }
  }

  public void tomcatStarted(CurrentAdmin currAdmin) throws InstallerException {
    List<Object> hooks = currAdmin.getVersionEntry()
                                  .getUpdationAlgorithmConfiguration()
                                  .getHooks("tomcat-started");
    for (Object hook : hooks) {
      ((TomcatStartedHook) hook).tomcatStarted(currAdmin);
    }
  }

  public void newAsReady(CurrentAdmin currAdmin) throws InstallerException {
    List<Object> hooks = currAdmin.getVersionEntry()
                                  .getUpdationAlgorithmConfiguration()
                                  .getHooks("new-as-ready");
    for (Object hook : hooks) {
      ((NewAsReadyHook) hook).newAsReady(currAdmin);
    }
  }

  public void updateFinished(CurrentAdmin currAdmin) throws InstallerException {
    List<Object> hooks = currAdmin.getVersionEntry()
                                  .getUpdationAlgorithmConfiguration()
                                  .getHooks("update-finished");
    for (Object hook : hooks) {
      ((UpdateFinishedHook) hook).updateFinished(currAdmin);
    }
  }

}
