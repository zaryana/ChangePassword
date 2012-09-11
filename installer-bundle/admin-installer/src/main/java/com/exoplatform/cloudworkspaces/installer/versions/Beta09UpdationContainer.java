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
package com.exoplatform.cloudworkspaces.installer.versions;

import com.exoplatform.cloudworkspaces.installer.InstallerException;
import com.exoplatform.cloudworkspaces.installer.configuration.AdminDirectories;
import com.exoplatform.cloudworkspaces.installer.configuration.CurrentAdmin;
import com.exoplatform.cloudworkspaces.installer.configuration.PreviousAdmin;
import com.exoplatform.cloudworkspaces.installer.upgrade.AdminUpgradeAlgorithm;
import com.exoplatform.cloudworkspaces.installer.upgrade.Beta09AdminUpgradeAlgorithm;
import com.exoplatform.cloudworkspaces.installer.upgrade.VersionEntry;

public class Beta09UpdationContainer extends Beta08UpdationContainer {

  @Override
  public PreviousAdmin getPreviousAdmin(AdminDirectories prevAdminDirs, VersionEntry version) throws InstallerException {
    return new Beta08Admin(prevAdminDirs, version);
  }

  @Override
  public CurrentAdmin getCurrentAdmin(AdminDirectories currAdminDirs, VersionEntry version) throws InstallerException {
    return new Beta09Admin(currAdminDirs, version);
  }

  public Class<? extends AdminUpgradeAlgorithm> getUpgradeAlgorithm() {
    return Beta09AdminUpgradeAlgorithm.class;
  }

}
