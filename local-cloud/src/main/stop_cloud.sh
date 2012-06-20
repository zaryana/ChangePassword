#!/bin/bash

if [ -e $1 ] ; then
  echo "ERROR! Access URL required. Example: stop_cloud.sh cloud@cwks"
  exit 1
fi

ssh -i ~/.ssh/wks-cloud.key $1 "cd /home/cloud/cloud-worskpaces/  && ./stop.sh"
