package com.exoplatform.cloudworkspaces;

import com.exoplatform.cloud.admin.CloudAdminException;
import org.apache.commons.configuration.Configuration;
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

public class UserRequestDAO {
  Configuration               cloudAdminConfiguration;

  PasswordCipher              passwordCipher;

  private static final Logger LOG = LoggerFactory.getLogger(UserRequestDAO.class);

  public UserRequestDAO(Configuration cloudAdminConfiguration, PasswordCipher passwordCipher) {
    this.cloudAdminConfiguration = cloudAdminConfiguration;
    this.passwordCipher = passwordCipher;
  }

  public void put(UserRequest req) throws CloudAdminException {

    if (searchByEmail(req.getUserEmail()) != null)
      throw new CloudAdminException("Request to create or join a Cloud Workspace from "
          + req.getUserEmail()
          + " already submitted, it is on the processing currently. Wait for the creation will be done or use another email.");

    String folderName = getRegistrationWaitingFolder();
    File folder = new File(folderName);
    if (!folder.exists())
      folder.mkdir();
    File propertyFile = new File(folderName + req.getTenantName() + "_"
        + System.currentTimeMillis() + ".properties");

    Properties properties = new Properties();
    properties.setProperty("action", req.getState().toString());
    properties.setProperty("tenant", req.getTenantName());
    properties.setProperty("user-mail", req.getUserEmail());
    properties.setProperty("first-name", req.getFirstName());
    properties.setProperty("last-name", req.getLastName());
    properties.setProperty("company-name", req.getCompanyName());
    properties.setProperty("phone", req.getPhone());
    if (System.getProperty("cloud.admin.crypt.registration.password") != null
      && System.getProperty("cloud.admin.crypt.registration.password").equals("true")) {
      properties.setProperty("password", passwordCipher.encrypt(req.getPassword()));
    } else {
      properties.setProperty("password", req.getPassword());
    }
    properties.setProperty("confirmation-id", req.getConfirmationId());
    properties.setProperty("isadministrator", Boolean.toString(req.isAdministrator()));

    try {
      propertyFile.createNewFile();
      properties.store(new FileOutputStream(propertyFile), "");
    } catch (IOException e) {
      String msg = "Tenant queuing error: failed to create queue property file.";
      LOG.error(msg, e);
      throw new CloudAdminException("A problem happened during processing this request. It was reported to developers. Please, try again later.");
    }

  }

  public void delete(UserRequest req) throws CloudAdminException {
    String folderName = getRegistrationWaitingFolder();
    File folder = new File(folderName);
    if (!folder.exists())
      return;
    File propertyFile = new File(folderName + req.getFileName());
    propertyFile.delete();
  }

  public List<UserRequest> search(String tNameFilter, RequestState stateFilter) throws CloudAdminException {
    String password;
    List<UserRequest> result = new ArrayList<UserRequest>();
    String folderName = getRegistrationWaitingFolder();
    File[] list = new File(folderName).listFiles();
    if (list == null)
      return Collections.emptyList();
    for (File one : list) {
      if (tNameFilter == null || one.getName().startsWith(tNameFilter + "_")) {
        try {
          FileInputStream io = new FileInputStream(one);
          Properties newprops = new Properties();
          newprops.load(io);
          io.close();
          if (passwordCipher != null)
            password = passwordCipher.decrypt(newprops.getProperty("password"));
          else
            password = newprops.getProperty("password");
          if (stateFilter == null
              || newprops.getProperty("action").equalsIgnoreCase(stateFilter.toString())) {

            UserRequest req = new UserRequest(one.getName(),
                                              newprops.getProperty("tenant"),
                                              newprops.getProperty("user-mail"),
                                              newprops.getProperty("first-name"),
                                              newprops.getProperty("last-name"),
                                              newprops.getProperty("company-name"),
                                              newprops.getProperty("phone"),
                                              password,
                                              newprops.getProperty("confirmation-id"),
                                              Boolean.parseBoolean(newprops.getProperty("isadministrator")),
                                              RequestState.valueOf(newprops.getProperty("action")));
            result.add(req);
          }
        } catch (IOException e) {
          String msg = "Tenant queuing error: failed to read property file " + one.getName();
          LOG.error(msg, e);
          throw new CloudAdminException("A problem happened during processing this request. It was reported to developers. Please, try again later.");
        }
      }
    }
    return result;
  }

  public UserRequest searchByEmail(String email) throws CloudAdminException {
    String folderName = getRegistrationWaitingFolder();
    String password;
    File folder = new File(folderName);
    if (!folder.exists())
      return null;

    File[] list = folder.listFiles();
    if (list == null)
    {
      String msg = "Tenant queuing error: failed to read list files of " + folder.getName();
      LOG.error(msg);
      throw new CloudAdminException("A problem happened during processing request . It was reported to developers. Please, try again later.");
    }
    for (File one : list) {
      try {
        FileInputStream io = new FileInputStream(one);
        Properties newprops = new Properties();
        newprops.load(io);
        io.close();
        if (passwordCipher != null)
          password = passwordCipher.decrypt(newprops.getProperty("password"));
        else
          password = newprops.getProperty("password");
        if (newprops.getProperty("user-mail").equalsIgnoreCase(email)) {
          return new UserRequest(one.getName(),
                                 newprops.getProperty("tenant"),
                                 newprops.getProperty("user-mail"),
                                 newprops.getProperty("first-name"),
                                 newprops.getProperty("last-name"),
                                 newprops.getProperty("company-name"),
                                 newprops.getProperty("phone"),
                                 password,
                                 newprops.getProperty("confirmation-id"),
                                 Boolean.parseBoolean(newprops.getProperty("isadministrator")),
                                 RequestState.valueOf(newprops.getProperty("action")));
        }
      } catch (IOException e) {
        String msg = "Tenant queuing error: failed to read property file " + one.getName();
        LOG.error(msg, e);
        throw new CloudAdminException("A problem happened during processing request . It was reported to developers. Please, try again later.");
      }
    }
    return null;
  }

  public UserRequest searchByFilename(String filename) throws CloudAdminException {
    String folderName = getRegistrationWaitingFolder();
    String password;
    File folder = new File(folderName);
    if (!folder.exists())
      return null;
    File propertyFile = new File(folderName + filename);

    try {
      FileInputStream io = new FileInputStream(propertyFile);
      Properties newprops = new Properties();
      newprops.load(io);
      io.close();
      if (passwordCipher != null)
        password = passwordCipher.decrypt(newprops.getProperty("password"));
      else
        password = newprops.getProperty("password");
      return new UserRequest(propertyFile.getName(),
                             newprops.getProperty("tenant"),
                             newprops.getProperty("user-mail"),
                             newprops.getProperty("first-name"),
                             newprops.getProperty("last-name"),
                             newprops.getProperty("company-name"),
                             newprops.getProperty("phone"),
                             password,
                             newprops.getProperty("confirmation-id"),
                             Boolean.parseBoolean(newprops.getProperty("isadministrator")),
                             RequestState.valueOf(newprops.getProperty("action")));
    } catch (IOException e) {
      String msg = "Tenant queuing error: failed to read property file " + propertyFile.getName();
      LOG.error(msg, e);
      throw new CloudAdminException("A problem happened during processing request . It was reported to developers. Please, try again later.");
    }
  }

  private String getRegistrationWaitingFolder() throws CloudAdminException {
    String folder = cloudAdminConfiguration.getString("cloud.admin.tenant.waiting.dir", null);
    if (folder == null) {
      LOG.error("Registration waiting dir is not defined in the admin configuration");
      throw new CloudAdminException("An problem happened during processing this request. It was reported to developers. Please, try again later.");
    }
    return folder;
  }
}
