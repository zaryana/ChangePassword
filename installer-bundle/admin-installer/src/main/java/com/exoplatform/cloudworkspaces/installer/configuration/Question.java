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
package com.exoplatform.cloudworkspaces.installer.configuration;

public class Question {

  protected String parameter;

  protected String description;

  protected String defaults;

  protected String validateRegexp;

  protected String validateMessage;

  public Question() {
  }

  public Question(String parameter,
                  String description,
                  String defaults,
                  String validateRegexp,
                  String validateMessage) {
    this.parameter = parameter;
    this.description = description;
    this.defaults = defaults;
    this.validateRegexp = validateRegexp;
    this.validateMessage = validateMessage;
  }

  public String getParameter() {
    return parameter;
  }

  public void setParameter(String parameter) {
    this.parameter = parameter;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDefaults() {
    return defaults;
  }

  public void setDefaults(String defaults) {
    this.defaults = defaults;
  }

  public String getValidateRegexp() {
    return validateRegexp;
  }

  public void setValidateRegexp(String validateRegexp) {
    this.validateRegexp = validateRegexp;
  }

  public String getValidateMessage() {
    return validateMessage;
  }

  public void setValidateMessage(String validateMessage) {
    this.validateMessage = validateMessage;
  }

}
