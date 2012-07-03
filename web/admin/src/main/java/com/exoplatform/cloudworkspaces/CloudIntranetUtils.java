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

import com.exoplatform.cloudworkspaces.http.UserNotFoundException;
import com.exoplatform.cloudworkspaces.http.WorkspacesOrganizationRequestPerformer;

import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class CloudIntranetUtils {

  private ReferencesManager                            referencesManager;

  private final EmailBlacklist                         emailBlacklist;

  private final WorkspacesOrganizationRequestPerformer organizationRequestPerformer;

  public static final String                           CLOUD_ADMIN_HOSTNAME_FILE = "cloud.admin.hostname.file";

  public static final char                             TENANT_NAME_DELIMITER     = '-';

  private static final Logger                          LOG                       = LoggerFactory.getLogger(CloudIntranetUtils.class);

  public CloudIntranetUtils(ReferencesManager referencesManager,
                            EmailBlacklist emailBlacklist,
                            WorkspacesOrganizationRequestPerformer organizationRequestPerformer) {
    this.referencesManager = referencesManager;
    this.emailBlacklist = emailBlacklist;
    this.organizationRequestPerformer = organizationRequestPerformer;
  }

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

  public boolean validateName(String aName) throws CloudAdminException {
    String nameRegexp = "^[A-Za-z][\\u0000-\\u007F\\u0080-\\u00FFa-zA-Z0-9 '&-.]*[A-Za-z0-9]$";
    return Pattern.matches(nameRegexp, aName);
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

  public String getSandboxTenantName() {
    return System.getProperty("sandbox.tenant.name");
  }

  public boolean hasUsernameInSandboxTenant(String email) throws CloudAdminException {
    try {
      organizationRequestPerformer.getUserNameByEmail(getSandboxTenantName(), email);
      return true;
    } catch (UserNotFoundException e) {
      return false;
    }
  }

  public String getUsernameInSandboxTenant(String email) throws CloudAdminException {
    return organizationRequestPerformer.getUserNameByEmail(getSandboxTenantName(), email);
  }

  public UserMailInfo email2userMailInfo(String email) {
    if (emailBlacklist.isInBlackList(email)) {
      try {
        String defaultTenant = getSandboxTenantName();
        try {
          String username = getUsernameInSandboxTenant(email);
          return new UserMailInfo(username, defaultTenant);
        } catch (UserNotFoundException e) {
          // use default algorithm
        }
      } catch (CloudAdminException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
    String username = email.substring(0, email.indexOf('@'));
    String hostname = email.substring(email.indexOf("@") + 1).toLowerCase();
    String[] subdomains = hostname.split("\\.");
    String tenantName;
    if (subdomains.length < 3) {
      // first or second level domain name
      return new UserMailInfo(username, subdomains[0]);
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

    return new UserMailInfo(username, tenantName.replace('.', TENANT_NAME_DELIMITER));
  }

}
