package com.exoplatform.cloudworkspaces.multitenancy.plugin;

import org.exoplatform.cloudmanagement.multitenancy.BaseTenantCreationPlugin;
import org.exoplatform.cloudmanagement.multitenancy.TenantCreationException;
import org.exoplatform.cloudmanagement.status.TransientTenantStatus;
import org.exoplatform.container.configuration.ConfigurationException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.instrument.TenantAccessTimeStatisticCollector;

public class WorkspacesTenantCreationPlugin extends BaseTenantCreationPlugin
{
   
   public WorkspacesTenantCreationPlugin(InitParams params) throws ConfigurationException{
     super(params);      
   }

   @Override
   public void create(TransientTenantStatus status) throws TenantCreationException
   {
      TenantAccessTimeStatisticCollector.getInstance().setAccessTime(status.getTenantName(), System.currentTimeMillis());
   }
}
