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

import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationManager;
import com.exoplatform.cloudworkspaces.installer.downloader.BundleDownloader;
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;
import com.exoplatform.cloudworkspaces.installer.rest.CloudAdminServices;
import com.exoplatform.cloudworkspaces.installer.tomcat.AdminTomcatWrapper;

public class Beta08AdminUpgradeAlgorithm extends Beta07AdminUpgradeAlgorithm {

  public Beta08AdminUpgradeAlgorithm(InteractionManager interaction,
                                     AnswersManager answers,
                                     CloudAdminServices cloudAdminServices,
                                     BundleDownloader bundleDownloader,
                                     AdminTomcatWrapper tomcat,
                                     ConfigurationManager configurationManager) {
    super(interaction, answers, cloudAdminServices, bundleDownloader, tomcat, configurationManager);
  }

  @Override
  public String getVersion() {
    return "1.1.0-Beta08";
  }

}
