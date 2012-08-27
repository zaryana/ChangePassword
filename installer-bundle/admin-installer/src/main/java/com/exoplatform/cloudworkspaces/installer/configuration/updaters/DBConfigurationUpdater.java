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

public class DBConfigurationUpdater extends BaseConfigurationUpdater {

  private final Question DBUrlQuestion      = new Question("database.url",
                                                           "Set database url (with port). For example: localhost:3306",
                                                           null,
                                                           "^.*$",
                                                           null);

  private final Question DBUsernameQuestion = new Question("database.username",
                                                           "Set database username",
                                                           null,
                                                           "^.*$",
                                                           null);

  private final Question DBPasswordQuestion = new Question("database.password",
                                                           "Set database password",
                                                           null,
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
      interaction.println("Database settings");
      answers.addBlockName("Database settings");

      String prevUrl = ConfigUtils.findProperty(new File(previousTomcatDir, "bin"),
                                                "environment.sh",
                                                "EXO_DB_HOST");
      String prevUsername = ConfigUtils.findProperty(new File(previousTomcatDir, "bin"),
                                                     "environment.sh",
                                                     "EXO_DB_USER");
      String prevPassword = ConfigUtils.findProperty(new File(previousTomcatDir, "bin"),
                                                     "environment.sh",
                                                     "EXO_DB_PASSWORD");

      clearBlock();
      addToBlock(DBUrlQuestion, prevUrl);
      addToBlock(DBUsernameQuestion, prevUsername);
      addToBlock(DBPasswordQuestion, prevPassword);

      boolean usePrev = false;
      if (wasBlockChanged()) {
        usePrev = interaction.ask(new PreviousQuestion(getChanges())).equals("yes");
      }

      String url = prevUrl;
      String username = prevUsername;
      String password = prevPassword;
      if (!usePrev) {
        DBUrlQuestion.setDefaults(prevUrl);
        DBUsernameQuestion.setDefaults(prevUsername);
        DBPasswordQuestion.setDefaults(prevPassword);

        url = interaction.ask(DBUrlQuestion);
        username = interaction.ask(DBUsernameQuestion);
        password = interaction.ask(DBPasswordQuestion);
      }

      ConfigUtils.writeProperty(new File(tomcatDir, "bin"), "environment.sh", "EXO_DB_HOST", url);
      answers.addAnswer(DBUrlQuestion, url);
      ConfigUtils.writeProperty(new File(tomcatDir, "bin"),
                                "environment.sh",
                                "EXO_DB_USER",
                                username);
      answers.addAnswer(DBUsernameQuestion, username);
      ConfigUtils.writeProperty(new File(tomcatDir, "bin"),
                                "environment.sh",
                                "EXO_DB_PASSWORD",
                                password);
      answers.addAnswer(DBPasswordQuestion, password);
    } catch (IOException e) {
      throw new ConfigurationException("Updating db configuration failed", e);
    }
  }
}
