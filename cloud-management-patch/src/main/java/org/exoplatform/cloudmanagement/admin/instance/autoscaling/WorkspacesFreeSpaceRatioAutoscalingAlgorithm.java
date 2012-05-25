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
package org.exoplatform.cloudmanagement.admin.instance.autoscaling;

import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.configuration.ApplicationRecoveryConfiguration;
import org.exoplatform.cloudmanagement.admin.configuration.ApplicationServerConfiguration;
import org.exoplatform.cloudmanagement.admin.configuration.ApplicationServerConfigurationManager;
import org.exoplatform.cloudmanagement.admin.configuration.TenantInfoFieldName;
import org.exoplatform.cloudmanagement.admin.dao.TenantDataManagerException;
import org.exoplatform.cloudmanagement.admin.dao.TenantInfoDataManager;
import org.exoplatform.cloudmanagement.admin.instance.ApplicationServerType;
import org.exoplatform.cloudmanagement.admin.instance.ApplicationTypeConfigurationManager;
import org.exoplatform.cloudmanagement.admin.status.ApplicationServerStatus;
import org.exoplatform.cloudmanagement.admin.status.ApplicationServerStatusManager;
import org.exoplatform.cloudmanagement.status.TenantState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implementation of AutoscalingAlgorithm. This algorithm checks free space
 * ratio. Free space ratio calculates as: 1 - (ACTIVE_TENANTS_COUNT /
 * TOTAL_TENANT_SPACE), where ACTIVE_TENANTS_COUNT - count of online tenants and
 * tenants which suspended less than MAX_UNACTIVE_TIME ago, TOTAL_TENANT_SPACE -
 * total count of space for tenants on all application servers with any state
 * except STOPPING.
 * 
 * In cloud-admin configuration must be set MIN_FREE_SPACE_RATIO and
 * MAX_FREE_SPACE_RATIO. If current free space ratio less than
 * MIN_FREE_SPACE_RATIO, then algorithm add to result command list command to
 * start new application server with default application server type. If current
 * free space ratio more than MAX_FREE_SPACE_RATIO, then algorithm add to result
 * command list command to stop application server with min active tenants
 * count.
 */
public class WorkspacesFreeSpaceRatioAutoscalingAlgorithm implements AutoscalingAlgorithm, ApplicationRecoveryConfiguration
{

   private static final String STOP_EXPLANATION = "free space for tenants more than max";

   private static final String START_EXPLANATION = "free space for tenants less than min";

   private static final Logger LOG = LoggerFactory.getLogger(WorkspacesFreeSpaceRatioAutoscalingAlgorithm.class);

   public static final String TOTAL_SPACE = "total.space";

   public static final String TENANT_NUMBER = "tenant.number";

   public static final String MIN_RATIO = "min.ratio";

   public static final String CURRENT_RATIO = "current.ratio";

   public static final String EXPECTED_RATIO = "expected.ratio";

   public static final String MAX_RATIO = "max.ratio";

   public static final String CURRENT_SERVERS_COUNT = "current.servers.count";

   public static final String EXPECTED_SERVERS_COUNT = "expected.servers.count";

   public static final String TENANTS_FOR_STARTING = "tenants.for.starting";

   public static final String TENANTS_FOR_STOPPING = "tenants.for.stopping";

   public static final String SORTED_SERVERS = "sorted.servers";

   private final ApplicationServerStatusManager applicationServerManager;

   private final Configuration cloudAdminConfiguration;

   private final ApplicationTypeConfigurationManager applicationTypeManager;

   private final ApplicationServerConfigurationManager serverConfigurationManager;

   private final TenantInfoDataManager tenantInfoDataManager;

   public WorkspacesFreeSpaceRatioAutoscalingAlgorithm(ApplicationServerStatusManager applicationServerManager,
      Configuration cloudAdminConfiguration, ApplicationTypeConfigurationManager applicationTypeManager,
      ApplicationServerConfigurationManager serverConfigurationManager, TenantInfoDataManager tenantInfoDataManager)
   {
      super();
      this.applicationServerManager = applicationServerManager;
      this.cloudAdminConfiguration = cloudAdminConfiguration;
      this.applicationTypeManager = applicationTypeManager;
      this.serverConfigurationManager = serverConfigurationManager;
      this.tenantInfoDataManager = tenantInfoDataManager;
   }

   @Override
   public List<InstancesManagingCommand> calculate() throws CloudAdminException
   {
      if (applicationServerManager.getApplicationServerStatusMap().isEmpty())
      {
         return Collections.emptyList();
      }
      List<InstancesManagingCommand> commands = new ArrayList<InstancesManagingCommand>();

      FreeSpaceRatioAutoscalingState currentState = getCurrentState();

      double currentRatio = currentState.getCurrentRatio();
      double minRatio = currentState.getMinRatio();
      double maxRatio = currentState.getMaxRatio();
      int currentServersCount = currentState.getCurrentServersCount();
      int expectedServersCount = currentState.getExpectedServersCount();

      if (currentRatio < minRatio)
      {
         int newServersCount = expectedServersCount - currentServersCount;
         for (int i = 0; i < newServersCount; i++)
         {
            commands.add(new StartApplicationServerCommand(cloudAdminConfiguration
               .getString(CLOUD_ADMIN_APPLICATION_DEFAULT_TYPE), START_EXPLANATION));
         }
      }
      else if (currentRatio > maxRatio)
      {
         int serversStopsCount = currentServersCount - expectedServersCount;
         List<ApplicationServerStatus> servers = currentState.getSortedServers();
         Iterator<ApplicationServerStatus> serversIterator = servers.iterator();
         for (int i = 0; i < serversStopsCount; i++)
         {
            commands.add(new StopApplicationServerCommand(serversIterator.next().getAlias(), STOP_EXPLANATION));
         }
      }

      return commands;
   }

   public String explain(AutoscalingState currentAutoscalingState)
   {
      FreeSpaceRatioAutoscalingState currentState = (FreeSpaceRatioAutoscalingState)currentAutoscalingState;
      StringBuilder builder = new StringBuilder();

      // current state
      builder.append("Now: ");
      builder.append(currentState.getTenantNumber());
      builder.append(" tenants, in ");
      builder.append(currentState.getCurrentServersCount());
      builder.append(" application servers, max tenants count ");
      builder.append(currentState.getTotalSpace());
      builder.append("\n");

      // current ratio
      builder.append("Current ratio: ");
      if (currentState.getTotalSpace() != 0)
      {
         builder.append(new DecimalFormat("#.##").format(currentState.getCurrentRatio()));
      }
      else
      {
         builder.append("NaN");
      }
      builder.append(", min ratio: ");
      builder.append(currentState.getMinRatio());
      builder.append(", max ratio: ");
      builder.append(currentState.getMaxRatio());
      builder.append("\n");

      if (currentState.getTotalSpace() != 0)
      {
         // left tenants
         builder.append(currentState.getTenantsForStarting());
         builder.append(" tenants left before new server starting");
         builder.append("\n");
         builder.append(currentState.getTenantsForStopping());
         builder.append(" tenants left before server stopping");
         builder.append("\n");
      }

      return builder.toString();
   }

   @Override
   public FreeSpaceRatioAutoscalingState getCurrentState() throws CloudAdminException
   {
      FreeSpaceRatioAutoscalingState result = new FreeSpaceRatioAutoscalingState();

      int totalSpace = serverConfigurationManager.getTotalSpaceSize();
      int tenantNumber = applicationServerManager.getActiveTenantSize(true);

      double minRatio =
         cloudAdminConfiguration
            .getDouble(ApplicationRecoveryConfiguration.CLOUD_ADMIN_APPLICATION_AUTOSCALING_MIN_FREESPACE_RATIO);
      double maxRatio =
         cloudAdminConfiguration
            .getDouble(ApplicationRecoveryConfiguration.CLOUD_ADMIN_APPLICATION_AUTOSCALING_MAX_FREESPACE_RATIO);
      if (minRatio > maxRatio)
      {
         throw new CloudAdminException("Min free space ratio must be less or equals than max free space ratio.");
      }
      double currentRatio;
      if (totalSpace == 0)
      {
         currentRatio = Double.NaN;
      }
      else
      {
         currentRatio = 1 - (double)tenantNumber / (double)totalSpace;
      }

      result.setTotalSpace(totalSpace);
      result.setTenantNumber(tenantNumber);
      result.setMinRatio(minRatio);
      result.setCurrentRatio(currentRatio);
      result.setMaxRatio(maxRatio);

      int currentServersCount = applicationServerManager.getApplicationServerStatusMap().size();
      result.setCurrentServersCount(currentServersCount);
      result.setExpectedServersCount(currentServersCount);

      if (totalSpace > 0)
      {
         LOG.info(
            "Current free space ratio {} ({} / {}), min ratio {}, max ratio {}",
            new String[]{String.valueOf(currentRatio), String.valueOf(tenantNumber), String.valueOf(totalSpace),
               String.valueOf(minRatio), String.valueOf(maxRatio)});
         if (currentRatio < minRatio)
         {
            int tenantsPerServer =
               applicationTypeManager.getConfiguration(
                  cloudAdminConfiguration
                     .getString(ApplicationRecoveryConfiguration.CLOUD_ADMIN_APPLICATION_DEFAULT_TYPE)).getInt(
                  ApplicationServerType.PROPERTY_REGISTER_CONFIG_PARAM_PREFIX + "."
                     + ApplicationServerConfiguration.MAX_TENANTS_NUM_PARAMETER);
            if (tenantsPerServer <= 0)
            {
               throw new CloudAdminException("Max tenants num in default application server type must be more than 0.");
            }

            int expectedTotalSpace = totalSpace;
            int expectedServersCount = currentServersCount;
            double expectedRatio = 1 - (double)tenantNumber / (double)expectedTotalSpace;
            while (expectedRatio < minRatio)
            {
               expectedTotalSpace += tenantsPerServer;
               expectedServersCount++;
               expectedRatio = 1 - (double)tenantNumber / (double)expectedTotalSpace;
            }
            result.setExpectedRatio(expectedRatio);
            result.setExpectedServersCount(expectedServersCount);
            result.setTenantsForStarting(0);
         }
         else
         {
            int tenantsForMinRatio = (int)Math.ceil((1 - minRatio) * totalSpace);
            int tenantsForStarting = tenantsForMinRatio - tenantNumber;
            result.setTenantsForStarting(tenantsForStarting);
         }

         if (currentRatio > maxRatio)
         {
            List<ApplicationServerStatus> servers = getSortedByLoadApplicationServers();
            Iterator<ApplicationServerStatus> serversIterator = servers.iterator();

            int expectedTotalSpace = totalSpace;
            int expectedServersCount = currentServersCount;
            double expectedRatio = 1 - (double)tenantNumber / (double)expectedTotalSpace;
            while (expectedRatio > maxRatio && serversIterator.hasNext())
            {
               ApplicationServerStatus nextServerStatus = serversIterator.next();

               expectedTotalSpace -=
                  serverConfigurationManager.getConfiguration(nextServerStatus.getAlias()).getInt(
                     ApplicationServerConfiguration.MAX_TENANTS_NUM_PARAMETER);
               expectedRatio = 1 - (double)tenantNumber / (double)expectedTotalSpace;
               if (expectedRatio >= minRatio)
               {
                  expectedServersCount--;
               }
            }

            result.setExpectedRatio(expectedRatio);
            result.setExpectedServersCount(expectedServersCount);
            result.setTenantsForStopping(0);
            result.setSortedServers(servers);
         }
         else
         {
            int tenantsForMaxRatio = (int)Math.floor((1 - maxRatio) * totalSpace);
            int tenantsForStopping = tenantNumber - tenantsForMaxRatio;
            result.setTenantsForStopping(tenantsForStopping);
         }
      }
      else
      {
         result.setExpectedRatio(Double.NaN);
      }

      return result;
   }

   private List<ApplicationServerStatus> getSortedByLoadApplicationServers() throws CloudAdminException
   {
      List<ApplicationServerStatus> result = new ArrayList<ApplicationServerStatus>();
      HashSet<String> serversWithCreations = new HashSet<String>();
      for (String tenant : tenantInfoDataManager.getNames(TenantInfoFieldName.PROPERTY_STATE,
         TenantState.CREATION.toString()))
      {
         serversWithCreations.add(tenantInfoDataManager.getValue(tenant, TenantInfoFieldName.PROPERTY_APSERVER_ALIAS));
      }
      for (ApplicationServerStatus server : applicationServerManager.getApplicationServerStatusMap().values())
         if (!serversWithCreations.contains(server.getAlias()))
            result.add(server);
      try
      {
         Collections.sort(result, new Comparator<ApplicationServerStatus>()
         {
            private final Map<String, Integer> activeTenantNumberMap = new HashMap<String, Integer>();

            /**
             * Compare in reverse order.
             */
            @Override
            public int compare(ApplicationServerStatus o1, ApplicationServerStatus o2)
            {
               try
               {
                  return getLoad(o1.getAlias()).compareTo(getLoad(o2.getAlias()));
               }
               catch (TenantDataManagerException e)
               {
                  // Runtime exception wrapper to stop sorting of array and throwing real exception
                  throw new RuntimeException(e);
               }
            }

            /**
             * 
             * @param applicationServerAlias
             * @return - get load value from cache ore calculate wit
             *         applicationServerManager
             * @throws org.exoplatform.cloudmanagement.admin.dao.TenantDataManagerException
             */
            private Integer getLoad(String applicationServerAlias) throws TenantDataManagerException
            {
               Integer result = activeTenantNumberMap.get(applicationServerAlias);
               if (result == null)
               {
                  result = applicationServerManager.getActiveTenantSize(applicationServerAlias);
                  activeTenantNumberMap.put(applicationServerAlias, result);
               }
               return result;
            }

         });
         return result;
      }
      catch (RuntimeException e)
      {
         throw new CloudAdminException("Autoscaling algorithm couldn't calculate current state of cloud", e);
      }

   }

}
