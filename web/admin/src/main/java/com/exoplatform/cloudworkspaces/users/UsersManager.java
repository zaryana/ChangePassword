/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package com.exoplatform.cloudworkspaces.users;

import com.exoplatform.cloudworkspaces.NotificationMailSender;
import com.exoplatform.cloudworkspaces.ReferencesManager;
import com.exoplatform.cloudworkspaces.RequestState;
import com.exoplatform.cloudworkspaces.UserAlreadyExistsException;
import com.exoplatform.cloudworkspaces.UserRequest;
import com.exoplatform.cloudworkspaces.UserRequestDAO;
import com.exoplatform.cloudworkspaces.http.WorkspacesOrganizationRequestPerformer;

import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.configuration.TenantInfoFieldName;
import org.exoplatform.cloudmanagement.admin.dao.TenantInfoDataManager;
import org.exoplatform.cloudmanagement.admin.util.AdminConfigurationUtil;
import org.exoplatform.cloudmanagement.status.TenantState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UsersManager {

  private static final Logger                          LOG = LoggerFactory.getLogger(UsersManager.class);

  private final Configuration                          cloudAdminConfiguration;

  private final WorkspacesOrganizationRequestPerformer workspacesOrganizationRequestPerformer;

  private final TenantInfoDataManager                  tenantInfoDataManager;

  private final NotificationMailSender                 notificationMailSender;

  private final UserLimitsStorage                      userLimitsStorage;

  private final UserRequestDAO                         userRequestDao;

  private ReferencesManager                            referencesManager;

  public UsersManager(Configuration cloudAdminConfiguration,
                      WorkspacesOrganizationRequestPerformer workspacesOrganizationRequestPerformer,
                      TenantInfoDataManager tenantInfoDataManager,
                      NotificationMailSender notificationMailSender,
                      UserLimitsStorage userLimitsStorage,
                      UserRequestDAO userRequestDao,
                      ReferencesManager referencesManager) {
    this.cloudAdminConfiguration = cloudAdminConfiguration;
    this.workspacesOrganizationRequestPerformer = workspacesOrganizationRequestPerformer;
    this.tenantInfoDataManager = tenantInfoDataManager;
    this.notificationMailSender = notificationMailSender;
    this.userLimitsStorage = userLimitsStorage;
    this.userRequestDao = userRequestDao;
  }

  public synchronized void joinAll() throws CloudAdminException {
    for (String tenant : tenantInfoDataManager.getNames()) {
      joinAll(tenant);
    }
  }

  public synchronized void joinAll(String tName) throws CloudAdminException {
    TenantState state = TenantState.valueOf(tenantInfoDataManager.getValue(tName,
                                                                           TenantInfoFieldName.PROPERTY_STATE));
    if (state.equals(TenantState.ONLINE)) {
      joinAll(tName, RequestState.WAITING_LIMIT);
      joinAll(tName, RequestState.WAITING_JOIN);
    }
  }

  public synchronized void joinAll(String tName, RequestState state) throws CloudAdminException {
    if (state.equals(RequestState.WAITING_CREATION))
      throw new CloudAdminException("Given request state does not implies autojoining.");

    switch (state) {
    case WAITING_JOIN:
      joinWaitingJoinAdministrators(tName);
      joinWaitingJoinUsers(tName);
      break;
    case WAITING_LIMIT:
      joinWaitingLimitUsersWithPassword(tName);
      joinWaitingLimitUsersWithoutPassword(tName);
      break;
    }

    if (state.equals(RequestState.WAITING_LIMIT)) {
      joinWaitingJoinAdministrators(tName);
      joinWaitingJoinUsers(tName);
    }
  }

  public synchronized void joinWaitingJoinAdministrators(String tName) throws CloudAdminException {
    for (UserRequest user : userRequestDao.search(tName, RequestState.WAITING_JOIN)) {

      if (user.isAdministrator()) {
        String tenant = user.getTenantName();
        String userMail = user.getUserEmail();
        String fName = user.getFirstName();
        String lName = user.getLastName();
        String username = userMail.substring(0, (userMail.indexOf("@")));

        // Prepare properties for mailing
        Map<String, String> props = new HashMap<String, String>();
        props.put("tenant.masterhost",
                  AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration));
        props.put("tenant.repository.name", tenant);
        props.put("user.mail", userMail);
        props.put("user.name", username);
        props.put("first.name", fName);
        props.put("last.name", lName);

        try {
          joinUser(user);
          notificationMailSender.sendIntranetCreatedEmail(userMail, props);
          userRequestDao.delete(user);
        } catch (UserAlreadyExistsException e) {
          LOG.warn("Administrator " + userMail + " is already exists, deleting from waiting queue.");
          notificationMailSender.sendIntranetCreatedEmail(userMail, props);
          userRequestDao.delete(user);
        }
      }
    }
  }

  public synchronized void joinWaitingJoinUsers(String tName) throws CloudAdminException {
    for (UserRequest user : userRequestDao.search(tName, RequestState.WAITING_JOIN)) {
      if (!user.isAdministrator()) {
        String tenant = user.getTenantName();
        String userMail = user.getUserEmail();
        String fName = user.getFirstName();
        String lName = user.getLastName();
        String username = userMail.substring(0, (userMail.indexOf("@")));

        joinUser(user);

        // Prepare properties for mailing
        Map<String, String> props = new HashMap<String, String>();
        props.put("tenant.masterhost",
                  AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration));
        props.put("tenant.repository.name", tenant);
        props.put("user.mail", userMail);
        props.put("user.name", username);
        props.put("first.name", fName);
        props.put("last.name", lName);

        try {
          joinUser(user);
          notificationMailSender.sendUserJoinedEmails(tenant, fName, userMail, props);
          userRequestDao.delete(user);
        } catch (UserAlreadyExistsException e) {
          LOG.warn("User " + userMail + " is already exists, deleting from waiting queue.");
          notificationMailSender.sendUserJoinedEmails(tenant, fName, userMail, props);
          userRequestDao.delete(user);
        } catch (UsersLimitExceedException e) {
          // Limit reached
          props.put("users.maxallowed",
                    Integer.toString(userLimitsStorage.getMaxUsersForTenant(tenant)));
          notificationMailSender.sendJoinRejectedEmails(tenant, userMail, props);
          // Changing type from WAITING_JOIN to WAITING_LIMIT
          UserRequest two = user.clone();
          two.setState(RequestState.WAITING_LIMIT);
          userRequestDao.delete(user);
          try {
            Thread.sleep(500);
          } catch (InterruptedException ignored) {
          }
          userRequestDao.put(two);
        }
      }
    }
  }

  public synchronized void joinWaitingLimitUsersWithoutPassword(String tName) throws CloudAdminException {
    for (UserRequest user : userRequestDao.search(tName, RequestState.WAITING_LIMIT)) {
      String tenant = user.getTenantName();
      String userMail = user.getUserEmail();
      String username = userMail.substring(0, (userMail.indexOf("@")));

      // Whose who only signed up and stopped on limit - sending them join links
      if (user.getPassword().isEmpty()) {
        Map<String, String> props = new HashMap<String, String>();
        props.put("tenant.masterhost",
                  AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration));
        props.put("tenant.repository.name", tenant);
        props.put("user.mail", userMail);
        props.put("rfid", referencesManager.putEmail(userMail, UUID.randomUUID().toString()));
        if (workspacesOrganizationRequestPerformer.isNewUserAllowed(tenant, username)) {
          LOG.info("Sending join letter to " + userMail + " - his tenant is raised user limit.");
          notificationMailSender.sendOkToJoinEmail(userMail, props);
          userRequestDao.delete(user);
        }
      }
    }
  }

  public synchronized void joinWaitingLimitUsersWithPassword(String tName) throws CloudAdminException {
    for (UserRequest user : userRequestDao.search(tName, RequestState.WAITING_LIMIT)) {
      if (!user.getPassword().isEmpty()) {
        String tenant = user.getTenantName();
        String userMail = user.getUserEmail();
        String fName = user.getFirstName();
        String lName = user.getLastName();
        String username = userMail.substring(0, (userMail.indexOf("@")));

        joinUser(user);

        Map<String, String> props = new HashMap<String, String>();
        props.put("tenant.masterhost",
                  AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration));
        props.put("tenant.repository.name", tenant);
        props.put("user.mail", userMail);
        props.put("user.name", username);
        props.put("first.name", fName);
        props.put("last.name", lName);

        notificationMailSender.sendUserJoinedEmails(tenant, fName, userMail, props);
        userRequestDao.delete(user);
      }
    }
  }

  private void joinUser(UserRequest user) throws CloudAdminException {
    // Checking status
    if (!tenantInfoDataManager.getValue(user.getTenantName(), TenantInfoFieldName.PROPERTY_STATE)
                              .equals(TenantState.ONLINE.toString())) {
      String msg = "Tenant " + user.getTenantName() + " is not online, auto join skipped for user "
          + user.getUserEmail();
      LOG.warn(msg);
      return;
    }
    String tenant = user.getTenantName();
    String userMail = user.getUserEmail();
    String fName = user.getFirstName();
    String lName = user.getLastName();
    String username = userMail.substring(0, (userMail.indexOf("@")));

    try {
      if (!workspacesOrganizationRequestPerformer.isNewUserAllowed(tenant, username)) {
        throw new UsersLimitExceedException("Not enough space for this user in tenant");
      }
      LOG.info("Joining {} {} to tenant {} from queue.", new Object[] {
          (user.isAdministrator()) ? "administrator" : "user", userMail, tenant });
      workspacesOrganizationRequestPerformer.storeUser(tenant,
                                                       userMail,
                                                       fName,
                                                       lName,
                                                       user.getPassword(),
                                                       user.isAdministrator());
    } catch (UserAlreadyExistsException ex) {
      LOG.warn("Administrator " + userMail + " is already exists, deleting from waiting queue.");
    }
  }

}
