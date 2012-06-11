#!/bin/sh
#
# Copyright (C) 2011 eXo Platform SAS.
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
# Prepare Cloud Workspaces Application Server instance for use in production.
# This script 
# * unzips Platform bundle 
# * starts local MySQL server and creates database 'repository' with user clouduser/cloud12321
# * starts the Platform with sessions of EXO_DB_HOST EXO_DB_USER and EXO_DB_PASSWORD pointing to local MySQL
# * waits for Platform start and 
# * call agent's template service to create a tenant template backup (JCR backup)
# * wait for backup done and 
# * stops the Platform server
# * achives the Platform server as a new Platform bundle (optional)
