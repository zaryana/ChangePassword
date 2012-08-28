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

sleep 40s

lpath=`pwd`
curl_res="$lpath/curl.res"
is_ready() {
  local status=""
  local state=""
  # will wait for 30min
  timeout=$((30 * 60))
  local tstep=5
  local t=0
  as=$(ssh -l cloud -i ~/.ssh/wks-cloud.key $access "cat /home/cloud/cloud-workspaces/as.txt") 
  while [ "$state" != "ONLINE" ] && [ $t -lt $timeout ] ; do
    if [ "$1" == "app" ] ; then
      status=$(curl --connect-timeout 900 -s  -u cloudadmin:cloudadmin --output $curl_res --write-out %{http_code}  "http://$remote_cwks/rest/private/cloud-admin/instance-service/server-state/$as")
    else
      status=$(curl --connect-timeout 900 -s  -u cloudadmin:cloudadmin --output $curl_res --write-out %{http_code}  "http://$remote_cwks/rest/private/cloud-admin/cloudworkspaces/tenant-service/status/$1")
    fi

    if [ $status -ne '200' ] ; then
      echo "Unexpected status from REST call: $status, exiting.";
      exit 1
    fi

    state=$(cat "$curl_res")
    sleep $tstep
    t=$((t + tstep))
    echo -ne "\rwaiting $t seconds from $timeout, app/tenant state: $state           "
  done

  if [ $t -ge $timeout ] ; then
    echo "WARNING! Server not responding in time. See app server logs for details."
    exit 1
  fi
}

is_ready "app"

#Create sandbox
echo -ne "\n Creating tenant demo \n"
res=$(curl --connect-timeout 900 -s  -X POST -u cloudadmin:cloudadmin "http://$remote_cwks/rest/private/cloud-admin/tenant-service/create/demo")

is_ready "demo"

sleep 20s

#Create default
if [ ! -z $2 ] ; then
  echo ""
  echo -ne "Creating tenant $2 \n"
  res=$(curl --connect-timeout 900 -s  -X POST -u cloudadmin:cloudadmin "http://$remote_cwks/rest/private/cloud-admin/tenant-service/create/$2")
  is_ready "$2"
fi

echo ""
echo "Local Cloud started succesfully at $remote_cwks                " # need spaces to rewrite waiting text
exit 0

