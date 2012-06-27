#!/bin/bash

./cloud_stop.sh cloud@wks-acc.exoplatform.org

RETVAL=$?
if [ ! $RETVAL -eq 0 ] ; then
  echo "Cloud stop failed."
fi 