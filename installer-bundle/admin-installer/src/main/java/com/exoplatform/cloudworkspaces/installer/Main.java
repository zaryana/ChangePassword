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

import com.exoplatform.cloudworkspaces.installer.configuration.AdminDirectories;
import com.exoplatform.cloudworkspaces.installer.downloader.BundleDownloader;
import com.exoplatform.cloudworkspaces.installer.downloader.FromFileBundleDownloader;
import com.exoplatform.cloudworkspaces.installer.downloader.IntranetBundleDownloader;
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManagerWithAnswers;
import com.exoplatform.cloudworkspaces.installer.interaction.StreamInteractionManager;
import com.exoplatform.cloudworkspaces.installer.upgrade.AdminUpgradeAlgorithm;
import com.exoplatform.cloudworkspaces.installer.upgrade.VersionEntry;
import com.exoplatform.cloudworkspaces.installer.upgrade.VersionsManager;

import org.picocontainer.PicoContainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.err.println("Choose upgrade or install command");
      return;
    }
    String[] newargs = new String[args.length - 1];
    for (int i = 1; i < args.length; i++) {
      newargs[i - 1] = args[i];
    }
    if (args[0].indexOf(':') < 0) {
      System.err.println("Choose command with format:\ncommand:profile\nWhere command - upgrade or install\nand profile - profile for your type of cloud");
      return;
    }
    String cmd = args[0].substring(0, args[0].indexOf(':'));
    String profile = args[0].substring(args[0].indexOf(':') + 1);
    if (cmd.equals("upgrade")) {
      upgrade(profile, newargs);
    } else if (cmd.equals("install")) {
      install(profile, newargs);
    } else {
      System.err.println("Choose upgrade or install command");
      return;
    }
  }

  public static void upgrade(String profile, String[] args) throws Exception {
    VersionsManager versionsManager = new VersionsManager();

    String version = null;
    String answersFile = null;
    String conf = System.getenv("EXO_ADMIN_CONF_DIR");
    String data = System.getenv("EXO_ADMIN_DATA_DIR");
    String prevTomcat = null;
    String tomcat = null;
    String saveTo = null;
    String bundle = null;
    boolean isClearTenants = false;
    Map<String, String> priorityAnswers = new HashMap<String, String>();
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-v") || args[i].equals("--version"))
        version = args[i + 1];
      if (args[i].equals("-a") || args[i].equals("--answers"))
        answersFile = args[i + 1];
      if (args[i].equals("-pt") || args[i].equals("--prev-tomcat"))
        prevTomcat = args[i + 1];
      if (args[i].equals("-t") || args[i].equals("--tomcat"))
        tomcat = args[i + 1];
      if (args[i].equals("-s") || args[i].equals("--save-to"))
        saveTo = args[i + 1];
      if (args[i].equals("-b") || args[i].equals("--bundle"))
        bundle = args[i + 1];
      if (args[i].startsWith("-D")) {
        String key = args[i].substring(2, args[i].indexOf('='));
        String value = args[i].substring(args[i].indexOf('=') + 1);
        priorityAnswers.put(key, value);
      }
      if (args[i].equals("--clear-tenants"))
        isClearTenants = true;
    }
    if (version == null) {
      System.err.println("Set version for upgrade");
      return;
    }

    if (prevTomcat == null) {
      System.err.println("Set path to previous tomcat directory. Use --prev-tomcat");
      return;
    }
    File prevTomcatDir = new File(prevTomcat);
    if (!prevTomcatDir.exists() || !prevTomcatDir.isDirectory()) {
      System.err.println("Previous tomcat dir not exists or is not directory");
      return;
    }

    if (tomcat == null) {
      System.err.println("Set path to tomcat directory. Use --tomcat");
      return;
    }
    File tomcatDir = new File(tomcat);
    if (tomcatDir.exists() && !tomcatDir.isDirectory()) {
      System.err.println("Tomcat dir exists but it's not directory");
      return;
    }

    File confDir = null;
    if (conf == null) {
      confDir = new File(tomcatDir, "exo-admin-conf");
    } else {
      confDir = new File(conf);
      if (!confDir.exists() || !confDir.isDirectory()) {
        System.err.println("Conf dir not exists or is not directory");
        return;
      }
    }

    File dataDir = null;
    if (data == null) {
      dataDir = new File(tomcatDir, "data");
    } else {
      dataDir = new File(data);
      if (!dataDir.exists() || !dataDir.isDirectory()) {
        System.out.println("Data dir not exists or is not directory");
        return;
      }
    }

    AnswersManager answersManager = new AnswersManager((saveTo == null) ? null : new File(saveTo));

    InteractionManager interaction = null;
    if (answersFile == null) {
      interaction = new StreamInteractionManager();
    } else {
      interaction = new InteractionManagerWithAnswers(new File(answersFile), priorityAnswers);
    }

    AdminDirectories prevAdminDirs = new AdminDirectories(prevTomcatDir, confDir, dataDir);
    AdminDirectories nextAdminDirs = new AdminDirectories(tomcatDir, confDir, dataDir);

    VersionEntry nextVersion = versionsManager.getVersionEntry(version, profile);
    String prevVersionStr = getCurrentVersion(prevTomcatDir);
    if (prevVersionStr == null)
      prevVersionStr = nextVersion.getFromVersion();
    VersionEntry prevVersion = versionsManager.getVersionEntry(prevVersionStr, profile);

    BundleDownloader downloader = null;
    if (bundle == null) {
      downloader = new IntranetBundleDownloader(nextVersion.getBundleUrl());
    } else {
      downloader = new FromFileBundleDownloader(new File(bundle));
    }

    PicoContainer container = nextVersion.getContainerClass()
                                         .newInstance()
                                         .getContainer(prevAdminDirs,
                                                       nextAdminDirs,
                                                       prevVersion,
                                                       nextVersion,
                                                       downloader,
                                                       interaction,
                                                       answersManager);
    container.getComponent(AdminUpgradeAlgorithm.class).upgrade(nextAdminDirs, isClearTenants);
  }

  public static void install(String profile, String[] args) throws Exception {
    VersionsManager versionsManager = new VersionsManager();

    String version = null;
    String answersFile = null;
    String conf = System.getenv("EXO_ADMIN_CONF_DIR");
    String data = System.getenv("EXO_ADMIN_DATA_DIR");
    String tomcat = null;
    String saveTo = null;
    String bundle = null;
    Map<String, String> priorityAnswers = new HashMap<String, String>();
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-v") || args[i].equals("--version"))
        version = args[i + 1];
      if (args[i].equals("-a") || args[i].equals("--answers"))
        answersFile = args[i + 1];
      if (args[i].equals("-t") || args[i].equals("--tomcat"))
        tomcat = args[i + 1];
      if (args[i].equals("-s") || args[i].equals("--save-to"))
        saveTo = args[i + 1];
      if (args[i].equals("-b") || args[i].equals("--bundle"))
        bundle = args[i + 1];
      if (args[i].startsWith("-D")) {
        String key = args[i].substring(2, args[i].indexOf('='));
        String value = args[i].substring(args[i].indexOf('=') + 1);
        priorityAnswers.put(key, value);
      }
    }
    if (version == null) {
      System.err.println("Set version for upgrade");
      return;
    }

    if (tomcat == null) {
      System.err.println("Set path to tomcat directory. Use --tomcat");
      return;
    }
    File tomcatDir = new File(tomcat);
    if (tomcatDir.exists() && !tomcatDir.isDirectory()) {
      System.err.println("Tomcat dir exists but it's not directory");
      return;
    }

    File confDir = null;
    if (conf == null) {
      confDir = new File(tomcatDir, "exo-admin-conf");
    } else {
      confDir = new File(conf);
      if (!confDir.exists()) {
        confDir.mkdirs();
      }
      if (!confDir.isDirectory()) {
        System.err.println("Conf dir is not directory");
        return;
      }
    }

    File dataDir = null;
    if (data == null) {
      dataDir = new File(tomcatDir, "data");
    } else {
      dataDir = new File(data);
      if (!dataDir.exists()) {
        dataDir.mkdirs();
      }
      if (!dataDir.isDirectory()) {
        System.out.println("Data dir is not directory");
        return;
      }
    }

    AnswersManager answersManager = new AnswersManager((saveTo == null) ? null : new File(saveTo));

    InteractionManager interaction = null;
    if (answersFile == null) {
      interaction = new StreamInteractionManager();
    } else {
      interaction = new InteractionManagerWithAnswers(new File(answersFile), priorityAnswers);
    }

    AdminDirectories prevAdminDirs = new AdminDirectories(tomcatDir, confDir, dataDir);
    AdminDirectories nextAdminDirs = new AdminDirectories(tomcatDir, confDir, dataDir);

    VersionEntry nextVersion = versionsManager.getVersionEntry(version, profile);
    VersionEntry prevVersion = versionsManager.getVersionEntry(nextVersion.getFromVersion(),
                                                               profile);

    BundleDownloader downloader = null;
    if (bundle == null) {
      downloader = new IntranetBundleDownloader(nextVersion.getBundleUrl());
    } else {
      downloader = new FromFileBundleDownloader(new File(bundle));
    }

    PicoContainer container = nextVersion.getContainerClass()
                                         .newInstance()
                                         .getContainer(prevAdminDirs,
                                                       nextAdminDirs,
                                                       prevVersion,
                                                       nextVersion,
                                                       downloader,
                                                       interaction,
                                                       answersManager);
    container.getComponent(AdminUpgradeAlgorithm.class).install(nextAdminDirs);
  }

  public static String getCurrentVersion(File prevTomcat) throws FileNotFoundException, IOException {
    File rootWarFile = new File(prevTomcat, "webapps/ROOT.war");
    ZipInputStream zin = new ZipInputStream(new FileInputStream(rootWarFile));
    try {
      ZipEntry entry = zin.getNextEntry();
      InputStream stream = null;
      while (entry != null) {
        if (entry.getName().equals("WEB-INF/classes/version") && !entry.isDirectory()) {
          stream = zin;
          break;
        }
        entry = zin.getNextEntry();
      }
      if (stream == null)
        return null;
      Properties properties = new Properties();
      properties.load(stream);
      return properties.getProperty("version");
    } finally {
      zin.close();
    }
  }
}
