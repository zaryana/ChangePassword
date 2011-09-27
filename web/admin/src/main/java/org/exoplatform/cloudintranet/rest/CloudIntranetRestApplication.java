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
package org.exoplatform.cloudintranet.rest;

import java.util.HashSet;
import java.util.Set;

import org.exoplatform.cloudmanagement.admin.rest.CloudAdminExceptionMapper;
import org.exoplatform.cloudmanagement.admin.rest.CloudAdminInfoService;
import org.exoplatform.cloudmanagement.admin.rest.TenantCreatorWithCloudAdminRole;
import org.exoplatform.cloudmanagement.admin.rest.TenantResumingService;

import javax.ws.rs.core.Application;

/**
 * Cloud-admin REST application initializer.
 */
public class CloudIntranetRestApplication extends Application
{
   /**
    * @see javax.ws.rs.core.Application#getClasses()
    */
   @Override
   public Set<Class<?>> getClasses()
   {
      Set<Class<?>> cls = new HashSet<Class<?>>(4);
      cls.add(IntranetAdminService.class);
      cls.add(TenantCreatorWithCloudAdminRole.class);
      cls.add(CloudAdminInfoService.class);
      cls.add(TenantResumingService.class);
      return cls;
   }

   @Override
   public Set<Object> getSingletons()
   {
      Set<Object> objs = new HashSet<Object>(1);
      objs.add(new CloudAdminExceptionMapper());
      return objs;
   }

}
