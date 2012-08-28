#!/bin/sh
#
#       /etc/rc.d/init.d/start_with_ebs.sh
#
# Starts the acpi daemon
#
# chkconfig: 345 26 74
# description: Start script for attach ebs disk and start Tomcat

### BEGIN INIT INFO
# Provides: start_with_ebs.sh
# Default-Start:  2 3 4 5
# Default-Stop: 0 1 6
# Short-Description: start script
# Description: Start script for attach ebs disk and start Tomcat
### END INIT INFO

ResFile="/tmp/Out.res"
DEVICE="/dev/xvdd"
SUSER='cl-server'
APP_DIR="/home/${SUSER}"
DATA_DIR="cl-srv-data"
SRC_DIR="install"
SCR_Log="/root/init.log"
WGET_Log="/root/wget-out.log"
INIT="/root/.init"
CMDSQL="/usr/bin/mysql"
JMXTrCFG="/etc/sysconfig/jmxtrans"

URL="http://169.254.169.254"				# Base URL for retrieve instance metadata
URI_InstID="/latest/meta-data/instance-id"		# Instance ID , used as name for default repo for app.server
URI_ExtIP="/latest/meta-data/public-ipv4"		# External IP , used for JMX access
URI_ExtHNm="/latest/meta-data/public-hostname"		# external DNS name, needed for update hostname to this name ( Apache SSL )

URI_UData="/latest/user-data"				# Additional EBS drive, used as storage for tomcat ( indexes )

EC2_HOME="/usr/local/ec2/api-tools-1.5.3.1"
JAVA_HOME="/usr/local/jdk1.6.0_32-64"
EC2_PRIVATE_KEY="/root/.settings/pk-CldWks-Launcher.pem"
EC2_CERT="/root/.settings/cert-CldWks-Launcher.pem"

PATH=$EC2_HOME/bin:$JAVA_HOME/bin:$PATH:$HOME/bin
export EC2_HOME JAVA_HOME EC2_PRIVATE_KEY EC2_CERT PATH

#Begin main program
	[ -f "${INIT}" ] && {
	  echo "Run not at first time" >> $SCR_Log
	  exit 1
	} || {
	  touch "${INIT}"
	}
	echo "Started" >> $SCR_Log
	StartTime=`date +'%s'`

# Get instance ID
	B=0
	while [[ "${B}" -lt "20" ]]; do
	  wget -c ${URL}${URI_InstID} -O $ResFile --no-clobber --wait=15 --retry-connrefused --tries=20 -o ${WGET_Log}
	  InstID=$(grep -E '^i-[[:xdigit:]]{8}$' $ResFile)
	  [ -n "${InstID}" ] && {
	    B=21
	  } || {
	    ((B++))
	    sleep 5
	  }
	  [ -f "$ResFile" ] && rm -rf $ResFile || true
	done
	CurTime=`date +'%s'`
	CurTime=$((CurTime-$StartTime))	
	[[ "${B}" -ne "21" ]] && {
	  echo "Error retrieving Instance ID (${CurTime}s)" >> $SCR_Log
	  exit 2
	} || {
	  echo "Instance ID : ${InstID} (${CurTime}s)" >> $SCR_Log
	}
# Get External IP
	wget -c ${URL}${URI_ExtIP} -O $ResFile --no-clobber --retry-connrefused --tries=10 -o ${WGET_Log}
	ExtIP=$(grep -E '^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$' $ResFile)
	[ -n "$ExtIP" ] && {
	  rm -rf $ResFile
	  echo "External IP : ${ExtIP}" >> $SCR_Log
	} || {
	  echo "Error retrieving External IP" >> $SCR_Log
	  exit 2
	}
# Get External hostname
	wget -c ${URL}${URI_ExtHNm} -O $ResFile --no-clobber --retry-connrefused --tries=10 -o ${WGET_Log}
	ExtHNm=$(grep -E '^ec2-((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\-){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).compute-1.amazonaws.com$' $ResFile)
	[ -n "$ExtHNm" ] && {
	  rm -rf $ResFile
	  echo "External hostname : ${ExtHNm}" >> $SCR_Log
	} || {
	  echo "Error retrieving External hostname" >> $SCR_Log
	  exit 2
	}
# Get User data
	wget ${URL}${URI_UData} -O $ResFile --no-clobber --retry-connrefused --tries=10 -o ${WGET_Log}
# get volume-ID
	VolID=`grep ^volume.id $ResFile | cut -d '=' -f 2`
	[ -z "$VolID" ] && {
	  echo "Error retrieving VolumeID" >> $SCR_Log
	  exit 2
	} || {
	  echo "Volume ID : $VolID" >> $SCR_Log
	}
# Get tenant masterhost
	TMHost=`grep ^masterHost $ResFile | cut -d '=' -f 2`
	[ -z "$TMHost" ] && {
	  echo "TenantMasterHost is empty" >> $SCR_Log
	  exit 2
	} || {
	  echo "MasterHost : $TMHost" >> $SCR_Log
	}
# Get DB host name
	DBHost=`grep ^db.url $ResFile | cut -d '=' -f 2`
	[ -z "$DBHost" ] && {
	  echo "DB hostname is empty" >> $SCR_Log
	  exit 2
	} || {
	  echo "DBHost : $DBHost" >> $SCR_Log
	}
# Get DB user name
	DBUName=`grep ^db.username $ResFile | cut -d '=' -f 2`
	[ -z "$DBUName" ] && {
	  echo "DB username is empty" >> $SCR_Log
	  exit 2
	} || {
	  echo "DB User name : $DBUName" >> $SCR_Log
	}
# Get DB user name pass
	DBUPass=`grep ^db.password $ResFile | cut -d '=' -f 2`
	[ -z "$DBUPass" ] && {
	  echo "DB user password is empty" >> $SCR_Log
	  exit 2
	} || {
	  echo "DB User password: defined" >> $SCR_Log
	}
# Get agent user name
	AgentUName=`grep ^username $ResFile | cut -d '=' -f 2`
	[ -z "$AgentUName" ] && {
	  echo "Agent username is empty" >> $SCR_Log
	  exit 2
	} || {
	  echo "Agent User name : $AgentUName" >> $SCR_Log
	}
# Get agent user name pass
	AgentUPass=`grep ^password $ResFile | cut -d '=' -f 2`
	[ -z "$AgentUPass" ] && {
	  echo "Agent user password is empty" >> $SCR_Log
	  exit 2
	} || {
	  echo "Agent User password: defined" >> $SCR_Log
	}
# Get mail from
	MailFrom=`grep ^mail.admin.email $ResFile | cut -d '=' -f 2`
	[ -z "$MailFrom" ] && {
	  echo "Mail from is empty" >> $SCR_Log
	  exit 2
	} || {
	  echo "Mail from: $MailFrom" >> $SCR_Log
	}
# Get mail host
	MailHost=`grep ^mail.host $ResFile | cut -d '=' -f 2`
	[ -z "$MailHost" ] && {
	  echo "Mail host is empty" >> $SCR_Log
	  exit 2
	} || {
	  echo "Mail host: $MailHost" >> $SCR_Log
	}
# Get mail port
	MailPort=`grep ^mail.port $ResFile | cut -d '=' -f 2`
	[ -z "$MailPort" ] && {
	  echo "Mail port is empty" >> $SCR_Log
	  exit 2
	} || {
	  echo "Mail port: $MailPort" >> $SCR_Log
	}
# Get mail user name
	MailUName=`grep ^mail.user $ResFile | cut -d '=' -f 2`
	[ -z "$MailUName" ] && {
	  echo "Mail user name is empty" >> $SCR_Log
	  exit 2
	} || {
	  echo "Mail user name: $MailUName" >> $SCR_Log
	}
# Get mail user password
	MailUPass=`grep ^mail.password $ResFile | cut -d '=' -f 2`
	[ -z "$MailUPass" ] && {
	  echo "Mail user password is empty" >> $SCR_Log
	  exit 2
	} || {
	  echo "Mail user password: defined" >> $SCR_Log
	}
# Get Graphite server address
	GraphSHost=`grep ^graphite.host $ResFile | cut -d '=' -f 2`
	[ -z "$GraphSHost" ] && {
	  echo "Grathite server is not defined. JMX will not started" >> $SCR_Log
	  JMXTr=""
	} || {
	  echo "Graphite server is ${GraphSHost}" >> $SCR_Log
	  JMXTr="1"
	}
# Get Graphite server port
	GraphSPort=`grep ^graphite.port $ResFile | cut -d '=' -f 2`
	[ -z "$GraphSPort" ] && {
	  echo "Port of grathite server is not defined. JMX will not started" >> $SCR_Log
	  JMXTr=""
	} || {
	  [ -n "${JMXTr}" ] && {
	    echo "Graphite server port is ${GraphSPort}" >> $SCR_Log
	    JMXTr="1"
	  } || {
	    true
	  }
	}
	
# Attache EBS volume for data
	ec2-attach-volume -i $InstID -d "/dev/sdd" $VolID 2>>$SCR_Log && {
	  StartTime=`date +'%s'`
	  while true; do
	    [ -e "$DEVICE" ] && break || {
	      CurTime=`date +'%s'`
	      Diff=$((CurTime-$StartTime))
	      true
	      if [[ "${Diff}" -gt "300" ]]; then
	        break
	      else
	        sleep 5
	      fi
	    }
	  done
	  [ -e "$DEVICE" ] && {
	    echo "Volume $VolID attached as $DEVICE" >> $SCR_Log
	  } || {
	    echo "Attached drive is not exist ($Diff)" >> $SCR_Log
	    exit 2
	  }
	} || {
	  echo "Error while attaching EBS with $VolID to $InstID" >> $SCR_Log
	  exit 2
	}
	
# Create FS on an attached volume
	echo -n "Creating EXT3 on an $DEVICE :" >> $SCR_Log
	mkfs.ext3 -q $DEVICE 2>>$SCR_Log && {
	  echo "OK" >>$SCR_Log
	} || {
	  echo "FAIL" >> $SCR_Log
	  exit 2
	}

# Create data directory	
	echo -n "Creating data directory " >> $SCR_Log
	mkdir -pv "${APP_DIR}/${DATA_DIR}" 2>>$SCR_Log || {
	  echo "FAIL" >> $SCR_Log
	  exit 2
	} || {
	  echo "OK" >> $SCR_Log
	}

# Mount EBS drive into data directory
	echo -n "Mounting $DEVICE into data directory " >> $SCR_Log
	mount $DEVICE "$APP_DIR/$DATA_DIR" 2>>$SCR_Log && {
	  echo "OK" >> $SCR_Log
	} || {
	  echo "FAIL" >> $SCR_Log
	  exit 2
	} 

# Unpack zipped bundle
#	echo -n "Unpacking bundle " >>$SCR_Log
#	unzip -qq "${APP_DIR}/${SRC_DIR}/*.zip" -d $APP_DIR 2>>$SCR_Log && {
#	  echo "OK" >> $SCR_Log
#	} || {
#	  echo "FAIL" >> $SCR_Log 
#	  exit 2
#	}

# Set ownership for unpacked app.server directory
	echo -n "Set ownership for app.server directory " >> $SCR_Log
	chown -R ${SUSER}:${SUSER} "$APP_DIR/app-server-tomcat" 2>>$SCR_Log && {
	  echo "OK" >> $SCR_Log
	} || {
	  echo "FAIL" >> $SCR_Log
	  exit 2
	}

# Change permissions on unpacked app.server directory	
	echo -n "Change permissions for ap.server directory " >> $SCR_Log
	chmod -R "o-wxr,g-w" "$APP_DIR/app-server-tomcat" 2>>$SCR_Log && {
	  echo "OK" >> $SCR_Log
	} || {
	  echo "FAIL" >> $SCR_Log
	  exit 2
	}

# Create directory for logs files ( on additional ephemeral drive )	
	echo -n "Creating directory for logs " >> $SCR_Log
	mkdir -p /mnt/u01/cl-srv-logs 2>>$SCR_Log && {
	  echo "OK" >> $SCR_Log
	} || {
	  echo "FAIL" >> $SCR_Log
	  exit 2
	}

# Set permissions for logs directory
	echo -n "Setting permissions for logs directory " >> $SCR_Log
	chown -R ${SUSER}:${SUSER} /mnt/u01/cl-srv-logs 2>>$SCR_Log && {
	  echo "OK" >> $SCR_Log
	} || {
	  echo "FAIL" >> $SCR_Log
	  exit 2
	}

# Remove existing logs location inside app.server directory
	echo -n "Delete default logs directory" >> $SCR_Log
	rm -rf "${APP_DIR}/app-server-tomcat/logs" >> $SCR_Log && {
	  echo "OK" >> $SCR_Log
	} || {
	  echo "FAIL" >> $SCR_Log
	  exit 2
	}

# Set new symlink for logs directory
	echo -n "Set symlink for default logs directory " >> $SCR_Log
	ln -s "/mnt/u01/cl-srv-logs" "${APP_DIR}/app-server-tomcat/logs" 2>>$SCR_Log && {
	  echo "OK" >> $SCR_Log
	} || {
	  echo "FAIL" >> $SCR_Log
	  exit 2
	}

# Updating tomcat's configuration
	echo "Updating tomcat configuration" >> $SCR_Log
	sed -r -i -f "${APP_DIR}/update-server.xml.sed" "${APP_DIR}/app-server-tomcat/conf/server.xml"
	sed -r -i -f "${APP_DIR}/update-conf.prop.sed" "${APP_DIR}/app-server-tomcat/gatein/conf/configuration.properties"
	sed -r -i -f "${APP_DIR}/update-rootpsw.sed" "${APP_DIR}/app-server-tomcat/gatein/conf/cloud/users.properties"
	
# Put data into correct location
	echo -n "Install backup into new location " >> $SCR_Log
	mv "${APP_DIR}/app-server-tomcat/gatein/backup" "${APP_DIR}/${DATA_DIR}" 2>>$SCR_Log && {
	  echo "OK" >> $SCR_Log
	} || {
	  echo "FAIL" >> $SCR_Log
	  exit 2
	}

	echo -n "Install repository configuration " >> $SCR_Log
	cp "${APP_DIR}/app-server-tomcat/gatein/data/jcr/repository-configuration.xml" "${APP_DIR}/${DATA_DIR}" 2>>$SCR_Log && {
	  echo "OK" >> $SCR_Log
	} || {
	  echo "FAIL" >> $SCR_Log
	  exit 2
	}

	echo -n "Set permission for data directory " >> $SCR_Log
	chown -R ${SUSER}:${SUSER} $APP_DIR/$DATA_DIR 2>>$SCR_Log && {
	  echo "OK" >> $SCR_Log
	} || {
	  echo "FAIL" >> $SCR_Log
	  exit 2
	}
	
# Update system environment
	echo "Updating environment" >> $SCR_Log
	sed -r -i -e "s/^HOST_EXTERNAL_ADDR=(.)*$/HOST_EXTERNAL_ADDR=$ExtIP/g" "${APP_DIR}/.bashrc"
	sed -r -i -e "s/^EXO_DB_HOST=(.*)$/EXO_DB_HOST=${DBHost}/g" "${APP_DIR}/.bashrc"
	sed -r -i -e "s/^EXO_DB_USER=(.*)$/EXO_DB_USER=${DBUName}/g" "${APP_DIR}/.bashrc"
	sed -r -i -e "s/^EXO_DB_PASSWORD=(.*)$/EXO_DB_PASSWORD=${DBUPass}/g" "${APP_DIR}/.bashrc"
	sed -r -i -e "s/^TENANT_MASTERHOST=(.*)$/TENANT_MASTERHOST=${TMHost}/g" "${APP_DIR}/.bashrc"
	sed -r -i -e "s/^TENANT_REPOSITORY=(.*)$/TENANT_REPOSITORY=${InstID}/g" "${APP_DIR}/.bashrc"

	sed -r -i -e "s/^EMAIL_SMTP_FROM=(.*)$/EMAIL_SMTP_FROM=${MailFrom}/g" "${APP_DIR}/.bashrc"
	sed -r -i -e "s/^EMAIL_SMTP_USERNAME=(.*)$/EMAIL_SMTP_USERNAME=${MailUName}/g" "${APP_DIR}/.bashrc"
	sed -r -i -e "s/^EMAIL_SMTP_PASSWORD=(.*)$/EMAIL_SMTP_PASSWORD=${MailUPass}/g" "${APP_DIR}/.bashrc"
	sed -r -i -e "s/^EMAIL_SMTP_HOST=(.*)$/EMAIL_SMTP_HOST=${MailHost}/g" "${APP_DIR}/.bashrc"
	sed -r -i -e "s/^EMAIL_SMTP_PORT=(.*)$/EMAIL_SMTP_PORT=${MailPort}/g" "${APP_DIR}/.bashrc"

# Update tomcat users
	sed -r -i -e "s/<user username=\"cloudadmin\" password=\"cloudadmin\"/<user username=\"$AgentUName\" password=\"$AgentUPass\"/g" "${APP_DIR}/app-server-tomcat/conf/tomcat-users.xml"

# Create database for default repository
	DBPort=`echo $DBHost | cut -f 2 -d\:`
	DBHost=`echo $DBHost | cut -f 1 -d\:`
	echo -n "DB server on-line :" >> $SCR_Log
	DBTime1=`date +'%s'`
	DBTimeDiff=0
	while [[ "$DBTimeDiff" -lt "240" ]]; do
	  ping -c 1 -W 1 -q ${DBHost} && break  || {
	    sleep 5
	    DBTimeDiff=`date +'%s'`
	    DBTimeDiff=$((DBTimeDiff-$DBTime1))
	  }
	done
	
	[[ "$DBTimeDiff" -ge "240" ]] && {
	  echo "No" >> $SCR_Log
	  echo "Stopping because can't create default repository" >> $SCR_Log
	  exit 2
	} || {
	  echo "Yes" >> $SCR_Log
	}
	
	[ -z "$DBPort" ] && DBPort='3306'
	
	echo -n "Create DB for default repository " >> $SCR_Log
	SQL="create database \`${InstID}\` default charset latin1 collate latin1_general_cs;"
	$CMDSQL "--user=${DBUName}" "--password=${DBUPass}" "--host=${DBHost}" "--port=${DBPort}" -B -N -e "$SQL" -w --connect-timeout=20 2>>$SCR_Log && {
	  echo "OK" >> $SCR_Log
	} || {
	  echo "FAIL" >> $SCR_Log
	  exit 2
	}
# Set hostname to an AWS external hostname 
	/bin/hostname proba.exoplatform.org $ExtHNm
	CurHostName=`/bin/hostname`
	[[ "${CurHostName}" -eq "${ExtHNm}" ]] && {
	  echo "Hostname updated to : $CurHostName" >> $SCR_Log
	} || {
	  echo "Hostname wasn't updated. Perhaps, access to log files will not available"
	}
# Need to launch Apache HTTPD for log access
	/etc/init.d/httpd start && {
	  echo "HTTP daemon successfully started" >> $SCR_Log
	} || {
	  echo "HTTP daemon not started :(" >> $SCR_Log
	}
# Check for JMXTrans
	[ -n "${JMXTr}" ] && {
	  sed -r -i -e "s/^JMXTrHost=NOT_DEFINED$/JMXTrHost=${GraphSHost}/" "${JMXTrCFG}"
	  sed -r -i -e "s/^JMXTrPort=NOT_DEFINED$/JMXTrPort=${GraphSPort}/" "${JMXTrCFG}"
	  /etc/init.d/jmxtrans start
	  echo "JMXTrans started" >> ${SCR_Log}
	} || {
	  echo "JMXTrans wasn't launched" >> ${SCR_Log}
	}

# Starting app.server
	echo "Starting app.server (tomcat)" >> $SCR_Log
	su - ${SUSER} -c "$APP_DIR/app-server-tomcat/start_eXo.sh"
#	/etc/init.d/monit start

	CurTime=`date +'%s'`
	CurTime=$((CurTime-$StartTime))
	echo "Preparations launch time : $CurTime" >> $SCR_Log

exit 0
