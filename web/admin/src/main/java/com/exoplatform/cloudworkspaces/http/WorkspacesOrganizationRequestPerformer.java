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
package com.exoplatform.cloudworkspaces.http;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

import com.exoplatform.cloudworkspaces.UserAlreadyExistsException;
import com.exoplatform.cloudworkspaces.users.UserLimitsStorage;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.configuration.ApplicationServerConfigurationManager;
import org.exoplatform.cloudmanagement.admin.configuration.TenantInfoFieldName;
import org.exoplatform.cloudmanagement.admin.dao.TenantInfoDataManager;
import org.exoplatform.cloudmanagement.admin.http.HttpClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

public class WorkspacesOrganizationRequestPerformer {

  private static final Logger                         LOG                       = LoggerFactory.getLogger(WorkspacesOrganizationRequestPerformer.class);

  private static final String                         ORGANIZATION_SERVICE_PATH = "cloud-agent/rest/cloudworkspaces/organization";

  private final TenantInfoDataManager                 tenantInfoDataManager;

  private final ApplicationServerConfigurationManager applicationServerConfigurationManager;

  private final HttpClientManager                     httpClientManager;

  private final UserLimitsStorage                     userLimitsStorage;

  public WorkspacesOrganizationRequestPerformer(TenantInfoDataManager tenantInfoDataManager,
                                                ApplicationServerConfigurationManager applicationServerConfigurationManager,
                                                HttpClientManager httpClientManager,
                                                UserLimitsStorage userLimitsStorage) {
    this.tenantInfoDataManager = tenantInfoDataManager;
    this.applicationServerConfigurationManager = applicationServerConfigurationManager;
    this.httpClientManager = httpClientManager;
    this.userLimitsStorage = userLimitsStorage;
  }

  public void storeUser(String tName,
                        String userMail,
                        String firstName,
                        String lastName,
                        String password,
                        boolean isAdministrator) throws CloudAdminException {

    String username = userMail.substring(0, (userMail.indexOf("@")));

    String alias = tenantInfoDataManager.getValue(tName,
                                                  TenantInfoFieldName.PROPERTY_APSERVER_ALIAS);
    String baseUri = applicationServerConfigurationManager.getHttpUriToServer(alias);
    HttpClient httpClient = httpClientManager.getHttpClient(alias);

    StringBuilder strUrl = new StringBuilder();
    strUrl.append(baseUri);
    strUrl.append(ORGANIZATION_SERVICE_PATH + "/adduser");

    ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
    params.add(new BasicNameValuePair("tname", tName));
    params.add(new BasicNameValuePair("URI", username));
    params.add(new BasicNameValuePair("username", username));
    params.add(new BasicNameValuePair("password", password));
    params.add(new BasicNameValuePair("first-name", firstName));
    params.add(new BasicNameValuePair("last-name", lastName));
    params.add(new BasicNameValuePair("email", userMail));
    params.add(new BasicNameValuePair("isadministrator", Boolean.toString(isAdministrator)));

    HttpPost request = new HttpPost(strUrl.toString());
    HttpResponse response = null;
    try {
      request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
      response = httpClient.execute(request);
      if (response.getStatusLine().getStatusCode() != HTTP_CREATED) {
        LOG.error("Unable to add user to workspace {} ({}) - HTTP status: {}", new Object[] {
            tName, alias, response.getStatusLine().getStatusCode() });
        throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");
      }
    } catch (UnsupportedEncodingException e) {
      LOG.error(e.getMessage(), e);
      throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.",
                                    e);
    } catch (ClientProtocolException e) {
      LOG.error(e.getMessage(), e);
      throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.",
                                    e);
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.",
                                    e);
    } finally {
      if (response != null) {
        try {
          response.getEntity().getContent().close();
        } catch (IllegalStateException e) {
          throw new CloudAdminException("An problem happened during closing http connection.");
        } catch (IOException e) {
          throw new CloudAdminException("An problem happened during closing http connection.");
        }
      }
    }
  }

  public void updatePassword(String tName, String email, String password) throws CloudAdminException {
    if (email == null || password == null)
      throw new CloudAdminException("Cannot validate user with such input data. Please, review it.");

    String username = email.substring(0, (email.indexOf("@")));

    String alias = tenantInfoDataManager.getValue(tName,
                                                  TenantInfoFieldName.PROPERTY_APSERVER_ALIAS);
    String baseUri = applicationServerConfigurationManager.getHttpUriToServer(alias);
    HttpClient httpClient = httpClientManager.getHttpClient(alias);
    StringBuilder strUrl = new StringBuilder();
    strUrl.append(baseUri);
    strUrl.append(ORGANIZATION_SERVICE_PATH + "/newpassword/");
    ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
    try {
      params.add(new BasicNameValuePair("tname", tName));
      params.add(new BasicNameValuePair("username", java.net.URLEncoder.encode(username, "utf-8")));
      params.add(new BasicNameValuePair("password", java.net.URLEncoder.encode(password, "utf-8")));
    } catch (UnsupportedEncodingException e) {
      LOG.error(e.getMessage(), e);
      throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");
    }

    HttpPost request = new HttpPost(strUrl.toString());
    HttpResponse response = null;
    try {
      request.setEntity(new UrlEncodedFormEntity(params));
      response = httpClient.execute(request);
      if (response.getStatusLine().getStatusCode() != HTTP_OK) {
        LOG.error("Unable to change password user {} to workspace {} - HTTP status: {}",
                  new Object[] { email, tName, response.getStatusLine().getStatusCode() });
        throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");
      }
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");
    } finally {
      if (response != null) {
        try {
          response.getEntity().getContent().close();
        } catch (IllegalStateException e) {
          throw new CloudAdminException("An problem happened during closing http connection.");
        } catch (IOException e) {
          throw new CloudAdminException("An problem happened during closing http connection.");
        }
      }
    }
  }

  public Map<String, String> getTenantUsers(String tName, boolean isAdministratorsOnly) throws CloudAdminException {

    StringBuilder strUrl = new StringBuilder();

    String alias = tenantInfoDataManager.getValue(tName,
                                                  TenantInfoFieldName.PROPERTY_APSERVER_ALIAS);
    String baseUri = applicationServerConfigurationManager.getHttpUriToServer(alias);
    HttpClient httpClient = httpClientManager.getHttpClient(alias);

    strUrl.append(baseUri);
    strUrl.append(ORGANIZATION_SERVICE_PATH + "/users/" + tName);
    strUrl.append('?');
    strUrl.append("administratorsonly=" + isAdministratorsOnly);

    HttpGet request = new HttpGet(strUrl.toString());
    try {
      return httpClient.execute(request, new WorkspacesUsersListResponseHandler());
    } catch (ClientProtocolException e) {
      LOG.error("Unable to get users list from workspace {} - Reason: {}",
                tName,
                e.getLocalizedMessage());
      throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");
    } catch (IOException e) {
      LOG.error("Unable to get users list from workspace {} - Reason: {}",
                tName,
                e.getLocalizedMessage());
      throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");
    }
  }

  public Map<String, String> getTenantAdministrators(String tName) throws CloudAdminException {

    return getTenantUsers(tName, true);
  }

  public boolean isNewUserAllowed(String tName, String username) throws CloudAdminException {
    if (tName == null || username == null)
      throw new CloudAdminException("Cannot validate user with such input data. Please, review it.");

    // TODO throw exception if tenant not created yet
    // throw new CloudAdminException("Workspace " + tName
    // + " is not created yet or it was suspended. Please try again later.");

    int maxUsers = userLimitsStorage.getMaxUsersForTenant(tName);

    Map<String, String> users = getTenantUsers(tName, false);

    if (users.containsKey(username))
      throw new UserAlreadyExistsException("This user has already registered on workspace " + tName);

    int usersCount = users.size();
    if (users.containsKey("root"))
      usersCount--;

    return maxUsers == -1 || usersCount < maxUsers;
  }

}
