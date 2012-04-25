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
package com.exoplatform.cloudworkspaces.instance;

import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.instance.autoscaling.ConfigurationUserDataGenerator;
import org.exoplatform.cloudmanagement.admin.tenant.DatabaseServerSelectionAlgorithm;
import org.exoplatform.cloudmanagement.admin.util.AdminConfigurationUtil;
import org.exoplatform.cloudmanagement.status.DatabaseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkspacesUserDataGenerator extends ConfigurationUserDataGenerator {

  private static final Logger                    LOG = LoggerFactory.getLogger(WorkspacesUserDataGenerator.class);

  private final Configuration                    cloudAdminConfiguration;

  private final DatabaseServerSelectionAlgorithm databaseServerSelectionAlgorithm;

  public WorkspacesUserDataGenerator(Configuration cloudAdminConfiguration,
                                     DatabaseServerSelectionAlgorithm databaseServerSelectionAlgorithm) {
    this.cloudAdminConfiguration = cloudAdminConfiguration;
    this.databaseServerSelectionAlgorithm = databaseServerSelectionAlgorithm;
  }

  @Override
  public String generateUserData(Configuration instanceType,
                                 Configuration applicationServerConfiguration) {
    String superUserdata = super.generateUserData(instanceType, applicationServerConfiguration);
    StringBuilder userdataBuilder = new StringBuilder();
    userdataBuilder.append(superUserdata);
    userdataBuilder.append('&');
    try {
      String tenantMasterhost = AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration);
      DatabaseInfo database = databaseServerSelectionAlgorithm.selectServers().iterator().next();
      String url = database.getUrl();
      int starthost = url.indexOf("//") + 2;
      int endhost = url.indexOf(":", starthost);
      String host = url.substring(starthost, endhost);
      String username = database.getUsername();
      String password = database.getPassword();

      userdataBuilder.append(tenantMasterhost);
      userdataBuilder.append('&');
      userdataBuilder.append(host);
      userdataBuilder.append('&');
      userdataBuilder.append(username);
      userdataBuilder.append('&');
      userdataBuilder.append(password);

      return userdataBuilder.toString();
    } catch (CloudAdminException e) {
      LOG.error("User data generation failed.", e);
    }
    return null;
  }

}
