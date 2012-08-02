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
package com.exoplatform.cloudworkspaces;

import com.exoplatform.cloud.multitenancy.TenantNameResolver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class WorkspacesTenantStartFilter implements Filter {

  private String resumingPage = "http://${tenant.masterhost}/resuming-hide.jsp";

  private String notFoundPage = "http://${tenant.masterhost}/home.jsp";

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    String resumingPageParam = filterConfig.getInitParameter("resumingPage");
    if (resumingPageParam != null && !resumingPageParam.trim().isEmpty()) {
      this.resumingPage = resumingPageParam;
    }
    String notFoundPageParam = filterConfig.getInitParameter("notFoundPage");
    if (notFoundPageParam != null && !notFoundPageParam.trim().isEmpty()) {
      this.notFoundPage = notFoundPageParam;
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                                                                                           ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String url = httpRequest.getRequestURL().toString();
    if (url.contains("/rest/cloud-admin") || url.contains("/rest/private/cloud-admin")) {
      chain.doFilter(httpRequest, response);
    } else {
      String tenant = TenantNameResolver.getTenantName(url);

      if (tenant != null) {

        if (url.contains("/rest/cloud-agent/info-service/is-ready/")) {
          OutputStream stream = response.getOutputStream();
          try {
            stream.write("false".getBytes());
          } finally {
            stream.close();
          }
          return;
        }

        // resume tenant
        HttpURLConnection connection = null;
        URL resuming = new URL(resumingPage.replace("${tenant.masterhost}",
                                                    System.getProperty("tenant.masterhost")));
        connection = (HttpURLConnection) resuming.openConnection();
        connection.setRequestMethod("GET");

        connection.connect();

        if (connection.getResponseCode() == 200) {
          InputStream content = (InputStream) connection.getContent();
          ByteArrayOutputStream bout = new ByteArrayOutputStream();
          try {
            int length = 0;
            byte[] buf = new byte[10 * 1024];
            while (length >= 0) {
              bout.write(buf, 0, length);
              length = content.read(buf);
            }
          } finally {
            content.close();
            bout.close();
          }
          String html = new String(bout.toByteArray());
          String headTemplate = "<head>";
          String baseTag = "<base href=\"http://" + System.getProperty("tenant.masterhost")
              + "\"></base>";
          String tenantTag = "<span id='tenantname' style='display: none'>" + tenant + "</span>";
          String contactUsFrom = "onclick=\"showContactUsForm('/contact-us.jsp');\"";
          String contactUsTo = "target=\"blank\" href=\"/index.jsp\"";

          OutputStream stream = response.getOutputStream();
          try {
            int head = html.indexOf(headTemplate);
            stream.write(html.substring(0, head + headTemplate.length()).getBytes());
            stream.write(baseTag.getBytes());
            stream.write(tenantTag.getBytes());
            int contactUs = html.indexOf(contactUsFrom);
            stream.write(html.substring(head + headTemplate.length(), contactUs).getBytes());
            stream.write(contactUsTo.getBytes());
            stream.write(html.substring(contactUs + contactUsFrom.length()).getBytes());
          } finally {
            stream.close();
          }
        }

      } else {
        chain.doFilter(httpRequest, response);
      }
    }
  }

  @Override
  public void destroy() {
    // do nothing
  }

}
