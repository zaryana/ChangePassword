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

import org.jclouds.ec2.domain.Volume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Tests to see if a volume is completed.
 * 
 */
public class VolumeAvailable implements Predicate<Volume>
{

   private static final Logger LOG = LoggerFactory.getLogger(VolumeAvailable.class);

   private final AWSRefresher refresher;

   public VolumeAvailable(AWSRefresher refresher)
   {
      this.refresher = refresher;
   }

   public boolean apply(Volume volume)
   {
      LOG.debug("looking for status on volume {}", volume.getId());
      Volume currentVolumeState = refresher.refresh(volume);

      LOG.debug("{}: looking for status {}: currently: {}", new Object[]{volume.getId(), Volume.Status.AVAILABLE,
         currentVolumeState.getStatus()});
      return currentVolumeState.getStatus() == Volume.Status.AVAILABLE;
   }

}