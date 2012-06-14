#!/bin/bash


# Unzipping platform to create template
AS_ZIP="cloud-workspaces-platform-bundle-tomcat.zip"
unzip -q ./local-cloud/$AS_ZIP -d ./local-cloud/
rm ./local-cloud/$AS_ZIP

# create default database - need host name
mysql -hlocalhost -u$EXO_DB_USER -p$EXO_DB_PASSWORD -e "create database \`repository\`"

# Starting PLF
cd ./local-cloud/app-server-tomcat
./start_eXo.sh

# Waiting for full start
sleep 60s
curl --connect-timeout 1800 -s  http://localhost:8080/portal/intranet/home

# Asking to create template
echo "Creating Tenant Template (JCR backup)"
ID=`curl --connect-timeout 900 -s -S -X POST -u $CLOUD_AGENT_USERNAME:$CLOUD_AGENT_PASSWORD http://localhost:8080/cloud-agent/rest/cloud-agent/template-service/template`
echo "Issued Tenant Template: $ID"
sleep 15s

# Check template OK (by length)
IDlen=$(echo ${#ID})
if [ $IDlen -ne 32 ] ; then
  echo "ERROR: Invalid Template ID received, exiting."
  exit 1
fi

hasID=""
i=0
timeout=240
# wait no more 20min for a backup
while [ -z $hasID ] && [ $i -lt $timeout ] ; do
  IDS=`curl --connect-timeout 900 -s -S -X GET -u $CLOUD_AGENT_USERNAME:$CLOUD_AGENT_PASSWORD http://localhost:8080/cloud-agent/rest/cloud-agent/template-service/template`
  hasID=`expr match "$IDS" ".*\"\($ID\)\".*"`
  i=$((i + 1))
sleep 5s
done

if [ $i -eq $timeout ] ; then
 echo "WARNING! Template $ID creation was not finished in time. See app server logs for details."
fi

# Ok, now we can stop it
./stop_eXo.sh -force

# Cleanup the app server
rm -rf ./logs/*
rm -rf ./work/*
rm -rf ./temp/*
rm -rf ./gatein/data/jcr/repository

# Back to home
cd ../..

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