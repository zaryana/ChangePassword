package com.exoplatform.cloudworkspaces.portlet.FeedbackPortlet;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.groovyscript.GroovyTemplate;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormTextAreaInput;

@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/groovy/FeedbackPortlet/UIFeedbackForm.gtmpl",
  events = {
    @EventConfig(listeners = UIFeedbackForm.FeedbackSentActionListener.class)
  }
)
public class UIFeedbackForm extends UIForm {
  private static final Log LOG = ExoLogger.getLogger(UIFeedbackForm.class);

  public UIFeedbackForm() {
    this.addChild(new UIFormTextAreaInput("uiTxtFeedback", "uiTxtFeedback", ""));
  }

  public static class FeedbackSentActionListener extends EventListener<UIFeedbackForm> {
    @Override
    public void execute(Event<UIFeedbackForm> event) throws Exception {
      UIFeedbackForm feedbackForm = event.getSource();
      UIFeedbackMainContainer mainContainer = feedbackForm.getAncestorOfType(UIFeedbackMainContainer.class);
      // Send feedback in background
      event.getRequestContext().addUIComponentToUpdateByAjax(mainContainer);
      try {
        RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
        ConfigurationManager confManager = (ConfigurationManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ConfigurationManager.class);

        Properties emailSettings = new Properties();
        emailSettings.load(confManager.getInputStream("war:/conf/portlet/FeedbackPortlet/email-settings.properties"));
        GroovyTemplate emailTemplate = new GroovyTemplate(new InputStreamReader(confManager.getInputStream("war:/conf/portlet/FeedbackPortlet/email-template.gtmpl")));

        String senderName = "Anonymous", senderEmail = "";
        String userId = ConversationState.getCurrent().getIdentity().getUserId();
        if (!userId.equals("__anonim")) {
          OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
          User user = organizationService.getUserHandler().findUserByName(userId);
          senderName = user.getFullName();
          senderEmail = user.getEmail();
        }

        Map<String, String> binding;
        binding = new HashMap<String, String>();
        binding.put("feedback", feedbackForm.getChild(UIFormTextAreaInput.class).getValue());
        binding.put("senderName", senderName);
        binding.put("senderEmail", senderEmail);
        binding.put("tenant", ((RepositoryImpl) repoService.getCurrentRepository()).getName());
        UserNode currentPage = Util.getUIPortal().getNavPath();
        binding.put("pageName", currentPage.getResolvedLabel());
        binding.put("pageRef", currentPage.getPageRef());

        String adminEmail = System.getProperty("gatein.email.smtp.from");
        InternetAddress from = new InternetAddress(emailSettings.getProperty("from.email", adminEmail), emailSettings.getProperty("from.name", "CW Feedback"));
        InternetAddress to = new InternetAddress(emailSettings.getProperty("to.email", adminEmail), emailSettings.getProperty("to.name", "eXo Cloud Workspaces Administrator"));

        sendMail(emailSettings.getProperty("subject", senderName + (senderEmail.isEmpty() ? "" : " (" + senderEmail + ")")), emailTemplate.render(binding), from, to);
        LOG.info("Feedback from " + senderName + " was sent");

      } catch (Exception e) {
        LOG.debug(e.getMessage(), e);
      }
    }

    private void sendMail(String subject, String content, InternetAddress from, InternetAddress to) throws Exception {
      MailService mailService = (MailService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(MailService.class);
      Session mailSession = mailService.getMailSession();
      MimeMessage message = new MimeMessage(mailSession);

      message.setSubject(subject);
      message.setFrom(from);
      message.setRecipient(RecipientType.TO, to);

      MimeMultipart mailContent = new MimeMultipart("alternative");
      MimeBodyPart text = new MimeBodyPart();
      MimeBodyPart html = new MimeBodyPart();
      text.setText(content);
      html.setContent(content, "text/html; charset=ISO-8859-1");
      mailContent.addBodyPart(text);
      mailContent.addBodyPart(html);

      message.setContent(mailContent);
      mailService.sendMessage(message);
    }

  }
}
