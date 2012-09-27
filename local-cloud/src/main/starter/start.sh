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
    java -jar cloud-workspaces-admin-installer.jar install:local-cloud --version 1.1.1 --tomcat admin-tomcat --bundle cloud-workspaces-admin-bundle-tomcat.zip --answers ~/local-answers.txt
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
