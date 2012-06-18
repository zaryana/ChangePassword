#!/bin/bash


# Unzipping platform to create template
AS_ZIP="cloud-workspaces-platform-bundle-tomcat.zip"
unzip -q ./local-cloud/$AS_ZIP -d ./local-cloud/
rm ./local-cloud/$AS_ZIP

# Starting PLF
cd ./local-cloud/app-server-tomcat
./prepare_instance.sh -use_profile_settings


RETVAL=$?
if [ ! $RETVAL -eq 0 ] ; then
  echo "Prepare-instance script failed, exiting now.."
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
curl --connect-timeout 900 -s  -X POST -u cloudadmin:cloudadmin http://localhost:8080/rest/private/cloud-admin/application-service/start-server?type=local-cloud-agent


#That's it!
exit 0