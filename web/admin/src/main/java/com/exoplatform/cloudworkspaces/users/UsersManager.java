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

import com.exoplatform.cloud.admin.CloudAdminException;
import com.exoplatform.cloud.admin.configuration.TenantInfoFieldName;
import com.exoplatform.cloud.admin.dao.TenantInfoDataManager;
import com.exoplatform.cloud.admin.util.AdminConfigurationUtil;
import com.exoplatform.cloud.status.TenantState;
import com.exoplatform.cloudworkspaces.CloudIntranetUtils;
import com.exoplatform.cloudworkspaces.NotificationMailSender;
import com.exoplatform.cloudworkspaces.ReferencesManager;
import com.exoplatform.cloudworkspaces.RequestState;
import com.exoplatform.cloudworkspaces.UserAlreadyExistsException;
import com.exoplatform.cloudworkspaces.UserRequest;
import com.exoplatform.cloudworkspaces.UserRequestDAO;
import com.exoplatform.cloudworkspaces.http.WorkspacesOrganizationRequestPerformer;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UsersManager {

  private static final Logger                          LOG = LoggerFactory.getLogger(UsersManager.class);

  private final Configuration                          cloudAdminConfiguration;

  private final CloudIntranetUtils                     utils;

  private final WorkspacesOrganizationRequestPerformer workspacesOrganizationRequestPerformer;

  private final TenantInfoDataManager                  tenantInfoDataManager;

  private final NotificationMailSender                 notificationMailSender;

  private final UserLimitsStorage                      userLimitsStorage;

  private final UserRequestDAO                         userRequestDao;

  private final ReferencesManager                      referencesManager;

  public UsersManager(Configuration cloudAdminConfiguration,
                      CloudIntranetUtils utils,
                      WorkspacesOrganizationRequestPerformer workspacesOrganizationRequestPerformer,
                      TenantInfoDataManager tenantInfoDataManager,
                      NotificationMailSender notificationMailSender,
                      UserLimitsStorage userLimitsStorage,
                      UserRequestDAO userRequestDao,
                      ReferencesManager referencesManager) {
    this.cloudAdminConfiguration = cloudAdminConfiguration;
    this.utils = utils;
    this.workspacesOrganizationRequestPerformer = workspacesOrganizationRequestPerformer;
    this.tenantInfoDataManager = tenantInfoDataManager;
    this.notificationMailSender = notificationMailSender;
    this.userLimitsStorage = userLimitsStorage;
    this.userRequestDao = userRequestDao;
    this.referencesManager = referencesManager;
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
      joinWaitingLimitUsers(tName);
      break;
    }
  }

  public synchronized void joinWaitingJoinAdministrators(String tName) throws CloudAdminException {
    for (UserRequest user : userRequestDao.search(tName, RequestState.WAITING_JOIN)) {

      if (user.isAdministrator()) {
        String tenant = user.getTenantName();
        String userMail = user.getUserEmail();
        String fName = user.getFirstName();
        String lName = user.getLastName();
        String username = utils.email2userMailInfo(userMail).getUsername();

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
          // notificationMailSender.sendIntranetCreatedEmail(userMail, props);
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
        String username = utils.email2userMailInfo(userMail).getUsername();

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
          // notificationMailSender.sendUserJoinedEmails(tenant, fName,
          // userMail, props);
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
        } catch (UsersFormNotFilledException e) {
          Map<String, String> formProps = new HashMap<String, String>();
          formProps.put("tenant.masterhost",
                        AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration));
          formProps.put("tenant.repository.name", tenant);
          formProps.put("user.mail", userMail);
          formProps.put("rfid", referencesManager.putEmail(userMail, UUID.randomUUID().toString()));
          LOG.info("Sending join letter to " + userMail + " - his tenant is raised user limit.");
          notificationMailSender.sendOkToJoinEmail(userMail, formProps);
          userRequestDao.delete(user);
        }
      }
    }
  }

  public synchronized void joinWaitingLimitUsers(String tName) throws CloudAdminException {
    for (UserRequest user : userRequestDao.search(tName, RequestState.WAITING_LIMIT)) {
      String tenant = user.getTenantName();
      String userMail = user.getUserEmail();
      String fName = user.getFirstName();
      String lName = user.getLastName();
      String username = utils.email2userMailInfo(userMail).getUsername();

      Map<String, String> props = new HashMap<String, String>();
      props.put("tenant.masterhost", AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration));
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
        // notificationMailSender.sendUserJoinedEmails(tenant, fName, userMail,
        // props);
        userRequestDao.delete(user);
      } catch (UsersLimitExceedException e) {
        // do nothing this user already has status WAITING_LIMIT
      } catch (UsersFormNotFilledException e) {
        Map<String, String> formProps = new HashMap<String, String>();
        formProps.put("tenant.masterhost",
                      AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration));
        formProps.put("tenant.repository.name", tenant);
        formProps.put("user.mail", userMail);
        formProps.put("rfid", referencesManager.putEmail(userMail, UUID.randomUUID().toString()));
        LOG.info("Sending join letter to " + userMail + " - his tenant is raised user limit.");
        notificationMailSender.sendOkToJoinEmail(userMail, formProps);
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
    String username = utils.email2userMailInfo(userMail).getUsername();

    try {
      if (!workspacesOrganizationRequestPerformer.isNewUserAllowed(tenant, username)) {
        throw new UsersLimitExceedException("Not enough space for this user in tenant");
      }

      if (user.getPassword().isEmpty()) {
        throw new UsersFormNotFilledException("User's form not filled yet.");
      }
      LOG.info("Joining {} {} to tenant {} from queue.", new Object[] {
          (user.isAdministrator()) ? "administrator" : "user", userMail, tenant });
      workspacesOrganizationRequestPerformer.storeUser(tenant,
                                                       username,
                                                       userMail,
                                                       fName,
                                                       lName,
                                                       user.getPassword(),
                                                       user.isAdministrator());
    } catch (UserAlreadyExistsException ex) {
      LOG.warn((user.isAdministrator() ? "Administrator " : "User ") + userMail
          + " is already exists, deleting from waiting queue.");
    }
  }

}
