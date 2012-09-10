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
package com.exoplatform.cloudworkspaces.installer.versions;

import com.exoplatform.cloudworkspaces.installer.configuration.Admin;
import com.exoplatform.cloudworkspaces.installer.configuration.AdminConfiguration;
import com.exoplatform.cloudworkspaces.installer.configuration.AdminDirectories;
import com.exoplatform.cloudworkspaces.installer.configuration.parameters.ConfigurationParameter;
import com.exoplatform.cloudworkspaces.installer.configuration.parameters.PropertiesConfigurationParameter;
import com.exoplatform.cloudworkspaces.installer.configuration.parameters.TomcatUserConfigurationParameter;
import com.exoplatform.cloudworkspaces.installer.rest.M10CloudAdminServices;
import com.exoplatform.cloudworkspaces.installer.tomcat.AdminTomcatWrapperImpl;

import java.util.ArrayList;
import java.util.List;

public class Beta07Admin extends Admin {

  public Beta07Admin(AdminDirectories adminDirectories) {
    super(adminDirectories);

    this.adminConfiguration = new AdminConfiguration(this.adminDirectories,
                                                     generateConfigurationParameterList());
    this.cloudAdminServices = M10CloudAdminServices.class;
    this.adminTomcatWrapper = AdminTomcatWrapperImpl.class;
  }

  protected List<ConfigurationParameter> generateConfigurationParameterList() {
    // cloud settings
    List<ConfigurationParameter> parameterList = new ArrayList<ConfigurationParameter>();
    {
      PropertiesConfigurationParameter tenantMasterhost = new PropertiesConfigurationParameter("tenant.masterhost",
                                                                                               "cloud-workspaces.com");
      tenantMasterhost.addSource("admin.properties", "cloud.admin.frontend.server.host");
      parameterList.add(tenantMasterhost);
    }
    {
      PropertiesConfigurationParameter cloudAgentUsername = new PropertiesConfigurationParameter("cloud.agent.username",
                                                                                                 "cloud-agent");
      cloudAgentUsername.addSource("application-types/cloud-agent.properties", "username");
      parameterList.add(cloudAgentUsername);
    }
    {
      PropertiesConfigurationParameter backupId = new PropertiesConfigurationParameter("cloud.admin.tenant.backup.id",
                                                                                       null);
      backupId.addSource("admin.properties", "cloud.admin.tenant.backup.id");
      parameterList.add(backupId);
    }
    {
      PropertiesConfigurationParameter demoTenant = new PropertiesConfigurationParameter("cloud.admin.demo.tenant.name",
                                                                                         null);
      demoTenant.addSource("admin.properties", "cloud.admin.demo.tenant.name");
      parameterList.add(demoTenant);
    }

    // database settings
    {
      PropertiesConfigurationParameter adminDbUrl = new PropertiesConfigurationParameter("admin.db.url",
                                                                                         "localhost:3306");
      adminDbUrl.addSource("db-servers/db1.properties", "url", "jdbc:mysql://{}/");
      adminDbUrl.addSource("application-types/cloud-agent.properties", "db.url");
      parameterList.add(adminDbUrl);
    }
    {
      PropertiesConfigurationParameter adminDbUsername = new PropertiesConfigurationParameter("admin.db.username",
                                                                                              "dbuser");
      adminDbUsername.addSource("db-servers/db1.properties", "username");
      adminDbUsername.addSource("application-types/cloud-agent.properties", "db.username");
      parameterList.add(adminDbUsername);
    }
    {
      PropertiesConfigurationParameter adminDbPassword = new PropertiesConfigurationParameter("admin.db.password",
                                                                                              "dbpass");
      adminDbPassword.addSource("db-servers/db1.properties", "password");
      adminDbPassword.addSource("application-types/cloud-agent.properties", "db.password");
      parameterList.add(adminDbPassword);
    }

    // mail settings
    {
      PropertiesConfigurationParameter cloudAdminMailHost = new PropertiesConfigurationParameter("cloud.admin.mail.host",
                                                                                                 "smtp.gmail.com");
      cloudAdminMailHost.addSource("admin.properties", "cloud.admin.mail.host");
      cloudAdminMailHost.addSource("application-types/cloud-agent.properties", "mail.host");
      parameterList.add(cloudAdminMailHost);
    }
    {
      PropertiesConfigurationParameter cloudAdminMailPort = new PropertiesConfigurationParameter("cloud.admin.mail.port",
                                                                                                 "465");
      cloudAdminMailPort.addSource("admin.properties", "cloud.admin.mail.port");
      cloudAdminMailPort.addSource("application-types/cloud-agent.properties", "mail.port");
      parameterList.add(cloudAdminMailPort);
    }
    {
      PropertiesConfigurationParameter cloudAdminMailSmtpAuth = new PropertiesConfigurationParameter("cloud.admin.mail.smtp.auth",
                                                                                                     "true");
      cloudAdminMailSmtpAuth.addSource("admin.properties", "cloud.admin.mail.smtp.auth");
      parameterList.add(cloudAdminMailSmtpAuth);
    }
    {
      PropertiesConfigurationParameter cloudAdminMailSmtpAuthUsername = new PropertiesConfigurationParameter("cloud.admin.mail.smtp.auth.username",
                                                                                                             "exo.plf.cloud.test1@gmail.com");
      cloudAdminMailSmtpAuthUsername.addSource("admin.properties",
                                               "cloud.admin.mail.smtp.auth.username");
      cloudAdminMailSmtpAuthUsername.addSource("application-types/cloud-agent.properties",
                                               "mail.user");
      parameterList.add(cloudAdminMailSmtpAuthUsername);
    }
    {
      PropertiesConfigurationParameter cloudAdminMailSmtpAuthPassword = new PropertiesConfigurationParameter("cloud.admin.mail.smtp.auth.password",
                                                                                                             "exo.plf.cloud.test112321");
      cloudAdminMailSmtpAuthPassword.addSource("admin.properties",
                                               "cloud.admin.mail.smtp.auth.password");
      cloudAdminMailSmtpAuthPassword.addSource("application-types/cloud-agent.properties",
                                               "mail.password");
      parameterList.add(cloudAdminMailSmtpAuthPassword);
    }
    {
      PropertiesConfigurationParameter cloudAdminMailAdminEmail = new PropertiesConfigurationParameter("cloud.admin.mail.admin.email",
                                                                                                       "exo.plf.cloud.test1@gmail.com");
      cloudAdminMailAdminEmail.addSource("admin.properties", "cloud.admin.mail.admin.email");
      cloudAdminMailAdminEmail.addSource("application-types/cloud-agent.properties",
                                         "mail.admin.email");
      parameterList.add(cloudAdminMailAdminEmail);
    }
    {
      PropertiesConfigurationParameter cloudAdminMailSupportEmail = new PropertiesConfigurationParameter("cloud.admin.mail.support.email",
                                                                                                         "exo.plf.cloud.test1@gmail.com");
      cloudAdminMailSupportEmail.addSource("admin.properties", "cloud.admin.mail.support.email");
      parameterList.add(cloudAdminMailSupportEmail);
    }
    {
      PropertiesConfigurationParameter cloudAdminMailSupportFrom = new PropertiesConfigurationParameter("cloud.admin.mail.support.from",
                                                                                                        "exo.plf.cloud.test1@gmail.com");
      cloudAdminMailSupportFrom.addSource("admin.properties", "cloud.admin.mail.support.from");
      parameterList.add(cloudAdminMailSupportFrom);
    }
    {
      PropertiesConfigurationParameter cloudAdminMailSalesEmail = new PropertiesConfigurationParameter("cloud.admin.mail.sales.email",
                                                                                                       "exo.plf.cloud.test1@gmail.com");
      cloudAdminMailSalesEmail.addSource("admin.properties", "cloud.admin.mail.sales.email");
      parameterList.add(cloudAdminMailSalesEmail);
    }

    // AWS settings
    {
      PropertiesConfigurationParameter cloudAdminApplicationDefaultType = new PropertiesConfigurationParameter("cloud.admin.application.default.type",
                                                                                                               "cloud-agent");
      cloudAdminApplicationDefaultType.addSource("admin.properties",
                                                 "cloud.admin.application.default.type");
      parameterList.add(cloudAdminApplicationDefaultType);
    }
    {
      PropertiesConfigurationParameter cloudAdminApplicationCloudServiceType = new PropertiesConfigurationParameter("cloud.admin.application.cloud.service.type",
                                                                                                                    "com.exoplatform.cloud.admin.instance.aws.AWSCloudServerClient");
      cloudAdminApplicationCloudServiceType.addSource("admin.properties",
                                                      "cloud.admin.application.cloud.service.type");
      parameterList.add(cloudAdminApplicationCloudServiceType);
    }
    {
      PropertiesConfigurationParameter cloudClientName = new PropertiesConfigurationParameter("cloud.client.name",
                                                                                              "aws-ec2");
      cloudClientName.addSource("admin.properties", "cloud.client.name");
      parameterList.add(cloudClientName);
    }
    {
      PropertiesConfigurationParameter awsApiVersion = new PropertiesConfigurationParameter("aws-ec2.api-version",
                                                                                            "2011-05-15");
      awsApiVersion.addSource("admin.properties", "aws-ec2.api-version");
      parameterList.add(awsApiVersion);
    }
    {
      PropertiesConfigurationParameter awsEc2Identity = new PropertiesConfigurationParameter("aws-ec2.identity",
                                                                                             null);
      awsEc2Identity.addSource("admin.properties", "aws-ec2.identity");
      parameterList.add(awsEc2Identity);
    }
    {
      PropertiesConfigurationParameter awsEc2Credential = new PropertiesConfigurationParameter("aws-ec2.credential",
                                                                                               null);
      awsEc2Credential.addSource("admin.properties", "aws-ec2.credential");
      parameterList.add(awsEc2Credential);
    }

    // Instance settings
    {
      PropertiesConfigurationParameter availabilityZone = new PropertiesConfigurationParameter("types.cloud.agent.availability.zone",
                                                                                               null);
      availabilityZone.addSource("application-types/cloud-agent.properties", "availability.zone");
      parameterList.add(availabilityZone);
    }
    {
      PropertiesConfigurationParameter securityGroupName = new PropertiesConfigurationParameter("types.cloud.agent.security.group.name",
                                                                                                null);
      securityGroupName.addSource("application-types/cloud-agent.properties", "security.group.name");
      parameterList.add(securityGroupName);
    }
    {
      PropertiesConfigurationParameter keyName = new PropertiesConfigurationParameter("types.cloud.agent.key.name",
                                                                                      null);
      keyName.addSource("application-types/cloud-agent.properties", "key.name");
      parameterList.add(keyName);
    }
    {
      PropertiesConfigurationParameter instanceType = new PropertiesConfigurationParameter("types.cloud.agent.instance.type",
                                                                                           null);
      instanceType.addSource("application-types/cloud-agent.properties", "instance.type");
      parameterList.add(instanceType);
    }
    {
      PropertiesConfigurationParameter imageId = new PropertiesConfigurationParameter("types.cloud.agent.image.id",
                                                                                      null);
      imageId.addSource("application-types/cloud-agent.properties", "image.id");
      parameterList.add(imageId);
    }

    // Tomcat users
    parameterList.add(new TomcatUserConfigurationParameter("cloudadmin", "cloudadmin"));
    parameterList.add(new TomcatUserConfigurationParameter("cloudmanager", "cloudmanager"));
    return parameterList;
  }

}
