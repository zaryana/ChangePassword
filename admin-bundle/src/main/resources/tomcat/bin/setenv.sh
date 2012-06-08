# Environment Variable Prerequisites
#
# JAVA_OPTS override jvm options for example: Xmx, Xms etc.

# custom JAVA options
[ -z "$EXO_JAVA_OPTS" ]  && EXO_JAVA_OPTS="-Xmx128m"
[ -z "$JMXACC" ] && JMXACC="$CATALINA_HOME/conf/jmxremote.access"
[ -z "$JMXPAS" ] && JMXPAS="$CATALINA_HOME/conf/jmxremote.password"

# this host external address
# HOST_EXTERNAL_ADDR=`ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}'`
[ -z "$HOST_EXTERNAL_ADDR" ]  && HOST_EXTERNAL_ADDR="localhost"

# Set global cloud names
# master tenant name
[ -z "$TENANT_MASTERHOST" ]  && TENANT_MASTERHOST="cloud-workspaces.com"

# default app server
[ -z "$TENANT_DEFAULT_HOST" ]  && TENANT_DEFAULT_HOST="as1:8080"

# dir for admin data
[ -z "$EXO_ADMIN_DATA_DIR" ]  && EXO_ADMIN_DATA_DIR="$CATALINA_HOME/data"

# admin logs
[ -z "$EXO_ADMIN_LOGS_DIR" ]  && EXO_ADMIN_LOGS_DIR="$CATALINA_HOME/logs/cloud-admin"

# admin config
[ -z "$EXO_ADMIN_CONF_DIR" ]  && EXO_ADMIN_CONF_DIR="$CATALINA_HOME/exo-admin-conf"

# logback smtp appender configuration file
[ -z "${EXO_LOGBACK_SMTP_CONF}" ] && EXO_LOGBACK_SMTP_CONF="${CATALINA_HOME}/conf/logback-smtp-appender.xml"

# admin email
[ -z "$CLOUD_MAIL_HOST" ]  && CLOUD_MAIL_HOST="smtp.gmail.com"
[ -z "$CLOUD_MAIL_PORT" ]  && CLOUD_MAIL_PORT="465"
[ -z "$CLOUD_MAIL_SSL" ]  && CLOUD_MAIL_SSL="true"
[ -z "$CLOUD_MAIL_USER" ]  && CLOUD_MAIL_USER="exo.plf.cloud.test1@gmail.com"
[ -z "$CLOUD_MAIL_PASSWORD" ]  && CLOUD_MAIL_PASSWORD="exo.plf.cloud.test112321"
[ -z "$CLOUD_MAIL_SMTP_SOCKETFACTORY_CLASS" ]  && CLOUD_MAIL_SMTP_SOCKETFACTORY_CLASS="javax.net.ssl.SSLSocketFactory"
[ -z "$CLOUD_MAIL_SMTP_SOCKETFACTORY_FALLBACK" ]  && CLOUD_MAIL_SMTP_SOCKETFACTORY_FALLBACK="false"
[ -z "$CLOUD_MAIL_SMTP_SOCKETFACTORY_PORT" ]  && CLOUD_MAIL_SMTP_SOCKETFACTORY_PORT="465"
[ -z "$CLOUD_MAIL_SMTP_AUTH" ]  && CLOUD_MAIL_SMTP_AUTH="true"
[ -z "$CLOUD_ADMIN_EMAIL" ]  && CLOUD_ADMIN_EMAIL="exo.plf.cloud.test1@gmail.com"
[ -z "$CLOUD_LOGGER_EMAIL" ]  && CLOUD_LOGGER_EMAIL="exo.plf.cloud.test1@gmail.com"
[ -z "$CLOUD_SUPPORT_EMAIL" ]  && CLOUD_SUPPORT_EMAIL="exo.plf.cloud.test1@gmail.com"
[ -z "$CLOUD_SUPPORT_SENDER" ]  && CLOUD_SUPPORT_SENDER="exo.plf.cloud.test1@gmail.com"
[ -z "$CLOUD_SALES_EMAIL" ]  && CLOUD_SALES_EMAIL="exo.plf.cloud.test1@gmail.com"

# admin credentials
[ -z "$CLOUD_AGENT_USERNAME" ]  && CLOUD_AGENT_USERNAME="cloudadmin"
[ -z "$CLOUD_AGENT_PASSWORD" ]  && CLOUD_AGENT_PASSWORD="cloudadmin"
[ -z "$CLOUD_AGENT_DB_SCHEMES_DIR" ]  && CLOUD_AGENT_DB_SCHEMES_DIR="../../cloud/databases"

# DB connection send to agent
[ -z "$CLOUD_AGENT_DB_URL" ] && CLOUD_AGENT_DB_URL="localhost:3306"
[ -z "$CLOUD_AGENT_DB_USERNAME" ] && CLOUD_AGENT_DB_USERNAME="dbuser"
[ -z "$CLOUD_AGENT_DB_PASSWORD" ] && CLOUD_AGENT_DB_PASSWORD="dbpass"

# admin variables
EXO_CLOUD_ADMIN_OPTS="-Dadmin.agent.auth.username=$CLOUD_AGENT_USERNAME \
                      -Dadmin.agent.auth.password=$CLOUD_AGENT_PASSWORD \
                      -Dadmin.agent.db.url=$CLOUD_AGENT_DB_URL \
                      -Dadmin.agent.db.username=$CLOUD_AGENT_DB_USERNAME \
                      -Dadmin.agent.db.password=$CLOUD_AGENT_DB_PASSWORD \
                      -Dadmin.agent.db.schemes.dir=$CLOUD_AGENT_DB_SCHEMES_DIR \
                      -Dcloud.admin.log.dir=$EXO_ADMIN_LOGS_DIR \
                      -Dcloud.admin.mail.host=$CLOUD_MAIL_HOST \
                      -Dcloud.admin.mail.port=$CLOUD_MAIL_PORT \
                      -Dcloud.admin.mail.ssl=$CLOUD_MAIL_SSL \
                      -Dcloud.admin.mail.user=$CLOUD_MAIL_USER \
                      -Dcloud.admin.mail.password=$CLOUD_MAIL_PASSWORD \
                      -Dcloud.admin.mail.smtp.socketFactory.class=$CLOUD_MAIL_SMTP_SOCKETFACTORY_CLASS \
                      -Dcloud.admin.mail.smtp.socketFactory.fallback=$CLOUD_MAIL_SMTP_SOCKETFACTORY_FALLBACK \
                      -Dcloud.admin.mail.smtp.socketFactory.port=$CLOUD_MAIL_SMTP_SOCKETFACTORY_PORT \
                      -Dcloud.admin.mail.smtp.auth=$CLOUD_MAIL_SMTP_AUTH \
                      -Dcloud.admin.mail.admin.email=$CLOUD_ADMIN_EMAIL \
                      -Dcloud.admin.mail.logger.email=$CLOUD_LOGGER_EMAIL \
                      -Dcloud.admin.mail.support.email=$CLOUD_SUPPORT_EMAIL \
                      -Dcloud.admin.mail.support.sender=$CLOUD_SUPPORT_SENDER \
                      -Dcloud.admin.mail.sales.email=$CLOUD_SALES_EMAIL \
                      -Dcloud.admin.data.dir=$EXO_ADMIN_DATA_DIR \
                      -Dtenant.masterhost=$TENANT_MASTERHOST \
                      -Dcloud.admin.haproxy.default.host=$TENANT_DEFAULT_HOST \
                      -Dcloud.admin.configuration.dir=$EXO_ADMIN_CONF_DIR \
                      -Dcloud.admin.userlimit=$EXO_ADMIN_CONF_DIR/user-limits.properties \
                      -Dcloud.admin.hostname.file=$EXO_ADMIN_CONF_DIR/hostname.cfg \
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
${CATALINA_HOME}/lib/slf4j-api.jar:$CATALINA_HOME/lib/security-logback-logging-1.1-M6.jar:${CATALINA_HOME}/lib/logback-classic.jar:${CATALINA_HOME}/lib/logback-core.jar:\
${CATALINA_HOME}/lib/mail.jar"

# Catalina pid file
[ -z "$CATALINA_PID" ]  && CATALINA_PID="$CATALINA_HOME/temp/catalina.tmp"
export CATALINA_PID
