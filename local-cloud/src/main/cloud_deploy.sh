#!/bin/bash

if [ -z $1 ] ; then
  echo "ERROR! Access URL required. Example: deploy_cloud.sh cloud@cwks"
  exit 1
fi

access=$1

PACKAGE_NAME=$(find . -maxdepth 1 -name "cloud-workspaces-local-cloud*.zip")

if [ -z "$PACKAGE_NAME" ]; then
  echo "Can't find bundle to upload, exiting."
  exit 1
fi

url=($(echo $1 | tr "@" "\n"))
remote_cwks=${url[1]}

ssh -l cloud -i ~/.ssh/wks-cloud.key $access "rm -rf /home/cloud/cloud-workspaces/*"
scp -i ~/.ssh/wks-cloud.key $PACKAGE_NAME $access:/home/cloud/cloud-workspaces/
ssh -l cloud -i ~/.ssh/wks-cloud.key $access "unzip -q /home/cloud/cloud-workspaces/$PACKAGE_NAME -d /home/cloud/cloud-workspaces/"
ssh -l cloud -i ~/.ssh/wks-cloud.key $access "cd /home/cloud/cloud-workspaces/ && ./start.sh"

exit 0

