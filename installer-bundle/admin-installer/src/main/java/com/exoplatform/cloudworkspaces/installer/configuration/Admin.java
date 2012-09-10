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
package com.exoplatform.cloudworkspaces.installer.configuration;

import com.exoplatform.cloudworkspaces.installer.InstallerException;
import com.exoplatform.cloudworkspaces.installer.rest.CloudAdminServices;
import com.exoplatform.cloudworkspaces.installer.tomcat.AdminTomcatWrapper;

import java.lang.reflect.InvocationTargetException;

public abstract class Admin implements PreviousAdmin, CurrentAdmin {

  protected final AdminDirectories              adminDirectories;

  protected AdminConfiguration                  adminConfiguration;

  protected Class<? extends CloudAdminServices> cloudAdminServices;

  protected Class<? extends AdminTomcatWrapper> adminTomcatWrapper;

  public Admin(AdminDirectories adminDirectories) {
    this.adminDirectories = adminDirectories;
  }

  public Admin(AdminDirectories adminDirectories,
               AdminConfiguration adminConfiguration,
               Class<? extends CloudAdminServices> cloudAdminServices,
               Class<? extends AdminTomcatWrapper> adminTomcatWrapper) {
    this.adminDirectories = adminDirectories;
    this.adminConfiguration = adminConfiguration;
    this.cloudAdminServices = cloudAdminServices;
    this.adminTomcatWrapper = adminTomcatWrapper;
  }

  public AdminDirectories getAdminDirectories() {
    return adminDirectories;
  }

  public AdminConfiguration getAdminConfiguration() {
    return adminConfiguration;
  }

  public CloudAdminServices getCloudAdminServices() throws InstallerException {
    try {
      return cloudAdminServices.getConstructor(Admin.class).newInstance(this);
    } catch (SecurityException e) {
      throw new InstallerException("Exception while creating CloudAdminServices instance", e);
    } catch (InstantiationException e) {
      throw new InstallerException("Exception while creating CloudAdminServices instance", e);
    } catch (IllegalAccessException e) {
      throw new InstallerException("Exception while creating CloudAdminServices instance", e);
    } catch (InvocationTargetException e) {
      throw new InstallerException("Exception while creating CloudAdminServices instance", e);
    } catch (NoSuchMethodException e) {
      throw new InstallerException("Exception while creating CloudAdminServices instance", e);
    }
  }

  public AdminTomcatWrapper getAdminTomcatWrapper() throws InstallerException {
    try {
      return adminTomcatWrapper.getConstructor(Admin.class).newInstance(this);
    } catch (SecurityException e) {
      throw new InstallerException("Exception while creating AdminTomcatWrapper instance", e);
    } catch (InstantiationException e) {
      throw new InstallerException("Exception while creating AdminTomcatWrapper instance", e);
    } catch (IllegalAccessException e) {
      throw new InstallerException("Exception while creating AdminTomcatWrapper instance", e);
    } catch (InvocationTargetException e) {
      throw new InstallerException("Exception while creating AdminTomcatWrapper instance", e);
    } catch (NoSuchMethodException e) {
      throw new InstallerException("Exception while creating AdminTomcatWrapper instance", e);
    }
  }
}
