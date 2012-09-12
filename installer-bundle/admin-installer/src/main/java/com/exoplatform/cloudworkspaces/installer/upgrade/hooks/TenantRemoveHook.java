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
import com.exoplatform.cloudworkspaces.installer.upgrade.Logger;
import com.exoplatform.cloudworkspaces.installer.upgrade.UpdateFinishedHook;

import org.w3c.dom.Node;

import java.util.Map;

public class TenantRemoveHook implements UpdateFinishedHook {

  private final String tenant;

  public TenantRemoveHook(Node node) {
    this.tenant = XmlUtils.getChild(node, "tenant").getTextContent();
  }

  @Override
  public void updateFinished(CurrentAdmin currAdmin) throws InstallerException {
    CloudAdminServices cloudAdminServices = currAdmin.getCloudAdminServices();
    Logger.timePrintln("Removing exoplatform tenant...");
    Map<String, String> status = cloudAdminServices.tenantStatus(tenant);
    if (!status.isEmpty()) {
      if (status.get("state").equals("ONLINE"))
        cloudAdminServices.tenantStop(tenant);
      cloudAdminServices.tenantRemove(tenant);
    }
  }

}
