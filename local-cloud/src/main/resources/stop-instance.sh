#!/bin/bash

INSTANCE_ID=$1

cd ./local-cloud/$INSTANCE_ID/app-server-tomcat
./stop_eXo.sh -force

cd ..
eval `cat free_resources.sh`

cd ..
cd ..
rm -R ./local-cloud/$INSTANCE_ID
