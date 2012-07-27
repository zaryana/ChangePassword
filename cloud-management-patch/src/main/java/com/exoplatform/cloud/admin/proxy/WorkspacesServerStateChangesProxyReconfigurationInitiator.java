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
package com.exoplatform.cloud.admin.proxy;

import com.exoplatform.cloud.admin.proxy.haproxy.WorkspacesHaproxyConfigurator;
import com.exoplatform.cloud.admin.status.ApplicationServerStatusManager;
import com.exoplatform.cloud.admin.status.ServerBecomeOfflineListener;
import com.exoplatform.cloud.admin.status.ServerBecomeOnlineListener;
import com.exoplatform.cloud.status.TenantInfo;

import org.picocontainer.Startable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;

/**
 *
 */
public class WorkspacesServerStateChangesProxyReconfigurationInitiator extends
   ServerStateChangesProxyReconfigurationInitiator implements Startable
{

   private static final Logger LOG = LoggerFactory
      .getLogger(WorkspacesServerStateChangesProxyReconfigurationInitiator.class);

   private final ProxyConfigurator proxyConfigurator;

   private final ApplicationServerStatusManager applicationServerStatusManager;

   private final HashSet<String> aliases = new HashSet<String>();

   /**
    * @param proxyConfigurator ProxyConfigurator
    */
   public WorkspacesServerStateChangesProxyReconfigurationInitiator(ProxyConfigurator proxyConfigurator,
      ApplicationServerStatusManager applicationServerStatusManager)
   {
      super(proxyConfigurator);
      this.proxyConfigurator = proxyConfigurator;
      this.applicationServerStatusManager = applicationServerStatusManager;
   }

   /**
    * @see ServerBecomeOnlineListener#onServerBecomeOnline(String,
    *      java.util.List)
    */
   @Override
   public void onServerBecomeOnline(String serverName, List<TenantInfo> currentTenantStates)
   {
      if (!aliases.isEmpty())
      {
         aliases.remove(serverName);
         if (aliases.isEmpty())
         {
            ((WorkspacesHaproxyConfigurator)proxyConfigurator).setReady(true);
         }
      }
      super.onServerBecomeOnline(serverName, currentTenantStates);
   }

   /**
    * @see ServerBecomeOfflineListener#onServerBecomeOffline(String)
    */
   @Override
   public void onServerBecomeOffline(String serverName)
   {
      LOG.debug("Reload proxy because server {} become offline", serverName);
      if (!aliases.isEmpty())
      {
         aliases.remove(serverName);
         if (aliases.isEmpty())
         {
            ((WorkspacesHaproxyConfigurator)proxyConfigurator).setReady(true);
         }
      }
      super.onServerBecomeOffline(serverName);
   }

   @Override
   public void start()
   {
      for (String alias : applicationServerStatusManager.getApplicationServerStatusMap().keySet())
      {
         aliases.add(alias);
      }
      if (aliases.isEmpty())
      {
         ((WorkspacesHaproxyConfigurator)proxyConfigurator).setReady(true);
         proxyConfigurator.reconfigure();
      }
   }

   @Override
   public void stop()
   {
      // do nothing
   }

}
