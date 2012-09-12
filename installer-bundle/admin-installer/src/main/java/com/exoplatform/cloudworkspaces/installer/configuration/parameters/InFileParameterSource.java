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

import com.exoplatform.cloudworkspaces.installer.XmlUtils;
import com.exoplatform.cloudworkspaces.installer.configuration.AdminDirectories;
import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationException;

import org.w3c.dom.Node;

import java.io.File;

public abstract class InFileParameterSource extends ParameterSource {

  protected final String file;

  public InFileParameterSource(Node node) throws ConfigurationException {
    super(node);
    Node file = XmlUtils.getChild(node, "file");
    if (file == null)
      throw new ConfigurationException("Property file not found in source");
    this.file = file.getTextContent();
  }

  public abstract String get(File confFile) throws ConfigurationException;

  public abstract void set(File confFile, String value) throws ConfigurationException;

  @Override
  public String get(AdminDirectories directories) throws ConfigurationException {
    String file = this.file.replace("${tomcat}", directories.getTomcatDir().getAbsolutePath());
    file = file.replace("${conf}", directories.getConfDir().getAbsolutePath());
    file = file.replace("${data}", directories.getDataDir().getAbsolutePath());
    file = file.replace("//", "/");
    return get(new File(file));
  }

  @Override
  public void set(AdminDirectories directories, String value) throws ConfigurationException {
    String file = this.file.replace("${tomcat}", directories.getTomcatDir().getAbsolutePath());
    file = file.replace("${conf}", directories.getConfDir().getAbsolutePath());
    file = file.replace("${data}", directories.getDataDir().getAbsolutePath());
    file = file.replace("//", "/");
    set(new File(file), value);
  }

}
