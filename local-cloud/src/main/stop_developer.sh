#!/bin/bash

./cloud_stop.sh cloud@cwks

RETVAL=$?
if [ ! $RETVAL -eq 0 ] ; then
  echo "Cloud stop failed."
fi 