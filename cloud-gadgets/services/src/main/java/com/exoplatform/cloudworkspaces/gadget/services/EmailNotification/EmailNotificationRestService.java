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
    EmailNotificationService emailNotificationService = (EmailNotificationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(EmailNotificationService.class);
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      NodeHierarchyCreator nodeCreator = (NodeHierarchyCreator) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
      OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);

      String userLocale = organizationService.getUserProfileHandler().findUserProfileByName(userId).getAttribute("user.language");
      Node userAppDataNode = nodeCreator.getUserNode(sProvider, userId).getNode("ApplicationData");

      MessagesCache messagesCache = new MessagesCache(EmailNotificationService.class);
      String interval = messagesCache.getDefault().getProperty("defaultInterval", "day");

      boolean isSummaryMail = false;
      ArrayList<EmailNotificationPluginBean> pluginBeans = new ArrayList<EmailNotificationPluginBean>();

      String emailNotificationPrefs = EmailNotificationService.APP_NAME + "/" + EmailNotificationService.PREFS;
      if (!userAppDataNode.hasNode(emailNotificationPrefs)) {
        for (EmailNotificationPlugin plugin : emailNotificationService.getPlugins()) {
          MessagesCache pluginMessages = new MessagesCache(plugin.getClass());
          String settingMessage = pluginMessages.get(userLocale).getProperty("setting");
          boolean isDefault = Boolean.parseBoolean(pluginMessages.getDefault().getProperty("isDefault", "true"));
          pluginBeans.add(new EmailNotificationPluginBean(plugin.getName(), settingMessage, isDefault));
        }
      } else {
        Node emailNotificationPrefsNode = userAppDataNode.getNode(emailNotificationPrefs);
        interval = emailNotificationPrefsNode.getProperty("Interval").getString();
        isSummaryMail = emailNotificationPrefsNode.getProperty("isSummaryMail").getBoolean();
        String pluginsProp = emailNotificationPrefsNode.getProperty("NotificationPlugins").getString();

        for (EmailNotificationPlugin plugin : emailNotificationService.getPlugins()) {
          MessagesCache pluginMessages = new MessagesCache(plugin.getClass());
          String settingMessage = pluginMessages.get(userLocale).getProperty("setting");
          pluginBeans.add(new EmailNotificationPluginBean(plugin.getName(), settingMessage, pluginsProp.contains(plugin.getName())));
        }
      }

      return new EmailNotificationPrefsBean(isSummaryMail, interval, pluginBeans);

    } catch (Exception e) {
      LOG.debug(e.getMessage(), e);
      throw e;
    } finally {
      sProvider.close();
    }
  }

  public void setUserPrefs(String userId, Boolean isSummaryMail, String interval, String notificationPlugins) throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      NodeHierarchyCreator nodeCreator = (NodeHierarchyCreator) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
      Node userAppDataNode = nodeCreator.getUserNode(sProvider, userId).getNode("ApplicationData");
      
      Node emailNotifNode, prefsNode;
      if (!userAppDataNode.hasNode(EmailNotificationService.APP_NAME)){
        emailNotifNode = userAppDataNode.addNode(EmailNotificationService.APP_NAME);
        userAppDataNode.save();
      } else {
        emailNotifNode = userAppDataNode.getNode(EmailNotificationService.APP_NAME);
      }
      
      if (!emailNotifNode.hasNode(EmailNotificationService.PREFS)){
        prefsNode = emailNotifNode.addNode(EmailNotificationService.PREFS);
        emailNotifNode.save();
      } else {
        prefsNode = emailNotifNode.getNode(EmailNotificationService.PREFS);
      }

      MessagesCache messagesCache = new MessagesCache(EmailNotificationService.class);
      prefsNode.setProperty("userPrefsVersion", messagesCache.getDefault().getProperty("userPrefsVersion", ""));
      prefsNode.setProperty("isSummaryMail", isSummaryMail);
      prefsNode.setProperty("Interval", interval);
      prefsNode.setProperty("NotificationPlugins", notificationPlugins);
      prefsNode.save();
      
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
  public Response setPrefs(@FormParam("isSummaryMail") Boolean isSummaryMail, @FormParam("interval") String interval, @FormParam("notificationPlugins") String notificationPlugins) {
    try {
      setUserPrefs(ConversationState.getCurrent().getIdentity().getUserId(), isSummaryMail, interval, notificationPlugins);
      return Response.ok().cacheControl(cacheControl).build();
    } catch (Exception e) {
      LOG.debug(e.getMessage(), e);
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).cacheControl(cacheControl).build();
    }
  }

}
