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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;


/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          vietnt@exoplatform.com
 * Jul 18, 2012  
 */
public class EmailNotificationStorage {

  private final static Log LOG = ExoLogger.getLogger(EmailNotificationStorage.class);

  private static final String DEFAULT_TENANT_NAME = Long.toHexString(System.currentTimeMillis() + System.identityHashCode("currentTenant"));

  private Map<String, Map<String, Map<String, Set<Event>>>> eventStorage = new HashMap<String, Map<String, Map<String, Set<Event>>>>();

  public Set<Event> getEvents(Plugin plugin, String user) {
    if (user == null) {
      Set<Event> events = new HashSet<Event>();
      for (Set<Event> set : getEventStorageOfPlugin(plugin).values()) {
        events.addAll(set);
      }
      return events;
    }
    if (getEventStorageOfPlugin(plugin).get(user) == null) {
      getEventStorageOfPlugin(plugin).put(user, new HashSet<Event>());
    }
    return getEventStorageOfPlugin(plugin).get(user);
  }
  
  private Map<String, Map<String, Set<Event>>> getEventStorageOfCurrentTenant() {
    String currentTenant = getCurrentTenantName();
    if (eventStorage.get(currentTenant) == null) {
      eventStorage.put(currentTenant, new HashMap<String, Map<String, Set<Event>>>());
    }
    return eventStorage.get(currentTenant);
  }

  private Map<String, Set<Event>> getEventStorageOfPlugin(Plugin plugin) {
    if (getEventStorageOfCurrentTenant().get(plugin.name()) == null) {
      getEventStorageOfCurrentTenant().put(plugin.name(), new HashMap<String, Set<Event>>());
    }
    return getEventStorageOfCurrentTenant().get(plugin.name());
  }

  static private String getCurrentTenantName() {
    RepositoryService repositoryService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    try {
      return repositoryService.getCurrentRepository().getConfiguration().getName();
    } catch (RepositoryException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Can not get current repository", e);
      }
    }
    return DEFAULT_TENANT_NAME;
  }

}
