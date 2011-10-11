function Tenants() {
}

var prefixUrl = location.protocol + '//' + location.hostname;
var queryString = location.query;

if (location.port) {
	prefixUrl += ':' + location.port;
}
var user;
var auth = null;

/** Getting list chain */
Tenants.prototype.init = function() {
	accessUrl = prefixUrl + '/rest/cloud-admin';
	accessSecureUrl = prefixUrl + '/rest/private/cloud-admin';
	tenantServicePath = accessUrl + "/public-tenant-service/";
	infoServicePath = accessSecureUrl + "/info-service/";
	refreshInterval = 10000;
	is_chrome = (navigator.userAgent.toLowerCase().indexOf('chrome') > -1 || navigator.userAgent
			.toLowerCase().indexOf('safari') > -1);
	email = queryString.substring(queryString.indexOf('email='), queryString.indexOf('&'));
	uuid = queryString.substring(queryString.indexOf('id='), queryString.length);
	
}





/* Sending request */
Tenants.prototype.doSingupRequest = function() {

	var url = tenantServicePath + "signup";
	var params = {};
	_gel("t_submit").value = "Wait..";
	_gel("t_submit").disabled = true;

}

/* Sending request */
Tenants.prototype.doCreationRequest = function() {

	var url = tenantServicePath + "join";
	var params = {};
	_gel("t_submit").value = "Wait..";
	_gel("t_submit").disabled = true;

}

/* Sending request */
Tenants.prototype.doJoinRequest = function() {

	var url = tenantServicePath + "create";

	_gel("t_submit").value = "Wait..";
	_gel("t_submit").disabled = true;
     tenants.xmlhttpPost(url, tenants.handleSignupResponse, )
}


Tenants.prototype.handleSignupResponse = function(resp) {

	if (resp.errors == "") {
		_gel("messageString").innerHTML = "<div class=\"Ok\">Tenant creation request sent successfully! Check your email for instructions.</div>";
	} else {
		_gel("messageString").innerHTML = resp.text;
	}
	_gel("t_submit").disabled = false;
	_gel("t_submit").value = "Submit";
	_gel("t_name").value = "";
	_gel("t_email").value = "";
}


Tenants.prototype.handleCreationResponse = function(resp) {

	if (resp.errors == "") {
		_gel("messageString").innerHTML = "<div class=\"Ok\">Tenant creation request sent successfully! Check your email for instructions.</div>";
	} else {
		_gel("messageString").innerHTML = resp.text;
	}
	_gel("t_submit").disabled = false;
	_gel("t_submit").value = "Submit";
	_gel("t_name").value = "";
	_gel("t_email").value = "";
}

Tenants.prototype.handleJoinResponse = function(resp) {

	if (resp.errors == "") {
		_gel("messageString").innerHTML = "<div class=\"Ok\">Tenant confirmed successfully! Creation is in progress. You will receive email when done.</div>";
	} else {
		_gel("messageString").innerHTML = resp.text;
	}

	_gel("t_submitId").disabled = false;
	_gel("t_submitId").value = "Submit";
	_gel("t_id").value = "";

}

Tenants.prototype.xmlhttpPost =  function (strURL, handler, paramsMapper) {
    var xmlHttpReq = false;
    var self = this;
    // Mozilla/Safari
    if (window.XMLHttpRequest) {
        self.xmlHttpReq = new XMLHttpRequest();
    }
    // IE
    else if (window.ActiveXObject) {
        self.xmlHttpReq = new ActiveXObject("Microsoft.XMLHTTP");
    }
    self.xmlHttpReq.open('POST', strURL, true);
    self.xmlHttpReq.setRequestHeader('Content-Type', 'application/json');
    self.xmlHttpReq.onreadystatechange = function() {
        if (self.xmlHttpReq.readyState == 4) {
            updatepage(self.xmlHttpReq.responseText);
        }
    }
    self.xmlHttpReq.send(getquerystring());
}

Tenants.prototype.getquerystringJoin = function () {
    var form     = document.forms['f1'];
    var word = form.word.value;
    qstr = 'w=' + escape(word);  // NOTE: no '?' before querystring
    return qstr;
}

Tenants.prototype.getquerystringCreate = function () {
    var form     = document.forms['f1'];
    var word = form.word.value;
    qstr = 'w=' + escape(word);  // NOTE: no '?' before querystring
    return qstr;
}

function _gel(id){
  return Document.getElementById(id);
}

var tenants = new Tenants();
