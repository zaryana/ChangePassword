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
define([ "jquery" ], function() {

	function Tenant() {
		var accessUrl = prefixUrl + "/rest/cloud-admin";
		var accessSecureUrl = prefixUrl + "/rest/private/cloud-admin";
		var tenantServicePath = accessUrl + "/cloudworkspaces/tenant-service";
		var tenantSecureServicePath = accessSecureUrl + "/cloudworkspaces/tenant-service"; // TODO remove

		var redirectWrapper = function(callbacks) {
			return function(jqXHR, data, textStatus) {
				// custom redirect
				if (callbacks.redirect) {
					var location = jqXHR.getResponseHeader("Location");
					callbacks.redirect(location);
				}
			};
		};
		
		var initRequestDefaults = function(request, callbacks) {
			request.fail(function(jqXHR, textStatus, err) {
				if (callbacks.fail && jqXHR.status != 309) {
					callbacks.fail(jqXHR.responseText, err); // return response body and textual portion of the HTTP status
				}
			});
			request.done(function(data, textStatus, jqXHR) {
				if (callbacks.done) {
				  // TODO callbacks.done(data); // Data???? No.
					// Yes- The function gets passed three arguments: 
					// * The data returned from the server, formatted according to the dataType parameter; 
					// * a string describing the status; 
					// * and the jqXHR (in jQuery 1.4.x, XMLHttpRequest)
					// callbacks.done(jqXHR.responseText); // it's not formatted data from the response, why we need it?
					callbacks.done(data);
				}
			});
			request.always(function(jqXHR, textStatus) {
				if (callbacks.always) {
					callbacks.always();
				}
			});
		};

		this.signup = function(data, callbacks) {
			var request = $.ajax({
				type : "POST",
				url : tenantServicePath + "/signup",
				dataType : "text",
				data : data,
				//contentType: "application/x-www-form-urlencoded",
				statusCode : {
					309 : redirectWrapper(callbacks),
					400 : function(jqXHR, textStatus, err) {
						// wrong (non company) email
						if (callbacks.wrongEmail) {
							callbacks.wrongEmail(jqXHR.responseText);
						}
					}
				}
			});

			initRequestDefaults(request, callbacks);
		};

		this.create = function(data, callbacks) {
			var request = $.ajax({
				type : "POST",
				url : tenantServicePath + "/create",
				dataType : "text",
				data : data
			});

			initRequestDefaults(request, callbacks);
		};

		this.join = function(data, callbacks) {
			var request = $.ajax({
				type : "POST",
				url : tenantServicePath + "/join",
				dataType : "text",
				data : data,
				statusCode : {
					309 : redirectWrapper(callbacks)
				}
			});

			initRequestDefaults(request, callbacks);
		};

		this.start = function(data, callbacks) {
			var request = $.ajax({
				url : accessUrl + "/tenant-service/start?tenant=" + data.tenantname,
				async : true,
				dataType : "text"
			});

			initRequestDefaults(request, callbacks);
		};

		this.status = function(data, callbacks) {
			if (settings.tenantName && settings.tenantName.length > 0) {
				var request = $.ajax({
					type : "POST",
					url : tenantServicePath + "/status/" + data.tenantname,
					dataType : "text"
				});

				initRequestDefaults(request, callbacks);
			} else {
				logError("Tenant.status(): tenant name required");
			}
		};

		this.isUserExists = function(data, callbacks) {
			var request = $.ajax({
				url : tenantServicePath + "/isuserexist/" + data.tenantname + "/" + data.username,
				dataType : "text"
			});

			initRequestDefaults(request, callbacks);
		};

		this.getLoginUrl = function(data) {
			if (data.tenantname) {
				var loginUrl = location.protocol + "//" + data.tenantname + '.' + hostName + "/portal";

				if (data.username && data.password) {
					loginUrl += "/login?username=" + data.username + "&password=" + data.password + "&";
				} else {
					loginUrl += "/dologin?";
				}

				loginUrl += "initialURI=/portal/intranet/welcome";
				return loginUrl;
			} else {
				logError("Tenant.getLoginUrl(): tenant name required");
				throw "Tenant name required";
			}
		};

		this.getEmail = function(data, callbacks) {
			var request = $.ajax({
				url : tenantServicePath + "/uuid/" + data.uuid,
				dataType : "text"
			});

			initRequestDefaults(request, callbacks);
		};

		this.getUserInfo = function(email) {
			var info;
			var err;
			// ask synchronously
			var request = $.ajax({
				async : false,
				url : tenantServicePath + "/usermailinfo/" + email,
				dataType : "json"
			});
			request.fail(function(jqXHR, textStatus, err) {
				logError("Tenant.getUserMailInfo(): cannot process user record for " + email);
				err = "Application error: cannot process user record. Please contact support.";
			});
			request.done(function(data, textStatus, jqXHR) {
				info = data;
			});

			if (err) {
				throw err;
			} else {
				return info;
			}
		};

		this.sendFeedback = function(data, callbacks) {
			var request = $.ajax({
				async : true,
				type : "POST",
				url : tenantServicePath + "/contactus",
				dataType : "text",
				data : data
			});

			initRequestDefaults(request, callbacks);
		}

		this.resetPassword = function(data, callbacks) {
			var request = $.ajax({
				async : true,
				type : "GET",
				url : tenantServicePath + "/passrestore/" + data.email,
				dataType : "text",
				data : data,
				statusCode : {
					309 : redirectWrapper(callbacks)
				}
			});

			initRequestDefaults(request, callbacks);
		}
		
		this.changePassword = function(data, callbacks) {
			var request = $.ajax({
				async : true,
				type : "POST",
				url : tenantServicePath + "/passconfirm/",
				dataType : "text",
				data : data,
				statusCode : {
					309 : redirectWrapper(callbacks)
				}
			});

			initRequestDefaults(request, callbacks);
		}
	};

	return new Tenant();
});
