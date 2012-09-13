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

import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;

public class ShParameterSource extends PropertiesParameterSource {

  public ShParameterSource(Node node) throws ConfigurationException {
    super(node);
  }

  @Override
  public void set(File confFile, String value) throws ConfigurationException {
    try {
      String maskedValue = mask.replace("{}", value);
      ConfigUtils.writeQuotedProperty(confFile, key, maskedValue);
    } catch (IOException e) {
      throw new ConfigurationException("Could not set property with key " + key + " and value "
          + value + " to file " + confFile.getAbsolutePath());
    }
  }

}
