#!/bin/bash

./cloud_deploy.sh cloud@cwks

RETVAL=$?
if [ ! $RETVAL -eq 0 ] ; then
  echo "Cloud deploy and start failed"
fi

