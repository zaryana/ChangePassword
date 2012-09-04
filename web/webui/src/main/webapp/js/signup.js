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
require([ "cloud/tenant", "cloud/marketo", "cloud/marketo.cookies", "cloud/trackers", "cloud/support", "jquery.lightbox", "thickbox" ], function(tenant, marketo, marketoCookies, trackers, support) {

	function signup() {
		var userMail = $('#email').val();
		if (userMail.length == 0) {
			$("#messageString").html("Please, indicate your email.");
			return;
		}

		if (userMail.indexOf('%') > -1) {
			$("#messageString").html("Your email contains disallowed characters.");
			return;
		}

		$("#t_submit").val("Wait...");
		$("#t_submit").attr('disabled', 'disabled');

		// tenants.xmlhttpPost(url, tenants.handleSignupResponse, tenants.getquerystringSignup, null); -->
		tenant.signup({
			"user-mail" : userMail
		}, {
			done : function(resp) {
				if (resp == "") {
					marketo.send({
						"Email" : userMail,
						"Cloud_Workspaces_User__c" : "Yes",
						"Search_Engine__c" : $("input[name=Search_Engine__c]").val(),
						"Search_String__c" : $("input[name=Search_String__c]").val(),
						"Pay_Per_Click_Keyword__c" : $("input[name=Pay_Per_Click_Keyword__c]").val(),
						"sfga" : $("input[name=sfga]").val(),
						"lpId" : $("input[name=lpId]").val(),
						"subId" : $("input[name=subId]").val(),
						"formid" : $("input[name=formid]").val(),
						"_mkt_trk" : $("input[name=_mkt_trk]").val()
					}, function() {
						window.location = prefixUrl + "/signup-done.jsp";
					});
				} else {
					// TODO don't need Marketo here, see tryAgain callback below
					/*
					 * marketo.send({ "Email" : $('#email').val(), "Cloud_Workspaces_User__c" : "No", "Search_Engine__c" :
					 * $('input[name=Search_Engine__c]').val(), "Search_String__c" : $('input[name=Search_String__c]').val(),
					 * "Pay_Per_Click_Keyword__c" : $('input[name=Pay_Per_Click_Keyword__c]').val(), "sfga" : $('input[name=sfga]').val(),
					 * "LeadSource" : $('#LeadSource').val(), "lpId" : $('input[name=lpId]').val(), "subId" : $('input[name=subId]').val(),
					 * "formid" : $('input[name=formid]').val(), "_mkt_trk" : $('input[name=_mkt_trk]').val() }, function() { });
					 */
					$("#messageString").html("<span class='WarningIcon'>" + resp + "</span>");
				}
			},
			tryAgain : function(location) {
				marketo.send({
					"Email" : userMail,
					"Cloud_Workspaces_User__c" : "No",
					"Search_Engine__c" : $("input[name=Search_Engine__c]").val(),
					"Search_String__c" : $("input[name=Search_String__c]").val(),
					"Pay_Per_Click_Keyword__c" : $("input[name=Pay_Per_Click_Keyword__c]").val(),
					"sfga" : $("input[name=sfga]").val(),
					"LeadSource" : $("#LeadSource").val(),
					"lpId" : $("input[name=lpId]").val(),
					"subId" : $("input[name=subId]").val(),
					"formid" : $("input[name=formid]").val(),
					"_mkt_trk" : $("input[name=_mkt_trk]").val()
				}, function() {
					window.location = location;
				});
			},
			resuming : function(location) {
				window.location = location;
			},
			wrongEmail : function(message) {
				$("#messageString").html("<span class='WarningIcon'>" + message + "</span>");
			},
			fail : function(err) {
				$("#messageString").html("<span class='WarningIcon'>Unexpected error happened (" + err + "). Please contact support.</span>");
			},
			always : function() {
				// finally enable button
				$("#t_submit").removeAttr("disabled");
				$("#t_submit").val("Sign Up");
			}
		});
	}

	// init page
	// domReady(function () { // RequireJS way of an init
	$(function() {
		$("#t_submit").click(signup);

		// set marketo cookies
		marketoCookies.set(document);

		$(".lightbox").lightbox({
			fitToScreen : true,
			imageClickClose : false,
			fileLoadingImage : "/lightbox/images/loading.gif",
			fileBottomNavCloseImage : "/lightbox/images/closelabel.gif"
		});

		// preload images to display in lightbox immediately
		var links = document.getElementsByTagName("a");
		for ( var i in links) {
			var link = links[i];
			if (link.className && link.className === "lightbox" && link.href) {
				preload(link.href);
			}
		}

		function preload(imageSrc) {
			var imageObj = new Image();
			imageObj.src = imageSrc;
		}

		// init if it's Try Again page
		if ($("#tryAgainNote").length > 0) {
			var tryagainMessage = getCookie("cw-tryagain");
			if (tryagainMessage) {
				$("#tryAgainNote").html("Sorry, we really need a company email address.");
			} else  {
				setCookie("cw-tryagain", "true", 5);
			}
		}
	});
});
