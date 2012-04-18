/**
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
package com.exoplatform.cloudworkspaces.users;

import com.exoplatform.cloudworkspaces.*;
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

/*
 * Created with IntelliJ IDEA.
 * User: makis  mshaposhnik@exoplatform.com
 * Date: 4/17/12
 * Time: 1:15 PM
 */
public class TestUserAutojoin {

  UsersManager manager;
  WorkspacesOrganizationRequestPerformer  requestPerformer;
  ReferencesManager referencesManager;
  NotificationMailSender notificationMailSender;
  TenantInfoDataManager  tenantInfoDataManager;

  @BeforeMethod
  public void initMocks() throws TenantDataManagerException {
    Configuration cloudAdminConfiguration = new CompositeConfiguration();
    cloudAdminConfiguration.setProperty("cloud.admin.tenant.waiting.dir", "target/test-classes/queue");
    referencesManager = Mockito.mock((ReferencesManager.class));
    CloudIntranetUtils utils = Mockito.mock((CloudIntranetUtils.class));
    requestPerformer = Mockito.mock(WorkspacesOrganizationRequestPerformer.class);
    UserLimitsStorage userLimitsStorage = Mockito.mock(UserLimitsStorage.class);
    notificationMailSender = Mockito.mock(NotificationMailSender.class);
    tenantInfoDataManager =  Mockito.mock(TenantInfoDataManager.class);
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
    Mockito.verify(requestPerformer, Mockito.times(3)).storeUser(Matchers.anyString(),  // 3 of 3 users joined
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyBoolean());

  }

  @Test
  public void testNotEnoughFreeSpace() throws CloudAdminException{
    Set<String> tenantSet = new HashSet<String>();
    tenantSet.add("aaa");
    tenantSet.add("bbb");
    tenantSet.add("ccc");
    Mockito.when(tenantInfoDataManager.getNames()).thenReturn(tenantSet);
    Mockito.when(tenantInfoDataManager.getValue(Matchers.anyString(), Matchers.anyString())).thenReturn("ONLINE");
    Mockito.when(requestPerformer.isNewUserAllowed(Matchers.anyString(), Matchers.anyString())).thenReturn(true);
    Mockito.when(requestPerformer.isNewUserAllowed(Matchers.eq("ccc"), Matchers.anyString())).thenReturn(false);
    manager.joinAll();
    Mockito.verify(requestPerformer, Mockito.times(2)).storeUser(Matchers.anyString(), // 2 of 3 users joined
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyBoolean());

  }

  @Test
  public void testTenantStateNotOnline() throws CloudAdminException{
    Set<String> tenantSet = new HashSet<String>();
    tenantSet.add("aaa");
    tenantSet.add("bbb");
    tenantSet.add("ccc");
    Mockito.when(tenantInfoDataManager.getNames()).thenReturn(tenantSet);
    Mockito.when(tenantInfoDataManager.getValue(Matchers.anyString(), Matchers.anyString())).thenReturn("ONLINE");
    Mockito.when(tenantInfoDataManager.getValue(Matchers.eq("ccc"), Matchers.anyString())).thenReturn("WAITING_CREATION");
    Mockito.when(requestPerformer.isNewUserAllowed(Matchers.anyString(), Matchers.anyString())).thenReturn(true);
    manager.joinAll();
    Mockito.verify(requestPerformer, Mockito.times(2)).storeUser(Matchers.anyString(), // 2 of 3 users joined
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyBoolean());

  }

  @Test
  public void testUserRegistrationNotComplete() throws CloudAdminException{
    Set<String> tenantSet = new HashSet<String>();
    tenantSet.add("ddd");
    Mockito.when(tenantInfoDataManager.getNames()).thenReturn(tenantSet);
    Mockito.when(tenantInfoDataManager.getValue(Matchers.anyString(), Matchers.anyString())).thenReturn("ONLINE");
    Mockito.when(requestPerformer.isNewUserAllowed(Matchers.anyString(), Matchers.anyString())).thenReturn(true);
    Mockito.when(referencesManager.putEmail(Matchers.anyString(), Matchers.anyString())).thenReturn("123123123");
    manager.joinAll();
    Mockito.verify(requestPerformer, Mockito.times(0)).storeUser(Matchers.anyString(), //User not joined
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyBoolean());

    Mockito.verify(notificationMailSender, Mockito.times(1)).sendOkToJoinEmail(Matchers.anyString(), //Join link is send
                                                                               Matchers.anyMap());

  }

  @Test
  public void testUserAlreadyExists() throws CloudAdminException{
    Set<String> tenantSet = new HashSet<String>();
    tenantSet.add("bbb");
    Mockito.when(tenantInfoDataManager.getNames()).thenReturn(tenantSet);
    Mockito.when(tenantInfoDataManager.getValue(Matchers.anyString(), Matchers.anyString())).thenReturn("ONLINE");
    Mockito.when(requestPerformer.isNewUserAllowed(Matchers.anyString(), Matchers.anyString())).thenThrow(UserAlreadyExistsException.class);
    manager.joinAll();
    Mockito.verify(requestPerformer, Mockito.times(0)).storeUser(Matchers.anyString(), //User not joined
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyString(),
                                                                 Matchers.anyBoolean());
    Mockito.verify(notificationMailSender, Mockito.times(1)).sendUserJoinedEmails(Matchers.anyString(), //Appropriate email send
                                                                                  Matchers.anyString(),
                                                                                  Matchers.anyString(),
                                                                                   Matchers.anyMap());

  }

}
