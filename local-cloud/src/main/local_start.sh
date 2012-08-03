#!/bin/bash


PACKAGE_NAME=$(find . -maxdepth 1 -name "cloud-workspaces-local-cloud*.zip")

if [ -z "$PACKAGE_NAME" ]; then
  echo "Can't find bundle to upload, exiting."
  exit 1
fi


#rm -rf /home/cloud/cloud-workspaces/*

find . -not -name '*zip' -and -not -name "local*"  -type f -o -type d | xargs rm -rf
unzip -q ./$PACKAGE_NAME -d .
./start.sh

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
  as=$(cat ./as.txt)
  while [ "$state" != "ONLINE" ] && [ $t -lt $timeout ] ; do
  
   if [ "$1" == "app" ] ; then
      status=$(curl --connect-timeout 900 -s  -u cloudadmin:cloudadmin --output $curl_res --write-out %{http_code}  "http://localhost:8080/rest/private/cloud-admin/instance-service/server-state/$as")
   else
      status=$(curl --connect-timeout 900 -s  -u cloudadmin:cloudadmin --output $curl_res --write-out %{http_code}  "http://localhost:8080/rest/private/cloud-admin/cloudworkspaces/tenant-service/status/$1")
   fi
    
  if [ $status -ne '200' ] ; then
    echo "Unexpected status from REST call: $status, exiting.";
    exit 1
  fi

    state=$(cat "$curl_res")
    sleep $tstep
    t=$((t + tstep))
    echo -ne "\rwaiting $t seconds from $timeout, app/tenant status: $state         "
  done
  
  if [ $t -ge $timeout ] ; then
    echo "WARNING! Server not responding in time. See app server logs for details."
    exit 1
  fi
}


is_ready "app"


  if [ -z "$1" ] || [ "$1" -ne "-no-demo" ] ; then
    #Create sandbox
    echo -ne "\n Creating tenant demo \n"
    res=$(curl --connect-timeout 900 -s  -X POST -u cloudadmin:cloudadmin "http://localhost:8080/rest/private/cloud-admin/tenant-service/create/demo")
    is_ready "demo"
  fi

  sleep 20s
  

echo ""
echo "Local Cloud started succesfully at $remote_cwks                " # need spaces to rewrite waiting text
exit 0

