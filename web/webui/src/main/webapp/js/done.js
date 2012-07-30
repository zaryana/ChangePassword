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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNUjr
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
		// load trackers
		trackers.load();
		
		var email = location.hash.substring(1);
		if (email) {
			var split = email.split('@');
	    var tenantName = getUserMailInfo(email).tenant; // TODO
	    var userName = ""; // TODO
	  
	    tenant.status({
	    	tenantName : tenantName,
				done : function(resp) {
						var search = "ONLINE";
						if (resp.substring(0, search.length) === search) {
							tenant.isUserExists({
								tenantName : tenantName,
								userName : userName, 
								exists : function() {
									var tenantUrl = tenant.getLoginUrl({ 
										tenantName : tenantName
									});		
									$("#sign_link").html("You can now <span style=\"color:#19BBE7;\"><u>sign-in</u></span> the " + tenantName + "  Workspace.");
									$("#sign_link").attr("href", tenantUrl);
								},
								notExists : function() {
									$("#sign_link").html("<span style=\"color:#b81919;\">We cannot add you to the " + tenantName + " Workspace at the moment. The Workspace administrator has been notified of your attempt to join.</span>");
								},
								serverError : function(err) {
									logError("User exist '" + userName +	"@" + tenantName + "' service failed. " + err); // for debug
									$("#sign_link").html("The " + tenantName + " Workspace is beind created.<br/> We will inform you by email when ready.");
								}
							});
						} else {
							$("#sign_link").html("The " + tenantName + " Workspace is beind created.<br/> We will inform you by email when ready.");
						}
				},
				serverError : function(err) {
					logError("Tenant status '" + tenantName + "' service failed. " + err); // for debug
					$("#sign_link").html("The " + tenantName + " Workspace is beind created.<br/> We will inform you by email when ready.");
				} 
				//, always : function() {}
			});
		}
		
	});
});
