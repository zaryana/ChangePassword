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

import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManagerWithAnswers;
import com.exoplatform.cloudworkspaces.installer.interaction.StreamInteractionManager;
import com.exoplatform.cloudworkspaces.installer.upgrade.AdminUpgradeAlgorithm;
import com.exoplatform.cloudworkspaces.installer.upgrade.VersionEntry;
import com.exoplatform.cloudworkspaces.installer.upgrade.VersionsManager;

import org.picocontainer.PicoContainer;

import java.io.File;

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
    if (args[0].equals("upgrade")) {
      upgrade(args);
    } else if (args[0].equals("install")) {
      install(args);
    } else {
      System.err.println("Choose upgrade or install command");
      return;
    }
  }

  public static void install(String[] args) throws Exception {
    VersionsManager versionsManager = new VersionsManager();

    String version = null;
    String answersFile = null;
    String conf = System.getenv("EXO_ADMIN_CONF_DIR");
    String data = System.getenv("EXO_ADMIN_DATA_DIR");
    String tomcat = "admin-tomcat";
    String saveTo = null;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-v") || args[i].equals("--version"))
        version = args[i + 1];
      if (args[i].equals("-a") || args[i].equals("--answers"))
        answersFile = args[i + 1];
      if (args[i].equals("-t") || args[i].equals("--tomcat"))
        tomcat = args[i + 1];
      if (args[i].equals("-s") || args[i].equals("--save-to"))
        saveTo = args[i + 1];
    }
    if (version == null) {
      System.err.println("Set version for upgrade");
    }
    if (conf == null) {
      System.err.println("Set admin configuration dir in environment. Variable - EXO_ADMIN_CONF_DIR");
      return;
    }
    File confDir = new File(conf);
    if (!confDir.exists() || !confDir.isDirectory()) {
      confDir.mkdirs();
    }
    if (tomcat == null)
      System.err.println("Set path to tomcat directory. Use --tomcat");
    File tomcatDir = new File(tomcat);
    if (!tomcatDir.exists() || !tomcatDir.isDirectory()) {
      System.err.println("Tomcat dir not exists or is not directory");
    }
    if (data == null)
      System.err.println("Set admin configuration dir in environment. Variable - EXO_ADMIN_DATA_DIR");
    File dataDir = new File(data);
    if (!dataDir.exists() || !dataDir.isDirectory()) {
      dataDir.mkdirs();
    }
    AnswersManager answersManager = new AnswersManager((saveTo == null) ? null : new File(saveTo));

    InteractionManager interaction = null;
    if (answersFile == null) {
      interaction = new StreamInteractionManager();
    } else {
      interaction = new InteractionManagerWithAnswers(new File(answersFile));
    }

    VersionEntry entry = versionsManager.getVersionEntry(version);
    PicoContainer container = entry.getContainerClass()
                                   .newInstance()
                                   .getContainer(entry, interaction, answersManager);
    container.getComponent(AdminUpgradeAlgorithm.class).install(confDir, tomcatDir, dataDir);
  }

  public static void upgrade(String[] args) throws Exception {
    VersionsManager versionsManager = new VersionsManager();

    String version = null;
    String answersFile = null;
    String conf = System.getenv("EXO_ADMIN_CONF_DIR");
    String data = System.getenv("EXO_ADMIN_DATA_DIR");
    String tomcat = "admin-tomcat";
    String saveTo = null;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-v") || args[i].equals("--version"))
        version = args[i + 1];
      if (args[i].equals("-a") || args[i].equals("--answers"))
        answersFile = args[i + 1];
      if (args[i].equals("-t") || args[i].equals("--tomcat"))
        tomcat = args[i + 1];
      if (args[i].equals("-s") || args[i].equals("--save-to"))
        saveTo = args[i + 1];
    }
    if (version == null) {
      System.err.println("Set version for upgrade");
      return;
    }
    if (conf == null) {
      System.err.println("Set admin configuration dir in environment. Variable - EXO_ADMIN_CONF_DIR");
      return;
    }
    File confDir = new File(conf);
    if (!confDir.exists() || !confDir.isDirectory()) {
      System.err.println("Conf dir not exists or is not directory");
      return;
    }
    if (tomcat == null) {
      System.err.println("Set path to tomcat directory. Use --tomcat");
      return;
    }
    File tomcatDir = new File(tomcat);
    if (!tomcatDir.exists() || !tomcatDir.isDirectory()) {
      System.err.println("Tomcat dir not exists or is not directory");
    }
    if (data == null) {
      System.err.println("Set admin configuration dir in environment. Variable - EXO_ADMIN_DATA_DIR");
      return;
    }
    File dataDir = new File(data);
    if (!dataDir.exists() || !dataDir.isDirectory()) {
      System.out.println("Data dir not exists or is not directory");
      return;
    }
    AnswersManager answersManager = new AnswersManager((saveTo == null) ? null : new File(saveTo));

    InteractionManager interaction = null;
    if (answersFile == null) {
      interaction = new StreamInteractionManager();
    } else {
      interaction = new InteractionManagerWithAnswers(new File(answersFile));
    }

    VersionEntry entry = versionsManager.getVersionEntry(version);
    PicoContainer container = entry.getContainerClass()
                                   .newInstance()
                                   .getContainer(entry, interaction, answersManager);
    container.getComponent(AdminUpgradeAlgorithm.class).upgrade(confDir, tomcatDir, dataDir);
  }

}
