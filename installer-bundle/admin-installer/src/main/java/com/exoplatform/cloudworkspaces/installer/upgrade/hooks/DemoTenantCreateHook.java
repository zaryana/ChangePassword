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
package com.exoplatform.cloudworkspaces.installer.upgrade.hooks;

import com.exoplatform.cloudworkspaces.installer.InstallerException;
import com.exoplatform.cloudworkspaces.installer.XmlUtils;
import com.exoplatform.cloudworkspaces.installer.configuration.CurrentAdmin;
import com.exoplatform.cloudworkspaces.installer.rest.CloudAdminServices;
import com.exoplatform.cloudworkspaces.installer.upgrade.Hook.UpdateFinishedHook;
import com.exoplatform.cloudworkspaces.installer.upgrade.Logger;

import org.w3c.dom.Node;

public class DemoTenantCreateHook implements UpdateFinishedHook {

  private final String email;

  public DemoTenantCreateHook(Node node) {
    this.email = XmlUtils.getChild(node, "email").getTextContent();
  }

  @Override
  public void updateFinished(CurrentAdmin currAdmin) throws InstallerException {
    String tenant = currAdmin.getAdminConfiguration().get("cloud.admin.demo.tenant.name");
    CloudAdminServices cloudAdminServices = currAdmin.getCloudAdminServices();
    if (!cloudAdminServices.isTenantExists(tenant)) {
      Logger.timePrintln("Creating " + tenant + " tenant with owner " + email);
      cloudAdminServices.createTenant(tenant, email);
    } else {
      Logger.timePrintln("Tenant with name " + tenant + " already exists, skip creation");
    }
  }

}
