package org.exoplatform.cloudmanagement.admin;

import static org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration.CLOUD_ADMIN_MAIL_AUTH;
import static org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration.CLOUD_ADMIN_MAIL_HOST;
import static org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration.CLOUD_ADMIN_MAIL_PORT;
import static org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration.CLOUD_ADMIN_MAIL_SENDER;
import static org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration.CLOUD_ADMIN_MAIL_SMTP_AUTH_PASSWORD;
import static org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration.CLOUD_ADMIN_MAIL_SMTP_AUTH_USERNAME;
import static org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration.CLOUD_ADMIN_MAIL_SMTP_SOCKETFACTORY_CLASS;
import static org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration.CLOUD_ADMIN_MAIL_SMTP_SOCKETFACTORY_FALLBACK;
import static org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration.CLOUD_ADMIN_MAIL_SMTP_SOCKETFACTORY_PORT;
import static org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration.CLOUD_ADMIN_MAIL_TRANSPORT_PROTOCOL;

import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.util.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * This class uses to sending emails from cloud-admin. Any class in cloud-admin,
 * that wants to send email must use this class.
 */
public class WorkspacesMailSender
{

   private static final Logger LOG = LoggerFactory.getLogger(WorkspacesMailSender.class);

   private final Configuration cloudAdminConfiguration;

   public WorkspacesMailSender(Configuration cloudAdminConfiguration)
   {
      super();
      this.cloudAdminConfiguration = cloudAdminConfiguration;
   }

   public void sendMail(String email, String subject, String mailTemplateFile, Map<String, String> templateProperties,
      boolean isAdminMail) throws CloudAdminException
   {
      sendMail(email, subject, mailTemplateFile, templateProperties, isAdminMail, null);
   }

   /**
    * <p>
    * Sends mail from template. Method gets {@code mailTemplateFile} variable,
    * that links to template file in file system or resources. In template may
    * be used variables, which have format ${name_of_a_variable}. All variables
    * that must be resolved must be put into {@code templateProperties} map.
    * </p>
    * <p>
    * Subject of email can be set by variable {@code subject}
    * </p>
    * <p>
    * Method can send more than one copy of email. In variable {@code email} may
    * be set more than one email addresses separated by comma.
    * </p>
    * 
    * @param email
    *           addresses of recipients separated by comma
    * @param subject
    *           subject of mail
    * @param mailTemplateFile
    *           path to mail template
    * @param templateProperties
    *           variables map to resolve template
    * @throws CloudAdminException
    *            if any error occurred.
    */
   public void sendMail(String email, String subject, String mailTemplateFile, Map<String, String> templateProperties,
      boolean isAdminMail, String replyTo) throws CloudAdminException
   {

      if (mailTemplateFile == null)
      {
         throw new CloudAdminException(500, "Mail template configuration not found. Please contact support.");
      }
      try
      {
         String templateContent = Deserializer.getResourceContent(mailTemplateFile);

         if (templateContent == null)
         {
            throw new TenantRegistrationException(500, "Mail template from resource " + mailTemplateFile
               + "not found. Please contact support.");
         }

         String body;

         body = Deserializer.resolveTemplate(templateContent, templateProperties);

         if (!cloudAdminConfiguration.containsKey(CLOUD_ADMIN_MAIL_TRANSPORT_PROTOCOL)
            || !cloudAdminConfiguration.containsKey(CLOUD_ADMIN_MAIL_HOST)
            || !cloudAdminConfiguration.containsKey(CLOUD_ADMIN_MAIL_PORT)
            || !cloudAdminConfiguration.containsKey(CLOUD_ADMIN_MAIL_AUTH))
         {
            LOG.warn("Mail parameters is not configured. Mail subject {} ", subject);
            return;
         }

         Properties props = new Properties();

         // SMTP protocol properties
         props.put("mail.transport.protocol", cloudAdminConfiguration.getString(CLOUD_ADMIN_MAIL_TRANSPORT_PROTOCOL));
         props.put("mail.smtp.host", cloudAdminConfiguration.getString(CLOUD_ADMIN_MAIL_HOST));
         props.put("mail.smtp.port", cloudAdminConfiguration.getString(CLOUD_ADMIN_MAIL_PORT));
         props.put("mail.smtp.auth", cloudAdminConfiguration.getString(CLOUD_ADMIN_MAIL_AUTH));

         Session mailSession;
         if (Boolean.parseBoolean(props.getProperty("mail.smtp.auth")))
         {
            props.put("mail.smtp.socketFactory.port",
               cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_SMTP_SOCKETFACTORY_PORT));
            props.put("mail.smtp.socketFactory.class",
               cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_SMTP_SOCKETFACTORY_CLASS));
            props.put("mail.smtp.socketFactory.fallback",
               cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_SMTP_SOCKETFACTORY_FALLBACK));

            final String mailUserName = cloudAdminConfiguration.getString(CLOUD_ADMIN_MAIL_SMTP_AUTH_USERNAME);
            final String mailPassword = cloudAdminConfiguration.getString(CLOUD_ADMIN_MAIL_SMTP_AUTH_PASSWORD);

            mailSession = Session.getInstance(props, new Authenticator()
            {
               @Override
               protected PasswordAuthentication getPasswordAuthentication()
               {
                  return new PasswordAuthentication(mailUserName, mailPassword);
               }
            });
         }
         else
         {
            mailSession = Session.getInstance(props);
         }

         MimeMessage message = new MimeMessage(mailSession);
         message.setContent(body, "text/html");
         message.setSubject(subject);
         if (isAdminMail)
         {
            message.setFrom(new InternetAddress(cloudAdminConfiguration.getString(CLOUD_ADMIN_MAIL_SENDER)));
         }
         else
         {
            message.setFrom(new InternetAddress(cloudAdminConfiguration.getString("cloud.admin.mail.support.sender")));
         }
         if (replyTo != null)
         {
            InternetAddress addr = new InternetAddress(replyTo);
            message.setReplyTo(new Address[]{addr});
         }
         message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));

         Transport.send(message, message.getRecipients(Message.RecipientType.TO));
      }

      catch (IOException e)
      {
         LOG.error("Error during sending confirmation mail", e);
         throw new TenantRegistrationException(500, "Unable to send mail. Please contact support.", e);
      }
      catch (MessagingException e)
      {
         LOG.error("Error during sending confirmation mail", e);
         throw new TenantRegistrationException(500, "Unable to send mail. Please contact support.", e);
      }
   }

   /**
    * Method sends email using {@link #sendMail(String, String, String, Map)}
    * method, but if some errors caused, method print error by logger and don't
    * throw it. Use this method if you want send mail, and don't want catch a
    * exception.
    * 
    * @param email
    * @param subject
    * @param mailTemplateFile
    * @param templateProperties
    * @see #sendMail(String, String, String, Map)
    */
   public void sendMailQuietly(String email, String subject, String mailTemplateFile,
      Map<String, String> templateProperties, boolean isAdminMail, String replyTo)
   {
      try
      {
         sendMail(email, subject, mailTemplateFile, templateProperties, isAdminMail, replyTo);
      }
      catch (CloudAdminException e)
      {
         LOG.error(e.getLocalizedMessage(), e);
      }
   }

}
