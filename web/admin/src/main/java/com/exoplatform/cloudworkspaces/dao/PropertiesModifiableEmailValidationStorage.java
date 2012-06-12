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
package com.exoplatform.cloudworkspaces.dao;

import static org.exoplatform.cloudmanagement.admin.configuration.AdminConfiguration.CLOUD_ADMIN_TENANT_QUEUE_DIR;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.dao.file.PropertiesEmailValidationStorage;
import org.exoplatform.cloudmanagement.admin.dao.file.PropertiesStorage;

public class PropertiesModifiableEmailValidationStorage extends PropertiesEmailValidationStorage
    implements ModifiableEmailValidationStorage {

  public final static String FOLDER_NAME_IN_QUEUE_DIR = "validation";

  private PropertiesStorage  storage;

  public PropertiesModifiableEmailValidationStorage(Configuration adminConfiguration) throws IOException {
    super(adminConfiguration);
    this.storage = new PropertiesStorage(new File(adminConfiguration.getString(CLOUD_ADMIN_TENANT_QUEUE_DIR)),
                                         FOLDER_NAME_IN_QUEUE_DIR);
  }

  /**
   * @see org.exoplatform.cloudmanagement.admin.dao.EmailValidationStorage#setValidationData(java.util.Map)
   */
  @Override
  public boolean setValidationData(String uuid, Map<String, String> validationData) throws CloudAdminException {
    try {
      storage.set(uuid, validationData);
    } catch (IOException e) {
      throw new CloudAdminException(e.getLocalizedMessage(), e);
    }
    return true;

  }

}
