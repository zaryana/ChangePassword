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
  public void update(PreviousAdmin prevAdmin,
                     CurrentAdmin currAdmin,
                     InteractionManager interaction,
                     AnswersManager answers) throws InstallerException {
    interaction.println("");
    interaction.println("");
    interaction.println("Database settings");
    answers.addBlockName("Database settings");

    AdminConfiguration prevConfiguration = prevAdmin.getAdminConfiguration();
    AdminConfiguration currConfiguration = currAdmin.getAdminConfiguration();

    String prevUrl = prevConfiguration.get("admin.db.url");
    String prevUsername = prevConfiguration.get("admin.db.username");
    String prevPassword = prevConfiguration.get("admin.db.password");

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

    currConfiguration.set("admin.db.url", url);
    currConfiguration.set("admin.db.username", username);
    currConfiguration.set("admin.db.password", password);
    answers.addAnswer(DBUrlQuestion, url);
    answers.addAnswer(DBUsernameQuestion, username);
    answers.addAnswer(DBPasswordQuestion, password);
  }
}
