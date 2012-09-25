#!/bin/bash

# Unzipping platform to create template
AS_ZIP="cloud-workspaces-platform-bundle-tomcat.zip"

for f in `ls -a ./local-cloud/ | grep as`
do
  if [ -d ./local-cloud/$f ]
  then
    echo "Error: Application server folder found. Seems it's not a first run, try re-deploy again."
    exit 1
  fi
done

if [ ! -d ./local-cloud/app-server-tomcat ]
then
    # unzip and prpeare cloud
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
    #cd admin-tomcat/exo-admin-conf
    #sed -i s/cloud.admin.tenant.backup.id=NO_ID/cloud.admin.tenant.backup.id=$ID/ admin.properties

    #cd ../..

    # Installing admin
    java -jar cloud-workspaces-admin-installer.jar install --version 1.1.0 --tomcat admin-tomcat --answers ~/local-answers.txt --bundle cloud-workspaces-admin-bundle-tomcat.zip -Dcloud.admin.tenant.backup.id=$ID
    exit 0
fi # otherwise use prepared cloud

# Starting admin
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
