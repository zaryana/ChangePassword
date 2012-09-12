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

import org.w3c.dom.Node;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ParameterSourceFactory {

  private static final Map<String, Class<? extends ParameterSource>> sources = new HashMap<String, Class<? extends ParameterSource>>();

  static {
    sources.put("properties", PropertiesParameterSource.class);
    sources.put("xml", XmlParameterSource.class);
  }

  public static ParameterSource getSource(Node source) throws InstallerException {
    String type = source.getAttributes().getNamedItem("type").getTextContent();
    try {
      if (!sources.containsKey(type))
        throw new InstallerException("Source type '" + type + "' not found");
      return sources.get(type).getConstructor(Node.class).newInstance(source);
    } catch (IllegalArgumentException e) {
      throw new InstallerException("Error happened while getting ParameterSource with key " + type);
    } catch (SecurityException e) {
      throw new InstallerException("Error happened while getting ParameterSource with key " + type);
    } catch (InstantiationException e) {
      throw new InstallerException("Error happened while getting ParameterSource with key " + type);
    } catch (IllegalAccessException e) {
      throw new InstallerException("Error happened while getting ParameterSource with key " + type);
    } catch (InvocationTargetException e) {
      throw new InstallerException("Error happened while getting ParameterSource with key " + type);
    } catch (NoSuchMethodException e) {
      throw new InstallerException("Error happened while getting ParameterSource with key " + type);
    }
  }

}
