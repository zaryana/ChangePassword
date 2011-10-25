package com.exoplatform.cloudintranet;

import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration;
import org.exoplatform.cloudmanagement.admin.queue.TenantQueueException;
import org.exoplatform.cloudmanagement.admin.status.CloudInfoHolder;
import org.exoplatform.cloudmanagement.status.TenantState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TenantCreatedListenerThread implements Runnable
{

   private static final Logger LOG = LoggerFactory.getLogger(TenantCreatedListenerThread.class);


   private String tName;

   private String email;

   private String firstName;

   private String companyName;

   private String lastName;

   private String phone;

   private String password;

   private CloudInfoHolder cloudInfoHolder;

   private int interval = 15000;
   
   private int limit = 1440; //360 minutes; 1440 times by 15 sec;

   private CloudAdminConfiguration cloudAdminConfiguration;
   

   public TenantCreatedListenerThread(String tName, String userMail, String firstName, String lastName, String companyName,
      String phone, String password, CloudInfoHolder cloudInfoHolder, CloudAdminConfiguration cloudAdminConfiguration)
   {
      this.tName = tName;
      this.email = userMail;
      this.firstName = firstName;
      this.lastName = lastName;
      this.companyName = companyName;
      this.phone = phone;
      this.password = password;
      this.cloudInfoHolder = cloudInfoHolder;
      this.cloudAdminConfiguration = cloudAdminConfiguration;

   }

   @Override
   public void run()
   {
      CloudIntranetUtils utils = new CloudIntranetUtils(cloudAdminConfiguration);

      if (cloudInfoHolder.isTenantExists(tName))
      {
         int count = 0;
         try
         {
            while (!cloudInfoHolder.getTenantStatus(tName).getState().equals(TenantState.ONLINE))
            {
               if (count > limit)
                  throw new CloudAdminException("Tenant creation timeout reached");
               Thread.sleep(interval);
               count++;
            }
            
            String root_password = UUID.randomUUID().toString().replace("-", "").substring(0, 9);
            utils.storeUser(tName, email, firstName, lastName, password);
            utils.storeRoot(tName, email, "root", "root", root_password);
            Map<String, String> props = new HashMap<String, String>();
            props.put("tenant.masterhost", cloudAdminConfiguration.getMasterHost());
            props.put("tenant.repository.name", tName);
            props.put("user.mail", email);
            props.put("root.password", root_password);
            utils.sendIntranetCreatedEmails(email, props);
         }
         catch (TenantQueueException e)
         {
            LOG.error(e.getMessage());
            utils.sendAdminErrorEmail("Unable to finish tenant '" + tName + "' creation", e);
         }
         catch (InterruptedException e)
         {
            LOG.error(e.getMessage());
            utils.sendAdminErrorEmail("Unable to finish tenant '" + tName + "' creation", e);
         }
         catch (CloudAdminException e)
         {
            LOG.error(e.getMessage());
            utils.sendAdminErrorEmail("Unable to finish tenant '" + tName + "' creation", e);
         }
      }
      else
      {
         LOG.error("Unable to find tenant '" + tName + "' in creation queue.");
      }

   }

}
