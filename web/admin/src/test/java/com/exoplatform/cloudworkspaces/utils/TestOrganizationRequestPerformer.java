package com.exoplatform.cloudworkspaces.utils;

import com.exoplatform.cloudworkspaces.UserAlreadyExistsException;
import com.exoplatform.cloudworkspaces.http.WorkspacesOrganizationRequestPerformer;
import com.exoplatform.cloudworkspaces.http.WorkspacesUsersListResponseHandler;
import com.exoplatform.cloudworkspaces.users.UserLimitsStorage;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.exoplatform.cloudmanagement.admin.configuration.ApplicationServerConfigurationManager;
import org.exoplatform.cloudmanagement.admin.dao.TenantInfoDataManager;
import org.exoplatform.cloudmanagement.admin.http.HttpClientManager;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: makis mshaposhnik@exoplatform.com
 * Date: 4/19/12
 * Time: 11:22 AM
 */
public class TestOrganizationRequestPerformer {

  TenantInfoDataManager tenantInfoDataManager;
  ApplicationServerConfigurationManager applicationServerConfigurationManager;
  HttpClientManager httpClientManager;
  HttpClient client;
  HttpResponse response;
  UserLimitsStorage storage;
  WorkspacesOrganizationRequestPerformer performer;


  @BeforeMethod
  public void initMocks(){
    tenantInfoDataManager =  Mockito.mock(TenantInfoDataManager.class);
    applicationServerConfigurationManager = Mockito.mock(ApplicationServerConfigurationManager.class);
    httpClientManager = Mockito.mock(HttpClientManager.class);
    response = Mockito.mock(HttpResponse.class);
    client =  Mockito.mock(HttpClient.class);
    storage = Mockito.mock(UserLimitsStorage.class);
    performer = new WorkspacesOrganizationRequestPerformer(tenantInfoDataManager,
                                                           applicationServerConfigurationManager,
                                                           httpClientManager,
                                                           storage);
  }


  @Test
  public void testStoreUser() throws Exception {
    Mockito.when(tenantInfoDataManager.getValue(Matchers.anyString(), Matchers.anyString())).thenReturn("host0");
    Mockito.when(applicationServerConfigurationManager.getHttpUriToServer(Matchers.anyString())).thenReturn("aaa");
    Mockito.when(httpClientManager.getHttpClient(Matchers.anyString())).thenReturn(client);
    HttpEntity entity = new StringEntity("");
    HttpResponse resp =  new BasicHttpResponse(

            new StatusLine() {
              @Override
              public ProtocolVersion getProtocolVersion() {
                return new ProtocolVersion("http", 1, 1);
              }

              @Override
              public int getStatusCode() {
                return 201;
              }

              @Override
              public String getReasonPhrase() {
                return "Created";
              }
            });
    resp.setEntity(entity);
    ArgumentsCacheAnswer<HttpResponse> answer = new ArgumentsCacheAnswer<HttpResponse>(resp);
    Mockito.when(client.execute((HttpUriRequest) Matchers.any())).thenAnswer(answer);

    performer.storeUser("aaa", "test", "test@aaa.com", "fname", "lname", "pass", true);
    Mockito.verify(client, Mockito.atLeastOnce()).execute((HttpPost) Mockito.anyObject());
    HttpPost post = (HttpPost)answer.getArgument(0);
    String requestParams = readInputStreamAsString(post.getEntity().getContent());
    requestParams = URLDecoder.decode(requestParams, "utf-8");
    //System.out.println(requestParams);
    Assert.assertTrue(requestParams.contains("tname=aaa"));
    Assert.assertTrue(requestParams.contains("username=test"));
    Assert.assertTrue(requestParams.contains("email=test@aaa.com"));
    Assert.assertTrue(requestParams.contains("first-name=fname"));
    Assert.assertTrue(requestParams.contains("last-name=lname"));
    Assert.assertTrue(requestParams.contains("password=pass"));
    Assert.assertTrue(requestParams.contains("isadministrator=true"));
  }


  @Test
  public void testUpdatePassword() throws Exception {
    Mockito.when(tenantInfoDataManager.getValue(Matchers.anyString(), Matchers.anyString())).thenReturn("host0");
    Mockito.when(applicationServerConfigurationManager.getHttpUriToServer(Matchers.anyString())).thenReturn("aaa");
    Mockito.when(httpClientManager.getHttpClient(Matchers.anyString())).thenReturn(client);
    HttpEntity entity = new StringEntity("");
    HttpResponse resp =  new BasicHttpResponse(

            new StatusLine() {
              @Override
              public ProtocolVersion getProtocolVersion() {
                return new ProtocolVersion("http", 1, 1);
              }

              @Override
              public int getStatusCode() {
                return 200;
              }

              @Override
              public String getReasonPhrase() {
                return "Ok";
              }
            });
    resp.setEntity(entity);
    ArgumentsCacheAnswer<HttpResponse> answer = new ArgumentsCacheAnswer<HttpResponse>(resp);
    Mockito.when(client.execute((HttpUriRequest) Matchers.any())).thenAnswer(answer);

    performer.updatePassword("aaa", "test", "test@aaa.com", "pass");
    Mockito.verify(client, Mockito.atLeastOnce()).execute((HttpPost) Mockito.anyObject());
    HttpPost post = (HttpPost)answer.getArgument(0);
    String requestParams = readInputStreamAsString(post.getEntity().getContent());
    requestParams = URLDecoder.decode(requestParams, "utf-8");
    //System.out.println(requestParams);
    Assert.assertTrue(requestParams.contains("tname=aaa"));
    Assert.assertTrue(requestParams.contains("username=test"));
    Assert.assertTrue(requestParams.contains("password=pass"));
  }

  @Test
  public void testGetTenantUsers() throws Exception {
    Mockito.when(tenantInfoDataManager.getValue(Matchers.anyString(), Matchers.anyString())).thenReturn("host0");
    Mockito.when(applicationServerConfigurationManager.getHttpUriToServer(Matchers.anyString())).thenReturn("aaa");
    Mockito.when(httpClientManager.getHttpClient(Matchers.anyString())).thenReturn(client);
    HttpEntity entity = new StringEntity("");
    HttpResponse resp = null;
    ArgumentsCacheAnswer<HttpResponse> answer = new ArgumentsCacheAnswer<HttpResponse>(resp);
    Mockito.when(client.execute((HttpUriRequest) Matchers.any(), (WorkspacesUsersListResponseHandler) Mockito.anyObject())).thenAnswer(answer);

    performer.getTenantUsers("aaa", false);
    Mockito.verify(client, Mockito.atLeastOnce()).execute((HttpGet) Mockito.anyObject(), (WorkspacesUsersListResponseHandler) Mockito.anyObject());
    HttpGet get = (HttpGet)answer.getArgument(0);
    String requestParams = get.getURI().toString();
    Assert.assertTrue(requestParams.contains("aaa"));
    Assert.assertTrue(requestParams.contains("administratorsonly=false"));
  }

  @Test
  public void testGetTenantAdministrators() throws Exception {
    Mockito.when(tenantInfoDataManager.getValue(Matchers.anyString(), Matchers.anyString())).thenReturn("host0");
    Mockito.when(applicationServerConfigurationManager.getHttpUriToServer(Matchers.anyString())).thenReturn("aaa");
    Mockito.when(httpClientManager.getHttpClient(Matchers.anyString())).thenReturn(client);
    HttpEntity entity = new StringEntity("");
    HttpResponse resp = null;
    ArgumentsCacheAnswer<HttpResponse> answer = new ArgumentsCacheAnswer<HttpResponse>(resp);
    Mockito.when(client.execute((HttpUriRequest) Matchers.any(), (WorkspacesUsersListResponseHandler) Mockito.anyObject())).thenAnswer(answer);

    performer.getTenantUsers("aaa", true);
    Mockito.verify(client, Mockito.atLeastOnce()).execute((HttpGet) Mockito.anyObject(), (WorkspacesUsersListResponseHandler) Mockito.anyObject());
    HttpGet get = (HttpGet)answer.getArgument(0);
    String requestParams = get.getURI().toString();
    Assert.assertTrue(requestParams.contains("aaa"));
    Assert.assertTrue(requestParams.contains("administratorsonly=true"));
  }

  @Test
  public void testIsNewUserAllowed() throws Exception {
    Mockito.when(tenantInfoDataManager.getValue(Matchers.anyString(), Matchers.anyString())).thenReturn("host0");
    Mockito.when(applicationServerConfigurationManager.getHttpUriToServer(Matchers.anyString())).thenReturn("aaa");
    Mockito.when(httpClientManager.getHttpClient(Matchers.anyString())).thenReturn(client);
    Mockito.when(storage.getMaxUsersForTenant(Matchers.eq("aaa"))).thenReturn(10);
    Map<String, String> map = new HashMap<String, String>();
    map.put("test", "test@aaa.com");
    map.put("any", "any@yaaa.com");
    Mockito.when(client.execute((HttpUriRequest) Matchers.any(), (WorkspacesUsersListResponseHandler) Mockito.anyObject())).thenReturn(map);
    Assert.assertTrue(performer.isNewUserAllowed("aaa", "test1"));
  }

  @Test
  public void testIsNewUserAllowedLimitReached() throws Exception {
    Mockito.when(tenantInfoDataManager.getValue(Matchers.anyString(), Matchers.anyString())).thenReturn("host0");
    Mockito.when(applicationServerConfigurationManager.getHttpUriToServer(Matchers.anyString())).thenReturn("aaa");
    Mockito.when(httpClientManager.getHttpClient(Matchers.anyString())).thenReturn(client);
    Mockito.when(storage.getMaxUsersForTenant(Matchers.eq("aaa"))).thenReturn(2);
    Map<String, String> map = new HashMap<String, String>();
    map.put("test", "test@aaa.com");
    map.put("any", "any@yaaa.com");
    Mockito.when(client.execute((HttpUriRequest) Matchers.any(), (WorkspacesUsersListResponseHandler) Mockito.anyObject())).thenReturn(map);
    Assert.assertFalse(performer.isNewUserAllowed("aaa", "test123"));
  }


  @Test(expectedExceptions = {UserAlreadyExistsException.class})
  public void testIsNewUserAllowedWhenExists() throws Exception {
    Mockito.when(tenantInfoDataManager.getValue(Matchers.anyString(), Matchers.anyString())).thenReturn("host0");
    Mockito.when(applicationServerConfigurationManager.getHttpUriToServer(Matchers.anyString())).thenReturn("aaa");
    Mockito.when(httpClientManager.getHttpClient(Matchers.anyString())).thenReturn(client);
    Mockito.when(storage.getMaxUsersForTenant(Matchers.eq("aaa"))).thenReturn(10);
    Map<String, String> map = new HashMap<String, String>();
    map.put("test", "test@aaa.com");
    map.put("any", "any@yaaa.com");
    Mockito.when(client.execute((HttpUriRequest) Matchers.any(), (WorkspacesUsersListResponseHandler) Mockito.anyObject())).thenReturn(map);
    Assert.assertFalse(performer.isNewUserAllowed("aaa", "any"));
  }




  class ArgumentsCacheAnswer<T> implements Answer<T> {

    private final List<Object> arguments;

    private final T            result;

    public ArgumentsCacheAnswer(T result) {
      this.result = result;
      this.arguments = new ArrayList<Object>();
    }

    @Override
    public T answer(InvocationOnMock invocation) throws Throwable {
      for (Object argument : invocation.getArguments())
        arguments.add(argument);
      return result;
    }

    public Object getArgument(int index) {
      return arguments.get(index);
    }

    public List<Object> getArguments() {
      return arguments;
    }

  }


  public static String readInputStreamAsString(InputStream in)
          throws IOException {

    BufferedInputStream bis = new BufferedInputStream(in);
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    int result = bis.read();
    while(result != -1) {
      byte b = (byte)result;
      buf.write(b);
      result = bis.read();
    }
    return buf.toString("UTF-8");
  }

}
