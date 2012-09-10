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
import com.exoplatform.cloudworkspaces.installer.configuration.Admin;

import java.io.File;
import java.io.IOException;

public class AdminTomcatWrapperImpl implements AdminTomcatWrapper {

  private Admin admin;

  public AdminTomcatWrapperImpl(Admin admin) {
    this.admin = admin;
  }

  public AdminTomcatWrapperImpl() {
  }

  @Override
  public void startTomcat() throws InstallerException {
    execute("./catalina.sh start", new File(admin.getAdminDirectories().getTomcatDir(), "bin"));
  }

  @Override
  public void stopTomcat() throws InstallerException {
    execute("./catalina.sh stop -force",
            new File(admin.getAdminDirectories().getTomcatDir(), "bin"));
  }

  private void execute(String cmd, File dir) throws InstallerException {
    try {
      Process process = Runtime.getRuntime().exec(cmd, null, dir);
      int result = process.waitFor();
      if (result != 0)
        throw new InstallerException("Starting or stopping tomcat was failed with status " + result);
    } catch (IOException e) {
      throw new InstallerException("Error while starting/stopping tomcat", e);
    } catch (InterruptedException e) {
      throw new InstallerException("Error while starting/stopping tomcat", e);
    }
  }

}
