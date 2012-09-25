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
import com.exoplatform.cloudworkspaces.installer.configuration.CurrentAdmin;
import com.exoplatform.cloudworkspaces.installer.configuration.PreviousAdmin;

public interface Hook {
  
  public static interface BeforeConfigurationGeneratingHook extends Hook {
    public void beforeConfigurationGenerating(PreviousAdmin prevAdmin, CurrentAdmin currAdmin) throws InstallerException;
  }

  public static interface ConfigurationGeneratedHook extends Hook {
    public void configurationGenerated(PreviousAdmin prevAdmin, CurrentAdmin currAdmin) throws InstallerException;
  }

  public interface NewAsReadyHook extends Hook {
    public void newAsReady(CurrentAdmin currAdmin) throws InstallerException;
  }

  public interface TomcatStartedHook extends Hook {
    public void tomcatStarted(CurrentAdmin currAdmin) throws InstallerException;
  }

  public interface TomcatStoppedHook extends Hook {
    public void tomcatStopped(CurrentAdmin currAdmin) throws InstallerException;
  }

  public interface UpdateFinishedHook extends Hook {
    public void updateFinished(CurrentAdmin currAdmin) throws InstallerException;
  }

  public interface UpdateStartedHook extends Hook {
    public void updateStarted(CurrentAdmin currAdmin) throws InstallerException;
  }

}
