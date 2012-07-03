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
package com.exoplatform.cloudworkspaces.listener;

import org.exoplatform.cloudmanagement.admin.mail.ServerMaintenanceStateChecker;
import org.exoplatform.cloudmanagement.admin.proxy.ServerStateChangesProxyReconfigurationInitiator;
import org.exoplatform.cloudmanagement.admin.recover.ServerRemoverInterrupter;
import org.exoplatform.cloudmanagement.admin.status.ServerBecomeOnlineListener;
import org.exoplatform.cloudmanagement.admin.status.ServerOnlineListenersInvoker;
import org.exoplatform.cloudmanagement.admin.tenant.InactiveTenantSuspender;
import org.exoplatform.cloudmanagement.status.TenantInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class WorkspacesServerOnlineListenersInvoker extends ServerOnlineListenersInvoker {

  private static final Logger                           LOG = LoggerFactory.getLogger(WorkspacesServerOnlineListenersInvoker.class);

  protected final ArrayList<ServerBecomeOnlineListener> workspacesOnlineListeners;

  public WorkspacesServerOnlineListenersInvoker(ServerMaintenanceStateChecker serverMaintenanceStateChecker,
                                                ServerRemoverInterrupter serverRemoverInterrupter,
                                                InactiveTenantSuspender inactiveTenantSuspender,
                                                ServerStateChangesProxyReconfigurationInitiator serverStateChangesProxyReconfigurationInitiator,
                                                JoinAllInOnlineServerListener joinAllInOnlineServerListener) {
    super(serverMaintenanceStateChecker,
          serverRemoverInterrupter,
          inactiveTenantSuspender,
          serverStateChangesProxyReconfigurationInitiator);

    this.workspacesOnlineListeners = new ArrayList<ServerBecomeOnlineListener>();
    this.workspacesOnlineListeners.add(joinAllInOnlineServerListener);
  }

  @Override
  public void invoke(String serverAlias, List<TenantInfo> currentTenantStates) {
    super.invoke(serverAlias, currentTenantStates);

    for (ServerBecomeOnlineListener listener : workspacesOnlineListeners) {
      LOG.debug("Invoke ServerBecomeOnlineListener {}", listener.getClass().getCanonicalName());
      listener.onServerBecomeOnline(serverAlias, currentTenantStates);
    }
  }

}
