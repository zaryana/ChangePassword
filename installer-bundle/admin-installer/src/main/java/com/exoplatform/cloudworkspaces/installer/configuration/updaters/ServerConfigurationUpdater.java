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
package com.exoplatform.cloudworkspaces.installer.configuration.updaters;

import com.exoplatform.cloudworkspaces.installer.FileUtils;
import com.exoplatform.cloudworkspaces.installer.configuration.BaseConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationException;
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;

import java.io.File;
import java.io.IOException;

public class ServerConfigurationUpdater extends BaseConfigurationUpdater {

  @Override
  public void update(File confDir,
                     File tomcatDir,
                     File previousConfDir,
                     File previousTomcatDir,
                     InteractionManager interaction,
                     AnswersManager answers) throws ConfigurationException {
    try {
      File applicationServers = new File(confDir, "application-servers");
      if (!applicationServers.exists())
        if (!applicationServers.mkdirs())
          throw new IOException("Could not create application-servers directory");
      File previousApplicationServers = new File(previousConfDir, "application-servers");
      if (previousApplicationServers.exists() && previousApplicationServers.isDirectory()) {
        for (File server : previousApplicationServers.listFiles()) {
          File current = new File(applicationServers, server.getName());
          FileUtils.copyFile(server, current);
        }
      }
    } catch (IOException e) {
      throw new ConfigurationException(e);
    }
  }

}
