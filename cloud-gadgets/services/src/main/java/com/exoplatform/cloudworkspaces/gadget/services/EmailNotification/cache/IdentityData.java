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

package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.Event;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          vietnt@exoplatform.com
 * Aug 13, 2012  
 */
public class IdentityData implements CacheData<Set<Event>> {

  private final Set<Event> events;

  public IdentityData(final Set<Event> events) throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream os = new ObjectOutputStream(bos);
    os.writeObject(events);
    os.close();
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    ObjectInputStream oInputStream = new ObjectInputStream(bis);
    this.events = (Set<Event>) oInputStream.readObject();
  }

  public Set<Event> build() {
    return events;
  }

}