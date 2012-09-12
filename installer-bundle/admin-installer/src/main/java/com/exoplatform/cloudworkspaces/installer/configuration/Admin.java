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
import com.exoplatform.cloudworkspaces.installer.upgrade.VersionEntry;

import java.lang.reflect.InvocationTargetException;

public class Admin implements PreviousAdmin, CurrentAdmin {

  protected final AdminDirectories                    adminDirectories;

  protected final AdminConfiguration                  adminConfiguration;

  protected final Class<? extends CloudAdminServices> cloudAdminServices;

  protected final Class<? extends AdminTomcatWrapper> adminTomcatWrapper;

  protected final VersionEntry                        version;

  public Admin(AdminDirectories adminDirectories, VersionEntry version) throws InstallerException {
    this.adminDirectories = adminDirectories;
    this.adminConfiguration = new AdminConfiguration(this.adminDirectories,
                                                     version.getConfigurationParameters());
    this.cloudAdminServices = version.getCloudAdminServicesImpl();
    this.adminTomcatWrapper = version.getAdminTomcatWrapperImpl();
    this.version = version;
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

  @Override
  public VersionEntry getVersionEntry() {
    return version;
  }
}
