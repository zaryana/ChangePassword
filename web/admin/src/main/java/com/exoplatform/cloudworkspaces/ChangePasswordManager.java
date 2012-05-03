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

import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

public class ChangePasswordManager {

  public static final long    REFERENCE_TIME_LIMIT                = 360 * 60 * 1000;

  public static final String  CLOUD_ADMIN_PASSWORD_REFERENCES_DIR = "cloud.admin.password.references.dir";

  private Configuration       cloudAdminConfiguration;

  private static final Logger LOG                                 = LoggerFactory.getLogger(ChangePasswordManager.class);

  public ChangePasswordManager(Configuration cloudAdminConfiguration) {
    this.cloudAdminConfiguration = cloudAdminConfiguration;

  }

  public String addReference(String email) throws CloudAdminException {
    String folderName = getPasswordReferencesFolder();
    File folder = new File(folderName);
    if (!folder.exists())
      folder.mkdir();
    String uuid = UUID.randomUUID().toString();
    File propertyFile = new File(folderName + uuid + ".properties");

    Properties properties = new Properties();
    properties.setProperty("email", email);
    properties.setProperty("uuid", uuid);
    properties.setProperty("created", Long.toString(System.currentTimeMillis()));
    try {
      propertyFile.createNewFile();
      properties.store(new FileOutputStream(propertyFile), "");
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new CloudAdminException("A problem happened during processsing this request. It was reported to developers. Please, try again later.");
    }
    return uuid;
  }

  public String validateReference(String uuid) throws CloudAdminException {
    String folderName = getPasswordReferencesFolder();
    File folder = new File(folderName);
    final String errMessage = "Your confirmation link has expired. Please <a href='/reset-password.jsp'>try again</a>.";
    if (!folder.exists())
      throw new CloudAdminException(errMessage);
    File propertyFile = new File(folderName + uuid + ".properties");
    try {
      FileInputStream io = new FileInputStream(propertyFile);
      Properties newprops = new Properties();
      newprops.load(io);
      io.close();
      String timestamp = newprops.getProperty("created");
      if ((System.currentTimeMillis() - Long.valueOf(timestamp)) > REFERENCE_TIME_LIMIT) {
        propertyFile.delete();
        throw new CloudAdminException(errMessage);
      }
      if (!newprops.getProperty("uuid").equals(uuid)) {
        propertyFile.delete();
        throw new CloudAdminException(errMessage);
      }
      propertyFile.delete();
      return newprops.getProperty("email");
    } catch (FileNotFoundException e) {
      throw new CloudAdminException(errMessage);
    } catch (IOException e) {
      String msg = "Password restore error : failed to read property file "
          + propertyFile.getName();
      LOG.error(msg, e);
      throw new CloudAdminException("A problem happened during processing request . It was reported to developers. Please, try again later.");
    }
  }

  private String getPasswordReferencesFolder() throws CloudAdminException {
    String folder = cloudAdminConfiguration.getString(CLOUD_ADMIN_PASSWORD_REFERENCES_DIR, null);
    if (folder == null) {
      LOG.error("Registration waitind dir is not defined in the admin configuration");
      throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");
    }
    return folder;
  }

}
