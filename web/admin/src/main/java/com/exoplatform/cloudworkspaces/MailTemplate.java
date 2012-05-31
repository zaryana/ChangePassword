/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.exoplatform.cloudworkspaces;

import java.util.Map;

import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.status.TenantStatus;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: MailTemplate.java 34360 2012-05-31 10:19:59Z pnedonosko $
 */
public abstract class MailTemplate {

  protected final String name;

  protected final String subject;

  /**
   * Create template with given name.
   * 
   * @param name String name
   */
  public MailTemplate(String subject, String name) {
    this.name = name;
    this.subject = subject;
  }

  /**
   * Subject to use with this template.
   * 
   * @return Strung with subject text
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Template name.
   * 
   * @return String with template name
   */
  public String getName() {
    return name;
  }

  /**
   * Provides mapping for keys in the template.
   * 
   * @param tenant TenantStatus target tenant
   * @throws CloudAdminException if cannot read the tenant status
   * @return Map of String pairs with key-value for template processing.
   */
  public abstract Map<String, String> mapping(TenantStatus tenant) throws CloudAdminException;

}
