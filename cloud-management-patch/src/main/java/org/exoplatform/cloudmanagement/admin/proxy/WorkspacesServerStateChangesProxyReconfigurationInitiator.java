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
package org.exoplatform.cloudmanagement.admin.proxy;

import org.exoplatform.cloudmanagement.admin.status.ApplicationServerStatusManager;
import org.exoplatform.cloudmanagement.admin.status.ServerBecomeOfflineListener;
import org.exoplatform.cloudmanagement.admin.status.ServerBecomeOnlineListener;
import org.exoplatform.cloudmanagement.status.TenantInfo;
import org.picocontainer.Startable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;

/**
 *
 */
public class WorkspacesServerStateChangesProxyReconfigurationInitiator extends
   ServerStateChangesProxyReconfigurationInitiator implements ServerBecomeOfflineListener, ServerBecomeOnlineListener,
   Startable
{

   private static final Logger LOG = LoggerFactory.getLogger(ServerStateChangesProxyReconfigurationInitiator.class);

   private final ProxyLoadBalancerConfigurator proxyLoadBalancerConfigurator;

   private final ApplicationServerStatusManager applicationServerStatusManager;

   private boolean isStarted = false;

   private final HashSet<String> serversOnStart = new HashSet<String>();

   /**
    * @param proxyLoadBalancerConfigurator
    */
   public WorkspacesServerStateChangesProxyReconfigurationInitiator(
      ProxyLoadBalancerConfigurator proxyLoadBalancerConfigurator,
      ApplicationServerStatusManager applicationServerStatusManager)
   {
      super(proxyLoadBalancerConfigurator);
      this.proxyLoadBalancerConfigurator = proxyLoadBalancerConfigurator;
      this.applicationServerStatusManager = applicationServerStatusManager;
   }

   /**
    * @see ServerBecomeOnlineListener#onServerBecomeOnline(String,
    *      java.util.List)
    */
   @Override
   public void onServerBecomeOnline(String serverName, List<TenantInfo> currentTenantStates)
   {
      LOG.debug("Reload proxy because server {} become online", serverName);
      updateLoadBalancer(serverName);
   }

   /**
    * @see ServerBecomeOfflineListener#onServerBecomeOffline(String)
    */
   @Override
   public void onServerBecomeOffline(String serverName)
   {
      LOG.debug("Reload proxy because server {} become offline", serverName);
      updateLoadBalancer(serverName);
   }

   private void updateLoadBalancer(String serverName)
   {
      if (!isStarted)
      {
         synchronized (serversOnStart)
         {
            serversOnStart.remove(serverName);
            if (serversOnStart.isEmpty())
            {
               if (proxyLoadBalancerConfigurator instanceof WorkspacesProxyLoadBalancerConfigurator)
                  ((WorkspacesProxyLoadBalancerConfigurator)proxyLoadBalancerConfigurator).allowReloading();
               isStarted = true;
            }
            else
               return;
         }
      }
      proxyLoadBalancerConfigurator.updateLoadBalancerConfigurationWithCurrentCloudTopology();
   }

   @Override
   public void start()
   {
      serversOnStart.addAll(applicationServerStatusManager.getApplicationServerStatusMap().keySet());
      if (serversOnStart.isEmpty())
      {
         if (proxyLoadBalancerConfigurator instanceof WorkspacesProxyLoadBalancerConfigurator)
            ((WorkspacesProxyLoadBalancerConfigurator)proxyLoadBalancerConfigurator).allowReloading();
         isStarted = true;
      }
   }

   @Override
   public void stop()
   {
   }

}
