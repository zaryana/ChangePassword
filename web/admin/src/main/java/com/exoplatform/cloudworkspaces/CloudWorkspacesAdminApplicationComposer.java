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
package com.exoplatform.cloudworkspaces;

import com.exoplatform.cloudworkspaces.rest.CloudWorkspacesTenantService;
import com.exoplatform.cloudworkspaces.shell.ShellConfigurationService;

import org.everrest.core.ResourceBinder;
import org.exoplatform.cloudmanagement.admin.WorkspacesMailSender;
import org.exoplatform.cloudmanagement.admin.rest.CloudAdminApplicationComposer;
import org.exoplatform.ide.shell.server.CLIResourceFactory;
import org.exoplatform.ide.shell.server.rest.CLIResourcesService;
import org.picocontainer.MutablePicoContainer;

import javax.servlet.ServletContext;

/**
 * cloud-ide.com specific composer
 */
public class CloudWorkspacesAdminApplicationComposer extends CloudAdminApplicationComposer
{

   @Override
   protected void doComposeApplication(MutablePicoContainer container, ServletContext servletContext)
   {
      super.doComposeApplication(container, servletContext);
      container.addComponent(CLIResourceFactory.class);
      container.addComponent(ResourceBinder.class, servletContext.getAttribute(ResourceBinder.class.getName()));
      container.addComponent(WorkspacesMailSender.class);

   }

   @Override
   protected void doComposeRequest(MutablePicoContainer container)
   {
      super.doComposeRequest(container);
      container.addComponent(CloudWorkspacesTenantService.class);
      container.addComponent(ShellConfigurationService.class);
      container.addComponent(CLIResourcesService.class);
      /*
      container.addComponent(TenantCreatorWithEmailAuthorization.class);
      */
   }

}
