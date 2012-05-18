#!/bin/bash

TOMCAT_ZIP="cloud-workspaces-platform-1.1.0-Alpha4-SNAPSHOT-tomcat.zip"

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
DB_HOST=$3
DB_USER=$4
DB_PASSWORD=$5
SERVER_PORT=$(($FIRST_SERVER_PORT + $6))
RMI_REGISTRY_PORT=$(($FIRST_RMI_REGISTRY_PORT + $6))
RMI_SERVER_PORT=$(($FIRST_RMI_SERVER_PORT + $6))
REDIRECT_PORT=$(($FIRST_REDIRECT_PORT + $6))
CONNECTOR_PORT=$(($FIRST_CONNECTOR_PORT + $6))
PORT=$(($FIRST_PORT + $6))

DEFAULT_DATABASE="repository_$INSTANCE_ID"


# create instance directory
mkdir $INSTANCE_ID

cd $INSTANCE_ID

# unzip tomcat
cp ../$TOMCAT_ZIP $TOMCAT_ZIP
unzip $TOMCAT_ZIP
rm $TOMCAT_ZIP

# configure setenv.sh
cd app-server-tomcat/bin
sed -i s/TENANT_MASTERHOST=\"cloud-workspaces.com\"/TENANT_MASTERHOST=\"$TENANT_MASTERHOST\"/ setenv.sh
sed -i s/EXO_DB_HOST=\"localhost:3306\"/EXO_DB_HOST=\"$DB_HOST:3306\"/ setenv.sh
sed -i s/EXO_DB_USER=\"clouduser\"/EXO_DB_USER=\"$DB_USER\"/ setenv.sh
sed -i s/EXO_DB_PASSWORD=\"cloud12321\"/EXO_DB_PASSWORD=\"$DB_PASSWORD\"/ setenv.sh
sed -i s/TENANT_REPOSITORY=\"repository\"/TENANT_REPOSITORY=\"$DEFAULT_DATABASE\"/ setenv.sh

# create default database
mysql -h$DB_HOST -u$DB_USER -p$DB_PASSWORD -e "create database \`$DEFAULT_DATABASE\`"

# configure server.xml
cd ../conf
sed -i s/$DEFAULT_SERVER_PORT/$SERVER_PORT/ server.xml
sed -i s/$DEFAULT_RMI_REGISTRY_PORT/$RMI_REGISTRY_PORT/ server.xml
sed -i s/$DEFAULT_RMI_SERVER_PORT/$RMI_SERVER_PORT/ server.xml
sed -i s/$DEFAULT_REDIRECT_PORT/$REDIRECT_PORT/ server.xml
sed -i s/$DEFAULT_CONNECTOR_PORT/$CONNECTOR_PORT/ server.xml
sed -i s/$DEFAULT_PORT/$PORT/ server.xml

# copy backup
cd ../gatein
cp ../../../backup.zip backup.zip
unzip backup.zip
rm backup.zip

cd ../..
echo mysql -h$DB_HOST -u$DB_USER -p$DB_PASSWORD -e \"drop database $DEFAULT_DATABASE\" > free_resources.sh

# start tomcat
cd app-server-tomcat
./run_eXo.sh
