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

public class CloudConfigurationUpdater implements ConfigurationUpdater {
  private final Question tenantMasterhostQuestion = new Question("tenant.masterhost",
                                                                 "Set tenant masterhost",
                                                                 "cloud-workspaces.com",
                                                                 "^.*$",
                                                                 null);

  private final Question agentUsernameQuestion    = new Question("cloud.agent.username",
                                                                 "Set cloud-agent username",
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
    interaction.println("Cloud settings");
    answers.addBlockName("Cloud settings");

    AdminConfiguration prevConfiguration = prevAdmin.getAdminConfiguration();
    AdminConfiguration currConfiguration = currAdmin.getAdminConfiguration();

    String prevTenantMasterhost = prevConfiguration.getCurrentOrDefault("tenant.masterhost");
    String prevAgentUsername = prevConfiguration.getCurrentOrDefault("cloud.agent.username");

    tenantMasterhostQuestion.setDefaults(prevTenantMasterhost);
    agentUsernameQuestion.setDefaults(prevAgentUsername);

    String tenantMasterhost = prevTenantMasterhost;
    String agentUsername = prevAgentUsername;
    if (!interaction.askGroup(tenantMasterhostQuestion, agentUsernameQuestion)) {
      tenantMasterhost = interaction.ask(tenantMasterhostQuestion);
      agentUsername = interaction.ask(agentUsernameQuestion);
    }
    currConfiguration.set("tenant.masterhost", tenantMasterhost);
    currConfiguration.set("cloud.agent.username", agentUsername);
    answers.addAnswer(tenantMasterhostQuestion, tenantMasterhost);
    answers.addAnswer(agentUsernameQuestion, agentUsername);
  }
}
