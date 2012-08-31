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

import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.Plugin;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          vietnt@exoplatform.com
 * Aug 13, 2012  
 */
public class IdentityKey extends ScopeCacheKey {

  private final Plugin plugin;

  private final String user;

  public IdentityKey(final Plugin plugin, final String user) {
    this.plugin = plugin;
    this.user = user;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IdentityKey)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    IdentityKey that = (IdentityKey) o;

    if (plugin != that.plugin) {
      return false;
    }

    if (user != null ? !user.equals(that.user) : that.user != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * 31 * result + 31 * (plugin != null ? plugin.hashCode() : 0) + (user != null ? user.hashCode() : 0);
    return result;
  }

}
