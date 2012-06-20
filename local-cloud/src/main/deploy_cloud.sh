#!/bin/bash

if [ -e $1 ] ; then
  echo "ERROR! Access URL required. Example: deploy_cloud.sh cloud@cwks"
  exit 1
fi

  PACKAGE_NAME=$(find . -maxdepth 1 -name "cloud-workspaces-local-cloud*.zip" -printf %f\\n)

   if [ -z "$PACKAGE_NAME" ]; then
     echo "Can't find bundle to upload, exiting."
     exit 1
  fi

  url=$(echo $1 | tr "@" "\n")
  remote_host=${url[1]}

  ssh -l cloud -i ~/.ssh/wks-cloud.key $1 "rm -rf /home/cloud/cloud-workspaces/*"
  scp -i ~/.ssh/wks-cloud.key $PACKAGE_NAME $1:/home/cloud/cloud-workspaces/
  ssh -l cloud -i ~/.ssh/wks-cloud.key $1 "unzip -q /home/cloud/cloud-workspaces/$PACKAGE_NAME -d /home/cloud/cloud-workspaces/"
  ssh -l cloud -i ~/.ssh/wks-cloud.key $1 "cd /home/cloud/cloud-workspaces/ && ./start.sh"

  sleep 40s

  get() {
      echo $(curl --write-out %{http_code} --connect-timeout 60  --silent --output /dev/null $1)
  }

  code=0
  # will wait for 30min
  timeout=$((30 * 60))
  tstep=5
  t=0
  while [ $code -ne 302 ] && [ $code -ne 200 ] && [ ! $code -ge 500 ] && [ $t -lt $timeout ] ; do
     code=$(get "http://$remote_host/portal/intranet/home")
     sleep $tstep
     t=$((t + tstep))
     echo "waiting $t seconds from $timeout, $code"
  done

  if [ $t -ge $timeout ] ; then
      echo "WARNING! Server not responding in time. See app server logs for details."
      exit 1
  fi

  echo "Local Cloud started succesfully at $remote_host"
  exit 0

