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

import com.exoplatform.cloudworkspaces.http.WorkspacesOrganizationRequestPerformer;
import com.exoplatform.cloudworkspaces.instance.WorkspacesUserDataGenerator;
import com.exoplatform.cloudworkspaces.listener.AsyncTenantStarter;
import com.exoplatform.cloudworkspaces.listener.JoinAllInOnlineServerListener;
import com.exoplatform.cloudworkspaces.listener.TenantCreatedListener;
import com.exoplatform.cloudworkspaces.listener.UserLimitSupervisor;
import com.exoplatform.cloudworkspaces.listener.WorkspacesServerOnlineListenersInvoker;
import com.exoplatform.cloudworkspaces.rest.CloudWorkspacesTenantService;
import com.exoplatform.cloudworkspaces.shell.ShellConfigurationService;
import com.exoplatform.cloudworkspaces.users.UserLimitsStorage;
import com.exoplatform.cloudworkspaces.users.UsersManager;

import org.everrest.core.ResourceBinder;
import org.exoplatform.cloudmanagement.admin.WorkspacesMailSender;
import org.exoplatform.cloudmanagement.admin.instance.UserDataGenerator;
import org.exoplatform.cloudmanagement.admin.instance.autoscaling.AutoscalingAlgorithm;
import org.exoplatform.cloudmanagement.admin.instance.autoscaling.WorkspacesFreeSpaceRatioAutoscalingAlgorithm;
import org.exoplatform.cloudmanagement.admin.mail.TenantOperationMailSenderInitiator;
import org.exoplatform.cloudmanagement.admin.mail.WorkspacesTenantOperationMailSenderInitiator;
import org.exoplatform.cloudmanagement.admin.rest.CloudAdminApplicationComposer;
import org.exoplatform.cloudmanagement.admin.rest.TenantCreator;
import org.exoplatform.cloudmanagement.admin.status.ServerOnlineListenersInvoker;
import org.exoplatform.ide.shell.server.CLIResourceFactory;
import org.exoplatform.ide.shell.server.rest.CLIResourcesService;
import org.picocontainer.MutablePicoContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exoplatform.cloudworkspaces.StatisticAllTenants;

import javax.servlet.ServletContext;

/**
 * cloud-ide.com specific composer
 */
public class CloudWorkspacesAdminApplicationComposer extends CloudAdminApplicationComposer {

  private static final Logger LOG = LoggerFactory.getLogger(CloudWorkspacesAdminApplicationComposer.class);

  @Override
  protected void doComposeApplication(MutablePicoContainer container, ServletContext servletContext) {
    super.doComposeApplication(container, servletContext);
    container.addComponent(CLIResourceFactory.class);
    container.addComponent(ResourceBinder.class,
                           servletContext.getAttribute(ResourceBinder.class.getName()));

    container.addComponent(WorkspacesMailSender.class);

    container.addComponent(UserLimitsStorage.class);
    container.addComponent(WorkspacesOrganizationRequestPerformer.class);
    container.addComponent(NotificationMailSender.class);
    container.addComponent(ReferencesManager.class);
    container.addComponent(CloudIntranetUtils.class);
    container.addComponent(UserRequestDAO.class);

    container.addComponent(TenantCreatedListener.class);

    container.addComponent(UserLimitSupervisor.class);

    container.removeComponent(ServerOnlineListenersInvoker.class);
    container.addComponent(JoinAllInOnlineServerListener.class);
    container.addComponent(WorkspacesServerOnlineListenersInvoker.class);

    container.addComponent(AsyncTenantStarter.class);

    container.addComponent(UsersManager.class);
    container.addComponent(EmailBlacklist.class);
    container.addComponent(ChangePasswordManager.class);

    container.addComponent(TenantCreator.class);

    container.removeComponent(AutoscalingAlgorithm.class);
    container.addComponent(AutoscalingAlgorithm.class,
                           WorkspacesFreeSpaceRatioAutoscalingAlgorithm.class);

    container.removeComponent(TenantOperationMailSenderInitiator.class);
    container.addComponent(WorkspacesTenantOperationMailSenderInitiator.class);

    container.removeComponent(UserDataGenerator.class);
    container.addComponent(UserDataGenerator.class, WorkspacesUserDataGenerator.class);
  }

  @Override
  protected void doComposeRequest(MutablePicoContainer container) {
    super.doComposeRequest(container);
    container.addComponent(CloudWorkspacesTenantService.class);
    container.addComponent(ShellConfigurationService.class);
    container.addComponent(CLIResourcesService.class);
    container.addComponent(StatisticAllTenants.class);
    /*
     * container.addComponent(TenantCreatorWithEmailAuthorization.class);
     */
  }

}
