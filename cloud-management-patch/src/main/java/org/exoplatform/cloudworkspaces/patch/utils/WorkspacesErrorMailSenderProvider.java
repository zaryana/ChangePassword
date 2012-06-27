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
package org.exoplatform.cloudworkspaces.patch.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * This component uses as factory to send emails to admin about errors in CM to
 * avoid setting more dependencies to patched components.
 */
public class WorkspacesErrorMailSenderProvider
{

   private static final Logger LOG = LoggerFactory.getLogger(WorkspacesErrorMailSender.class);

   private static volatile WorkspacesErrorMailSender instance;

   private static final long errorLifeTime = 10 * 60 * 1000;

   private static final HashMap<String, Long> lastTimeMap = new HashMap<String, Long>();

   public static void setInstance(WorkspacesErrorMailSender mailSender)
   {
      instance = mailSender;
   }

   public static synchronized void sendErrorToAdminIfNew(String errorUID, String subject, String body)
   {
      collecterrors();
      if (instance != null)
      {
         if (isNeedToSend(errorUID))
            instance.sendErrorToAdmin(subject, body);
      }
      else
         LOG.error("WorkspacesErrorMailSender not initialized yet!");
   }

   public static synchronized void sendErrorToAdminIfNew(String errorUID, String message, Exception cause)
   {
      collecterrors();
      if (instance != null)
      {
         if (isNeedToSend(errorUID))
            instance.sendErrorToAdmin(message, cause);
      }
      else
         LOG.error("WorkspacesErrorMailSender not initialized yet!");
   }

   public static synchronized void sendErrorToAdmin(String subject, String body)
   {
      collecterrors();
      if (instance != null)
      {
         instance.sendErrorToAdmin(subject, body);
      }
      else
         LOG.error("WorkspacesErrorMailSender not initialized yet!");
   }

   public static synchronized void sendErrorToAdmin(String message, Exception cause)
   {
      collecterrors();
      if (instance != null)
      {
         instance.sendErrorToAdmin(message, cause);
      }
      else
         LOG.error("WorkspacesErrorMailSender not initialized yet!");
   }

   private static boolean isNeedToSend(String errorUID)
   {
      if (lastTimeMap.containsKey(errorUID))
      {
         long lastTime = lastTimeMap.get(errorUID);
         long currentTime = System.currentTimeMillis();
         if (currentTime - lastTime < errorLifeTime)
         {
            lastTimeMap.put(errorUID, currentTime);
            return false;
         }
      }
      lastTimeMap.put(errorUID, System.currentTimeMillis());
      return true;
   }

   private static void collecterrors()
   {
      ArrayList<String> keysToRemove = new ArrayList<String>();

      for (Entry<String, Long> entry : lastTimeMap.entrySet())
      {
         if (System.currentTimeMillis() - entry.getValue() > errorLifeTime)
         {
            keysToRemove.add(entry.getKey());
         }
      }
      for (String key : keysToRemove)
      {
         lastTimeMap.remove(key);
      }
   }

}
