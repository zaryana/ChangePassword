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

import com.exoplatform.cloudworkspaces.installer.versions.UpdationContainer;

import com.exoplatform.cloudworkspaces.installer.InstallerException;

import java.util.Properties;

public class VersionEntry {

  private final String     version;

  private final Properties properties;

  public VersionEntry(String version, Properties properties) {
    this.version = version;
    this.properties = properties;
  }

  public String getVersion() {
    return version;
  }

  public String getFromVersion() {
    return properties.getProperty("from.version");
  }

  public String getBundleUrl() {
    return properties.getProperty("bundle.url");
  }

  public Class<? extends UpdationContainer> getContainerClass() throws InstallerException {
    try {
      return (Class<? extends UpdationContainer>) Thread.currentThread()
                                                        .getContextClassLoader()
                                                        .loadClass(properties.getProperty("update.container"));
    } catch (ClassNotFoundException e) {
      throw new InstallerException("Class with updating container not found", e);
    }
  }

  @Override
  public int hashCode() {
    return version.hashCode();
  }

  @Override
  public boolean equals(Object q) {
    if (q == null)
      return false;
    return this.equals(((VersionEntry) q).version);
  }
}
