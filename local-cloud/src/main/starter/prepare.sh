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

if [ -d ./local-cloud/app-server-tomcat ]
then
    echo "Application server folder found. Seems it's not a first run."
fi

# unzip and prpeare cloud
unzip -q ./local-cloud/$AS_ZIP -d ./local-cloud/
rm ./local-cloud/$AS_ZIP

# Starting PLF
cd ./local-cloud/app-server-tomcat
./prepare_instance.sh -use_profile_settings > /dev/null

RETVAL=$?
if [ ! $RETVAL -eq 0 ] ; then
  echo "Prepare-instance script failed, exiting now."
  exit 1
fi

read -r ID < ./template_id.txt

echo $ID
exit 0;
