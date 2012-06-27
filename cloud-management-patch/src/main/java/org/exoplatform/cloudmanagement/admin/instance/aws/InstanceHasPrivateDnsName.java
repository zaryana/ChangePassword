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
package org.exoplatform.cloudmanagement.admin.instance.aws;

import com.google.common.base.Predicate;

import org.jclouds.aws.AWSResponseException;
import org.jclouds.ec2.domain.RunningInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test RunningInstance predicate for getting private dns name.
 */
public class InstanceHasPrivateDnsName implements Predicate<RunningInstance>
{

   private static final Logger LOG = LoggerFactory.getLogger(InstanceHasPrivateDnsName.class);

   private final AWSRefresher refresher;

   public InstanceHasPrivateDnsName(AWSRefresher refresher)
   {
      this.refresher = refresher;
   }

   public boolean apply(RunningInstance instance)
   {
      LOG.debug("looking for ipAddress on instance {}", instance.getId());
      try
      {
         instance = refresher.refresh(instance);
         return instance.getPrivateDnsName() != null;
      }
      catch (AWSResponseException e)
      {
         if (e.getError().getCode().equals("InvalidInstanceID.NotFound"))
         {
            return false;
         }
         throw e;
      }
   }
}
