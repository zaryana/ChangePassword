function Tenants() {
}

var prefixUrl = location.protocol + '//' + location.hostname;
var queryString = location.search;
var loopfuseURL = "http://lfov.net/webrecorder/f";

if (location.port) {
	prefixUrl += ':' + location.port;
}
var user;
var auth = null;

/** Getting list chain */
Tenants.prototype.init = function() {
	accessUrl = prefixUrl + '/rest/cloud-admin';
	accessSecureUrl = prefixUrl + '/rest/private/cloud-admin';
	tenantServicePath = accessUrl + "/public-tenant-service";
	infoServicePath = accessSecureUrl + "/info-service/";
	refreshInterval = 10000;
	is_chrome = (navigator.userAgent.toLowerCase().indexOf('chrome') > -1 || navigator.userAgent
			.toLowerCase().indexOf('safari') > -1);

        if (queryString != null && queryString != "") {
        var email_start = queryString.indexOf('email=');
        var uuid_start = queryString.indexOf('id=');
        var end  = queryString.indexOf('&');
        if (end == -1)
	   email = (email_start != -1) ? queryString.substring(email_start+6) : null;
        else
           email = (email_start != -1) ? queryString.substring(email_start+6, end) : null;
     
        if (uuid_start != -1)
	   uuid = queryString.substring(uuid_start+3);

       if (email != null && email != "")
         _gel('email').value = email;

       if (uuid != null && uuid != "")
         _gel('confirmation-id').value = uuid;
      }
	
}


/* Sending request */
Tenants.prototype.doSingupRequest = function() {
	var url = tenantServicePath + "/signup";
	_gel("t_submit").value = "Wait..";
	_gel("t_submit").disabled = true;
         tenants.xmlhttpPost(url,tenants.handleSignupResponse,tenants.getquerystringSignup);

}

/* Sending request */
Tenants.prototype.doCreationRequest = function() {
	var url = tenantServicePath + "/create";
	
	if (_gel("password").value != _gel("password2").value){
	   _gel("messageString").innerHTML = "<div class=\"Ok\">Passwords does not match.</div>";
	   return;
	}
	
	_gel("t_submit").value = "Wait..";
	_gel("t_submit").disabled = true;
        tenants.xmlhttpPost(url, tenants.handleCreationResponse, tenants.getquerystringCreate);

}

/* Sending request */
Tenants.prototype.doJoinRequest = function() {
	var url = tenantServicePath + "/join";
	if (_gel("password").value != _gel("password2").value){
	   _gel("messageString").innerHTML = "<div class=\"Ok\">Passwords does not match.</div>";
	   return;
	}

	_gel("t_submit").value = "Wait..";
	_gel("t_submit").disabled = true;
        tenants.xmlhttpPost(url, tenants.handleJoinResponse, tenants.getquerystringJoin);
}


Tenants.prototype.handleSignupResponse = function(resp) {

	if (resp == "") {
		//_gel("messageString").innerHTML = "<div class=\"Ok\">Your signup request sent successfully! Check your email for instructions.</div>";
		window.location = prefixUrl + "/cloud/signup-done.html";
	} else {
		_gel("messageString").innerHTML = resp;
	}
	_gel("t_submit").disabled = false;
	_gel("t_submit").value = "Sign Up";
}


Tenants.prototype.handleCreationResponse = function(resp) {

	if (resp == "") {
		//_gel("messageString").innerHTML = "<div class=\"Ok\">Intranet creation request sent successfully! Check your email for instructions.</div>";
		window.location = prefixUrl + "/cloud/registration-done.html";
	} else {
		_gel("messageString").innerHTML = resp;
	}
	_gel("t_submit").disabled = false;
	_gel("t_submit").value = "Create";
}


Tenants.prototype.handleLoopfuseResponse = function(resp) {
//None there;
}


Tenants.prototype.handleJoinResponse = function(resp) {

	if (resp == "") {
		//_gel("messageString").innerHTML = "<div class=\"Ok\">Tenant join successfull! Check your email for instructions.</div>";
		window.location = prefixUrl + "/cloud/join-done.html";
	} else {
		_gel("messageString").innerHTML = resp;
	}

	_gel("t_submit").disabled = false;
	_gel("t_submit").value = "Sign In";

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
    self.xmlHttpReq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    self.xmlHttpReq.onreadystatechange = function() {
        if (self.xmlHttpReq.readyState == 4) {
            handler(self.xmlHttpReq.responseText);
        }
    }
    self.xmlHttpReq.send(paramsMapper());
}

Tenants.prototype.getquerystringSignup = function () {
    qstr = 'user-mail=' + _gel('email').value; 
    return qstr;
}

Tenants.prototype.getLoopfuseQuerystringSignup = function () {
    qstr = 'email=' + _gel('email').value; 
    qstr += '&formid=' + _gel('formid').value;
    qstr += '&cid=' + _gel('cid').value;
    return qstr;
}

Tenants.prototype.getquerystringJoin = function () {
    qstr = 'user-mail=' + _gel('email').value; 
    qstr += '&first-name=' + _gel('first_name').value;
    qstr += '&last-name=' + _gel('last_name').value;
    qstr += '&password=' + _gel('password').value;
    return qstr;
}

Tenants.prototype.getLoopfusequerystringJoin = function () {
    qstr = 'email=' + _gel('email').value; 
    qstr += '&first_name=' + _gel('first_name').value;
    qstr += '&last_name=' + _gel('last_name').value;
    qstr += '&formid=' + _gel('formid').value;
    qstr += '&cid=' + _gel('cid').value;

    return qstr;
}


Tenants.prototype.getquerystringCreate = function () {
    qstr = 'user-mail=' + _gel('email').value;
    qstr += '&first-name=' + _gel('first_name').value;
    qstr += '&last-name=' + _gel('last_name').value;
    qstr += '&password=' + _gel('password').value;
    qstr += '&phone=' + _gel('phone_work').value;
    qstr += '&company-name=' + _gel('company').value;
    qstr += '&confirmation-id=' + _gel('confirmation-id').value;
    return qstr;
}


Tenants.prototype.getLoopfusequerystringCreate = function () {
    qstr = 'email=' + _gel('email').value;
    qstr += '&first_name=' + _gel('first_name').value;
    qstr += '&last_name=' + _gel('last_name').value;
    qstr += '&phone_work=' + _gel('phone_work').value;
    qstr += '&company=' + _gel('company').value;
    qstr += '&formid=' + _gel('formid').value;
    qstr += '&cid=' + _gel('cid').value;

    return qstr;
}

function _gel(id){
  return document.getElementById(id);
}

function onlyNumbers(evt)
{
	
	var charCode = (evt.which) ? evt.which : event.keyCode;

	if (charCode > 31 && ((charCode < 48 || charCode > 57) && charCode !=45 
	      && charCode !=40 && charCode !=41 && charCode !=43))
		return false;

	return true;

}

var tenants = new Tenants();
