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

public class CloudConfigurationUpdater extends BaseConfigurationUpdater {
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

  private final Question agentPasswordQuestion    = new Question("cloud.agent.password",
                                                                 "Set cloud-agent password",
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
      interaction.println("Cloud settings");
      answers.addBlockName("Cloud settings");

      String prevTenantMasterhost = ConfigUtils.findProperty(new File(previousTomcatDir, "bin"),
                                                             "environment.sh",
                                                             "TENANT_MASTERHOST");
      String prevAgentUsername = ConfigUtils.findProperty(new File(previousTomcatDir, "bin"),
                                                          "environment.sh",
                                                          "CLOUD_AGENT_USERNAME");
      String prevAgentPassword = ConfigUtils.findProperty(new File(previousTomcatDir, "bin"),
                                                          "environment.sh",
                                                          "CLOUD_AGENT_PASSWORD");

      clearBlock();
      addToBlock(tenantMasterhostQuestion, prevTenantMasterhost);
      addToBlock(agentUsernameQuestion, prevAgentUsername);
      addToBlock(agentPasswordQuestion, prevAgentPassword);

      boolean usePrev = false;
      if (wasBlockChanged()) {
        usePrev = interaction.ask(new PreviousQuestion(getChanges())).equals("yes");
      }

      String tenantMasterhost = prevTenantMasterhost;
      String agentUsername = prevAgentUsername;
      String agentPassword = prevAgentPassword;
      if (!usePrev) {
        tenantMasterhostQuestion.setDefaults(prevTenantMasterhost);
        agentUsernameQuestion.setDefaults(prevAgentUsername);
        agentPasswordQuestion.setDefaults(prevAgentPassword);

        tenantMasterhost = interaction.ask(tenantMasterhostQuestion);
        agentUsername = interaction.ask(agentUsernameQuestion);
        agentPassword = interaction.ask(agentPasswordQuestion);
      }

      ConfigUtils.writeProperty(new File(tomcatDir, "bin"),
                                "environment.sh",
                                "TENANT_MASTERHOST",
                                tenantMasterhost);
      answers.addAnswer(tenantMasterhostQuestion, tenantMasterhost);
      ConfigUtils.writeProperty(new File(tomcatDir, "bin"),
                                "environment.sh",
                                "CLOUD_AGENT_USERNAME",
                                agentUsername);
      answers.addAnswer(agentUsernameQuestion, agentUsername);
      ConfigUtils.writeProperty(new File(tomcatDir, "bin"),
                                "environment.sh",
                                "CLOUD_AGENT_PASSWORD",
                                agentPassword);
      answers.addAnswer(agentPasswordQuestion, agentPassword);
    } catch (IOException e) {
      throw new ConfigurationException("Updating cloud configuration failed", e);
    }
  }

}
