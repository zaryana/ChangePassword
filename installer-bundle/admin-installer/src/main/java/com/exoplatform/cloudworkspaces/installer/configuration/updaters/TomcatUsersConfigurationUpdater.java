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

import com.exoplatform.cloudworkspaces.installer.InstallerException;
import com.exoplatform.cloudworkspaces.installer.configuration.AdminConfiguration;
import com.exoplatform.cloudworkspaces.installer.configuration.BaseConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.CurrentAdmin;
import com.exoplatform.cloudworkspaces.installer.configuration.PreviousAdmin;
import com.exoplatform.cloudworkspaces.installer.configuration.PreviousQuestion;
import com.exoplatform.cloudworkspaces.installer.configuration.Question;
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;

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
  public void update(PreviousAdmin prevAdmin,
                     CurrentAdmin currAdmin,
                     InteractionManager interaction,
                     AnswersManager answers) throws InstallerException {
    interaction.println("");
    interaction.println("");
    interaction.println("Tomcat users settings");
    answers.addBlockName("Tomcat users settings");

    AdminConfiguration prevConfiguration = prevAdmin.getAdminConfiguration();
    AdminConfiguration currConfiguration = currAdmin.getAdminConfiguration();

    String prevAdminPassword = prevConfiguration.get("tomcat.users.cloudadmin.password");
    String prevManagerPassword = prevConfiguration.get("tomcat.users.cloudmanager.password");

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

    currConfiguration.set("tomcat.users.cloudadmin.password", adminPass);
    currConfiguration.set("tomcat.users.cloudmanager.password", managerPass);
    answers.addAnswer(tomcatAdminPassQuestion, adminPass);
    answers.addAnswer(tomcatManagerPassQuestion, managerPass);
  }
}
