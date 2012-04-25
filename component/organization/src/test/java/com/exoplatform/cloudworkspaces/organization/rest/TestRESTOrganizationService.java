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
package com.exoplatform.cloudworkspaces.organization.rest;


import com.jayway.restassured.RestAssured;
import org.everrest.assured.EverrestJetty;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.organization.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.ITestContext;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

@Listeners(value = { EverrestJetty.class, MockitoTestNGListener.class })
public class TestRESTOrganizationService {

  private static final String                    TENANT_NAME = "aaa";

  private static final String                    URI = "/uri";

  public static final String                     USERNAME      = "test";

  public static final String                     PASSWORD      = "password";

  public static final String                     FIRST_NAME    = "first-name";

  public static final String                     USER_MAIL     = "test@aaa.com";

  public static final String                     LAST_NAME     = "last-name";

  private static final String                    AUTH_USERNAME = "cldadmin";

  private static final String                    AUTH_PASSWORD = "tomcat";


  @Mock
  RepositoryService repositoryService;

  @Mock
  OrganizationService organizationService;

  @Mock
  UserHandler userHandler;

  @Mock
  GroupHandler groupHandler;

  @Mock
  MembershipTypeHandler membership_type_handler;

  @Mock
  MembershipHandler membership_handler;

  @Mock
  MembershipType membership_member;

  @Mock
  MembershipType membership_all;

  @Mock
  User newUser;

  @InjectMocks
  private WorkspacesRESTOrganizationServiceImpl WSOrganizationService;


  @Test
  public void testCreateUserAdministrator(ITestContext context) throws Exception {

    Mockito.when(organizationService.getUserHandler()).thenReturn(userHandler);
    Mockito.when(organizationService.getGroupHandler()).thenReturn(groupHandler);
    Mockito.when(organizationService.getMembershipTypeHandler()).thenReturn(membership_type_handler);
    Mockito.when(organizationService.getMembershipHandler()).thenReturn(membership_handler);
    Mockito.when(membership_type_handler.findMembershipType(Mockito.eq("member"))).thenReturn(membership_member);
    Mockito.when(membership_type_handler.findMembershipType(Mockito.eq("*"))).thenReturn(membership_all);
    Mockito.when(userHandler.createUserInstance(Mockito.anyString())).thenReturn(newUser);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("tname", TENANT_NAME)
               .formParam("URI", URI)
               .formParam("username", USERNAME)
               .formParam("password", PASSWORD)
               .formParam("first-name", FIRST_NAME)
               .formParam("last-name", LAST_NAME)
               .formParam("email", USER_MAIL)
               .formParam("isadministrator", "true")
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Response.Status.CREATED.getStatusCode())
               .when()
               .post("/rest/cloudworkspaces/organization/adduser");

    Mockito.verify(organizationService).getUserHandler();

    //Mockito.verifyNoMoreInteractions(notificationMailSender);
  }


}