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
package com.exoplatform.cloudworkspaces.listener;

import com.exoplatform.cloudworkspaces.users.UserLimitsStorage;
import com.exoplatform.cloudworkspaces.users.UsersManager;

import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.picocontainer.Startable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class UserLimitSupervisor implements Startable {

  private static final Logger LOG = LoggerFactory.getLogger(UserLimitSupervisor.class);

  private final Timer         supervisorTimer;

  public UserLimitSupervisor(UserLimitsStorage userLimitsStorage, UsersManager usersManager) {
    this(userLimitsStorage, usersManager, 120000);
  }

  public UserLimitSupervisor(final UserLimitsStorage userLimitsStorage,
                             final UsersManager usersManager,
                             final long delay) {

    this.supervisorTimer = new Timer(true);
    this.supervisorTimer.schedule(new TimerTask() {

      private long previousLastModifiedTime;

      @Override
      public void run() {
        try {
          if (userLimitsStorage.getLastModifiedTime() > previousLastModifiedTime) {
            previousLastModifiedTime = userLimitsStorage.getLastModifiedTime();
            usersManager.joinAll();
          }
        } catch (CloudAdminException e) {
          LOG.error("Exception in limit listener", e);
        }
      }

    }, 0, delay);
  }

  @Override
  public void start() {
    // do nothing
  }

  @Override
  public void stop() {
    supervisorTimer.cancel();
  }

}
