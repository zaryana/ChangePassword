# Environment Variable Prerequisites
#
# JAVA_OPTS override jvm options for example: Xmx, Xms etc.

# Set global cloud names

# master tenant name
[ -z "$TENANT_MASTERHOST" ]  && TENANT_MASTERHOST="cloud-workspaces.com"

# dir for admin data
[ -z "$EXO_ADMIN_DATA_DIR" ]  && EXO_ADMIN_DATA_DIR="$CATALINA_HOME/data"

# admin logs
[ -z "$EXO_ADMIN_LOGS_DIR" ]  && EXO_ADMIN_LOGS_DIR="$CATALINA_HOME/logs/cloud-admin"

# admin config
[ -z "$EXO_ADMIN_CONF_DIR" ]  && EXO_ADMIN_CONF_DIR="$CATALINA_HOME/exo-admin-conf/"

# this host external address
# HOST_EXTERNAL_ADDR=`ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}'`
[ -z "$HOST_EXTERNAL_ADDR" ]  && HOST_EXTERNAL_ADDR="localhost"

# admin variables
EXO_CLOUD_ADMIN_OPTS="-Dcloud.admin.log.dir=$EXO_ADMIN_LOGS_DIR \
                      -Dcloud.admin.data.dir=$EXO_ADMIN_DATA_DIR \
                      -Dtenant.masterhost=$TENANT_MASTERHOST \
                      -Dcloud.admin.configuration.dir=$EXO_ADMIN_CONF_DIR \
                      -Dcloud.admin.whitelist=$EXO_ADMIN_CONF_DIR/whitelist.properties \
                      -Dcloud.admin.configuration.file=$EXO_ADMIN_CONF_DIR/admin.properties "

JMX_OPTS="-Dcom.sun.management.jmxremote=true -Djava.rmi.server.hostname=$HOST_EXTERNAL_ADDR \
	-Dcom.sun.management.jmxremote.authenticate=true \
	-Dcom.sun.management.jmxremote.password.file=$CATALINA_HOME/conf/jmxremote.password \
	-Dcom.sun.management.jmxremote.access.file=$CATALINA_HOME/conf/jmxremote.access \
	-Dcom.sun.management.jmxremote.ssl=false"

#uncomment if you want to debug app server
#REMOTE_DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y"
REMOTE_DEBUG=""

export JAVA_OPTS="-Xms1g -Xmx4g $JAVA_OPTS $EXO_CLOUD_ADMIN_OPTS $REMOTE_DEBUG $JMX_OPTS"

# Catalina pid file
[ -z "$CATALINA_PID" ]  && CATALINA_PID="$CATALINA_HOME/temp/catalina.tmp"
export CATALINA_PID
