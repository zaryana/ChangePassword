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

import com.exoplatform.cloudworkspaces.installer.InstallerException;
import com.exoplatform.cloudworkspaces.installer.XmlUtils;
import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.parameters.ConfigurationParametersSet;
import com.exoplatform.cloudworkspaces.installer.rest.CloudAdminServices;
import com.exoplatform.cloudworkspaces.installer.tomcat.AdminTomcatWrapper;
import com.exoplatform.cloudworkspaces.installer.versions.UpdationContainer;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class VersionEntry {

  private final String   version;

  private final Node     versionNode;

  private final Document xml;

  private final XPath    xPath;

  public VersionEntry(String version, InputStream stream) throws InstallerException {
    this.version = version;
    try {
      this.xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
      this.xPath = XPathFactory.newInstance().newXPath();
      this.versionNode = XmlUtils.getChild(xml, "version");
      if (versionNode == null)
        throw new InstallerException("Node with name 'version' not found for version " + version);
    } catch (SAXException e) {
      throw new InstallerException("Couldn't parse configuration file for version " + version, e);
    } catch (IOException e) {
      throw new InstallerException("Couldn't parse configuration file for version " + version, e);
    } catch (ParserConfigurationException e) {
      throw new InstallerException("Couldn't parse configuration file for version " + version, e);
    }
  }

  public String getVersion() {
    return version;
  }

  public String getFromVersion() throws InstallerException {
    return evaluateXPath("/version/from-version");
  }

  public String getBundleUrl() throws InstallerException {
    return evaluateXPath("/version/download/url");
  }

  public Class<? extends UpdationContainer> getContainerClass() throws InstallerException {
    try {
      return (Class<? extends UpdationContainer>) Thread.currentThread()
                                                        .getContextClassLoader()
                                                        .loadClass(evaluateXPath("/version/container"));
    } catch (ClassNotFoundException e) {
      throw new InstallerException("Class with updating container not found", e);
    }
  }

  public Class<? extends CloudAdminServices> getCloudAdminServicesImpl() throws InstallerException {
    try {
      return (Class<? extends CloudAdminServices>) Thread.currentThread()
                                                         .getContextClassLoader()
                                                         .loadClass(evaluateXPath("/version/admin/rest"));
    } catch (ClassNotFoundException e) {
      throw new InstallerException("Implementation of CloudAdminServices not found", e);
    }
  }

  public Class<? extends AdminTomcatWrapper> getAdminTomcatWrapperImpl() throws InstallerException {
    try {
      return (Class<? extends AdminTomcatWrapper>) Thread.currentThread()
                                                         .getContextClassLoader()
                                                         .loadClass(evaluateXPath("/version/admin/tomcat"));
    } catch (ClassNotFoundException e) {
      throw new InstallerException("Implementation of AdminTomcatWrapper not found", e);
    }
  }

  private String evaluateXPath(String xpath) throws InstallerException {
    try {
      XPathExpression expression = xPath.compile(xpath);
      return expression.evaluate(xml);
    } catch (XPathExpressionException e) {
      throw new InstallerException("Couldn't get " + xpath + " property from versions file");
    }
  }

  public UpdationAlgorithmConfiguration getUpdationAlgorithmConfiguration() {
    return new UpdationAlgorithmConfiguration(XmlUtils.getChild(versionNode, "updation"));
  }

  public ConfigurationParametersSet getConfigurationParameters() throws InstallerException {
    Node parameters = XmlUtils.getChild(versionNode, "configuration", "parameters");

    ConfigurationParametersSet result = new ConfigurationParametersSet(parameters);
    return result;
  }

  public List<ConfigurationUpdater> getConfigurationUpdaters() throws InstallerException {
    ArrayList<ConfigurationUpdater> updaters = new ArrayList<ConfigurationUpdater>();
    for (Node updater : XmlUtils.getChildren(XmlUtils.getChild(versionNode,
                                                               "configuration",
                                                               "updaters"), "updater")) {
      String clazz = updater.getTextContent();
      try {
        updaters.add((ConfigurationUpdater) Thread.currentThread()
                                                  .getContextClassLoader()
                                                  .loadClass(clazz)
                                                  .newInstance());
      } catch (InstantiationException e) {
        throw new InstallerException("Couldn't used configuration updater " + clazz);
      } catch (IllegalAccessException e) {
        throw new InstallerException("Couldn't used configuration updater " + clazz);
      } catch (ClassNotFoundException e) {
        throw new InstallerException("Couldn't used configuration updater " + clazz);
      }
    }
    return updaters;
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
