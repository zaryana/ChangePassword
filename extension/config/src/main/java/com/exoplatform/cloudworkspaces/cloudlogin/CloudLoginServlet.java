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

import com.exoplatform.cloudworkspaces.cloudlogin.CloudLoginService;
import com.exoplatform.cloudworkspaces.cloudlogin.data.CloudLoginStatus;

/**
 * Servlet to manage cloud login screens
 * <p>
 * If there is a problem, forward to context path uri
 * 
 * @author Clement
 *
 */
public class CloudLoginServlet extends HttpServlet {
  private static final long serialVersionUID = -8682277458138577779L;

  private static final Log logger = ExoLogger.getLogger(CloudLoginServlet.class);

  private static final String JSP_SERVLET_CTX = "/cloud";
  
  public static final String CL_SERVLET_URL = "/cloudlogin";
  public static final String CL_JSP_RESOURCE = "/StartedStep.jsp";
  public static final String CLOUD_REQUESTED_URI = "cloudRequestUri";
  public static final String CLOUD_STEP_TO = "step";
  public static final String CLOUD_PROCESS_DISPLAYED = "cloudProcessDisplayed";
  public static final String INITIAL_URI_ATTRIBUTE = "org.cloudworkspaces.login.initial_uri";
  
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
    Integer cloudStepTo = 1;
    Boolean cloudProcessDisplayed = false;
    try {
      String sStepTo = request.getParameter(CLOUD_STEP_TO);
      if(sStepTo != null) {
        cloudStepTo = Integer.valueOf(sStepTo);
      }
    }
    catch(Exception e) {
      logger.error("Cloud login: impossible to get parameter " + CLOUD_STEP_TO, e);
    }
    try {
      String sCloudProcessDisplayed = request.getParameter(CLOUD_PROCESS_DISPLAYED);
      if(sCloudProcessDisplayed != null) {
        cloudProcessDisplayed = Boolean.valueOf(sCloudProcessDisplayed);
      }
    }
    catch(Exception e) {
      logger.error("Cloud login: impossible to get parameter " + CLOUD_PROCESS_DISPLAYED, e);
    }
    
    if(cloudRequestedUri != null) {
      // Set attribute to propagate requested uri
      request.setAttribute(CLOUD_REQUESTED_URI, cloudRequestedUri);
      
      CloudLoginStatus currentStatus = getCloudLoginService().getStatus(user);
      if(CloudLoginStatus.INITIATED.equals(currentStatus)) {
        if(! cloudProcessDisplayed) {
          // need to display Steps (1,2,3)
          /*StringBuffer targetJsp = new StringBuffer(CL_JSP_RESOURCE);
          if(cloudStepTo != null && cloudStepTo > 0 && cloudStepTo < 4) {
            targetJsp.append(cloudStepTo).append(".jsp");
          }
          else {
            // by default, screen 1
            targetJsp.append("1.jsp");
          }*/

          try {
            request.setAttribute(INITIAL_URI_ATTRIBUTE, cloudRequestedUri);
            // Display Screen
            ServletContext jspContext = request.getSession().getServletContext().getContext(JSP_SERVLET_CTX);
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
        logger.error("Cloud login process cannot be lauched because status is " + currentStatus + " and should be " + CloudLoginStatus.INITIATED);
      }
    }
    else {
      // Case of no cloudRequestedUri, this is a problem, we redirect to context path
      // TODO: HOME PAGE
      targetUri.append(request.getContextPath());
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
