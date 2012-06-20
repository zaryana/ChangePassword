#!/bin/bash

./stop_cloud.sh cloud@wks-acc.exoplatform.org

RETVAL=$?
if [ ! $RETVAL -eq 0 ] ; then
  echo "Stop cloud script failed, exiting now.."
fi 