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
import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class XmlParameterSource extends InFileParameterSource {

  private final String xpath;

  public XmlParameterSource(Node node) throws ConfigurationException {
    super(node);
    Node xpath = XmlUtils.getChild(node, "xpath");
    if (xpath == null) {
      throw new ConfigurationException("Property xpath not found in xml source");
    }
    this.xpath = xpath.getTextContent();
  }

  @Override
  public String get(File confFile) throws ConfigurationException {
    try {
      Document document = DocumentBuilderFactory.newInstance()
                                                .newDocumentBuilder()
                                                .parse(new FileInputStream(confFile));
      XPath xPath = XPathFactory.newInstance().newXPath();
      XPathExpression expression = xPath.compile(xpath);
      return expression.evaluate(document);
    } catch (XPathExpressionException e) {
      throw new ConfigurationException("Error while getting value from file "
          + confFile.getAbsolutePath() + " with xpath " + xpath, e);
    } catch (FileNotFoundException e) {
      throw new ConfigurationException("Error while getting value from file "
          + confFile.getAbsolutePath() + " with xpath " + xpath, e);
    } catch (SAXException e) {
      throw new ConfigurationException("Error while getting value from file "
          + confFile.getAbsolutePath() + " with xpath " + xpath, e);
    } catch (IOException e) {
      throw new ConfigurationException("Error while getting value from file "
          + confFile.getAbsolutePath() + " with xpath " + xpath, e);
    } catch (ParserConfigurationException e) {
      throw new ConfigurationException("Error while getting value from file "
          + confFile.getAbsolutePath() + " with xpath " + xpath, e);
    }
  }

  @Override
  public void set(File confFile, String value) throws ConfigurationException {
    try {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
      Document doc = docBuilder.parse(confFile);

      XPath xPath = XPathFactory.newInstance().newXPath();
      XPathExpression expression = xPath.compile(xpath);
      Node node = (Node) expression.evaluate(doc, XPathConstants.NODE);
      node.setTextContent(value);

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(confFile);
      transformer.transform(source, result);
    } catch (TransformerException e) {
      throw new ConfigurationException("Error while setting value " + value + " to file "
          + confFile.getAbsolutePath() + " with xpath " + xpath, e);
    } catch (ParserConfigurationException e) {
      throw new ConfigurationException("Error while setting value " + value + " to file "
          + confFile.getAbsolutePath() + " with xpath " + xpath, e);
    } catch (XPathExpressionException e) {
      throw new ConfigurationException("Error while setting value " + value + " to file "
          + confFile.getAbsolutePath() + " with xpath " + xpath, e);
    } catch (SAXException e) {
      throw new ConfigurationException("Error while setting value " + value + " to file "
          + confFile.getAbsolutePath() + " with xpath " + xpath, e);
    } catch (IOException e) {
      throw new ConfigurationException("Error while setting value " + value + " to file "
          + confFile.getAbsolutePath() + " with xpath " + xpath, e);
    }
  }

}
