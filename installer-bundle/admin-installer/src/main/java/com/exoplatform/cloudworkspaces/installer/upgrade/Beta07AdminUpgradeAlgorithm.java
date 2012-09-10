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
package com.exoplatform.cloudworkspaces.installer.upgrade;

import com.exoplatform.cloudworkspaces.installer.InstallerException;
import com.exoplatform.cloudworkspaces.installer.configuration.AdminConfigurationManager;
import com.exoplatform.cloudworkspaces.installer.configuration.CurrentAdmin;
import com.exoplatform.cloudworkspaces.installer.configuration.PreviousAdmin;
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;
import com.exoplatform.cloudworkspaces.installer.rest.CloudAdminServices;

public class Beta07AdminUpgradeAlgorithm extends MinorAdminUpgradeAlgorithm {

  public Beta07AdminUpgradeAlgorithm(PreviousAdmin prevAdmin,
                                     CurrentAdmin currAdmin,
                                     InteractionManager interaction,
                                     AnswersManager answers,
                                     AdminConfigurationManager configurationManager) {
    super(prevAdmin, currAdmin, interaction, answers, configurationManager);
  }

  @Override
  public String getVersion() {
    return "1.1.0-Beta07";
  }

  @Override
  public void configurationGenerated(PreviousAdmin prevAdmin, CurrentAdmin currAdmin) {
    // do nothing
  }

  @Override
  public void updateStarted(CurrentAdmin currAdmin) {
    // do nothing
  }

  @Override
  public void tomcatStopped(CurrentAdmin currAdmin) {
    // do nothing
  }

  @Override
  public void tomcatStarted(CurrentAdmin currAdmin) {
    // do nothing
  }

  @Override
  public void newAsReady(CurrentAdmin currAdmin) {
    // do nothing
  }

  @Override
  public void updateFinished(CurrentAdmin currAdmin) throws InstallerException {
    CloudAdminServices cloudAdminServices = currAdmin.getCloudAdminServices();
    logger.timePrintln("Removing exoplatform tenant...");
    removeTenant("exoplatform", cloudAdminServices);
    logger.timePrintln("Creating exoplatform tenant...");
    cloudAdminServices.createTenant("exoplatform", "kregent@exoplatform.com");
    String demo = currAdmin.getAdminConfiguration().get("cloud.admin.demo.tenant.name");
    if (demo == null || demo.isEmpty())
      throw new InstallerException("cloud.admin.demo.tenant.name property not found in admin.properties");
    if (!cloudAdminServices.isTenantExists(demo)) {
      logger.timePrintln("Demo tenant with name " + demo + " not found. Creating " + demo
          + " tenant...");
      cloudAdminServices.createTenant(demo, "kregent@exoplatform.com");
    } else {
      logger.timePrintln("Demo tenant with name " + demo + " found");
    }
  }

}
