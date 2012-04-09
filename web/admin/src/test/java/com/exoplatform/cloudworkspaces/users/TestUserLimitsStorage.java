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

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class TestUserLimitsStorage {

  private static final String TENANT1       = "tenant1";

  private static final int    TENANT1_LIMIT = 5;

  private static final String TENANT2       = "tenant2";

  private static final int    TENANT2_LIMIT = 15;

  private static final String TENANT3       = "tenant3";

  private Configuration       cloudAdminConfiguration;

  private UserLimitsStorage   userLimitsStorage;

  private File                userLimitFile;

  @BeforeMethod
  public void prepareConfigurationWithUserLimitFile() throws ConfigurationException,
                                                     FileNotFoundException,
                                                     IOException {
    cloudAdminConfiguration = new CompositeConfiguration();

    userLimitFile = new File("target/test-classes/user-limits.properties");
    Properties userLimit = new Properties();
    userLimit.setProperty(TENANT1, String.valueOf(TENANT1_LIMIT));
    userLimit.setProperty(TENANT2, String.valueOf(TENANT2_LIMIT));
    FileOutputStream stream = new FileOutputStream(userLimitFile);
    try {
      userLimit.store(new FileOutputStream(userLimitFile), "");
    } finally {
      stream.close();
    }
    System.setProperty("cloud.admin.userlimit", userLimitFile.getAbsolutePath());

    this.userLimitsStorage = new UserLimitsStorage(cloudAdminConfiguration);
    this.userLimitsStorage.setRefreshDelay(100);
  }

  @Test
  public void testGetMaxUsersForExistedTenant() {
    Assert.assertEquals(userLimitsStorage.getMaxUsersForTenant(TENANT1), TENANT1_LIMIT);
  }

  @Test
  public void testGetMaxUsersForNotExistedTenant() {
    Assert.assertEquals(userLimitsStorage.getMaxUsersForTenant(TENANT3), 20);
  }

  @Test
  public void testGetMaxUsersForNotExistedTenantWithChangedCloudAdminTenantMaxUsers() {
    cloudAdminConfiguration.setProperty(UserLimitsStorage.CLOUD_ADMIN_TENANT_MAXUSERS, 50);

    Assert.assertEquals(userLimitsStorage.getMaxUsersForTenant(TENANT3), 50);
  }

  @Test
  public void testReloading() throws FileNotFoundException, IOException, InterruptedException {
    long lastModified = userLimitsStorage.getLastModifiedTime();

    /*
     * lastModified flag for files stores in seconds, so, wait one second while
     * changing of file was changed lastModified flag.
     */
    Thread.sleep(1000);

    Properties userLimits = new Properties();
    FileInputStream stream = new FileInputStream(userLimitFile);
    try {
      userLimits.load(new FileInputStream(userLimitFile));
    } finally {
      stream.close();
    }
    userLimits.setProperty(TENANT1, String.valueOf(10));
    FileOutputStream out = new FileOutputStream(userLimitFile);
    try {
      userLimits.store(out, "");
    } finally {
      out.close();
    }

    Assert.assertNotEquals(userLimitsStorage.getLastModifiedTime(), lastModified);
    Assert.assertEquals(userLimitsStorage.getMaxUsersForTenant(TENANT1), 10);
  }

}
