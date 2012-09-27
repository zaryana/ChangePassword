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

public class CloudClientConfigurationUpdater implements ConfigurationUpdater {
  private final Question applicationDefaultTypeQuestion = new Question("application.default.type",
                                                                       "Set application default type",
                                                                       "cloud-agent",
                                                                       "^.*$",
                                                                       null);

  private final Question cloudServiceTypeQuestion       = new Question("cloud.service.type",
                                                                       "Set cloud service connector class",
                                                                       "com.exoplatform.cloud.admin.instance.aws.AWSCloudServerClient",
                                                                       "^.*$",
                                                                       null);

  private final Question cloudClientNameQuestion        = new Question("cloud.client.name",
                                                                       "Set cloud client name (for amazon aws-ec2)",
                                                                       "aws-ec2",
                                                                       "^.*$",
                                                                       null);

  @Override
  public void update(PreviousAdmin prevAdmin,
                     CurrentAdmin currAdmin,
                     InteractionManager interaction,
                     AnswersManager answers) throws InstallerException {
    interaction.println("");
    interaction.println("");
    interaction.println("Cloud client settings");
    answers.addBlockName("Cloud client settings");

    AdminConfiguration prevConfiguration = prevAdmin.getAdminConfiguration();
    AdminConfiguration currConfiguration = currAdmin.getAdminConfiguration();
    String prevApplicationDefaultType = prevConfiguration.getCurrentOrDefault("cloud.admin.application.default.type");
    String prevCloudServiceType = prevConfiguration.getCurrentOrDefault("cloud.admin.application.cloud.service.type");
    String prevCloudClientName = prevConfiguration.getCurrentOrDefault("cloud.client.name");

    applicationDefaultTypeQuestion.setDefaults(prevApplicationDefaultType);
    cloudServiceTypeQuestion.setDefaults(prevCloudServiceType);
    cloudClientNameQuestion.setDefaults(prevCloudClientName);

    String applicationDefaultType = prevApplicationDefaultType;
    String cloudServiceType = prevCloudServiceType;
    String cloudClientName = prevCloudClientName;
    if (!interaction.askGroup(applicationDefaultTypeQuestion,
                             cloudServiceTypeQuestion,
                             cloudClientNameQuestion)) {
      applicationDefaultType = interaction.ask(applicationDefaultTypeQuestion);
      cloudServiceType = interaction.ask(cloudServiceTypeQuestion);
      cloudClientName = interaction.ask(cloudClientNameQuestion);
    }
    currConfiguration.set("cloud.admin.application.default.type", applicationDefaultType);
    currConfiguration.set("cloud.admin.application.cloud.service.type", cloudServiceType);
    currConfiguration.set("cloud.client.name", cloudClientName);
    answers.addAnswer(applicationDefaultTypeQuestion, applicationDefaultType);
    answers.addAnswer(cloudServiceTypeQuestion, cloudServiceType);
    answers.addAnswer(cloudClientNameQuestion, cloudClientName);
  }

}
