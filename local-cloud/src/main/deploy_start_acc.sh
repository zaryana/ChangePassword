#!/bin/bash

PACKAGE_NAME="cloud-workspaces-local-cloud-1.1.0-Beta01.zip"

ssh -i ~/.ssh/wks-acc.key cl-admin@wks-acc.exoplatform.org "rm -rf /home/cl-admin/acceptance/*"
scp -i ~/.ssh/wks-acc.key $PACKAGE_NAME cl-admin@wks-acc.exoplatform.org:/home/cl-admin/acceptance
ssh -i ~/.ssh/wks-acc.key cl-admin@wks-acc.exoplatform.org "unzip -q /home/cl-admin/acceptance/$PACKAGE_NAME -d /home/cl-admin/acceptance/"
ssh -i ~/.ssh/wks-acc.key cl-admin@wks-acc.exoplatform.org "cd /home/cl-admin/acceptance/  && ./start.sh"

sleep 120s
curl --connect-timeout 1800 -s  http://wks-acc.exoplatform.org/portal/intranet/home
echo "Acceptance started succesfully at http://wks-acc.exoplatform.org"
exit 0