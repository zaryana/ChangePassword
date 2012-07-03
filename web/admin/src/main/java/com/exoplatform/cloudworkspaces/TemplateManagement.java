/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package com.exoplatform.cloudworkspaces;

import static org.exoplatform.cloudmanagement.admin.configuration.AdminConfiguration.CLOUD_ADMIN_TENANT_BACKUP_ID;
import static org.exoplatform.cloudmanagement.admin.configuration.AdminConfiguration.CLOUD_ADMIN_TENANT_QUEUE_DIR;

import com.exoplatform.cloudworkspaces.dao.ModifiableEmailValidationStorage;

import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.configuration.TenantInfoFieldName;
import org.exoplatform.cloudmanagement.admin.dao.TenantDataManagerException;
import org.exoplatform.cloudmanagement.admin.dao.TenantInfoDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class TemplateManagement {

  private static final Logger                    LOG = LoggerFactory.getLogger(TemplateManagement.class);

  private final Configuration                    cloudAdminConfiguration;

  private final ModifiableEmailValidationStorage modifiableEmailValidationStorage;

  public TemplateManagement(Configuration cloudAdminConfiguration,
                            TenantInfoDataManager tenantInfoDataManager,
                            ModifiableEmailValidationStorage modifiableEmailValidationStorage) {
    this.cloudAdminConfiguration = cloudAdminConfiguration;
    this.modifiableEmailValidationStorage = modifiableEmailValidationStorage;
  }

  /**
   * Update Template Id for all tenants on validation to a currently used by the
   * cloud, see parameter <i>cloud.admin.tenant.backup.id</i> in admin
   * configuration.
   * 
   * @throws CloudAdminException in case if validation storage not found
   */
  public void updateTemplateId() throws CloudAdminException {

    int counter = 0;

    String newTemplateId = cloudAdminConfiguration.getString(CLOUD_ADMIN_TENANT_BACKUP_ID);

    LOG.info("Updating tenants on validation to current Template Id " + newTemplateId);

    final File tenantQueueDir = new File(cloudAdminConfiguration.getString(CLOUD_ADMIN_TENANT_QUEUE_DIR),
                                         "validation");
    if (!tenantQueueDir.exists()) {
      LOG.error("Queue storage " + tenantQueueDir.getAbsolutePath() + " not found");
      throw new CloudAdminException(500, "Cannot read queue storage. Contact administrators.");
    }

    String listForTenantQueueDir[] = tenantQueueDir.list();

    for (String id : listForTenantQueueDir) {

      String uuid = id.substring(0, id.indexOf('.'));

      Map<String, String> validationData = modifiableEmailValidationStorage.getValidationData(uuid);
      validationData.put(TenantInfoFieldName.PROPERTY_TEMPLATE_ID, newTemplateId);

      try {
        if (!modifiableEmailValidationStorage.setValidationData(uuid, validationData)) {
          LOG.error("Cannot update templateId");
        }
      } catch (TenantDataManagerException e) {
        throw new CloudAdminException(e.getLocalizedMessage(), e);
      }
      counter++;
    }
    LOG.info("Template Id update to tenants on validation. " + "Update to " + counter + " users");
  }
}
