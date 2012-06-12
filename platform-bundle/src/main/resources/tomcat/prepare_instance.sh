#!/bin/sh
#
# Copyright (C) 2011 eXo Platform SAS.
# 
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
# 
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

# Prepare Cloud Workspaces Application Server instance for use in production.
# This script 
# * starts local MySQL server and creates database 'repository' with user clouduser/cloud12321
# * starts the Platform with sessions of EXO_DB_HOST EXO_DB_USER and EXO_DB_PASSWORD pointing to local MySQL
# * waits for Platform start and 
# * call agent's template service to create a tenant template backup (JCR backup)
# * wait for backup done and 
# * stops the Platform server
# * stops the MySQL server

  # Check if App server Platform isn't already running
  asPid="`pwd`/temp/catalina.tmp"
  if [ -e $asPid ] && [ -n "`ps -A | grep \`cat $asPid\``" ] ; then
     # already started - exit with error
     echo "ERROR: App server already running. Stop it and run this script again."
     exit 1
  fi


  # Starting local mysql
  EXO_DB_HOST="localhost:3306"
  EXO_DB_USER="root"
  EXO_DB_PASSWORD="root"
  # yum install mysql
  sudo service mysqld start
  # /usr/bin/mysqladmin -u password '$EXO_DB_PASSWORD'

  SQL='drop database repository; create database repository default charset latin1 collate latin1_general_cs;'
  mysql --user=$EXO_DB_USER --password=$EXO_DB_PASSWORD --host=localhost -B -N -e "$SQL" -w >> mysql.log

  # Starting PLF
  echo "Starting App server Platform"
  export EXO_DB_HOST EXO_DB_USER EXO_DB_PASSWORD
  ./start_eXo.sh

  # Waiting for full start
  sleep 90s
  curl --connect-timeout 900  http://localhost:8080/portal/intranet/home

  # Asking to create template
  echo "Creating Tenant Template (JCR backup)"
  ID=`curl -s -S -X POST -u $CLOUD_AGENT_USERNAME:$CLOUD_AGENT_PASSWORD http://localhost:8080/cloud-agent/rest/cloud-agent/template-service/template`
  echo "Issued Tenant Template: $ID"
  sleep 15s

  # Check template OK (by length)
  IDlen=$(echo ${#ID})
  if [ $IDlen -ne 32 ] ; then
    echo "ERROR: Invalid Template ID received, exiting."
    exit 1
  fi

  hasID=""
  i=0
  timeout=240
  # wait no more 20min for a backup
  while [ -z $hasID ] && [ $i -lt $timeout ] ; do
    IDS=`curl -s -S -X GET -u $CLOUD_AGENT_USERNAME:$CLOUD_AGENT_PASSWORD http://localhost:8080/cloud-agent/rest/cloud-agent/template-service/template`
    hasID=`expr match "$IDS" ".*\"\($ID\)\".*"`
    i=$((i + 1))
    sleep 5s
  done

  if [ $i -eq $timeout ] ; then
    echo "WARNING! Template $ID creation was not finished in time. See app server logs for details."
  fi

  # Ok, now we can stop it
  ./stop_eXo.sh -force

  # Cleanup the app server
  rm -rf ./logs/*
  rm -rf ./work/*
  rm -rf ./temp/*
  rm -rf ./gatein/gadgets/*
  rm -rf ./gatein/data/jta/*
  rm -rf ./gatein/data/jcr/repository

  # stop mysql
  sudo service mysqld stop
  # yum erase mysql

  echo ""
  echo "***** App server prepared *****"
  echo "Tenant Template created: $ID"
  echo ""
  echo "Configure Admin server respectively in admin.properties file:"
  echo "cloud.admin.tenant.backup.id=$ID"
