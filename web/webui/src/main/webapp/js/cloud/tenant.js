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
		var tenantSecureServicePath = accessSecureUrl + "/cloudworkspaces/tenant-service";

		var initRequestDefaults(request, callbacks) {
			request.fail(function(jqXHR, textStatus, err) {
				// general error handling (all other errors)
				if (callbacks.fail) {
					callbacks.fail(textStatus);
				}
			});
			request.done(function(data, textStatus, jqXHR) {
				if (callbacks.done) {
					callbacks.done(data);
				}
			});
			request.always(function(jqXHR, textStatus) {
				if (callbacks.always) {
					callbacks.always();
				}
			});
		}
		
		this.signup = function(data, callbacks) {
			var request = $.ajax({
				type : "POST",
				url : tenantServicePath + "/signup",
				data : data,
				// contentType: "application/x-www-form-urlencoded",
				statusCode : {
					309 : function(data, textStatus, jqXHR) {
						// custom redirect
						if (callbacks.redirect) {
							var location = jqXHR.getResponseHeader("Location");
							callbacks.redirect(location);
						}
					},

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
				data : data
			});
			
			initRequestDefaults(request, callbacks);
		};

		this.status = function(data, callbacks) {
			if (settings.tenantName && settings.tenantName.length > 0) {
				var request = $.ajax({
					type : "POST",
					url : tenantServicePath + "/status/" + data.tenantname,
					dataType : 'text'
				});
				
				initRequestDefaults(request, callbacks);
			} else {
				logError("Tenant.status(): tenant name required");
			}
		};

		this.isUserExists = function(data, callbacks) {
			var request = $.ajax({
				url : tenantServicePath + "/isuserexist/" + data.tenantname + "/" + data.username,
				dataType : 'text'
			});
			
			initRequestDefaults(request, callbacks);
		};

		this.getLoginUrl = function(data) {
			if (data.tenantName) {
				var loginUrl = location.protocol + "//" + data.tenantName + '.' + hostName + "/portal";

				if (data.userName && data.password) {
					loginUrl += "/login?username=" + data.userName + "&password=" + data.password + "&";
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
				url : var checkURL = tenantServicePath + "/uuid/" + data.uuid,
				dataType : "text"
			});
			
			initRequestDefaults(request, callbacks);
			
		};
		
		this.getUserInfo = function(email) {
			var info;
			// ask synchronously
			var request = $.ajax({
				async: false,
				url : tenantServicePath + "/usermailinfo/" + email,
				dataType : 'json'
			});
			request.fail(function(jqXHR, textStatus, err) {
				logError("Tenant.getUserMailInfo(): cannot process user record for " + email);
				throw "Application error: cannot process user record. Please contact support.";
			});
			request.done(function(data, textStatus, jqXHR) {
				function(data) {
					info =  data;
        }
			});
			
			return info;
		}
	};

	return new Tenant();
});
