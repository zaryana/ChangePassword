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

public class AwsConfigurationUpdater implements ConfigurationUpdater {
  private final Question cloudAwsVersionQuestion     = new Question("cloud.aws.version",
                                                                    "Set cloud aws api version",
                                                                    "2011-05-15",
                                                                    "^.*$",
                                                                    null);

  private final Question cloudAwsIdentityQuestion    = new Question("cloud.aws.identity",
                                                                    "Set cloud aws identity",
                                                                    null,
                                                                    "^.*$",
                                                                    null);

  private final Question cloudAwsCredentialsQuestion = new Question("cloud.aws.credentials",
                                                                    "Set cloud aws credentials",
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
    interaction.println("Aws settings");
    answers.addBlockName("Aws settings");

    AdminConfiguration prevConfiguration = prevAdmin.getAdminConfiguration();
    AdminConfiguration currConfiguration = currAdmin.getAdminConfiguration();

    String prevCloudAwsVersion = prevConfiguration.getCurrentOrDefault("aws-ec2.api-version");
    String prevAwsIdentity = prevConfiguration.getCurrentOrDefault("aws-ec2.identity");
    String prevAwsCredentials = prevConfiguration.getCurrentOrDefault("aws-ec2.credential");

    cloudAwsVersionQuestion.setDefaults(prevCloudAwsVersion);
    cloudAwsIdentityQuestion.setDefaults(prevAwsIdentity);
    cloudAwsCredentialsQuestion.setDefaults(prevAwsCredentials);

    String cloudAwsVersion = prevCloudAwsVersion;
    String awsIdentity = prevAwsIdentity;
    String awsCredentials = prevAwsCredentials;
    if (interaction.askGroup(cloudAwsVersionQuestion,
                             cloudAwsIdentityQuestion,
                             cloudAwsCredentialsQuestion)) {
      cloudAwsVersion = interaction.ask(cloudAwsVersionQuestion);
      awsIdentity = interaction.ask(cloudAwsIdentityQuestion);
      awsCredentials = interaction.ask(cloudAwsCredentialsQuestion);
    }
    currConfiguration.set("aws-ec2.api-version", cloudAwsVersion);
    currConfiguration.set("aws-ec2.identity", awsIdentity);
    currConfiguration.set("aws-ec2.credential", awsCredentials);
    answers.addAnswer(cloudAwsVersionQuestion, cloudAwsVersion);
    answers.addAnswer(cloudAwsIdentityQuestion, awsIdentity);
    answers.addAnswer(cloudAwsCredentialsQuestion, awsCredentials);
  }

}
