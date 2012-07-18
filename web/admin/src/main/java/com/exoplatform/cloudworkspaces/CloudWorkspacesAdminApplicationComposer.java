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

import com.exoplatform.cloudworkspaces.dao.PropertiesModifiableEmailValidationStorage;
import com.exoplatform.cloudworkspaces.http.WorkspacesOrganizationRequestPerformer;
import com.exoplatform.cloudworkspaces.instance.WorkspacesUserDataGenerator;
import com.exoplatform.cloudworkspaces.listener.AsyncTenantStarter;
import com.exoplatform.cloudworkspaces.listener.DemoTenantOnlineListener;
import com.exoplatform.cloudworkspaces.listener.JoinAllInOnlineServerListener;
import com.exoplatform.cloudworkspaces.listener.TenantCreatedListener;
import com.exoplatform.cloudworkspaces.listener.UserLimitSupervisor;
import com.exoplatform.cloudworkspaces.listener.WorkspacesServerOnlineListenersInvoker;
import com.exoplatform.cloudworkspaces.patch.WorkspacesErrorMailSenderImpl;
import com.exoplatform.cloudworkspaces.rest.CloudWorkspacesInfoService;
import com.exoplatform.cloudworkspaces.rest.CloudWorkspacesTenantService;
import com.exoplatform.cloudworkspaces.shell.ShellConfigurationService;
import com.exoplatform.cloudworkspaces.users.UserLimitsStorage;
import com.exoplatform.cloudworkspaces.users.UsersManager;

import org.everrest.core.ResourceBinder;
import com.exoplatform.cloud.admin.WorkspacesMailSender;
import com.exoplatform.cloud.admin.dao.EmailValidationStorage;
import com.exoplatform.cloud.admin.instance.UserDataGenerator;
import com.exoplatform.cloud.admin.instance.autoscaling.AutoscalingAlgorithm;
import com.exoplatform.cloud.admin.instance.autoscaling.WorkspacesFreeSpaceRatioAutoscalingAlgorithm;
import com.exoplatform.cloud.admin.mail.TenantOperationMailSenderInitiator;
import com.exoplatform.cloud.admin.mail.WorkspacesTenantOperationMailSenderInitiator;
import com.exoplatform.cloud.admin.proxy.ProxyLoadBalancerConfigurator;
import com.exoplatform.cloud.admin.proxy.ServerStateChangesProxyReconfigurationInitiator;
import com.exoplatform.cloud.admin.proxy.WorkspacesProxyLoadBalancerConfigurator;
import com.exoplatform.cloud.admin.proxy.WorkspacesServerStateChangesProxyReconfigurationInitiator;
import com.exoplatform.cloud.admin.rest.CloudAdminApplicationComposer;
import com.exoplatform.cloud.admin.rest.TenantCreator;
import com.exoplatform.cloud.admin.rest.TenantService;
import com.exoplatform.cloud.admin.rest.WorkspacesTenantService;
import com.exoplatform.cloud.admin.status.ServerOnlineListenersInvoker;
import com.exoplatform.cloud.admin.tenant.ServerSelectionAlgorithm;
import com.exoplatform.cloud.admin.tenant.TenantSuspender;
import com.exoplatform.cloud.admin.tenant.WorkspacesLowestLoadFactorServerSelectionAlgorithm;
import com.exoplatform.cloud.admin.tenant.WorkspacesTenantSuspender;
import com.exoplatform.cloud.admin.util.ServerStatusMailer;
import com.exoplatform.cloud.admin.util.WorkspacesServerStatusMailer;
import org.exoplatform.ide.shell.server.CLIResourceFactory;
import org.exoplatform.ide.shell.server.rest.CLIResourcesService;
import org.picocontainer.MutablePicoContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    container.addComponent(PasswordCipher.class);
    container.addComponent(UserRequestDAO.class);
    container.addComponent(TemplateManagement.class);

    container.removeComponent(EmailValidationStorage.class);
    container.addComponent(EmailValidationStorage.class,
                           PropertiesModifiableEmailValidationStorage.class);

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

    container.removeComponent(ServerStatusMailer.class);
    container.addComponent(ServerStatusMailer.class, WorkspacesServerStatusMailer.class);

    container.removeComponent(TenantSuspender.class);
    container.addComponent(TenantSuspender.class, WorkspacesTenantSuspender.class);

    container.removeComponent(ServerSelectionAlgorithm.class);
    container.addComponent(ServerSelectionAlgorithm.class,
                           WorkspacesLowestLoadFactorServerSelectionAlgorithm.class);

    // configure CM patch utils
    container.addComponent(WorkspacesErrorMailSenderImpl.class);

    // CLDINT-618
    container.removeComponent(ProxyLoadBalancerConfigurator.class);
    container.addComponent(ProxyLoadBalancerConfigurator.class,
                           WorkspacesProxyLoadBalancerConfigurator.class);

    // CLDINT-614
    container.removeComponent(ServerStateChangesProxyReconfigurationInitiator.class);
    container.addComponent(ServerStateChangesProxyReconfigurationInitiator.class,
                           WorkspacesServerStateChangesProxyReconfigurationInitiator.class);

    container.addComponent(DemoTenantOnlineKeeper.class);
    container.addComponent(DemoTenantOnlineListener.class);
  }

  @Override
  protected void doComposeRequest(MutablePicoContainer container) {
    super.doComposeRequest(container);
    container.addComponent(CloudWorkspacesTenantService.class);
    container.addComponent(CloudWorkspacesInfoService.class);
    container.addComponent(ShellConfigurationService.class);
    container.addComponent(CLIResourcesService.class);
    container.addComponent(StatisticAllTenants.class);

    container.removeComponent(TenantService.class);
    container.addComponent(WorkspacesTenantService.class);
    /*
     * container.addComponent(TenantCreatorWithEmailAuthorization.class);
     */
  }

}
