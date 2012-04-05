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

import com.exoplatform.cloudworkspaces.ChangePasswordManager;
import com.exoplatform.cloudworkspaces.CloudIntranetUtils;
import com.exoplatform.cloudworkspaces.NotificationMailSender;
import com.exoplatform.cloudworkspaces.ReferencesManager;
import com.exoplatform.cloudworkspaces.RequestState;
import com.exoplatform.cloudworkspaces.UserAlreadyExistsException;
import com.exoplatform.cloudworkspaces.UserRequest;
import com.exoplatform.cloudworkspaces.UserRequestDAO;
import com.exoplatform.cloudworkspaces.http.WorkspacesOrganizationRequestPerformer;
import com.exoplatform.cloudworkspaces.listener.AsyncTenantStarter;
import com.exoplatform.cloudworkspaces.users.UserLimitsStorage;

import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.TenantAlreadyExistException;
import org.exoplatform.cloudmanagement.admin.WorkspacesMailSender;
import org.exoplatform.cloudmanagement.admin.configuration.TenantInfoFieldName;
import org.exoplatform.cloudmanagement.admin.dao.EmailValidationStorage;
import org.exoplatform.cloudmanagement.admin.dao.TenantDataManagerException;
import org.exoplatform.cloudmanagement.admin.dao.TenantInfoDataManager;
import org.exoplatform.cloudmanagement.admin.rest.TenantCreator;
import org.exoplatform.cloudmanagement.admin.tenant.TenantNameValidator;
import org.exoplatform.cloudmanagement.admin.tenant.TenantStateDataManager;
import org.exoplatform.cloudmanagement.admin.tenant.UserMailValidator;
import org.exoplatform.cloudmanagement.admin.util.AdminConfigurationUtil;
import org.exoplatform.cloudmanagement.status.TenantState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.swing.text.StyledEditorKit.BoldAction;
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
public class CloudWorkspacesTenantService extends TenantCreator {

  CloudIntranetUtils                             utils;

  UserRequestDAO                                 requestDao;

  private static final Logger                    LOG = LoggerFactory.getLogger(CloudWorkspacesTenantService.class);

  private WorkspacesOrganizationRequestPerformer workspacesOrganizationRequestPerformer;

  private NotificationMailSender                 notificationMailSender;

  private UserLimitsStorage                      userLimitsStorage;

  private ReferencesManager                      referencesManager;

  private AsyncTenantStarter                     tenantStarter;

  public CloudWorkspacesTenantService(EmailValidationStorage emailValidationStorage,
                                      TenantStateDataManager tenantStateDataManager,
                                      TenantNameValidator tenantNameValidator,
                                      UserMailValidator userMailValidator,
                                      TenantInfoDataManager tenantInfoDataManager,
                                      WorkspacesOrganizationRequestPerformer workspacesOrganizationRequestPerformer,
                                      NotificationMailSender notificationMailSender,
                                      UserLimitsStorage userLimitsStorage,
                                      Configuration cloudAdminConfiguration,
                                      WorkspacesMailSender mailSender,
                                      ReferencesManager referencesManager,
                                      UserRequestDAO requestDao,
                                      AsyncTenantStarter tenantStarter,
                                      CloudIntranetUtils cloudIntranetUtils) {
    super(emailValidationStorage,
          tenantStateDataManager,
          tenantNameValidator,
          userMailValidator,
          tenantInfoDataManager,
          cloudAdminConfiguration,
          mailSender);
    this.requestDao = requestDao;
    this.workspacesOrganizationRequestPerformer = workspacesOrganizationRequestPerformer;
    this.notificationMailSender = notificationMailSender;
    this.userLimitsStorage = userLimitsStorage;
    this.referencesManager = referencesManager;
    this.tenantStarter = tenantStarter;
    this.utils = cloudIntranetUtils;
  }

  /**
   * Sign-up the Cloud. Result is an email with instructions on creation or
   * joining a tenant.
   * 
   * @param String userMail email address of user to signup
   * @return Response OK with details message or an error.
   * @throws CloudAdminException if error occurs
   */
  @POST
  @Path("/signup")
  public Response signup(@FormParam("user-mail") String userMail) throws CloudAdminException {
    LOG.info("Received Signup request from " + userMail);
    String tName = null;
    String username = null;
    try {
      if (!utils.validateEmail(userMail)) {
        LOG.info("User " + userMail + " rejected. Need valid email address.");
        return Response.status(Status.BAD_REQUEST)
                       .entity("Please enter a valid email address.")
                       .build();
      }

      username = userMail.substring(0, (userMail.indexOf("@")));

      if (utils.isInBlackList(userMail)) {
        String domain = userMail.substring(userMail.indexOf("@"));
        LOG.info("User " + userMail + " rejected. Need work email address.");
        return Response.status(Status.BAD_REQUEST)
                       .entity("Sorry, we can't sign you up with an email address " + domain
                           + ". Try with your work email.")
                       .build();
      }

      tName = utils.email2tenantName(userMail);

      if (requestDao.searchByEmail(userMail) == null) {
        Response resp = super.createTenantWithEmailConfirmation(tName, userMail);
        referencesManager.putEmail(userMail, (String) resp.getEntity());
      } else {
        LOG.info("User " + userMail + " already signed up to " + tName
            + ". Wait until a workspace will be created.");
        return Response.ok("You already signed up. Wait until your workspace will be created. We will inform you when it will be ready.")
                       .build();
      }
    } catch (TenantAlreadyExistException ex) {
      Map<String, String> props = new HashMap<String, String>();
      props.put("tenant.masterhost", AdminConfigurationUtil.getMasterHost(adminConfiguration));
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
        case SUSPENDED: {
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
                                     + AdminConfigurationUtil.getMasterHost(adminConfiguration)
                                     + "/resuming.jsp?email=" + userMail)
                         .build();
        }
        default: {
          String msg = "Sorry, we cannot process your join request right now, workspace seems not ready. Please, try again later.";
          LOG.warn("Signup failed for user " + userMail + ", tenant " + tName + " state is "
              + tenantInfoDataManager.getValue(tName, TenantInfoFieldName.PROPERTY_STATE));
          return Response.status(Status.SERVICE_UNAVAILABLE).entity(msg).build();
        }
        }
      } catch (UserAlreadyExistsException e) {
        // Custom status for disable ajax auto redirection;
        LOG.info("User " + userMail + " already signed up to " + tName
            + ". Redirect to signin page.");
        return Response.status(309)
                       .header("Location",
                               "http://" + AdminConfigurationUtil.getMasterHost(adminConfiguration)
                                   + "/signin.jsp?email=" + userMail)
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
   * @param String userMail email address of user to signup/join.
   * @return Response with URL for a registration/join or with a client error.
   * @throws CloudAdminException if error occurs
   */
  @POST
  @Path("/signup-link")
  public Response signupLink(@FormParam("user-mail") String userMail) throws CloudAdminException {
    LOG.info("Received Signup Link request for " + userMail);
    String tName = null;
    String username = null;
    try {
      if (!utils.validateEmail(userMail)) {
        return Response.status(Status.BAD_REQUEST).entity("Invalid email address.").build();
      }

      username = userMail.substring(0, (userMail.indexOf("@")));

      if (utils.isInBlackList(userMail)) {
        String domain = userMail.substring(userMail.indexOf("@"));
        return Response.status(Status.BAD_REQUEST)
                       .entity("Cannot sign up with an email address " + domain
                           + ". Require work email.")
                       .build();
      }
      tName = utils.email2tenantName(userMail);

      if (requestDao.searchByEmail(userMail) == null) {
        String uuid = super.createTenant(tName, userMail);
        referencesManager.putEmail(userMail, uuid);
        URI location = URI.create("http://"
            + AdminConfigurationUtil.getMasterHost(adminConfiguration) + "/registration.jsp?id="
            + uuid);
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
                       .build();
      }
    } catch (TenantAlreadyExistException ex) {
      try {
        TenantState tState = TenantState.valueOf(tenantInfoDataManager.getValue(tName,
                                                                                TenantInfoFieldName.PROPERTY_STATE));
        switch (tState) {
        case CREATION:
        case WAITING_CREATION:
        case SUSPENDED: {
          final String uuid = UUID.randomUUID().toString();
          referencesManager.putEmail(userMail, uuid);
          return Response.ok()
                         .entity("http://"
                             + AdminConfigurationUtil.getMasterHost(adminConfiguration)
                             + "/join.jsp?rfid=" + uuid)
                         .build();
        }
        case ONLINE: {
          if (workspacesOrganizationRequestPerformer.isNewUserAllowed(tName, username)) {
            // send OK email
            final String uuid = UUID.randomUUID().toString();
            referencesManager.putEmail(userMail, uuid);
            return Response.ok()
                           .entity("http://"
                               + AdminConfigurationUtil.getMasterHost(adminConfiguration)
                               + "/join.jsp?rfid=" + uuid)
                           .build();
          } else {
            LOG.info("Link request for join of user " + userMail + " to " + tName
                + " rejected - users limit reached.");
            return Response.status(Status.BAD_REQUEST)
                           .entity("Cannot invite " + userMail + " to " + tName
                               + ". Maximum number of users reached.")
                           .build();
          }
        }
        default: {
          String msg = "Sorry, we cannot process your join request right now, workspace seems not ready. Please, try again later.";
          LOG.warn("Link request for signup of user " + userMail + " failed, tenant " + tName
              + " state is "
              + tenantInfoDataManager.getValue(tName, TenantInfoFieldName.PROPERTY_STATE));
          return Response.status(Status.SERVICE_UNAVAILABLE).entity(msg).build();
        }
        }
      } catch (UserAlreadyExistsException e) {
        LOG.info("Client error: user " + userMail + " already signed up to " + tName + ".");
        return Response.status(Status.CONFLICT)
                       .entity("User " + userMail + " already signed up to " + tName + ".")
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
                       .build();

      if (!utils.validateUUID(userMail, uuid))
        return Response.status(Status.BAD_REQUEST)
                       .entity("Email address provided does not match with hash.")
                       .build();

      username = userMail.substring(0, (userMail.indexOf("@")));
      tName = utils.email2tenantName(userMail);
      // Prepare properties for mailing
      Map<String, String> props = new HashMap<String, String>();
      props.put("tenant.masterhost", AdminConfigurationUtil.getMasterHost(adminConfiguration));
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
        break;
      }
      case SUSPENDED: {
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
                               "http://" + AdminConfigurationUtil.getMasterHost(adminConfiguration)
                                   + "/resuming.jsp?email=" + userMail)
                       .build();
      }
      default: {
        String msg = "Sorry, we cannot process your join request right now, workspace seems not ready. Please, try again later.";
        LOG.warn("Joining user " + userMail + " failed, tenant " + tName + " state is "
            + tenantInfoDataManager.getValue(tName, TenantInfoFieldName.PROPERTY_STATE));
        return Response.status(Status.SERVICE_UNAVAILABLE).entity(msg).build();
      }
      }

    } catch (CloudAdminException e) {
      LOG.warn("User " + username + " join failed, put him in join queue.");
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
                     .build();

    if (!utils.validateUUID(userMail, uuid))
      return Response.status(Status.BAD_REQUEST)
                     .entity("Sorry, your registration link has expired. Please sign up again.")
                     .build();

    if (utils.isInBlackList(userMail)) {
      String domain = userMail.substring(userMail.indexOf("@"));
      return Response.status(Status.BAD_REQUEST)
                     .entity("Sorry, we can't create workspace with an email address " + domain
                         + ". Try with your work email.")
                     .build();
    }

    String tName = utils.email2tenantName(userMail);
    Response resp = super.createTenantWithConfirmedEmail(uuid);
    if (resp.getStatus() != 200) {
      notificationMailSender.sendAdminErrorEmail("Tenant " + tName + " creation admin error: "
          + resp.getEntity(), null);
      return Response.status(resp.getStatus())
                     .entity("An problem happened during processsing this request. It was reported to developers. Please, try again later.")
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
    return Response.ok().build();
  }

  /**
   * Retrieves status string of the given tenant.
   * 
   * @param tenantName
   * @return Response
   * @throws TenantDataManagerException
   * @throws CloudAdminException
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
                            @FormParam("first-name") String firstName,
                            @FormParam("subject") String subject,
                            @FormParam("text") String text) {
    notificationMailSender.sendContactUsEmail(userMail, firstName, subject, text);
    return Response.ok().build();
  }

  /*
   * @POST
   * @Path("/create") public Response create(@FormParam("user-mail") String
   * userMail,
   * @FormParam("first-name") String firstName,
   * @FormParam("last-name") String lastName,
   * @FormParam("company-name") String companyName,
   * @FormParam("phone") String phone,
   * @FormParam("password") String password,
   * @FormParam("confirmation-id") String uuid) throws CloudAdminException { if
   * (!utils.validateEmail(userMail)) return Response.status(Status.BAD_REQUEST)
   * .entity("Please enter a valid email address.") .build(); if
   * (!utils.validateUUID(userMail, uuid)) return
   * Response.status(Status.BAD_REQUEST)
   * .entity("Sorry, your registration link has expired. Please sign up again.")
   * .build(); if (utils.isInBlackList(userMail)) { String domain =
   * userMail.substring(userMail.indexOf("@")); return
   * Response.status(Status.BAD_REQUEST)
   * .entity("Sorry, we can't create workspace with an email address " + domain
   * + ". Try with your work email.") .build(); } String tName =
   * utils.email2tenantName(userMail); Map<String, String> props = new
   * HashMap<String, String>(); String username = userMail.substring(0,
   * (userMail.indexOf("@"))); props.put("tenant.masterhost",
   * AdminConfigurationUtil.getMasterHost(adminConfiguration));
   * props.put("tenant.repository.name", tName); props.put("user.mail",
   * userMail); props.put("user.name", username); props.put("first.name",
   * firstName); props.put("last.name", lastName);
   * utils.sendCreationQueuedEmails(tName, userMail, props); UserRequest req =
   * new UserRequest("", tName, userMail, firstName, lastName, companyName,
   * phone, password, uuid, true, RequestState.WAITING_CREATION);
   * requestDao.put(req); new
   * ReferencesManager(adminConfiguration).removeEmail(userMail); return
   * Response.ok().build(); }
   * @GET
   * @RolesAllowed("cloud-manager")
   * @Produces(MediaType.APPLICATION_JSON)
   * @Path("/requests") public Map<String, String[]> getTenantRequests() throws
   * CloudAdminException { Map<String, String[]> result = new HashMap<String,
   * String[]>(); List<UserRequest> list = requestDao.search(null,
   * RequestState.WAITING_CREATION); if (list.isEmpty()) return result; for
   * (UserRequest one : list) { try { String tName = one.getTenantName();
   * String[] data = new String[5]; data[0] = tName; data[1] =
   * one.getUserEmail(); data[2] = one.getFirstName() + " " + one.getLastName();
   * data[3] = one.getCompanyName(); data[4] = one.getPhone();
   * result.put(one.getFileName().substring(0, one.getFileName().indexOf(".")),
   * data); } catch (Exception e) { LOG.error(e.getMessage());
   * utils.sendAdminErrorEmail(e.getMessage(), e); throw new
   * CloudAdminException(
   * "A problem happened during retrieving requests list . It was reported to developers. Please, try again later."
   * ); } } return utils.sortByComparator(result); }
   * @GET
   * @Path("/validate/{decision}/{filename}")
   * @RolesAllowed("cloud-manager")
   * @Produces(MediaType.TEXT_PLAIN) public Response
   * validate(@PathParam("decision") String decision,
   * @PathParam("filename") String filename) throws CloudAdminException {
   * filename = filename + ".properties"; UserRequest req =
   * requestDao.searchByFilename(filename); if (req == null) {
   * LOG.warn("Validation requested file which can not be found anymore: " +
   * filename); return
   * Response.serverError().entity("File can not be found on server anymore."
   * ).build(); } if (decision.equalsIgnoreCase("accept")) { Response resp =
   * createIntranet(req.getUserEmail(), req.getFirstName(), req.getLastName(),
   * req.getCompanyName(), req.getPhone(), req.getPassword(),
   * req.getConfirmationId()); if (resp.getStatus() == 200 && resp.getEntity()
   * == null) { List<UserRequest> list = requestDao.search(req.getTenantName(),
   * RequestState.WAITING_CREATION); for (UserRequest one : list) { UserRequest
   * req_new = new UserRequest(one.getFileName(), one.getTenantName(),
   * one.getUserEmail(), one.getFirstName(), one.getLastName(),
   * one.getCompanyName(), one.getPhone(), one.getPassword(),
   * one.getConfirmationId(), one.getUserEmail().equals(req.getUserEmail()) ?
   * true : false, RequestState.WAITING_JOIN); requestDao.delete(one); try {
   * Thread.sleep(100); // To let FS finish } catch (InterruptedException e) {
   * LOG.warn(e.getMessage()); } requestDao.put(req_new); } return resp; } else
   * { String msg =
   * "Can not finish accept operation - service returned HTTP status " +
   * resp.getStatus(); LOG.error(msg); utils.sendAdminErrorEmail(msg, null);
   * return Response.serverError()
   * .entity("Operation failed. It was reported to developers.") .build(); } }
   * else if (decision.equalsIgnoreCase("refuse")) { LOG.info("Tenant " +
   * req.getTenantName() + " creation was refused."); requestDao.delete(req);
   * return Response.ok().build(); } else if
   * (decision.equalsIgnoreCase("blacklist")) { Map<String, String> props = new
   * HashMap<String, String>(); props.put("tenant.masterhost",
   * AdminConfigurationUtil.getMasterHost(adminConfiguration));
   * props.put("user.name", req.getFirstName());
   * utils.sendCreationRejectedEmail(req.getTenantName(), req.getUserEmail(),
   * props); utils.putInBlackList(req.getUserEmail()); requestDao.delete(req);
   * return Response.ok().build(); } else { throw new
   * CloudAdminException("Unknown action."); } }
   */

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
    if (email != null)
      return Response.ok(email).build();
    else
      return Response.status(Status.BAD_REQUEST)
                     .entity("Warning! You are using broken link to the Registration Page. Please sign up again.")
                     .build();
  }

  @GET
  @Path("passrestore/{email}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response passrestore(@PathParam("email") String email) throws CloudAdminException {
    if (!utils.validateEmail(email))
      return Response.status(Status.BAD_REQUEST)
                     .entity("Please enter a valid email address.")
                     .build();

    ChangePasswordManager manager = new ChangePasswordManager(adminConfiguration);
    String username = email.substring(0, (email.indexOf("@")));
    String tName = utils.email2tenantName(email);

    if (!tenantInfoDataManager.isExists(tName)) {
      return Response.status(Status.BAD_REQUEST)
                     .entity("This email " + email + " is not registered on Cloud Workspaces.")
                     .build();
    }
    TenantState tState = TenantState.valueOf(tenantInfoDataManager.getValue(tName,
                                                                            TenantInfoFieldName.PROPERTY_STATE));
    switch (tState) {
    case ONLINE: {

      if (isuserexist(tName, username).getEntity().equals("true")) {
        String uuid = manager.addReference(email);
        notificationMailSender.sendPasswordRestoreEmail(email, tName, uuid);
      } else {
        return Response.status(Status.BAD_REQUEST)
                       .entity("User with email " + email
                           + " is not registered on Cloud Workspaces.")
                       .build();
      }
      return Response.ok().build();
    }

    case SUSPENDED: {
      return Response.status(309)
                     .header("Location",
                             "http://" + AdminConfigurationUtil.getMasterHost(adminConfiguration)
                                 + "/resuming.jsp?email=" + email)
                     .build();
    }

    default: {
      return Response.status(Status.SERVICE_UNAVAILABLE)
                     .entity("Workspace " + tName + " seems not ready. Please, try again later.")
                     .build();
    }
    }
  }

  @POST
  @Path("passconfirm")
  @Produces(MediaType.TEXT_PLAIN)
  public Response passconfirm(@FormParam("uuid") String uuid, @FormParam("password") String password) throws CloudAdminException {
    ChangePasswordManager manager = new ChangePasswordManager(adminConfiguration);
    try {
      String email = manager.validateReference(uuid);
      String tName = utils.email2tenantName(email);
      workspacesOrganizationRequestPerformer.updatePassword(tName, email, password);
      return Response.ok().build();
    } catch (CloudAdminException e) {
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  /**
   * Answers on question "Does the given email's domain address is blackisted?".
   * Used in Invitation gadget.
   * 
   * @param String email
   * @return String, TRUE or FALSE - the answer on the question
   *         "Does the given email's domain address is blackisted?"
   */
  @GET
  @Path("blacklisted/{email}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response blacklisted(@PathParam("email") String email) {
    boolean blacklisted = utils.isInBlackList(email);
    return Response.ok(Boolean.toString(blacklisted)).build();
  }

  @GET
  @Path("tenantname/{email}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response tenantname(@PathParam("email") String email) {
    return Response.ok(utils.email2tenantName(email)).build();
  }

}
