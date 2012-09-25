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
package com.exoplatform.cloudworkspaces.installer;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class XmlUtils {

  public static Node getChild(Node node, String... tags) {
    if (tags.length == 0)
      return node;
    for (String tag : tags) {
      node = getChild(node, tag);
    }
    return node;
  }

  public static Node getChild(Node node, String tag) {
    if (node == null)
      return null;
    NodeList nodes = node.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node curr = nodes.item(i);
      if (curr.getNodeName().equals(tag))
        return curr;
    }
    return null;
  }

  public static List<Node> getChildren(Node node, String tag) {
    ArrayList<Node> result = new ArrayList<Node>();
    if (node == null)
      return result;
    NodeList nodes = node.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node curr = nodes.item(i);
      if (curr.getNodeName().equals(tag))
        result.add(curr);
    }
    return result;
  }

}
