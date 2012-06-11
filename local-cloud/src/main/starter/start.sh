#!/bin/bash

# Unzipping platform to create template
AS_ZIP="cloud-workspaces-platform-bundle-tomcat.zip"
unzip ./local-cloud/$AS_ZIP -d ./local-cloud/

# create default database - need host name
mysql -hlocalhost -u$EXO_DB_USER -p$EXO_DB_PASSWORD -e "create database \`repository\`"

# Starting PLF
cd ./local-cloud/app-server-tomcat
./start_eXo.sh

# Waiting for full start
sleep 60s
curl --connect-timeout 900  http://localhost:8080/portal/intranet/home

# Asking to create template
ID="`curl -X POST -u cloudadmin:cloudadmin http://localhost:8080/cloud-agent/rest/cloud-agent/template-service/template`"

# Check template OK (by length)
LEN=$(echo ${#ID})
if [$LEN -ne 32 ]; then
  echo "Invalid backup ID received, exiting"
  exit 1
fi

# Ok, now we can stop it
./stop_eXo.sh -force

# Zip a template
cd ./gatein && zip -r -8   ../../backup.zip  ./backup && cd ..

# Back to home
cd ../..


# Delete temporary bundle
rm -rf ./local-cloud/app-server-tomcat


# Set backup ID in admin conf
cd admin-tomcat/exo-admin-conf
sed -i s/cloud.admin.tenant.backup.id=NO_ID/cloud.admin.tenant.backup.id=$ID/ admin.properties

# Starting admin
cd ../..
cd admin-tomcat/bin
./catalina.sh start

sleep 10s

# Starting first AS
curl -X POST -u cloudadmin:cloudadmin http://localhost:8080/rest/private/cloud-admin/application-service/start-server?type=local-cloud-agent


#That's it!
exit 0