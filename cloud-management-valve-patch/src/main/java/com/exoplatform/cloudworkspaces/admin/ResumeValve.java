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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.servlet.ServletException;

/**
 * Valve configuration: <Valve
 * className="com.exoplatform.cloudworkspaces.admin.ResumeValve"
 * resumeTimeout="30000"
 * serversOverloadUrl="http://h1.exoplatform.org/overloaded.html"
 * resumeRestServiceUrl
 * ="http://localhost:8080/rest/cloud-admin/tenant-service/resume/"/>
 * 
 */
public class ResumeValve extends ValveBase
{
   private static final Logger LOG = LoggerFactory.getLogger(ResumeValve.class);

   private int resumeTimeout = 30 * 1000;

   private String serversOverloadUrl = "http://h1.exoplatform.org/overloaded.html";

   private String resumeRestServiceUrl = "http://localhost:8080/rest/cloud-admin/tenant-service/resume";

   public int getResumeTimeout()
   {
      return resumeTimeout;
   }

   public void setResumeTimeout(int resumeTimeout)
   {
      this.resumeTimeout = resumeTimeout;
   }

   public String getServersOverloadUrl()
   {
      return serversOverloadUrl;
   }

   public void setServersOverloadUrl(String serversOverloadUrl)
   {
      this.serversOverloadUrl = serversOverloadUrl;
   }

   public String getResumeRestServiceUrl()
   {
      return resumeRestServiceUrl;
   }

   public void setResumeRestServiceUrl(String resumeRestServiceUrl)
   {
      this.resumeRestServiceUrl = resumeRestServiceUrl;
   }

   @Override
   public void invoke(Request request, Response response) throws IOException, ServletException
   {
      String url = request.getRequestURL().toString();
      if (url.contains("/rest/cloud-admin") || url.contains("/rest/private/cloud-admin"))
      {
         getNext().invoke(request, response);
      }
      else
      {
         String tenant = TenantNameResolver.getTenantName(url);

         if (tenant != null)
         {
            // resume tenant
            String strUrl = resumeRestServiceUrl + "?tenant=" + tenant;
            URL restUrl;
            HttpURLConnection connection = null;

            restUrl = new URL(strUrl.toString());

            try
            {
               connection = (HttpURLConnection)restUrl.openConnection();
               connection.setConnectTimeout(resumeTimeout);
               connection.setReadTimeout(resumeTimeout);
               connection.setRequestMethod("GET");

               connection.connect();

               // read Response
               if (connection.getResponseCode() == 200)
               {
                  // tenant resumed successfully
                  StringBuilder urlBuilder = new StringBuilder();
                  urlBuilder.append("http://");
                  urlBuilder.append(request.getHeader("HOST"));
                  urlBuilder.append(request.getRequestURI());

                  response.sendRedirect(url);
               }
               else
               {
                  LOG.warn("Tenant {} resuming failed with status {} by path {}",
                     new String[]{tenant, Integer.toString(connection.getResponseCode()), strUrl});
                  response.sendRedirect(serversOverloadUrl);
               }
            }
            catch (SocketTimeoutException e)
            {
               LOG.error(e.getLocalizedMessage(), e);
               response.sendRedirect(serversOverloadUrl);
            }
         }
         else
         {
            getNext().invoke(request, response);
         }
      }
   }

}
