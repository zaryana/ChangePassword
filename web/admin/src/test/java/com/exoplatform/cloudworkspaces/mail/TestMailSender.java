package com.exoplatform.cloudworkspaces.mail;


import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.exoplatform.cloudworkspaces.MailingProperties;
import com.exoplatform.cloudworkspaces.NotificationMailSender;
import com.exoplatform.cloudworkspaces.dao.ModifiableEmailValidationStorage;
import com.exoplatform.cloudworkspaces.http.WorkspacesOrganizationRequestPerformer;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.everrest.assured.AvailablePortFinder;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.WorkspacesMailSender;
import org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration;
import org.exoplatform.cloudmanagement.admin.dao.EmailValidationStorage;
import org.exoplatform.cloudmanagement.admin.dao.TenantInfoDataManager;
import org.exoplatform.cloudmanagement.admin.tenant.TenantNameValidator;
import org.exoplatform.cloudmanagement.admin.tenant.UserMailValidator;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration.CLOUD_ADMIN_MAIL_SENDER;

/**
 * Created with IntelliJ IDEA.
 * User: makis
 * Date: 4/20/12
 * Time: 12:42 PM
 */
public class TestMailSender {

  private SimpleSmtpServer server;
  NotificationMailSender sender;
  Configuration cloudAdminConfiguration;
  WorkspacesOrganizationRequestPerformer requestPerformer;
  EmailValidationStorage emailValidationStorage;
  TenantNameValidator tenantNameValidator;
  UserMailValidator userMailValidator;
  TenantInfoDataManager tenantInfoDataManager;
  ModifiableEmailValidationStorage modifiableEmailValidationStorage;

  @BeforeMethod
  public void initMocks(){
    int smtpPort = AvailablePortFinder.getNextAvailable(2049);
    server = SimpleSmtpServer.start(smtpPort);
    cloudAdminConfiguration = new CompositeConfiguration();
    cloudAdminConfiguration.setProperty(MailConfiguration.CLOUD_ADMIN_MAIL_HOST, "localhost");
    cloudAdminConfiguration.setProperty(MailConfiguration.CLOUD_ADMIN_MAIL_PORT,
                                        Integer.toString(smtpPort));
    cloudAdminConfiguration.setProperty(MailConfiguration.CLOUD_ADMIN_MAIL_TRANSPORT_PROTOCOL,
                                        "smtp");
    cloudAdminConfiguration.setProperty(MailConfiguration.CLOUD_ADMIN_MAIL_AUTH, "false");
    cloudAdminConfiguration.setProperty(CLOUD_ADMIN_MAIL_SENDER, "admin@cw.com");
    cloudAdminConfiguration.setProperty("cloud.admin.mail.support.sender", "support@cw.com");
    requestPerformer = Mockito.mock(WorkspacesOrganizationRequestPerformer.class);

    WorkspacesMailSender wks_sender = new WorkspacesMailSender(cloudAdminConfiguration);
    sender = new NotificationMailSender(cloudAdminConfiguration,
                                        wks_sender,
                                        requestPerformer,
                                        emailValidationStorage,
                                        tenantNameValidator,
                                        userMailValidator,
                                        tenantInfoDataManager,
                                        modifiableEmailValidationStorage);
    cleanUpMails();
  }

  @Test
  public void testsOkToJoinEmail() throws Exception{
    final String MESSAGE_SUBJECT = "OkToJoin";
    Map<String, String> params = new HashMap<String, String>();
    params.put("test", "message");

    cloudAdminConfiguration.setProperty(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_TEMPLATE, "target/test-classes/template/OkToJoinEmail.html");
    cloudAdminConfiguration.setProperty(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_SUBJECT, MESSAGE_SUBJECT);
    sender.sendOkToJoinEmail("test@test.com", params);

    Assert.assertEquals(server.getReceivedEmailSize(), 1);
    Iterator emails = server.getReceivedEmail();
    SmtpMessage oneEmail = (SmtpMessage)emails.next();
    Assert.assertNotNull(oneEmail);
    String mailbody = oneEmail.getBody();
    Assert.assertNotNull(mailbody);
    Assert.assertTrue(mailbody.contains("test"));
    Assert.assertTrue(mailbody.contains("message"));
    Assert.assertTrue(oneEmail.getHeaderValue("Subject").contains("OkToJoin"));
    Assert.assertTrue(oneEmail.getHeaderValue("From").contains("support")); //its not an admin email
  }


  @Test
  public void testsSendJoinRejectedEmails() throws Exception{
    final String USER_SUBJECT = "JoinRejectedUser";
    final String OWNER_SUBJECT = "JoinRejectedOwner";
    final String SALES_SUBJECT = "JoinRejectedSales for ${company}";
    Map<String, String> params = new HashMap<String, String>();
    params.put("test", "message");
    params.put("tenant.repository.name", "aaa");
    Map<String, String> admins = new HashMap<String, String>();
    admins.put("admin1", "admin1@aaa.com");
    admins.put("root", "root@aaa.com");


    cloudAdminConfiguration.setProperty(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_CLOSED_USER_TEMPLATE, "target/test-classes/template/JoinRejectedEmail.html");
    cloudAdminConfiguration.setProperty(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_CLOSED_USER_SUBJECT, USER_SUBJECT);

    cloudAdminConfiguration.setProperty(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_CLOSED_OWNER_TEMPLATE, "target/test-classes/template/JoinRejectedEmail.html");
    cloudAdminConfiguration.setProperty(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_CLOSED_OWNER_SUBJECT, OWNER_SUBJECT);

    cloudAdminConfiguration.setProperty(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_CLOSED_SALES_TEMPLATE, "target/test-classes/template/JoinRejectedEmail.html");
    cloudAdminConfiguration.setProperty(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_CLOSED_SALES_SUBJECT, SALES_SUBJECT);
    cloudAdminConfiguration.setProperty(MailingProperties.CLOUD_ADMIN_MAIL_SALES_EMAIL, "testsales@test.com");

    Mockito.when(requestPerformer.getTenantAdministrators(Matchers.anyString())).thenReturn(admins);

    sender.sendJoinRejectedEmails("aaa", "test@test.com", params);

    Assert.assertEquals(server.getReceivedEmailSize(), 3);
    Iterator emails = server.getReceivedEmail();
    while (emails.hasNext()) {
      SmtpMessage oneEmail = (SmtpMessage)emails.next();
      Assert.assertNotNull(oneEmail);
      String mailbody = oneEmail.getBody();
      Assert.assertNotNull(mailbody);
      Assert.assertTrue(mailbody.contains("test"));
      Assert.assertTrue(mailbody.contains("message"));
      if(oneEmail.getHeaderValue("Subject").contains("JoinRejectedSales"))
        Assert.assertTrue(oneEmail.getHeaderValue("From").contains("admin")); //its an admin email
      else
        Assert.assertTrue(oneEmail.getHeaderValue("From").contains("support")); //another is not an admin email
    }
  }

  @Test
  public void testsSendUserJoinedEmails() throws Exception{
    final String USER_SUBJECT = "UserJoinedUser";
    final String OWNER_SUBJECT = "UserJoinedOwner";
    Map<String, String> params = new HashMap<String, String>();
    params.put("test", "message");
    params.put("tenant.repository.name", "aaa");
    Map<String, String> admins = new HashMap<String, String>();
    admins.put("admin1", "admin1@aaa.com");
    admins.put("root", "root@aaa.com");

    cloudAdminConfiguration.setProperty(MailingProperties.CLOUD_ADMIN_MAIL_USER_JOINED_TEMPLATE, "target/test-classes/template/UserJoinedEmail.html");
    cloudAdminConfiguration.setProperty(MailingProperties.CLOUD_ADMIN_MAIL_USER_JOINED_SUBJECT, USER_SUBJECT);

    cloudAdminConfiguration.setProperty(MailingProperties.CLOUD_ADMIN_MAIL_USER_JOINED_OWNER_TEMPLATE, "target/test-classes/template/UserJoinedEmail.html");
    cloudAdminConfiguration.setProperty(MailingProperties.CLOUD_ADMIN_MAIL_USER_JOINED_OWNER_SUBJECT, OWNER_SUBJECT);

    Mockito.when(requestPerformer.getTenantAdministrators(Matchers.anyString())).thenReturn(admins);

    sender.sendUserJoinedEmails("aaa", "fName", "test@test.com", params);

    Assert.assertEquals(server.getReceivedEmailSize(), 2);
    Iterator emails = server.getReceivedEmail();
    while (emails.hasNext()) {
      SmtpMessage oneEmail = (SmtpMessage)emails.next();
      Assert.assertNotNull(oneEmail);
      String mailbody = oneEmail.getBody();
      Assert.assertNotNull(mailbody);
      Assert.assertTrue(mailbody.contains("test"));
      Assert.assertTrue(mailbody.contains("message"));
      Assert.assertTrue(oneEmail.getHeaderValue("From").contains("support")); //its not an admin email
    }
  }


  @Test
  public void testIntranetCreatedEmail() throws Exception{
    final String MESSAGE_SUBJECT = "IntranetCreated";
    Map<String, String> params = new HashMap<String, String>();
    params.put("test", "message");
    params.put("tenant.repository.name", "aaa");

    cloudAdminConfiguration.setProperty(MailingProperties.CLOUD_ADMIN_MAIL_USER_INTRANET_CREATED_TEMPLATE, "target/test-classes/template/IntranetCreatedEmail.html");
    cloudAdminConfiguration.setProperty(MailingProperties.CLOUD_ADMIN_MAIL_USER_INTRANET_CREATED_SUBJECT, MESSAGE_SUBJECT);
    sender.sendIntranetCreatedEmail("test@test.com", params);

    //Thread.sleep(2000);
    Assert.assertEquals(server.getReceivedEmailSize(), 1);
    Iterator emails = server.getReceivedEmail();
    SmtpMessage oneEmail = (SmtpMessage)emails.next();
    Assert.assertNotNull(oneEmail);
    String mailbody = oneEmail.getBody();
    Assert.assertNotNull(mailbody);
    Assert.assertTrue(mailbody.contains("test"));
    Assert.assertTrue(mailbody.contains("message"));
    Assert.assertTrue(oneEmail.getHeaderValue("Subject").contains("IntranetCreated"));
    Assert.assertTrue(oneEmail.getHeaderValue("From").contains("support")); //its not an admin email
  }


  @Test
  public void testContactUsEmail() throws Exception{

    cloudAdminConfiguration.setProperty(MailingProperties.CLOUD_ADMIN_MAIL_CONTACT_TEMPLATE, "target/test-classes/template/ContactUsEmail.html");
    sender.sendContactUsEmail("test@test.com", "fname", "subject", "contact-us text");

    //Thread.sleep(2000);
    Assert.assertEquals(server.getReceivedEmailSize(), 1);
    Iterator emails = server.getReceivedEmail();
    SmtpMessage oneEmail = (SmtpMessage)emails.next();
    Assert.assertNotNull(oneEmail);
    String mailbody = oneEmail.getBody();
    Assert.assertNotNull(mailbody);
    Assert.assertTrue(mailbody.contains("fname"));
    Assert.assertTrue(mailbody.contains("contact-us text"));
    Assert.assertTrue(oneEmail.getHeaderValue("From").contains("support")); //its not an admin email
  }


  @Test
  public void testPasswordRestoreEmail() throws Exception{

    cloudAdminConfiguration.setProperty(MailingProperties.CLOUD_ADMIN_MAIL_PASSWORD_RESTORE_SUBJECT, "PasswordRestoreSubject");
    cloudAdminConfiguration.setProperty(MailingProperties.CLOUD_ADMIN_MAIL_PASSWORD_RESTORE_TEMPLATE, "target/test-classes/template/PasswordRestoreEmail.html");
    sender.sendPasswordRestoreEmail("test@test.com", "aaa", "123-456-789");

    //Thread.sleep(2000);
    Assert.assertEquals(server.getReceivedEmailSize(), 1);
    Iterator emails = server.getReceivedEmail();
    SmtpMessage oneEmail = (SmtpMessage)emails.next();
    Assert.assertNotNull(oneEmail);
    String mailbody = oneEmail.getBody();
    Assert.assertNotNull(mailbody);
    Assert.assertTrue(mailbody.contains("123-456-789"));
    Assert.assertTrue(oneEmail.getHeaderValue("Subject").contains("PasswordRestoreSubject"));
    Assert.assertTrue(oneEmail.getHeaderValue("From").contains("support")); //its not an admin email
  }


  @Test
  public void testAdminErrorEmail() throws Exception{

    cloudAdminConfiguration.setProperty(MailConfiguration.CLOUD_ADMIN_MAIL_SENDER, "admin@test.com");
    cloudAdminConfiguration.setProperty(MailConfiguration.CLOUD_ADMIN_MAIL_ADMIN_EMAIL, "aaa@test.com, bbb@test.com");

    cloudAdminConfiguration.setProperty(MailConfiguration.CLOUD_ADMIN_MAIL_ADMIN_ERROR_SUBJECT, "AdminErrorSubject");
    cloudAdminConfiguration.setProperty(MailConfiguration.CLOUD_ADMIN_MAIL_ADMIN_ERROR_TEMPLATE, "target/test-classes/template/AdminErrorEmail.html");
    sender.sendAdminErrorEmail("Test Message", new CloudAdminException("Test Exception"));

    Assert.assertEquals(server.getReceivedEmailSize(), 2);
    Iterator emails = server.getReceivedEmail();
    SmtpMessage oneEmail = (SmtpMessage)emails.next();
    Assert.assertNotNull(oneEmail);
    String mailbody = oneEmail.getBody();
    Assert.assertNotNull(mailbody);
    Assert.assertTrue(mailbody.contains("Test Message"));
    Assert.assertTrue(mailbody.contains("Test Exception"));
    Assert.assertTrue(oneEmail.getHeaderValue("Subject").contains("AdminErrorSubject"));
    Assert.assertTrue(oneEmail.getHeaderValue("From").contains("admin")); // its
                                                                          // an
                                                                          // admin
                                                                          // email
  }

  @Test
  public void testSendMailToValidation() throws CloudAdminException {

    sender.sendCustomEmail("userMail",
                           "tenantName",
                           "uuid",
                           "target/test-classes/template/ValidationEmail.html",
                           "subject");

    Assert.assertEquals(server.getReceivedEmailSize(), 1);
    Iterator emails = server.getReceivedEmail();
    SmtpMessage oneEmail = (SmtpMessage) emails.next();
    Assert.assertNotNull(oneEmail);
    String mailbody = oneEmail.getBody();
    Assert.assertNotNull(mailbody);
    Assert.assertTrue(mailbody.contains("uuid"));
    Assert.assertTrue(oneEmail.getHeaderValue("From").contains("support"));
  }

  @AfterMethod
  public void cleanUpMails() {
    Assert.assertNotNull(server);
    if (server.getReceivedEmailSize() > 0) {
      Iterator it = server.getReceivedEmail();
      while (it.hasNext()) {
        it.next();
        it.remove();
      }
    }

  }
}
