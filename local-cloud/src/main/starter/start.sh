#!/bin/bash

# Unzipping platform to create template
AS_ZIP="cloud-workspaces-platform-bundle-tomcat.zip"

if [ -d ./local-cloud/app-server-tomcat ]
then
    echo "Error: Application server folder found. Seems it's not a first run, try re-deploy again."
    exit 1
fi


unzip -q ./local-cloud/$AS_ZIP -d ./local-cloud/
rm ./local-cloud/$AS_ZIP

# Starting PLF
cd ./local-cloud/app-server-tomcat
./prepare_instance.sh -use_profile_settings


RETVAL=$?
if [ ! $RETVAL -eq 0 ] ; then
  echo "Prepare-instance script failed, exiting now."
  exit 1
fi

read -r ID < ./template_id.txt


# Back to home
cd ../..

echo "ID:" $ID

# Set backup ID in admin conf
cd admin-tomcat/exo-admin-conf
sed -i s/cloud.admin.tenant.backup.id=NO_ID/cloud.admin.tenant.backup.id=$ID/ admin.properties

# Starting admin
cd ../..
cd admin-tomcat/bin
./catalina.sh start

sleep 30s

# Starting first AS

#Block autoscaling
res=$(curl --connect-timeout 900 -s  -X POST -u cloudadmin:cloudadmin http://localhost:8080/rest/private/cloud-admin/autoscaling-service/block-autoscaling)


as_name=$(curl --connect-timeout 900 -s  -X POST -u cloudadmin:cloudadmin http://localhost:8080/rest/private/cloud-admin/instance-service/start-server?type=local-cloud-agent)
echo "Started app server $as_name"
cd ../..
echo $as_name > ./as.txt

#Allow autoscaling
res=$(curl --connect-timeout 900 -s  -X POST -u cloudadmin:cloudadmin http://localhost:8080/rest/private/cloud-admin/autoscaling-service/allow-autoscaling)

#That's it!
exit 0