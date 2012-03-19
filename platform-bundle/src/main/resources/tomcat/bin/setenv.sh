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
#

# production script to set environment variables for eXo Platform

# custom JAVA options
[ -z "$EXO_JAVA_OPTS" ]  && EXO_JAVA_OPTS="-Xms1g -Xmx4g -XX:MaxPermSize=256m -XX:+UseCompressedOops"

# master tenant name
[ -z "$TENANT_MASTERHOST" ]  && TENANT_MASTERHOST="cloud-workspaces.com"

# master tenant repository
[ -z "$TENANT_REPOSITORY" ]  && TENANT_REPOSITORY="repository"

# cloud database
[ -z "$EXO_DB_HOST" ]  && EXO_DB_HOST="localhost:3306"
[ -z "$EXO_DB_USER" ]  && EXO_DB_USER="clouduser"
[ -z "$EXO_DB_PASSWORD" ]  && EXO_DB_PASSWORD="cloud12321"

# dir for jcr data for all tenants
[ -z "$EXO_TENANT_DATA_DIR" ]  && EXO_TENANT_DATA_DIR="$CATALINA_HOME/gatein/data/jcr"

# dir for jcr backup files 
[ -z "$EXO_BACKUP_DIR" ]  && EXO_BACKUP_DIR="$CATALINA_HOME/gatein/backup"

# this host external address
# HOST_EXTERNAL_ADDR=`ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}'`
[ -z "$HOST_EXTERNAL_ADDR" ]  && HOST_EXTERNAL_ADDR="localhost"

# eXo profiles
[ -z "$EXO_PROFILES" ]  && EXO_PROFILES="-Dexo.profiles=default,cloud"

LOG_OPTS="-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog"
SECURITY_OPTS="-Djava.security.auth.login.config=../conf/jaas.conf"

EXO_OPTS="-Dexo.product.developing=false -Dexo.conf.dir.name=gatein/conf"
IDE_OPTS="-Djavasrc=$JAVA_HOME/src.zip -Djre.lib=$JAVA_HOME/jre/lib"

EXO_CLOUD_OPTS="-javaagent:../lib/cloud-agent-instrument-1.1-M6-SNAPSHOT.jar=../gatein/conf/cloud/agent-configuration.xml \
    -Dgroovy.script.method.iteration.time=60000 \
    -Dtenant.masterhost=$TENANT_MASTERHOST \
    -Dtenant.repository.name=$TENANT_REPOSITORY \
    -Dtenant.data.dir=$EXO_TENANT_DATA_DIR \
    -Dtenant.db.host=$EXO_DB_HOST \
    -Dtenant.db.user=$EXO_DB_USER \
    -Dtenant.db.password=$EXO_DB_PASSWORD \
    -Dexo.backup.dir=$EXO_BACKUP_DIR"

EXO_CLOUD_SECURITY_OPTS="-Djava.security.manager=org.exoplatform.cloudmanagement.security.TenantSecurityManager \
    -Djava.security.policy==../conf/catalina.policy"

JMX_OPTS="-Dcom.sun.management.jmxremote=true -Djava.rmi.server.hostname=$HOST_EXTERNAL_ADDR \
    -Dcom.sun.management.jmxremote.password.file=$CATALINA_HOME/conf/jmxremote.password \
    -Dcom.sun.management.jmxremote.access.file=$CATALINA_HOME/conf/jmxremote.access \
    -Dcom.sun.management.jmxremote.authenticate=true \
    -Dcom.sun.management.jmxremote.ssl=false"

# Remote debug configuration
#REMOTE_DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
REMOTE_DEBUG=""

export JAVA_OPTS="$EXO_JAVA_OPTS $JAVA_OPTS $LOG_OPTS $SECURITY_OPTS $EXO_OPTS $IDE_OPTS $EXO_CLOUD_OPTS $EXO_CLOUD_SECURITY_OPTS $EXO_CLOUD_ADMIN_OPTS $JMX_OPTS $REMOTE_DEBUG $EXO_PROFILES"

export CLASSPATH="$CATALINA_HOME/lib/cloud-agent-security-1.1-M6-SNAPSHOT.jar:$CATALINA_HOME/conf/:$CATALINA_HOME/lib/jul-to-slf4j-1.5.8.jar:$CATALINA_HOME/lib/slf4j-api-1.5.8.jar:$CATALINA_HOME/lib/logback-classic-0.9.20.jar:$CATALINA_HOME/lib/logback-core-0.9.20.jar"



