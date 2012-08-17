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

public class AwsConfigurationUpdater extends BaseConfigurationUpdater {
  private final Question cloudApplicationDefaultTypeQuestion = new Question("cloud.application.default.type",
                                                                            "Set cloud application default type",
                                                                            "cloud-agent",
                                                                            "^.*$",
                                                                            null);

  private final Question cloudServiceTypeQuestion            = new Question("cloud.service.type",
                                                                            "Set cloud service connector class",
                                                                            "com.exoplatform.cloud.admin.instance.aws.AWSCloudServerClient",
                                                                            "^.*$",
                                                                            null);

  private final Question cloudClientNameQuestion             = new Question("cloud.client.name",
                                                                            "Set cloud client name (for amazon aws-ec2)",
                                                                            "aws-ec2",
                                                                            "^.*$",
                                                                            null);

  private final Question cloudAwsVersionQuestion             = new Question("cloud.aws.version",
                                                                            "Set cloud aws api version",
                                                                            "2011-05-15",
                                                                            "^.*$",
                                                                            null);

  private final Question cloudAwsIdentityQuestion            = new Question("cloud.aws.identity",
                                                                            "Set cloud aws identity",
                                                                            null,
                                                                            "^.*$",
                                                                            null);

  private final Question cloudAwsCredentialsQuestion         = new Question("cloud.aws.credentials",
                                                                            "Set cloud aws credentials",
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
      interaction.println("Aws settings");

      String prevApplicationDefaultType = ConfigUtils.findProperty(previousConfDir,
                                                                   "environment.sh",
                                                                   "CLOUD_APPLICATION_DEFAULT_TYPE");
      String prevCloudServiceType = ConfigUtils.findProperty(previousConfDir,
                                                             "environment.sh",
                                                             "CLOUD_SERVICE_TYPE");
      String prevCloudClientName = ConfigUtils.findProperty(previousConfDir,
                                                            "environment.sh",
                                                            "CLOUD_CLIENT_NAME");
      String prevCloudAwsVersion = ConfigUtils.findProperty(previousConfDir,
                                                            "environment.sh",
                                                            "CLOUD_AWS_VERSION");

      clearBlock();
      addToBlock(cloudApplicationDefaultTypeQuestion, prevApplicationDefaultType);
      addToBlock(cloudServiceTypeQuestion, prevCloudServiceType);
      addToBlock(cloudClientNameQuestion, prevCloudClientName);
      addToBlock(cloudAwsVersionQuestion, prevCloudAwsVersion);

      boolean usePrev = false;
      if (wasBlockChanged()) {
        usePrev = interaction.ask(new PreviousQuestion(getChanges())).equals("yes");
      }

      String applicationDefaultType = prevApplicationDefaultType;
      String cloudServiceType = prevCloudServiceType;
      String cloudClientName = prevCloudClientName;
      String cloudAwsVersion = prevCloudAwsVersion;
      if (!usePrev) {
        cloudApplicationDefaultTypeQuestion.setDefaults(prevApplicationDefaultType);
        cloudServiceTypeQuestion.setDefaults(prevCloudServiceType);
        cloudClientNameQuestion.setDefaults(prevCloudClientName);
        cloudAwsVersionQuestion.setDefaults(prevCloudAwsVersion);

        applicationDefaultType = interaction.ask(cloudApplicationDefaultTypeQuestion);
        cloudServiceType = interaction.ask(cloudServiceTypeQuestion);
        cloudClientName = interaction.ask(cloudClientNameQuestion);
        cloudAwsVersion = interaction.ask(cloudAwsVersionQuestion);
      }
      ConfigUtils.writeProperty(confDir,
                                "environment.sh",
                                "CLOUD_APPLICATION_DEFAULT_TYPE",
                                applicationDefaultType);
      answers.addAnswer(cloudApplicationDefaultTypeQuestion, applicationDefaultType);
      ConfigUtils.writeProperty(confDir, "environment.sh", "CLOUD_SERVICE_TYPE", cloudServiceType);
      answers.addAnswer(cloudServiceTypeQuestion, cloudServiceType);
      ConfigUtils.writeProperty(confDir, "environment.sh", "CLOUD_CLIENT_NAME", cloudClientName);
      answers.addAnswer(cloudClientNameQuestion, cloudClientName);
      ConfigUtils.writeProperty(confDir, "environment.sh", "CLOUD_AWS_VERSION", cloudAwsVersion);
      answers.addAnswer(cloudAwsVersionQuestion, cloudAwsVersion);

      String prevAwsIdentity = ConfigUtils.findProperty(previousConfDir,
                                                        "environment.sh",
                                                        "CLOUD_AWS_IDENTITY");
      String prevAwsCredentials = ConfigUtils.findProperty(previousConfDir,
                                                           "environment.sh",
                                                           "CLOUD_AWS_CREDENTIALS");

      clearBlock();
      addToBlock(cloudAwsIdentityQuestion, prevAwsIdentity);
      addToBlock(cloudAwsCredentialsQuestion, prevAwsCredentials);

      usePrev = false;
      if (wasBlockChanged()) {
        usePrev = interaction.ask(new PreviousQuestion(getChanges())).equals("yes");
      }

      String awsIdentity = prevAwsIdentity;
      String awsCredentials = prevAwsCredentials;
      if (!usePrev) {
        cloudAwsIdentityQuestion.setDefaults(prevAwsIdentity);
        cloudAwsCredentialsQuestion.setDefaults(prevAwsCredentials);

        awsIdentity = interaction.ask(cloudAwsIdentityQuestion);
        awsCredentials = interaction.ask(cloudAwsCredentialsQuestion);
      }
      ConfigUtils.writeProperty(confDir, "environment.sh", "CLOUD_AWS_IDENTITY", awsIdentity);
      answers.addAnswer(cloudAwsIdentityQuestion, awsIdentity);
      ConfigUtils.writeProperty(confDir, "environment.sh", "CLOUD_AWS_CREDENTIALS", awsCredentials);
      answers.addAnswer(cloudAwsCredentialsQuestion, awsCredentials);
    } catch (IOException e) {
      throw new ConfigurationException("Updating autoscaling configuration failed", e);
    }
  }

}
