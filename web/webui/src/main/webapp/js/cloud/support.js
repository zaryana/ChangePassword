require(["jquery", "jquery.validate", "cloud/tenant", "cloud/marketo"], function($, validate, tenant, marketo) {

	function showContactUs() {
		
	}
	
	function sendContact() {
		var url = tenantServicePath + "/contactus";
	  var valid = $("#mycontactForm").valid();
	  if (!valid)
	    return;
	  tenants.xmlhttpPost(url, tenants.handleContactResponse, tenants.getquerystringContactUs, null);

	  // document.getElementById(CONTACT_US_CONTAINER_ID).style.display = "none";
	  // document.getElementById(MASK_LAYER_ID).style.display = "none";
	  $("#submitButton").val("Wait...");
	  $("$cancelButton").attr("disabled", "disabled");
	}
	
	function cancel() {
		$('#MaskLayer').hide(); 
		hideContactUsForm();
	}
	
	$(function() {
		$("#submitButton").click(sendContact);
		$("#cancelButton").click(cancel);
	});
	
});