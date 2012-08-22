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

import com.exoplatform.cloud.admin.CloudAdminException;
import com.exoplatform.cloud.admin.configuration.ApplicationServerConfiguration;
import com.exoplatform.cloud.admin.instance.ApplicationServerDataGenerator;
import com.exoplatform.cloud.admin.instance.ApplicationServerType;
import com.exoplatform.cloud.admin.tenant.DatabaseServerSelectionAlgorithm;
import com.exoplatform.cloud.admin.util.AdminConfigurationUtil;
import com.exoplatform.cloud.status.DatabaseInfo;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class WorkspacesUserDataGenerator implements ApplicationServerDataGenerator<String> {

  private static final Logger                    LOG = LoggerFactory.getLogger(WorkspacesUserDataGenerator.class);

  private final Configuration                    cloudAdminConfiguration;

  private final DatabaseServerSelectionAlgorithm databaseServerSelectionAlgorithm;

  public WorkspacesUserDataGenerator(Configuration cloudAdminConfiguration,
                                     DatabaseServerSelectionAlgorithm databaseServerSelectionAlgorithm) {
    this.cloudAdminConfiguration = cloudAdminConfiguration;
    this.databaseServerSelectionAlgorithm = databaseServerSelectionAlgorithm;
  }

  public String generate(Map<Object, Object> properties) {
    StringBuilder userdataBuilder = new StringBuilder();
    try {
      String volumeId = null;
      if (properties.containsKey(ApplicationServerType.PROPERTY_VOLUME_ID)) {
        volumeId = (String) properties.get(ApplicationServerType.PROPERTY_VOLUME_ID);
      }
      String tenantMasterhost = AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration);
      DatabaseInfo database = databaseServerSelectionAlgorithm.selectServers().iterator().next();
      String url = database.getUrl();
      int starthost = url.indexOf("//") + 2;
      int endhost = url.indexOf(":", starthost);
      String host = url.substring(starthost, endhost);
      String username = database.getUsername();
      String password = database.getPassword();

      userdataBuilder.append(volumeId);
      userdataBuilder.append('&');
      userdataBuilder.append(tenantMasterhost);
      userdataBuilder.append('&');
      userdataBuilder.append(host);
      userdataBuilder.append('&');
      userdataBuilder.append(username);
      userdataBuilder.append('&');
      userdataBuilder.append(password);

      userdataBuilder.append('&');
      userdataBuilder.append(System.getProperty("admin.agent.auth.username"));
      userdataBuilder.append('&');
      // userdataBuilder.append(System.getProperty("admin.agent.auth.password"));
      userdataBuilder.append(properties.get(ApplicationServerConfiguration.PASSWORD_PARAMETER));

      userdataBuilder.append('&');
      userdataBuilder.append(System.getProperty("cloud.admin.mail.admin.email"));
      userdataBuilder.append('&');
      userdataBuilder.append(System.getProperty("cloud.admin.mail.host"));
      userdataBuilder.append('&');
      userdataBuilder.append(System.getProperty("cloud.admin.mail.port"));
      userdataBuilder.append('&');
      userdataBuilder.append(System.getProperty("cloud.admin.mail.user"));
      userdataBuilder.append('&');
      userdataBuilder.append(System.getProperty("cloud.admin.mail.password"));
      userdataBuilder.append('&');
      userdataBuilder.append(System.getProperty("graphite.host"));
      userdataBuilder.append('&');
      userdataBuilder.append(System.getProperty("graphite.port"));

      return userdataBuilder.toString();
    } catch (CloudAdminException e) {
      LOG.error("User data generation failed.", e);
    }
    return null;
  }

}
