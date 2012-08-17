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
import com.exoplatform.cloudworkspaces.installer.configuration.BaseConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationException;
import com.exoplatform.cloudworkspaces.installer.configuration.PreviousQuestion;
import com.exoplatform.cloudworkspaces.installer.configuration.Question;
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;

import java.io.File;
import java.io.IOException;

public class InstanceConfigurationUpdater extends BaseConfigurationUpdater {
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
  public void update(File confDir,
                     File tomcatDir,
                     File previousConfDir,
                     File previousTomcatDir,
                     InteractionManager interaction,
                     AnswersManager answers) throws ConfigurationException {
    try {
      interaction.println("");
      interaction.println("");
      interaction.println("Instance settings");

      String prevAvailabilityZone = ConfigUtils.findProperty(previousConfDir,
                                                             "environment.sh",
                                                             "AS_AVAILABILITY_ZONE");
      String prevSecurityGroupName = ConfigUtils.findProperty(previousConfDir,
                                                              "environment.sh",
                                                              "AS_SECURITY_GROUP_NAME");
      String prevKeyName = ConfigUtils.findProperty(previousConfDir,
                                                    "environment.sh",
                                                    "AS_KEY_NAME");

      clearBlock();
      addToBlock(asAvailabilityZoneQuestion, prevAvailabilityZone);
      addToBlock(asSecurityGroupNameQuestion, prevSecurityGroupName);
      addToBlock(asKeyNameQuestion, prevKeyName);

      boolean usePrev = false;
      if (wasBlockChanged()) {
        usePrev = interaction.ask(new PreviousQuestion(getChanges())).equals("yes");
      }

      String availabilityZone = prevAvailabilityZone;
      String securityGroupName = prevSecurityGroupName;
      String keyName = prevKeyName;
      if (!usePrev) {
        asAvailabilityZoneQuestion.setDefaults(prevAvailabilityZone);
        asSecurityGroupNameQuestion.setDefaults(prevSecurityGroupName);
        asKeyNameQuestion.setDefaults(prevKeyName);

        availabilityZone = interaction.ask(asAvailabilityZoneQuestion);
        securityGroupName = interaction.ask(asSecurityGroupNameQuestion);
        keyName = interaction.ask(asKeyNameQuestion);
      }
      ConfigUtils.writeProperty(confDir, "environment.sh", "AS_AVAILABILITY_ZONE", availabilityZone);
      answers.addAnswer(asAvailabilityZoneQuestion, availabilityZone);
      ConfigUtils.writeProperty(confDir,
                                "environment.sh",
                                "AS_SECURITY_GROUP_NAME",
                                securityGroupName);
      answers.addAnswer(asSecurityGroupNameQuestion, securityGroupName);
      ConfigUtils.writeProperty(confDir, "environment.sh", "AS_KEY_NAME", keyName);
      answers.addAnswer(asKeyNameQuestion, keyName);

      asImageIdQuestion.setDefaults(ConfigUtils.findProperty(previousConfDir,
                                                             "environment.sh",
                                                             "AS_IMAGE_ID"));
      String asImageId = interaction.ask(asImageIdQuestion);

      ConfigUtils.writeProperty(confDir, "environment.sh", "AS_IMAGE_ID", asImageId);
      answers.addAnswer(asImageIdQuestion, asImageId);
    } catch (IOException e) {
      throw new ConfigurationException("Updating instance configuration failed", e);
    }
  }

}
