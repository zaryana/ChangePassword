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
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigurationManager {

  private final InteractionManager         interactionManager;

  private final AnswersManager             answersManager;

  private final File                       previousConfDir;

  private final File                       previousTomcatDir;

  private File                             confDir;

  private File                             tomcatDir;

  private final File                       bundleZip;

  private final List<ConfigurationUpdater> updaters;

  public ConfigurationManager(File previousConfDir,
                              File previousTomcatDir,
                              File bundleZip,
                              List<ConfigurationUpdater> updaters,
                              InteractionManager interactionManager,
                              AnswersManager answersManager) {
    this.interactionManager = interactionManager;
    this.answersManager = answersManager;
    this.previousConfDir = previousConfDir;
    this.previousTomcatDir = previousTomcatDir;
    this.bundleZip = bundleZip;
    this.updaters = updaters;
  }

  public void configure() throws ConfigurationException {
    try {
      File bundleDir = new File(previousTomcatDir.getParentFile(), "new-admin-bundle/");
      if (bundleDir.exists())
        FileUtils.deleteDir(bundleDir);
      if (!bundleDir.mkdirs()) {
        throw new IOException("Couldn't create bundle dir");
      }
      FileUtils.unzipTo(bundleZip, bundleDir);

      this.confDir = new File(previousConfDir.getAbsolutePath() + ".new");
      if (confDir.exists())
        FileUtils.deleteDir(confDir);
      if (!confDir.mkdirs())
        throw new IOException("Couldn't create directory " + confDir.getAbsolutePath());
      FileUtils.copyDirs(new File(bundleDir, "admin-tomcat/exo-admin-conf"), confDir);
      tomcatDir = new File(previousTomcatDir.getAbsolutePath() + ".new");
      if (tomcatDir.exists())
        FileUtils.deleteDir(tomcatDir);
      if (!tomcatDir.mkdirs())
        throw new IOException("Couldn't create directory " + tomcatDir.getAbsolutePath());
      FileUtils.copyDirs(new File(bundleDir, "admin-tomcat"), tomcatDir);

      File prevEnvFile = new File(previousConfDir, "environment.sh");
      if (!prevEnvFile.exists()) {
        File envFile = new File(confDir, "environment.sh");
        FileUtils.copyFile(envFile, prevEnvFile);
      }

      for (ConfigurationUpdater updater : updaters) {
        updater.update(confDir,
                       tomcatDir,
                       previousConfDir,
                       previousTomcatDir,
                       interactionManager,
                       answersManager);
      }
    } catch (IOException e) {
      throw new ConfigurationException(e);
    }
  }

  public void update() throws ConfigurationException {
    try {
      File oldConfDir = new File(previousConfDir.getAbsolutePath() + ".old");
      if (oldConfDir.exists())
        FileUtils.deleteDir(oldConfDir);
      if (!previousConfDir.renameTo(oldConfDir))
        throw new IOException("Couldn't move old configuration directory");
      if (!confDir.renameTo(previousConfDir))
        throw new IOException("Couldn't move new configuration directory to configuration directory");
      File oldTomcatDir = new File(previousTomcatDir.getAbsolutePath() + ".old");
      if (oldTomcatDir.exists())
        FileUtils.deleteDir(oldTomcatDir);
      if (!previousTomcatDir.renameTo(oldTomcatDir))
        throw new IOException("Couldn't move old tomcat directory");
      if (!tomcatDir.renameTo(previousTomcatDir))
        throw new IOException("Couldn't move new tomcat directory to tomcat directory");
    } catch (IOException e) {
      throw new ConfigurationException(e);
    }
  }

}
