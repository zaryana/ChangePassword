# Environment Variable Prerequisites
#
# JAVA_OPTS override jvm options for example: Xmx, Xms etc.

# custom JAVA options
[ -z "$EXO_JAVA_OPTS" ]  && EXO_JAVA_OPTS="-Xmx96m"
[ -z "$JMXACC" ] && JMXACC="$CATALINA_HOME/conf/jmxremote.access"
[ -z "$JMXPAS" ] && JMXPAS="$CATALINA_HOME/conf/jmxremote.password"

# this host external address
# HOST_EXTERNAL_ADDR=`ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}'`
[ -z "$HOST_EXTERNAL_ADDR" ]  && HOST_EXTERNAL_ADDR="localhost"

# Set global cloud names
# master tenant name
TENANT_MASTERHOST="cloud-workspaces.com"

# dir for admin data
[ -z "$EXO_ADMIN_DATA_DIR" ]  && EXO_ADMIN_DATA_DIR="$CATALINA_HOME/data"

# admin logs
[ -z "$EXO_ADMIN_LOGS_DIR" ]  && EXO_ADMIN_LOGS_DIR="$CATALINA_HOME/logs/cloud-admin"

# admin config
[ -z "$EXO_ADMIN_CONF_DIR" ]  && EXO_ADMIN_CONF_DIR="$CATALINA_HOME/exo-admin-conf"

# logback smtp appender configuration file
[ -z "${EXO_LOGBACK_SMTP_CONF}" ] && EXO_LOGBACK_SMTP_CONF="${CATALINA_HOME}/conf/logback-smtp-appender.xml"

# needs for showing logs of application servers through apache
APPLICATION_SERVER_LOGS_PORT="8085"

# admin variables
EXO_CLOUD_ADMIN_OPTS="-Dapplication.server.logs.port=$APPLICATION_SERVER_LOGS_PORT \
                      -Dcloud.admin.log.dir=$EXO_ADMIN_LOGS_DIR \
                      -Dcloud.admin.crypt.registration.password=true \
                      -Dcloud.admin.data.dir=$EXO_ADMIN_DATA_DIR \
                      -Dtenant.masterhost=$TENANT_MASTERHOST \
                      -Dcloud.admin.configuration.dir=$EXO_ADMIN_CONF_DIR \
                      -Dcloud.admin.userlimit=$EXO_ADMIN_CONF_DIR/user-limits.properties \
                      -Dcloud.admin.configuration.file=$EXO_ADMIN_CONF_DIR/admin.properties \
                      -Dcloud.admin.hostname.file=$EXO_ADMIN_CONF_DIR/hostname.cfg \
                      -Dlogback.configurationFile=$CATALINA_HOME/conf/logback.xml \
                      -Dlogback.smtp.appender.conf.file=${EXO_LOGBACK_SMTP_CONF}"

JMX_OPTS="-Dcom.sun.management.jmxremote=true -Djava.rmi.server.hostname=$HOST_EXTERNAL_ADDR \
    -Dcom.sun.management.jmxremote.authenticate=true \
    -Dcom.sun.management.jmxremote.password.file=$JMXPAS \
    -Dcom.sun.management.jmxremote.access.file=$JMXACC \
    -Dcom.sun.management.jmxremote.ssl=false"

#uncomment if you want to debug app server
#REMOTE_DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y"
REMOTE_DEBUG=""

export JAVA_OPTS="$EXO_JAVA_OPTS $JAVA_OPTS $EXO_CLOUD_ADMIN_OPTS $REMOTE_DEBUG $JMX_OPTS"
export CLASSPATH="${CATALINA_HOME}/conf/:${CATALINA_HOME}/lib/jul-to-slf4j.jar:\
${CATALINA_HOME}/lib/slf4j-api.jar:$CATALINA_HOME/lib/security-logback-logging-CM_VERSION.jar:${CATALINA_HOME}/lib/logback-classic.jar:${CATALINA_HOME}/lib/logback-core.jar:\
${CATALINA_HOME}/lib/mail.jar"

# Catalina pid file
[ -z "$CATALINA_PID" ]  && CATALINA_PID="$CATALINA_HOME/temp/catalina.tmp"
export CATALINA_PID
