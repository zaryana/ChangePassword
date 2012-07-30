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
require(["cloud/tenant", "cloud/marketo", "cloud/marketo.cookies", "cloud/trackers", "cloud/support" ], 
		function(tenant, marketo, marketoCookies, trackers, support) {

	function fillForm(email) {
		var split = email.split("@");
		var prefix = split[0];
		$("#email").val(email);
		$("#username").val(prefix);
		if (prefix.indexOf(".") > -1) {
			var splittedName = prefix.split('.');
			$("#first_name").val(splittedName[0].capitalize());
			$("#last_name").val(splittedName[1].capitalize());
		} else {
			$("#first_name").val(prefix);
		}
	}

	function setConfirmationId(uuid) {
		if (uuid != "") {
			$("#confirmation-id").val(uuid);
		} else {
			$("#messageString").html("Application error: damaged confirmation id. Please contact support.");
		}
	}
	
	function initRegistrationForm(uuid) {
		tenant.getEmail({
			uuid : uuid,
			onEmail : function(email) {
				if (email != null && email != "") {
					fillForm(email);
				} else {
					$("#messageString").html("Application error: email is not found. Please contact support.");
				}
				setConfirmationId(uuid);
			},
			serverError : function(err) {
				var email = $.getUrlVar("email");
				if (email != null && email != "") {
					fillForm(email);
				} else {
					// $("#messageString").html("Warning! You are using broken link to the Registration Page. Please sign up again.");
					$("#messageString").html(err); // using error from the server
				}
				setConfirmationId(uuid);
			}
		});
	}
	
	function initJoinForm(rfid, email) {
        userinfo = getUserMailInfo(email); // TODO
        $('#email').val(email);
        var prefix = userinfo.username;
        $('#username').val(prefix);
        if (prefix.indexOf('.') > -1) {
          var splittedName = prefix.split('.');
          $('#first_name').val(splittedName[0].capitalize());
          $('#last_name').val(splittedName[1].capitalize());
        } else {
          $('#first_name').val(prefix.capitalize());
        }
        $('#workspace').val(userinfo.tenant);
        $('#rfid').val(rfid);
      } else {
        $("#messageString").html("Application error: email is not found. Please contact support.");
      }
    }
	}

	$(function() {
		if (queryString != null && queryString != "") {
			var id = $.getUrlVar("id");
			if (id) {
				initRegistrationForm(id);
			} else {
				var rfid = $.getUrlVar("rfid");
				if (rfid) {
					initJoinForm(rfid);
				} else {
		      $("#joinForm").html("<br><center><a class='BackIcon' href='index.jsp'>Home</a></center>");
		      $("#messageString").html("Sorry, your registration link has expired. Please <a class='TenantFormMsg' href='index.jsp'><u>sign up</u></a> again.");
		      return;
		    }
			}
		}
	});

});
