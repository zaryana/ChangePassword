/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.exoplatform.cloudworkspaces;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.status.ApplicationServerState;
import org.exoplatform.cloudmanagement.admin.status.ApplicationServerStatus;
import org.exoplatform.cloudmanagement.admin.status.ApplicationServerStatusManager;
import org.exoplatform.cloudmanagement.admin.tenant.AgentRequestPerformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cloud-admin statistic services.
 */
@Path("/cloud-admin/statistic")
public class StatisticAllTenants {
	
	private static final Logger LOG = LoggerFactory.getLogger(StatisticAllTenants.class);
  
	private final ApplicationServerStatusManager applicationServerManager;
	
	private final AgentRequestPerformer requestPerformer;
	
	public StatisticAllTenants(ApplicationServerStatusManager applicationServerManager, 
			AgentRequestPerformer requestPerformer){
		super();
		this.applicationServerManager = applicationServerManager;
		this.requestPerformer = requestPerformer;
	}
	
	/**
	* This service returns number of users for each tenant.
	* 
	* Response example:
	* 
	* <pre>
	* {
	*     "tenant1": 3,
	*     "tenant2": 10
	* }
	* </pre>
	* 
	*/
	@Path("number-of-users")
	@GET
	@Produces("application/json")
	@RolesAllowed({"cloud-admin", "cloud-manager"})
	public Response getUsersList()
	{
		Map<String, Integer> allList = new HashMap<String, Integer>();

	    for (Entry<String, ApplicationServerStatus> server : applicationServerManager.getApplicationServerStatusMap()
	    .entrySet())
	    {
	    	if (server.getValue().getServerState() == ApplicationServerState.ONLINE)
	    	{
	    		Map<String,Map<String,String>> userList = new HashMap<String, Map<String, String>>(); 
	    		try
	            {
	               userList = requestPerformer.getUserLists(server.getKey());
	               for (String tenantName : userList.keySet()){
	            	   if (tenantName.indexOf("repository_as") == -1)
	            	   {
	            		   allList.put(tenantName, userList.get(tenantName).size());
	            	   }
	               }
	    		}
	            catch (CloudAdminException e)
	            {
	               LOG.error(e.getLocalizedMessage(), e);
	            }
	         }
	    }
	    return Response.ok(allList, MediaType.APPLICATION_JSON).build();
	}
}
