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
require([ "cloud/tenant", "cloud/marketo", "cloud/marketo.cookies", "cloud/trackers", "cloud/support" ], function(tenant, marketo, marketoCookies, trackers, support) {

	function fillForm(uuid) {
		if (uuid != "") {
			tenant.getEmail({
				uuid : id
			}, {
				done : function(email) {
					if (email != null && email != "") {
						try {
							var user = tenant.getUserInfo(email);
							var userName = user.username;
							$("#email").val(email);
							$("#username").val(userName);
							if (userName.indexOf(".") > -1) {
								var splittedName = userName.split('.');
								$("#first_name").val(splittedName[0].capitalize());
								$("#last_name").val(splittedName[1].capitalize());
							} else {
								$("#first_name").val(userName);
							}

							$("#confirmation-id").val(uuid);
						} catch (err) {
							logError("user.fillForm(): " + err.message);
							formError("Application error: user record not found for " + email + ". Please contact support.");
						}
					} else {
						formError("Application error: email is not found. Please contact support.");
					}
				},
				fail : function(err) {
					formError(err); // using error from the server
					/*
					 * var email = $.getUrlVar("email"); if (email != null && email != "") { fillForm(email, id); } else { formError(err); //
					 * using error from the server }
					 */
				}
			});
		} else {
			formError("Application error: damaged confirmation id. Please contact support.");
		}
	}

	function formError(message) {
		$("#messageString").html(message);
		$("#formDisplay").html("<br><center><a class='BackIcon' href='index.jsp'>Home</a></center>");
	}

	function userError(message) {
		$("#messageString").html(message);
	}

	function createTenant() {
		if ($("#confirmation-id").val().length == 0) {
			// $("#messageString").html("Cannot process request, confirmation ID is not set.");
			formError("Sorry, we cannot process this request. Please <a class='TenantFormMsg' href='index.jsp'><u>sign up</u></a> again.");
			return;
		}

		jQuery.validator.setDefaults({
			errorPlacement : function(error, element) {
				error.appendTo(element.next());
			},
		});

		$("#registrationForm").validate({
			rules : {
				password : {
					required : true,
					minlength : 6,
				},
				password2 : {
					required : true,
					minlength : 6,
					equalTo : "#password"
				},
				company : {
					required : true,
					regexp : "^[A-Za-z][\u0000-\u007F\u0080-\u00FFa-zA-Z0-9 '&-.]*[A-Za-z0-9]$"
				},
				first_name : {
					required : true,
					regexp : "^[A-Za-z][\u0000-\u007F\u0080-\u00FFa-zA-Z0-9 '&-.]*[A-Za-z0-9]$"
				},
				last_name : {
					required : true,
					regexp : "^[A-Za-z][\u0000-\u007F\u0080-\u00FFa-zA-Z0-9 '&-.]*[A-Za-z0-9]$"
				}
			}
		});

		var valid = $("#registrationForm").valid();
		if (valid) {
			$("#t_submit").val("Wait...");
			$("#t_submit").attr('disabled', 'disabled');
			// tenants.xmlhttpPost(url, tenants.handleCreationResponse, tenants.getquerystringCreate, null);

			tenant.create({
				"user-mail" : settings.userMail,
				"first-name" : settings.firstName,
				"last-name" : settings.lastName,
				"password" : settings.password,
				"phone" : settings.phone,
				"company-name" : settings.companyName,
				"confirmation-id" : settings.confirmationId
			}, {

			});
		}
	}

	function joinTenant() {

	}

	$(function() {
		if (queryString != null && queryString != "") {
			if ($("registrationForm").length > 0) {
				var id = $.getUrlVar("id");
				if (id) {
					fillForm(id);
					$("#t_submit").click(createTenant);
				} else {
					formError("Sorry, it's wrong registration link. Please <a class='TenantFormMsg' href='index.jsp'><u>sign up</u></a> again.");
				}
			} else if ($("joinForm").length > 0) {
				var rfid = $.getUrlVar("rfid");
				if (rfid) {
					fillForm(rfid);
					$("#t_submit").click(joinTenant);
				} else {
					formError("Sorry, it's wrong registration link. Please <a class='TenantFormMsg' href='index.jsp'><u>sign up</u></a> again.");
				}
			}
		}
	});

});
