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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class TestReferencesManager {

  private static final String EMAIL1 = "test1@mail.com";

  private static final String EMAIL2 = "test2@mail.com";

  private static final String HASH1  = "12345-67890-abcdef";

  private static final String HASH2  = "qwert-yuiop-asdfgh";

  private Configuration       cloudAdminConfiguration;

  private ReferencesManager   referencesManager;

  @BeforeMethod
  public void initDependencies() {
    cloudAdminConfiguration = new CompositeConfiguration();
    cloudAdminConfiguration.setProperty(ReferencesManager.CLOUD_ADMIN_REFERENCES_DIR,
                                        "target/test-classes");
    cloudAdminConfiguration.setProperty(ReferencesManager.CLOUD_ADMIN_REFERENCES_FILE,
                                        "references.txt");

    referencesManager = new ReferencesManager(cloudAdminConfiguration);
  }

  @Test
  public void testGetHashIfItExists() throws IOException, CloudAdminException {
    clear();
    add(EMAIL1, HASH1);

    Assert.assertEquals(referencesManager.getHash(EMAIL1), HASH1);
  }

  @Test
  public void testGetHashIfItNotExists() throws IOException, CloudAdminException {
    clear();

    Assert.assertNull(referencesManager.getHash(EMAIL1));
  }

  @Test
  public void testGetEmailIfItExists() throws IOException, CloudAdminException {
    clear();
    add(EMAIL1, HASH1);

    Assert.assertEquals(referencesManager.getEmail(HASH1), EMAIL1);
  }

  @Test
  public void testGetEmailIfItNotExists() throws IOException, CloudAdminException {
    clear();

    Assert.assertNull(referencesManager.getEmail(HASH1));
  }

  @Test
  public void testPutEmailIfItExists() throws IOException, CloudAdminException {
    clear();
    add(EMAIL1, HASH1);

    Assert.assertEquals(referencesManager.putEmail(EMAIL1, HASH2), HASH2);

    assertProperty(EMAIL1, HASH2);
    Assert.assertEquals(referencesManager.getHash(EMAIL1), HASH2);
  }

  @Test
  public void testPutEmailIfItNotExists() throws IOException, CloudAdminException {
    clear();

    Assert.assertEquals(referencesManager.putEmail(EMAIL1, HASH1), HASH1);

    assertProperty(EMAIL1, HASH1);
    Assert.assertEquals(referencesManager.getHash(EMAIL1), HASH1);
  }

  @Test
  public void testRemoveEmailIfItExists() throws IOException, CloudAdminException {
    clear();
    add(EMAIL1, HASH1);
    add(EMAIL2, HASH2);

    referencesManager.removeEmail(EMAIL1);

    assertProperty(EMAIL2, HASH2);
    assertProperty(EMAIL1, null);
    Assert.assertEquals(referencesManager.getHash(EMAIL2), HASH2);
    Assert.assertNull(referencesManager.getHash(EMAIL1));
  }

  private void clear() throws IOException {
    File referencesFile = new File(cloudAdminConfiguration.getString(ReferencesManager.CLOUD_ADMIN_REFERENCES_DIR),
                                   cloudAdminConfiguration.getString(ReferencesManager.CLOUD_ADMIN_REFERENCES_FILE));
    referencesFile.delete();
    referencesFile.createNewFile();
  }

  private void add(String email, String hash) throws FileNotFoundException, IOException {
    File referencesFile = new File(cloudAdminConfiguration.getString(ReferencesManager.CLOUD_ADMIN_REFERENCES_DIR),
                                   cloudAdminConfiguration.getString(ReferencesManager.CLOUD_ADMIN_REFERENCES_FILE));
    Properties properties = new Properties();
    FileInputStream fis = new FileInputStream(referencesFile);
    try {
      properties.load(fis);
    } finally {
      fis.close();
    }
    properties.setProperty(email, hash);
    FileOutputStream fout = new FileOutputStream(referencesFile);
    try {
      properties.store(fout, null);
    } finally {
      fout.close();
    }
  }

  private void assertProperty(String email, String hash) throws IOException {
    File referencesFile = new File(cloudAdminConfiguration.getString(ReferencesManager.CLOUD_ADMIN_REFERENCES_DIR),
                                   cloudAdminConfiguration.getString(ReferencesManager.CLOUD_ADMIN_REFERENCES_FILE));
    Properties properties = new Properties();
    FileInputStream fis = new FileInputStream(referencesFile);
    try {
      properties.load(fis);
    } finally {
      fis.close();
    }
    Assert.assertEquals(properties.getProperty(email), hash);
  }

}
