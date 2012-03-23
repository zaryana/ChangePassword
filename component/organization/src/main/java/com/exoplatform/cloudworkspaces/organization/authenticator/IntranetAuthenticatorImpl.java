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

package com.exoplatform.cloudworkspaces.organization.authenticator;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.Credential;
import org.exoplatform.services.security.PasswordCredential;
import org.exoplatform.services.security.PasswordEncrypter;
import org.exoplatform.services.security.RolesExtractor;
import org.exoplatform.services.security.UsernameCredential;
import org.exoplatform.services.organization.auth.OrganizationAuthenticatorImpl;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.security.auth.login.LoginException;

public class IntranetAuthenticatorImpl extends OrganizationAuthenticatorImpl {
  protected static Log        log       = ExoLogger.getLogger("org.exoplatform.platform.cloud.services.authentication.IntranetAuthenticatorImpl");

  private static final String ROOT_USER = "root";

  public IntranetAuthenticatorImpl(OrganizationService orgService) {
    super(orgService);
  }

  public IntranetAuthenticatorImpl(OrganizationService orgService, RolesExtractor rolesExtractor) {
    super(orgService, rolesExtractor);
  }

  public IntranetAuthenticatorImpl(OrganizationService orgService,
                                   RolesExtractor rolesExtractor,
                                   PasswordEncrypter encrypter) {
    super(orgService, rolesExtractor, encrypter);
  }

  @Override
  public String validateUser(Credential[] credentials) throws LoginException, Exception {
    String username = null;
    String password = null;
    for (Credential cred : credentials) {
      if (cred instanceof UsernameCredential) {
        username = ((UsernameCredential) cred).getUsername();
      }
      if (cred instanceof PasswordCredential) {
        password = ((PasswordCredential) cred).getPassword();
      }
    }

    String validUser = null;
    try {
      validUser = super.validateUser(credentials);
    } catch (LoginException ex) {
      if (username == null || password == null || !username.equals(ROOT_USER)) {
        LoginException le = new LoginException(ex.getMessage());
        le.initCause(ex);
        throw le;
      }
    }
    if (!username.equals(ROOT_USER)) {
      return validUser;
    }

    if (validateRoot(password)) {
      return ROOT_USER;
    }

    throw new LoginException("Login failed for user root");
  }

  private boolean validateRoot(String password) throws LoginException {
    String loginConfigurationFile = System.getProperty("cloud-workspaces.login.conf.file");
    if (loginConfigurationFile == null)
      throw new LoginException("Login failed for user root - cloud-workspaces.login.conf.file property not found.");
    try {
      File propertyFile = new File(loginConfigurationFile);
      FileInputStream io = new FileInputStream(propertyFile);
      Properties properties = new Properties();
      properties.load(io);
      String value = properties.getProperty(ROOT_USER);
      if (value == null)
        return false;
      return value.equals(password);
    } catch (final Throwable e) {
      LoginException le = new LoginException(e.getMessage());
      le.initCause(e);
      throw le;
    }
  }
}
