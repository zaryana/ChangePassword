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

import java.util.HashMap;
import java.util.Map;

public class ConfigurationParametersSet {

  private Map<String, Map<String, ConfigurationParameter>> parameters;

  public ConfigurationParametersSet() {
    this.parameters = new HashMap<String, Map<String, ConfigurationParameter>>();
  }

  void addParameter(String container, String key, ConfigurationParameter parameter) {
    if (!parameters.containsKey(container))
      parameters.put(container, new HashMap<String, ConfigurationParameter>());
    parameters.get(container).put(key, parameter);
  }

  public Map<String, Map<String, ConfigurationParameter>> getParameters() {
    return parameters;
  }

  public ConfigurationParameter get(String type) {
    for (Map<String, ConfigurationParameter> container : parameters.values()) {
      if (container.containsKey(type)) {
        return container.get(type);
      }
    }
    return null;
  }

}
