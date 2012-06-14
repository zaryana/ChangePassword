#!/bin/bash

sudo /etc/init.d/haproxy $1 >/dev/null 2>&1 && {
  echo OK
  exit 0
} || {
  echo FAIL
  exit 1
}
