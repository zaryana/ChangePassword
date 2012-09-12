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

import com.exoplatform.cloudworkspaces.installer.InstallerException;
import com.exoplatform.cloudworkspaces.installer.XmlUtils;
import com.exoplatform.cloudworkspaces.installer.configuration.AdminDirectories;
import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationException;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationParameter {

  private final String                name;

  private final String                defaults;

  private final List<ParameterSource> sources = new ArrayList<ParameterSource>();

  public ConfigurationParameter(Node xmlNode) throws InstallerException {
    Node name = XmlUtils.getChild(xmlNode, "name");
    if (name == null)
      throw new ConfigurationException("Property name not found in parameter configuration");
    this.name = name.getTextContent();
    Node defaults = XmlUtils.getChild(xmlNode, "default");
    if (defaults != null)
      this.defaults = defaults.getTextContent();
    else
      this.defaults = null;

    for (Node source : XmlUtils.getChildren(XmlUtils.getChild(xmlNode, "sources"), "source")) {
      this.sources.add(ParameterSourceFactory.getSource(source));
    }
  }

  public String getName() {
    return name;
  }

  public String getDefault() {
    return defaults;
  }

  public String get(AdminDirectories directories) throws ConfigurationException {
    return sources.get(0).get(directories);
  }

  public void set(AdminDirectories directories, String value) throws ConfigurationException {
    for (ParameterSource source : sources) {
      source.set(directories, value);
    }
  }

}
