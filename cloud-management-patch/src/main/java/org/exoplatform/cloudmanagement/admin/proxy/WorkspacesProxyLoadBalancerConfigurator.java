/*
 * Copyright (C) 2011 eXo Platform SAS.
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

import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.configuration.ApplicationServerConfigurationManager;
import org.exoplatform.cloudmanagement.admin.dao.TenantInfoDataManager;
import org.exoplatform.cloudmanagement.admin.loadbalancer.configurator.ManagementConnection;
import org.exoplatform.cloudmanagement.admin.loadbalancer.model.LoadBalancerConfiguration;
import org.exoplatform.cloudmanagement.admin.status.ApplicationServerStatusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class WorkspacesProxyLoadBalancerConfigurator extends ProxyLoadBalancerConfigurator
{

   private static final Logger LOG = LoggerFactory.getLogger(ProxyLoadBalancerConfigurator.class);

   private final TenantInfoDataManager infoDataManager;

   private final ApplicationServerStatusManager applicationServerManager;

   private final ApplicationServerConfigurationManager serverConfigurationManager;

   private final Configuration adminConfiguration;

   public WorkspacesProxyLoadBalancerConfigurator(Configuration adminConfiguration,
      TenantInfoDataManager infoDataManager, ApplicationServerConfigurationManager serverConfigurationManager,
      ApplicationServerStatusManager applicationServerManager, ManagementConnection managementConnection)
   {
      super(adminConfiguration, infoDataManager, serverConfigurationManager, applicationServerManager,
         managementConnection);
      this.infoDataManager = infoDataManager;
      this.applicationServerManager = applicationServerManager;
      this.serverConfigurationManager = serverConfigurationManager;
      this.adminConfiguration = adminConfiguration;
   }

   /**
    * Return configuration of proxy-load balancer according to the admin
    * configuration and current state of tenants, servers
    * 
    * @throws CloudAdminException
    */
   public synchronized LoadBalancerConfiguration getLoadBalancerConfigurationOfCurrentCloudTopology()
      throws CloudAdminException
   {
      long start = System.currentTimeMillis();
      CloudLoadBalancerConfigurationGenerator configurationGenerator =
         new WorkspacesSimpleLoadBalancerConfigurationGenerator(infoDataManager, applicationServerManager,
            serverConfigurationManager, adminConfiguration);
      final LoadBalancerConfiguration result = configurationGenerator.generate();

      LOG.debug("LoadBalancerConfiguration generation {}", System.currentTimeMillis() - start);
      return result;

   }
}
