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

import static org.apache.commons.configuration.ConfigurationConverter.getProperties;
import static org.exoplatform.cloudmanagement.admin.util.ConfigurationConverter.subset;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Module;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationUtils;
import org.exoplatform.cloudmanagement.admin.configuration.ApplicationServerConfiguration;
import org.exoplatform.cloudmanagement.admin.instance.ApplicationServerType;
import org.exoplatform.cloudmanagement.admin.instance.CloudManagingException;
import org.exoplatform.cloudmanagement.admin.instance.CloudServerClient;
import org.exoplatform.cloudmanagement.admin.instance.UserDataGenerator;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.ec2.EC2Client;
import org.jclouds.ec2.domain.InstanceStateChange;
import org.jclouds.ec2.domain.RunningInstance;
import org.jclouds.ec2.domain.Volume;
import org.jclouds.ec2.options.RunInstancesOptions;
import org.jclouds.ec2.predicates.InstanceStateRunning;
import org.jclouds.ec2.predicates.InstanceStateTerminated;
import org.jclouds.ec2.services.ElasticBlockStoreClient;
import org.jclouds.ec2.services.InstanceClient;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.predicates.RetryablePredicate;
import org.jclouds.ssh.jsch.config.JschSshClientModule;
import org.picocontainer.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of cloud server client for eucalyptus and amazon services.
 */
public class AWSCloudServerClient implements CloudServerClient, ApplicationServerType, ApplicationServerConfiguration,
   Disposable
{

   private static final Logger LOG = LoggerFactory.getLogger(AWSCloudServerClient.class);

   private final ComputeServiceContext context;

   private final UserDataGenerator userDataGenerator;

   private final EC2Client ec2Client;

   private final AWSRefresher refresher;

   private final RetryablePredicate<Volume> volumeAvailable;

   private final RetryablePredicate<RunningInstance> instanceRunning;

   private final RetryablePredicate<RunningInstance> instanceHavePrivateDns;

   private final InstanceClient instanceClient;

   private final RetryablePredicate<RunningInstance> instanceTerminated;

   private final ElasticBlockStoreClient blockStoreClient;

   /**
    * 
    */
   public AWSCloudServerClient(Configuration adminConfiguration, UserDataGenerator userDataGenerator)
   {

      this.userDataGenerator = userDataGenerator;
      String clientName = adminConfiguration.getString(CLOUD_CLIENT_PROPERTY_NAME);
      this.context =
         new ComputeServiceContextFactory().createContext(clientName,
            ImmutableSet.<Module> of(new SLF4JLoggingModule(), new JschSshClientModule()),
            getProperties(subset(adminConfiguration, clientName)));
      this.ec2Client = EC2Client.class.cast(context.getProviderSpecificContext().getApi());
      this.refresher = new AWSRefresher(ec2Client);
      this.instanceClient = ec2Client.getInstanceServices();
      this.blockStoreClient = ec2Client.getElasticBlockStoreServices();
      this.volumeAvailable = new RetryablePredicate<Volume>(new VolumeAvailable(refresher), 600, 10, TimeUnit.SECONDS);
      this.instanceRunning =
         new RetryablePredicate<RunningInstance>(new InstanceStateRunning(ec2Client), 600, 5, TimeUnit.SECONDS);
      this.instanceTerminated =
         new RetryablePredicate<RunningInstance>(new InstanceStateTerminated(ec2Client), 600, 5, TimeUnit.SECONDS);
      this.instanceHavePrivateDns =
         new RetryablePredicate<RunningInstance>(new InstanceHasPrivateDnsName(refresher), 600, 5, TimeUnit.SECONDS);

   }

   @Override
   public Configuration createServer(Configuration configuration) throws CloudManagingException
   {

      try
      {
         Volume volume =
            blockStoreClient.createVolumeInAvailabilityZone(configuration.getString(PROPERTY_AVAILABILITY_ZONE),
               configuration.getInt(PROPERTY_VOLUME_SIZE));

         if (!volumeAvailable.apply(volume))
         {
            throw new CloudManagingException("Fail to create volume " + volume.getId());
         }

         Configuration applicationServerConfiguration = new BaseConfiguration();
         applicationServerConfiguration.setProperty(VOLUME_ID, volume.getId());

         //add default from type configuration to configuration of the new application server.
         ConfigurationUtils.append(configuration.subset(PROPERTY_REGISTER_CONFIG_PARAM_PREFIX),
            applicationServerConfiguration);
         String userData = userDataGenerator.generateUserData(configuration, applicationServerConfiguration);

         LOG.info("Start new instance with volumeId = {}", volume.getId());

         if (!configuration.containsKey(PROPERTY_IMAGE_ID) || !configuration.containsKey(PROPERTY_INSTANCE_TYPE))
         {
            throw new CloudManagingException("Image id or instance type not set in " + configuration);
         }

         RunInstancesOptions options = new RunInstancesOptions();
         options = options.asType(configuration.getString(PROPERTY_INSTANCE_TYPE));
         options = options.withUserData(userData.getBytes(Charset.forName("UTF-8")));
         if (configuration.containsKey(PROPERTY_KERNEL_ID))
         {
            options = options.withKernelId(configuration.getString(PROPERTY_KERNEL_ID));
         }
         if (configuration.containsKey(PROPERTY_RAMDISK_ID))
         {
            options = options.withRamdisk(configuration.getString(PROPERTY_RAMDISK_ID));
         }
         options = options.withKeyName(configuration.getString(PROPERTY_KEY_NAME));
         options = options.withSecurityGroups(configuration.getStringArray(PROPERTY_SECURITY_GROUP_NAME));

         RunningInstance instance =
            Iterables.getOnlyElement(instanceClient.runInstancesInRegion(null,
               configuration.getString(PROPERTY_AVAILABILITY_ZONE), configuration.getString(PROPERTY_IMAGE_ID), 1, 1,
               options));

         String alias = instance.getId();
         if (alias == null || alias.isEmpty())
         {
            String message = "Start instance command return wrong result. Not instance id found";
            LOG.error(message);
            throw new CloudManagingException(message);
         }

         if (!instanceRunning.apply(instance) || !instanceHavePrivateDns.apply(instance))
         {
            LOG.error("Fail to start instance with id {}."
               + " Cloud didn't show what instance is running or instance have private DNS in 10 min."
               + "Command for suspending will be send to cloud", alias);
            terminateInternal(alias, null, volume.getId());
            throw new CloudManagingException("Fail to start instance with id " + alias + " or get private dns ");
         }
         LOG.info("Instance {} created ", instance.getId());
         instance = refresher.refresh(instance);

         applicationServerConfiguration.setProperty(ALIAS_PARAMETER, alias);
         applicationServerConfiguration.setProperty(ALIAS_PARAMETER, alias);
         applicationServerConfiguration.setProperty(HOST_PARAMETER, instance.getPrivateDnsName());
         applicationServerConfiguration.setProperty("public.host", instance.getDnsName());
         applicationServerConfiguration.setProperty(INSTANCE_ID, alias);
         applicationServerConfiguration.setProperty(SERVER_TYPE, configuration.getString(SERVER_TYPE));
         applicationServerConfiguration.setProperty(ON_MAINTENANCE, false);
         applicationServerConfiguration.setProperty(PORT_PARAMETER, "8080");

         return applicationServerConfiguration;
      }
      catch (NoSuchElementException e)
      {
         //TODO remove this check
         LOG.error(e.getLocalizedMessage(), e);
         throw e;
      }
   }

   @Override
   public void removeServer(Configuration instanceConfiguration) throws CloudManagingException
   {
      try
      {
         String instanceId = instanceConfiguration.getString(INSTANCE_ID);
         String region = null;
         String volumeId =
            instanceConfiguration.containsKey(VOLUME_ID) ? instanceConfiguration.getString(VOLUME_ID) : null;
         terminateInternal(instanceId, region, volumeId);
      }
      catch (NoSuchElementException e)
      {
         //TODO remove this check
         LOG.error(e.getLocalizedMessage(), e);
         throw e;
      }
   }

   /**
    * Execute instance termination command.
    * 
    * @param instanceId
    * @param region
    * @param volumeId
    * @throws CloudManagingException
    */
   private void terminateInternal(String instanceId, String region, String volumeId) throws CloudManagingException
   {

      LOG.info("Termination instance {} in region {}", instanceId, region);
      if (instanceId != null && !instanceId.isEmpty())
      {
         RunningInstance freshInstance = refresher.getFreshInstance(region, instanceId);

         InstanceStateChange state =
            Iterables.getOnlyElement(instanceClient.terminateInstancesInRegion(region, instanceId));

         if (!instanceTerminated.apply(freshInstance))
         {
            throw new CloudManagingException("Fail to terminate instance " + state.getInstanceId());
         }
      }
      if (volumeId != null && !volumeId.isEmpty())
      {
         LOG.info("Removing volume {}", volumeId);
         blockStoreClient.deleteVolumeInRegion(region, volumeId);
      }
   }

   /**
    * @see org.picocontainer.Disposable#dispose()
    */
   @Override
   public void dispose()
   {
      context.close();

   }
}
