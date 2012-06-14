#!/bin/bash

ssh -i ~/.ssh/wks-acc.key cl-admin@wks-acc.exoplatform.org "cd /home/cl-admin/acceptance/  && ./stop.sh"