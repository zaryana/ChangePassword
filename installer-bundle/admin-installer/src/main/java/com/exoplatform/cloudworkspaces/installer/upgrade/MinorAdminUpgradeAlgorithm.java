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

import com.exoplatform.cloudworkspaces.installer.InstallerConfiguration;
import com.exoplatform.cloudworkspaces.installer.InstallerException;
import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationManager;
import com.exoplatform.cloudworkspaces.installer.downloader.BundleDownloader;
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;
import com.exoplatform.cloudworkspaces.installer.rest.CloudAdminServices;
import com.exoplatform.cloudworkspaces.installer.tomcat.AdminTomcatWrapper;

import java.io.File;

/**
 * Just start AS with new image id wait while it became ONLINE and stop previous
 * servers
 */
public abstract class MinorAdminUpgradeAlgorithm extends AdminUpgradeAlgorithm {

  @Override
  public void upgrade(File previousConfDir,
                      File previousTomcatDir,
                      File dataDir,
                      InstallerConfiguration configuration,
                      InteractionManager interaction) throws InstallerException {
    File bundleZip = new File(previousTomcatDir.getParentFile(), "admin-bundle-" + getVersion()
        + ".zip");
    if (bundleZip.exists()) {
      if (!bundleZip.delete()) {
        throw new InstallerException("Could not delete file " + bundleZip.getAbsolutePath());
      }
    }
    BundleDownloader downloader = getBundleDownloader(configuration);
    downloader.downloadAdminTo(configuration.getProperty("bundle.url"),
                               configuration.getProperty("bundle.username"),
                               configuration.getProperty("bundle.password"),
                               bundleZip);

    AnswersManager answers = new AnswersManager();

    ConfigurationManager configurationManager = getConfigurationManager(previousConfDir,
                                                                        previousTomcatDir,
                                                                        bundleZip,
                                                                        interaction,
                                                                        answers);
    configurationManager.configure();

    CloudAdminServices cloudAdminServices = getCloudAdminServices(answers.getAnswer("tenant.masterhost"),
                                                                  "cloudadmin",
                                                                  answers.getAnswer("tomcat.users.admin.password"));
    updateStarted(previousConfDir, previousTomcatDir, dataDir, cloudAdminServices);

    AdminTomcatWrapper tomcat = getTomcatWrapper(previousTomcatDir);
    tomcat.stopTomcat();
    configurationManager.update();

    tomcatStopped(previousConfDir, previousTomcatDir, dataDir);

    tomcat.startTomcat();

    tomcatStarted(previousConfDir, previousTomcatDir, dataDir, cloudAdminServices);

    cloudAdminServices.blockAutoscaling();
    String newAlias = cloudAdminServices.serverStart(answers.getAnswer("cloud.application.default.type"));

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
    newAsReady(previousConfDir, previousTomcatDir, dataDir, cloudAdminServices);
    for (String alias : cloudAdminServices.serverStates().keySet()) {
      if (!alias.equals(newAlias)) {
        cloudAdminServices.serverStop(alias);
      }
    }
    cloudAdminServices.allowAutoscaling();

    updateFinished(previousConfDir, previousTomcatDir, dataDir, cloudAdminServices);
  }

  public abstract BundleDownloader getBundleDownloader(InstallerConfiguration configuration) throws InstallerException;

  public abstract ConfigurationManager getConfigurationManager(File previousConfDir,
                                                               File previousTomcatDir,
                                                               File bundleZip,
                                                               InteractionManager interaction,
                                                               AnswersManager answers) throws InstallerException;

  public abstract AdminTomcatWrapper getTomcatWrapper(File tomcatDir) throws InstallerException;

  public abstract CloudAdminServices getCloudAdminServices(String tenantMasterhost,
                                                           String adminUsername,
                                                           String adminPassword) throws InstallerException;

  public abstract String getVersion();

  public abstract void configurationGenerated(File previousConfDir,
                                              File confDir,
                                              File previousTomcatDir,
                                              File tomcatDir,
                                              File dataDir) throws InstallerException;

  public abstract void updateStarted(File confDir,
                                     File tomcatDir,
                                     File dataDir,
                                     CloudAdminServices cloudAdminServices) throws InstallerException;

  public abstract void tomcatStopped(File confDir, File tomcatDir, File dataDir) throws InstallerException;

  public abstract void tomcatStarted(File confDir,
                                     File tomcatDir,
                                     File dataDir,
                                     CloudAdminServices cloudAdminServices) throws InstallerException;

  public abstract void newAsReady(File confDir,
                                  File tomcatDir,
                                  File dataDir,
                                  CloudAdminServices cloudAdminServices) throws InstallerException;

  public abstract void updateFinished(File confDir,
                                      File tomcatDir,
                                      File dataDir,
                                      CloudAdminServices cloudAdminServices) throws InstallerException;

}
