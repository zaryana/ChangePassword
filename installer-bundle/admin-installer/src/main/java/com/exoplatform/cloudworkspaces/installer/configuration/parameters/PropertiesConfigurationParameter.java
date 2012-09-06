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
package com.exoplatform.cloudworkspaces.installer.configuration.parameters;

import com.exoplatform.cloudworkspaces.installer.ConfigUtils;
import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PropertiesConfigurationParameter implements ConfigurationParameter {

  protected final String               name;

  protected final String               defaults;

  protected final List<ParameterEntry> parameters;

  public PropertiesConfigurationParameter(String file, String key, String defaults) {
    this.parameters = new ArrayList<ParameterEntry>();
    parameters.add(new ParameterEntry(file, key));
    this.name = key;
    this.defaults = defaults;
  }

  public PropertiesConfigurationParameter(String file1,
                                          String key1,
                                          String file2,
                                          String key2,
                                          String defaults) {
    this.parameters = new ArrayList<ParameterEntry>();
    parameters.add(new ParameterEntry(file1, key1));
    parameters.add(new ParameterEntry(file2, key2));
    this.name = key1;
    this.defaults = defaults;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDefault() {
    return defaults;
  }

  @Override
  public String get(File tomcatDir, File confDir, File dataDir) throws ConfigurationException {
    ParameterEntry param = parameters.get(0);
    try {
      return ConfigUtils.findProperty(confDir, param.file, param.key);
    } catch (IOException e) {
      throw new ConfigurationException("Could not get property with key " + param.key
          + " from file " + new File(confDir, param.file).getAbsolutePath());
    }
  }

  @Override
  public void set(File tomcatDir, File confDir, File dataDir, String value) throws ConfigurationException {
    for (ParameterEntry param : parameters) {
      try {
        ConfigUtils.writeProperty(confDir, param.file, param.key, value);
      } catch (IOException e) {
        throw new ConfigurationException("Could not set property with key " + param.key
            + " and value " + value + " to file " + new File(confDir, param.file).getAbsolutePath());
      }
    }
  }

  public void addSource(String file, String key) {
    parameters.add(new ParameterEntry(file, key));
  }

  private static class ParameterEntry {

    final String file;

    final String key;

    public ParameterEntry(String file, String key) {
      this.file = file;
      this.key = key;
    }

  }

}
