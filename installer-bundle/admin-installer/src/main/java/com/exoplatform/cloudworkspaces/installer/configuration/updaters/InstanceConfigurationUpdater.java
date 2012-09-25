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

public class InstanceConfigurationUpdater implements ConfigurationUpdater {
  private final Question asInstanceTypeQuestion      = new Question("as.instance.type",
                                                                    "Set server's instance type",
                                                                    null,
                                                                    "^.*$",
                                                                    null);

  private final Question asImageIdQuestion           = new Question("as.image.id",
                                                                    "Set application server's image id",
                                                                    null,
                                                                    "^.*$",
                                                                    null);

  private final Question asAvailabilityZoneQuestion  = new Question("as.availability.zone",
                                                                    "Set application server's availability zone",
                                                                    null,
                                                                    "^.*$",
                                                                    null);

  private final Question asSecurityGroupNameQuestion = new Question("as.security.group.name",
                                                                    "Set application server's security group name",
                                                                    null,
                                                                    "^.*$",
                                                                    null);

  private final Question asKeyNameQuestion           = new Question("as.key.name",
                                                                    "Set application server's key name",
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
    interaction.println("Instance settings");
    answers.addBlockName("Instance settings");

    AdminConfiguration prevConfiguration = prevAdmin.getAdminConfiguration();
    AdminConfiguration currConfiguration = currAdmin.getAdminConfiguration();

    String prevAvailabilityZone = prevConfiguration.getCurrentOrDefault("types.cloud.agent.availability.zone");
    String prevSecurityGroupName = prevConfiguration.getCurrentOrDefault("types.cloud.agent.security.group.name");
    String prevKeyName = prevConfiguration.getCurrentOrDefault("types.cloud.agent.key.name");
    String prevInstanceType = prevConfiguration.getCurrentOrDefault("types.cloud.agent.instance.type");

    asAvailabilityZoneQuestion.setDefaults(prevAvailabilityZone);
    asSecurityGroupNameQuestion.setDefaults(prevSecurityGroupName);
    asKeyNameQuestion.setDefaults(prevKeyName);
    asInstanceTypeQuestion.setDefaults(prevInstanceType);

    String availabilityZone = prevAvailabilityZone;
    String securityGroupName = prevSecurityGroupName;
    String keyName = prevKeyName;
    String instanceType = prevInstanceType;
    if (!interaction.askGroup(asAvailabilityZoneQuestion,
                              asSecurityGroupNameQuestion,
                              asKeyNameQuestion,
                              asInstanceTypeQuestion)) {
      availabilityZone = interaction.ask(asAvailabilityZoneQuestion);
      securityGroupName = interaction.ask(asSecurityGroupNameQuestion);
      keyName = interaction.ask(asKeyNameQuestion);
      instanceType = interaction.ask(asInstanceTypeQuestion);
    }
    currConfiguration.set("types.cloud.agent.availability.zone", availabilityZone);
    currConfiguration.set("types.cloud.agent.security.group.name", securityGroupName);
    currConfiguration.set("types.cloud.agent.key.name", keyName);
    currConfiguration.set("types.cloud.agent.instance.type", instanceType);
    answers.addAnswer(asAvailabilityZoneQuestion, availabilityZone);
    answers.addAnswer(asSecurityGroupNameQuestion, securityGroupName);
    answers.addAnswer(asKeyNameQuestion, keyName);
    answers.addAnswer(asInstanceTypeQuestion, instanceType);

    String prevImageId = prevConfiguration.getCurrentOrDefault("types.cloud.agent.image.id");
    asImageIdQuestion.setDefaults(prevImageId);
    String asImageId = interaction.ask(asImageIdQuestion);

    currConfiguration.set("types.cloud.agent.image.id", asImageId);
    answers.addAnswer(asImageIdQuestion, asImageId);
  }
}
