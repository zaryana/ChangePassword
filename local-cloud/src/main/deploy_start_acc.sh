#!/bin/bash

./deploy_cloud.sh cloud@wks-acc.exoplatform.org

RETVAL=$?
if [ ! $RETVAL -eq 0 ] ; then
  echo "Deploy cloud script failed, exiting now.."
fi

