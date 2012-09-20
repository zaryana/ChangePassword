#!/bin/bash

TOMCAT_DIR="app-server-tomcat"

DEFAULT_SERVER_PORT=8005
DEFAULT_RMI_REGISTRY_PORT=6969
DEFAULT_RMI_SERVER_PORT=7979
DEFAULT_REDIRECT_PORT=8443
DEFAULT_CONNECTOR_PORT=8009
DEFAULT_PORT=8080

FIRST_SERVER_PORT=8100
FIRST_RMI_REGISTRY_PORT=6979
FIRST_RMI_SERVER_PORT=7989
FIRST_REDIRECT_PORT=8453
FIRST_CONNECTOR_PORT=8200
FIRST_PORT=8090

INSTANCE_ID=$1
TENANT_MASTERHOST=$2
AGENT_USERNAME=$3
AGENT_PASSWORD=$4
DB_HOST=$5
DB_USER=$6
DB_PASSWORD=$7

AS_NUMBER=$8
SERVER_PORT=$(($FIRST_SERVER_PORT + $AS_NUMBER))
RMI_REGISTRY_PORT=$(($FIRST_RMI_REGISTRY_PORT + $AS_NUMBER))
RMI_SERVER_PORT=$(($FIRST_RMI_SERVER_PORT + $AS_NUMBER))
REDIRECT_PORT=$(($FIRST_REDIRECT_PORT + $AS_NUMBER))
CONNECTOR_PORT=$(($FIRST_CONNECTOR_PORT + $AS_NUMBER))
PORT=$(($FIRST_PORT + $AS_NUMBER))

DEFAULT_DATABASE="repository_$INSTANCE_ID"

# create instance directory
mkdir $INSTANCE_ID

cd $INSTANCE_ID

# unzip tomcat
cp -R ../$TOMCAT_DIR $TOMCAT_DIR

# configure setenv.sh
cd app-server-tomcat/bin
sed -i s/TENANT_MASTERHOST=\"cloud-workspaces.com\"/TENANT_MASTERHOST=\"$TENANT_MASTERHOST\"/ setenv.sh
sed -i s/EXO_DB_HOST=\"localhost:3306\"/EXO_DB_HOST=\"$DB_HOST:3306\"/ setenv.sh
sed -i s/EXO_DB_USER=\"cloud\"/EXO_DB_USER=\"$DB_USER\"/ setenv.sh
sed -i s/EXO_DB_PASSWORD=\"cloud\"/EXO_DB_PASSWORD=\"$DB_PASSWORD\"/ setenv.sh
sed -i s/TENANT_REPOSITORY=\"repository\"/TENANT_REPOSITORY=\"$DEFAULT_DATABASE\"/ setenv.sh

# create default database
mysql -h$DB_HOST -u$DB_USER -p$DB_PASSWORD -e "create database \`$DEFAULT_DATABASE\`"

cd ../conf

# configure tomcat-users.xml
sed -i "s/<user username=\"cloudadmin\" password=\"cloudadmin\"/<user username=\"$AGENT_USERNAME\" password=\"$AGENT_PASSWORD\"/" tomcat-users.xml

# configure server.xml
sed -i s/$DEFAULT_SERVER_PORT/$SERVER_PORT/ server.xml
sed -i s/$DEFAULT_RMI_REGISTRY_PORT/$RMI_REGISTRY_PORT/ server.xml
sed -i s/$DEFAULT_RMI_SERVER_PORT/$RMI_SERVER_PORT/ server.xml
sed -i s/$DEFAULT_REDIRECT_PORT/$REDIRECT_PORT/ server.xml
sed -i s/$DEFAULT_CONNECTOR_PORT/$CONNECTOR_PORT/ server.xml
sed -i s/$DEFAULT_PORT/$PORT/ server.xml

cd ../..
echo mysql -h$DB_HOST -u$DB_USER -p$DB_PASSWORD -e \"drop database $DEFAULT_DATABASE\" > free_resources.sh

# start tomcat
cd app-server-tomcat
./run_eXo.sh
