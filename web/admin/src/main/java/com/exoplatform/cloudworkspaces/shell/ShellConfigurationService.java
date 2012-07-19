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
package com.exoplatform.cloudworkspaces.shell;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

/**
 * This service gives all necessary information for shell application start.
 */
@Path("/cloud-admin/shell")
public class ShellConfigurationService {
  @GET
  @Path("/user")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> getCurrentUserName(@Context SecurityContext securityContext) {
    String userName = securityContext.getUserPrincipal().getName();
    ShellUser shellUser = new ShellUser(userName);
    Map<String, Object> result = new HashMap<String, Object>(1);
    result.put("user", shellUser);

    return result;
  }
}
