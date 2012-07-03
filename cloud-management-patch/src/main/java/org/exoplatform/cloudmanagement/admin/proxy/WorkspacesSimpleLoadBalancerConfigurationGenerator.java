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

import static org.exoplatform.cloudmanagement.admin.configuration.ApplicationServerConfiguration.ON_MAINTENANCE;

import com.google.common.io.Closeables;

import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.configuration.ApplicationServerConfigurationManager;
import org.exoplatform.cloudmanagement.admin.configuration.ProxyConfiguration;
import org.exoplatform.cloudmanagement.admin.configuration.TenantInfoFieldName;
import org.exoplatform.cloudmanagement.admin.dao.TenantInfoDataManager;
import org.exoplatform.cloudmanagement.admin.loadbalancer.model.BackendSection;
import org.exoplatform.cloudmanagement.admin.loadbalancer.model.ConfigurationSection;
import org.exoplatform.cloudmanagement.admin.loadbalancer.model.FrontendSection;
import org.exoplatform.cloudmanagement.admin.loadbalancer.model.LoadBalancerConfiguration;
import org.exoplatform.cloudmanagement.admin.loadbalancer.model.parameter.Parameter;
import org.exoplatform.cloudmanagement.admin.loadbalancer.model.parameter.ProxyParameter;
import org.exoplatform.cloudmanagement.admin.loadbalancer.model.parameter.proxy.AclParameter;
import org.exoplatform.cloudmanagement.admin.loadbalancer.model.parameter.proxy.BindParameter;
import org.exoplatform.cloudmanagement.admin.loadbalancer.model.parameter.proxy.CaptureRequestHeaderParameter;
import org.exoplatform.cloudmanagement.admin.loadbalancer.model.parameter.proxy.CommentParameter;
import org.exoplatform.cloudmanagement.admin.loadbalancer.model.parameter.proxy.DefaultBackendParameter;
import org.exoplatform.cloudmanagement.admin.loadbalancer.model.parameter.proxy.ModeParameter;
import org.exoplatform.cloudmanagement.admin.loadbalancer.model.parameter.proxy.RedirectParameter;
import org.exoplatform.cloudmanagement.admin.loadbalancer.model.parameter.proxy.ServerParameter;
import org.exoplatform.cloudmanagement.admin.loadbalancer.model.parameter.proxy.UseBackendParameter;
import org.exoplatform.cloudmanagement.admin.proxy.WorkspacesProxyDirectionSelector.Direction;
import org.exoplatform.cloudmanagement.admin.status.ApplicationServerState;
import org.exoplatform.cloudmanagement.admin.status.ApplicationServerStatus;
import org.exoplatform.cloudmanagement.admin.status.ApplicationServerStatusManager;
import org.exoplatform.cloudmanagement.admin.util.AdminConfigurationUtil;
import org.exoplatform.cloudmanagement.status.TenantState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 *  
 */
public class WorkspacesSimpleLoadBalancerConfigurationGenerator implements CloudLoadBalancerConfigurationGenerator
{

   /**
    * Generate admin backend
    */
   private class AdminBackendGenerator implements GenerationStep
   {
      public static final String ADMIN_BK_NAME = "cl_admin_bk";

      @Override
      public LoadBalancerConfiguration generate(LoadBalancerConfiguration configuration) throws CloudAdminException
      {
         // add admin backend
         BackendSection backend = new BackendSection(ADMIN_BK_NAME);
         backend.addParameter(new ModeParameter("http"));
         backend.addParameter(new ServerParameter(ADMIN_BK_NAME, adminConfiguration
            .getString(ProxyConfiguration.CLOUD_ADMIN_HAPROXY_CLOUD_ADMIN_ADDR), "check"));
         configuration.addConfigurationSection(backend);
         return configuration;
      }
   }

   /**
    * Generate backend configuration
    */
   private class BackendGenerator implements GenerationStep
   {

      @Override
      public LoadBalancerConfiguration generate(LoadBalancerConfiguration configuration) throws CloudAdminException
      {
         // add online servers backends
         for (ApplicationServerStatus asStatus : applicationServerManager.getApplicationServerStatusMap().values())
         {
            String alias = asStatus.getAlias();
            switch (asStatus.getServerState())
            {
               case ONLINE :
                  BackendSection backend = new BackendSection("bk_" + alias);
                  backend.addParameter(new ModeParameter("http"));

                  String httpUriToServer = serverConfigurationManager.getHttpUriToServer(alias);
                  //uri without last slash and http://
                  backend.addParameter(new ServerParameter("bk_" + alias, httpUriToServer.substring(7,
                     httpUriToServer.length() - 1), "check"));
                  configuration.addConfigurationSection(backend);
                  break;

            }
         }
         return configuration;
      }
   }

   /**
    * Generate frontend
    */
   private class FrontendGenerator implements GenerationStep
   {

      @Override
      public LoadBalancerConfiguration generate(LoadBalancerConfiguration configuration) throws CloudAdminException
      {
         List<ProxyParameter> aclList = new ArrayList<ProxyParameter>();
         List<ProxyParameter> useRuleList = new ArrayList<ProxyParameter>();
         List<ProxyParameter> redirectRuleList = new ArrayList<ProxyParameter>();

         Set<String> activeBackends = getActiveBackendsAlias(configuration);

         // add frontend
         FrontendSection frontendSection = new FrontendSection("http-in");
         frontendSection.addParameter(new BindParameter("*:80"));

         // add logging tenant name
         if (adminConfiguration.getBoolean(ProxyConfiguration.CLOUD_ADMIN_HAPROXY_CONF_LOGGING, false))
         {
            frontendSection.addParameter(new CaptureRequestHeaderParameter("Host", "45"));
         }

         frontendSection.addParameter(new CommentParameter("cloud-admin"));

         // redirect /rest/cloud-admin and /rest/private/cloud-admin to cloud
         // admin
         aclList.add(new AclParameter("acl_cloud_admin", "url_beg", "/rest" + "/cloud-admin"));
         aclList.add(new AclParameter("acl_cloud_admin", "url_beg", "/rest/private" + "/cloud-admin"));

         // admin shell rules
         // Redirect all www.cloud-ide.com/admin-shell and
         // cloud-ide.com/admin-shell request to cloud-admin
         aclList.add(new AclParameter("acl_cloud_admin_shell", "url_beg -i", "/admin-shell"));

         aclList.add(new AclParameter("acl_master_host_no_www", "hdr_beg(Host)", AdminConfigurationUtil
            .getMasterHost(adminConfiguration)));
         aclList.add(new AclParameter("acl_master_host_www", "hdr_beg(Host)", "www."
            + AdminConfigurationUtil.getMasterHost(adminConfiguration)));

         useRuleList.add(new UseBackendParameter(AdminBackendGenerator.ADMIN_BK_NAME, "if", "acl_cloud_admin"));
         useRuleList.add(new UseBackendParameter(AdminBackendGenerator.ADMIN_BK_NAME, "if",
            "acl_cloud_admin_shell acl_master_host_no_www or acl_cloud_admin_shell acl_master_host_www"));

         //sort it with natural order.
         Map<String, Map<String, String>> allTenants =
            new TreeMap<String, Map<String, String>>(infoDataManager.getKeyValues());
         LOG.debug("Tenant number for proxy configuration {} ", allTenants.size());

         for (Entry<String, Map<String, String>> tenantKeyValue : allTenants.entrySet())
         {

            Map<String, String> tenantStatusMap = tenantKeyValue.getValue();
            String tenantName = tenantKeyValue.getKey();

            if (!tenantStatusMap.containsKey(TenantInfoFieldName.PROPERTY_STATE))
            {
               LOG.warn("incomplete tenant {} information. No state field found", tenantName);
               continue;
            }

            String asAlias = tenantStatusMap.get(TenantInfoFieldName.PROPERTY_APSERVER_ALIAS);

            ApplicationServerState asState = null;
            if (asAlias != null)
            {

               ApplicationServerStatus applicationServerStatus =
                  applicationServerManager.getApplicationServerStatus(asAlias);
               if (applicationServerStatus == null || applicationServerStatus.getServerState() == null)
               {
                  LOG.warn("Tenant {} registered on AS {} which is not specified in configuration", tenantName, asAlias);
                  continue;
               }
               asState = applicationServerStatus.getServerState();
            }

            Direction direction = Direction.SKIP;

            try
            {
               direction =
                  directionSelector.getDirection(
                     TenantState.valueOf(tenantStatusMap.get(TenantInfoFieldName.PROPERTY_STATE)), asState);
            }
            catch (CloudAdminException e)
            {
               LOG.warn(e.getLocalizedMessage() + " in tenant {} on {}", tenantName, asAlias);
            }
            // tenant destination
            switch (direction)
            {
               case ADMIN :
                  // tenant condition
                  aclList.add(new AclParameter("acl_" + tenantName, "hdr_beg(Host)", tenantName + "."
                     + AdminConfigurationUtil.getMasterHost(adminConfiguration)));

                  useRuleList.add(new UseBackendParameter(AdminBackendGenerator.ADMIN_BK_NAME, "if", "acl_"
                     + tenantName));
                  break;
               case APPLICATION_SERVER :
                  if (activeBackends.contains("bk_" + asAlias))
                  {
                     // tenant condition
                     aclList.add(new AclParameter("acl_" + tenantName, "hdr_beg(Host)", tenantName + "."
                        + AdminConfigurationUtil.getMasterHost(adminConfiguration)));

                     useRuleList.add(new UseBackendParameter("bk_" + asAlias, "if", "acl_" + tenantName));
                  }
                  else
                  {
                     LOG.warn("No backend defined  for tenant {} on as {}", tenantName, asAlias);
                  }
                  break;
               case MAINTENANCE :
                  // tenant condition
                  aclList.add(new AclParameter("acl_" + tenantName, "hdr_beg(Host)", tenantName + "."
                     + AdminConfigurationUtil.getMasterHost(adminConfiguration)));

                  redirectRuleList.add(new RedirectParameter("location", adminConfiguration
                     .getString(ProxyConfiguration.CLOUD_ADMIN_HAPROXY_MAINTENANCE_PAGE), "if", "acl_" + tenantName));
                  break;
               case SKIP :
                  break;
               default :
                  LOG.warn("Unknown direction {} for tenant {}", direction.toString(), tenantName);
                  break;
            };

         }
         for (ProxyParameter proxyParameter : aclList)
         {
            frontendSection.addParameter(proxyParameter);
         }
         for (ProxyParameter proxyParameter : redirectRuleList)
         {
            frontendSection.addParameter(proxyParameter);

         }

         for (ProxyParameter proxyParameter : useRuleList)
         {
            frontendSection.addParameter(proxyParameter);

         }

         // add default backend
         String defaultBk = getLowestLoadDefaultServer(frontendSection, configuration, adminConfiguration);
         if (defaultBk != null)
         {
            frontendSection.addParameter(new DefaultBackendParameter(defaultBk));
         }
         else
         {
            LOG.warn("No ONLINE server found. Set admin as default backend");
            frontendSection.addParameter(new DefaultBackendParameter(AdminBackendGenerator.ADMIN_BK_NAME));
         }

         configuration.addConfigurationSection(frontendSection);
         return configuration;
      }

      /**
       * 
       * @param configuration
       * @return names of active backends in configuration
       */
      private Set<String> getActiveBackendsAlias(LoadBalancerConfiguration configuration)
      {
         Set<String> result = new HashSet<String>();
         for (ConfigurationSection section : configuration.getConfigurationSections())
         {
            if (section instanceof BackendSection)
            {
               result.add(((BackendSection)section).getName());
            }
         }
         return result;

      }
   }

   /**
    * Generate default configuration
    */
   private class GenerateFromDefault implements GenerationStep
   {

      @Override
      public LoadBalancerConfiguration generate(LoadBalancerConfiguration configuration) throws CloudAdminException
      {
         String haproxyConfigurationFile =
            adminConfiguration.getString(ProxyConfiguration.CLOUD_ADMIN_HAPROXY_CONF_DEFAULT_FILE);

         InputStream defaultConfig = null;

         try
         {
            if (haproxyConfigurationFile != null)
            {
               defaultConfig = new BufferedInputStream(new FileInputStream(haproxyConfigurationFile));
            }
            else
            {
               defaultConfig = this.getClass().getResourceAsStream("/" + HAPROXY_DEFAULT_CONFIG);
            }
            // BufferedReader input = new BufferedReader(
            return new LoadBalancerConfiguration(new InputStreamReader(defaultConfig));

         }
         catch (FileNotFoundException e)
         {
            throw new CloudAdminException(e.getLocalizedMessage(), e);
         }
         catch (IOException e)
         {
            throw new CloudAdminException(e.getLocalizedMessage(), e);
         }
         finally
         {
            if (defaultConfig != null)
            {
               Closeables.closeQuietly(defaultConfig);
            }
         }
      }
   }

   /**
    * One step in process of LoadBalancer configuration generation.
    * 
    */
   private interface GenerationStep
   {
      LoadBalancerConfiguration generate(LoadBalancerConfiguration configuration) throws CloudAdminException;
   }

   private static final Logger LOG = LoggerFactory.getLogger(SimpleLoadBalancerConfigurationGenerator.class);

   private final Configuration adminConfiguration;

   private final List<GenerationStep> generationQueue;

   private final WorkspacesProxyDirectionSelector directionSelector;

   private final TenantInfoDataManager infoDataManager;

   private final ApplicationServerStatusManager applicationServerManager;

   private final ApplicationServerConfigurationManager serverConfigurationManager;

   /**
    * @param infoDataManager
    * @param adminConfiguration
    */
   public WorkspacesSimpleLoadBalancerConfigurationGenerator(TenantInfoDataManager infoDataManager,
      ApplicationServerStatusManager applicationServerManager,
      ApplicationServerConfigurationManager serverConfigurationManager, Configuration adminConfiguration)
   {
      super();
      this.infoDataManager = infoDataManager;
      this.applicationServerManager = applicationServerManager;
      this.serverConfigurationManager = serverConfigurationManager;
      this.adminConfiguration = adminConfiguration;
      this.directionSelector = new WorkspacesProxyDirectionSelector();
      this.generationQueue = new LinkedList<GenerationStep>();
      this.generationQueue.add(new GenerateFromDefault());
      this.generationQueue.add(new BackendGenerator());
      this.generationQueue.add(new AdminBackendGenerator());
      this.generationQueue.add(new FrontendGenerator());
   }

   @Override
   public LoadBalancerConfiguration generate() throws CloudAdminException
   {
      LoadBalancerConfiguration result = null;
      for (GenerationStep step : generationQueue)
      {
         result = step.generate(result);
      }
      return result;
   }

   /**
    * 
    * @param frontendSection
    * @param adminConfiguration
    * @return
    * @throws CloudAdminException
    */
   private String getLowestLoadDefaultServer(FrontendSection frontendSection, LoadBalancerConfiguration configuration,
      Configuration adminConfiguration) throws CloudAdminException
   {
      Map<String, Integer> map = new HashMap<String, Integer>();

      for (ConfigurationSection section : configuration.getConfigurationSections())
      {
         if (section instanceof BackendSection && ((BackendSection)section).getName().startsWith("bk_"))
         {
            map.put(((BackendSection)section).getName(), 0);
         }
      }

      // BackendSection lowesLoadBackend = null;
      for (Parameter configurationParam : frontendSection.getConfigurationParameters())
      {
         if (configurationParam instanceof UseBackendParameter)
         {
            final String backend = ((UseBackendParameter)configurationParam).getBackend();

            if (backend.startsWith("bk_"))
            {
               map.put(backend, map.get(backend) + 1);
            }
         }
      }
      // make sorting optimization
      String lowestLoadBk = null;
      int lowestLoad = Integer.MAX_VALUE;

      for (Entry<String, Integer> element : map.entrySet())
      {
         if (lowestLoadBk == null || element.getValue() < lowestLoad)
         {
            final ApplicationServerStatus applicationServerStatus =
               applicationServerManager.getApplicationServerStatus(element.getKey().substring(3));

            if (applicationServerStatus != null)
            {
               LOG.debug("Bk {} state{}", element.getKey().substring(3), applicationServerStatus.toString());

               if (!serverConfigurationManager.getConfiguration(applicationServerStatus.getAlias()).getBoolean(
                  ON_MAINTENANCE))
               {
                  lowestLoadBk = element.getKey();
                  lowestLoad = element.getValue();
               }
            }

         }
      }

      if (lowestLoadBk != null)
      {
         return lowestLoadBk;
      }
      else
      {

         String defaultAlias = null;
         for (ApplicationServerStatus server : applicationServerManager.getApplicationServerStatusMap().values())
         {
            if (server.getServerState().equals(ApplicationServerState.ONLINE))
            {
               defaultAlias = "bk_" + server.getAlias();
               break;
            }
         }

         return defaultAlias;
      }

   }

}
