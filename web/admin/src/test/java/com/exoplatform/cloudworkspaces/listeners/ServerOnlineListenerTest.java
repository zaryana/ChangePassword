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

import com.exoplatform.cloudworkspaces.listener.JoinAllInOnlineServerListener;
import com.exoplatform.cloudworkspaces.listener.WorkspacesServerOnlineListenersInvoker;
import com.exoplatform.cloudworkspaces.users.UsersManager;
import org.exoplatform.cloudmanagement.admin.dao.TenantDataManagerException;
import org.exoplatform.cloudmanagement.admin.mail.ServerMaintenanceStateChecker;
import org.exoplatform.cloudmanagement.admin.proxy.ServerStateChangesProxyReconfigurationInitiator;
import org.exoplatform.cloudmanagement.admin.recover.ServerRemoverInterrupter;
import org.exoplatform.cloudmanagement.admin.tenant.InactiveTenantSuspender;
import org.exoplatform.cloudmanagement.status.TenantInfo;
import org.exoplatform.cloudmanagement.status.TenantState;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: makis mshaposhnik@exoplatform.com
 * Date: 4/18/12
 * Time: 4:59 PM
 */
public class ServerOnlineListenerTest {

  UsersManager usersManager;
  WorkspacesServerOnlineListenersInvoker invoker;
  TenantInfo info =     Mockito.mock(TenantInfo.class);

  @BeforeMethod
  public void initMocks() throws TenantDataManagerException {
    ServerMaintenanceStateChecker checker = Mockito.mock(ServerMaintenanceStateChecker.class);
    ServerRemoverInterrupter serverRemoverInterrupter = Mockito.mock(ServerRemoverInterrupter.class);
    InactiveTenantSuspender inactiveTenantSuspender =   Mockito.mock(InactiveTenantSuspender.class);
    ServerStateChangesProxyReconfigurationInitiator serverStateChangesProxyReconfigurationInitiator = Mockito.mock(ServerStateChangesProxyReconfigurationInitiator.class);
    usersManager = Mockito.mock(UsersManager.class);
    JoinAllInOnlineServerListener listener = new JoinAllInOnlineServerListener(usersManager);
    invoker = new WorkspacesServerOnlineListenersInvoker(checker,
                                                         serverRemoverInterrupter,
                                                         inactiveTenantSuspender,
                                                         serverStateChangesProxyReconfigurationInitiator,
                                                         listener);
  }

  @Test
  public void testOnServerBecomeOnline() throws Exception {
    ArrayList<TenantInfo> list = new ArrayList<TenantInfo>();
    Mockito.when(info.getTenantName()).thenReturn("aaa");
    Mockito.when(info.getState()).thenReturn(TenantState.ONLINE);
    list.add(info);
    invoker.invoke("host1", list);
    Mockito.verify(usersManager, Mockito.atLeastOnce()).joinAll(Matchers.eq("aaa"));

  }
}
