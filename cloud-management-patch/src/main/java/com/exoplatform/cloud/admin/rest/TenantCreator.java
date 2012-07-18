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
package com.exoplatform.cloud.admin.rest;

import static com.exoplatform.cloud.admin.configuration.AdminConfiguration.CLOUD_ADMIN_TENANT_BACKUP_ID;

import com.exoplatform.cloud.admin.CloudAdminException;
import com.exoplatform.cloud.admin.TenantValidationException;
import com.exoplatform.cloud.admin.configuration.TenantInfoFieldName;
import com.exoplatform.cloud.admin.dao.EmailValidationStorage;
import com.exoplatform.cloud.admin.dao.TenantInfoDataManager;
import com.exoplatform.cloud.admin.tenant.TenantNameValidator;
import com.exoplatform.cloud.admin.tenant.TenantStateDataManager;
import com.exoplatform.cloud.admin.tenant.UserMailValidator;
import com.exoplatform.cloud.admin.util.MailSender;
import com.exoplatform.cloud.status.TenantState;

import org.apache.commons.configuration.Configuration;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/cloud-admin/public-tenant-service")
public class TenantCreator extends TenantCreatorWithEmailAuthorization
{
   public TenantCreator(EmailValidationStorage emailValidationStorage, TenantStateDataManager tenantStateDataManager,
      TenantNameValidator tenantNameValidator, UserMailValidator userMailValidator,
      TenantInfoDataManager tenantInfoDataManager, Configuration cloudAdminConfiguration, MailSender mailSender)
   {
      super(emailValidationStorage, tenantStateDataManager, tenantNameValidator, userMailValidator,
         tenantInfoDataManager, cloudAdminConfiguration, mailSender);
   }

   @POST
   @Path("/create-confirmed")
   @Override
   public Response createTenantWithConfirmedEmail(@QueryParam("id") String uuid) throws CloudAdminException
   {
      return super.createTenantWithConfirmedEmail(uuid);
   }

   @POST
   @Path("/create-with-confirm/{tenantname}/{user-mail}")
   @Override
   public Response createTenantWithEmailConfirmation(@PathParam("tenantname") String tenantName,
      @PathParam("user-mail") String userMail) throws CloudAdminException
   {
      return super.createTenantWithEmailConfirmation(tenantName, userMail);
   }

   /**
    * Create tenant request record in Cloud Admin and return Id of this request.
    * This method doesn't send any email messages to an user.
    * 
    * @param tenantName
    *           String, requested tenant name
    * @param userMail
    *           String, user email address
    * @return String with tenant request Id
    * @throws CloudAdminException
    *            if error occurs
    */
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
