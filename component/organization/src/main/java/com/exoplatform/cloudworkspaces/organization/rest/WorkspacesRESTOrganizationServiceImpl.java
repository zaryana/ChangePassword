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
package com.exoplatform.cloudworkspaces.organization.rest;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * The Class IntranetRESTOrganizationServiceImpl.
 */
@Path("/cloudworkspaces/organization")
public class WorkspacesRESTOrganizationServiceImpl {
  protected static final Logger       LOG          = LoggerFactory.getLogger(WorkspacesRESTOrganizationServiceImpl.class);

  protected Format                    dateFormater = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss SSS");

  protected final RepositoryService   repositoryService;

  protected final OrganizationService organizationService;

  protected final String              hostInfo;

  public WorkspacesRESTOrganizationServiceImpl(RepositoryService repositoryService,
                                               OrganizationService organizationService) {
    this.repositoryService = repositoryService;
    this.organizationService = organizationService;

    String hostname;
    try {
      Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();

      StringBuffer allIfs = new StringBuffer("");
      while (nis.hasMoreElements()) {
        NetworkInterface ni = nis.nextElement();
        if (ni != null && !ni.isLoopback()) {
          Enumeration<InetAddress> ia = ni.getInetAddresses();
          StringBuffer allAddrs = new StringBuffer("");
          while (ia.hasMoreElements()) {
            InetAddress n = ia.nextElement();
            if (n != null && !n.isLoopbackAddress()) {
              if (allAddrs.length() > 0) {
                allAddrs.append(", ");
              } else {

                allAddrs.append("")
                        .append(n.getCanonicalHostName())
                        .append(" (")
                        .append(n.getHostAddress())
                        .append(")");
              }
            }
          }
          allIfs.append("[").append(allAddrs).append("]");
        }
      }

      if (allIfs.length() > 0) {
        hostname = allIfs.toString();
      } else {
        InetAddress lo = InetAddress.getLocalHost();
        hostname = lo.getCanonicalHostName() + " (" + lo.getHostAddress() + ")";
      }
    } catch (Throwable th) {
      hostname = "UNKNOWN: " + th.getMessage();
    }
    this.hostInfo = hostname;
  }

  /**
   * Creates the user on given repository.
   * 
   * @param tname the workspace name
   * @param baseURI the base uri
   * @param userName the user name
   * @param password the password
   * @param firstName the first name
   * @param lastName the last name
   * @param email the email
   * @return the response
   * @throws Exception the exception
   */
  @POST
  @Path("/adduser")
  @RolesAllowed("cloud-admin")
  public Response createUser(@FormParam("tname")
  String tname, @FormParam("URI")
  String baseURI, @FormParam("username")
  String userName, @FormParam("password")
  String password, @FormParam("first-name")
  String firstName, @FormParam("last-name")
  String lastName, @FormParam("email")
  String email, @FormParam("isadministrator")
  String administrator) throws Exception {
    try {
      repositoryService.setCurrentRepositoryName(tname);
      UserHandler userHandler = organizationService.getUserHandler();
      User newUser = userHandler.createUserInstance(userName);
      newUser.setPassword(password);
      newUser.setFirstName(firstName);
      newUser.setLastName(lastName);
      newUser.setEmail(email);
      userHandler.createUser(newUser, true);

      GroupHandler groupHandler = organizationService.getGroupHandler();
      MembershipType membership_member = organizationService.getMembershipTypeHandler()
                                                            .findMembershipType("member");
      if (Boolean.parseBoolean(administrator)) {
        Group adminGroup = groupHandler.findGroupById("/platform/administrators");
        organizationService.getMembershipHandler().linkMembership(newUser,
                                                                  adminGroup,
                                                                  membership_member,
                                                                  true);
      }

      return Response.status(HTTPStatus.CREATED)
                     .entity("Created")
                     .type(MediaType.TEXT_PLAIN)
                     .build();
    } catch (Exception e) {
      String err = "Unable to store user " + userName + " in tenant " + tname;
      LOG.error(err, e);
      e.printStackTrace();
      throw new WebApplicationException(e, Response.status(HTTPStatus.INTERNAL_ERROR)
                                                   .entity(errorMessage(err, e))
                                                   .type("text/plain")
                                                   .build());
    }
  }

  /**
   * Gets the administrators list for given workspace.
   * 
   * @param tname workspace name
   * @return json username:email value
   * @throws Exception
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/users/{tname}")
  @RolesAllowed("cloud-admin")
  public Map<String, String> getUsersList(@PathParam("tname")
  String tname, @QueryParam("administratorsonly")
  String onlyAdmins) throws Exception {
    try {
      Map<String, String> result = new HashMap<String, String>();
      repositoryService.setCurrentRepositoryName(tname);
      ListAccess<User> list = organizationService.getUserHandler().findAllUsers();// findUsersByGroupId("/platform/administrators");
      for (User one : list.load(0, list.getSize())) {
        Collection<Group> groups = organizationService.getGroupHandler()
                                                      .findGroupsOfUser(one.getUserName());
        for (Group group : groups) {
          if (!Boolean.parseBoolean(onlyAdmins)
              || group.getId().equalsIgnoreCase("/platform/administrators"))
            result.put(one.getUserName(), one.getEmail());
        }
      }
      return result;
    } catch (Exception e) {
      String err = "Unable to get administrators list in workspace " + tname;
      LOG.error(err, e);
      throw new WebApplicationException(e, Response.status(HTTPStatus.INTERNAL_ERROR)
                                                   .entity(errorMessage(err, e))
                                                   .type("text/plain")
                                                   .build());
    }
  }

  @GET
  @Path("/usernamebyemail/{tname}/{email}")
  @RolesAllowed("cloud-admin")
  public Response hasUserByEmail(@PathParam("tname")
  String tname, @PathParam("email")
  String email) {
    try {
      repositoryService.setCurrentRepositoryName(tname);
      Query emailQuery = new Query();
      emailQuery.setEmail(email);
      ListAccess<User> users = organizationService.getUserHandler().findUsersByQuery(emailQuery);
      if (users.getSize() == 0)
        return Response.status(404).build();
      return Response.ok(String.valueOf(users.load(0, 1)[0].getUserName()))
                     .type(MediaType.TEXT_PLAIN)
                     .build();
    } catch (Exception e) {
      String err = "Unable to find users by email in workspace " + tname;
      LOG.error(err, e);
      throw new WebApplicationException(e, Response.status(HTTPStatus.INTERNAL_ERROR)
                                                   .entity(errorMessage(err, e))
                                                   .type("text/plain")
                                                   .build());
    }
  }

  @POST
  @Path("/newpassword")
  @RolesAllowed("cloud-admin")
  public Response updatePassword(@FormParam("tname")
  String tname, @FormParam("username")
  String userName, @FormParam("password")
  String password) throws Exception {
    try {
      repositoryService.setCurrentRepositoryName(tname);
      User user = organizationService.getUserHandler().findUserByName(userName);

      if (user != null) {
        // save new instance of an User object to workaround JCR org service
        // caching to let PasswordEncrypterUserListener to know that we have to
        // encript this password
        User updatedUser = organizationService.getUserHandler()
                                              .createUserInstance(user.getUserName());
        updatedUser.setFirstName(user.getFirstName());
        updatedUser.setLastName(user.getLastName());
        updatedUser.setFullName(user.getFullName());
        updatedUser.setEmail(user.getEmail());
        updatedUser.setOrganizationId(user.getOrganizationId());
        updatedUser.setPassword(password);
        // and deprecated
        updatedUser.setCreatedDate(user.getCreatedDate());
        updatedUser.setLastLoginTime(user.getLastLoginTime());

        organizationService.getUserHandler().saveUser(updatedUser, true);
        return Response.ok().build();
      } else {
        return Response.status(Status.BAD_REQUEST)
                       .entity("User " + userName + " not found on " + tname)
                       .type(MediaType.TEXT_PLAIN)
                       .build();
      }
    } catch (Exception e) {
      String err = "Unable to change password of user " + userName + " on " + tname;
      LOG.error(err, e);
      throw new WebApplicationException(e, Response.status(HTTPStatus.INTERNAL_ERROR)
                                                   .entity(errorMessage(err, e))
                                                   .type("text/plain")
                                                   .build());
    }
  }

  protected String errorMessage(String message, Exception err) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintWriter wr = new PrintWriter(baos);
    try {
      err.printStackTrace(wr);
      wr.flush();
      StringBuilder str = new StringBuilder();
      str.append('[');
      str.append(dateFormater.format(new Date()));
      str.append(']');
      str.append(' ');
      str.append(hostInfo);
      str.append(':');
      str.append(message);
      str.append("\r\n");
      str.append(new String(baos.toByteArray()));
      return str.toString();
    } catch (Throwable th) {
      LOG.error("Cannot prepare error message:", th);
      return message + " (Error trace isn't available, see server logs (" + hostInfo
          + ") for details)";
    } finally {
      wr.close();
    }
  }
}
