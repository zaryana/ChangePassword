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

	function fillForm(uuid) {
		if (uuid != "") {
			tenant.getEmail({
				uuid : uuid
			}, {
				done : function(email) {
					if (email != null && email != "") {
						try {
							var userInfo = tenant.getUserInfo(email);
							if ($("#workspace").length > 0) {
								// need for join form' login url
								$("#workspace").val(userInfo.tenant);
							}
							var userName = userInfo.username;
							$("#email").val(email);
							$("#username").val(userName);
							if (userName.indexOf(".") > 0) {
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
			formError("Sorry, we cannot process this request. Please <a class='TenantFormMsg' href='index.jsp'><u>sign up</u></a> again.");
			return;
		}

		$("#registrationForm").validate({
			rules : {
				password : {
					required : true,
					minlength : 6
				},
				password2 : {
					required : true,
					minlength : 6,
					equalTo : "#password"
				},
				company : {
					required : true,
					regexp : namePattern
				},
				first_name : {
					required : true,
					regexp : namePattern
				},
				last_name : {
					required : true,
					regexp : namePattern
				}
			}
		});

		var valid = $("#registrationForm").valid();
		if (valid) {
			$("#t_submit").val("Wait...");
			$("#t_submit").attr("disabled", "disabled");

			tenant.create({
				"user-mail" : $.trim($("#email").val()),
				"first-name" : $.trim($("#first_name").val()),
				"last-name" : $.trim($("#last_name").val()),
				"password" : $.trim($("#password").val()),
				"phone" : $.trim($("#phone_work").val()),
				"company-name" : $.trim($("#company").val()),
				"confirmation-id" : $.trim($("#confirmation-id").val())
			}, {
				fail : function(err) {
					userError(err);
				},
				done : function(resp) {
					marketo.send({
						"Email" : $("#email").val(),
						"FirstName" : $("#first_name").val(),
						"LastName" : $("#last_name").val(),
						"Company" : $("#company").val(),
						"Phone" : $("#phone_work").val(),
						"Cloud_Workspaces_User__c" : "Yes",
						"lpId" : $("input[name=lpId]").val(),
						"subId" : $("input[name=subId]").val(),
						"formid" : $("input[name=formid]").val(),
						"_mkt_trk" : $("input[name=_mkt_trk]").val()
					}, function() {
						window.location = prefixUrl + "/registration-done.jsp";
					});
				},
				always : function() {
					$("#t_submit").removeAttr("disabled");
					$("#t_submit").val("Create");
				}
			});
		}
	}

	function joinTenant() {
		if ($("#confirmation-id").val().length == 0) {
			formError("Sorry, we cannot process this request. Please <a class='TenantFormMsg' href='index.jsp'><u>sign up</u></a> again.");
			return;
		}

		$("#joinForm").validate({
			rules : {
				password : {
					required : true,
					minlength : 6
				},
				password2 : {
					required : true,
					minlength : 6,
					equalTo : "#password"
				},
				first_name : {
					required : true,
					regexp : namePattern
				},
				last_name : {
					required : true,
					regexp : namePattern
				}
			}
		});

		var valid = $("#joinForm").valid();
		if (valid) {
			$("#t_submit").val("Wait...");
			$("#t_submit").attr("disabled", "disabled");

			tenant.join({
				"user-mail" : $.trim($("#email").val()),
				"first-name" : $.trim($("#first_name").val()),
				"last-name" : $.trim($("#last_name").val()),
				"password" : $.trim($("#password").val()),
				"confirmation-id" : $.trim($("#confirmation-id").val())
			}, {
				fail : function(err) {
					userError(err);
				},
				done : function() {
					marketo.send(marketoUserJoinData(), function() {
						var loginUrl = tenant.getLoginUrl({
							tenantname : $("#workspace").val(), // form already have an username and workspace, filled in fillForm
							username : $("#username").val(), 
							password : $("#password").val()
						});
						window.location = loginUrl;
					});
				},
				redirect : function(location) {
					marketo.send(marketoUserJoinData(), function() {
						window.location = location;
					});
				},
				always : function() {
					$("#t_submit").removeAttr('disabled');
					$("#t_submit").val("Sign In");
				}
			});
		}
	}

	// data for join
	function marketoUserJoinData() {
		return {
			"Email" : $("#email").val(),
			"FirstName" : $("#first_name").val(),
			"LastName" : $("#last_name").val(),
			"Cloud_Workspaces_User__c" : "Yes",
			"lpId" : $("input[name=lpId]").val(),
			"subId" : $("input[name=subId]").val(),
			"formid" : $("input[name=formid]").val(),
			"_mkt_trk" : $("input[name=_mkt_trk]").val()
		};
	}

	function resetPassword() {
		$("#t_submit").val("Wait...");

		$("#resetPasswordForm").validate({
			rules : {
				email : {
					required : true,
				}
			},
		});

		if ($("#resePasswordtForm").valid()) {
			tenant.resetPassword({
				email : $("#email").val()
			}, {
				fail : function(err) {
					$("#messageString").html(err);
				},
				redirect : function(location) {
					window.location = location;
				},
				done : function(resp) {
					$("#t_submit").hide();
					$("#messageString").html("<span style=\"color:#19BBE7;\">Request completed, check your email for instructions.</span>");
				},
				always : function() {
					$("#t_submit").val("Change my password");
				}
			});
		}
	}

	function changePassword() {
		$("#t_submit").val("Wait...");
	  
	  $("#changePasswordForm").validate({
	    rules : {
	      password : {
	        required : true,
	        minlength : 6,
	      },
	      password2 : {
	        required : true,
	        minlength : 6,
	        equalTo : "#password"
	      }
	    }
	  });

	  if ($("#changePasswordForm").valid()) {
	  	tenant.changePassword({
	  		uuid : $("#id").val(),
	  		password : $.trim($("#password").val())
	  	},{
	  		fail : function(err) {
          $("#messageString").html(err);
          $("#t_submit").val("Submit");
        },
        done : function(data) {
          $("#t_submit").val("Submit");
          var loginUrl = tenant.getLoginUrl({
						tenantname : data
					});
          $("#messageString").html("<span style=\"color:#19BBE7;\">Success. You can now <a href='" + loginUrl +
          		"'>login</a> with your new password.</span>");
        }
	  	});
	  }
	}

	$(function() {
		if (queryString != null && queryString != "") {
			if ($("#registrationForm").length > 0) {
				var id = $.getUrlVar("id");
				if (id) {
					fillForm(id);
					$("#t_submit").click(createTenant);
				} else {
					formError("Sorry, it's wrong registration link. Please <a class='TenantFormMsg' href='index.jsp'><u>sign up</u></a> again.");
				}
			} else if ($("#joinForm").length > 0) {
				var rfid = $.getUrlVar("rfid");
				if (rfid) {
					fillForm(rfid);
					$("#t_submit").click(joinTenant);
				} else {
					formError("Sorry, it's wrong registration link. Please <a class='TenantFormMsg' href='index.jsp'><u>sign up</u></a> again.");
				}
			} else if ($("#changePasswordForm").length > 0) {
				var id = $.getUrlVar('id');
		    if (id != null && id != "") {
		      $('#id').val(id);
		    } else {
		      $("#messageString").html("Your link to this page is broken. Please, re-request it.");
		    }
				$("#t_submit").click(changePassword);
			}
		} else if ($("#resetPasswordForm").length > 0) {
			$("#t_submit").click(resetPassword);
		}
	});

});
