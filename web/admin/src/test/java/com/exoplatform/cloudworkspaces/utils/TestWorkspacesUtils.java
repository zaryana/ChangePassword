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

import com.exoplatform.cloudworkspaces.CloudIntranetUtils;
import com.exoplatform.cloudworkspaces.EmailBlacklist;
import com.exoplatform.cloudworkspaces.ReferencesManager;
import com.exoplatform.cloudworkspaces.http.WorkspacesOrganizationRequestPerformer;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.dao.TenantInfoDataManager;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestWorkspacesUtils {

  CloudIntranetUtils utils;

  @BeforeMethod
  public void initMocks() {
    Configuration cloudAdminConfiguration = new CompositeConfiguration();
    ReferencesManager referencesManager = new ReferencesManager(cloudAdminConfiguration);
    EmailBlacklist blacklist = Mockito.mock(EmailBlacklist.class);
    WorkspacesOrganizationRequestPerformer organizationRequestPerformer = Mockito.mock(WorkspacesOrganizationRequestPerformer.class);
    TenantInfoDataManager tenantInfoDataManager = Mockito.mock(TenantInfoDataManager.class);
    utils = new CloudIntranetUtils(cloudAdminConfiguration,
                                   referencesManager,
                                   blacklist,
                                   organizationRequestPerformer,
                                   tenantInfoDataManager);
  }

  @DataProvider(name = "emails1")
  public Object[][] data() {
    return new Object[][] { { "test1@exoplatform.com", true },
        { "test1@exoplatform.com.ua.net", true }, { "test2@oracle", false }, { "@oracle", false },
        { "oracle", false }, { "test2@", false }, { "test2", false } };
  }

  @DataProvider(name = "emails2")
  public Object[][] data2() {
    return new Object[][] { { "test1@exoplatform.com", "exoplatform" },
        { "test1@exoplatform.com.ua.net", "exoplatform" },
        { "test1@exoplatform.fed.us", "exoplatform" },
        { "test1@exoplatform.crimea.ua", "exoplatform-crimea" },
        { "test1@exoplatform.crimea.zp.ua", "exoplatform-crimea-zp" },
        { "test1@exoplatform.crimea.pa.us", "exoplatform-crimea-pa" } };
  }

  @Test(dataProvider = "emails1")
  public void testValidateEmail(String email, boolean result) {
    Assert.assertEquals(utils.validateEmail(email), result);
  }

  @Test(dataProvider = "emails2")
  public void testEmail2TenantName(String email, String result) {
    System.setProperty("cloud.admin.hostname.file", "target/test-classes/hostname.cfg");
    Assert.assertEquals(utils.email2userMailInfo(email).getTenant(), result);
  }

}
