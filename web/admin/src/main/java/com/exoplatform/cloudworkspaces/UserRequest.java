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
package com.exoplatform.cloudworkspaces;

public class UserRequest implements Cloneable
{
   private RequestState state;
   private String fileName;
   private String tenantName;
   private String userEmail;
   private String firstName;
   private String lastName;
   private String companyName;
   private String phone;
   private String password;
   private String confirmationId;
   private boolean isAdministrator;
   
   public UserRequest(String filename, String tenantName, String userMail, String firstName, String lastName,
      String companyName, String phone, String password, String confirmationId, boolean isAdministrator, RequestState state){
     this.fileName = filename;
     this.tenantName = tenantName;
     this.userEmail = userMail;
     this.firstName = firstName;
     this.lastName = lastName;
     this.companyName = companyName;
     this.phone = phone;
     this.password = password;
     this.confirmationId = confirmationId;
     this.isAdministrator = isAdministrator;
     this.state = state;
   } 
   

   public RequestState getState()
   {
      return state;
   }
   public void setState(RequestState state)
   {
      this.state = state;
   }
   public String getConfirmationId()
   {
      return confirmationId;
   }
   public void setConfirmationId(String confirmationId)
   {
      this.confirmationId = confirmationId;
   }
   public boolean isAdministrator()
   {
      return isAdministrator;
   }
   public void setAdministrator(boolean isAdministrator)
   {
      this.isAdministrator = isAdministrator;
   }
   public String getFileName()
   {
      return fileName;
   }
   public String getTenantName()
   {
      return tenantName;
   }
   public String getUserEmail()
   {
      return userEmail;
   }
   public String getFirstName()
   {
      return firstName;
   }
   public String getLastName()
   {
      return lastName;
   }
   public String getCompanyName()
   {
      return companyName;
   }
   public String getPhone()
   {
      return phone;
   }
   public String getPassword()
   {
      return password;
   }
   
   public UserRequest clone() {
      try
      {
         return (UserRequest)super.clone();
      }
      catch (CloneNotSupportedException e)
      {
         return this; 
      }
   }
}
