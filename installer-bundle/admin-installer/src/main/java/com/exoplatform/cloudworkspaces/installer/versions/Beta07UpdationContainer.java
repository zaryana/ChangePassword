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
package com.exoplatform.cloudworkspaces.installer.versions;

import com.exoplatform.cloudworkspaces.installer.InstallerException;
import com.exoplatform.cloudworkspaces.installer.configuration.AdminConfigurationManager;
import com.exoplatform.cloudworkspaces.installer.configuration.AdminDirectories;
import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.CurrentAdmin;
import com.exoplatform.cloudworkspaces.installer.configuration.PreviousAdmin;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.AwsConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.BackupConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.CloudConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.DBConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.InstanceConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.MailConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.ServerConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.TomcatUsersConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.downloader.IntranetBundleDownloader;
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;
import com.exoplatform.cloudworkspaces.installer.upgrade.AdminUpgradeAlgorithm;
import com.exoplatform.cloudworkspaces.installer.upgrade.Beta07AdminUpgradeAlgorithm;
import com.exoplatform.cloudworkspaces.installer.upgrade.VersionEntry;

import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.PicoContainer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Beta07UpdationContainer extends UpdationContainer {

  @Override
  public PicoContainer getContainer(AdminDirectories prevAdminDirs,
                                    AdminDirectories nextAdminDirs,
                                    VersionEntry version,
                                    InteractionManager interaction,
                                    AnswersManager answers) throws InstallerException {

    File bundleZip = new File(nextAdminDirs.getTomcatDir().getAbsolutePath() + ".zip");
    if (bundleZip.exists()) {
      if (!bundleZip.delete()) {
        throw new InstallerException("Could not delete file " + bundleZip.getAbsolutePath());
      }
    }
    System.out.println("Downloading new admin bundle...");
    IntranetBundleDownloader bundleDownloader = new IntranetBundleDownloader(version.getBundleUrl());
    // FromFileBundleDownloader bundleDownloader = new
    // FromFileBundleDownloader(new File(version.getBundleUrl()));
    bundleDownloader.downloadAdminTo(bundleZip);

    AdminDirectories currAdminDirs;
    try {
      currAdminDirs = AdminDirectories.createFromBundle(bundleZip,
                                                        new File(nextAdminDirs.getTomcatDir()
                                                                              .getAbsolutePath()
                                                            + ".new"));
    } catch (IOException e) {
      throw new InstallerException("Error while unzipping admin bundle", e);
    } catch (InterruptedException e) {
      throw new InstallerException("Error while unzipping admin bundle", e);
    }

    PreviousAdmin prevAdmin = getPreviousAdmin(prevAdminDirs);
    CurrentAdmin currAdmin = getCurrentAdmin(currAdminDirs);

    DefaultPicoContainer container = new DefaultPicoContainer();
    container.addComponent(InteractionManager.class, interaction);
    container.addComponent(AnswersManager.class, answers);
    container.addComponent(PreviousAdmin.class, prevAdmin);
    container.addComponent(CurrentAdmin.class, currAdmin);

    AdminConfigurationManager configurationManager = new AdminConfigurationManager(prevAdmin,
                                                                                   currAdmin,
                                                                                   getConfigurationUpdaters(),
                                                                                   interaction,
                                                                                   answers);
    container.addComponent(AdminConfigurationManager.class, configurationManager);

    container.addComponent(AdminUpgradeAlgorithm.class, getUpgradeAlgorithm());
    return container;
  }

  public PreviousAdmin getPreviousAdmin(AdminDirectories prevAdminDirs) {
    return new Beta07Admin(prevAdminDirs);
  }

  public CurrentAdmin getCurrentAdmin(AdminDirectories currAdminDirs) {
    return new Beta07Admin(currAdminDirs);
  }

  public List<ConfigurationUpdater> getConfigurationUpdaters() {
    List<ConfigurationUpdater> updaters = new ArrayList<ConfigurationUpdater>();
    updaters.add(new DBConfigurationUpdater());
    updaters.add(new CloudConfigurationUpdater());
    updaters.add(new TomcatUsersConfigurationUpdater());
    updaters.add(new MailConfigurationUpdater());
    updaters.add(new AwsConfigurationUpdater());
    updaters.add(new InstanceConfigurationUpdater());
    updaters.add(new ServerConfigurationUpdater());
    updaters.add(new BackupConfigurationUpdater());
    return updaters;
  }

  public Class<? extends AdminUpgradeAlgorithm> getUpgradeAlgorithm() {
    return Beta07AdminUpgradeAlgorithm.class;
  }

}
