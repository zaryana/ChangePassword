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
package com.exoplatform.cloudworkspaces.installer.rest;

import com.exoplatform.cloudworkspaces.installer.InstallerException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class M10CloudAdminServices implements CloudAdminServices {

  protected final String            tenantMasterhost;

  protected final DefaultHttpClient httpClient;

  public M10CloudAdminServices(String tenantMasterhost, String adminUsername, String adminPassword) throws InstallerException {
    try {
      this.tenantMasterhost = tenantMasterhost;
      this.httpClient = new DefaultHttpClient();
      URI masterhostUri = new URI(tenantMasterhost);
      httpClient.getCredentialsProvider()
                .setCredentials(new AuthScope(masterhostUri.getHost(), masterhostUri.getPort()),
                                new UsernamePasswordCredentials(adminUsername, adminPassword));

    } catch (URISyntaxException e) {
      throw new InstallerException(e);
    }
  }

  public String serverStart(String cloudType) throws AdminException {
    HttpResponse response = doPostRequest("/rest/private/cloud-admin/instance-service/start-server?type="
        + cloudType);
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      return handler.streamToString(handler.handleResponse(response).getContent());
    } catch (IllegalStateException e) {
      throw new AdminException(e);
    } catch (IOException e) {
      throw new AdminException(e);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public void serverStop(String alias) throws AdminException {
    HttpResponse response = doPostRequest("/rest/private/cloud-admin/instance-service/stop-server?alias="
        + alias);
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      handler.handleResponse(response);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public Map<String, String> serverStates() throws AdminException {
    HttpResponse response = doGetRequest("/rest/private/cloud-admin/instance-service/server-states");
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      return handler.parseToMap(handler.handleResponse(response));
    } catch (IOException e) {
      throw new AdminException(e);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public String serverState(String alias) throws AdminException {
    HttpResponse response = doGetRequest("/rest/private/cloud-admin/instance-service/server-state/"
        + alias);
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      return handler.streamToString(handler.handleResponse(response).getContent());
    } catch (IllegalStateException e) {
      throw new AdminException(e);
    } catch (IOException e) {
      throw new AdminException(e);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public Map<String, Map<String, String>> getTypes() throws AdminException {
    HttpResponse response = doGetRequest("/rest/private/cloud-admin/instance-service/get-types");
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      return handler.parseToMapMap(handler.handleResponse(response));
    } catch (IOException e) {
      throw new AdminException(e);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public Map<String, Map<String, Object>> describeInstances() throws AdminException {
    HttpResponse response = doGetRequest("/rest/private/cloud-admin/instance-service/describe-instances");
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      return handler.parseToMapMapObject(handler.handleResponse(response));
    } catch (IOException e) {
      throw new AdminException(e);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public Map<String, Object> describeInstance(String instanceId) throws AdminException {
    HttpResponse response = doGetRequest("/rest/private/cloud-admin/instance-service/describe-instance?instanceId="
        + instanceId);
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      return handler.parseToMapObject(handler.handleResponse(response));
    } catch (IOException e) {
      throw new AdminException(e);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public Map<String, String> databaseConfig(String dbAlias) throws AdminException {
    HttpResponse response = doGetRequest("/rest/private/cloud-admin/db-service/configuration?dbAlias="
        + dbAlias);
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      return handler.parseToMap(handler.handleResponse(response));
    } catch (IOException e) {
      throw new AdminException(e);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public void allowAutoscaling() throws AdminException {
    HttpResponse response = doPostRequest("/rest/private/cloud-admin/autoscaling-service/allow-autoscaling");
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      handler.handleResponse(response);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public void blockAutoscaling() throws AdminException {
    HttpResponse response = doPostRequest("/rest/private/cloud-admin/autoscaling-service/block-autoscaling");
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      handler.handleResponse(response);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public String autoscalingStatus() throws AdminException {
    HttpResponse response = doGetRequest("/rest/private/cloud-admin/autoscaling-service/autoscaling-status");
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      return handler.streamToString(handler.handleResponse(response).getContent());
    } catch (IllegalStateException e) {
      throw new AdminException(e);
    } catch (IOException e) {
      throw new AdminException(e);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public void autoscalingCheck() throws AdminException {
    HttpResponse response = doGetRequest("/rest/private/cloud-admin/autoscaling-service/check-servers");
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      handler.handleResponse(response);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public Map<String, String> createTenant(String tenant, String email) throws AdminException {
    HttpResponse response = doPostRequest("/rest/private/cloud-admin/tenant-service/create/"
        + tenant + "/" + email);
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      return handler.parseToMap(handler.handleResponse(response));
    } catch (IOException e) {
      throw new AdminException(e);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public Map<String, String> tenantStatus(String tenant) throws AdminException {
    HttpResponse response = doGetRequest("/rest/private/cloud-admin/tenant-service/tenant-status?tenant="
        + tenant);
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      return handler.parseToMap(handler.handleResponse(response));
    } catch (IOException e) {
      throw new AdminException(e);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public void tenantEnable(String tenant) throws AdminException {
    HttpResponse response = doPostRequest("/rest/private/cloud-admin/tenant-service/enable?tenant="
        + tenant);
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      handler.handleResponse(response);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public void tenantDisable(String tenant) throws AdminException {
    HttpResponse response = doPostRequest("/rest/private/cloud-admin/tenant-service/disable?tenant="
        + tenant);
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      handler.handleResponse(response);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public void tenantStart(String tenant) throws AdminException {
    HttpResponse response = doPostRequest("/rest/private/cloud-admin/tenant-service/start?tenant="
        + tenant);
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      handler.handleResponse(response);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public void tenantStop(String tenant) throws AdminException {
    HttpResponse response = doPostRequest("/rest/private/cloud-admin/tenant-service/stop?tenant="
        + tenant);
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      handler.handleResponse(response);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public void tenantRemove(String tenant) throws AdminException {
    HttpResponse response = doPostRequest("/rest/private/cloud-admin/tenant-service/remove?tenant="
        + tenant);
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      handler.handleResponse(response);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public void tenantRestartCreation(String tenant) throws AdminException {
    HttpResponse response = doPostRequest("/rest/private/cloud-admin/tenant-service/restart-creation?tenant="
        + tenant);
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      handler.handleResponse(response);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public List<String> tenantList() throws AdminException {
    HttpResponse response = doGetRequest("/rest/private/cloud-admin/tenant-service/tenant-list-all");
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      return handler.parseToList(handler.handleResponse(response));
    } catch (IOException e) {
      throw new AdminException(e);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public Map<String, List<String>> tenantListOrderAs() throws AdminException {
    HttpResponse response = doGetRequest("/rest/private/cloud-admin/tenant-service/tenant-list-order-as");
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      return handler.parseToMapList(handler.handleResponse(response));
    } catch (IOException e) {
      throw new AdminException(e);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public Map<String, List<String>> tenantListOrderState() throws AdminException {
    HttpResponse response = doGetRequest("/rest/private/cloud-admin/tenant-service/tenant-list-order-state");
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      return handler.parseToMapList(handler.handleResponse(response));
    } catch (IOException e) {
      throw new AdminException(e);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public int tenantNumber() throws AdminException {
    HttpResponse response = doGetRequest("/rest/private/cloud-admin/tenant-service/tenant-number");
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      return handler.parseAsJson(handler.handleResponse(response))
                    .getElement("tenant-number")
                    .getIntValue();
    } catch (IOException e) {
      throw new AdminException(e);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public Map<String, Integer> tenantNumberOrderAs() throws AdminException {
    HttpResponse response = doGetRequest("/rest/private/cloud-admin/tenant-service/tenant-number-order-as");
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      return handler.parseToIntMap(handler.handleResponse(response));
    } catch (IOException e) {
      throw new AdminException(e);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  public Map<String, Integer> tenantNumberOrderState() throws AdminException {
    HttpResponse response = doGetRequest("/rest/private/cloud-admin/tenant-service/tenant-number-order-state");
    try {
      BaseResponseHandler handler = new BaseResponseHandler();
      return handler.parseToIntMap(handler.handleResponse(response));
    } catch (IOException e) {
      throw new AdminException(e);
    } finally {
      try {
        response.getEntity().getContent().close();
      } catch (IllegalStateException e) {
        throw new AdminException(e);
      } catch (IOException e) {
        throw new AdminException(e);
      }
    }
  }

  protected HttpResponse doPostRequest(String rest) throws AdminException {
    return doRequest(new HttpPost("http://" + tenantMasterhost + rest));
  }

  protected HttpResponse doGetRequest(String rest) throws AdminException {
    return doRequest(new HttpGet("http://" + tenantMasterhost + rest));
  }

  protected HttpResponse doRequest(HttpRequestBase request) throws AdminException {
    try {
      return httpClient.execute(request);
    } catch (ClientProtocolException e) {
      throw new AdminException(e);
    } catch (IOException e) {
      throw new AdminException(e);
    }
  }

}
