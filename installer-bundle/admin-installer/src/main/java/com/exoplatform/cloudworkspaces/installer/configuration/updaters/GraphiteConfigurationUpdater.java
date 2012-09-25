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
import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.CurrentAdmin;
import com.exoplatform.cloudworkspaces.installer.configuration.PreviousAdmin;
import com.exoplatform.cloudworkspaces.installer.configuration.Question;
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;

public class GraphiteConfigurationUpdater implements ConfigurationUpdater {

  private final Question graphiteHostQuestion = new Question("graphite.host",
                                                             "Set graphite host. For example: localhost",
                                                             "localhost",
                                                             "^.*$",
                                                             null);

  private final Question graphitePortQuestion = new Question("graphite.port",
                                                             "Set graphite port",
                                                             "2003",
                                                             "^.*$",
                                                             null);

  @Override
  public void update(PreviousAdmin prevAdmin,
                     CurrentAdmin currAdmin,
                     InteractionManager interaction,
                     AnswersManager answers) throws InstallerException {
    interaction.println("");
    interaction.println("");
    interaction.println("Graphite settings");
    answers.addBlockName("Graphite settings");

    AdminConfiguration prevConfiguration = prevAdmin.getAdminConfiguration();
    AdminConfiguration currConfiguration = currAdmin.getAdminConfiguration();

    String prevGraphiteHost = prevConfiguration.getCurrentOrDefault("graphite.host");
    String prevGraphitePort = prevConfiguration.getCurrentOrDefault("graphite.port");

    graphiteHostQuestion.setDefaults(prevGraphiteHost);
    graphitePortQuestion.setDefaults(prevGraphitePort);

    String graphiteHost = prevGraphiteHost;
    String graphitePort = prevGraphitePort;
    if (!interaction.askGroup(graphiteHostQuestion, graphitePortQuestion)) {
      graphiteHost = interaction.ask(graphiteHostQuestion);
      graphitePort = interaction.ask(graphitePortQuestion);
    }
    currConfiguration.set("graphite.host", graphiteHost);
    currConfiguration.set("graphite.port", graphitePort);
    answers.addAnswer(graphiteHostQuestion, graphiteHost);
    answers.addAnswer(graphitePortQuestion, graphitePort);
  }

}
