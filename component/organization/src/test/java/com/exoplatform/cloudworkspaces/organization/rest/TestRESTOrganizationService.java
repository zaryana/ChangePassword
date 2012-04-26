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
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.services.organization.impl.UserImpl;
import org.exoplatform.services.organization.impl.mock.LazyListImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

@Listeners(value = { EverrestJetty.class, MockitoTestNGListener.class })
public class TestRESTOrganizationService {

  private static final String                   TENANT_NAME   = "aaa";

  private static final String                   URI           = "/uri";

  public static final String                    USERNAME      = "test";

  public static final String                    PASSWORD      = "password";

  public static final String                    FIRST_NAME    = "first-name";

  public static final String                    USER_MAIL     = "test@aaa.com";

  public static final String                    LAST_NAME     = "last-name";

  private static final String                   AUTH_USERNAME = "cldadmin";

  private static final String                   AUTH_PASSWORD = "tomcat";

  @Mock
  RepositoryService                             repositoryService;

  @Mock
  OrganizationService                           organizationService;

  @Mock
  UserHandler                                   userHandler;

  @Mock
  GroupHandler                                  groupHandler;

  @Mock
  MembershipTypeHandler                         membership_type_handler;

  @Mock
  MembershipHandler                             membership_handler;

  @Mock
  MembershipType                                membership_member;

  @Mock
  MembershipType                                membership_all;

  @Mock
  User                                          newUser;

  @InjectMocks
  private WorkspacesRESTOrganizationServiceImpl WSOrganizationService;

  @Test
  public void testCreateUserAdministrator(ITestContext context) throws Exception {

    Mockito.when(organizationService.getUserHandler()).thenReturn(userHandler);
    Mockito.when(organizationService.getGroupHandler()).thenReturn(groupHandler);
    Mockito.when(organizationService.getMembershipTypeHandler())
           .thenReturn(membership_type_handler);
    Mockito.when(organizationService.getMembershipHandler()).thenReturn(membership_handler);
    Mockito.when(membership_type_handler.findMembershipType(Mockito.eq("member")))
           .thenReturn(membership_member);
    Mockito.when(membership_type_handler.findMembershipType(Mockito.eq("*")))
           .thenReturn(membership_all);
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

    Mockito.verify(userHandler).createUser((User) Mockito.anyObject(), Mockito.eq(true));

    Mockito.verify(membership_handler, Mockito.times(3))
           .linkMembership((User) Mockito.anyObject(),
                           (Group) Mockito.anyObject(),
                           (MembershipType) Mockito.anyObject(),
                           Mockito.eq(true));
  }

  @Test
  public void testCreateUserNotAdministrator(ITestContext context) throws Exception {

    Mockito.when(organizationService.getUserHandler()).thenReturn(userHandler);
    Mockito.when(organizationService.getGroupHandler()).thenReturn(groupHandler);
    Mockito.when(organizationService.getMembershipTypeHandler())
           .thenReturn(membership_type_handler);
    Mockito.when(organizationService.getMembershipHandler()).thenReturn(membership_handler);
    Mockito.when(membership_type_handler.findMembershipType(Mockito.eq("member")))
           .thenReturn(membership_member);
    Mockito.when(membership_type_handler.findMembershipType(Mockito.eq("*")))
           .thenReturn(membership_all);
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
               .formParam("isadministrator", "false")
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Response.Status.CREATED.getStatusCode())
               .when()
               .post("/rest/cloudworkspaces/organization/adduser");

    Mockito.verify(userHandler).createUser((User) Mockito.anyObject(), Mockito.eq(true));

    Mockito.verify(membership_handler, Mockito.never())
           .linkMembership((User) Mockito.anyObject(),
                           (Group) Mockito.anyObject(),
                           (MembershipType) Mockito.anyObject(),
                           Mockito.eq(true));
  }

  @Test
  public void testGetUsersAll(ITestContext context) throws Exception {

    List<User> users = new ArrayList<User>();
    users.add(new UserImpl("user1"));
    users.add(new UserImpl("user2"));
    users.add(new UserImpl("administrator1"));
    User[] usersArr = users.toArray(new User[0]);

    List<Group> group1 = new ArrayList<Group>();
    GroupImpl gr = new GroupImpl();
    gr.setId("/platform/users");
    group1.add(gr);

    List<Group> group2 = new ArrayList<Group>();
    GroupImpl gr2 = new GroupImpl();
    gr2.setId("/platform/administrators");
    group2.add(gr2);

    LazyListImpl _list = Mockito.mock(LazyListImpl.class);
    Mockito.when(_list.load(Mockito.anyInt(), Mockito.anyInt())).thenReturn(usersArr);
    Mockito.when(organizationService.getUserHandler()).thenReturn(userHandler);
    Mockito.when(userHandler.findAllUsers()).thenReturn(_list);
    Mockito.when(organizationService.getGroupHandler()).thenReturn(groupHandler);
    Mockito.when(groupHandler.findGroupsOfUser(Mockito.anyString())).thenReturn(group1);
    Mockito.when(groupHandler.findGroupsOfUser(Mockito.eq("administrator1"))).thenReturn(group2);

    com.jayway.restassured.response.Response resp = RestAssured.given()
                                                               .auth()
                                                               .basic(AUTH_USERNAME, AUTH_PASSWORD)
                                                               .queryParam("administratorsonly",
                                                                           "false")
                                                               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
                                                               .expect()
                                                               .statusCode(Response.Status.OK.getStatusCode())
                                                               .when()
                                                               .get("/rest/cloudworkspaces/organization/users/"
                                                                   + TENANT_NAME);
    Map<String, String> map = resp.jsonPath().get();// getBody().asString());
    Assert.assertEquals(map.size(), 3);
  }

  @Test
  public void testGetUsersAdministrators(ITestContext context) throws Exception {

    List<User> users = new ArrayList<User>();
    users.add(new UserImpl("user1"));
    users.add(new UserImpl("user2"));
    users.add(new UserImpl("administrator1"));
    User[] usersArr = users.toArray(new User[0]);

    List<Group> group1 = new ArrayList<Group>();
    GroupImpl gr = new GroupImpl();
    gr.setId("/platform/users");
    group1.add(gr);

    List<Group> group2 = new ArrayList<Group>();
    GroupImpl gr2 = new GroupImpl();
    gr2.setId("/platform/administrators");
    group2.add(gr2);

    LazyListImpl _list = Mockito.mock(LazyListImpl.class);
    Mockito.when(_list.load(Mockito.anyInt(), Mockito.anyInt())).thenReturn(usersArr);
    Mockito.when(organizationService.getUserHandler()).thenReturn(userHandler);
    Mockito.when(userHandler.findAllUsers()).thenReturn(_list);
    Mockito.when(organizationService.getGroupHandler()).thenReturn(groupHandler);
    Mockito.when(groupHandler.findGroupsOfUser(Mockito.anyString())).thenReturn(group1);
    Mockito.when(groupHandler.findGroupsOfUser(Mockito.eq("administrator1"))).thenReturn(group2);

    com.jayway.restassured.response.Response resp = RestAssured.given()
                                                               .auth()
                                                               .basic(AUTH_USERNAME, AUTH_PASSWORD)
                                                               .queryParam("administratorsonly",
                                                                           "true")
                                                               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
                                                               .expect()
                                                               .statusCode(Response.Status.OK.getStatusCode())
                                                               .when()
                                                               .get("/rest/cloudworkspaces/organization/users/"
                                                                   + TENANT_NAME);
    Map<String, String> map = resp.jsonPath().get();
    Assert.assertEquals(map.size(), 1);
  }

  @Test
  public void testUpdatePasswordUserNotExists(ITestContext context) throws Exception {

    Mockito.when(organizationService.getUserHandler()).thenReturn(userHandler);
    Mockito.when(userHandler.findUserByName(Mockito.anyString())).thenReturn(null);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("tname", TENANT_NAME)
               .formParam("username", USERNAME)
               .formParam("password", PASSWORD)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
               .when()
               .post("/rest/cloudworkspaces/organization/newpassword");
  }

  @Test
  public void testUpdatePassword(ITestContext context) throws Exception {

    User testUser = new UserImpl(USERNAME);

    Mockito.when(organizationService.getUserHandler()).thenReturn(userHandler);
    Mockito.when(userHandler.findUserByName(Mockito.anyString())).thenReturn(testUser);
    Mockito.when(userHandler.createUserInstance(Mockito.anyString())).thenReturn(testUser);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("tname", TENANT_NAME)
               .formParam("username", USERNAME)
               .formParam("password", PASSWORD)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Response.Status.OK.getStatusCode())
               .when()
               .post("/rest/cloudworkspaces/organization/newpassword");

    Mockito.verify(userHandler).saveUser(Mockito.eq(testUser), Mockito.eq(true));
  }
}
