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
package com.exoplatform.cloudworkspaces.installer.configuration;

import com.exoplatform.cloudworkspaces.installer.configuration.parameters.ConfigurationParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminConfiguration {

  protected final AdminDirectories                    directories;

  protected final Map<String, ConfigurationParameter> parameters;

  public AdminConfiguration(AdminDirectories directories, List<ConfigurationParameter> parameterList) {
    this.directories = directories;
    this.parameters = new HashMap<String, ConfigurationParameter>();
    for (ConfigurationParameter parameter : parameterList) {
      parameters.put(parameter.getName(), parameter);
    }
  }

  public String get(String key) throws ConfigurationException {
    return get(parameters.get(key));
  }

  public String get(ConfigurationParameter key) throws ConfigurationException {
    return key.get(directories.getTomcatDir(), directories.getConfDir(), directories.getDataDir());
  }

  public void set(String key, String value) throws ConfigurationException {
    set(parameters.get(key), value);
  }

  public void set(ConfigurationParameter key, String value) throws ConfigurationException {
    key.set(directories.getTomcatDir(), directories.getConfDir(), directories.getDataDir(), value);
  }

}
