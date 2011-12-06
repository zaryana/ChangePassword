#!/bin/sh
#
# Copyright (C) 2009 eXo Platform SAS.
# 
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
# 
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.
#

#Production Script to launch GateIn
#See gatein-dev.sh for development starup

# Computes the absolute path of eXo
cd `dirname "$0"`

if [ "$EXO_PROFILES" = "" ] ; then 
	EXO_PROFILES="-Dexo.profiles=default,cloud,webos"
fi

JVM64_OPTS="-XX:+UseCompressedOops"
JAVA_OPTS="$JAVA_OPTS -Xms1g -Xmx4g -XX:MaxPermSize=256m $JVM64_OPTS $EXO_PROFILES"
export JAVA_OPTS

# Launches the server
exec "$PRGDIR"./catalina.sh "$@"
