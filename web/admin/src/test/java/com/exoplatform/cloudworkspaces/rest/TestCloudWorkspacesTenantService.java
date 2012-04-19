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
package com.exoplatform.cloudworkspaces.rest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import com.exoplatform.cloudworkspaces.ChangePasswordManager;
import com.exoplatform.cloudworkspaces.CloudIntranetUtils;
import com.exoplatform.cloudworkspaces.EmailBlacklist;
import com.exoplatform.cloudworkspaces.NotificationMailSender;
import com.exoplatform.cloudworkspaces.ReferencesManager;
import com.exoplatform.cloudworkspaces.UserAlreadyExistsException;
import com.exoplatform.cloudworkspaces.UserRequest;
import com.exoplatform.cloudworkspaces.UserRequestDAO;
import com.exoplatform.cloudworkspaces.http.WorkspacesOrganizationRequestPerformer;
import com.exoplatform.cloudworkspaces.listener.AsyncTenantStarter;
import com.exoplatform.cloudworkspaces.users.UserLimitsStorage;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import org.apache.commons.configuration.Configuration;
import org.everrest.assured.EverrestJetty;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.TenantAlreadyExistException;
import org.exoplatform.cloudmanagement.admin.configuration.AdminConfiguration;
import org.exoplatform.cloudmanagement.admin.configuration.TenantInfoFieldName;
import org.exoplatform.cloudmanagement.admin.dao.TenantDataManagerException;
import org.exoplatform.cloudmanagement.admin.dao.TenantInfoDataManager;
import org.exoplatform.cloudmanagement.admin.rest.TenantCreator;
import org.exoplatform.cloudmanagement.status.TenantState;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.ITestContext;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * signup -<br>
 * signup-link -<br>
 * join - done<br>
 * create -<br>
 * status - done<br>
 * contactus - done<br>
 * isuserexist - done<br>
 * maxallowed - done<br>
 * uuid - done<br>
 * passrestore - done<br>
 * passconfirm - done<br>
 * blacklisted - done<br>
 * tenantname - done<br>
 */
@Listeners(value = { EverrestJetty.class, MockitoTestNGListener.class })
public class TestCloudWorkspacesTenantService {

  private final String                           AUTH_USERNAME = "cldadmin";

  private final String                           AUTH_PASSWORD = "tomcat";

  @Mock
  private TenantCreator                          tenantCreator;

  @Mock
  private TenantInfoDataManager                  tenantInfoDataManager;

  @Mock
  private WorkspacesOrganizationRequestPerformer workspacesOrganizationRequestPerformer;

  @Mock
  private NotificationMailSender                 notificationMailSender;

  @Mock
  private UserLimitsStorage                      userLimitsStorage;

  @Mock
  private Configuration                          cloudAdminConfiguration;

  @Mock
  private ReferencesManager                      referencesManager;

  @Mock
  private UserRequestDAO                         requestDao;

  @Mock
  private AsyncTenantStarter                     tenantStarter;

  @Mock
  private CloudIntranetUtils                     cloudIntranetUtils;

  @Mock
  private EmailBlacklist                         emailBlacklist;

  @Mock
  private ChangePasswordManager                  changePasswordManager;

  @InjectMocks
  private CloudWorkspacesTenantService           cloudWorkspacesTenantService;

  @Test
  public void testSendContactUs(ITestContext context) {
    final String USER_MAIL = "test0@tenant.com";
    final String FIRST_NAME = "first-name";
    final String SUBJECT = "subject";
    final String TEXT = "text";

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", USER_MAIL)
               .formParam("first-name", FIRST_NAME)
               .formParam("subject", SUBJECT)
               .formParam("text", TEXT)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/contactus");

    Mockito.verify(notificationMailSender).sendContactUsEmail(Matchers.eq(USER_MAIL),
                                                              Matchers.eq(FIRST_NAME),
                                                              Matchers.eq(SUBJECT),
                                                              Matchers.anyString());

    Mockito.verifyNoMoreInteractions(notificationMailSender);
  }

  @Test
  public void testGetTenantName(ITestContext context) {
    final String EMAIL = "test@exoplatform.com";
    final String TENANT = "exoplatform";

    Mockito.when(cloudIntranetUtils.email2tenantName(EMAIL)).thenReturn(TENANT);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .pathParam("email", EMAIL)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .contentType(ContentType.TEXT)
               .body(equalTo(TENANT))
               .when()
               .get("/rest/cloud-admin/cloudworkspaces/tenant-service/tenantname/{email}");

    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils);
  }

  @Test
  public void testBlacklisted(ITestContext context) {
    final String EMAIL = "test@exoplatform.com";
    final boolean isBlacklisted = true;

    Mockito.when(emailBlacklist.isInBlackList(EMAIL)).thenReturn(isBlacklisted);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .pathParam("email", EMAIL)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .contentType(ContentType.TEXT)
               .body(equalTo(String.valueOf(isBlacklisted)))
               .when()
               .get("/rest/cloud-admin/cloudworkspaces/tenant-service/blacklisted/{email}");

    Mockito.verify(emailBlacklist).isInBlackList(Matchers.eq(EMAIL));

    Mockito.verifyNoMoreInteractions(emailBlacklist);
  }

  @Test
  public void testIsUserExistIfUserNotExist(ITestContext context) throws CloudAdminException {
    final String TENANT = "tenant";
    final String USERNAME = "test";

    Mockito.when(workspacesOrganizationRequestPerformer.isNewUserAllowed(TENANT, USERNAME))
           .thenReturn(true);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .pathParam("tenantname", TENANT)
               .pathParam("username", USERNAME)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .contentType(ContentType.TEXT)
               .body(equalTo(String.valueOf(false)))
               .when()
               .get("/rest/cloud-admin/cloudworkspaces/tenant-service/isuserexist/{tenantname}/{username}");

    Mockito.verify(workspacesOrganizationRequestPerformer).isNewUserAllowed(Matchers.eq(TENANT),
                                                                            Matchers.eq(USERNAME));

    Mockito.verifyNoMoreInteractions(workspacesOrganizationRequestPerformer);
  }

  @Test
  public void testIsUserExistIfUserNotExistButNewUserNotAllowed(ITestContext context) throws CloudAdminException {
    final String TENANT = "tenant";
    final String USERNAME = "test";

    Mockito.when(workspacesOrganizationRequestPerformer.isNewUserAllowed(TENANT, USERNAME))
           .thenReturn(false);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .pathParam("tenantname", TENANT)
               .pathParam("username", USERNAME)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .contentType(ContentType.TEXT)
               .body(equalTo(String.valueOf(false)))
               .when()
               .get("/rest/cloud-admin/cloudworkspaces/tenant-service/isuserexist/{tenantname}/{username}");

    Mockito.verify(workspacesOrganizationRequestPerformer).isNewUserAllowed(Matchers.eq(TENANT),
                                                                            Matchers.eq(USERNAME));

    Mockito.verifyNoMoreInteractions(workspacesOrganizationRequestPerformer);
  }

  @Test
  public void testIsUserExistIfUserExist(ITestContext context) throws CloudAdminException {
    final String TENANT = "tenant";
    final String USERNAME = "test";

    Mockito.when(workspacesOrganizationRequestPerformer.isNewUserAllowed(TENANT, USERNAME))
           .thenThrow(UserAlreadyExistsException.class);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .pathParam("tenantname", TENANT)
               .pathParam("username", USERNAME)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .contentType(ContentType.TEXT)
               .body(equalTo(String.valueOf(true)))
               .when()
               .get("/rest/cloud-admin/cloudworkspaces/tenant-service/isuserexist/{tenantname}/{username}");

    Mockito.verify(workspacesOrganizationRequestPerformer).isNewUserAllowed(Matchers.eq(TENANT),
                                                                            Matchers.eq(USERNAME));

    Mockito.verifyNoMoreInteractions(workspacesOrganizationRequestPerformer);
  }

  @Test
  public void testMaxAllowed(ITestContext context) {
    final String TENANT = "tenant";
    final int MAX_ALLOWED = 123;

    Mockito.when(userLimitsStorage.getMaxUsersForTenant(TENANT)).thenReturn(MAX_ALLOWED);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .pathParam("tenantname", TENANT)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .contentType(ContentType.TEXT)
               .body(equalTo(String.valueOf(MAX_ALLOWED)))
               .when()
               .get("/rest/cloud-admin/cloudworkspaces/tenant-service/maxallowed/{tenantname}");

    Mockito.verify(userLimitsStorage).getMaxUsersForTenant(TENANT);

    Mockito.verifyNoMoreInteractions(userLimitsStorage);
  }

  @Test
  public void testTenantStatusIfTenantExists(ITestContext context) throws TenantDataManagerException {
    final String TENANT = "tenant";
    final TenantState STATE = TenantState.ONLINE;

    Mockito.when(tenantInfoDataManager.isExists(TENANT)).thenReturn(true);
    Mockito.when(tenantInfoDataManager.getValue(TENANT, TenantInfoFieldName.PROPERTY_STATE))
           .thenReturn(STATE.toString());

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .pathParam("tenantname", TENANT)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .contentType(ContentType.TEXT)
               .body(equalTo(STATE.toString()))
               .when()
               .get("/rest/cloud-admin/cloudworkspaces/tenant-service/status/{tenantname}");

    Mockito.verify(tenantInfoDataManager).isExists(Matchers.eq(TENANT));
    Mockito.verify(tenantInfoDataManager).getValue(Matchers.eq(TENANT),
                                                   Matchers.eq(TenantInfoFieldName.PROPERTY_STATE));

    Mockito.verifyNoMoreInteractions(tenantInfoDataManager);
  }

  @Test
  public void testTenantStatusIfTenantNotExists(ITestContext context) throws TenantDataManagerException {
    final String TENANT = "tenant";

    Mockito.when(tenantInfoDataManager.isExists(TENANT)).thenReturn(false);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .pathParam("tenantname", TENANT)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.NOT_FOUND.getStatusCode())
               .when()
               .get("/rest/cloud-admin/cloudworkspaces/tenant-service/status/{tenantname}");

    Mockito.verify(tenantInfoDataManager).isExists(Matchers.eq(TENANT));

    Mockito.verifyNoMoreInteractions(tenantInfoDataManager);
  }

  @Test
  public void testUuidIfReferenceExists(ITestContext context) throws CloudAdminException {
    final String UUID = "12345-67890-abcdef";
    final String EMAIL = "test@tenant.com";

    Mockito.when(referencesManager.getEmail(UUID)).thenReturn(EMAIL);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .pathParam("uuid", UUID)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .when()
               .get("/rest/cloud-admin/cloudworkspaces/tenant-service/uuid/{uuid}");

    Mockito.verify(referencesManager).getEmail(Matchers.eq(UUID));

    Mockito.verifyNoMoreInteractions(referencesManager);
  }

  @Test
  public void testUuidIfReferenceNotExists(ITestContext context) throws CloudAdminException {
    final String UUID = "12345-67890-abcdef";

    Mockito.when(referencesManager.getEmail(UUID)).thenReturn(null);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .pathParam("uuid", UUID)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.BAD_REQUEST.getStatusCode())
               .body(containsString("broken link"))
               .when()
               .get("/rest/cloud-admin/cloudworkspaces/tenant-service/uuid/{uuid}");

    Mockito.verify(referencesManager).getEmail(Matchers.eq(UUID));

    Mockito.verifyNoMoreInteractions(referencesManager);
  }

  @Test
  public void testPassConfirmIfReferenceExists(ITestContext context) throws CloudAdminException {
    final String UUID = "12345-67890-abcdef";
    final String EMAIL = "test@tenant.com";
    final String TENANT = "tenant";
    final String PASSWORD = "test-password";

    Mockito.when(changePasswordManager.validateReference(UUID)).thenReturn(EMAIL);
    Mockito.when(cloudIntranetUtils.email2tenantName(EMAIL)).thenReturn(TENANT);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("uuid", UUID)
               .formParam("password", PASSWORD)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/passconfirm");

    Mockito.verify(changePasswordManager).validateReference(Matchers.eq(UUID));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(workspacesOrganizationRequestPerformer).updatePassword(Matchers.eq(TENANT),
                                                                          Matchers.eq(EMAIL),
                                                                          Matchers.eq(PASSWORD));

    Mockito.verifyNoMoreInteractions(changePasswordManager,
                                     cloudIntranetUtils,
                                     workspacesOrganizationRequestPerformer);
  }

  @Test
  public void testPassRestoreIfEmailNotValid(ITestContext context) {
    final String EMAIL = "test@tenant.com";

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(false);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .pathParam("email", EMAIL)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.BAD_REQUEST.getStatusCode())
               .contentType(ContentType.TEXT)
               .when()
               .get("/rest/cloud-admin/cloudworkspaces/tenant-service/passrestore/{email}");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
  }

  @Test
  public void testPassRestoreIfTenantNotExists(ITestContext context) throws TenantDataManagerException {
    final String EMAIL = "test@tenant.com";
    final String TENANT = "tenant";

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.email2tenantName(EMAIL)).thenReturn(TENANT);
    Mockito.when(tenantInfoDataManager.isExists(TENANT)).thenReturn(false);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .pathParam("email", EMAIL)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.BAD_REQUEST.getStatusCode())
               .contentType(ContentType.TEXT)
               .when()
               .get("/rest/cloud-admin/cloudworkspaces/tenant-service/passrestore/{email}");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(tenantInfoDataManager).isExists(Matchers.eq(TENANT));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils, tenantInfoDataManager);
  }

  @Test
  public void testPassRestoreIfTenantIsOnlineButUserNotFound(ITestContext context) throws CloudAdminException {
    final String EMAIL = "test@tenant.com";
    final String TENANT = "tenant";
    final TenantState STATE = TenantState.ONLINE;

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.email2tenantName(EMAIL)).thenReturn(TENANT);
    Mockito.when(tenantInfoDataManager.isExists(TENANT)).thenReturn(true);
    Mockito.when(tenantInfoDataManager.getValue(TENANT, TenantInfoFieldName.PROPERTY_STATE))
           .thenReturn(STATE.toString());
    Map<String, String> users = new HashMap<String, String>();
    Mockito.when(workspacesOrganizationRequestPerformer.getTenantUsers(TENANT, false))
           .thenReturn(users);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .pathParam("email", EMAIL)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.BAD_REQUEST.getStatusCode())
               .contentType(ContentType.TEXT)
               .when()
               .get("/rest/cloud-admin/cloudworkspaces/tenant-service/passrestore/{email}");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(tenantInfoDataManager).isExists(Matchers.eq(TENANT));
    Mockito.verify(tenantInfoDataManager).getValue(Matchers.eq(TENANT),
                                                   Matchers.eq(TenantInfoFieldName.PROPERTY_STATE));
    Mockito.verify(workspacesOrganizationRequestPerformer).getTenantUsers(Matchers.eq(TENANT),
                                                                          Matchers.eq(false));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils,
                                     tenantInfoDataManager,
                                     workspacesOrganizationRequestPerformer);
  }

  @Test
  public void testPassRestoreIfTenantIsOnlineAndUserFound(ITestContext context) throws CloudAdminException {
    final String USERNAME = "test";
    final String EMAIL = "test@tenant.com";
    final String TENANT = "tenant";
    final TenantState STATE = TenantState.ONLINE;
    final String UUID = "12345-67890-abcdef";

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.email2tenantName(EMAIL)).thenReturn(TENANT);
    Mockito.when(tenantInfoDataManager.isExists(TENANT)).thenReturn(true);
    Mockito.when(tenantInfoDataManager.getValue(TENANT, TenantInfoFieldName.PROPERTY_STATE))
           .thenReturn(STATE.toString());
    Map<String, String> users = new HashMap<String, String>();
    users.put(USERNAME, EMAIL);
    Mockito.when(workspacesOrganizationRequestPerformer.getTenantUsers(TENANT, false))
           .thenReturn(users);
    Mockito.when(changePasswordManager.addReference(EMAIL)).thenReturn(UUID);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .pathParam("email", EMAIL)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .when()
               .get("/rest/cloud-admin/cloudworkspaces/tenant-service/passrestore/{email}");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(tenantInfoDataManager).isExists(Matchers.eq(TENANT));
    Mockito.verify(tenantInfoDataManager).getValue(Matchers.eq(TENANT),
                                                   Matchers.eq(TenantInfoFieldName.PROPERTY_STATE));
    Mockito.verify(workspacesOrganizationRequestPerformer).getTenantUsers(Matchers.eq(TENANT),
                                                                          Matchers.eq(false));
    Mockito.verify(changePasswordManager).addReference(Matchers.eq(EMAIL));
    Mockito.verify(notificationMailSender).sendPasswordRestoreEmail(Matchers.eq(EMAIL),
                                                                    Matchers.eq(TENANT),
                                                                    Matchers.eq(UUID));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils,
                                     tenantInfoDataManager,
                                     workspacesOrganizationRequestPerformer,
                                     changePasswordManager,
                                     notificationMailSender);
  }

  @Test
  public void testPassRestoreIfTenantIsSuspended(ITestContext context) throws CloudAdminException {
    final String EMAIL = "test@tenant.com";
    final String TENANT = "tenant";
    final TenantState STATE = TenantState.SUSPENDED;

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.email2tenantName(EMAIL)).thenReturn(TENANT);
    Mockito.when(tenantInfoDataManager.isExists(TENANT)).thenReturn(true);
    Mockito.when(tenantInfoDataManager.getValue(TENANT, TenantInfoFieldName.PROPERTY_STATE))
           .thenReturn(STATE.toString());

    Mockito.when(cloudAdminConfiguration.getString(AdminConfiguration.CLOUD_ADMIN_FRONT_END_SERVER_HOST))
           .thenReturn("cloud-workspaces");
    Mockito.when(cloudAdminConfiguration.getString(AdminConfiguration.CLOUD_ADMIN_FRONT_END_SERVER_PORT))
           .thenReturn("8080");

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .pathParam("email", EMAIL)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(309)
               .header("Location", "http://cloud-workspaces:8080/resuming.jsp?email=" + EMAIL)
               .when()
               .get("/rest/cloud-admin/cloudworkspaces/tenant-service/passrestore/{email}");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(tenantInfoDataManager).isExists(Matchers.eq(TENANT));
    Mockito.verify(tenantInfoDataManager).getValue(Matchers.eq(TENANT),
                                                   Matchers.eq(TenantInfoFieldName.PROPERTY_STATE));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils,
                                     tenantInfoDataManager,
                                     workspacesOrganizationRequestPerformer,
                                     changePasswordManager,
                                     notificationMailSender);
  }

  @Test
  public void testPassRestoreIfTenantIsUnknown(ITestContext context) throws CloudAdminException {
    final String EMAIL = "test@tenant.com";
    final String TENANT = "tenant";
    final TenantState STATE = TenantState.UNKNOWN;

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.email2tenantName(EMAIL)).thenReturn(TENANT);
    Mockito.when(tenantInfoDataManager.isExists(TENANT)).thenReturn(true);
    Mockito.when(tenantInfoDataManager.getValue(TENANT, TenantInfoFieldName.PROPERTY_STATE))
           .thenReturn(STATE.toString());

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .pathParam("email", EMAIL)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.SERVICE_UNAVAILABLE.getStatusCode())
               .body(containsString("not ready"))
               .when()
               .get("/rest/cloud-admin/cloudworkspaces/tenant-service/passrestore/{email}");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(tenantInfoDataManager).isExists(Matchers.eq(TENANT));
    Mockito.verify(tenantInfoDataManager).getValue(Matchers.eq(TENANT),
                                                   Matchers.eq(TenantInfoFieldName.PROPERTY_STATE));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils,
                                     tenantInfoDataManager,
                                     workspacesOrganizationRequestPerformer,
                                     changePasswordManager,
                                     notificationMailSender);
  }

  @Test
  public void testJoinIfEmailNotValid(ITestContext context) throws CloudAdminException {
    final String EMAIL = "test@tenant.com";
    final String FIRST_NAME = "firstname";
    final String LAST_NAME = "lastname";
    final String PASSWORD = "password";
    final String CONFIRMATION_ID = "12345-67890-abcdef";

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(false);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .formParam("first-name", FIRST_NAME)
               .formParam("last-name", LAST_NAME)
               .formParam("password", PASSWORD)
               .formParam("confirmation-id", CONFIRMATION_ID)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.BAD_REQUEST.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/join");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils);
  }

  @Test
  public void testJoinIfUUIDNotValid(ITestContext context) throws CloudAdminException {
    final String EMAIL = "test@tenant.com";
    final String FIRST_NAME = "firstname";
    final String LAST_NAME = "lastname";
    final String PASSWORD = "password";
    final String CONFIRMATION_ID = "12345-67890-abcdef";

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.validateUUID(EMAIL, CONFIRMATION_ID)).thenReturn(false);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .formParam("first-name", FIRST_NAME)
               .formParam("last-name", LAST_NAME)
               .formParam("password", PASSWORD)
               .formParam("confirmation-id", CONFIRMATION_ID)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.BAD_REQUEST.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/join");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).validateUUID(Matchers.eq(EMAIL),
                                                    Matchers.eq(CONFIRMATION_ID));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils);
  }

  @Test
  public void testJoinIfTenantOnlineAndUserAllowed(ITestContext context) throws CloudAdminException {
    final String USERNAME = "test";
    final String EMAIL = "test@tenant.com";
    final String FIRST_NAME = "firstname";
    final String LAST_NAME = "lastname";
    final String PASSWORD = "password";
    final String CONFIRMATION_ID = "12345-67890-abcdef";
    final String TENANT = "tenant";
    final TenantState STATE = TenantState.ONLINE;

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.validateUUID(EMAIL, CONFIRMATION_ID)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.email2tenantName(EMAIL)).thenReturn(TENANT);
    Mockito.when(tenantInfoDataManager.getValue(TENANT, TenantInfoFieldName.PROPERTY_STATE))
           .thenReturn(STATE.toString());
    Mockito.when(workspacesOrganizationRequestPerformer.isNewUserAllowed(TENANT, USERNAME))
           .thenReturn(true);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .formParam("first-name", FIRST_NAME)
               .formParam("last-name", LAST_NAME)
               .formParam("password", PASSWORD)
               .formParam("confirmation-id", CONFIRMATION_ID)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/join");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).validateUUID(Matchers.eq(EMAIL),
                                                    Matchers.eq(CONFIRMATION_ID));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(tenantInfoDataManager).getValue(Matchers.eq(TENANT),
                                                   Matchers.eq(TenantInfoFieldName.PROPERTY_STATE));
    Mockito.verify(workspacesOrganizationRequestPerformer).isNewUserAllowed(Matchers.eq(TENANT),
                                                                            Matchers.eq(USERNAME));
    Mockito.verify(workspacesOrganizationRequestPerformer).storeUser(Matchers.eq(TENANT),
                                                                     Matchers.eq(EMAIL),
                                                                     Matchers.eq(FIRST_NAME),
                                                                     Matchers.eq(LAST_NAME),
                                                                     Matchers.eq(PASSWORD),
                                                                     Matchers.eq(false));
    Mockito.verify(notificationMailSender).sendUserJoinedEmails(Matchers.eq(TENANT),
                                                                Matchers.eq(FIRST_NAME),
                                                                Matchers.eq(EMAIL),
                                                                Matchers.anyMap());
    Mockito.verify(referencesManager).removeEmail(Matchers.eq(EMAIL));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils,
                                     tenantInfoDataManager,
                                     workspacesOrganizationRequestPerformer,
                                     notificationMailSender,
                                     referencesManager);
  }

  @Test
  public void testJoinIfTenantOnlineAndUserNotAllowed(ITestContext context) throws CloudAdminException {
    final String USERNAME = "test";
    final String EMAIL = "test@tenant.com";
    final String FIRST_NAME = "firstname";
    final String LAST_NAME = "lastname";
    final String PASSWORD = "password";
    final String CONFIRMATION_ID = "12345-67890-abcdef";
    final String TENANT = "tenant";
    final TenantState STATE = TenantState.ONLINE;

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.validateUUID(EMAIL, CONFIRMATION_ID)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.email2tenantName(EMAIL)).thenReturn(TENANT);
    Mockito.when(tenantInfoDataManager.getValue(TENANT, TenantInfoFieldName.PROPERTY_STATE))
           .thenReturn(STATE.toString());
    Mockito.when(workspacesOrganizationRequestPerformer.isNewUserAllowed(TENANT, USERNAME))
           .thenReturn(false);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .formParam("first-name", FIRST_NAME)
               .formParam("last-name", LAST_NAME)
               .formParam("password", PASSWORD)
               .formParam("confirmation-id", CONFIRMATION_ID)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/join");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).validateUUID(Matchers.eq(EMAIL),
                                                    Matchers.eq(CONFIRMATION_ID));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(tenantInfoDataManager).getValue(Matchers.eq(TENANT),
                                                   Matchers.eq(TenantInfoFieldName.PROPERTY_STATE));
    Mockito.verify(workspacesOrganizationRequestPerformer).isNewUserAllowed(Matchers.eq(TENANT),
                                                                            Matchers.eq(USERNAME));
    Mockito.verify(requestDao).put(Matchers.any(UserRequest.class));
    Mockito.verify(notificationMailSender).sendJoinRejectedEmails(Matchers.eq(TENANT),
                                                                  Matchers.eq(EMAIL),
                                                                  Matchers.anyMap());
    Mockito.verify(referencesManager).removeEmail(Matchers.eq(EMAIL));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils,
                                     tenantInfoDataManager,
                                     workspacesOrganizationRequestPerformer,
                                     notificationMailSender,
                                     requestDao,
                                     referencesManager);
  }

  @Test
  public void testJoinIfTenantOnlineAndUserAlreadyExists(ITestContext context) throws CloudAdminException {
    final String USERNAME = "test";
    final String EMAIL = "test@tenant.com";
    final String FIRST_NAME = "firstname";
    final String LAST_NAME = "lastname";
    final String PASSWORD = "password";
    final String CONFIRMATION_ID = "12345-67890-abcdef";
    final String TENANT = "tenant";
    final TenantState STATE = TenantState.ONLINE;

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.validateUUID(EMAIL, CONFIRMATION_ID)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.email2tenantName(EMAIL)).thenReturn(TENANT);
    Mockito.when(tenantInfoDataManager.getValue(TENANT, TenantInfoFieldName.PROPERTY_STATE))
           .thenReturn(STATE.toString());
    Mockito.when(workspacesOrganizationRequestPerformer.isNewUserAllowed(TENANT, USERNAME))
           .thenThrow(UserAlreadyExistsException.class);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .formParam("first-name", FIRST_NAME)
               .formParam("last-name", LAST_NAME)
               .formParam("password", PASSWORD)
               .formParam("confirmation-id", CONFIRMATION_ID)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/join");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).validateUUID(Matchers.eq(EMAIL),
                                                    Matchers.eq(CONFIRMATION_ID));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(tenantInfoDataManager).getValue(Matchers.eq(TENANT),
                                                   Matchers.eq(TenantInfoFieldName.PROPERTY_STATE));
    Mockito.verify(workspacesOrganizationRequestPerformer).isNewUserAllowed(Matchers.eq(TENANT),
                                                                            Matchers.eq(USERNAME));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils,
                                     tenantInfoDataManager,
                                     workspacesOrganizationRequestPerformer,
                                     notificationMailSender,
                                     requestDao);
  }

  @Test
  public void testJoinIfTenantCreation(ITestContext context) throws CloudAdminException {
    final String USERNAME = "test";
    final String EMAIL = "test@tenant.com";
    final String FIRST_NAME = "firstname";
    final String LAST_NAME = "lastname";
    final String PASSWORD = "password";
    final String CONFIRMATION_ID = "12345-67890-abcdef";
    final String TENANT = "tenant";
    final TenantState STATE = TenantState.WAITING_CREATION;

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.validateUUID(EMAIL, CONFIRMATION_ID)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.email2tenantName(EMAIL)).thenReturn(TENANT);
    Mockito.when(tenantInfoDataManager.getValue(TENANT, TenantInfoFieldName.PROPERTY_STATE))
           .thenReturn(STATE.toString());

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .formParam("first-name", FIRST_NAME)
               .formParam("last-name", LAST_NAME)
               .formParam("password", PASSWORD)
               .formParam("confirmation-id", CONFIRMATION_ID)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/join");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).validateUUID(Matchers.eq(EMAIL),
                                                    Matchers.eq(CONFIRMATION_ID));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(tenantInfoDataManager).getValue(Matchers.eq(TENANT),
                                                   Matchers.eq(TenantInfoFieldName.PROPERTY_STATE));
    Mockito.verify(requestDao).put(Matchers.any(UserRequest.class));
    Mockito.verify(referencesManager).removeEmail(Matchers.eq(EMAIL));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils,
                                     tenantInfoDataManager,
                                     workspacesOrganizationRequestPerformer,
                                     notificationMailSender,
                                     requestDao,
                                     referencesManager);
  }

  @Test
  public void testJoinIfTenantSuspended(ITestContext context) throws CloudAdminException {
    final String USERNAME = "test";
    final String EMAIL = "test@tenant.com";
    final String FIRST_NAME = "firstname";
    final String LAST_NAME = "lastname";
    final String PASSWORD = "password";
    final String CONFIRMATION_ID = "12345-67890-abcdef";
    final String TENANT = "tenant";
    final TenantState STATE = TenantState.SUSPENDED;

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.validateUUID(EMAIL, CONFIRMATION_ID)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.email2tenantName(EMAIL)).thenReturn(TENANT);
    Mockito.when(tenantInfoDataManager.getValue(TENANT, TenantInfoFieldName.PROPERTY_STATE))
           .thenReturn(STATE.toString());

    Mockito.when(cloudAdminConfiguration.getString(AdminConfiguration.CLOUD_ADMIN_FRONT_END_SERVER_HOST))
           .thenReturn("cloud-workspaces");
    Mockito.when(cloudAdminConfiguration.getString(AdminConfiguration.CLOUD_ADMIN_FRONT_END_SERVER_PORT))
           .thenReturn("8080");

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .formParam("first-name", FIRST_NAME)
               .formParam("last-name", LAST_NAME)
               .formParam("password", PASSWORD)
               .formParam("confirmation-id", CONFIRMATION_ID)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(309)
               .header("Location", "http://cloud-workspaces:8080/resuming.jsp?email=" + EMAIL)
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/join");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).validateUUID(Matchers.eq(EMAIL),
                                                    Matchers.eq(CONFIRMATION_ID));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(tenantInfoDataManager).getValue(Matchers.eq(TENANT),
                                                   Matchers.eq(TenantInfoFieldName.PROPERTY_STATE));
    Mockito.verify(tenantStarter).startTenant(Matchers.eq(TENANT));
    Mockito.verify(requestDao).put(Matchers.any(UserRequest.class));
    Mockito.verify(referencesManager).removeEmail(Matchers.eq(EMAIL));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils,
                                     tenantInfoDataManager,
                                     workspacesOrganizationRequestPerformer,
                                     notificationMailSender,
                                     requestDao,
                                     tenantStarter,
                                     referencesManager);
  }

  @Test
  public void testJoinIfTenantUnknown(ITestContext context) throws CloudAdminException {
    final String USERNAME = "test";
    final String EMAIL = "test@tenant.com";
    final String FIRST_NAME = "firstname";
    final String LAST_NAME = "lastname";
    final String PASSWORD = "password";
    final String CONFIRMATION_ID = "12345-67890-abcdef";
    final String TENANT = "tenant";
    final TenantState STATE = TenantState.UNKNOWN;

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.validateUUID(EMAIL, CONFIRMATION_ID)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.email2tenantName(EMAIL)).thenReturn(TENANT);
    Mockito.when(tenantInfoDataManager.getValue(TENANT, TenantInfoFieldName.PROPERTY_STATE))
           .thenReturn(STATE.toString());

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .formParam("first-name", FIRST_NAME)
               .formParam("last-name", LAST_NAME)
               .formParam("password", PASSWORD)
               .formParam("confirmation-id", CONFIRMATION_ID)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.SERVICE_UNAVAILABLE.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/join");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).validateUUID(Matchers.eq(EMAIL),
                                                    Matchers.eq(CONFIRMATION_ID));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(tenantInfoDataManager).getValue(Matchers.eq(TENANT),
                                                   Matchers.eq(TenantInfoFieldName.PROPERTY_STATE));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils,
                                     tenantInfoDataManager,
                                     workspacesOrganizationRequestPerformer,
                                     notificationMailSender);
  }

  @Test
  public void testJoinIfJoinFailed(ITestContext context) throws CloudAdminException {
    final String USERNAME = "test";
    final String EMAIL = "test@tenant.com";
    final String FIRST_NAME = "firstname";
    final String LAST_NAME = "lastname";
    final String PASSWORD = "password";
    final String CONFIRMATION_ID = "12345-67890-abcdef";
    final String TENANT = "tenant";
    final TenantState STATE = TenantState.UNKNOWN;

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.validateUUID(EMAIL, CONFIRMATION_ID))
           .thenThrow(CloudAdminException.class);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .formParam("first-name", FIRST_NAME)
               .formParam("last-name", LAST_NAME)
               .formParam("password", PASSWORD)
               .formParam("confirmation-id", CONFIRMATION_ID)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/join");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).validateUUID(Matchers.eq(EMAIL),
                                                    Matchers.eq(CONFIRMATION_ID));
    Mockito.verify(requestDao).put(Matchers.any(UserRequest.class));
    Mockito.verify(referencesManager).removeEmail(Matchers.eq(EMAIL));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils,
                                     tenantInfoDataManager,
                                     workspacesOrganizationRequestPerformer,
                                     notificationMailSender,
                                     referencesManager);
  }

  @Test
  public void testCreateIfEmailNotValid(ITestContext context) {
    final String EMAIL = "test@tenant.com";
    final String FIRST_NAME = "first-name";
    final String LAST_NAME = "last-name";
    final String COMPANY_NAME = "company-name";
    final String PHONE = "phone";
    final String PASSWORD = "password";
    final String CONFIRMATION_ID = "12345-67890-abcdef";

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(false);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .formParam("first-name", FIRST_NAME)
               .formParam("last-name", LAST_NAME)
               .formParam("company-name", COMPANY_NAME)
               .formParam("phone", PHONE)
               .formParam("password", PASSWORD)
               .formParam("confirmation-id", CONFIRMATION_ID)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.BAD_REQUEST.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/create");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils);
  }

  @Test
  public void testCreateIfUuidNotValid(ITestContext context) throws CloudAdminException {
    final String EMAIL = "test@tenant.com";
    final String FIRST_NAME = "first-name";
    final String LAST_NAME = "last-name";
    final String COMPANY_NAME = "company-name";
    final String PHONE = "phone";
    final String PASSWORD = "password";
    final String CONFIRMATION_ID = "12345-67890-abcdef";

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.validateUUID(EMAIL, CONFIRMATION_ID)).thenReturn(false);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .formParam("first-name", FIRST_NAME)
               .formParam("last-name", LAST_NAME)
               .formParam("company-name", COMPANY_NAME)
               .formParam("phone", PHONE)
               .formParam("password", PASSWORD)
               .formParam("confirmation-id", CONFIRMATION_ID)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.BAD_REQUEST.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/create");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).validateUUID(Matchers.eq(EMAIL),
                                                    Matchers.eq(CONFIRMATION_ID));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils);
  }

  @Test
  public void testCreateIfEmailInBlacklist(ITestContext context) throws CloudAdminException {
    final String EMAIL = "test@tenant.com";
    final String FIRST_NAME = "first-name";
    final String LAST_NAME = "last-name";
    final String COMPANY_NAME = "company-name";
    final String PHONE = "phone";
    final String PASSWORD = "password";
    final String CONFIRMATION_ID = "12345-67890-abcdef";

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.validateUUID(EMAIL, CONFIRMATION_ID)).thenReturn(true);
    Mockito.when(emailBlacklist.isInBlackList(EMAIL)).thenReturn(true);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .formParam("first-name", FIRST_NAME)
               .formParam("last-name", LAST_NAME)
               .formParam("company-name", COMPANY_NAME)
               .formParam("phone", PHONE)
               .formParam("password", PASSWORD)
               .formParam("confirmation-id", CONFIRMATION_ID)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.BAD_REQUEST.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/create");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).validateUUID(Matchers.eq(EMAIL),
                                                    Matchers.eq(CONFIRMATION_ID));
    Mockito.verify(emailBlacklist).isInBlackList(Matchers.eq(EMAIL));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils, emailBlacklist);
  }

  @Test
  public void testCreateIfCreationWasFailed(ITestContext context) throws CloudAdminException {
    final String EMAIL = "test@tenant.com";
    final String FIRST_NAME = "first-name";
    final String LAST_NAME = "last-name";
    final String COMPANY_NAME = "company-name";
    final String PHONE = "phone";
    final String PASSWORD = "password";
    final String CONFIRMATION_ID = "12345-67890-abcdef";
    final int RESPONSE_STATUS = 500;

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.validateUUID(EMAIL, CONFIRMATION_ID)).thenReturn(true);
    Mockito.when(emailBlacklist.isInBlackList(EMAIL)).thenReturn(false);
    Response response = Mockito.mock(Response.class);
    Mockito.when(tenantCreator.createTenantWithConfirmedEmail(CONFIRMATION_ID))
           .thenReturn(response);
    Mockito.when(response.getStatus()).thenReturn(RESPONSE_STATUS);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .formParam("first-name", FIRST_NAME)
               .formParam("last-name", LAST_NAME)
               .formParam("company-name", COMPANY_NAME)
               .formParam("phone", PHONE)
               .formParam("password", PASSWORD)
               .formParam("confirmation-id", CONFIRMATION_ID)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(RESPONSE_STATUS)
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/create");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).validateUUID(Matchers.eq(EMAIL),
                                                    Matchers.eq(CONFIRMATION_ID));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(emailBlacklist).isInBlackList(Matchers.eq(EMAIL));
    Mockito.verify(tenantCreator).createTenantWithConfirmedEmail(Matchers.eq(CONFIRMATION_ID));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils, emailBlacklist, tenantCreator);
  }

  @Test
  public void testCreateIfCreationOk(ITestContext context) throws CloudAdminException {
    final String EMAIL = "test@tenant.com";
    final String FIRST_NAME = "first-name";
    final String LAST_NAME = "last-name";
    final String COMPANY_NAME = "company-name";
    final String PHONE = "phone";
    final String PASSWORD = "password";
    final String CONFIRMATION_ID = "12345-67890-abcdef";

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.validateUUID(EMAIL, CONFIRMATION_ID)).thenReturn(true);
    Mockito.when(emailBlacklist.isInBlackList(EMAIL)).thenReturn(false);
    Response response = Mockito.mock(Response.class);
    Mockito.when(tenantCreator.createTenantWithConfirmedEmail(CONFIRMATION_ID))
           .thenReturn(response);
    Mockito.when(response.getStatus()).thenReturn(200);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .formParam("first-name", FIRST_NAME)
               .formParam("last-name", LAST_NAME)
               .formParam("company-name", COMPANY_NAME)
               .formParam("phone", PHONE)
               .formParam("password", PASSWORD)
               .formParam("confirmation-id", CONFIRMATION_ID)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/create");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).validateUUID(Matchers.eq(EMAIL),
                                                    Matchers.eq(CONFIRMATION_ID));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(emailBlacklist).isInBlackList(Matchers.eq(EMAIL));
    Mockito.verify(tenantCreator).createTenantWithConfirmedEmail(Matchers.eq(CONFIRMATION_ID));
    Mockito.verify(requestDao).put(Matchers.any(UserRequest.class));
    Mockito.verify(referencesManager).removeEmail(Matchers.eq(EMAIL));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils,
                                     emailBlacklist,
                                     tenantCreator,
                                     referencesManager);
  }

  @Test
  public void testCreateIfTenantAlreadyExists(ITestContext context) throws CloudAdminException {
    final String EMAIL = "test@tenant.com";
    final String FIRST_NAME = "first-name";
    final String LAST_NAME = "last-name";
    final String COMPANY_NAME = "company-name";
    final String PHONE = "phone";
    final String PASSWORD = "password";
    final String CONFIRMATION_ID = "12345-67890-abcdef";

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(cloudIntranetUtils.validateUUID(EMAIL, CONFIRMATION_ID)).thenReturn(true);
    Mockito.when(emailBlacklist.isInBlackList(EMAIL)).thenReturn(false);
    Response response = Mockito.mock(Response.class);
    Mockito.when(tenantCreator.createTenantWithConfirmedEmail(CONFIRMATION_ID))
           .thenReturn(response);
    Mockito.when(response.getStatus()).thenThrow(TenantAlreadyExistException.class);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .formParam("first-name", FIRST_NAME)
               .formParam("last-name", LAST_NAME)
               .formParam("company-name", COMPANY_NAME)
               .formParam("phone", PHONE)
               .formParam("password", PASSWORD)
               .formParam("confirmation-id", CONFIRMATION_ID)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.BAD_REQUEST.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/create");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).validateUUID(Matchers.eq(EMAIL),
                                                    Matchers.eq(CONFIRMATION_ID));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(emailBlacklist).isInBlackList(Matchers.eq(EMAIL));
    Mockito.verify(tenantCreator).createTenantWithConfirmedEmail(Matchers.eq(CONFIRMATION_ID));
    Mockito.verify(referencesManager).removeEmail(Matchers.eq(EMAIL));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils,
                                     emailBlacklist,
                                     tenantCreator,
                                     referencesManager);
  }

  @Test
  public void testSignupIfEmailNotValid(ITestContext context) {
    final String EMAIL = "test@tenant.com";

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(false);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.BAD_REQUEST.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/signup");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils);
  }

  @Test
  public void testSignupIfEmailInBlacklist(ITestContext context) {
    final String EMAIL = "test@tenant.com";

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(emailBlacklist.isInBlackList(EMAIL)).thenReturn(true);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.BAD_REQUEST.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/signup");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(emailBlacklist).isInBlackList(Matchers.eq(EMAIL));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils, emailBlacklist);
  }

  @Test
  public void testSignupIfOk(ITestContext context) throws CloudAdminException {
    final String USERNAME = "test";
    final String EMAIL = "test@tenant.com";
    final String TENANT = "tenant";
    final String CONFIRMATION_ID = "12345-67890-abcdef";

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(emailBlacklist.isInBlackList(EMAIL)).thenReturn(false);
    Mockito.when(cloudIntranetUtils.email2tenantName(EMAIL)).thenReturn(TENANT);

    Response response = Mockito.mock(Response.class);
    Mockito.when(tenantCreator.createTenantWithEmailConfirmation(TENANT, EMAIL))
           .thenReturn(response);
    Mockito.when(response.getStatus()).thenReturn(200);
    Mockito.when(response.getEntity()).thenReturn(CONFIRMATION_ID);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/signup");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(emailBlacklist).isInBlackList(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(requestDao).searchByEmail(Matchers.eq(EMAIL));
    Mockito.verify(tenantCreator).createTenantWithEmailConfirmation(Matchers.eq(TENANT),
                                                                    Matchers.eq(EMAIL));
    Mockito.verify(referencesManager).putEmail(Matchers.eq(EMAIL), Matchers.eq(CONFIRMATION_ID));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils,
                                     emailBlacklist,
                                     tenantCreator,
                                     referencesManager,
                                     requestDao);
  }

  @Test
  public void testSignupIfUserAlreadyExists(ITestContext context) throws CloudAdminException {
    final String USERNAME = "test";
    final String EMAIL = "test@tenant.com";
    final String TENANT = "tenant";

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(emailBlacklist.isInBlackList(EMAIL)).thenReturn(false);
    Mockito.when(cloudIntranetUtils.email2tenantName(EMAIL)).thenReturn(TENANT);
    Mockito.when(requestDao.searchByEmail(EMAIL)).thenReturn(Mockito.mock(UserRequest.class));

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/signup");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(emailBlacklist).isInBlackList(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(requestDao).searchByEmail(Matchers.eq(EMAIL));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils,
                                     emailBlacklist,
                                     tenantCreator,
                                     referencesManager);
  }

  @Test
  public void testSignupIfTenantCreation(ITestContext context) throws CloudAdminException {
    final String USERNAME = "test";
    final String EMAIL = "test@tenant.com";
    final String TENANT = "tenant";
    final TenantState STATE = TenantState.CREATION;

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(emailBlacklist.isInBlackList(EMAIL)).thenReturn(false);
    Mockito.when(cloudIntranetUtils.email2tenantName(EMAIL)).thenReturn(TENANT);
    Mockito.when(tenantCreator.createTenantWithEmailConfirmation(TENANT, EMAIL))
           .thenThrow(TenantAlreadyExistException.class);
    Mockito.when(tenantInfoDataManager.getValue(TENANT, TenantInfoFieldName.PROPERTY_STATE))
           .thenReturn(STATE.toString());

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/signup");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(emailBlacklist).isInBlackList(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(requestDao).searchByEmail(Matchers.eq(EMAIL));
    Mockito.verify(tenantCreator).createTenantWithEmailConfirmation(Matchers.eq(TENANT),
                                                                    Matchers.eq(EMAIL));
    Mockito.verify(tenantInfoDataManager).getValue(Matchers.eq(TENANT),
                                                   Matchers.eq(TenantInfoFieldName.PROPERTY_STATE));
    Mockito.verify(referencesManager).putEmail(Matchers.eq(EMAIL), Matchers.anyString());
    Mockito.verify(notificationMailSender).sendOkToJoinEmail(Matchers.eq(EMAIL), Matchers.anyMap());

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils,
                                     emailBlacklist,
                                     tenantCreator,
                                     referencesManager,
                                     tenantInfoDataManager);
  }

  @Test
  public void testSignupIfTenantOnlineAndUserAllowed(ITestContext context) throws CloudAdminException {
    final String USERNAME = "test";
    final String EMAIL = "test@tenant.com";
    final String TENANT = "tenant";
    final TenantState STATE = TenantState.ONLINE;

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(emailBlacklist.isInBlackList(EMAIL)).thenReturn(false);
    Mockito.when(cloudIntranetUtils.email2tenantName(EMAIL)).thenReturn(TENANT);
    Mockito.when(tenantCreator.createTenantWithEmailConfirmation(TENANT, EMAIL))
           .thenThrow(TenantAlreadyExistException.class);
    Mockito.when(tenantInfoDataManager.getValue(TENANT, TenantInfoFieldName.PROPERTY_STATE))
           .thenReturn(STATE.toString());
    Mockito.when(workspacesOrganizationRequestPerformer.isNewUserAllowed(TENANT, USERNAME))
           .thenReturn(true);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/signup");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(emailBlacklist).isInBlackList(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(requestDao).searchByEmail(Matchers.eq(EMAIL));
    Mockito.verify(tenantCreator).createTenantWithEmailConfirmation(Matchers.eq(TENANT),
                                                                    Matchers.eq(EMAIL));
    Mockito.verify(tenantInfoDataManager).getValue(Matchers.eq(TENANT),
                                                   Matchers.eq(TenantInfoFieldName.PROPERTY_STATE));
    Mockito.verify(workspacesOrganizationRequestPerformer).isNewUserAllowed(Matchers.eq(TENANT),
                                                                            Matchers.eq(USERNAME));
    Mockito.verify(referencesManager).putEmail(Matchers.eq(EMAIL), Matchers.anyString());
    Mockito.verify(notificationMailSender).sendOkToJoinEmail(Matchers.eq(EMAIL), Matchers.anyMap());

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils,
                                     emailBlacklist,
                                     tenantCreator,
                                     referencesManager,
                                     tenantInfoDataManager,
                                     workspacesOrganizationRequestPerformer);
  }

  @Test
  public void testSignupIfTenantOnlineAndUserNotAllowed(ITestContext context) throws CloudAdminException {
    final String USERNAME = "test";
    final String EMAIL = "test@tenant.com";
    final String TENANT = "tenant";
    final TenantState STATE = TenantState.ONLINE;

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(emailBlacklist.isInBlackList(EMAIL)).thenReturn(false);
    Mockito.when(cloudIntranetUtils.email2tenantName(EMAIL)).thenReturn(TENANT);
    Mockito.when(tenantCreator.createTenantWithEmailConfirmation(TENANT, EMAIL))
           .thenThrow(TenantAlreadyExistException.class);
    Mockito.when(tenantInfoDataManager.getValue(TENANT, TenantInfoFieldName.PROPERTY_STATE))
           .thenReturn(STATE.toString());
    Mockito.when(workspacesOrganizationRequestPerformer.isNewUserAllowed(TENANT, USERNAME))
           .thenReturn(false);

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(Status.OK.getStatusCode())
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/signup");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(emailBlacklist).isInBlackList(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(requestDao).searchByEmail(Matchers.eq(EMAIL));
    Mockito.verify(tenantCreator).createTenantWithEmailConfirmation(Matchers.eq(TENANT),
                                                                    Matchers.eq(EMAIL));
    Mockito.verify(tenantInfoDataManager).getValue(Matchers.eq(TENANT),
                                                   Matchers.eq(TenantInfoFieldName.PROPERTY_STATE));
    Mockito.verify(workspacesOrganizationRequestPerformer).isNewUserAllowed(Matchers.eq(TENANT),
                                                                            Matchers.eq(USERNAME));
    Mockito.verify(requestDao).put(Matchers.any(UserRequest.class));
    Mockito.verify(notificationMailSender).sendJoinRejectedEmails(Matchers.eq(TENANT),
                                                                  Matchers.eq(EMAIL),
                                                                  Matchers.anyMap());

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils,
                                     emailBlacklist,
                                     tenantCreator,
                                     referencesManager,
                                     tenantInfoDataManager,
                                     workspacesOrganizationRequestPerformer);
  }

  @Test
  public void testSignupIfTenantOnlineAndUserAlreadyExist(ITestContext context) throws CloudAdminException {
    final String USERNAME = "test";
    final String EMAIL = "test@tenant.com";
    final String TENANT = "tenant";
    final TenantState STATE = TenantState.ONLINE;

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(emailBlacklist.isInBlackList(EMAIL)).thenReturn(false);
    Mockito.when(cloudIntranetUtils.email2tenantName(EMAIL)).thenReturn(TENANT);
    Mockito.when(tenantCreator.createTenantWithEmailConfirmation(TENANT, EMAIL))
           .thenThrow(TenantAlreadyExistException.class);
    Mockito.when(tenantInfoDataManager.getValue(TENANT, TenantInfoFieldName.PROPERTY_STATE))
           .thenReturn(STATE.toString());
    Mockito.when(workspacesOrganizationRequestPerformer.isNewUserAllowed(TENANT, USERNAME))
           .thenThrow(UserAlreadyExistsException.class);

    Mockito.when(cloudAdminConfiguration.getString(AdminConfiguration.CLOUD_ADMIN_FRONT_END_SERVER_HOST))
           .thenReturn("cloud-workspaces");
    Mockito.when(cloudAdminConfiguration.getString(AdminConfiguration.CLOUD_ADMIN_FRONT_END_SERVER_PORT))
           .thenReturn("8080");

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(309)
               .header("Location", "http://cloud-workspaces:8080/signin.jsp?email=" + EMAIL)
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/signup");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(emailBlacklist).isInBlackList(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(requestDao).searchByEmail(Matchers.eq(EMAIL));
    Mockito.verify(tenantCreator).createTenantWithEmailConfirmation(Matchers.eq(TENANT),
                                                                    Matchers.eq(EMAIL));
    Mockito.verify(tenantInfoDataManager).getValue(Matchers.eq(TENANT),
                                                   Matchers.eq(TenantInfoFieldName.PROPERTY_STATE));
    Mockito.verify(workspacesOrganizationRequestPerformer).isNewUserAllowed(Matchers.eq(TENANT),
                                                                            Matchers.eq(USERNAME));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils,
                                     emailBlacklist,
                                     tenantCreator,
                                     referencesManager,
                                     tenantInfoDataManager,
                                     workspacesOrganizationRequestPerformer);
  }

  @Test
  public void testSignupIfTenantSuspended(ITestContext context) throws CloudAdminException {
    final String USERNAME = "test";
    final String EMAIL = "test@tenant.com";
    final String TENANT = "tenant";
    final TenantState STATE = TenantState.SUSPENDED;

    Mockito.when(cloudIntranetUtils.validateEmail(EMAIL)).thenReturn(true);
    Mockito.when(emailBlacklist.isInBlackList(EMAIL)).thenReturn(false);
    Mockito.when(cloudIntranetUtils.email2tenantName(EMAIL)).thenReturn(TENANT);
    Mockito.when(tenantCreator.createTenantWithEmailConfirmation(TENANT, EMAIL))
           .thenThrow(TenantAlreadyExistException.class);
    Mockito.when(tenantInfoDataManager.getValue(TENANT, TenantInfoFieldName.PROPERTY_STATE))
           .thenReturn(STATE.toString());

    Mockito.when(cloudAdminConfiguration.getString(AdminConfiguration.CLOUD_ADMIN_FRONT_END_SERVER_HOST))
           .thenReturn("cloud-workspaces");
    Mockito.when(cloudAdminConfiguration.getString(AdminConfiguration.CLOUD_ADMIN_FRONT_END_SERVER_PORT))
           .thenReturn("8080");

    RestAssured.given()
               .auth()
               .basic(AUTH_USERNAME, AUTH_PASSWORD)
               .formParam("user-mail", EMAIL)
               .port((Integer) context.getAttribute(EverrestJetty.JETTY_PORT))
               .expect()
               .statusCode(309)
               .header("Location", "http://cloud-workspaces:8080/resuming.jsp?email=" + EMAIL)
               .when()
               .post("/rest/cloud-admin/cloudworkspaces/tenant-service/signup");

    Mockito.verify(cloudIntranetUtils).validateEmail(Matchers.eq(EMAIL));
    Mockito.verify(emailBlacklist).isInBlackList(Matchers.eq(EMAIL));
    Mockito.verify(cloudIntranetUtils).email2tenantName(Matchers.eq(EMAIL));
    Mockito.verify(requestDao).searchByEmail(Matchers.eq(EMAIL));
    Mockito.verify(tenantCreator).createTenantWithEmailConfirmation(Matchers.eq(TENANT),
                                                                    Matchers.eq(EMAIL));
    Mockito.verify(tenantInfoDataManager).getValue(Matchers.eq(TENANT),
                                                   Matchers.eq(TenantInfoFieldName.PROPERTY_STATE));
    Mockito.verify(tenantStarter).startTenant(Matchers.eq(TENANT));
    Mockito.verify(requestDao).put(Matchers.any(UserRequest.class));

    Mockito.verifyNoMoreInteractions(cloudIntranetUtils,
                                     emailBlacklist,
                                     tenantCreator,
                                     referencesManager,
                                     tenantInfoDataManager,
                                     workspacesOrganizationRequestPerformer);
  }

}
