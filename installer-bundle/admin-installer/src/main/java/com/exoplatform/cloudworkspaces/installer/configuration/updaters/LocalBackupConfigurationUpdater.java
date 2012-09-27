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
package com.exoplatform.cloudworkspaces.installer.configuration.updaters;

import com.exoplatform.cloudworkspaces.installer.InstallerException;
import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.CurrentAdmin;
import com.exoplatform.cloudworkspaces.installer.configuration.PreviousAdmin;
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class LocalBackupConfigurationUpdater implements ConfigurationUpdater {

  @Override
  public void update(PreviousAdmin prevAdmin,
                     CurrentAdmin currAdmin,
                     InteractionManager interaction,
                     AnswersManager answers) throws InstallerException {
    String[] env = { "EXO_DB_HOST=" + answers.getAnswer("database.url"),
        "EXO_DB_USER=" + answers.getAnswer("database.username"),
        "EXO_DB_PASSWORD=" + answers.getAnswer("database.password"),
        "TENANT_MASTERHOST=" + answers.getAnswer("tenant.masterhost"),
        "CLOUD_AGENT_USERNAME=cloudadmin", "CLOUD_AGENT_PASSWORD=cloudadmin",
        "JAVA_HOME=" + System.getenv("JAVA_HOME") };
    String backupId = execute("./prepare_instance.sh", env, new File("").getAbsoluteFile());
    currAdmin.getAdminConfiguration().set("cloud.admin.tenant.backup.id", backupId);
  }

  private String execute(String cmd, String[] env, File dir) throws InstallerException {
    try {
      Process process = Runtime.getRuntime().exec(cmd, env, dir);
      int result = process.waitFor();
      Scanner in = new Scanner(process.getInputStream());
      StringBuilder message = new StringBuilder();
      while (in.hasNextLine())
        message.append(in.nextLine());
      if (result != 0)
        throw new InstallerException("Executing command " + cmd + " in directory "
            + dir.getAbsolutePath() + " failed with status " + result + " and message " + message);
      return message.toString();
    } catch (IOException e) {
      throw new InstallerException("Error while executing command " + cmd + " in directory "
          + dir.getAbsolutePath(), e);
    } catch (InterruptedException e) {
      throw new InstallerException("Error while executing command " + cmd + " in directory "
          + dir.getAbsolutePath(), e);
    }
  }

}
