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

import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;

public class HashProvider
{
   private static final Logger LOG = LoggerFactory.getLogger(HashProvider.class);
   
   public static String getHash(String email) throws CloudAdminException{
      
      String hashFileName = System.getProperty("cloud.admin.hashfile.dir");
      File hashDir = new File(hashFileName);
      if (!hashDir.exists())
        hashDir.mkdir();
      try
      {
         File file = new File(hashDir + "requests.properties");
         if (!file.exists())
            return null;
         FileInputStream io = new FileInputStream(file);
         Properties properties = new Properties();
         properties.load(io);
         io.close();
         return properties.getProperty(email);
      }
      catch (IOException e)
      {
         LOG.error("Email-hash I/O exception.", e);
         throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");
      }
   }
   
   
   public static String getEmail(String hash) throws CloudAdminException{
      String hashFileName = System.getProperty("cloud.admin.hashfile.dir");
      File hashDir = new File(hashFileName);
      if (!hashDir.exists())
         hashDir.mkdir();
      try
      {
         File file = new File(hashDir + "requests.properties");
         if (!file.exists())
            return null;
         FileInputStream io = new FileInputStream(file);
         Properties properties = new Properties();
         properties.load(io);
         io.close();
         Enumeration<String> propKeys = (Enumeration<String>)properties.propertyNames();
         while (propKeys.hasMoreElements()){
            String one = propKeys.nextElement();
            if (properties.getProperty(one).equals(hash))
               return one;
         }
      }
      catch (IOException e)
      {
         LOG.error("Email-hash I/O exception.", e);
         throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");
      }
      return null;
   }
      

   public static String putEmail(String email) throws CloudAdminException{
      String hashFileName = System.getProperty("cloud.admin.hashfile.dir");
      File hashDir = new File(hashFileName);
      if (!hashDir.exists())
         hashDir.mkdir();
      try
      {
         File file = new File(hashDir + "requests.properties");
         if (!file.exists())
            file.createNewFile();
         FileInputStream io = new FileInputStream(file);
         Properties properties = new Properties();
         properties.load(io);
         io.close();
         String uuid = generateHash();
         properties.setProperty(email, uuid );
         properties.store(new FileOutputStream(file), "");
         return uuid;
      }
      catch (IOException e)
      {
         LOG.error("Email-hash I/O exception.", e);
         throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");
      }
      
   }
   
   public static void removeEmail(String email) throws CloudAdminException{
      String hashFileName = System.getProperty("cloud.admin.hashfile.dir");
      File hashDir = new File(hashFileName);
      if (!hashDir.exists())
         hashDir.mkdir();
      try
      {
         File file = new File(hashDir + "requests.properties");
         if (!file.exists())
            return;
         FileInputStream io = new FileInputStream(file);
         Properties properties = new Properties();
         properties.load(io);
         io.close();
         properties.remove(email);
         properties.store(new FileOutputStream(file), "");
      }
      catch (IOException e)
      {
         LOG.error("Email-hash I/O exception.", e);
         throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");
      }
   }
   
   private static String generateHash(){
      return UUID.randomUUID().toString();
   } 

}
