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
package com.exoplatform.cloudworkspaces.users;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

public class UserLimitsStorage {

  private static final String           CLOUD_ADMIN_TENANT_MAXUSERS = "cloud.admin.tenant.maxusers";

  private final Configuration           cloudAdminConfiguration;

  private final PropertiesConfiguration userLimits;

  public UserLimitsStorage(Configuration cloudAdminConfiguration) throws ConfigurationException {
    this.cloudAdminConfiguration = cloudAdminConfiguration;
    String filePath = System.getProperty("cloud.admin.userlimit", null);
    if (filePath != null) {
      this.userLimits = new PropertiesConfiguration(filePath);
      this.userLimits.setReloadingStrategy(new FileChangedReloadingStrategy());
    } else {
      this.userLimits = null;
    }
  }

  public int getMaxUsersForTenant(String tName) {
    if (userLimits != null) {
      return userLimits.getInt(tName);
    }
    return cloudAdminConfiguration.getInt(CLOUD_ADMIN_TENANT_MAXUSERS, 20);
  }

}
