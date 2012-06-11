#!/bin/bash


curl -s -u cloudadmin:cloudadmin 'http://localhost:8080/rest/private/cloud-admin/info-service/server-states' | \
sed -e 's/[{}]/''/g' | awk '{n=split($0,a,","); for (i=1; i<=n; i++) {f=split(a[i],b, ":");  print b[1]; system("./local-cloud/stop-instance.sh " b[1])  } }'


curl -s -u cloudadmin:cloudadmin 'http://localhost:8080/rest/private/cloud-admin/info-service/tenant-list-all' | \
sed -e 's/["\[]/''/g' |sed -e 's/[]]/''/g' | awk '{n=split($0,a,","); for (i=1; i<=n; i++) { print a[i];  system("mysql -hlocalhost -u$EXO_DB_USER -p$EXO_DB_PASSWORD -e \"drop database " a[i] "\"") } }'

cd admin-tomcat/bin
./catalina.sh stop -force

# drop default database
mysql -hlocalhost -u$EXO_DB_USER -p$EXO_DB_PASSWORD -e "drop database \`repository\`"

