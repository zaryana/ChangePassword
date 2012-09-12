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
import com.exoplatform.cloudworkspaces.installer.XmlUtils;
import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationException;

import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertiesParameterSource extends InFileParameterSource {

  private final String key;

  private final String mask;

  public PropertiesParameterSource(Node node) throws ConfigurationException {
    super(node);
    Node key = XmlUtils.getChild(node, "key");
    if (key == null)
      throw new ConfigurationException("Property key not found in properties source");
    this.key = key.getTextContent();
    Node mask = XmlUtils.getChild(node, "mask");
    if (mask != null)
      this.mask = mask.getTextContent();
    else
      this.mask = "{}";
  }

  @Override
  public String get(File confFile) throws ConfigurationException {
    try {
      String value = ConfigUtils.findProperty(confFile, key);
      if (value == null) {
        return null;
      }
      Pattern pattern = Pattern.compile(mask.replace("{}", "(.*)"));
      Matcher matcher = pattern.matcher(value);
      matcher.find();
      return matcher.group(1);
    } catch (IOException e) {
      throw new ConfigurationException("Could not get property with key " + key + " from file "
          + confFile.getAbsolutePath());
    }
  }

  @Override
  public void set(File confFile, String value) throws ConfigurationException {
    try {
      String maskedValue = mask.replace("{}", value);
      ConfigUtils.writeProperty(confFile, key, maskedValue);
    } catch (IOException e) {
      throw new ConfigurationException("Could not set property with key " + key + " and value "
          + value + " to file " + confFile.getAbsolutePath());
    }
  }

}
