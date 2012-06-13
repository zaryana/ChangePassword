#!/bin/bash

INSTANCE_ID=$1

cd ./$INSTANCE_ID/app-server-tomcat
./stop_eXo.sh -force

cd ..
eval `cat free_resources.sh`

cd ..
cd ..
rm -R ./$INSTANCE_ID
