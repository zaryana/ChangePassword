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
define(["jquery"], function() {

	function Tenant() {
		var accessUrl = prefixUrl + "/rest/cloud-admin";
		var accessSecureUrl = prefixUrl + "/rest/private/cloud-admin";
		var tenantServicePath = accessUrl + "/cloudworkspaces/tenant-service";
		var tenantSecureServicePath = accessSecureUrl + "/cloudworkspaces/tenant-service";

		this.signup = function(settings) {
			var request = $.ajax({
				type : "POST",
				url : tenantServicePath + "/signup",
				// contentType: "application/x-www-form-urlencoded",
				statusCode : {
					309 : function(data, textStatus, jqXHR) {
						// custom redirect
						if (settings.redirect) {
							var location = jqXHR.getResponseHeader("Location");
							settings.redirect(location);
						}
					},

					400 : function(jqXHR, textStatus, err) {
						// wrong (non company) email
						if (settings.wrongEmail) {
							settings.wrongEmail(jqXHR.responseText);
						}
					}
				},
				data : {
					"user-mail" : settings.userMail
				}
			});
			request.fail(function(jqXHR, textStatus, err) {
				// general error handling (all other errors)
				if (settings.serverError) {
					settings.serverError(textStatus);
				}
			});
			request.done(function(data, textStatus, jqXHR) {
				if (settings.done) {
					settings.done(data);
				}
			});
			request.always(function(jqXHR, textStatus) {
				if (settings.always) {
					settings.always();
				}
			});
		};

		this.status = function(settings) {
			if (settings.tenantName && settings.tenantName.length > 0) {
				var request = $.ajax({
					type : "POST",
					url : tenantServicePath + "/status/" + settings.tenantName,
					dataType : 'text'
				});
				request.fail(function(jqXHR, textStatus, err) {
					// general error handling (all other errors)
					if (settings.serverError) {
						settings.serverError(textStatus);
					}
				});
				request.done(function(data, textStatus, jqXHR) {
					if (settings.done) {
						settings.done(data);
					}
				});
				request.always(function(jqXHR, textStatus) {
					if (settings.always) {
						settings.always();
					}
				});
			} else {
				logError("Tenant.status(): tenant name required");
			}
		};

		this.isUserExists = function(settings) {
			var request = $.ajax({
				url : tenantServicePath + "/isuserexist/" + settings.tenantName + "/" + settings.userName,
				dataType : 'text'
			});
			request.fail(function(jqXHR, textStatus, err) {
				// general error handling (all other errors)
				if (settings.serverError) {
					settings.serverError(textStatus);
				}
			});
			request.done(function(data, textStatus, jqXHR) {
				if (data == "true") {
					if (settings.exists) {
						settings.exists();
					}
				} else if (data == "false") {
					if (settings.notExists) {
						settings.notExists();
					}
				}
			});
			request.always(function(jqXHR, textStatus) {
				if (settings.always) {
					settings.always();
				}
			});
		};

		this.getLoginUrl = function(settings) {
			if (settings.tenantName) {
				var loginUrl = location.protocol + "//" + settings.tenantName + '.' + hostName + "/portal";

				if (settings.userName && settings.password) {
					loginUrl += "/login?username=" + settings.userName + "&password=" + settings.password + "&";
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

		this.getEmail = function(settings) {
			var request = $.ajax({
				url : var checkURL = tenantServicePath + "/uuid/" + settings.uuid,
				dataType : "text"
			});
			request.fail(function(jqXHR, textStatus, err) {
				// general error handling (all other errors)
				if (settings.serverError) {
					settings.serverError(textStatus);
				}
			});
			request.done(function(data, textStatus, jqXHR) {
				if (settings.onEmail) {
						settings.onEmail(data);
				}
			});
			request.always(function(jqXHR, textStatus) {
				if (settings.always) {
					settings.always();
				}
			});
		};
	};

	return new Tenant();
});
