package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jcr.Node;
import javax.mail.internet.InternetAddress;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.groovyscript.GroovyTemplate;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.quartz.JobExecutionContext;

import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.utils.EmailTemplateCache;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.utils.MessagesCache;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.utils.MultiTenancyJob;

public class EmailNotificationJob extends MultiTenancyJob {
  private static Log LOG = ExoLogger.getLogger(EmailNotificationJob.class);

  @Override
  public Class<? extends MultiTenancyTask> getTask() {
    return EmailNotificationTask.class;
  }

  public class EmailNotificationTask extends MultiTenancyTask {
    public EmailNotificationTask(JobExecutionContext context, String repoName) {
      super(context, repoName);
      try {
        MessagesCache messagesCache = new MessagesCache(EmailNotificationService.class);
        Boolean.parseBoolean(messagesCache.getDefault().getProperty("cleanOldJcrData", "false"));
        cleanOldJcrData(repoName);
      } catch (Exception e) {
        LOG.debug("Exception when cleaning old JCR data: " + e.getMessage(), e);
      }
    }

    @Override
    public void run() {
      LOG.debug("EmailNotificationTask running for " + repoName);
      SessionProvider sProvider = SessionProvider.createSystemProvider();
      try {
        super.run();
        IdentityManager identityManager = (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
        OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
        NodeHierarchyCreator nodeCreator = (NodeHierarchyCreator) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
        EmailNotificationService emailNotificationService = (EmailNotificationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(EmailNotificationService.class);

        MessagesCache messagesCache = new MessagesCache(EmailNotificationService.class);
        Properties setting = messagesCache.getDefault();
        EmailTemplateCache templatesCache = new EmailTemplateCache(EmailNotificationService.class);
        Map<String, MessagesCache> pluginMessagesCaches = new HashMap<String, MessagesCache>();

        for (EmailNotificationPlugin plugin : emailNotificationService.getPlugins()) {
          pluginMessagesCaches.put(plugin.getName(), new MessagesCache(plugin.getClass()));
        }

        UserACL userACL = (UserACL) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(UserACL.class);
        String superUserName = userACL.getSuperUser();

        ListAccess<User> laUsers = organizationService.getUserHandler().findAllUsers();

        for (User user : laUsers.load(0, laUsers.getSize())) {
          String userId = user.getUserName();
          if(null != superUserName && superUserName.equals(userId)) // skip running for root
            continue;
          
          String userLocale = organizationService.getUserProfileHandler().findUserProfileByName(userId).getAttribute("user.language");
          Node userAppDataNode = nodeCreator.getUserNode(sProvider, userId).getNode("ApplicationData");
          if (userAppDataNode == null)
            continue;

          boolean isSendMailByDefault = Boolean.parseBoolean(setting.getProperty("isSendMailByDefault", "true"));
          String emailNotificationPrefs = EmailNotificationService.APP_NAME + "/" + EmailNotificationService.PREFS;
          boolean isServiceRegistered = userAppDataNode.hasNode(emailNotificationPrefs);

          if(isServiceRegistered){
            Node prefsNode = userAppDataNode.getNode(emailNotificationPrefs);
            String prefsVersion = prefsNode.hasProperty("userPrefsVersion") ? prefsNode.getProperty("userPrefsVersion").getValue().getString() : "";
            
            // user prefs version changed, remove old prefs for it to be rebuilt
            if(!prefsVersion.equals(setting.getProperty("userPrefsVersion", ""))){
              prefsNode.remove();
              userAppDataNode.save();
              isServiceRegistered = false;
            }
          }

          if (isSendMailByDefault && !isServiceRegistered) {
            EmailNotificationRestService emailNotificationRestService = (EmailNotificationRestService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(EmailNotificationRestService.class);
            StringBuilder notificationPlugins = new StringBuilder();
            String prefix = "";
            for (EmailNotificationPlugin plugin : emailNotificationService.getPlugins()) {
              boolean isDefault = Boolean.parseBoolean(pluginMessagesCaches.get(plugin.getName()).getDefault().getProperty("isDefault", "true"));
              if (isDefault) {
                notificationPlugins.append(prefix);
                prefix = ",";
                notificationPlugins.append(plugin.getName());
              }
            }
            emailNotificationRestService.setUserPrefs(userId, false, setting.getProperty("defaultInterval", "day"), notificationPlugins.toString());
            isServiceRegistered = true;
          }

          if (isServiceRegistered) {
            Node prefsNode = userAppDataNode.getNode(emailNotificationPrefs);
            String interval = prefsNode.getProperty("Interval").getString();
            Boolean isSummaryMail = prefsNode.getProperty("isSummaryMail").getBoolean();
            if (!prefsNode.hasProperty("LastRun")) {
              prefsNode.setProperty("LastRun", System.currentTimeMillis());
              prefsNode.save();
            }

            long lastRun = prefsNode.getProperty("LastRun").getLong();
            long nextRun = lastRun;

            if (isSummaryMail) {
              if (interval.equals("never")) {
                continue;
              } else if (interval.equals("day")) {
                nextRun = DateTimeUtils.nextDayOf(lastRun);
              } else if (interval.equals("week")) {
                nextRun = DateTimeUtils.nextMondayOf(lastRun);
              } else if (interval.equals("month")) {
                nextRun = DateTimeUtils.nextMonthOf(lastRun);
              }
            } else {
              nextRun = DateTimeUtils.nextMinuteOf(lastRun);
            }

            if (System.currentTimeMillis() < nextRun) {
              continue;
            }

            List<String> notificationPlugins = Arrays.asList(prefsNode.getProperty("NotificationPlugins").getString().split(","));

            StringBuilder builder = new StringBuilder();

            Map<String, Object> runningContext = new HashMap<String, Object>();
            runningContext.put("userId", userId);
            runningContext.put("userLocale", userLocale);
            runningContext.put("repoName", this.repoName);
            runningContext.put("lastRun", new Long(lastRun));
            runningContext.put("isSummaryMail", isSummaryMail);

            String mailSubject = "";
            for (EmailNotificationPlugin plugin : emailNotificationService.getPlugins()) {
              if (notificationPlugins.contains(plugin.getName())) {
                MessagesCache pluginMessagesCache = pluginMessagesCaches.get(plugin.getName());
                runningContext.put("pluginMessagesCache", pluginMessagesCache);
                String pluginNotification = plugin.exec(runningContext);
                if (!pluginNotification.isEmpty()) {
                  mailSubject = pluginMessagesCache.get((userLocale)).getProperty("title");
                  builder.append(pluginNotification);
                }
              }
            }

            prefsNode.setProperty("LastRun", System.currentTimeMillis());
            prefsNode.save();

            String notifications = builder.toString();
            if (notifications.isEmpty())
              continue;

            GroovyTemplate mailTemplate = new GroovyTemplate(templatesCache.get(userLocale));
            Map<String, Object> binding;
            Properties prop = messagesCache.get(userLocale);
            if(isSummaryMail){
              mailSubject = prop.getProperty("subject");
            }
            
            binding = new HashMap<String, Object>();
            binding.put("subject", mailSubject);
            binding.put("isSummaryMail", isSummaryMail);
            binding.put("user", user.getFirstName());
            binding.put("interval", prop.getProperty(interval));
            binding.put("notifications", notifications);
            binding.put("accountSettingsLink", this.repoName + "." + System.getProperty("tenant.masterhost") + "/" + setting.getProperty("accountSettingsLink"));

            Profile userProfile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false).getProfile();
            InternetAddress userAddr = new InternetAddress(userProfile.getEmail(), userProfile.getFullName());

            emailNotificationService.sendMail(prop.getProperty("subject"), mailTemplate.render(binding), new InternetAddress(System.getProperty("gatein.email.smtp.from"), "eXo Cloud Workspaces"), userAddr);
            LOG.info("Notification mail sent to " + userAddr.getAddress());
          }
        }
      } catch (Exception e) {
        LOG.debug(e.getMessage(), e);
      } finally {
        sProvider.close();
      }
    }

  }
  
  
  private void cleanOldJcrData(String currentRepoName) throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
      if (currentRepoName == null || currentRepoName.isEmpty()) {
        currentRepoName = repoService.getCurrentRepository().getConfiguration().getName();
      }
      ManageableRepository currentRepo = repoService.getRepository(currentRepoName);
      javax.jcr.Session session = sProvider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo);

      Node exoAppNode = session.getRootNode().getNode("exo:applications");

      // remove exo:applications/EmailNotification node
      if (exoAppNode.hasNode("EmailNotification")) {
        exoAppNode.getNode("EmailNotification").remove();
        exoAppNode.save();
      }

      NodeHierarchyCreator nodeCreator = (NodeHierarchyCreator) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
      OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);

      ListAccess<User> laUsers = organizationService.getUserHandler().findAllUsers();
      for (User user : laUsers.load(0, laUsers.getSize())) {
        Node userPrivateNode = nodeCreator.getUserNode(sProvider, user.getUserName()).getNode("Private");
        
        if(userPrivateNode.hasNode("EmailNotificationPrefs")){
          userPrivateNode.getNode("EmailNotificationPrefs").remove();
          userPrivateNode.save();
        }

        if(userPrivateNode.hasNode("EmailNotificationStorage")){
          userPrivateNode.getNode("EmailNotificationStorage").remove();
          userPrivateNode.save();
        }
      }
      
    } catch (Exception e) {
      throw e;
    } finally {
      sProvider.close();
    }
  }


}
