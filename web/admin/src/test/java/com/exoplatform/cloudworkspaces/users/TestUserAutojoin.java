package com.exoplatform.cloudworkspaces.users;

import com.exoplatform.cloudworkspaces.CloudIntranetUtils;
import com.exoplatform.cloudworkspaces.NotificationMailSender;
import com.exoplatform.cloudworkspaces.ReferencesManager;
import com.exoplatform.cloudworkspaces.UserRequestDAO;
import com.exoplatform.cloudworkspaces.http.WorkspacesOrganizationRequestPerformer;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.dao.TenantDataManagerException;
import org.exoplatform.cloudmanagement.admin.dao.TenantInfoDataManager;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: makis  mshaposhnik@exoplatform.com
 * Date: 4/17/12
 * Time: 1:15 PM
 */
public class TestUserAutojoin {

  UsersManager manager;
  WorkspacesOrganizationRequestPerformer  requestPerformer;
  TenantInfoDataManager  tenantInfoDataManager;

  @BeforeMethod
  public void initMocks() throws TenantDataManagerException {
    Configuration cloudAdminConfiguration = new CompositeConfiguration();
    cloudAdminConfiguration.setProperty("cloud.admin.tenant.waiting.dir", "target/test-classes/queue");
    ReferencesManager referencesManager = new ReferencesManager(cloudAdminConfiguration);
    CloudIntranetUtils utils = new CloudIntranetUtils(referencesManager);
    requestPerformer = Mockito.mock(WorkspacesOrganizationRequestPerformer.class);
    UserLimitsStorage userLimitsStorage = Mockito.mock(UserLimitsStorage.class);
    NotificationMailSender notificationMailSender = Mockito.mock(NotificationMailSender.class);
    tenantInfoDataManager =  Mockito.mock(TenantInfoDataManager.class);
//    tenantInfoDataManager.set("aaa", "state", "ONLINE");
//    tenantInfoDataManager.set("bbb", "state", "ONLINE");
//    tenantInfoDataManager.set("ccc", "state", "ONLINE");
    UserRequestDAO   userRequestDao  = new UserRequestDAO(cloudAdminConfiguration);

    this.manager = new UsersManager(cloudAdminConfiguration,
                                            requestPerformer,
                                            tenantInfoDataManager,
                                            notificationMailSender,
                                            userLimitsStorage,
                                            userRequestDao,
                                            referencesManager);
  }

  @Test
  public void testManagerCommon() throws CloudAdminException{
    Set<String> tenantSet = new HashSet<String>();
    tenantSet.add("aaa");
    tenantSet.add("bbb");
    tenantSet.add("ccc");
    Mockito.when(tenantInfoDataManager.getNames()).thenReturn(tenantSet);
    Mockito.when(tenantInfoDataManager.getValue(Matchers.anyString(), Matchers.anyString())).thenReturn("ONLINE");
    Mockito.when(requestPerformer.isNewUserAllowed(Matchers.anyString(), Matchers.anyString())).thenReturn(true);
    manager.joinAll();
    Mockito.verify(requestPerformer, Mockito.times(3)).storeUser(Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyBoolean());

  }

  @Test
  public void testManagerFreeSpace() throws CloudAdminException{
    Set<String> tenantSet = new HashSet<String>();
    tenantSet.add("aaa");
    tenantSet.add("bbb");
    tenantSet.add("ccc");
    Mockito.when(tenantInfoDataManager.getNames()).thenReturn(tenantSet);
    Mockito.when(tenantInfoDataManager.getValue(Matchers.anyString(), Matchers.anyString())).thenReturn("ONLINE");
    Mockito.when(requestPerformer.isNewUserAllowed(Matchers.anyString(), Matchers.anyString())).thenReturn(true);
    Mockito.when(requestPerformer.isNewUserAllowed(Matchers.eq("ccc"), Matchers.anyString())).thenReturn(false);
    manager.joinAll();
    Mockito.verify(requestPerformer, Mockito.times(2)).storeUser(Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyBoolean());

  }

  @Test
  public void testManagerTenantStateNotOnline() throws CloudAdminException{
    Set<String> tenantSet = new HashSet<String>();
    tenantSet.add("aaa");
    tenantSet.add("bbb");
    tenantSet.add("ccc");
    Mockito.when(tenantInfoDataManager.getNames()).thenReturn(tenantSet);
    Mockito.when(tenantInfoDataManager.getValue(Matchers.anyString(), Matchers.anyString())).thenReturn("ONLINE");
    Mockito.when(tenantInfoDataManager.getValue(Matchers.eq("ccc"), Matchers.anyString())).thenReturn("WAITING_CREATION");
    Mockito.when(requestPerformer.isNewUserAllowed(Matchers.anyString(), Matchers.anyString())).thenReturn(true);
    manager.joinAll();
    Mockito.verify(requestPerformer, Mockito.times(2)).storeUser(Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyBoolean());

  }
}
