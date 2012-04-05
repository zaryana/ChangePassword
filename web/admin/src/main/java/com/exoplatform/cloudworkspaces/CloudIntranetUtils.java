/*
 * 
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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class CloudIntranetUtils {

  private Configuration          cloudAdminConfiguration;

  private NotificationMailSender notificationMailSender;

  UserRequestDAO                 requestDao;

  private String                 blackListConfigurationFolder;

  private ReferencesManager      referencesManager;

  public static final String     CLOUD_ADMIN_HOSTNAME_FILE = "cloud.admin.hostname.file";

  public static final char       TENANT_NAME_DELIMITER     = '-';

  private static final Logger    LOG                       = LoggerFactory.getLogger(CloudIntranetUtils.class);

  public CloudIntranetUtils(Configuration cloudAdminConfiguration,
                            NotificationMailSender notificationMailSender,
                            UserRequestDAO requestDao,
                            ReferencesManager referencesManager) {
    this.cloudAdminConfiguration = cloudAdminConfiguration;
    this.notificationMailSender = notificationMailSender;
    this.blackListConfigurationFolder = cloudAdminConfiguration.getString("cloud.admin.blacklist.dir",
                                                                          null);
    this.referencesManager = referencesManager;
    this.requestDao = requestDao;
  }

  public boolean isInBlackList(String email) {
    String tail = email.substring(email.indexOf("@") + 1);
    if (blackListConfigurationFolder == null
        || (cloudAdminConfiguration.getString("cloud.admin.blacklist.file", null)) == null) {
      String tName = email2tenantName(email);
      LOG.info("Black list not configured, allowing tenant " + tName + " from email:" + email);
      return false;
    }
    File blacklistFolder = new File(blackListConfigurationFolder);
    if (!blacklistFolder.exists())
      return false;
    try {
      File propertyFile = new File(blacklistFolder + "/"
          + cloudAdminConfiguration.getString("cloud.admin.blacklist.file"));
      FileInputStream io = new FileInputStream(propertyFile);
      Properties properties = new Properties();
      properties.load(io);
      io.close();
      return properties.containsKey(tail);
    } catch (FileNotFoundException e) {
      String tName = email2tenantName(email);
      LOG.info("Black list file not found, allowing tenant " + tName + " from email:" + email);
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      notificationMailSender.sendAdminErrorEmail(e.getMessage(), e);
    }
    return false;
  }

  /*
   * public void putInBlackList(String email) { String tail =
   * email.substring(email.indexOf("@") + 1); if (blackListConfigurationFolder
   * == null || (cloudAdminConfiguration.getString("cloud.admin.blacklist.file",
   * null)) == null) { String msg =
   * "Blacklist action failed - blacklist folder/file not configured, cannot add new record for "
   * + tail; LOG.warn(msg); sendAdminErrorEmail(msg, null); return; } File
   * blacklistFolder = new File(blackListConfigurationFolder); if
   * (!blacklistFolder.exists()) blacklistFolder.mkdir(); try { File
   * propertyFile = new File(blacklistFolder + "/" +
   * cloudAdminConfiguration.getString("cloud.admin.blacklist.file")); if
   * (!propertyFile.exists()) propertyFile.createNewFile(); FileInputStream io =
   * new FileInputStream(propertyFile); Properties properties = new
   * Properties(); properties.load(io); io.close(); if
   * (properties.containsKey(tail)) { return; } else {
   * properties.setProperty(tail, new SimpleDateFormat("yyyy-MM-dd").format(new
   * Date())); properties.store(new FileOutputStream(propertyFile), ""); }
   * LOG.info("Registrations from " + tail + " was blacklisted."); } catch
   * (FileNotFoundException e) { LOG.error(e.getMessage(), e);
   * sendAdminErrorEmail(e.getMessage(), e); } catch (IOException e) {
   * LOG.error(e.getMessage(), e); sendAdminErrorEmail(e.getMessage(), e); } }
   */

  public boolean validateEmail(String aEmailAddress) {
    if (aEmailAddress == null)
      return false;
    boolean result = true;
    try {
      InternetAddress emailAddr = new InternetAddress(aEmailAddress);
      if (!hasNameAndDomain(aEmailAddress)) {
        result = false;
      }
    } catch (AddressException ex) {
      result = false;
    }
    return result;
  }

  private static boolean hasNameAndDomain(String aEmailAddress) {
    String[] tokens = aEmailAddress.split("@");
    return tokens.length == 2 && tokens[0].trim().length() > 0 && tokens[1].trim().length() > 0
        && tokens[1].split("\\.").length > 1;
  }

  public boolean validateUUID(String aEmailAddress, String UUID) throws CloudAdminException {
    String hash = referencesManager.getHash(aEmailAddress);
    if (hash == null)
      return false;
    else
      return hash.equals(UUID);
  }

  /**
   * Read text message from InputStream.
   * 
   * @param errStream InputStream
   * @return String
   * @throws IOException
   */
  @Deprecated
  public static String readText(InputStream errStream) throws IOException {
    if (errStream != null) {
      InputStreamReader errReader = new InputStreamReader(errStream);
      try {
        int r = -1;
        StringBuilder errText = new StringBuilder();
        char[] buff = new char[256];
        while ((r = errReader.read(buff)) >= 0) {
          errText.append(buff, 0, r);
        }
        return errText.toString();
      } finally {
        errReader.close();
      }
    } else {
      return null;
    }
  }

  public String email2tenantName(String email) {
    String hostname = email.substring(email.indexOf("@") + 1).toLowerCase();
    String[] subdomains = hostname.split("\\.");
    String tenantName;
    if (subdomains.length < 3) {
      // first or second level domain name
      return subdomains[0];
    } else {
      // special cases
      tenantName = hostname.substring(0, hostname.lastIndexOf("."));
    }

    String hostNamesConf = System.getProperty(CLOUD_ADMIN_HOSTNAME_FILE);
    try {
      FileInputStream stream = new FileInputStream(hostNamesConf);
      DataInputStream in = new DataInputStream(stream);
      try {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String hostRegexp;
        while ((hostRegexp = br.readLine()) != null) {
          Pattern p = Pattern.compile(hostRegexp);
          Matcher m = p.matcher(hostname);
          if (m.find()) {
            tenantName = hostname.substring(0, m.start());
            break;
          }
        }
      } finally {
        in.close();
      }
    } catch (FileNotFoundException e) {
      LOG.warn("Hostnames file cloud.admin.hostname.file not found. Using default logic for tenant name from "
          + email + ". Caused by error: " + e.getMessage());
    } catch (IOException e) {
      LOG.error("Cannot read hostnames file cloud.admin.hostname.file. Using default logic for tenant name from "
                    + email,
                e);
    }

    return tenantName.replace('.', TENANT_NAME_DELIMITER);
  }

  public Map<String, String[]> sortByComparator(Map<String, String[]> unsortMap) {

    List<String> list = new LinkedList<String>(unsortMap.keySet());
    // sort list based on comparator
    Collections.sort(list, Collections.reverseOrder(new Comparator<String>() {
      public int compare(String o1, String o2) {
        Long f1 = Long.valueOf(o1.substring(o1.indexOf("_") + 1));
        Long f2 = Long.valueOf(o2.substring(o2.indexOf("_") + 1));
        return f1.compareTo(f2);
      }
    }));
    // put sorted list into map again
    Map<String, String[]> sortedMap = new LinkedHashMap<String, String[]>();
    for (Iterator<String> it = list.iterator(); it.hasNext();) {
      String key = (String) it.next();
      sortedMap.put(key, unsortMap.get(key));
    }
    return sortedMap;
  }
}
