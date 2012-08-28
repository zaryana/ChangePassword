#!/bin/bash

./stop_acceptance.sh

./cloud_deploy.sh cloud@wks-acc.exoplatform.org exoplatform

RETVAL=$?
if [ ! $RETVAL -eq 0 ] ; then
  echo "Cloud deploy and start failed"
  exit 1
fi

