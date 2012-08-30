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
import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationManager;
import com.exoplatform.cloudworkspaces.installer.downloader.BundleDownloader;
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;
import com.exoplatform.cloudworkspaces.installer.rest.AdminException;
import com.exoplatform.cloudworkspaces.installer.rest.CloudAdminServices;
import com.exoplatform.cloudworkspaces.installer.tomcat.AdminTomcatWrapper;

import java.io.File;

/**
 * Just start AS with new image id wait while it became ONLINE and stop previous
 * servers
 */
public abstract class MinorAdminUpgradeAlgorithm extends AdminUpgradeAlgorithm {

  protected final InteractionManager   interaction;

  protected final AnswersManager       answers;

  protected final CloudAdminServices   cloudAdminServices;

  protected final BundleDownloader     bundleDownloader;

  protected final AdminTomcatWrapper   tomcat;

  protected final ConfigurationManager configurationManager;

  public MinorAdminUpgradeAlgorithm(InteractionManager interaction,
                                    AnswersManager answers,
                                    CloudAdminServices cloudAdminServices,
                                    BundleDownloader bundleDownloader,
                                    AdminTomcatWrapper tomcat,
                                    ConfigurationManager configurationManager) {
    this.interaction = interaction;
    this.answers = answers;
    this.cloudAdminServices = cloudAdminServices;
    this.bundleDownloader = bundleDownloader;
    this.tomcat = tomcat;
    this.configurationManager = configurationManager;
  }

  @Override
  public void upgrade(File previousConfDir, File previousTomcatDir, File dataDir) throws InstallerException {
    File bundleZip = new File(previousTomcatDir.getParentFile(), "admin-bundle-" + getVersion()
        + ".zip");
    if (bundleZip.exists()) {
      if (!bundleZip.delete()) {
        throw new InstallerException("Could not delete file " + bundleZip.getAbsolutePath());
      }
    }
    System.out.println("Downloading new admin bundle...");
    bundleDownloader.downloadAdminTo(bundleZip);

    configurationManager.bindTo(previousConfDir, previousTomcatDir, bundleZip);

    System.out.println("Start configuring admin...");
    configurationManager.configure();

    cloudAdminServices.bindTo(answers.getAnswer("tenant.masterhost"),
                              "cloudadmin",
                              answers.getAnswer("tomcat.users.admin.password"));
    updateStarted(previousConfDir, previousTomcatDir, dataDir);

    tomcat.bindTo(previousTomcatDir);

    System.out.println("Stopping admin tomcat...");
    tomcat.stopTomcat();
    System.out.println("Replacing old admin with new bundle...");
    configurationManager.update();

    tomcatStopped(previousConfDir, previousTomcatDir, dataDir);

    System.out.println("Starting admin tomcat...");
    tomcat.startTomcat();

    tomcatStarted(previousConfDir, previousTomcatDir, dataDir);

    boolean z = false;
    while (!z) {
      try {
        cloudAdminServices.serverStates();
        z = true;
      } catch (AdminException e) {
        z = false;
      }
    }
    System.out.println("Tomcat started. Starting new AS...");

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
    System.out.println("New AS ready. Stopping previous AS...");
    newAsReady(previousConfDir, previousTomcatDir, dataDir);
    for (String alias : cloudAdminServices.serverStates().keySet()) {
      if (!alias.equals(newAlias)) {
        System.out.println("Stopping " + alias + "...");
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
        System.out.println(alias + " stopped");
      }
    }
    cloudAdminServices.allowAutoscaling();

    updateFinished(previousConfDir, previousTomcatDir, dataDir);
    System.out.println("Updation admin to " + getVersion() + " finished successfully");
  }

  @Override
  public void install(File confDir, File tomcatDir, File dataDir) throws InstallerException {
    File bundleZip = new File(tomcatDir.getParentFile(), "admin-bundle-" + getVersion() + ".zip");
    if (bundleZip.exists()) {
      if (!bundleZip.delete()) {
        throw new InstallerException("Could not delete file " + bundleZip.getAbsolutePath());
      }
    }
    bundleDownloader.downloadAdminTo(bundleZip);

    configurationManager.bindTo(confDir, tomcatDir, bundleZip);

    configurationManager.configure();

    cloudAdminServices.bindTo(answers.getAnswer("tenant.masterhost"),
                              "cloudadmin",
                              answers.getAnswer("tomcat.users.admin.password"));
    updateStarted(confDir, tomcatDir, dataDir);

    tomcat.stopTomcat();
    configurationManager.update();

    tomcat.startTomcat();

    tomcatStarted(confDir, tomcatDir, dataDir);

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
    newAsReady(confDir, tomcatDir, dataDir);
    cloudAdminServices.allowAutoscaling();

    updateFinished(confDir, tomcatDir, dataDir);
  }

  public abstract String getVersion();

  public abstract void configurationGenerated(File previousConfDir,
                                              File confDir,
                                              File previousTomcatDir,
                                              File tomcatDir,
                                              File dataDir) throws InstallerException;

  public abstract void updateStarted(File confDir, File tomcatDir, File dataDir) throws InstallerException;

  public abstract void tomcatStopped(File confDir, File tomcatDir, File dataDir) throws InstallerException;

  public abstract void tomcatStarted(File confDir, File tomcatDir, File dataDir) throws InstallerException;

  public abstract void newAsReady(File confDir, File tomcatDir, File dataDir) throws InstallerException;

  public abstract void updateFinished(File confDir, File tomcatDir, File dataDir) throws InstallerException;

}
