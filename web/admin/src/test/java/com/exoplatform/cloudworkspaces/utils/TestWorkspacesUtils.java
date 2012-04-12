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
package com.exoplatform.cloudworkspaces.utils;


import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import com.exoplatform.cloudworkspaces.ReferencesManager;
import com.exoplatform.cloudworkspaces.UserRequestDAO;
import com.exoplatform.cloudworkspaces.NotificationMailSender;
import com.exoplatform.cloudworkspaces.CloudIntranetUtils;
import org.mockito.Mockito;
import com.exoplatform.cloudworkspaces.users.UserLimitsStorage;
import com.exoplatform.cloudworkspaces.users.UsersManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class TestWorkspacesUtils {
  
  CloudIntranetUtils utils;
  
  @BeforeMethod
  public void initMocks() {
    Configuration cloudAdminConfiguration = new CompositeConfiguration();
    NotificationMailSender notificationMailSender = Mockito.mock(NotificationMailSender.class);
    UserRequestDAO requestDao = new UserRequestDAO(cloudAdminConfiguration);
    ReferencesManager referencesManager = new ReferencesManager(cloudAdminConfiguration);
    utils = new CloudIntranetUtils(cloudAdminConfiguration, notificationMailSender, requestDao, referencesManager);
  }
  
  @DataProvider(name = "emails1")
  public Object[][] data() {
      return new Object[][]{
        {"test1@exoplatform.com", true}, 
        {"test1@exoplatform.com.ua.net", true},
        {"test2@oracle", false},
        {"@oracle", false},
        {"oracle", false},
        {"test2@", false},
        {"test2", false}
       };
  }

  @DataProvider(name = "emails2")
  public Object[][] data2() {
    return new Object[][]{
            {"test1@exoplatform.com", "exoplatform"},
            {"test1@exoplatform.com.ua.net", "exoplatform"},
            {"test1@exoplatform.fed.us", "exoplatform"},
            {"test1@exoplatform.crimea.ua", "exoplatform-crimea"},
            {"test1@exoplatform.crimea.zp.ua", "exoplatform-crimea-zp"},
    };
  }
  
  @Test(dataProvider = "emails1")
  public void testValidateEmail(String email, boolean result) {
    Assert.assertEquals(utils.validateEmail(email), result);
  }

  @Test(dataProvider = "emails2")
  public void testEmail2TenantName(String email, String result) {
    System.setProperty("cloud.admin.hostname.file", "target/test-classes/hostname.cfg");
    Assert.assertEquals(utils.email2tenantName(email), result);
  }
  
}