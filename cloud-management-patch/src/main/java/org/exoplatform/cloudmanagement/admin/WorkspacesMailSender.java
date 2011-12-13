/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.cloudmanagement.admin;

import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_AUTH;
import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_HOST;
import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_PORT;
import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_SENDER;
import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_SMTP_AUTH_PASSWORD;
import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_SMTP_AUTH_USERNAME;
import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_SMTP_SOCKETFACTORY_CLASS;
import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_SMTP_SOCKETFACTORY_FALLBACK;
import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_SMTP_SOCKETFACTORY_PORT;
import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_TRANSPORT_PROTOCOL;

import org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

/**
 * @author <a href="mailto:foo@bar.org">Foo Bar</a>
 * @version $Id: exo-jboss-codetemplates.xml 34360 2009-07-22 23:58:59Z
 *          aheritier $
 * 
 */
public class WorkspacesMailSender
{

   private static final Logger LOG = LoggerFactory.getLogger(WorkspacesMailSender.class);

   private final CloudAdminConfiguration cloudAdminConfiguration;

   public WorkspacesMailSender(CloudAdminConfiguration cloudAdminConfiguration)
   {
      super();
      this.cloudAdminConfiguration = cloudAdminConfiguration;
   }

   public void sendMail(String email, String subject, String mailTemplateFile, Map<String, String> templateProperties)
      throws CloudAdminException
   {

      if (mailTemplateFile == null)
      {
         throw new CloudAdminException(500, "Mail template registration not found");
      }

      File templateFile = new File(mailTemplateFile);
      if (!templateFile.exists())
      {
         throw new TenantRegistrationException(500, "Mail template not found at " + mailTemplateFile);
      }

      String body;
      try
      {
         body = Deserializer.resolveTemplate(new FileInputStream(templateFile), templateProperties);
      }
      catch (FileNotFoundException e1)
      {
         LOG.error("File with confirmation mail template was not found", e1);
         throw new CloudAdminException(500, "Unable to create tenant. Please contact with administrators.");
      }

      Properties props = new Properties();

      // SMTP protocol properties
      props.put("mail.transport.protocol", cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_TRANSPORT_PROTOCOL));

      props.put("mail.smtp.host", cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_HOST));
      props.put("mail.smtp.port", cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_PORT));
      props.put("mail.smtp.auth", cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_AUTH));

      Session mailSession;
      if (Boolean.parseBoolean(props.getProperty("mail.smtp.auth")))
      {
         props.put("mail.smtp.socketFactory.port",
            cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_SMTP_SOCKETFACTORY_PORT));
         props.put("mail.smtp.socketFactory.class",
            cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_SMTP_SOCKETFACTORY_CLASS));
         props.put("mail.smtp.socketFactory.fallback",
            cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_SMTP_SOCKETFACTORY_FALLBACK));

         final String mailUserName = cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_SMTP_AUTH_USERNAME);
         final String mailPassword = cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_SMTP_AUTH_PASSWORD);

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

      try
      {
         MimeMessage message = new MimeMessage(mailSession);
         message.setContent(body, "text/html");
         message.setHeader("Subject", subject);
         message.setFrom(new InternetAddress(cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_SENDER)));
         message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));

         Transport.send(message, message.getRecipients(Message.RecipientType.TO));
      }
      catch (NoSuchProviderException e)
      {
         LOG.error("Error during sending confirmation mail", e);
         throw new TenantRegistrationException(500, "Unable to create tenant. Please contact with administrators.");
      }
      catch (MessagingException e)
      {
         LOG.error("Error during sending confirmation mail", e);
         throw new TenantRegistrationException(500, "Unable to create tenant. Please contact with administrators.");
      }
   }
}
