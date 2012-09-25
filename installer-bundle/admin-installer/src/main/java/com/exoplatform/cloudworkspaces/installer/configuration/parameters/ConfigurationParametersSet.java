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
import com.exoplatform.cloudworkspaces.installer.upgrade.VersionEntry;

import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationParametersSet {

  private Map<String, Map<String, ConfigurationParameter>> parameters;

  public ConfigurationParametersSet(VersionEntry version, Node parameters) throws InstallerException {
    this.parameters = new HashMap<String, Map<String, ConfigurationParameter>>();

    for (Node group : XmlUtils.getChildren(parameters, "group")) {
      if (version.isProfileAllowed(group)) {
        String groupName = group.getAttributes().getNamedItem("name").getTextContent();
        this.parameters.put(groupName, new HashMap<String, ConfigurationParameter>());

        for (Node curr : XmlUtils.getChildren(group, "parameter")) {
          if (version.isProfileAllowed(curr)) {
            ConfigurationParameter parameter = new ConfigurationParameter(curr);
            this.parameters.get(groupName).put(parameter.getName(), parameter);
          }
        }
      }
    }
  }

  public Map<String, Map<String, ConfigurationParameter>> getParameters() {
    return parameters;
  }

  public ConfigurationParameter get(String key) {
    for (Map<String, ConfigurationParameter> group : parameters.values()) {
      if (group.containsKey(key)) {
        return group.get(key);
      }
    }
    return null;
  }

}
