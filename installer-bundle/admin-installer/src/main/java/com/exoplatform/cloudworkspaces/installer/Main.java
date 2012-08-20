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

import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationManager;
import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.AwsConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.CloudConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.DBConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.InstanceConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.MailConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.updaters.TomcatUsersConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.downloader.IntranetBundleDownloader;
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManagerWithAnswers;
import com.exoplatform.cloudworkspaces.installer.interaction.StreamInteractionManager;
import com.exoplatform.cloudworkspaces.installer.rest.M8CloudAdminServices;
import com.exoplatform.cloudworkspaces.installer.upgrade.AdminUpgradeAlgorithm;
import com.exoplatform.cloudworkspaces.installer.upgrade.VersionsManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.out.println("Choose upgrade or install command");
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
      System.out.println("Choose upgrade or install command");
    }
  }

  public static void install(String[] args) throws Exception {
  }

  public static void upgrade(String[] args) throws Exception {
    VersionsManager versionsManager = new VersionsManager(new File("/home/koster/test/versions"));

    String version = null;
    String answersFile = null;
    String installConfig = "install.conf";
    String conf = "admin-data/conf";
    String data = "admin-data/data";
    String tomcat = "admin-tomcat";
    String saveTo = null;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-v") || args[i].equals("--version"))
        version = args[i + 1];
      if (args[i].equals("-a") || args[i].equals("--answers"))
        answersFile = args[i + 1];
      if (args[i].equals("-i") || args[i].equals("--install-config"))
        installConfig = args[i + 1];
      if (args[i].equals("-c") || args[i].equals("--conf"))
        conf = args[i + 1];
      if (args[i].equals("-d") || args[i].equals("--data"))
        data = args[i + 1];
      if (args[i].equals("-t") || args[i].equals("--tomcat"))
        tomcat = args[i + 1];
      if (args[i].equals("-s") || args[i].equals("--save"))
        saveTo = args[i + 1];
    }
    if (version == null) {
      System.out.println("Set version for upgrade");
    }
    File installConfigFile = new File(installConfig);
    if (!installConfigFile.exists() || !installConfigFile.isFile()) {
      System.out.println("Install config file not exists or is not file");
    }
    File confDir = new File(conf);
    if (!confDir.exists() || !confDir.isDirectory()) {
      System.out.println("Conf dir not exists or is not directory");
    }
    File tomcatDir = new File(tomcat);
    if (!tomcatDir.exists() || !tomcatDir.isDirectory()) {
      System.out.println("Tomcat dir not exists or is not directory");
    }
    File dataDir = new File(data);
    if (!dataDir.exists() || !dataDir.isDirectory()) {
      System.out.println("Data dir not exists or is not directory");
    }
    AnswersManager answersManager = new AnswersManager((saveTo == null) ? null : new File(saveTo));

    InstallerConfiguration configuration = new InstallerConfiguration(installConfigFile);

    InteractionManager interaction = null;
    if (answersFile == null) {
      interaction = new StreamInteractionManager();
    } else {
      interaction = new InteractionManagerWithAnswers(new File(answersFile));
    }

    AdminUpgradeAlgorithm algorithm = versionsManager.getAlgorithm("1.1.0-Beta05", version);
    algorithm.upgrade(confDir, tomcatDir, dataDir, configuration, interaction, answersManager);
  }

  public static void rest(String[] args) throws Exception {
    M8CloudAdminServices services = new M8CloudAdminServices("wks-s.exoplatform.org",
                                                             "cloudadmin",
                                                             "cloudadmin");
    Object map = services.createTenant("test9");
    System.out.println(map);
  }

  public static void download(String[] args) throws Exception {
    IntranetBundleDownloader downloader = new IntranetBundleDownloader();
    downloader.downloadAdminTo("http://intranet.exoplatform.org/rest/jcr/repository/collaboration/Groups/platform/users/Documents/Builds/CloudWorkspaces/releases/1.1.0-Beta06/cloud-workspaces-admin-1.0.0-Beta06-tomcat.zip",
                               "",
                               "",
                               new File("/home/koster/downloaded-ADMIN"));
  }

  public static void config(String[] args) throws Exception {
    AnswersManager answers = new AnswersManager(new File("/home/koster/test/answers.txt"));
    InteractionManager interaction = new StreamInteractionManager();
    List<ConfigurationUpdater> updaters = new ArrayList<ConfigurationUpdater>();
    updaters.add(new DBConfigurationUpdater());
    updaters.add(new CloudConfigurationUpdater());
    updaters.add(new TomcatUsersConfigurationUpdater());
    updaters.add(new MailConfigurationUpdater());
    updaters.add(new AwsConfigurationUpdater());
    updaters.add(new InstanceConfigurationUpdater());
    ConfigurationManager configurationManager = new ConfigurationManager(new File("/home/koster/test/admin-conf/conf"),
                                                                         new File("/home/koster/test/admin-tomcat"),
                                                                         new File("/home/koster/test/bundle"),
                                                                         updaters,
                                                                         interaction,
                                                                         answers);
    configurationManager.configure();
    configurationManager.update();
  }
}
