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
      EmailNotificationService emailNotificationService = (EmailNotificationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(EmailNotificationService.class);
      emailNotificationService.initResourceBundle(this.repoName);
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

        MessagesCache messagesCache = new MessagesCache(EmailNotificationService.HOME);
        Properties setting = messagesCache.getDefault();
        EmailTemplateCache templatesCache = new EmailTemplateCache(EmailNotificationService.HOME);
        Map<String, MessagesCache> pluginMessagesCaches = new HashMap<String, MessagesCache>();

        for (EmailNotificationPlugin plugin : emailNotificationService.getPlugins()) {
          pluginMessagesCaches.put(plugin.getName(), new MessagesCache(EmailNotificationService.PLUGINS + "/" + plugin.getName()));
        }

        ListAccess<User> laUsers = organizationService.getUserHandler().findAllUsers();

        for (User user : laUsers.load(0, laUsers.getSize())) {
          String userId = user.getUserName();
          String userLocale = organizationService.getUserProfileHandler().findUserProfileByName(userId).getAttribute("user.language");
          Node userPrivateNode = nodeCreator.getUserNode(sProvider, userId).getNode("Private");
          if (userPrivateNode == null)
            continue;

          boolean isSendMailByDefault = Boolean.parseBoolean(setting.getProperty("isSendMailByDefault", "true"));
          boolean isServiceRegistered = userPrivateNode.hasNode(EmailNotificationService.PREFS);

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
            emailNotificationRestService.setUserPrefs(userId, false, setting.getProperty("defaultInterval", "week"), notificationPlugins.toString());
            isServiceRegistered = true;
          }

          if (isServiceRegistered) {
            Node emailNotificationPrefs = userPrivateNode.getNode(EmailNotificationService.PREFS);
            String interval = emailNotificationPrefs.getProperty("Interval").getString();
            Boolean isSummaryMail = emailNotificationPrefs.getProperty("isSummaryMail").getBoolean();
            if (!emailNotificationPrefs.hasProperty("LastRun")) {
              emailNotificationPrefs.setProperty("LastRun", System.currentTimeMillis());
              emailNotificationPrefs.save();
            }

            long lastRun = emailNotificationPrefs.getProperty("LastRun").getLong();
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

            List<String> notificationPlugins = Arrays.asList(emailNotificationPrefs.getProperty("NotificationPlugins").getString().split(","));

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

            emailNotificationPrefs.setProperty("LastRun", System.currentTimeMillis());
            emailNotificationPrefs.save();

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

            String subject = prop.getProperty("subject");
            String fromEmail = prop.getProperty("from.email");
            String fromName = prop.getProperty("from.name");

            emailNotificationService.sendMail(subject, mailTemplate.render(binding), new InternetAddress(fromEmail, fromName), userAddr);
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

}
