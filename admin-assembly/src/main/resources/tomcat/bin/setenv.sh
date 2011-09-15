# Environment Variable Prerequisites
#
# JAVA_OPTS override jvm options for example: Xmx, Xms etc.


# Set global cloud names
[ -z "${TENANT_MASTERHOST}" ]  && TENANT_MASTERHOST="localhost"

# 
[ -z "${EXO_ADMIN_DATA_DIR}" ]  && EXO_ADMIN_DATA_DIR="${CATALINA_HOME}/data"

[ -z "${EXO_ADMIN_LOGS_DIR}" ]  && EXO_ADMIN_LOGS_DIR="${CATALINA_HOME}/logs/cloud-admin"

[ -z "${EXO_ADMIN_CONF_DIR}" ]  && EXO_ADMIN_CONF_DIR="${CATALINA_HOME}/exo-admin-conf/"




# Sets some variables
                
EXO_CLOUD_ADMIN_OPTS="-Dcloud.admin.log.dir=${EXO_ADMIN_LOGS_DIR} \
                      -Dcloud.admin.data.dir=${EXO_ADMIN_DATA_DIR} \
                      -Dtenant.masterhost=${TENANT_MASTERHOST} \
                      -Dcloud.admin.configuration.dir=${EXO_ADMIN_CONF_DIR} \
                      -Dcloud.admin.configuration.file=${EXO_ADMIN_CONF_DIR}/admin.properties "

JMX_OPTS="-Dcom.sun.management.jmxremote.authenticate=true \
          -Dcom.sun.management.jmxremote.password.file=${CATALINA_HOME}/conf/jmxremote.password \
          -Dcom.sun.management.jmxremote.access.file=${CATALINA_HOME}/conf/jmxremote.access \
          -Dcom.sun.management.jmxremote.ssl=false"
                              

#uncomment if you want to debug app server
#REMOTE_DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y"
REMOTE_DEBUG=""

export JAVA_OPTS="$JAVA_OPTS $EXO_CLOUD_ADMIN_OPTS $REMOTE_DEBUG $JMX_OPTS"

echo "======="
echo $JAVA_OPTS
echo "======="

