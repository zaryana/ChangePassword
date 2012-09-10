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

import com.exoplatform.cloudworkspaces.installer.FileUtils;
import com.exoplatform.cloudworkspaces.installer.InstallerException;

import java.io.File;
import java.io.IOException;

public class AdminDirectories {

  private File    tomcatDir;

  private boolean isConfRelative;

  private String  confDir;

  private boolean isDataRelative;

  private String  dataDir;

  public AdminDirectories(File tomcatDir, File confDir, File dataDir) {
    this.tomcatDir = tomcatDir;

    this.confDir = getRelativePath(tomcatDir, confDir);
    this.isConfRelative = true;
    if (this.confDir == null) {
      this.confDir = confDir.getAbsolutePath();
      this.isConfRelative = false;
    }

    this.dataDir = getRelativePath(tomcatDir, dataDir);
    this.isDataRelative = true;
    if (this.dataDir == null) {
      this.dataDir = dataDir.getAbsolutePath();
      this.isDataRelative = false;
    }
  }

  public static AdminDirectories createFromBundle(File bundleZip, File toDir) throws IOException,
                                                                             InterruptedException {
    File unzipDir = new File(toDir.getAbsolutePath() + ".unzip");
    if (unzipDir.exists()) {
      FileUtils.deleteDir(unzipDir);
    }
    FileUtils.unzipTo(bundleZip, unzipDir);
    if (toDir.exists()) {
      FileUtils.deleteDir(toDir);
    }
    File from = new File(unzipDir, "admin-tomcat");
    if (!from.renameTo(toDir)) {
      throw new IOException("Couldn't move directory " + from.getAbsolutePath() + " to "
          + toDir.getAbsolutePath());
    }
    FileUtils.deleteDir(unzipDir);
    return new AdminDirectories(toDir, new File(toDir, "exo-admin-conf"), new File(toDir, "data"));
  }

  public File getTomcatDir() {
    return tomcatDir;
  }

  public boolean isConfRelative() {
    return isConfRelative;
  }

  public File getConfDir() {
    if (!isConfRelative)
      return new File(confDir);
    else
      return new File(tomcatDir, confDir);
  }

  public boolean isDataRelative() {
    return isDataRelative;
  }

  public File getDataDir() {
    if (!isDataRelative)
      return new File(dataDir);
    else
      return new File(tomcatDir, dataDir);
  }

  private void backupDir(File dir) throws InstallerException {
    if (dir.exists()) {
      File old = new File(dir.getAbsolutePath() + ".old");
      if (old.exists()) {
        try {
          FileUtils.deleteDir(old);
        } catch (IOException e) {
          throw new InstallerException("Error while deleting directory " + old.getAbsolutePath());
        }
      }
      if (!dir.renameTo(old)) {
        throw new InstallerException("Couldn't move directory " + dir.getAbsolutePath()
            + " to directory " + old.getAbsolutePath());
      }
    }
  }

  private String moveResourcesDir(File from, File to, boolean isToRelative) throws InstallerException {
    if (from.equals(to)) {
      if (!isToRelative) {
        backupDir(to);
        try {
          FileUtils.moveDir(from, to);
        } catch (IOException e) {
          throw new InstallerException("Couldn't move directory " + from.getAbsolutePath()
              + " to directory " + to.getAbsolutePath(), e);
        }
      }
    } else {
      backupDir(to);
      try {
        FileUtils.moveDir(from, to);
      } catch (IOException e) {
        throw new InstallerException("Couldn't move directory " + from.getAbsolutePath()
            + " to directory " + to.getAbsolutePath(), e);
      }
    }
    if (isToRelative) {
      return getRelativePath(tomcatDir, to);
    } else {
      return to.getAbsolutePath();
    }
  }

  public void moveTo(AdminDirectories to) throws InstallerException {
    /*
     * getConfDir and getDataDir may depends on getTomcatDir(). So, directories
     * MUST be calculated before moving tomcat dir.
     */
    File toTomcatDir = to.getTomcatDir();
    File toConfDir = to.getConfDir();
    File toDataDir = to.getDataDir();

    // move tomcat dir
    File tomcatDir = getTomcatDir();
    if (toTomcatDir.exists()) {
      to.backupDir(toTomcatDir);
    }
    if (!tomcatDir.renameTo(toTomcatDir)) {
      throw new InstallerException("Couldn't move directory " + tomcatDir.getAbsolutePath()
          + " to directory " + toTomcatDir.getAbsolutePath());
    }
    this.tomcatDir = toTomcatDir;

    this.confDir = moveResourcesDir(getConfDir(), toConfDir, to.isConfRelative());
    this.isConfRelative = to.isConfRelative();
    if (!to.getDataDir().exists()) {
      this.dataDir = moveResourcesDir(getDataDir(), toDataDir, to.isDataRelative());
      this.isDataRelative = to.isDataRelative();
    }
  }

  private String getRelativePath(File baseFile, File childFile) {
    String base = baseFile.getAbsolutePath();
    String child = childFile.getAbsolutePath();
    if (base.charAt(base.length() - 1) != File.pathSeparatorChar) {
      base += '/';
    }
    if (child.startsWith(base)) {
      return child.substring(base.length());
    }
    return null;
  }

}
