#!/bin/bash

ADMIN_ZIP="cloud-workspaces-admin-1.1.0-Alpha4-SNAPSHOT.zip"
AGENT_ZIP="cloud-workspaces-platform-1.1.0-Alpha4-SNAPSHOT-tomcat.zip"

unzip $ADMIN_ZIP
cp $AGENT_ZIP ./local-cloud


cd admin-tomcat/bin
./catalina.sh start

sleep 10s

curl -X POST -u cloudadmin:cloudadmin http://localhost:8080/rest/private/cloud-admin/application-service/start-server?type=local-cloud-agent
