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

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class UpdationAlgorithmConfiguration {

  private final VersionEntry version;

  private final Node   node;

  public UpdationAlgorithmConfiguration(VersionEntry version, Node node) {
    this.version = version;
    this.node = node;
  }

  public Class<? extends AdminUpgradeAlgorithm> getAlgorithmClass() throws InstallerException {
    try {
      return (Class<? extends AdminUpgradeAlgorithm>) Thread.currentThread()
                                                            .getContextClassLoader()
                                                            .loadClass(XmlUtils.getChild(node,
                                                                                         "algorithm")
                                                                               .getTextContent());
    } catch (ClassNotFoundException e) {
      throw new InstallerException("Tag with upgrade algorithm implementation not found", e);
    }
  }

  public List<Object> getHooks(String hookName) throws InstallerException {
    ArrayList<Object> hooks = new ArrayList<Object>();

    for (Node hook : XmlUtils.getChildren(XmlUtils.getChild(node, "hooks", hookName), "hook")) {
      String name = hook.getAttributes().getNamedItem("class").getTextContent();
      if (version.isProfileAllowed(hook)) {
        try {
          Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(name);
          hooks.add(clazz.getConstructor(Node.class).newInstance(hook));
        } catch (DOMException e) {
          throw new InstallerException("Error happened while " + hookName
              + " getting hooks from xml", e);
        } catch (ClassNotFoundException e) {
          throw new InstallerException("Hook " + name + " not found", e);
        } catch (IllegalArgumentException e) {
          throw new InstallerException("Error happened while creating " + name + " hook instance",
                                       e);
        } catch (SecurityException e) {
          throw new InstallerException("Error happened while creating " + name + " hook instance",
                                       e);
        } catch (InstantiationException e) {
          throw new InstallerException("Error happened while creating " + name + " hook instance",
                                       e);
        } catch (IllegalAccessException e) {
          throw new InstallerException("Error happened while creating " + name + " hook instance",
                                       e);
        } catch (InvocationTargetException e) {
          throw new InstallerException("Error happened while creating " + name + " hook instance",
                                       e);
        } catch (NoSuchMethodException e) {
          throw new InstallerException("Error happened while creating " + name + " hook instance",
                                       e);
        }
      }
    }
    return hooks;
  }

}
