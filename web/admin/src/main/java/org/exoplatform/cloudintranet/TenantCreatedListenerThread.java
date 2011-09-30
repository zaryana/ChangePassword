package org.exoplatform.cloudintranet;

import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.MailSender;
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

   private String tName;

   private String email;

   private String firstName;

   private String companyName;

   private String lastName;

   private String phone;

   private String password;

   private CloudInfoHolder cloudInfoHolder;

   private int interval = 15000;

   private CloudAdminConfiguration cloudAdminConfiguration;
   
   private static final Logger LOG = LoggerFactory.getLogger(TenantCreatedListenerThread.class);

   public TenantCreatedListenerThread(String userMail, String firstName, String lastName, String companyName, String phone,
      String password, CloudInfoHolder cloudInfoHolder, CloudAdminConfiguration cloudAdminConfiguration)
   {

      this.tName = userMail.substring(userMail.indexOf("@") + 1, userMail.indexOf(".", userMail.indexOf("@")));
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
      if (cloudInfoHolder.isTenantExists(tName))
      {

         try
         {
            while (!cloudInfoHolder.getTenantStatus(tName).getState().equals(TenantState.ONLINE))
            {
               Thread.sleep(interval);
            }
            CloudIntranetUtils utils = new CloudIntranetUtils(cloudAdminConfiguration);
            MailSender mailSender = new MailSender(cloudAdminConfiguration);
            String password = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
            utils.storeUser(email, firstName, lastName, password);
            utils.storeRoot(tName, email, firstName, lastName, password);
            Map<String, String> props = new HashMap<String, String>();
            props.put("tenant.masterhost", cloudAdminConfiguration.getMasterHost());
            props.put("tenant.name", tName);
            props.put("user.mail", email);
            //utils.sendIntranetCreatedEmails(email, props);
         }
         catch (TenantQueueException e)
         {
            LOG.error(e.getMessage());
         }
         catch (InterruptedException e)
         {
            LOG.error(e.getMessage());
         }
         catch (CloudAdminException e)
         {
            LOG.error(e.getMessage());
         }
      }
      else
      {
         LOG.error("Unable to find tenant '" + tName + "' in creation queue.");
      }

   }

}
