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
package com.exoplatform.cloudworkspaces.rest;

import com.exoplatform.cloud.admin.CloudAdminException;
import com.exoplatform.cloud.admin.configuration.TenantInfoFieldName;
import com.exoplatform.cloud.admin.dao.TenantDataManagerException;
import com.exoplatform.cloud.admin.dao.TenantInfoDataManager;
import com.exoplatform.cloud.admin.rest.TenantCreator;
import com.exoplatform.cloud.admin.util.AdminConfigurationUtil;
import com.exoplatform.cloud.status.TenantState;
import com.exoplatform.cloudworkspaces.ChangePasswordManager;
import com.exoplatform.cloudworkspaces.CloudIntranetUtils;
import com.exoplatform.cloudworkspaces.EmailBlacklist;
import com.exoplatform.cloudworkspaces.NotificationMailSender;
import com.exoplatform.cloudworkspaces.ReferencesManager;
import com.exoplatform.cloudworkspaces.RequestState;
import com.exoplatform.cloudworkspaces.TemplateManagement;
import com.exoplatform.cloudworkspaces.UserAlreadyExistsException;
import com.exoplatform.cloudworkspaces.UserMailInfo;
import com.exoplatform.cloudworkspaces.UserRequest;
import com.exoplatform.cloudworkspaces.UserRequestDAO;
import com.exoplatform.cloudworkspaces.http.WorkspacesOrganizationRequestPerformer;
import com.exoplatform.cloudworkspaces.listener.AsyncTenantStarter;
import com.exoplatform.cloudworkspaces.users.UserLimitsStorage;
import com.exoplatform.cloudworkspaces.users.UsersManager;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/cloud-admin/cloudworkspaces/tenant-service")
public class CloudWorkspacesTenantService {

  private static final Logger                    LOG = LoggerFactory.getLogger(CloudWorkspacesTenantService.class);

  private CloudIntranetUtils                     utils;

  private UserRequestDAO                         requestDao;

  private TenantCreator                          tenantCreator;

  private Configuration                          cloudAdminConfiguration;

  private TenantInfoDataManager                  tenantInfoDataManager;

  private WorkspacesOrganizationRequestPerformer workspacesOrganizationRequestPerformer;

  private NotificationMailSender                 notificationMailSender;

  private UserLimitsStorage                      userLimitsStorage;

  private ReferencesManager                      referencesManager;

  private AsyncTenantStarter                     tenantStarter;

  private EmailBlacklist                         emailBlacklist;

  private ChangePasswordManager                  changePasswordManager;

  private UsersManager                           usersManager;

  private TemplateManagement                     templateManagement;

  public CloudWorkspacesTenantService(TenantCreator tenantCreator,
                                      TenantInfoDataManager tenantInfoDataManager,
                                      WorkspacesOrganizationRequestPerformer workspacesOrganizationRequestPerformer,
                                      NotificationMailSender notificationMailSender,
                                      UserLimitsStorage userLimitsStorage,
                                      Configuration cloudAdminConfiguration,
                                      ReferencesManager referencesManager,
                                      UserRequestDAO requestDao,
                                      AsyncTenantStarter tenantStarter,
                                      CloudIntranetUtils cloudIntranetUtils,
                                      EmailBlacklist emailBlacklist,
                                      ChangePasswordManager changePasswordManager,
                                      UsersManager usersManager,
                                      TemplateManagement templateManagement) {
    this.cloudAdminConfiguration = cloudAdminConfiguration;
    this.tenantInfoDataManager = tenantInfoDataManager;
    this.tenantCreator = tenantCreator;
    this.requestDao = requestDao;
    this.workspacesOrganizationRequestPerformer = workspacesOrganizationRequestPerformer;
    this.notificationMailSender = notificationMailSender;
    this.userLimitsStorage = userLimitsStorage;
    this.referencesManager = referencesManager;
    this.tenantStarter = tenantStarter;
    this.utils = cloudIntranetUtils;
    this.emailBlacklist = emailBlacklist;
    this.changePasswordManager = changePasswordManager;
    this.usersManager = usersManager;
    this.templateManagement = templateManagement;
  }

  /**
   * Sign-up the Cloud. Result is an email with instructions on creation or
   * joining a tenant.
   * 
   * @param userMail email address of user to signup
   * @return Response OK with details message or an error.
   * @throws CloudAdminException if error occurs
   */
  @POST
  @Path("/signup")
  public Response signup(@FormParam("user-mail") String userMail) throws CloudAdminException {
    LOG.info("Received Signup request from " + userMail);
    String tName = null;
    String username = null;
    if (!utils.validateEmail(userMail)) {
      LOG.info("User " + userMail + " rejected. Need valid email address.");
      return Response.status(Status.BAD_REQUEST)
                     .entity("Please enter a valid email address.")
                     .type(MediaType.TEXT_PLAIN)
                     .build();
    }

    UserMailInfo userInfo = utils.email2userMailInfo(userMail);
    username = userInfo.getUsername();
    tName = userInfo.getTenant();

    if (emailBlacklist.isInBlackList(userMail) && !tName.equals(utils.getDemoTenantName())) {
      LOG.info("User " + userMail
          + " rejected. Need work email address. Redirecting to tryagain.jsp...");
      return Response.status(309)
                     .header("Location",
                             "http://"
                                 + AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration)
                                 + "/tryagain.jsp")
                     .header("Location-Handler", "tryAgain")
                     .build();
    }

    if (!tenantInfoDataManager.isExists(tName)) {
      if (requestDao.searchByEmail(userMail) == null) {
        Response resp = tenantCreator.createTenantWithEmailConfirmation(tName, userMail);
        referencesManager.putEmail(userMail, (String) resp.getEntity());
      } else {
        LOG.info("User " + userMail + " already signed up to " + tName
            + ". Wait until a workspace will be created.");
        return Response.ok("You already signed up. Wait until your workspace will be created. We will inform you when it will be ready.")
                       .type(MediaType.TEXT_PLAIN)
                       .build();
      }
    } else {
      Map<String, String> props = new HashMap<String, String>();
      props.put("tenant.masterhost", AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration));
      props.put("tenant.repository.name", tName);
      props.put("user.mail", userMail);

      try {
        TenantState tState = TenantState.valueOf(tenantInfoDataManager.getValue(tName,
                                                                                TenantInfoFieldName.PROPERTY_STATE));
        switch (tState) {
        case CREATION:
        case WAITING_CREATION: {
          props.put("rfid", referencesManager.putEmail(userMail, UUID.randomUUID().toString()));
          notificationMailSender.sendOkToJoinEmail(userMail, props);
          return Response.ok().build();
        }
        case ONLINE: {
          if (workspacesOrganizationRequestPerformer.isNewUserAllowed(tName, username)) {
            // send OK email
            props.put("rfid", referencesManager.putEmail(userMail, UUID.randomUUID().toString()));
            notificationMailSender.sendOkToJoinEmail(userMail, props);
            return Response.ok().build();
          } else {
            LOG.info("User " + userMail + " was put in waiting state - users limit reached.");
            UserRequest req = new UserRequest("",
                                              tName,
                                              userMail,
                                              "",
                                              "",
                                              "",
                                              "",
                                              "",
                                              "",
                                              false,
                                              RequestState.WAITING_LIMIT);
            requestDao.put(req);
            // send not allowed mails
            props.put("users.maxallowed",
                      Integer.toString(userLimitsStorage.getMaxUsersForTenant(tName)));
            notificationMailSender.sendJoinRejectedEmails(tName, userMail, props);
            return Response.ok().build();
          }
        }
        case STARTING:
        case STOPPING:
        case STOPPED: {
          LOG.info("User " + userMail
              + " was put in waiting state after singup - tenant suspended.");
          tenantStarter.startTenant(tName);
          UserRequest req = new UserRequest("",
                                            tName,
                                            userMail,
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            "",
                                            false,
                                            RequestState.WAITING_JOIN);
          requestDao.put(req);
          return Response.status(309)
                         .header("Location",
                                 "http://"
                                     + AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration)
                                     + "/resuming.jsp?email=" + userMail + "&action=signup")
                         .build();
        }
        default: {
          String msg = "Sorry, we cannot process your join request right now, workspace seems not ready. Please, try again later.";
          LOG.warn("Signup failed for user " + userMail + ", tenant " + tName + " state is "
              + tenantInfoDataManager.getValue(tName, TenantInfoFieldName.PROPERTY_STATE));
          return Response.status(Status.SERVICE_UNAVAILABLE)
                         .entity(msg)
                         .type(MediaType.TEXT_PLAIN)
                         .build();
        }
        }
      } catch (UserAlreadyExistsException e) {
        // Custom status for disable ajax auto redirection;
        LOG.info("User " + userMail + " already signed up to " + tName
            + ". Redirect to signin page.");
        return Response.status(309)
                       .header("Location",
                               "http://" + tName + "."
                                   + AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration))
                       .build();
      }
    }
    return Response.ok().build();
  }

  /**
   * Sign-up the Cloud. Result is a registration URL for a registeration or to
   * join to an existing tenant.<br/>
   * This URL can be used to proceed the user registration on to the Cloud.<br/>
   * <p>
   * Specification
   * </p>
   * <ul>
   * <li>Service return status 201 Created if such tenant can be created in the
   * cloud, the response entity will contain an URL for a registration of a new
   * tenant. This registration URL will be actual during some fixed period in
   * time (6h currently).</li>
   * <li>If requested tenant already exists and this user can be joined, the
   * service will return status 200 OK and a link to join to the tenant.</li>
   * <li>If such tenant already exists and an user already signed up to the
   * tenant (or it is in progress), the service will return client error 409
   * Conflict and a message "User EMAIL already signed up to TENANT_NAME.".</li>
   * <li>In case if a tenant creation isn't possible a related message will be
   * returned with status 400 Bad Request.</li>
   * <li>For an error, teh error message will be returned with status 500
   * Internal Server Error.</li>
   * </ul>
   * 
   * @param userMail email address of user to signup/join.
   * @return Response with URL for a registration/join or with a client error.
   * @throws CloudAdminException if error occurs
   */
  @POST
  @Path("/signup-link")
  public Response signupLink(@FormParam("user-mail") String userMail) throws CloudAdminException {
    LOG.info("Received Signup Link request for " + userMail);
    String tName = null;
    String username = null;
    if (!utils.validateEmail(userMail)) {
      return Response.status(Status.BAD_REQUEST)
                     .entity("Invalid email address.")
                     .type(MediaType.TEXT_PLAIN)
                     .build();
    }

    UserMailInfo userInfo = utils.email2userMailInfo(userMail);
    username = userInfo.getUsername();
    tName = userInfo.getTenant();
    if (emailBlacklist.isInBlackList(userMail) && !tName.equals(utils.getDemoTenantName())) {
      String domain = userMail.substring(userMail.indexOf("@"));
      return Response.status(Status.BAD_REQUEST)
                     .entity("Cannot sign up with an email address " + domain
                         + ". Require work email.")
                     .type(MediaType.TEXT_PLAIN)
                     .build();
    }

    if (!tenantInfoDataManager.isExists(tName)) {
      if (requestDao.searchByEmail(userMail) == null) {
        String uuid = tenantCreator.createTenant(tName, userMail);
        referencesManager.putEmail(userMail, uuid);
        URI location = URI.create("http://"
            + AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration)
            + "/registration.jsp?id=" + uuid);
        return Response.created(location).entity(location.toString()).build();
      } else {
        LOG.info("Client error: user " + userMail + " already signed up to " + tName
            + ". Wait until a workspace will be created.");
        return Response.status(Status.CONFLICT)
                       .entity("User "
                           + userMail
                           + " already signed up to "
                           + tName
                           + ". Wait until a workspace will be created. The user will be informed when it will be ready.")
                       .type(MediaType.TEXT_PLAIN)
                       .build();
      }
    } else {
      try {
        TenantState tState = TenantState.valueOf(tenantInfoDataManager.getValue(tName,
                                                                                TenantInfoFieldName.PROPERTY_STATE));
        switch (tState) {
        case CREATION:
        case WAITING_CREATION:
        case STARTING:
        case STOPPING:
        case STOPPED: {
          final String uuid = UUID.randomUUID().toString();
          referencesManager.putEmail(userMail, uuid);
          return Response.ok()
                         .entity("http://"
                             + AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration)
                             + "/join.jsp?rfid=" + uuid)
                         .type(MediaType.TEXT_PLAIN)
                         .build();
        }
        case ONLINE: {
          if (workspacesOrganizationRequestPerformer.isNewUserAllowed(tName, username)) {
            // send OK email
            final String uuid = UUID.randomUUID().toString();
            referencesManager.putEmail(userMail, uuid);
            return Response.ok()
                           .entity("http://"
                               + AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration)
                               + "/join.jsp?rfid=" + uuid)
                           .type(MediaType.TEXT_PLAIN)
                           .build();
          } else {
            LOG.info("Link request for join of user " + userMail + " to " + tName
                + " rejected - users limit reached.");
            return Response.status(Status.BAD_REQUEST)
                           .entity("Cannot invite " + userMail + " to " + tName
                               + ". Maximum number of users reached.")
                           .type(MediaType.TEXT_PLAIN)
                           .build();
          }
        }
        default: {
          String msg = "Sorry, we cannot process your join request right now, workspace seems not ready. Please, try again later.";
          LOG.warn("Link request for signup of user " + userMail + " failed, tenant " + tName
              + " state is "
              + tenantInfoDataManager.getValue(tName, TenantInfoFieldName.PROPERTY_STATE));
          return Response.status(Status.SERVICE_UNAVAILABLE)
                         .entity(msg)
                         .type(MediaType.TEXT_PLAIN)
                         .build();
        }
        }
      } catch (UserAlreadyExistsException e) {
        LOG.info("Client error: user " + userMail + " already signed up to " + tName + ".");
        return Response.status(Status.CONFLICT)
                       .entity("User " + userMail + " already signed up to " + tName + ".")
                       .type(MediaType.TEXT_PLAIN)
                       .build();
      }
    }
  }

  /**
   * Join to workspace service.
   * 
   * @param userMail
   * @param firstName
   * @param lastName
   * @param password
   * @param uuid
   * @return Response
   * @throws CloudAdminException
   */
  @POST
  @Path("/join")
  public Response joinIntranet(@FormParam("user-mail") String userMail,
                               @FormParam("first-name") String firstName,
                               @FormParam("last-name") String lastName,
                               @FormParam("password") String password,
                               @FormParam("confirmation-id") String uuid) throws CloudAdminException {
    String tName = null;
    String username = null;
    try {
      if (!utils.validateEmail(userMail))
        return Response.status(Status.BAD_REQUEST)
                       .entity("Please enter a valid email address.")
                       .type(MediaType.TEXT_PLAIN)
                       .build();

      if (!utils.validateUUID(userMail, uuid))
        return Response.status(Status.BAD_REQUEST)
                       .entity("Email address provided does not match with hash.")
                       .type(MediaType.TEXT_PLAIN)
                       .build();

      if (!utils.validateName(firstName))
        return Response.status(Status.BAD_REQUEST)
                       .entity("Sorry, such first-name is not allowed. Please correct it and sign up again.")
                       .type(MediaType.TEXT_PLAIN)
                       .build();

      if (!utils.validateName(lastName))
        return Response.status(Status.BAD_REQUEST)
                       .entity("Sorry, such last-name is not allowed. Please correct it and sign up again.")
                       .type(MediaType.TEXT_PLAIN)
                       .build();

      UserMailInfo userInfo = utils.email2userMailInfo(userMail);
      username = userInfo.getUsername();
      tName = userInfo.getTenant();
      // Prepare properties for mailing
      Map<String, String> props = new HashMap<String, String>();
      props.put("tenant.masterhost", AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration));
      props.put("tenant.repository.name", tName);
      props.put("user.mail", userMail);
      props.put("user.name", username);
      props.put("first.name", firstName);
      props.put("last.name", lastName);

      // Storing user & sending appropriate mails
      TenantState tState = TenantState.valueOf(tenantInfoDataManager.getValue(tName,
                                                                              TenantInfoFieldName.PROPERTY_STATE));
      switch (tState) {
      case ONLINE: {

        try {
          if (workspacesOrganizationRequestPerformer.isNewUserAllowed(tName, username)) {
            workspacesOrganizationRequestPerformer.storeUser(tName,
                                                             username,
                                                             userMail,
                                                             firstName,
                                                             lastName,
                                                             password,
                                                             false);
            notificationMailSender.sendUserJoinedEmails(tName, firstName, userMail, props);
            LOG.info("User " + userMail + " joined directly from form.");
          } else {
            // Limit reached
            LOG.info("User " + userMail + " join was put in waiting state - users limit reached.");
            UserRequest req = new UserRequest("",
                                              tName,
                                              userMail,
                                              firstName,
                                              lastName,
                                              "",
                                              "",
                                              password,
                                              "",
                                              false,
                                              RequestState.WAITING_LIMIT);
            requestDao.put(req);
            props.put("users.maxallowed",
                      Integer.toString(userLimitsStorage.getMaxUsersForTenant(tName)));
            notificationMailSender.sendJoinRejectedEmails(tName, userMail, props);
          }

        } catch (UserAlreadyExistsException e) {
          LOG.warn("User " + username + " already registered on workspace " + tName
              + ". Join request rejected. User warned on the Sign Up form.");
          return Response.ok(e.getMessage()).build();
        }
        break;
      }
      case CREATION:
      case WAITING_CREATION: {
        UserRequest req = new UserRequest("",
                                          tName,
                                          userMail,
                                          firstName,
                                          lastName,
                                          "",
                                          "",
                                          password,
                                          "",
                                          false,
                                          RequestState.WAITING_JOIN);
        requestDao.put(req);
        LOG.info("User " + userMail
            + " join was put in waiting state after join - tenant state WAITING_CREATION.");
        referencesManager.removeEmail(userMail);
        return Response.status(309)
                       .header("Location",
                               new StringBuilder().append("http://")
                                                  .append(AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration))
                                                  .append("/join-done.jsp#")
                                                  .append(userMail)
                                                  .toString())
                       .build();
      }
      case STARTING:
      case STOPPING:
      case STOPPED: {
        tenantStarter.startTenant(tName);
        LOG.info("User " + userMail + " was put in waiting state after join - tenant suspended.");
        UserRequest req = new UserRequest("",
                                          tName,
                                          userMail,
                                          firstName,
                                          lastName,
                                          "",
                                          "",
                                          password,
                                          "",
                                          false,
                                          RequestState.WAITING_JOIN);
        requestDao.put(req);
        referencesManager.removeEmail(userMail);
        return Response.status(309)
                       .header("Location",
                               "http://"
                                   + AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration)
                                   + "/resuming.jsp?email=" + userMail + "&action=join")
                       .build();
      }
      default: {
        String msg = "Sorry, we cannot process your join request right now, workspace seems not ready. Please, try again later.";
        LOG.warn("Joining user " + userMail + " failed, tenant " + tName + " state is " + tState);
        return Response.status(Status.SERVICE_UNAVAILABLE)
                       .entity(msg)
                       .type(MediaType.TEXT_PLAIN)
                       .build();
      }
      }

    } catch (CloudAdminException e) {
      LOG.warn("User " + username + " join failed with error '" + e + "', put him in join queue.");
      UserRequest req = new UserRequest("",
                                        tName,
                                        userMail,
                                        firstName,
                                        lastName,
                                        "",
                                        "",
                                        password,
                                        "",
                                        false,
                                        RequestState.WAITING_JOIN);
      requestDao.put(req);
    }
    referencesManager.removeEmail(userMail);
    return Response.ok().build();
  }

  /**
   * Service for creating workspaces.
   * 
   * @param userMail
   * @param firstName
   * @param lastName
   * @param companyName
   * @param phone
   * @param password
   * @param uuid
   * @return Response
   * @throws CloudAdminException
   */
  @POST
  @Path("/create")
  public Response createIntranet(@FormParam("user-mail") String userMail,
                                 @FormParam("first-name") String firstName,
                                 @FormParam("last-name") String lastName,
                                 @FormParam("company-name") String companyName,
                                 @FormParam("phone") String phone,
                                 @FormParam("password") String password,
                                 @FormParam("confirmation-id") String uuid) throws CloudAdminException {
    if (!utils.validateEmail(userMail))
      return Response.status(Status.BAD_REQUEST)
                     .entity("Please enter a valid email address.")
                     .type(MediaType.TEXT_PLAIN)
                     .build();

    if (!utils.validateUUID(userMail, uuid))
      return Response.status(Status.BAD_REQUEST)
                     .entity("Sorry, your registration link has expired. Please sign up again.")
                     .type(MediaType.TEXT_PLAIN)
                     .build();

    if (!utils.validateName(firstName))
      return Response.status(Status.BAD_REQUEST)
                     .entity("Sorry, such first-name is not allowed. Please correct it and sign up again.")
                     .type(MediaType.TEXT_PLAIN)
                     .build();

    if (!utils.validateName(lastName))
      return Response.status(Status.BAD_REQUEST)
                     .entity("Sorry, such last-name is not allowed. Please correct it and sign up again.")
                     .type(MediaType.TEXT_PLAIN)
                     .build();

    if (!utils.validateName(companyName))
      return Response.status(Status.BAD_REQUEST)
                     .entity("Sorry, such company is not allowed. Please correct it and sign up again.")
                     .type(MediaType.TEXT_PLAIN)
                     .build();

    if (emailBlacklist.isInBlackList(userMail)) {
      String domain = userMail.substring(userMail.indexOf("@"));
      return Response.status(Status.BAD_REQUEST)
                     .entity("Sorry, we can't create workspace with an email address " + domain
                         + ". Try with your work email.")
                     .type(MediaType.TEXT_PLAIN)
                     .build();
    }
    String tName = utils.email2userMailInfo(userMail).getTenant();

    if (!tenantInfoDataManager.isExists(tName)) {
      Response resp = tenantCreator.createTenantWithConfirmedEmail(uuid);
      if (resp.getStatus() != 200) {
        notificationMailSender.sendAdminErrorEmail("Tenant " + tName + " creation admin error: "
            + resp.getEntity(), null);
        return Response.status(resp.getStatus())
                       .entity("An problem happened during processsing this request. It was reported to developers. Please, try again later.")
                       .type(MediaType.TEXT_PLAIN)
                       .build();
      }
      UserRequest req = new UserRequest("",
                                        tName,
                                        userMail,
                                        firstName,
                                        lastName,
                                        companyName,
                                        phone,
                                        password,
                                        uuid,
                                        true,
                                        RequestState.WAITING_JOIN);
      requestDao.put(req);
      referencesManager.removeEmail(userMail);
      return Response.ok().type(MediaType.TEXT_PLAIN).build();
    } else {
      LOG.warn(" Duplicate creation request for tenant " + tName + " from " + userMail);
      referencesManager.removeEmail(userMail);
      return Response.status(Status.BAD_REQUEST)
                     .entity("Tenant with name " + tName + "already exists")
                     .type(MediaType.TEXT_PLAIN)
                     .build();
    }
  }

  /**
   * Retrieves status string of the given tenant.
   * 
   * @param tenantName String tenantName
   * @return Response
   * @throws TenantDataManagerException
   */
  @GET
  @Path("/status/{tenantname}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response tenantStatus(@PathParam("tenantname") String tenantName) throws TenantDataManagerException {
    if (tenantInfoDataManager.isExists(tenantName)) {
      String state = tenantInfoDataManager.getValue(tenantName, TenantInfoFieldName.PROPERTY_STATE);
      return Response.ok(state).build();
    }
    return Response.status(Status.NOT_FOUND).build();
  }

  @POST
  @Path("/contactus")
  public Response contactUs(@FormParam("user-mail") String userMail,
                            @FormParam("user-name") String userName,
                            @FormParam("user-phone") String userPhone,
                            @FormParam("subject") String subject,
                            @FormParam("text") String text) {
    notificationMailSender.sendContactUsEmail(userMail, userName, userPhone, subject, text);
    return Response.ok().build();
  }

  @GET
  @Path("/isuserexist/{tenantname}/{username}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response isuserexist(@PathParam("tenantname") String tName,
                              @PathParam("username") String username) throws CloudAdminException {
    try {
      workspacesOrganizationRequestPerformer.isNewUserAllowed(tName, username);
      return Response.ok(Boolean.toString(false)).build();
    } catch (UserAlreadyExistsException e) {
      return Response.ok(Boolean.toString(true)).build();
    }
  }

  @GET
  @Path("/maxallowed/{tenantname}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response maxallowed(@PathParam("tenantname") String tName) throws CloudAdminException {
    return Response.ok(Integer.toString(userLimitsStorage.getMaxUsersForTenant(tName))).build();
  }

  @GET
  @Path("uuid/{uuid}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response uuid(@PathParam("uuid") String uuid) throws CloudAdminException {
    String email = referencesManager.getEmail(uuid);
    if (email != null) {
      return Response.ok(email).build();
    } else {
      return Response.status(Status.BAD_REQUEST)
                     .entity("Warning! You are using broken link to the Registration Page. Please sign up again.")
                     .type(MediaType.TEXT_PLAIN)
                     .build();
    }
  }

  @GET
  @Path("passrestore/{email}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response passrestore(@PathParam("email") String email) throws CloudAdminException {
    if (!utils.validateEmail(email))
      return Response.status(Status.BAD_REQUEST)
                     .entity("Please enter a valid email address.")
                     .type(MediaType.TEXT_PLAIN)
                     .build();

    UserMailInfo userinfo = utils.email2userMailInfo(email);
    String tName = userinfo.getTenant();

    if (!tenantInfoDataManager.isExists(tName)) {
      return Response.status(Status.BAD_REQUEST)
                     .entity("This email " + email + " is not registered on Cloud Workspaces.")
                     .type(MediaType.TEXT_PLAIN)
                     .build();
    }
    TenantState tState = TenantState.valueOf(tenantInfoDataManager.getValue(tName,
                                                                            TenantInfoFieldName.PROPERTY_STATE));
    switch (tState) {
    case ONLINE: {

      if (workspacesOrganizationRequestPerformer.getTenantUsers(tName, false).containsValue(email)) {
        String uuid = changePasswordManager.addReference(email);
        notificationMailSender.sendPasswordRestoreEmail(email, tName, uuid);
      } else {
        return Response.status(Status.BAD_REQUEST)
                       .entity("User with email " + email
                           + " is not registered on Cloud Workspaces.")
                       .type(MediaType.TEXT_PLAIN)
                       .type(MediaType.TEXT_PLAIN)
                       .build();
      }
      return Response.ok().build();
    }

    case STARTING:
    case STOPPING:
    case STOPPED: {
      tenantStarter.startTenant(tName);
      return Response.status(309)
                     .header("Location",
                             "http://"
                                 + AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration)
                                 + "/resuming.jsp?email=" + email + "&action=reset")
                     .build();
    }

    default: {
      return Response.status(Status.SERVICE_UNAVAILABLE)
                     .entity("Workspace " + tName + " seems not ready. Please, try again later.")
                     .type(MediaType.TEXT_PLAIN)
                     .build();
    }
    }
  }

  @POST
  @Path("passconfirm")
  @Produces(MediaType.TEXT_PLAIN)
  public Response passconfirm(@FormParam("uuid") String uuid, @FormParam("password") String password) throws CloudAdminException {
    // TODO if uuid not found, return BAD_REQUEST
    String email = changePasswordManager.validateReference(uuid);
    UserMailInfo userInfo = utils.email2userMailInfo(email);
    String tName = userInfo.getTenant();
    String username = userInfo.getUsername();
    workspacesOrganizationRequestPerformer.updatePassword(tName, username, email, password);
    return Response.ok().entity(tName).build();
  }

  /**
   * Answers on question
   * "Does the given email's domain address is blacklisted?". Used in Invitation
   * gadget.
   * 
   * @param email String email to check
   * @return String, TRUE or FALSE - the answer on the question
   *         "Does the given email's domain address is blackisted?"
   */
  @GET
  @Path("blacklisted/{email}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response blacklisted(@PathParam("email") String email) {
    boolean blacklisted = emailBlacklist.isInBlackList(email);
    return Response.ok(Boolean.toString(blacklisted)).build();
  }

  @GET
  @Path("usermailinfo/{email}")
  @Produces(MediaType.APPLICATION_JSON)
  public UserMailInfo usermailinfo(@PathParam("email") String email) {
    return utils.email2userMailInfo(email);
  }

  @GET
  @Path("autojoin")
  @Produces(MediaType.TEXT_PLAIN)
  public Response joinAll() throws CloudAdminException {
    usersManager.joinAll();
    return Response.ok().build();
  }

  @GET
  @Path("autojoin/{tenantname}/{state}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response joinall(@PathParam("tenantname") String tName, @PathParam("state") String state) throws CloudAdminException {
    RequestState rstate = RequestState.valueOf(state);
    usersManager.joinAll(tName, rstate);
    return Response.ok().build();
  }

  /**
   * Send custom mail to owners of tenants on validation.
   * 
   * <pre>
   * possible use of "scope" parameter, /sendmail/{scope}:
   * * validating_email - send to users of tenants on validation
   * * suspended - send to users of suspended tenants
   * * online - send to users of online tenants
   * * creation_fail - send to users of tenants in error
   * * all - to all users
   * ....
   * </pre>
   * 
   * @param mailTemplate mail template to be send to given users
   * @param mailSubject subject for a mail message
   * @return Response OK with details message or an error.
   * @throws Exception if cannot send send custom email
   */
  @POST
  @RolesAllowed({ "cloud-admin", "cloud-manager" })
  @Path("/sendmail/{state}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response sendMail(@FormParam("template") String mailTemplate,
                           @FormParam("subject") String mailSubject,
                           @PathParam("state") String state) throws Exception {
    try {
      if (TenantState.VALIDATING_EMAIL.toString().equalsIgnoreCase(state)) {
        notificationMailSender.sendEmailToValidation(mailTemplate, mailSubject);
      } else {
        notificationMailSender.sendEmailForTenantsToState(mailTemplate, mailSubject, state);
      }
    } catch (CloudAdminException e) {
      String msg = "Cannot send custom email '" + mailSubject + ", mail template is "
          + mailTemplate + "'. Skipping it.";
      LOG.info(msg);
      throw e;
    }
    return Response.ok().build();
  }

  @POST
  @RolesAllowed("cloud-admin")
  @Path("/update/templateid")
  @Produces(MediaType.TEXT_PLAIN)
  public Response updateTempalateId() throws CloudAdminException {

    templateManagement.updateTemplateId();

    return Response.ok().build();
  }
}
