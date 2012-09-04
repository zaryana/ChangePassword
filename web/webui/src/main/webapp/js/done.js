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
require([ "cloud/tenant", "cloud/marketo", "cloud/trackers", "cloud/support", "thickbox" ], function(tenant, marketo, trackers, support) {

	// init *done page
	$(function() {
		var email = location.hash.substring(1);
		if (email) {
			var userinfo = tenant.getUserInfo(email);

			tenant.status({
				tenantname : userinfo.tenant
			}, {
				done : function(status) {
					var search = "ONLINE";
					if (status.substring(0, search.length) === search) {
						tenant.isUserExists({
							tenantname : userinfo.tenant,
							username : userinfo.username
						}, {
							done : function(result) {
								if (result == "true") {
									var tenantUrl = tenant.getLoginUrl({
										tenantName : userinfo.tenant
									});
									$("#sign_link").html("You can now <span style=\"color:#19BBE7;\"><u>sign-in</u></span> the " + userinfo.tenant + "  Workspace.");
									$("#sign_link").attr("href", tenantUrl);
								} else if (result == "false") {
									$("#sign_link").html("<span style=\"color:#b81919;\">We cannot add you to the " + userinfo.tenant + " Workspace at the moment. The Workspace administrator has been notified of your attempt to join.</span>");
								}
							},
							fail : function(err) {
								logError("done.init: User Exists service failed for '" + userinfo.username + "@" + userinfo.tenant + "'. " + err); // for
								// debug
								$("#sign_link").html("The " + userinfo.tenant + " Workspace is beind created.<br/> We will inform you by email when ready.");
							}
						});
					} else {
						$("#sign_link").html("The " + userinfo.tenant + " Workspace is beind created.<br/> We will inform you by email when ready.");
					}
				},
				fail : function(err) {
					logError("done.init: Tenant Status '" + userinfo.tenant + "' service failed. " + err); // for debug
					$("#sign_link").html("The " + userinfo.tenant + " Workspace is beind created.<br/> We will inform you by email when ready.");
				}
			});
		}
	});

});
