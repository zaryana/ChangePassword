require([ "jquery", "jquery.validate", "cloud/tenant", "cloud/marketo" ], function($, validate, tenant, marketo) {

	function showContactUs() {
		sendRequest({
			url : url,
			method : "GET",
			handler : onReceiveShowContactFormResponse,
			isAssinchronous : false,
			showLoader : false
		});
	}

	function sendContact() {
		var valid = $("#mycontactForm").valid();
		if (valid) {
			$("#submitButton").val("Wait...");
			$("$cancelButton").attr("disabled", "disabled");
			
			tenant.sendFeedback({
				"user-mail" : $.trim($("#email").val()),
				"user-name" : $.trim($("#name").val()),
				"subject" : $.trim($("#subject").val()),
				"text" : $.trim($("#ContactUs_Message__c").val())
			},{
				
			});
		}	
	}

	function cancel() {
		$("#MaskLayer").hide();
		$("#contactUsContainer").hide();
		
		//var container = document.getElementById(CONTACT_US_CONTAINER_ID);
		//container.innerHTML = "";
		//container.style.display = "none";
		//return false;
	}

	$(function() {
		if ($("#showContactUs").length > 0) {
			$("#showContactUs").click(showContactUs);
		} else if ($("#submitButton").length > 0 && $("#cancelButton").length > 0) {
			$("#submitButton").click(sendContact);
			$("#cancelButton").click(cancel);
		}
	});

});