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
package com.exoplatform.cloudworkspaces.installer.tomcat;

import com.exoplatform.cloudworkspaces.installer.InstallerException;

import java.io.File;
import java.util.Scanner;

public class AdminTomcatWrapperImpl implements AdminTomcatWrapper {

  private final File tomcatDir;

  public AdminTomcatWrapperImpl(File tomcatDir) {
    this.tomcatDir = tomcatDir;
  }

  @Override
  public void startTomcat() throws InstallerException {
    System.out.println("Please, start tomcat");
    new Scanner(System.in).next();
  }

  @Override
  public void stopTomcat() throws InstallerException {
    System.out.println("Please, stop tomcat");
    new Scanner(System.in).next();
  }

}
