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
package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          vietnt@exoplatform.com
 * Jul 19, 2012  
 */
public class Event implements Serializable {

  private String identity;

  private long   createdDate;
  
  private Map<String, String> attributes = new HashMap<String, String>();

  public Event(String identity, long createdDate) {
    this.identity = identity;
    this.createdDate = createdDate;
  }
  
  public String getIdentity() {
    return identity;
  }

  public void setIdentity(String identity) {
    this.identity = identity;
  }

  public long getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(long createdDate) {
    this.createdDate = createdDate;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  public final boolean equals(Object obj) {
    if (obj != null && obj instanceof Event) {
      return this.getIdentity().equals(((Event) obj).getIdentity());
    }
    return false;
  }
  
  public final int hashCode() {
    return this.getIdentity().hashCode();
  }

}
