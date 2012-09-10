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
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;

import java.util.List;

public class AdminConfigurationManager {

  protected final PreviousAdmin                    prevAdmin;

  protected final CurrentAdmin                     currAdmin;

  protected final List<ConfigurationUpdater> updaters;

  protected final InteractionManager         interactionManager;

  protected final AnswersManager             answersManager;

  public AdminConfigurationManager(PreviousAdmin prevAdmin,
                                   CurrentAdmin currAdmin,
                                   List<ConfigurationUpdater> updaters,
                                   InteractionManager interactionManager,
                                   AnswersManager answersManager) {
    this.prevAdmin = prevAdmin;
    this.currAdmin = currAdmin;
    this.updaters = updaters;
    this.interactionManager = interactionManager;
    this.answersManager = answersManager;
  }

  public void configure() throws InstallerException {
    for (ConfigurationUpdater updater : updaters) {
      updater.update(prevAdmin, currAdmin, interactionManager, answersManager);
    }
  }

}
