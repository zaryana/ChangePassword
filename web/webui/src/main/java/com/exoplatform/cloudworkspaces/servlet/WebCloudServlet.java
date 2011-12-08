package com.exoplatform.cloudworkspaces.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet used to access JSP situated into WEB-INF folder (for security reason)
 * 
 * @author Clement
 *
 */
public class WebCloudServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  
  private static final String REDIRECTION_PARAM = "to";
  private static final String JSP_LOCATION = "/WEB-INF/jsp/";
  private static final String JSP_EXTENSION = ".jsp";

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String paramTo = request.getParameter(REDIRECTION_PARAM);
    if(paramTo != null) {
      // Include asked JSP
      request.getRequestDispatcher(JSP_LOCATION + paramTo + JSP_EXTENSION).include(request, response);
    }
    else {
      // Redirest to the home
      response.sendRedirect(request.getContextPath());
    }
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }
}
