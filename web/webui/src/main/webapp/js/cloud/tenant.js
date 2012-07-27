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
		var	tenantSecureServicePath = accessSecureUrl + "/cloudworkspaces/tenant-service";
		
		/*this.init = function() {
		$.extend({
			getUrlVars : function() {
				var vars = [], hash;
				var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
				for (var i = 0; i < hashes.length; i++) {
					hash = hashes[i].split('=');
					vars.push(hash[0]);
					vars[hash[0]] = hash[1];
				}
				return vars;
			},
			getUrlVar : function(name) {
				return $.getUrlVars()[name];
			}
		});

		// jQuery.validator
		$.validator.addMethod('regexp', function(value, element, regexp) {
			var re = new RegExp(regexp);
			return this.optional(element) || re.test(value);
		}, "Such name cannot be used.");
		};*/
		
		this.signup = function(settings) {
			var request = $.ajax({
				type : "POST",
				url : tenantServicePath + "/signup",
				//contentType: "application/x-www-form-urlencoded",
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
				var self = this;
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
				logError("Tenant.status(): need tenant name");
			}
		}
		
		this.status0 = function(settings) {
			var self = this;
			$.ajax({
				url : tenantServicePath + "/status/" + settings.tenantName,
				success : function(data) {
					var search = "ONLINE";
					if (data.substring(0, search.length) === search) {
						tenant.isUserExists({
							userName : split[0], // TODO split[0] ?
							tenantName : tenantName,
							onSuccess : function(data) {
								var search = "true";
								if (data.substring(0, search.length) === search) {
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
				error : function(request, status, error) {
					$("#sign_link").html("The " + tName + " Workspace is beind created.<br/> We will inform you by email when ready.");
				},
				dataType : 'text'
			});
		};
		
		this.isUserExists = function(settings) {
			$.ajax({
				url : tenantServicePath + "/isuserexist/" + settings.tenantName + "/" + settings.userName,
				success : settings.onSuccess,
				error : settings.onError,
				dataType : 'text'
			});
		};
	};
	
	return new Tenant();
});