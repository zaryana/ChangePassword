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
package com.exoplatform.cloudworkspaces.admin;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.exoplatform.cloudmanagement.multitenancy.TenantNameResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;

/**
 * Valve configuration: <Valve
 * className="com.exoplatform.cloudworkspaces.admin.ResumeValve"
 * resumeTimeout="30000"
 * serversOverloadUrl="http://h1.exoplatform.org/overloaded.html"
 * resumeRestServiceUrl
 * ="http://localhost:8080/rest/cloud-admin/tenant-service/resume/"/>
 */
public class ResumeValve extends ValveBase {
  private static final Logger LOG          = LoggerFactory.getLogger(ResumeValve.class);

  private String              resumingPage = "http://${tenant.masterhost}/resuming-hide.jsp";

  public String getResumingPage() {
    return resumingPage;
  }

  public void setResumingPage(String resumingPage) {
    this.resumingPage = resumingPage;
  }

  @Override
  public void invoke(Request request, Response response) throws IOException, ServletException {
    String url = request.getRequestURL().toString();
    if (url.contains("/rest/cloud-admin") || url.contains("/rest/private/cloud-admin")) {
      getNext().invoke(request, response);
    } else {
      String tenant = TenantNameResolver.getTenantName(url);

      if (tenant != null) {
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
          String tenantTag = "<span id='tenantname' style='visibility:hidden'>" + tenant
              + "</span>";
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
        getNext().invoke(request, response);
      }
    }
  }

}
