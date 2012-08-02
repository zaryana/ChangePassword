require([ "cloud/tenant", "cloud/marketo", "cloud/trackers" ], function(tenant, marketo, trackers) {

	function showContactUs(event) {
		event.preventDefault();

		var body = document.body, html = document.documentElement;
		var height = Math.max(body.scrollHeight, body.offsetHeight, html.clientHeight, html.scrollHeight, html.offsetHeight);
		$("#MaskLayer").css("height", height - 140 + "px");
		$("#MaskLayer").show();

		var container = $("#contactUsContainer");
		// Get the window height and width
		var winH = $(window).height();
		var winW = $(window).width();

		// Set the popup window to center
		container.css("top", winH / 2 - container.height() / 2);
		container.css("left", winW / 2 - container.width() / 2);

		container.load(prefixUrl + "/contact-us.jsp", function() {
			container.show();

			$("#submitButton").click(sendFeedback);
			$("#cancelButton").click(cancel);

			// TODO we don't realy need it as all this happens on the initial page url, not contact-us.jsp page
			// Load JS file
			// $.getScript("/js/mktFormSupport.js");
			// Fill data for Marketo form
			// $.getScript("/js/trackers.js");
		});
	}

	function sendFeedback(event) {
		event.preventDefault();

		$.validator.setDefaults({
			errorPlacement : function(error, element) {
				error.appendTo(element.next());
			}
		});
		
		$("#mycontactForm").validate({
			rules : {
			  email : {
			    required : true,
			    minlength : 5,
			    regexp : emailPattern
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
		
		var valid = $("#mycontactForm").valid();
		if (valid) {
			$("#submitButton").val("Wait...");
			$("#cancelButton").attr("disabled", "disabled");

			tenant.sendFeedback({
				"user-mail" : $.trim($("#email").val()),
				"user-name" : $.trim($("#first_name").val()) + " " + $.trim($("#last_name").val()),
				"user-phone" : $.trim($("#phone_work").val()),
				"subject" : $.trim($("#subject").val()),
				"text" : $.trim($("#message").val())
			}, {
				fail : function(err) {
					$("#messageString").html(err);
				},
				done : function(resp) {
					if (resp == "") {
						marketo.send({
							"FirstName" : $.trim($("#first_name").val()),
							"LastName" : $.trim($("#last_name").val()),
							"Phone" : $.trim($("#phone_work").val()),
							"Email" : $("#email").val(),
							"Cloud_Workspaces_Contact_Us_Subject__c" : $("#subject").val(),
							"Cloud_Workspaces_Contact_Us_Message__c" : $("#message").val(),
							"Cloud_Workspaces_User__c" : "Yes",
							"lpId" : $('input[name=lpId]').val(),
							"subId" : $('input[name=subId]').val(),
							"formid" : $('input[name=formid]').val(),
							"_mkt_trk" : $('input[name=_mkt_trk]').val()
						}, function() {
							window.location = "/contact-us-done.jsp";
						});
					} else {
						// $("#MaskLayer").hide();
						// $("#contactUsContainer").hide();
						$("#messageString").html(resp);
					}
				}
			});
		}
	}

	function cancel(event) {
		event.preventDefault();

		$("#MaskLayer").hide();
		$("#contactUsContainer").hide();
	}

	$(function() {
		$("#showContactUs").click(showContactUs);
	});

});