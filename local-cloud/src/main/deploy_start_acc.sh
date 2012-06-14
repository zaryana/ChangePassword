#!/bin/bash

PACKAGE_NAME="cloud-workspaces-local-cloud-1.1.0-Beta01.zip"

ssh -i ~/.ssh/wks-acc.key cl-admin@wks-acc.exoplatform.org "rm -rf /home/cl-admin/acceptance/*"
scp -i ~/.ssh/wks-acc.key $PACKAGE_NAME cl-admin@wks-acc.exoplatform.org:/home/cl-admin/acceptance
ssh -i ~/.ssh/wks-acc.key cl-admin@wks-acc.exoplatform.org "unzip -q /home/cl-admin/acceptance/$PACKAGE_NAME -d /home/cl-admin/acceptance/"
ssh -i ~/.ssh/wks-acc.key cl-admin@wks-acc.exoplatform.org "cd /home/cl-admin/acceptance/  && ./start.sh"

sleep 300s

getHTTPCode () {
    echo $(curl --write-out %{http_code} --connect-timeout 1800  --silent --output /dev/null $1)
}

code=0
timeout=30
i=0
while [ $code -ne 302 ] && [ $i -lt $timeout]; do
   code=$(getHTTPCode 'http://wks-acc.exoplatform.org/portal/intranet/home')
   sleep 60s
   i=$((i + 1))
done

if [ $i -eq $timeout ] ; then
    echo "WARNING! Server not responding in time. See app server logs for details."
    exit 1
fi

echo "Acceptance started succesfully at http://wks-acc.exoplatform.org"
exit 0