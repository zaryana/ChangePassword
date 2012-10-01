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

public class TenantCreateHook implements UpdateFinishedHook {

  private final String firstName;

  private final String lastName;

  private final String companyName;

  private final String userMail;

  private final String phone;

  private final String tenant;

  private final String password;

  public TenantCreateHook(Node node) {
    this.firstName = XmlUtils.getChild(node, "first-name").getTextContent();
    this.lastName = XmlUtils.getChild(node, "last-name").getTextContent();
    this.companyName = XmlUtils.getChild(node, "company-name").getTextContent();
    this.userMail = XmlUtils.getChild(node, "user-mail").getTextContent();
    this.phone = XmlUtils.getChild(node, "phone").getTextContent();
    this.tenant = XmlUtils.getChild(node, "tenant").getTextContent();
    this.password = XmlUtils.getChild(node, "password").getTextContent();
  }

  @Override
  public void updateFinished(CurrentAdmin currAdmin) throws InstallerException {
    CloudAdminServices cloudAdminServices = currAdmin.getCloudAdminServices();
    Logger.timePrintln("Creating " + tenant + " tenant...");
    if (!cloudAdminServices.isTenantExists(tenant)) {
      cloudAdminServices.createTenantWithAdminUser(tenant,
                                                   userMail,
                                                   firstName,
                                                   lastName,
                                                   companyName,
                                                   phone,
                                                   password);
    } else {
      Logger.timePrintln("Tenant with name " + tenant + " already exists, skip creation");
    }
  }
}
