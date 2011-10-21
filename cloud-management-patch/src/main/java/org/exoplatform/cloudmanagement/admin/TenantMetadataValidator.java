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

import org.exoplatform.cloudmanagement.admin.status.CloudInfoHolder;
import org.exoplatform.cloudmanagement.status.TenantStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * 
 *
 */
public class TenantMetadataValidator
{
   public static final int MAX_TENANT_NAME_LENGTH = 20;

   public static final Pattern TENANT_NAME_PATTERN = Pattern.compile("[a-z\\d]*[a-z]+[a-z\\d]*");

   private static final Logger LOG = LoggerFactory.getLogger(TenantMetadataValidator.class);

   private final CloudInfoHolder cloudInfoHolder;

   public TenantMetadataValidator(CloudInfoHolder cloudInfoHolder)
   {
      this.cloudInfoHolder = cloudInfoHolder;
   }

   public void validate(TenantStatus tenantStatus) throws TenantValidationException
   {
      validateTenantName(tenantStatus);
      validateUserMail(tenantStatus);
      validateTenantAlreadyExists(tenantStatus);
   }

   public void validateTenantAlreadyExists(TenantStatus tenantStatus) throws TenantValidationException
   {
      if (cloudInfoHolder.isTenantExists(tenantStatus.getTenantName()))
      {
         throw new TenantAlreadyExistException(" This domain is already in use. If you are the owner,"
            + " check your email for further instructions; otherwise, please select a different domain name.");
      }
   }

   public void validateTenantName(TenantStatus tenantStatus) throws TenantValidationException
   {

      String tenantName = tenantStatus.getTenantName();
      if (tenantName == null || tenantName.length() == 0)
      {
         throw new TenantValidationException("Network name can't be null or ''");
      }

      // 'www' should be forbidden tenant name 
      if (tenantName.equals("www"))
      {
         throw new TenantValidationException("'www' is forbidden name for network.");
      }

      if (MAX_TENANT_NAME_LENGTH < tenantName.length())
      {
         throw new TenantValidationException("Network name should contain " + MAX_TENANT_NAME_LENGTH
            + " or less characters");
      }

      // Check tenant name for invalid characters.
      Matcher matcher = TENANT_NAME_PATTERN.matcher(tenantName);
      if (!matcher.matches())
      {
         if (LOG.isDebugEnabled())
         {
            LOG.debug("Network name: " + tenantName
               + " should contain only lower-case characters (a-z) and/or digits (0-9); at least one character is required.");
         }
         throw new TenantValidationException("Network name: " + tenantName
            + " should contain only lower-case characters (a-z) and/or digits (0-9); at least one character is required.");
      }

   }

   public void validateUserMail(TenantStatus tenantStatus) throws TenantValidationException
   {
      String userMail = tenantStatus.getProperty(TenantStatus.PROPERTY_USER_MAIL);
      if (userMail != null && userMail.length() > 0)
      {
         try
         {
            InternetAddress address = new InternetAddress(userMail);
            address.validate();
         }
         catch (AddressException e)
         {
            throw new TenantValidationException("E-Mail validation failed. Please check the format of your e-mail address.");
         }
      }
   }
}
