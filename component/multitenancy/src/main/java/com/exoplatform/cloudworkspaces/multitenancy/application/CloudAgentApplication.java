/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package com.exoplatform.cloudworkspaces.multitenancy.application;

import com.exoplatform.cloudworkspaces.multitenancy.rest.WorkspaceTenantTemplateService;
import com.exoplatform.cloudworkspaces.multitenancy.rest.WorkspacesCloudAgentInfoService;
import com.exoplatform.cloudworkspaces.initializer.rest.WorkspacesSpaceService;
import com.exoplatform.cloudworkspaces.organization.rest.WorkspacesRESTOrganizationServiceImpl;
import org.exoplatform.cloudmanagement.rest.TenantService;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class CloudAgentApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> cls = new HashSet<Class<?>>(3);
    // cls.add(CloudMailService.class);
    cls.add(WorkspacesCloudAgentInfoService.class);
    cls.add(TenantService.class);
    cls.add(WorkspacesRESTOrganizationServiceImpl.class);
    cls.add(WorkspaceTenantTemplateService.class);
    cls.add(WorkspacesSpaceService.class);
    return cls;
  }

}
