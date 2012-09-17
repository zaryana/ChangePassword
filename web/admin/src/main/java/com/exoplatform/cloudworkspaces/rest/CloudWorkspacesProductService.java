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
package com.exoplatform.cloudworkspaces.rest;

import com.exoplatform.cloud.admin.CloudAdminException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/cloud-admin/cloudworkspaces/product-service")
public class CloudWorkspacesProductService {

  private final String COMPANY = "eXo Platform SAS";

  private final String PRODUCT = "cloud-workspaces";

  private final String VERSION;

  private final String VERSION_HASH;

  public CloudWorkspacesProductService() throws CloudAdminException {
    try {
      InputStream version = Thread.currentThread()
                                  .getContextClassLoader()
                                  .getResourceAsStream("version");
      Properties properties = new Properties();
      properties.load(version);
      VERSION = properties.getProperty("version");
      VERSION_HASH = properties.getProperty("version.hash");
    } catch (IOException e) {
      throw new CloudAdminException("Couldn't found version properties file");
    }
  }

  @GET
  @Path("/version-only")
  @RolesAllowed({ "cloud-admin", "cloud-manager" })
  public String getVersion() {
    return VERSION;
  }

  @GET
  @Path("/version")
  @Produces("application/json")
  @RolesAllowed({ "cloud-admin", "cloud-manager" })
  public Map<String, String> getFullVersion() {
    Map<String, String> result = new HashMap<String, String>();
    result.put("company", COMPANY);
    result.put("product", PRODUCT);
    result.put("version", VERSION);
    result.put("version-hash", VERSION_HASH);
    return result;
  }

}
