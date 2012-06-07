#!/bin/bash


#DATA = `curl -u cloudadmin:cloudadmin http://makizz.exoplatform.com.ua/rest/private/cloud-admin/info-service/server-states`
#echo $DATA

curl -s -u cloudadmin:cloudadmin 'http://makizz.exoplatform.com.ua/rest/private/cloud-admin/info-service/server-states' | sed -e 's/[{}]/''/g' | awk '{n=split($0,a,","); for (i=1; i<=n; i++) {f=split(a[i],b, ":");  print b[1]; system("./local-cloud/stop-instance.sh " b[1])  } }'

cd admin-tomcat/bin
./catalina.sh stop -force

