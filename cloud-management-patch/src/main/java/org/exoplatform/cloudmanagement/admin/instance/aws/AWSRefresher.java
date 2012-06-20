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

import com.google.common.collect.Iterables;

import org.jclouds.ec2.EC2Client;
import org.jclouds.ec2.domain.RunningInstance;
import org.jclouds.ec2.domain.Volume;

import java.util.Set;

/**
 * Refresh state of volumes or instances.
 */
public class AWSRefresher
{
   private final EC2Client client;

   /**
    * @param client
    */
   public AWSRefresher(EC2Client client)
   {
      this.client = client;
   }

   /**
    * 
    * @param instance
    * @return fresh state of instance
    */
   public RunningInstance refresh(RunningInstance instance)
   {
      return Iterables.getOnlyElement(Iterables.getOnlyElement(client.getInstanceServices().describeInstancesInRegion(
         instance.getRegion(), instance.getId())));
   }

   /**
    * 
    * @param instanceId
    * @return fresh state of instance
    */
   public RunningInstance getFreshInstance(String region, String instanceId)
   {
      return Iterables.getOnlyElement(Iterables.getOnlyElement(client.getInstanceServices().describeInstancesInRegion(
         region, instanceId)));
   }

   /**
    * 
    * @param volume
    * @return fresh state of volume.
    */
   public Volume refresh(Volume volume)
   {
      Set<Volume> volumesInRegion =
         client.getElasticBlockStoreServices().describeVolumesInRegion(volume.getRegion(), volume.getId());
      Volume currentVolumeState = volume;
      if (volumesInRegion.size() == 1)
      {
         currentVolumeState = Iterables.getOnlyElement(volumesInRegion);
      }
      else
      {
         // Eucalyptus return all volumes even if we send volume id
         // can be removed id   describeVolumesInRegion return 1 element
         for (Volume currentVolume : volumesInRegion)
         {
            if (volume.getId().equals(currentVolume.getId()))
            {
               currentVolumeState = currentVolume;
            }
         }
      }
      return currentVolumeState;
   }
}
