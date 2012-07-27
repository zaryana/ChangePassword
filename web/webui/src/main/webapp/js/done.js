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
require([ "cloud/tenant", "cloud/marketo", "cloud/trackers", "cloud/support", "thickbox" ], function(tenant, marketo, trackers,
		support) {

	// init *done page
	$(function() {
		var email = location.hash.substring(1);
		if (email) {
	    var split = email.split('@');
	    var tenantName = getUserMailInfo(email).tenant; // TODO
	  
	    tenant.status({
	    	tenantName : tenantName,
				done : function(resp) {
						var search = "ONLINE";
						if (resp.substring(0, search.length) === search) {
							tenant.isUserExists({
								userName : split[0], // TODO split[0] ?
								tenantName : tenantName,
								onSuccess : function(resp) {
									var search = "true";
									if (resp.substring(0, search.length) === search) {
										$("#sign_link").html("You can now <span style=\"color:#19BBE7;\"><u>sign-in</u></span> the " + tName + "  Workspace.");
										$("#sign_link").attr("href", "/signin.jsp?email=" + email);
									} else {
										$("#sign_link").html("<span style=\"color:#b81919;\">We cannot add you to the " + tName + " Workspace at the moment. The Workspace administrator has been notified of your attempt to join.</span>");
									}
								},
								onError : function(request, status, error) {
									$("#sign_link").html("The " + tName + " Workspace is beind created.<br/> We will inform you by email when ready.");
								}
							});
						} else {
							$("#sign_link").html("The " + tName + " Workspace is beind created.<br/> We will inform you by email when ready.");
						}
				},
				serverError : function(err) {
					logError("Tenant status failed. " + err); // for debug
					$("#sign_link").html("The " + tenantName + " Workspace is beind created.<br/> We will inform you by email when ready.");
				} 
				//, always : function() {}
			});
		}
		// load trackers
		trackers.load();
	});
});
