/*
 * Copyright (C) 2011 eXo Platform SAS.
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

import com.exoplatform.cloud.admin.CloudAdminException;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

public class ReferencesManager {

  public static final String  CLOUD_ADMIN_REFERENCES_DIR  = "cloud.admin.references.dir";

  public static final String  CLOUD_ADMIN_REFERENCES_FILE = "cloud.admin.references.file";

  private Configuration       cloudAdminConfiguration;

  private String              referenceFilename;

  private static final Logger LOG                         = LoggerFactory.getLogger(ReferencesManager.class);

  private static Object       obj                         = new Object();

  public ReferencesManager(Configuration cloudAdminConfiguration) {
    this.cloudAdminConfiguration = cloudAdminConfiguration;
    this.referenceFilename = cloudAdminConfiguration.getString(CLOUD_ADMIN_REFERENCES_FILE, null);
  }

  public String getHash(String email) throws CloudAdminException {

    String hashFileName = getReferencesFolder();
    File hashDir = new File(hashFileName);
    if (!hashDir.exists())
      return null;
    try {
      File file = new File(hashDir + "/" + referenceFilename);
      if (!file.exists())
        return null;
      FileInputStream io = new FileInputStream(file);
      Properties properties = new Properties();
      properties.load(io);
      io.close();
      return properties.getProperty(email);
    } catch (IOException e) {
      LOG.error("I/O exception getting hash from email " + email, e);
      throw new CloudAdminException("An problem happened during processing this request. It was reported to developers. Please, try again later.");
    }
  }

  public String getEmail(String hash) throws CloudAdminException {
    String hashFileName = getReferencesFolder();
    File hashDir = new File(hashFileName);
    if (!hashDir.exists())
      return null;
    try {
      File file = new File(hashDir + "/" + referenceFilename);
      if (!file.exists())
        return null;
      FileInputStream io = new FileInputStream(file);
      Properties properties = new Properties();
      properties.load(io);
      io.close();
      Enumeration<String> propKeys = (Enumeration<String>) properties.propertyNames();
      while (propKeys.hasMoreElements()) {
        String one = propKeys.nextElement();
        if (properties.getProperty(one).equals(hash))
          return one;
      }
    } catch (IOException e) {
      LOG.error("I/O exception getting email from hash " + hash, e);
      throw new CloudAdminException("An problem happened during processing this request. It was reported to developers. Please, try again later.");
    }
    return null;
  }

  public String putEmail(String email, String uuid) throws CloudAdminException {
    String hashFileName = getReferencesFolder();
    File hashDir = new File(hashFileName);
    if (!hashDir.exists())
      hashDir.mkdir();
    try {
      File file = new File(hashDir + "/" + referenceFilename);
      if (!file.exists())
        file.createNewFile();
      synchronized (obj) {
        FileInputStream io = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(io);
        io.close();
        properties.setProperty(email, uuid);
        properties.store(new FileOutputStream(file), "");
      }
      return uuid;
    } catch (IOException e) {
      LOG.error("I/O exception writing email " + email + " , uuid: " + uuid, e);
      throw new CloudAdminException("An problem happened during processing this request. It was reported to developers. Please, try again later.");
    }

  }

  public void removeEmail(String email) throws CloudAdminException {
    String hashFileName = getReferencesFolder();
    File hashDir = new File(hashFileName);
    if (!hashDir.exists())
      hashDir.mkdir();
    try {
      File file = new File(hashDir + "/" + referenceFilename);
      if (!file.exists())
        return;
      synchronized (obj) {
        FileInputStream io = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(io);
        io.close();
        properties.remove(email);
        properties.store(new FileOutputStream(file), "");
      }
    } catch (IOException e) {
      LOG.error("I/O exception removing email " + email, e);
      throw new CloudAdminException("An problem happened during processing this request. It was reported to developers. Please, try again later.");
    }
  }

  private String getReferencesFolder() throws CloudAdminException {
    String folder = cloudAdminConfiguration.getString(CLOUD_ADMIN_REFERENCES_DIR, null);
    if (folder == null || referenceFilename == null) {
      LOG.error("References dir is not defined in the admin configuration");
      throw new CloudAdminException("An problem happened during processing this request. It was reported to developers. Please, try again later.");
    }
    return folder;
  }

}
