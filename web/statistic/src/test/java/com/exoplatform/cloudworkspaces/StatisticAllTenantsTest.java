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

import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.EverrestJetty.JETTY_PORT;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.everrest.assured.EverrestJetty;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.status.ApplicationServerState;
import org.exoplatform.cloudmanagement.admin.status.ApplicationServerStatus;
import org.exoplatform.cloudmanagement.admin.status.ApplicationServerStatusManager;
import org.exoplatform.cloudmanagement.admin.tenant.AgentRequestPerformer;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = { EverrestJetty.class, MockitoTestNGListener.class })
public class StatisticAllTenantsTest {

  private static final String                         REST_USERNAME = "cldadmin";

  private static final String                         REST_PASSWORD = "tomcat";

  private static final String                         ALIAS1        = "alias1";

  private static final String                         ALIAS2        = "alias2";

  private static final String                         TENANT1       = "tenant1";

  private static final String                         TENANT2       = "tenant2";

  private static final String                         TENANT1_USER  = "root1";

  private static final String                         TENANT2_USER  = "root2";

  private static final String                         TENANT1_MAIL  = "root1@localhost";

  private static final String                         TENANT2_MAIL  = "root2@localhost";

  private static ApplicationServerStatus              alias1;

  private static ApplicationServerStatus              alias2;

  private static Map<String, ApplicationServerStatus> aliases;

  private static Map<String, Map<String, String>>     alias1UserList;

  private static Map<String, Map<String, String>>     alias2UserList;

  @Mock
  private ApplicationServerStatusManager              applicationServerManager;

  @Mock
  private AgentRequestPerformer                       requestPerformer;

  /**
   * Will be deployed in jetty server
   */
  @SuppressWarnings("unused")
  @InjectMocks
  private StatisticAllTenants                         statisticAllTenants;

  @BeforeClass
  public static void init() {
    alias1 = new ApplicationServerStatus(ALIAS1);
    alias1.setServerState(ApplicationServerState.ONLINE);

    alias2 = new ApplicationServerStatus(ALIAS2);
    alias2.setServerState(ApplicationServerState.ONLINE);

    aliases = new HashMap<String, ApplicationServerStatus>();
    aliases.put(ALIAS1, alias1);
    aliases.put(ALIAS2, alias2);

    alias1UserList = new HashMap<String, Map<String, String>>();
    Map<String, String> tenant1UserList = new HashMap<String, String>();
    tenant1UserList.put(TENANT1_USER, TENANT1_MAIL);
    alias1UserList.put(TENANT1, tenant1UserList);

    alias2UserList = new HashMap<String, Map<String, String>>();
    Map<String, String> tenant2UserList = new HashMap<String, String>();
    tenant2UserList.put(TENANT2_USER, TENANT2_MAIL);
    alias2UserList.put(TENANT2, tenant2UserList);
  }

  @Test
  public void shouldReceiveUserNumberPerTenant(ITestContext context) throws CloudAdminException {
    when(applicationServerManager.getApplicationServerStatusMap()).thenReturn(aliases);
    when(requestPerformer.getUserLists(ALIAS1)).thenReturn(alias1UserList);
    when(requestPerformer.getUserLists(ALIAS2)).thenReturn(alias2UserList);

    given().auth()
           .basic(REST_USERNAME, REST_PASSWORD)
           .port((Integer) context.getAttribute(JETTY_PORT))
           .expect()
           .statusCode(Status.OK.getStatusCode())
           .body(TENANT1, equalTo(1))
           .and()
           .body(TENANT2, equalTo(1))
           .when()
           .get("rest/cloud-admin/statistic/number-of-users");
  }

  @Test
  public void shouldReceiveTenantName(ITestContext context) throws CloudAdminException {
    when(applicationServerManager.getApplicationServerStatusMap()).thenReturn(aliases);
    when(requestPerformer.getUserLists(ALIAS1)).thenReturn(alias1UserList);
    when(requestPerformer.getUserLists(ALIAS2)).thenReturn(alias2UserList);

    given().auth()
           .basic(REST_USERNAME, REST_PASSWORD)
           .port((Integer) context.getAttribute(JETTY_PORT))
           .expect()
           .statusCode(Status.OK.getStatusCode())
           .body("", hasKey(TENANT1))
           .body("", hasKey(TENANT2))
           .when()
           .get("rest/cloud-admin/statistic/number-of-users");
  }
}
