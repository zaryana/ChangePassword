package com.exoplatform.cloudworkspaces.listener;

import com.exoplatform.cloudworkspaces.RequestState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserLimitListenerThread implements Runnable
{

   private static final Logger LOG = LoggerFactory.getLogger(UserLimitListenerThread.class);
   
   private long modificationTimeOnStartup;
   
   @Override
   public void run()
   {
      try
      {
         String maxUsersConfigurationFileName = System.getProperty("cloud.admin.userlimit");
         if (maxUsersConfigurationFileName == null){
            LOG.warn("Maxusers configuration file not found, limit listener shutdown");
            return;
         }
         modificationTimeOnStartup = new File(maxUsersConfigurationFileName).lastModified();
         
         while (true){
            Thread.sleep(18000); //3 min
            Long modificationTime = new File(maxUsersConfigurationFileName).lastModified();
            if (modificationTime > modificationTimeOnStartup){
               LOG.info("Limit file changed, trying to join users waiting for limit.");
               modificationTimeOnStartup = modificationTime;
               StringBuilder strUrl = new StringBuilder();
               strUrl.append("http://");
               strUrl.append(System.getProperty("tenant.masterhost"));
               strUrl.append("/rest/cloud-admin/public-tenant-service/autojoin/" + RequestState.WAITING_LIMIT.toString());
               URL url = new URL(strUrl.toString());
               HttpURLConnection connection = (HttpURLConnection)url.openConnection();
               connection.setRequestMethod("GET");
               if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
               {
                  LOG.error("Unable to autojoin user. HTTP response: " + connection.getResponseCode());
               }
            }
         }
      }
      catch (Exception e)
      {
         LOG.error("Exception in limit listener", e);
      }
   }

}
