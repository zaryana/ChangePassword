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
package com.exoplatform.cloudworkspaces.installer.configuration.updaters;

import com.exoplatform.cloudworkspaces.installer.ConfigUtils;
import com.exoplatform.cloudworkspaces.installer.InstallerException;
import com.exoplatform.cloudworkspaces.installer.configuration.BaseConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationException;
import com.exoplatform.cloudworkspaces.installer.configuration.PreviousQuestion;
import com.exoplatform.cloudworkspaces.installer.configuration.Question;
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;

import java.io.File;
import java.io.IOException;

public class TomcatUsersConfigurationUpdater extends BaseConfigurationUpdater {
  private final Question tomcatAdminPassQuestion   = new Question("tomcat.users.admin.password",
                                                                  "Set password for cloudadmin user",
                                                                  "cloudadmin",
                                                                  "^.*$",
                                                                  null);

  private final Question tomcatManagerPassQuestion = new Question("tomcat.users.manager.password",
                                                                  "Set password for cloudmanager user",
                                                                  "cloudmanager",
                                                                  "^.*$",
                                                                  null);

  @Override
  public void update(File confDir,
                     File tomcatDir,
                     File previousConfDir,
                     File previousTomcatDir,
                     InteractionManager interaction,
                     AnswersManager answers) throws InstallerException {
    try {
      interaction.println("");
      interaction.println("");
      interaction.println("Tomcat users settings");
      answers.addBlockName("Tomcat users settings");

      String prevAdminPassword = ConfigUtils.find(previousTomcatDir,
                                                  "conf/tomcat-users.xml",
                                                  "<user username=\"cloudadmin\" password=\"([^\"]*)\"");
      String prevManagerPassword = ConfigUtils.find(previousTomcatDir,
                                                    "conf/tomcat-users.xml",
                                                    "<user username=\"cloudmanager\" password=\"([^\"]*)\"");

      clearBlock();
      addToBlock(tomcatAdminPassQuestion, prevAdminPassword);
      addToBlock(tomcatManagerPassQuestion, prevManagerPassword);

      boolean usePrev = false;
      if (wasBlockChanged()) {
        usePrev = interaction.ask(new PreviousQuestion(getChanges())).equals("yes");
      }

      String adminPass = prevAdminPassword;
      String managerPass = prevManagerPassword;
      if (!usePrev) {
        tomcatAdminPassQuestion.setDefaults(prevAdminPassword);
        tomcatManagerPassQuestion.setDefaults(prevManagerPassword);

        adminPass = interaction.ask(tomcatAdminPassQuestion);
        managerPass = interaction.ask(tomcatManagerPassQuestion);
      }

      ConfigUtils.replace(tomcatDir,
                          "conf/tomcat-users.xml",
                          "<user username=\"cloudadmin\" password=\"cloudadmin\"",
                          "<user username=\"cloudadmin\" password=\"" + adminPass + "\"");
      answers.addAnswer(tomcatAdminPassQuestion, adminPass);
      ConfigUtils.replace(tomcatDir,
                          "conf/tomcat-users.xml",
                          "<user username=\"cloudmanager\" password=\"cloudmanager\"",
                          "<user username=\"cloudmanager\" password=\"" + managerPass + "\"");
      answers.addAnswer(tomcatManagerPassQuestion, managerPass);
    } catch (IOException e) {
      throw new ConfigurationException("Updating tomcat-users configuration failed", e);
    }
  }
}
