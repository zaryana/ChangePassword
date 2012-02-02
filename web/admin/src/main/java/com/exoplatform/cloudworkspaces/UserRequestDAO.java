package com.exoplatform.cloudworkspaces;

import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class UserRequestDAO
{
   CloudAdminConfiguration cloudAdminConfiguration;
   
   private static final Logger LOG = LoggerFactory.getLogger(UserRequestDAO.class);
   
   public UserRequestDAO(CloudAdminConfiguration cloudAdminConfiguration){
      this.cloudAdminConfiguration = cloudAdminConfiguration; 
   }
   
   public void put(UserRequest req) throws CloudAdminException{
      
      if (searchByEmail(req.getUserEmail()) != null)
         throw new CloudAdminException("Request to create or join a Cloud Workspace from " + req.getUserEmail() + " already submitted, it is on the processing currently. Wait for the creation will be done or use another email.");
      
      String folderName = getRegistrationWaitingFolder();
      File folder = new File(folderName);
      if (!folder.exists())
         folder.mkdir();
      File propertyFile = new File(folderName + req.getTenantName() + "_"+ System.currentTimeMillis() + ".properties");
      
      Properties properties = new Properties();
      properties.setProperty("action", req.getState().toString());
      properties.setProperty("tenant", req.getTenantName());
      properties.setProperty("user-mail", req.getUserEmail());
      properties.setProperty("first-name", req.getFirstName());
      properties.setProperty("last-name", req.getLastName());
      properties.setProperty("company-name", req.getCompanyName());
      properties.setProperty("phone", req.getPhone());
      properties.setProperty("password", req.getPassword());
      properties.setProperty("confirmation-id", req.getConfirmationId());
      properties.setProperty("isadministrator", Boolean.toString(req.isAdministrator()));
      
      try
      {
         propertyFile.createNewFile();
         properties.store(new FileOutputStream(propertyFile), "");
      }
      catch (Exception e)
      {
         LOG.error(e.getMessage(), e);
         throw new CloudAdminException("A problem happened during processsing this request. It was reported to developers. Please, try again later.");
      }
   }
   
   
   public void delete(UserRequest req) throws CloudAdminException{
      String folderName = getRegistrationWaitingFolder();
      File folder = new File(folderName);
      if (!folder.exists())
         return;
      File propertyFile = new File(folderName + req.getFileName());
      try
      {
         propertyFile.delete();
      }
      catch (Exception e)
      {
         LOG.error(e.getMessage(), e);
         throw new CloudAdminException("A problem happened during processsing this request. It was reported to developers. Please, try again later.");
      }
   }
   
   
   public List<UserRequest> search(String tNameFilter, RequestState stateFilter) throws CloudAdminException{
      
      List<UserRequest> result = new ArrayList<UserRequest>();
      String folderName = getRegistrationWaitingFolder();
      File[] list = new File(folderName).listFiles();
      if (list == null)
        return Collections.emptyList();
      for (File one : list)
      {
         if (tNameFilter == null || one.getName().startsWith(tNameFilter + "_"))
         {
            try
            {
               FileInputStream io = new FileInputStream(one);
               Properties newprops = new Properties();
               newprops.load(io);
               io.close();
               if (stateFilter == null || newprops.getProperty("action").equalsIgnoreCase(stateFilter.toString()))
               {
                  UserRequest req = new UserRequest(one.getName(), newprops.getProperty("tenant"), 
                     newprops.getProperty("user-mail"), 
                     newprops.getProperty("first-name"),
                     newprops.getProperty("last-name"),
                     newprops.getProperty("company-name"),newprops.getProperty("phone"), 
                     newprops.getProperty("password"), 
                     newprops.getProperty("confirmation-id"), 
                     Boolean.parseBoolean(newprops.getProperty("isadministrator")),
                     RequestState.valueOf(newprops.getProperty("action")));
                  result.add(req);
               }
            }
            catch (IOException e)
            {
              LOG.error(e.getMessage(), e);
            }
         }
      }
      return result;
   }

   public UserRequest searchByEmail(String email) throws CloudAdminException
   {
      String folderName = getRegistrationWaitingFolder();
      File folder = new File(folderName);
      if (!folder.exists())
         return null;

      File[] list = folder.listFiles();
      for (File one : list)
      {
         try
         {
            FileInputStream io = new FileInputStream(one);
            Properties newprops = new Properties();
            newprops.load(io);
            io.close();
            if (newprops.getProperty("user-mail").equalsIgnoreCase(email))
            {
               return new UserRequest(one.getName(), newprops.getProperty("tenant"), 
                                      newprops.getProperty("user-mail"), 
                                      newprops.getProperty("first-name"),
                                      newprops.getProperty("last-name"),
                                      newprops.getProperty("company-name"),newprops.getProperty("phone"), 
                                      newprops.getProperty("password"), 
                                      newprops.getProperty("confirmation-id"), 
                                      Boolean.parseBoolean(newprops.getProperty("isadministrator")),
                                      RequestState.valueOf(newprops.getProperty("action")));
            }
         }
         catch (IOException e)
         {
            String msg = "Tenant queuing error : failed to read property file " + one.getName();
            throw new CloudAdminException(
               "A problem happened during processing request . It was reported to developers. Please, try again later.");
         }
      }
      return null;
   }
   
   public UserRequest searchByFilename(String filename) throws CloudAdminException
   {
      String folderName = getRegistrationWaitingFolder();
      File folder = new File(folderName);
      if (!folder.exists())
         return null;
      File propertyFile = new File(folderName + filename);

      try
      {
         FileInputStream io = new FileInputStream(propertyFile);
         Properties newprops = new Properties();
         newprops.load(io);
         io.close();
         return new UserRequest(propertyFile.getName(), newprops.getProperty("tenant"),
            newprops.getProperty("user-mail"), newprops.getProperty("first-name"), newprops.getProperty("last-name"),
            newprops.getProperty("company-name"), newprops.getProperty("phone"), newprops.getProperty("password"),
            newprops.getProperty("confirmation-id"), Boolean.parseBoolean(newprops.getProperty("isadministrator")),
            RequestState.valueOf(newprops.getProperty("action")));
      }
      catch (IOException e)
      {
         String msg = "Tenant queuing error : failed to read property file " + propertyFile.getName();
         throw new CloudAdminException(
            "A problem happened during processing request . It was reported to developers. Please, try again later.");
      }
   }
   
   
   
   private String getRegistrationWaitingFolder() throws CloudAdminException{
      String folder = cloudAdminConfiguration.getProperty("cloud.admin.tenant.waiting.dir", null);
      if (folder == null){
         LOG.error("Registration waitind dir is not defined in the admin configuration");
         throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");
      }
      return folder;
   }
}
