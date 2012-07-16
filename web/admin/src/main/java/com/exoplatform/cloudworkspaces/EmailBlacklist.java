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

import org.apache.commons.configuration.Configuration;
import org.picocontainer.Startable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;

public class EmailBlacklist implements Startable {

  public static final String  CLOUD_ADMIN_BLACKLIST_FILE = "cloud.admin.blacklist.file";

  private static final Logger LOG                        = LoggerFactory.getLogger(EmailBlacklist.class);

  private final File          blacklistFile;

  private HashSet<String>     blacklist;

  private long                lastModifiedTime           = 0;

  public EmailBlacklist(Configuration cloudAdminConfiguration) {
    String blacklistFileName = cloudAdminConfiguration.getString(CLOUD_ADMIN_BLACKLIST_FILE, null);
    this.blacklistFile = new File(blacklistFileName);
    if (blacklistFileName == null) {
      LOG.info("Blacklist not configured in admin.properties");
      this.blacklist = null;
    } else {
      LOG.info("Loading blacklist file from {}", blacklistFileName);
      try {
        reload();
      } catch (FileNotFoundException e) {
        LOG.warn("File with tenant name blacklist not found. Please, create file {} if you need add tenant name to blacklist.",
                 blacklistFile.getAbsolutePath());
      }
    }
  }

  public boolean isInBlackList(String email) {
    email = email.toLowerCase();
    if (blacklistFile == null) {
      return false;
    }
    try {
      if (mustReloaded())
        reload();
    } catch (FileNotFoundException e) {
      LOG.warn("File with tenant name blacklist not found. Please, create file {} if you need add tenant name to blacklist.",
               blacklistFile.getAbsolutePath());
    }

    if (blacklist != null) {
      String domain = email.substring(email.indexOf('@') + 1);
      String[] parts = domain.split("[.]");
      String prefix = parts[0];
      for (int i = 1; i < parts.length; i++) {
        if (blacklist.contains(prefix + ".*"))
          return true;
        prefix += "." + parts[i];
      }
      return blacklist.contains(prefix);
    }
    return false;
  }

  private void reload() throws FileNotFoundException {
    HashSet<String> newBlacklist = new HashSet<String>();
    Scanner in = new Scanner(blacklistFile);
    try {
      while (in.hasNextLine())
        newBlacklist.add(in.nextLine().trim());
    } finally {
      in.close();
    }
    lastModifiedTime = blacklistFile.lastModified();

    this.blacklist = newBlacklist;
  }

  private boolean mustReloaded() {
    return lastModifiedTime != blacklistFile.lastModified();
  }

  @Override
  public void start() {
    // do nothing
  }

  @Override
  public void stop() {
    // do nothing
  }

}
