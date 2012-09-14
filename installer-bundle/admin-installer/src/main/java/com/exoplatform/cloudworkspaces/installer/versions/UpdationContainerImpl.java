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
import com.exoplatform.cloudworkspaces.installer.configuration.Admin;
import com.exoplatform.cloudworkspaces.installer.configuration.AdminConfigurationManager;
import com.exoplatform.cloudworkspaces.installer.configuration.AdminDirectories;
import com.exoplatform.cloudworkspaces.installer.configuration.CurrentAdmin;
import com.exoplatform.cloudworkspaces.installer.configuration.PreviousAdmin;
import com.exoplatform.cloudworkspaces.installer.downloader.BundleDownloader;
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;
import com.exoplatform.cloudworkspaces.installer.upgrade.AdminUpgradeAlgorithm;
import com.exoplatform.cloudworkspaces.installer.upgrade.VersionEntry;

import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.PicoContainer;

import java.io.File;
import java.io.IOException;

public class UpdationContainerImpl extends UpdationContainer {

  @Override
  public PicoContainer getContainer(AdminDirectories prevAdminDirs,
                                    AdminDirectories nextAdminDirs,
                                    VersionEntry prevVersion,
                                    VersionEntry nextVersion,
                                    BundleDownloader downloader,
                                    InteractionManager interaction,
                                    AnswersManager answers) throws InstallerException {

    File bundleZip = new File(nextAdminDirs.getTomcatDir().getAbsolutePath() + ".zip");
    if (bundleZip.exists()) {
      if (!bundleZip.delete()) {
        throw new InstallerException("Could not delete file " + bundleZip.getAbsolutePath());
      }
    }
    System.out.println("Downloading new admin bundle...");
    downloader.downloadAdminTo(bundleZip);

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
    bundleZip.deleteOnExit();

    PreviousAdmin prevAdmin = new Admin(prevAdminDirs, prevVersion);
    CurrentAdmin currAdmin = new Admin(currAdminDirs, nextVersion);

    DefaultPicoContainer container = new DefaultPicoContainer();
    container.addComponent(InteractionManager.class, interaction);
    container.addComponent(AnswersManager.class, answers);
    container.addComponent(PreviousAdmin.class, prevAdmin);
    container.addComponent(CurrentAdmin.class, currAdmin);

    AdminConfigurationManager configurationManager = new AdminConfigurationManager(prevAdmin,
                                                                                   currAdmin,
                                                                                   nextVersion.getConfigurationUpdaters(),
                                                                                   interaction,
                                                                                   answers);
    container.addComponent(AdminConfigurationManager.class, configurationManager);

    container.addComponent(AdminUpgradeAlgorithm.class,
                           nextVersion.getUpdationAlgorithmConfiguration().getAlgorithmClass());
    return container;
  }

}
