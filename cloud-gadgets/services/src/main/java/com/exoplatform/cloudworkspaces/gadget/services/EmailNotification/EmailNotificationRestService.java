package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;

import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.utils.MessagesCache;

@Path("/email-notification/")
@Produces(MediaType.APPLICATION_JSON)
public class EmailNotificationRestService implements ResourceContainer {
	private static Log LOG = ExoLogger.getLogger(EmailNotificationRestService.class);

	private static final CacheControl cacheControl;
	static {
		RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
		cacheControl = new CacheControl();
		cacheControl.setNoCache(true);
		cacheControl.setNoStore(true);
	}

	public EmailNotificationPrefsBean getUserPrefs(String userId) throws Exception {
	  
    RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    String currentRepoName = repoService.getCurrentRepository().getConfiguration().getName();
    EmailNotificationService emailNotificationService = (EmailNotificationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(EmailNotificationService.class);
    emailNotificationService.initResourceBundle(currentRepoName);
    
    SessionProvider sProvider = SessionProvider.createSystemProvider();
		try {
			NodeHierarchyCreator nodeCreator = (NodeHierarchyCreator) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
			OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);

			String userLocale = organizationService.getUserProfileHandler().findUserProfileByName(userId).getAttribute("user.language");
			Node userPrivateNode = nodeCreator.getUserNode(sProvider, userId).getNode("Private");

			String interval = "never";
			ArrayList<EmailNotificationPluginBean> pluginBeans = new ArrayList<EmailNotificationPluginBean>();

			if(!userPrivateNode.hasNode(EmailNotificationService.PREFS)){
				for(EmailNotificationPlugin plugin:emailNotificationService.getPlugins()) {
					MessagesCache pluginMessages = new MessagesCache(EmailNotificationService.PLUGINS + "/" + plugin.getName());
					String settingMessage = pluginMessages.get(userLocale).getProperty("setting");
					boolean isDefault = Boolean.parseBoolean(pluginMessages.getDefault().getProperty("isDefault", "false"));
					pluginBeans.add(new EmailNotificationPluginBean(plugin.getName(), settingMessage, isDefault));
				}
			} else {
				Node emailNotificationPrefsNode = userPrivateNode.getNode(EmailNotificationService.PREFS);
				interval = emailNotificationPrefsNode.getProperty("Interval").getString();
				String pluginsProp = emailNotificationPrefsNode.getProperty("NotificationPlugins").getString();

				for(EmailNotificationPlugin plugin:emailNotificationService.getPlugins()) {
					MessagesCache pluginMessages = new MessagesCache(EmailNotificationService.PLUGINS + "/" + plugin.getName());
					String settingMessage = pluginMessages.get(userLocale).getProperty("setting");
					pluginBeans.add(new EmailNotificationPluginBean(plugin.getName(), settingMessage, pluginsProp.contains(plugin.getName())));
				}
			}

			return new EmailNotificationPrefsBean(interval, pluginBeans);

		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
			throw e;
		} finally {
			sProvider.close();
		}
	}

	@GET
	@Path("prefs")
	public Response getPrefs() {
		try {
			return Response.ok(getUserPrefs(ConversationState.getCurrent().getIdentity().getUserId()), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
			return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
		}
	}

	@POST
	@Path("prefs")
	public Response setPrefs(@FormParam("interval") String interval, @FormParam("notificationPlugins") String notificationPlugins) {
		SessionProvider sProvider = SessionProvider.createSystemProvider();
		try {
			String userId = ConversationState.getCurrent().getIdentity().getUserId();
			NodeHierarchyCreator nodeCreator = (NodeHierarchyCreator) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
			Node userPrivateNode = nodeCreator.getUserNode(sProvider, userId).getNode("Private");

			Node emailNotificationSettingNode;
			if(!userPrivateNode.hasNode(EmailNotificationService.PREFS)){
				emailNotificationSettingNode = userPrivateNode.addNode(EmailNotificationService.PREFS);
				// hide this node
				if(emailNotificationSettingNode.canAddMixin("exo:hiddenable")){
					emailNotificationSettingNode.addMixin("exo:hiddenable");
				}
				userPrivateNode.save();
			} else {
				emailNotificationSettingNode = userPrivateNode.getNode(EmailNotificationService.PREFS);
			}

			emailNotificationSettingNode.setProperty("Interval", interval);
			emailNotificationSettingNode.setProperty("NotificationPlugins", notificationPlugins);			
			emailNotificationSettingNode.save();

			return Response.ok().cacheControl(cacheControl).build();

		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
			return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
		} finally {
			sProvider.close();
		}
	}
	
}
