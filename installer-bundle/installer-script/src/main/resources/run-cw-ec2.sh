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

URL="http://169.254.169.254"				# Base URL for retrieve instance metadata
URI_InstID="/latest/meta-data/instance-id"		# Instance ID , used as name for default repo for app.server
URI_ExtIP="/latest/meta-data/public-ipv4"		# External IP , used for JMX access
URI_UData="/latest/user-data"				# Additional EBS drive, used as storage for tomcat ( indexes )

EC2_HOME="/usr/local/ec2/api-tools-1.5.3.1"
JAVA_HOME="/usr/local/jdk1.6.0_32-64"
EC2_PRIVATE_KEY="/root/.settings/pk-CldWks-Launcher.pem"
EC2_CERT="/root/.settings/cert-CldWks-Launcher.pem"

PATH=$EC2_HOME/bin:$JAVA_HOME/bin:$PATH:$HOME/bin
export EC2_HOME JAVA_HOME EC2_PRIVATE_KEY EC2_CERT PATH

#Begin main program
	if [ ! -f $INIT ]; then
	  touch $INIT
	else
	  echo "Run not at first time" >> $SCR_Log
	  exit 0
	fi

# Get instance ID
	date +'%s' >> $SCR_Log

	while true; do
	  ping -c 1 -W 1 -q proxys-cldint-stg.exoplatform.org && break  || sleep 5
	done

	date +'%s' >> $SCR_Log

	wget -c ${URL}${URI_InstID} -O $ResFile --no-clobber --wait=15 --retry-connrefused --tries=20 -o ${WGET_Log}
	date +'%s' >> $SCR_Log

	InstID=$(grep -E '^i-[[:xdigit:]]{8}$' $ResFile)
	[ -n "${InstID}" ] && {
	  rm -rf $ResFile
	  echo "Instance ID : ${InstID}" >> $SCR_Log
	} || {
	  echo "Error retrieving Instance ID" >> $SCR_Log
	  exit 2
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
# Get User data
	wget ${URL}${URI_UData} -O $ResFile --no-clobber --retry-connrefused --tries=10 -o ${WGET_Log}
# get volume-ID
	VolID=$(cut -f 1 -d\& $ResFile | grep -E 'vol-[[:xdigit:]]{8}' )
	[ -z "$VolID" ] && {
	  echo "Error retrieving VolumeID" >> $SCR_Log
	  exit 2
	} || {
	  echo "Volume ID : $VolID" >> $SCR_Log
	}
# Get tenant masterhost
	TMHost=$(cut -f 2 -d\& $ResFile | grep -E '[[:print:]]{5,}' )
	[ -z "$TMHost" ] && {
	  echo "TenantMasterHost is empty" >> $SCR_Log
	  exit 2
	} || {
	  echo "MasterHost : $TMHost" >> $SCR_Log
	}
# Get DB host name
	DBHost=$(cut -f 3 -d\& $ResFile | grep -E '[[:print:]]{16,}' )
	[ -z "$DBHost" ] && {
	  echo "DB hostname is empty" >> $SCR_Log
	  exit 2
	} || {
	  echo "DBHost : $DBHost" >> $SCR_Log
	}
	
# Get DB user name
	DBUName=$(cut -f 4 -d\& $ResFile | grep -E '[[:alnum:]]{3,}' )
	[ -z "$DBUName" ] && {
	  echo "DB username is empty" >> $SCR_Log
	  exit 2
	} || {
	  echo "DB User name : $DBUName" >> $SCR_Log
	}
# Get DB user name pass
	DBUPass=$(cut -f 5 -d\& $ResFile | grep -E '[[:print:]]{6,}' )
	[ -z "$DBUPass" ] && {
	  echo "DB user password is empty" >> $SCR_Log
	  exit 2
	} || {
	  echo "DB User password: defined" >> $SCR_Log
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
	echo -n "Unpacking bundle " >>$SCR_Log
	unzip -qq "${APP_DIR}/${SRC_DIR}/*.zip" -d $APP_DIR 2>>$SCR_Log && {
	  echo "OK" >> $SCR_Log
	} || {
	  echo "FAIL" >> $SCR_Log 
	  exit 2
	}

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

# Create database for default repository
	while true; do
	  ping -c 1 -W 1 -q ${DBHost} && break  || sleep 5
	done
	
	echo -n "Create DB for default repository " >> $SCR_Log
	SQL="create database \`${InstID}\` default charset latin1 collate latin1_general_cs;"
	$CMDSQL "--user=${DBUName}" "--password=${DBUPass}" "--host=${DBHost}" -B -N -e "$SQL" -w --connect-timeout=20 2>>$SCR_Log && {
	  echo "OK" >> $SCR_Log
	} || {
	  echo "FAIL" >> $SCR_Log
	  exit 2
	}

	su - ${SUSER} -c "$APP_DIR/app-server-tomcat/start_eXo.sh"
#	/etc/init.d/monit start

exit 0
