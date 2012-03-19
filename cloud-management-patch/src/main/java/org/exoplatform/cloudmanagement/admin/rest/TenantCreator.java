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
package org.exoplatform.cloudmanagement.admin.rest;

import static org.exoplatform.cloudmanagement.admin.configuration.AdminConfiguration.CLOUD_ADMIN_TENANT_BACKUP_ID;
import static org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration.CLOUD_ADMIN_MAIL_CONFIRMATION_SUBJECT;
import static org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration.CLOUD_ADMIN_MAIL_CONFIRMATION_TEMPLATE;
import static org.exoplatform.cloudmanagement.status.TenantInfoBuilder.tenant;

import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.TenantRegistrationException;
import org.exoplatform.cloudmanagement.admin.TenantValidationException;
import org.exoplatform.cloudmanagement.admin.WorkspacesMailSender;
import org.exoplatform.cloudmanagement.admin.configuration.TenantInfoFieldName;
import org.exoplatform.cloudmanagement.admin.dao.EmailValidationStorage;
import org.exoplatform.cloudmanagement.admin.dao.TenantInfoDataManager;
import org.exoplatform.cloudmanagement.admin.tenant.TenantNameValidator;
import org.exoplatform.cloudmanagement.admin.tenant.TenantStateDataManager;
import org.exoplatform.cloudmanagement.admin.tenant.UserMailValidator;
import org.exoplatform.cloudmanagement.admin.util.AdminConfigurationUtil;
import org.exoplatform.cloudmanagement.status.TenantState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Tenant creation service for public use with email authorization.
 * 
 * Algorithm of new tenant creation with email confirmation: <li>send a message
 * for specified email with URL including the "secret" token to activate your
 * tenant; <li>if e-mail is good user receive the message and enter the page
 * where you activate tenant and only after that it is created.
 */

@Path("/cloud-admin/public-tenant-service")
public class TenantCreator
{
   private static final Logger LOG = LoggerFactory.getLogger(TenantCreator.class);

   protected final Configuration adminConfiguration;

   protected final TenantNameValidator tenantNameValidator;

   protected final UserMailValidator userMailValidator;

   protected final TenantInfoDataManager tenantInfoDataManager;

   protected final EmailValidationStorage emailValidationStorage;

   protected final TenantStateDataManager tenantStateDataManager;

   protected final WorkspacesMailSender mailSender;

   public TenantCreator(EmailValidationStorage emailValidationStorage, TenantStateDataManager tenantStateDataManager,
      TenantNameValidator tenantNameValidator, UserMailValidator userMailValidator,
      TenantInfoDataManager tenantInfoDataManager, Configuration cloudAdminConfiguration,
      WorkspacesMailSender mailSender)
   {
      super();
      this.emailValidationStorage = emailValidationStorage;
      this.tenantStateDataManager = tenantStateDataManager;
      this.tenantNameValidator = tenantNameValidator;
      this.userMailValidator = userMailValidator;
      this.tenantInfoDataManager = tenantInfoDataManager;
      this.adminConfiguration = cloudAdminConfiguration;
      this.mailSender = mailSender;
   }

   /**
    * This service is the second step for creation new tenant. It checks if
    * received uuid registered in validation queue, if yes then gives for tenant
    * status WAITING_CREATION. In some time this tenant will be created.
    * 
    * <table>
    * <tr>
    * <th>Status</th>
    * <th>Error description</th>
    * </tr>
    * <tr>
    * <td>400</td>
    * <td>specified uuid is not registered in validation queue or war activated
    * earlier</td>
    * </tr>
    * <tr>
    * <td>400</td>
    * <td>tenant with specified name already exists</td>
    * </tr>
    * <tr>
    * <td>500</td>
    * <td>error on update tenant status</td>
    * </tr>
    * </table>
    * 
    * @param uuid
    *           - tenant identifier in validation queue
    * @return corresponded status 200
    * @throws CloudAdminException
    */
   @POST
   @Path("/create-confirmed")
   public Response createTenantWithConfirmedEmail(@QueryParam("id") String uuid) throws CloudAdminException
   {
      LOG.info("Received  tenant creation request  with id {}", uuid);

      if (!emailValidationStorage.isValid(uuid))
      {
         LOG.warn("Id {} unknown", uuid);
         return Response.status(Status.BAD_REQUEST)
            .entity("Your confirmation key is wrong or has already been activated.").build();
      }

      Map<String, String> validationData = emailValidationStorage.getValidationData(uuid);

      String tenantName = validationData.get(TenantInfoFieldName.PROPERTY_TENANT_NAME);
      String templateId = validationData.get(TenantInfoFieldName.PROPERTY_TEMPLATE_ID);
      if (templateId == null)
      {
         LOG.warn("TemplateId not found in validation data of tenant {}. Current TemplateId will be used", tenantName);
         templateId = adminConfiguration.getString(CLOUD_ADMIN_TENANT_BACKUP_ID);
      }

      String userMail = validationData.get(TenantInfoFieldName.PROPERTY_USER_MAIL);

      tenantNameValidator.validateTenantName(tenantName);
      userMailValidator.validateUserMail(userMail);
      if (tenantInfoDataManager.isExists(tenantName))
      {
         throw new TenantValidationException(" This domain is already in use. If you are the owner,"
            + " check your email for further instructions; otherwise, please select a different domain name.");
      }

      //save tenant state
      tenantStateDataManager.waitCreation(tenant(tenantName).templateId(templateId).adminMail(userMail).info());
      //remove validation information
      emailValidationStorage.remove(uuid);
      return Response.ok().build();
   }

   /**
    * This service is the first step for creation new tenant for user with
    * confirmed email address. It validates received data for tenant creation,
    * puts tenant metadata in validation queue and sends confirmation mail to
    * specified address.
    * 
    * <table>
    * <tr>
    * <th>Status</th>
    * <th>Error description</th>
    * </tr>
    * <tr>
    * <td>400</td>
    * <td>tenant with specified name already exists</td>
    * </tr>
    * <tr>
    * <td>400</td>
    * <td>tenant name is not correctly. It can't be null or ''; should contain
    * 20 or less characters; should contain lower cased Latin characters (a-z)
    * and digits (0-9); cannot be solely of digits; can't be in black list</td>
    * </tr>
    * <tr>
    * <td>400</td>
    * <td>user mail is not valid or in black list</td>
    * </tr>
    * <tr>
    * <td>500</td>
    * <td>template for confirmation mail not found</td>
    * </tr>
    * <tr>
    * <td>500</td>
    * <td>error during sending confirmation mail</td>
    * </tr>
    * </table>
    * 
    * @param tenantName
    *           - name for new tenant
    * @param userMail
    *           - email of tenant owner
    * @return correspondent status 200
    * @throws CloudAdminException
    */
   @POST
   @Path("/create-with-confirm/{tenantname}/{user-mail}")
   public Response createTenantWithEmailConfirmation(@PathParam("tenantname") String tenantName,
      @PathParam("user-mail") String userMail) throws CloudAdminException
   {
      LOG.info("Received tenant creation request for {} from {}", tenantName, userMail);
      Map<String, String> validationData = new HashMap<String, String>();
      validationData.put(TenantInfoFieldName.PROPERTY_TENANT_NAME, tenantName);
      validationData.put(TenantInfoFieldName.PROPERTY_USER_MAIL, userMail);
      validationData.put(TenantInfoFieldName.PROPERTY_STATE, TenantState.VALIDATING_EMAIL.toString());
      validationData.put(TenantInfoFieldName.PROPERTY_TEMPLATE_ID,
         adminConfiguration.getString(CLOUD_ADMIN_TENANT_BACKUP_ID));

      tenantNameValidator.validateTenantName(tenantName);
      userMailValidator.validateUserMail(userMail);
      if (tenantInfoDataManager.isExists(tenantName))
      {
         throw new TenantValidationException(" This domain is already in use. If you are the owner,"
            + " check your email for further instructions; otherwise, please select a different domain name.");
      }
      String validationId = emailValidationStorage.setValidationData(validationData);

      //send email
      String mailTemplate = adminConfiguration.getString(CLOUD_ADMIN_MAIL_CONFIRMATION_TEMPLATE);
      if (mailTemplate == null)
      {
         throw new TenantRegistrationException(500, "Mail template configuration not found. Please contact support.");
      }

      Map<String, String> props = new HashMap<String, String>();
      props.put("tenant.masterhost", AdminConfigurationUtil.getMasterHost(adminConfiguration));
      props.put("tenant.name", tenantName);
      props.put("user.mail", userMail);
      props.put("id", validationId);

      mailSender.sendMail(userMail, adminConfiguration.getString(CLOUD_ADMIN_MAIL_CONFIRMATION_SUBJECT), mailTemplate,
         props, false);

      return Response.ok(validationId).build();
   }

   public String createTenant(String tenantName, String userMail) throws CloudAdminException
   {
      Map<String, String> validationData = new HashMap<String, String>();
      validationData.put(TenantInfoFieldName.PROPERTY_TENANT_NAME, tenantName);
      validationData.put(TenantInfoFieldName.PROPERTY_USER_MAIL, userMail);
      validationData.put(TenantInfoFieldName.PROPERTY_STATE, TenantState.VALIDATING_EMAIL.toString());
      validationData.put(TenantInfoFieldName.PROPERTY_TEMPLATE_ID,
         adminConfiguration.getString(CLOUD_ADMIN_TENANT_BACKUP_ID));

      tenantNameValidator.validateTenantName(tenantName);
      userMailValidator.validateUserMail(userMail);
      if (tenantInfoDataManager.isExists(tenantName))
      {
         throw new TenantValidationException(" This domain is already in use. If you are the owner,"
            + " check your email for further instructions; otherwise, please select a different domain name.");
      }
      String validationId = emailValidationStorage.setValidationData(validationData);
      return validationId;
   }

}
