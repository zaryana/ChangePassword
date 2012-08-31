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

import com.exoplatform.cloudworkspaces.installer.ConfigUtils;
import com.exoplatform.cloudworkspaces.installer.InstallerException;
import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationManager;
import com.exoplatform.cloudworkspaces.installer.downloader.BundleDownloader;
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;
import com.exoplatform.cloudworkspaces.installer.rest.AdminException;
import com.exoplatform.cloudworkspaces.installer.rest.CloudAdminServices;
import com.exoplatform.cloudworkspaces.installer.tomcat.AdminTomcatWrapper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Beta07AdminUpgradeAlgorithm extends MinorAdminUpgradeAlgorithm {

  protected String demo;

  public Beta07AdminUpgradeAlgorithm(InteractionManager interaction,
                                     AnswersManager answers,
                                     CloudAdminServices cloudAdminServices,
                                     BundleDownloader bundleDownloader,
                                     AdminTomcatWrapper tomcat,
                                     ConfigurationManager configurationManager) {
    super(interaction, answers, cloudAdminServices, bundleDownloader, tomcat, configurationManager);
  }

  @Override
  public String getVersion() {
    return "1.1.0-Beta07";
  }

  @Override
  public void configurationGenerated(File previousConfDir,
                                     File confDir,
                                     File previousTomcatDir,
                                     File tomcatDir,
                                     File dataDir) {
    // do nothing
  }

  @Override
  public void updateStarted(File confDir, File tomcatDir, File dataDir) {
    // do nothing
  }

  @Override
  public void tomcatStopped(File confDir, File tomcatDir, File dataDir) {
    // do nothing
  }

  @Override
  public void tomcatStarted(File confDir, File tomcatDir, File dataDir) {
    // do nothing
  }

  @Override
  public void newAsReady(File confDir, File tomcatDir, File dataDir) throws AdminException {
    // do nothing
  }

  @Override
  public void updateFinished(File confDir, File tomcatDir, File dataDir) throws InstallerException {
    System.out.println("Removing exoplatform tenant...");
    removeTenant("exoplatform", cloudAdminServices);
    System.out.println("Creating exoplatform tenant...");
    cloudAdminServices.createTenant("exoplatform", "kregent@exoplatform.com");
    try {
      demo = ConfigUtils.findProperty(confDir, "admin.properties", "cloud.admin.demo.tenant.name");
      if (demo == null || demo.isEmpty())
        throw new InstallerException("cloud.admin.demo.tenant.name property not found in admin.properties");
      if (!cloudAdminServices.isTenantExists(demo)) {
        System.out.println("Demo tenant with name " + demo + " not found. Creating " + demo + " tenant...");
        cloudAdminServices.createTenant(demo, "kregent@exoplatform.com");
      }
      else {
        System.out.println("Demo tenant with name " + demo + " found");
      }
    } catch (IOException e) {
      throw new InstallerException("Could not get demo tenant name from admin.properties configuration");
    }
  }

  protected void removeTenant(String tenant, CloudAdminServices cloudAdminServices) throws AdminException {
    Map<String, String> status = cloudAdminServices.tenantStatus(tenant);
    if (!status.isEmpty()) {
      if (status.get("state").equals("ONLINE"))
        cloudAdminServices.tenantStop(tenant);
      cloudAdminServices.tenantRemove(tenant);
    }
  }

}
