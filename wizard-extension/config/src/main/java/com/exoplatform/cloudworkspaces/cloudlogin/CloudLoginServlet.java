/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.exoplatform.cloudworkspaces.cloudlogin;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.webui.Utils;

import com.exoplatform.cloudworkspaces.cloudlogin.CloudLoginService;
import com.exoplatform.cloudworkspaces.cloudlogin.data.CloudLoginStatus;

/**
 * Servlet to manage cloud login screens
 * <p>
 * If there is a problem, forward to HP
 * 
 * @author Clement
 *
 */
public class CloudLoginServlet extends HttpServlet {
  private static final long serialVersionUID = -8682277458138577779L;

  private static final Log logger = ExoLogger.getLogger(CloudLoginServlet.class);

  public static final String HP_SERVLET_CTX = "/portal";
  /**
   * CL_SERVLET_CTX depends on name of cloud webui war, in case of cloud-workspaces, it's named ROOT which is corresponding to "" context
   * if war name is "cloud.war", CL_SERVLET_CTX need to be named "cloud"
   */
  public static final String CL_SERVLET_CTX = "";
  public static final String CL_SERVLET_URL = "/cloudlogin";
  public static final String CL_JSP_RESOURCE = "/StartedStep.jsp";
  
  public static final String CLOUD_REQUESTED_URI = "cloudRequestUri";
  public static final String CLOUD_PROCESS_DISPLAYED = "cloudProcessDisplayed";
  public static final String INITIAL_URI_ATTRIBUTE = "org.cloudworkspaces.login.initial_uri";
  public static final String USER_PROFILE_FIRSTNAME = "org.cloudworkspaces.profile.first_name";
  public static final String USER_PROFILE_LASTNAME = "org.cloudworkspaces.profile.last_name";
  
  private CloudLoginService cloudLoginService;
  public CloudLoginService getCloudLoginService() {
    if(cloudLoginService == null) {
      cloudLoginService = (CloudLoginService)PortalContainer.getInstance().getComponentInstanceOfType(CloudLoginService.class);
    }
    return cloudLoginService;
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String user = request.getRemoteUser();
    StringBuffer targetUri = new StringBuffer("");

    // Get all parameters
    String cloudRequestedUri = request.getParameter(CLOUD_REQUESTED_URI);
    Boolean cloudProcessDisplayed = false;

    try {
      String sCloudProcessDisplayed = request.getParameter(CLOUD_PROCESS_DISPLAYED);
      if(sCloudProcessDisplayed != null) {
        cloudProcessDisplayed = Boolean.valueOf(sCloudProcessDisplayed);
      }
    }
    catch(Exception e) {
      logger.error("Cloud login: impossible to get parameter " + CLOUD_PROCESS_DISPLAYED, e);
    }
    
    if(user != null && cloudRequestedUri != null) {
      CloudLoginStatus currentStatus = getCloudLoginService().getStatus(user);
      if(CloudLoginStatus.INITIATED.equals(currentStatus)) {
        if(! cloudProcessDisplayed) {
          try {
            request.setAttribute(INITIAL_URI_ATTRIBUTE, cloudRequestedUri);
            
            // Get profile user
            Profile p = Utils.getUserIdentity(user, true).getProfile();
            String firstName = p.getProperty(Profile.FIRST_NAME).toString();
            String lastName = p.getProperty(Profile.LAST_NAME).toString();
            request.setAttribute(USER_PROFILE_FIRSTNAME, firstName);
            request.setAttribute(USER_PROFILE_LASTNAME, lastName);
            
            // Display Screen cloud login
            ServletContext jspContext = request.getSession().getServletContext().getContext("/" + CL_SERVLET_CTX);
            jspContext.getRequestDispatcher(CL_JSP_RESOURCE).include(request, response);
          }
          finally {
            request.removeAttribute(INITIAL_URI_ATTRIBUTE);
          }
        }
        else {
          getCloudLoginService().setStatus(user, CloudLoginStatus.DISPLAYED);
          // Need to redirect to requested uri
          targetUri.append(cloudRequestedUri);
        }
      }
      else {
        // Case of bad status, this is a problem, we redirect to home page
        targetUri.append(HP_SERVLET_CTX);
        logger.error("Cloud login process cannot be lauched because status is " + currentStatus + " and should be " + CloudLoginStatus.INITIATED);
      }
    }
    else {
      // Case of no cloudRequestedUri, this is a problem, we redirect to home page
      targetUri.append(HP_SERVLET_CTX);
    }
    
    if(targetUri.length() > 0) {
      // Redirect
      response.sendRedirect(targetUri.toString());
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }
}
