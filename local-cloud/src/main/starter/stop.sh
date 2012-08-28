#!/bin/bash

CLEAN=true
for ARG in "$@"
do
  if [ "$ARG" = "-noclean" ] ; then
    CLEAN=false
  fi
done

cd ./local-cloud

curl -s -u cloudadmin:cloudadmin 'http://localhost:8080/rest/private/cloud-admin/instance-service/server-states' | \
sed -e 's/[{}]/''/g' | awk '{n=split($0,a,","); for (i=1; i<=n; i++) {f=split(a[i],b, ":");  print b[1]; system("./stop-instance.sh " b[1])  } }'

cd ../

if $CLEAN ; then
  # list of tenants to clean theirs DBs
  # TODO it's not correct way as admin still runs and in theory can use tenant databases - use stop service instead
  curl -s -u cloudadmin:cloudadmin 'http://localhost:8080/rest/private/cloud-admin/tenant-service/tenant-list-all' | \
  sed -e 's/["\[]/''/g' |sed -e 's/[]]/''/g' | awk '{n=split($0,a,","); for (i=1; i<=n; i++) { print a[i];  system("mysql -hlocalhost -u$EXO_DB_USER -p$EXO_DB_PASSWORD -e \"drop database " a[i] "\"") } }'
fi

cd ./admin-tomcat/bin
./catalina.sh stop -force

cd ../

if $CLEAN ; then
  # remove data on admin
  rm -rf ./data/*
  rm -rf ./exo-admin-conf/application-servers/*
  rm -rf ./logs/*
fi 

cd ../

# drop default database
mysql -hlocalhost -u$EXO_DB_USER -p$EXO_DB_PASSWORD -e "drop database if exists repository; drop database if exists repository_as1; drop database if exists repository_as2; drop database if exists repository_as3;"

