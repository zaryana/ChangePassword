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
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class TestChangePasswordManager {

  private static final String   PASSWORD_REFERENCES_DIR = "target/test-classes/password/";

  private static final String   EMAIL                   = "test@mail.com";

  private static final String   UUID                    = "12345-67890-abcdef";

  private static final long     TIME_NOT_EXPIRED        = System.currentTimeMillis();

  private static final long     TIME_EXPIRED            = System.currentTimeMillis()
                                                            - ChangePasswordManager.REFERENCE_TIME_LIMIT;

  private Configuration         cloudAdminConfiguration;

  private ChangePasswordManager changePasswordManager;

  @BeforeMethod
  public void initDependencies() {
    cloudAdminConfiguration = new CompositeConfiguration();
    cloudAdminConfiguration.setProperty(ChangePasswordManager.CLOUD_ADMIN_PASSWORD_REFERENCES_DIR,
                                        PASSWORD_REFERENCES_DIR);

    changePasswordManager = new ChangePasswordManager(cloudAdminConfiguration);

    clear();
  }

  @Test
  public void testAddReference() throws CloudAdminException, IOException {
    String uuid = changePasswordManager.addReference(EMAIL);

    Assert.assertNotNull(getReferenceBy("email", EMAIL));
    Assert.assertTrue(getReferenceBy("email", EMAIL).containsKey("email"));
    Assert.assertEquals(uuid, getReferenceBy("email", EMAIL).getProperty("uuid"));
    Assert.assertTrue(getReferenceBy("email", EMAIL).containsKey("created"));
  }

  @Test
  public void testValidateReferenceIfOk() throws CloudAdminException, IOException {
    addReference(EMAIL, UUID, TIME_NOT_EXPIRED);

    Assert.assertEquals(changePasswordManager.validateReference(UUID), EMAIL);
  }

  @Test(expectedExceptions = { CloudAdminException.class })
  public void testValidateReferenceIfUuidNotFound() throws CloudAdminException, IOException {
    changePasswordManager.validateReference(UUID);
  }

  @Test(expectedExceptions = { CloudAdminException.class })
  public void testValidateReferenceIfTimeExpired() throws IOException, CloudAdminException {
    addReference(EMAIL, UUID, TIME_EXPIRED);

    Assert.assertTrue(new File(PASSWORD_REFERENCES_DIR, UUID + ".properties").exists());
    changePasswordManager.validateReference(UUID);
    Assert.assertFalse(new File(PASSWORD_REFERENCES_DIR, UUID + ".properties").exists());

    Assert.assertNull(getReferenceBy("uuid", UUID));
  }

  private void clear() {
    File referencesDir = new File(cloudAdminConfiguration.getString(ChangePasswordManager.CLOUD_ADMIN_PASSWORD_REFERENCES_DIR));
    if (!referencesDir.exists()) {
      Assert.assertTrue(referencesDir.mkdirs());
    } else {
      for (File reference : referencesDir.listFiles()) {
        Assert.assertTrue(reference.delete());
      }
    }
  }

  private void addReference(String email, String uuid, long time) throws IOException {
    File reference = new File(PASSWORD_REFERENCES_DIR, uuid + ".properties");

    Properties properties = new Properties();
    properties.setProperty("email", email);
    properties.setProperty("uuid", uuid);
    properties.setProperty("created", String.valueOf(time));

    FileOutputStream fout = new FileOutputStream(reference);
    try {
      properties.store(fout, null);
    } finally {
      fout.close();
    }
  }

  private Properties getReferenceBy(String key, String value) throws IOException {
    File referencesDir = new File(PASSWORD_REFERENCES_DIR);

    for (File reference : referencesDir.listFiles()) {
      Properties properties = new Properties();

      FileInputStream fin = new FileInputStream(reference);
      try {
        properties.load(fin);
        if (properties.getProperty(key).equals(value)) {
          return properties;
        }
      } finally {
        fin.close();
      }
    }
    return null;
  }

}
