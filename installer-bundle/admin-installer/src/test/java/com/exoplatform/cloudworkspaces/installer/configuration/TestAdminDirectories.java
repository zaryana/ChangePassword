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
package com.exoplatform.cloudworkspaces.installer.configuration;

import com.exoplatform.cloudworkspaces.installer.InstallerException;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Stack;

public class TestAdminDirectories {

  private static final File PREFIX = new File("target/test-classes/test-directories/").getAbsoluteFile();

  @Test
  public void testIfAnyRelativeDirectories() throws InstallerException {
    String[] existed = new String[] { "new-admin-tomcat/tomcat", "new-admin-conf/conf",
        "new-admin-data/data" };
    String[] from = new String[] { "new-admin-tomcat", "new-admin-conf", "new-admin-data" };
    String[] to = new String[] { "admin-tomcat", "admin-conf", "admin-data" };
    String[] expected = new String[] { "admin-tomcat/tomcat", "admin-conf/conf", "admin-data/data" };

    testMoveTo(existed, from, to, expected);
  }

  @Test
  public void testIfAnyRelativeDirectoriesButOldDirectoriesExists() throws InstallerException {
    String[] existed = new String[] { "new-admin-tomcat/tomcat", "new-admin-conf/conf",
        "new-admin-data/data", "admin-tomcat/old-tomcat", "admin-conf/old-conf" };
    String[] from = new String[] { "new-admin-tomcat", "new-admin-conf", "new-admin-data" };
    String[] to = new String[] { "admin-tomcat", "admin-conf", "admin-data" };
    String[] expected = new String[] { "admin-tomcat/tomcat", "admin-conf/conf", "admin-data/data",
        "admin-tomcat.old/old-tomcat", "admin-conf.old/old-conf" };

    testMoveTo(existed, from, to, expected);
  }

  @Test
  public void testIfConfAndDataDirectoriesRelative() throws InstallerException {
    String[] existed = new String[] { "new-admin-tomcat/tomcat",
        "new-admin-tomcat/admin-conf/conf", "new-admin-tomcat/admin-data/data" };
    String[] from = new String[] { "new-admin-tomcat", "new-admin-tomcat/admin-conf",
        "new-admin-tomcat/admin-data" };
    String[] to = new String[] { "admin-tomcat", "admin-tomcat/admin-conf",
        "admin-tomcat/admin-data" };
    String[] expected = new String[] { "admin-tomcat/tomcat", "admin-tomcat/admin-conf/conf",
        "admin-tomcat/admin-data/data" };

    testMoveTo(existed, from, to, expected);
  }

  public void testMoveTo(String[] existed, String[] from, String[] to, String[] expected) throws InstallerException {
    Stack<File> stack = new Stack<File>();
    stack.push(PREFIX);
    while (!stack.isEmpty()) {
      File curr = stack.pop();
      if (curr.exists()) {
        File[] childs = curr.listFiles();
        if (childs == null || childs.length == 0) {
          Assert.assertTrue(curr.delete());
        } else {
          stack.push(curr);
          for (File file : curr.listFiles()) {
            stack.push(file);
          }
        }
      }
    }

    for (String name : existed) {
      File file = new File(PREFIX, name);
      file.mkdirs();
    }

    AdminDirectories fromAdmin = new AdminDirectories(new File(PREFIX, from[0]),
                                                      new File(PREFIX, from[1]),
                                                      new File(PREFIX, from[2]));
    AdminDirectories toAdmin = new AdminDirectories(new File(PREFIX, to[0]),
                                                    new File(PREFIX, to[1]),
                                                    new File(PREFIX, to[2]));
    fromAdmin.moveTo(toAdmin);
    Assert.assertEquals(fromAdmin.getTomcatDir(), new File(PREFIX, to[0]));
    Assert.assertEquals(fromAdmin.getConfDir(), new File(PREFIX, to[1]));
    Assert.assertEquals(fromAdmin.getDataDir(), new File(PREFIX, to[2]));

    HashSet<File> expectedFiles = new HashSet<File>();
    for (String name : expected) {
      File curr = new File(PREFIX, name);
      while (!curr.equals(PREFIX)) {
        expectedFiles.add(curr);
        curr = curr.getParentFile();
      }
    }
    stack = new Stack<File>();
    for (File curr : PREFIX.listFiles()) {
      stack.push(curr);
    }
    while (!stack.isEmpty()) {
      File curr = stack.pop();
      if (curr.exists()) {
        Assert.assertTrue(expectedFiles.contains(curr));
        expectedFiles.remove(curr);
        for (File file : curr.listFiles()) {
          stack.push(file);
        }
      }
    }
  }
}
