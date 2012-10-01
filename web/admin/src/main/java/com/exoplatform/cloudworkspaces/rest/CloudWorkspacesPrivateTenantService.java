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
import com.exoplatform.cloud.admin.rest.TenantCreator;
import com.exoplatform.cloudworkspaces.RequestState;
import com.exoplatform.cloudworkspaces.UserRequest;
import com.exoplatform.cloudworkspaces.UserRequestDAO;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/cloud-admin/cloudworkspaces/private-tenant-service")
public class CloudWorkspacesPrivateTenantService {

  private final TenantCreator  tenantCreator;

  private final UserRequestDAO requestDao;

  public CloudWorkspacesPrivateTenantService(TenantCreator tenantCreator, UserRequestDAO requestDao) {
    this.tenantCreator = tenantCreator;
    this.requestDao = requestDao;
  }

  @POST
  @Path("/create")
  @RolesAllowed({ "cloud-admin" })
  public Response create(@FormParam("tenant") String tenant,
                         @FormParam("user-mail") String userMail,
                         @FormParam("first-name") String firstName,
                         @FormParam("last-name") String lastName,
                         @FormParam("company-name") String companyName,
                         @FormParam("phone") String phone,
                         @FormParam("password") String password) throws CloudAdminException {
    String uuid = tenantCreator.createTenant(tenant, userMail);
    tenantCreator.createTenantWithConfirmedEmail(uuid);
    UserRequest reques = new UserRequest("",
                                         tenant,
                                         userMail,
                                         firstName,
                                         lastName,
                                         companyName,
                                         phone,
                                         password,
                                         uuid,
                                         true,
                                         RequestState.WAITING_JOIN);
    requestDao.put(reques);
    return Response.ok().build();
  }

}
