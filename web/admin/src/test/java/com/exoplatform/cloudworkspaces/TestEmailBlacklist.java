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
package com.exoplatform.cloudworkspaces;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class TestEmailBlacklist {

  private Configuration  cloudAdminConfiguration;

  private EmailBlacklist emailBlacklist;

  private File           blacklistFile;

  @BeforeMethod
  public void init() throws ConfigurationException {
    cloudAdminConfiguration = new CompositeConfiguration();
    cloudAdminConfiguration.setProperty(EmailBlacklist.CLOUD_ADMIN_BLACKLIST_FILE,
                                        "target/test-classes/test-email.blacklist");
    System.setProperty("cloud.admin.hostname.file", "target/test-classes/hostname.cfg");
    blacklistFile = new File("target/test-classes/test-email.blacklist");

    emailBlacklist = new EmailBlacklist(cloudAdminConfiguration);
  }

  @DataProvider(name = "email-blacklist")
  public Object[][] emailsDataProvider() {
    return new Object[][] { { "test0@exoplatform.com", false }, { "test1@mailinator.com", true },
        { "email@mail.com", true }, { "email@fakemail.gov", true }, { "test@gmail.com", false },
        { "test@vlskdfjavlejqf.com", false } };
  }

  @Test(dataProvider = "email-blacklist")
  public void testCheckBlacklist(String email, boolean result) {
    Assert.assertEquals(emailBlacklist.isInBlackList(email), result);
  }

  @Test
  public void testAutoReloadingBlacklistFile() throws FileNotFoundException, InterruptedException {
    Assert.assertFalse(emailBlacklist.isInBlackList("email@gmail.com"));
    Assert.assertTrue(emailBlacklist.isInBlackList("test@mail.com"));
    Assert.assertTrue(emailBlacklist.isInBlackList("mail@mailinator.com"));

    File oldBlacklistFile = new File(blacklistFile.getParentFile(), "old-test-email.blacklist");
    blacklistFile.renameTo(oldBlacklistFile);

    PrintWriter out = new PrintWriter(blacklistFile);
    try {
      out.println("gmail.com");
      out.println("mailinator.com");
    } finally {
      out.close();
    }

    Assert.assertTrue(emailBlacklist.isInBlackList("email@gmail.com"));
    Assert.assertFalse(emailBlacklist.isInBlackList("test@mail.com"));
    Assert.assertTrue(emailBlacklist.isInBlackList("mail@mailinator.com"));

    oldBlacklistFile.renameTo(blacklistFile);
  }

}
