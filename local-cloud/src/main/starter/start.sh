#!/bin/bash

cd admin-tomcat/bin
./catalina.sh start

sleep 10s

curl -X POST -u cloudadmin:cloudadmin http://localhost:8080/rest/private/cloud-admin/application-service/start-server?type=local-cloud-agent
