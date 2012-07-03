#!/bin/bash

./stop_acceptance.sh

./cloud_deploy.sh cloud@wks-acc.exoplatform.org

RETVAL=$?
if [ ! $RETVAL -eq 0 ] ; then
  echo "Cloud deploy and start failed"
fi

