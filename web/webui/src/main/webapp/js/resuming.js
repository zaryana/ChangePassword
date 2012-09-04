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
require([ "cloud/tenant", "cloud/marketo", "cloud/trackers", "cloud/support" ], function(tenant, marketo, trackers, support) {

	var email;
	var tenantName;
	var userName;
	var loginUrl;
	var signupUrl = prefixUrl + "/signup-done.jsp";
	var resetUrl = prefixUrl + "/reset-password.jsp";

	function isOnline() {
		tenant.status({
			tenantname : tenantName
		}, {
			done : function(status) {
				var search = "ONLINE";
				if (status.substring(0, search.length) === search) {
					if ($.getUrlVar("action") == "signup") {
						tenant.isUserExists({
							tenantname : tenantName,
							username : userName
						}, {
							done : function(result) {
								if (result == "false") {
									window.location = signupUrl;
								} else {
									window.location = loginUrl;			
								}
							},
							fail : function(err) {
								logError("resuming.isOnline(): User Exists service failed for '" + userName + "@" + tenantName + "'. " + err); 
								$("#sign_link").html("The " + tenantName + " Workspace is beind created.<br/> We will inform you by email when ready.");
							}
						});
					} else if ($.getUrlVar("action") == "reset") {
						window.location = resetUrl;
					} else {
						window.location = loginUrl;
					}
				} else {
					setTimeout(isOnline, 5000); // try again in 5sec
				}
			},
			fail : function(err) {
				logError("resuming.isOnline(): Error getting the status '" + err + "'");
			}
		});
	}

	function tryResume() {
		tenant.start({
			tenantname : tenantName
		}, {
			fail : function(err, status) {
				if (err.indexOf("Starting failed... not available space on application servers") != -1) {
					setTimeout(tryResume, 5 * 60 * 1000); // try again in 5min
				} else {
					$("#messageString").html("Your tenant cannot be resumed in time. This was reported to administrators. Try again later.");
				}
			},
			done : function(data) {
				waitReadyWorkspace(tenantName, function() {
					window.location = document.URL;
				});
			}
		});
	}
	
	// TODO temporarily solution until the CM 1.2??
	function waitReadyWorkspace(tenantName, ready) {
	  var isReadyUrl = location.protocol + '//' + hostName + '/rest/cloud-agent/info-service/is-ready/' + tenantName;
	  $.ajax({
	    url : isReadyUrl,
	    async : false,
	    dataType : "text",
	    success : function(data) {
	      if (data === "true")
	        ready();
	      else
	        setTimeout(function() {
	          waitReadyWorkspace(tenantName, ready);
	        }, 5000);
	    },
	    error : function(response, status, error) {
	      setTimeout(function() {
	        waitReadyWorkspace(tenantName, ready);
	      }, 5000);
	    }
	  });
	}

	$(function() {
		if ( (queryString != null && queryString != "") || (location.pathname != null && location.pathname != "")) {
			if ($("#tenantname").length > 0) {
				// resuming page under a tenant url
				tenantName = $("#tenantname").text();
				$("#li1").html("If you are already a member of the " + tenantName + " social intranet, please <a href='javascript:void(0);' onClick='window.location.reload();'>try again</a> in a few minutes.");
				$("#li2").html("If you are trying to join the " + tenantName + " social intranet, check you mail box for an invitation.");
				tryResume(tenantName);
			} else {
				// redirected on resuming page
				email = $.getUrlVar("email");
				var userInfo = tenant.getUserInfo(email);
				tenantName = userInfo.tenant;
				userName = userInfo.username;
				loginUrl = tenant.getLoginUrl({
					tenantname : tenantName
				});

				$("#li1").html("If you are already a member of the " + tenantName + " Workspace, please <a href='" + loginUrl + "'>try again</a> in few minutes.");
				$("#li2").html("If you are trying to join " + tenantName + " Workspace, check you mail box for an invitation");

				isOnline();
			}
		}
	});
});