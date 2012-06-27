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
package com.exoplatform.cloudworkspaces.organization.listener;

/**
 * Created by The eXo Platform SAS.
 * 
 *   
 */
public class UserLimitException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 2783841994652825261L;

  public UserLimitException(String message, Throwable cause) {
    super(message, cause);
  }

  public UserLimitException(String message) {
    super(message);
  }
  
}
