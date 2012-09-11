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

import com.exoplatform.cloudworkspaces.installer.configuration.AdminConfigurationManager;
import com.exoplatform.cloudworkspaces.installer.configuration.CurrentAdmin;
import com.exoplatform.cloudworkspaces.installer.configuration.PreviousAdmin;
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;

public class Beta09AdminUpgradeAlgorithm extends Beta08AdminUpgradeAlgorithm {

  public Beta09AdminUpgradeAlgorithm(PreviousAdmin prevAdmin,
                                     CurrentAdmin currAdmin,
                                     InteractionManager interaction,
                                     AnswersManager answers,
                                     AdminConfigurationManager configurationManager) {
    super(prevAdmin, currAdmin, interaction, answers, configurationManager);
  }

  @Override
  public String getVersion() {
    return "1.1.0-Beta09";
  }

}
