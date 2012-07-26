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
package com.exoplatform.cloudworkspaces.listeners;

import com.exoplatform.cloud.admin.CloudAdminException;
import com.exoplatform.cloudworkspaces.listener.UserLimitSupervisor;
import com.exoplatform.cloudworkspaces.users.UserLimitsStorage;
import com.exoplatform.cloudworkspaces.users.UsersManager;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestUserLimitSupervisor {

  long                DELAY = 500;

  UserLimitsStorage   userLimitsStorage;

  UsersManager        usersManager;

  UserLimitSupervisor userLimitSupervisor;

  @BeforeMethod
  public void initMocks() {
    this.userLimitsStorage = Mockito.mock(UserLimitsStorage.class);
    this.usersManager = Mockito.mock(UsersManager.class);

  }

  @Test
  public void testCheckUserLimitsStorage() throws InterruptedException {
    this.userLimitSupervisor = new UserLimitSupervisor(userLimitsStorage, usersManager, DELAY);

    Thread.sleep(DELAY / 10);

    Mockito.verify(userLimitsStorage, Mockito.times(1)).getLastModifiedTime();

    Thread.sleep(DELAY);

    Mockito.verify(userLimitsStorage, Mockito.times(2)).getLastModifiedTime();
  }

  @Test
  public void testStopThreadWhenStopMethodInvoked() throws InterruptedException {
    this.userLimitSupervisor = new UserLimitSupervisor(userLimitsStorage, usersManager, DELAY);

    Thread.sleep(DELAY / 10);

    Mockito.verify(userLimitsStorage, Mockito.times(1)).getLastModifiedTime();

    userLimitSupervisor.stop();

    Thread.sleep(DELAY);

    Mockito.verifyNoMoreInteractions(userLimitsStorage);
  }

  @Test
  public void testNoCallJoinAllIfUserLimitsStorageNotChanged() throws InterruptedException {
    this.userLimitSupervisor = new UserLimitSupervisor(userLimitsStorage, usersManager, DELAY);

    Thread.sleep(DELAY / 10);

    Mockito.verify(userLimitsStorage).getLastModifiedTime();
    Mockito.verifyZeroInteractions(usersManager);
  }

  @Test
  public void testCallJoinAllIfUserLimitsStorageChanged() throws InterruptedException,
                                                         CloudAdminException {
    Mockito.when(userLimitsStorage.getLastModifiedTime()).thenReturn(12345L);
    this.userLimitSupervisor = new UserLimitSupervisor(userLimitsStorage, usersManager, DELAY);

    Thread.sleep(DELAY / 10);

    Mockito.verify(usersManager).joinAll();
  }

}
