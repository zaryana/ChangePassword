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

public class TomcatUserConfigurationParameter implements ConfigurationParameter {

  protected final String username;

  protected final String name;

  protected final String defaults;

  public TomcatUserConfigurationParameter(String username, String defaults) {
    this.username = username;
    this.name = "tomcat.users." + username + ".password";
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
    try {
      return ConfigUtils.find(tomcatDir, "conf/tomcat-users.xml", "<user username=\"" + username
          + "\" password=\"([^\"]*)\"");
    } catch (IOException e) {
      throw new ConfigurationException("Could not get password for user " + username
          + " from file " + new File(tomcatDir, "conf/tomcat-users.xml").getAbsolutePath());
    }
  }

  @Override
  public void set(File tomcatDir, File confDir, File dataDir, String value) throws ConfigurationException {
    try {
      ConfigUtils.replace(tomcatDir, "conf/tomcat-users.xml", "<user username=\"" + username
          + "\" password=\"([^\"]*)\"", "<user username=\"" + username + "\" password=\"" + value
          + "\"");
    } catch (IOException e) {
      throw new ConfigurationException("Could not set password for user " + username + " to file "
          + new File(tomcatDir, "conf/tomcat-users.xml").getAbsolutePath());
    }
  }
}
