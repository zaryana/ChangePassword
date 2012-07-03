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

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.web.filter.Filter;

import com.exoplatform.cloudworkspaces.cloudlogin.data.CloudLoginStatus;

/**
 * Filter used after user login to launch cloud login process depending cloud login status 
 * 
 * @author Clement
 *
 */
public class CloudLoginFilter implements Filter {

  private static final String LOGIN_URI = "/login";
  private static final String DOLOGIN_URI = "/dologin";
  private static final String REST_URI = "rest";

  private CloudLoginService cloudLoginService;
  public CloudLoginService getCloudLoginService() {
    if(cloudLoginService == null) {
      cloudLoginService = (CloudLoginService)PortalContainer.getInstance().getComponentInstanceOfType(CloudLoginService.class);
    }
    return cloudLoginService;
  }

  public CloudLoginFilter() {}

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest)request;
    HttpServletResponse httpServletResponse = (HttpServletResponse)response;
    
    String user = httpServletRequest.getRemoteUser();
    String requestUri = httpServletRequest.getRequestURI();
    String loginRequestUri = httpServletRequest.getContextPath() + LOGIN_URI;
    String dologinRequestUri = httpServletRequest.getContextPath() + DOLOGIN_URI;
    boolean isLoginUri = (requestUri.contains(loginRequestUri) || requestUri.contains(dologinRequestUri));
    boolean isRestUri = (requestUri.contains(REST_URI));
    
    if(! isLoginUri && !isRestUri && CloudLoginStatus.INITIATED.equals(getCloudLoginService().getStatus(user))) {

      // Get full url
      String reqUri = httpServletRequest.getRequestURI().toString();
      String queryString = httpServletRequest.getQueryString();
      if (queryString != null) {
          reqUri += "?"+queryString;
      }

      // Get extension servlet context (because this filter is not on same context than configuration of servlet we need to forward)
      ServletContext extensionContext = httpServletRequest.getSession().getServletContext().getContext("/" + CloudLoginServlet.CL_SERVLET_CTX);
      
      // Forward to step1 of cloud login with request_uri into parameters
      String uriTarget = CloudLoginServlet.CL_SERVLET_URL + "?" + CloudLoginServlet.CLOUD_REQUESTED_URI + "=" + reqUri;
      extensionContext.getRequestDispatcher(uriTarget).forward(httpServletRequest, httpServletResponse);
    }
    
    chain.doFilter(request, response);
  }
}
