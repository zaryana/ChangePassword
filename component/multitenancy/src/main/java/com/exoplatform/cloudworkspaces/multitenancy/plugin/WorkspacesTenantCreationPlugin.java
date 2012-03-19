package com.exoplatform.cloudworkspaces.multitenancy.plugin;

import org.exoplatform.cloudmanagement.multitenancy.TenantCreationException;
import org.exoplatform.cloudmanagement.multitenancy.plugin.BaseTenantCreationPlugin;
import org.exoplatform.cloudmanagement.status.TenantInfo;
import org.exoplatform.container.configuration.ConfigurationException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.statistic.TenantAccessTimeStatisticCollector;

public class WorkspacesTenantCreationPlugin extends BaseTenantCreationPlugin
{
   
   public WorkspacesTenantCreationPlugin(InitParams params) throws ConfigurationException{
     super(params);      
   }

   @Override
   public void create(TenantInfo status) throws TenantCreationException
   {
      TenantAccessTimeStatisticCollector.getInstance().setAccessTime(status.getTenantName(), System.currentTimeMillis());
   }
}
