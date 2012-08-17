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
package com.exoplatform.cloudworkspaces.installer.configuration.versions;

import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.AwsConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.CloudConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.DBConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.InstanceConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.MailConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.ServerConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.TomcatUsersConfigurationUpdater;

import java.util.ArrayList;
import java.util.List;

public class Beta06ConfigurationPack extends ConfigurationPack {

  @Override
  public List<ConfigurationUpdater> getUpdaters() {
    List<ConfigurationUpdater> updaters = new ArrayList<ConfigurationUpdater>();
    updaters.add(new DBConfigurationUpdater());
    updaters.add(new CloudConfigurationUpdater());
    updaters.add(new TomcatUsersConfigurationUpdater());
    updaters.add(new MailConfigurationUpdater());
    updaters.add(new AwsConfigurationUpdater());
    updaters.add(new InstanceConfigurationUpdater());
    updaters.add(new ServerConfigurationUpdater());
    return updaters;
  }

}
