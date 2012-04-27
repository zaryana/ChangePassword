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


import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.Credential;
import org.exoplatform.services.security.PasswordCredential;
import org.exoplatform.services.security.UsernameCredential;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.security.auth.login.LoginException;

public class TestWorkspacesAuthenticator {

  OrganizationService organizationService;
  CloudWorkspacesAuthenticatorImpl authenticator;
  UserHandler userHandler = Mockito.mock(UserHandler.class);


  @BeforeMethod
  public void initDependencies() {
    organizationService = Mockito.mock(OrganizationService.class);
    authenticator = new CloudWorkspacesAuthenticatorImpl(organizationService);
  }



//  Not possible yet because validation in super can't be checked/mocked;
//
//  @Test
//  public void testValidateUserOk() throws  Exception {
//    UsernameCredential usernameCredential = new UsernameCredential("user1");
//    PasswordCredential passwordCredential = new PasswordCredential("pass1");
//    Credential [] creds = {usernameCredential,passwordCredential};
//    authenticator.validateUser(creds);
//  }
//
//  @Test
//  public void testValidateUserFail() throws  Exception {
//
//  }

  @Test
  public void testValidateRoot() throws  Exception {

    System.setProperty("cloud-workspaces.login.conf.file", "target/test-classes/login.properties");
    Mockito.when(organizationService.getUserHandler()).thenReturn(userHandler);
    Mockito.when(userHandler.authenticate(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
    UsernameCredential usernameCredential = new UsernameCredential("root");
    PasswordCredential passwordCredential = new PasswordCredential("12345");
    Credential [] creds = {usernameCredential,passwordCredential};
    String validated = authenticator.validateUser(creds);
    Assert.assertEquals("root", validated);

  }

  @Test (expectedExceptions = {LoginException.class})
  public void testValidateRootFailed() throws  Exception {

    System.setProperty("cloud-workspaces.login.conf.file", "target/test-classes/login.properties");
    Mockito.when(organizationService.getUserHandler()).thenReturn(userHandler);
    Mockito.when(userHandler.authenticate(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
    UsernameCredential usernameCredential = new UsernameCredential("root");
    PasswordCredential passwordCredential = new PasswordCredential("root333");
    Credential [] creds = {usernameCredential,passwordCredential};
    authenticator.validateUser(creds);
  }
}