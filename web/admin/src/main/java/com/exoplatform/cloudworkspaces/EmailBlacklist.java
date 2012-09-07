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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailBlacklist implements Startable {

  public static final String  CLOUD_ADMIN_BLACKLIST_FILE = "cloud.admin.blacklist.file";

  private static final Logger LOG                        = LoggerFactory.getLogger(EmailBlacklist.class);

  private final File          blacklistFile;

  private HashSet<Pattern>     blacklist;

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
      for (Pattern p : blacklist) {
        Matcher m = p.matcher(domain);
        if (m.matches()) {
          return true;
        }
      }
    }
    return false;
  }

  private void reload() throws FileNotFoundException {
    HashSet<Pattern> newBlacklist = new HashSet<Pattern>();
    Scanner in = new Scanner(blacklistFile);
    try {
      while (in.hasNextLine()) {
        String line = in.nextLine().trim();
        StringBuilder b = new StringBuilder();
        for(int i=0; i<line.length(); ++i) {
          char ch = line.charAt(i);
          if (".".indexOf(ch) != -1)
            b.append("\\").append(ch).append("*");   //Converting "." into "\.*" - escaped dot or nothing (for cases like *.yahoo.*)
          else if ("*".indexOf(ch) != -1)            // Example: mail.com -> mail\.*com
            b.append("[\\d\\w]").append(ch);         //Converting "*" into "[\d\w]*" - any letter or digit, but not symbol
          else
            b.append(ch);
        }
        newBlacklist.add(Pattern.compile(b.toString()));
       }
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
