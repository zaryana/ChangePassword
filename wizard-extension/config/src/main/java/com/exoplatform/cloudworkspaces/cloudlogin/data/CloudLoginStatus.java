/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.exoplatform.cloudworkspaces.cloudlogin.data;

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

/**
 * <ul>
 * <li>NONE = no JCR folder, never login</li>
 * <li>INITIATED = Screens process currently running, JCR folder exists with value "INITIATED"</li>
 * <li>DISPLAYED = Screens displayed and viewed by user, end of process, JCR folder exists with value "DISPLAYED"</li>
 * </ul>
 * 
 * @author Clement
 *
 */
public enum CloudLoginStatus implements Value {
  NONE,
  INITIATED,
  DISPLAYED;
  
  public static CloudLoginStatus getCloudLoginStatus(String str) throws ValueFormatException, IllegalStateException, RepositoryException {
    CloudLoginStatus cloudLoginStatus = NONE;
    
    if(str != null && str.length() > 0) {
      for(CloudLoginStatus status : CloudLoginStatus.values()) {
        if(status.getString().equals(str)) {
          cloudLoginStatus = status;
          break;
        }
      }
    }
    
    return cloudLoginStatus;
  }

  @Override
  public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
    return this.toString();
  }

  @Override
  public InputStream getStream() throws IllegalStateException, RepositoryException {
    return null;
  }

  @Override
  public long getLong() throws ValueFormatException, IllegalStateException, RepositoryException {
    return 0;
  }

  @Override
  public double getDouble() throws ValueFormatException, IllegalStateException, RepositoryException {
    return 0;
  }

  @Override
  public Calendar getDate() throws ValueFormatException, IllegalStateException, RepositoryException {
    return null;
  }

  @Override
  public boolean getBoolean() throws ValueFormatException, IllegalStateException, RepositoryException {
   return false;
  }

  @Override
  public int getType() {
    return 0;
  }
}
