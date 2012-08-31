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
import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationManager;
import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.AwsConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.BackupConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.CloudConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.DBConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.InstanceConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.MailConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.ServerConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.TomcatUsersConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.downloader.BundleDownloader;
import com.exoplatform.cloudworkspaces.installer.downloader.IntranetBundleDownloader;
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;
import com.exoplatform.cloudworkspaces.installer.rest.CloudAdminServices;
import com.exoplatform.cloudworkspaces.installer.rest.M10CloudAdminServices;
import com.exoplatform.cloudworkspaces.installer.tomcat.AdminTomcatWrapper;
import com.exoplatform.cloudworkspaces.installer.tomcat.AdminTomcatWrapperImpl;
import com.exoplatform.cloudworkspaces.installer.upgrade.AdminUpgradeAlgorithm;
import com.exoplatform.cloudworkspaces.installer.upgrade.Beta08AdminUpgradeAlgorithm;
import com.exoplatform.cloudworkspaces.installer.upgrade.VersionEntry;

import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.PicoContainer;

import java.util.ArrayList;
import java.util.List;

public class Beta08UpdationContainer extends UpdationContainer {

  @Override
  public PicoContainer getContainer(VersionEntry version,
                                    InteractionManager interaction,
                                    AnswersManager answers) throws InstallerException {
    DefaultPicoContainer container = new DefaultPicoContainer();
    container.addComponent(InteractionManager.class, interaction);
    container.addComponent(AnswersManager.class, answers);
    container.addComponent(CloudAdminServices.class, M10CloudAdminServices.class);
    /*
     * container.addComponent(BundleDownloader.class, new
     * FromFileBundleDownloader(new File(version.getBundleUrl())));
     */
    container.addComponent(BundleDownloader.class,
                           new IntranetBundleDownloader(version.getBundleUrl()));

    container.addComponent(AdminTomcatWrapper.class, AdminTomcatWrapperImpl.class);

    List<ConfigurationUpdater> updaters = new ArrayList<ConfigurationUpdater>();
    updaters.add(new DBConfigurationUpdater());
    updaters.add(new CloudConfigurationUpdater());
    updaters.add(new TomcatUsersConfigurationUpdater());
    updaters.add(new MailConfigurationUpdater());
    updaters.add(new AwsConfigurationUpdater());
    updaters.add(new InstanceConfigurationUpdater());
    updaters.add(new ServerConfigurationUpdater());
    updaters.add(new BackupConfigurationUpdater());
    ConfigurationManager configurationManager = new ConfigurationManager(updaters,
                                                                         interaction,
                                                                         answers);
    container.addComponent(ConfigurationManager.class, configurationManager);

    container.addComponent(AdminUpgradeAlgorithm.class, Beta08AdminUpgradeAlgorithm.class);
    return container;
  }

}
