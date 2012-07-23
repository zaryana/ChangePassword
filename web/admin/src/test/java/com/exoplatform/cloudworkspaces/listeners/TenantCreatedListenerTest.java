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
package com.exoplatform.cloudworkspaces.listeners;

import com.exoplatform.cloud.admin.dao.TenantDataManagerException;
import com.exoplatform.cloud.admin.dao.TenantInfoDataManager;
import com.exoplatform.cloud.admin.tenant.TenantStateDataManager;
import com.exoplatform.cloud.status.TenantInfo;
import com.exoplatform.cloud.status.TenantState;
import com.exoplatform.cloudworkspaces.NotificationMailSender;
import com.exoplatform.cloudworkspaces.RequestState;
import com.exoplatform.cloudworkspaces.listener.TenantCreatedListener;
import com.exoplatform.cloudworkspaces.users.UsersManager;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created with IntelliJ IDEA. User: makis mshaposhnik@exoplatform.com Date:
 * 4/18/12 Time: 2:25 PM
 */
public class TenantCreatedListenerTest {

  TenantCreatedListener  listener;

  UsersManager           usersManager;

  TenantStateDataManager tenantStateDataManager;

  TenantInfo             info = Mockito.mock(TenantInfo.class);

  @BeforeMethod
  public void initMocks() throws TenantDataManagerException {
    Configuration cloudAdminConfiguration = new CompositeConfiguration();
    usersManager = Mockito.mock(UsersManager.class);
    TenantInfoDataManager tenantInfoDataManager = Mockito.mock(TenantInfoDataManager.class);
    tenantStateDataManager = new TenantStateDataManager(cloudAdminConfiguration,
                                                        tenantInfoDataManager);
    NotificationMailSender notificationMailSender = Mockito.mock(NotificationMailSender.class);
    this.listener = new TenantCreatedListener(tenantStateDataManager,
                                              notificationMailSender,
                                              usersManager);
    tenantStateDataManager.addTenantStateListener(listener);
  }

  @Test
  public void testOnTenantCreated() throws Exception {

    Mockito.when(info.getTenantName()).thenReturn("aaa");
    Mockito.when(info.getState()).thenReturn(TenantState.ONLINE);
    tenantStateDataManager.created(info);
    Mockito.verify(usersManager, Mockito.atLeastOnce())
           .joinAll(Matchers.eq("aaa"), Matchers.eq(RequestState.WAITING_JOIN));

  }

  @Test
  public void testOnTenantStarted() throws Exception {
    Mockito.when(info.getTenantName()).thenReturn("aaa");
    Mockito.when(info.getState()).thenReturn(TenantState.ONLINE);
    tenantStateDataManager.started(info.getTenantName());
    Mockito.verify(usersManager, Mockito.atLeastOnce())
           .joinAll(Matchers.eq("aaa"), Matchers.eq(RequestState.WAITING_JOIN));

  }
}
