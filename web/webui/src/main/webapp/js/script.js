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
    var end = queryString.indexOf('&');
    if (end == -1)
      email = (email_start != -1) ? queryString.substring(email_start + 6)
          : null;
    else
      email = (email_start != -1) ? queryString.substring(email_start + 6, end)
          : null;

    if (uuid_start != -1)
      uuid = queryString.substring(uuid_start + 3);

    if (email != null && email != "")
      _gel('email').value = email;

    if (uuid != null && uuid != "")
      _gel('confirmation-id').value = uuid;
  }

}


Tenants.prototype.doLogin = function() {
   var tname = _gel("workspace").value;
   var login = _gel("email").value;
   var pass = _gel("password").value;
   var redirect = location.protocol + '//' + tname + '.' + location.hostname;
   redirect += '/portal/login?username=';
   redirect += login;
   redirect += '&password=';
   redirect += pass;
   redirect += '&initialURI=/portal/intranet/welcome';
   window.location = redirect;
}


/* Sending request */
Tenants.prototype.doSingupRequest = function() {
  var url = tenantServicePath + "/signup";
  _gel("t_submit").value = "Wait..";
  _gel("t_submit").disabled = true;
  tenants.xmlhttpPost(url, tenants.handleSignupResponse,
  tenants.getquerystringSignup);

}

/* Sending request */
Tenants.prototype.doCreationRequest = function() {
  var url = tenantServicePath + "/create";
  
   if (_gel("password").value.length <6) {
    _gel("messageString").innerHTML = "<div class=\"Ok\">Password must consist of at least 6 characters.</div>";
    return;
    } 
      
    if (_gel("first_name").value.length == 0) {
    _gel("messageString").innerHTML = "<div class=\"Ok\">Please, indicate your First name.</div>";
    return;
    }
    
    if (_gel("last_name").value.length == 0) {
    _gel("messageString").innerHTML = "<div class=\"Ok\">Please, indicate your Last name.</div>";
    return;
    }
    
     if (_gel("company").value.length == 0) {
    _gel("messageString").innerHTML = "<div class=\"Ok\">Please, indicate your company name.</div>";
    return;
    }
  

  if (_gel("password").value != _gel("password2").value) {
    _gel("messageString").innerHTML = "<div class=\"Ok\">Passwords does not match.</div>";
    return;
  }

  _gel("t_submit").value = "Wait..";
  _gel("t_submit").disabled = true;
  tenants.xmlhttpPost(url, tenants.handleCreationResponse,
      tenants.getquerystringCreate);

}

/* Sending request */
Tenants.prototype.doJoinRequest = function() {
  var url = tenantServicePath + "/join";
  
  if (_gel("first_name").value.length == 0) {
    _gel("messageString").innerHTML = "<div class=\"Ok\">Please, indicate your First name.</div>";
    return;
    }
    
    if (_gel("last_name").value.length == 0) {
    _gel("messageString").innerHTML = "<div class=\"Ok\">Please, indicate your Last name.</div>";
    return;
    }
  
  
   if (_gel("password").value.length <6) {
    _gel("messageString").innerHTML = "<div class=\"Ok\">Password must consist of at least 6 characters.</div>";
    return;
    }
    
  if (_gel("password").value != _gel("password2").value) {
    _gel("messageString").innerHTML = "<div class=\"Ok\">Passwords does not match.</div>";
    return;
  }

  _gel("t_submit").value = "Wait..";
  _gel("t_submit").disabled = true;
  tenants.xmlhttpPost(url, tenants.handleJoinResponse,
      tenants.getquerystringJoin);
}

Tenants.prototype.handleSignupResponse = function(resp) {

  if (resp == "") {
    // window.location = prefixUrl + "/cloud/signup-done.html";
    sendDataToLoopfuse({
      "email" : _gel('email').value,
      // hidden LoopFuse fields
      "formid" : _gel('formid').value,
      "cid" : _gel('cid').value
    }, function() {
      window.location = prefixUrl + "/cloud/signup-done.html";
    });
  } else {
    _gel("messageString").innerHTML = resp;
  }
  _gel("t_submit").disabled = false;
  _gel("t_submit").value = "Sign Up";
}

Tenants.prototype.handleCreationResponse = function(resp) {

  if (resp == "") {
    // window.location = prefixUrl + "/cloud/registration-done.html";
    sendDataToLoopfuse({
      "email" : _gel('email').value,
      "first_name" : _gel('first_name').value,
      "last_name" : _gel('last_name').value,
      "company" : _gel('company').value,
      "phone_work" : _gel('phone_work').value,
      // hidden LoopFuse fields
      "formid" : _gel('formid').value,
      "cid" : _gel('cid').value
    }, function() {
      window.location = prefixUrl + "/cloud/registration-done.html";
    });
  } else {
    _gel("messageString").innerHTML = resp;
  }
  _gel("t_submit").disabled = false;
  _gel("t_submit").value = "Create";
}

Tenants.prototype.handleJoinResponse = function(resp) {

  if (resp == "") {
    // window.location = prefixUrl + "/cloud/join-done.html";
    sendDataToLoopfuse({
      "email" : _gel('email').value,
      "first_name" : _gel('first_name').value,
      "last_name" : _gel('last_name').value,
      // hidden LoopFuse fields
      "formid" : _gel('formid').value,
      "cid" : _gel('cid').value
    }, function() {
      window.location = prefixUrl + "/cloud/join-done.html";
    });
  } else {
    _gel("messageString").innerHTML = resp;
  }

  _gel("t_submit").disabled = false;
  _gel("t_submit").value = "Sign In";

}

Tenants.prototype.xmlhttpPost = function(strURL, handler, paramsMapper) {
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
  self.xmlHttpReq.setRequestHeader('Content-Type',
      'application/x-www-form-urlencoded');
  self.xmlHttpReq.onreadystatechange = function() {
    if (self.xmlHttpReq.readyState == 4) {
      handler(self.xmlHttpReq.responseText);
    }
  }
  self.xmlHttpReq.send(paramsMapper());
}

Tenants.prototype.getquerystringSignup = function() {
  qstr = 'user-mail=' + _gel('email').value;
  return qstr;
}

Tenants.prototype.getquerystringJoin = function() {
  qstr = 'user-mail=' + _gel('email').value;
  qstr += '&first-name=' + _gel('first_name').value;
  qstr += '&last-name=' + _gel('last_name').value;
  qstr += '&password=' + _gel('password').value;
  return qstr;
}

Tenants.prototype.getquerystringCreate = function() {
  qstr = 'user-mail=' + _gel('email').value;
  qstr += '&first-name=' + _gel('first_name').value;
  qstr += '&last-name=' + _gel('last_name').value;
  qstr += '&password=' + _gel('password').value;
  qstr += '&phone=' + _gel('phone_work').value;
  qstr += '&company-name=' + _gel('company').value;
  qstr += '&confirmation-id=' + _gel('confirmation-id').value;
  return qstr;
}

function _gel(id) {
  return document.getElementById(id);
}

function onlyNumbers(evt) {

  var charCode = (evt.which) ? evt.which : event.keyCode;

  if (charCode > 31
      && ((charCode < 48 || charCode > 57) && charCode != 45 && charCode != 40
          && charCode != 41 && charCode != 43))
    return false;

  return true;

}

function sendDataToLoopfuse(data, afterSubmitCallback) {

  var loopfuseOutputIframeId = "loopfuseOutput";
  var loopfuseOutputIframeName = "loopfuseOutput";
  var loopfuseFormId = "loopfuseForm";
  var loopfuseFormName = "cloud-workspaces-profile";

  if (jQuery && document.getElementById(loopfuseOutputIframeId)) {
    jQuery('#' + loopfuseFormId).remove();
    jQuery('#' + loopfuseOutputIframeId).attr('src', ""); // clear iframe

    jQuery('body').append(jQuery('<form/>', {
      id : loopfuseFormId,
      name : loopfuseFormName,
      method : 'POST',
      action : loopfuseURL,
      target : loopfuseOutputIframeName
    }));

    for ( var i in data) {
      jQuery('#' + loopfuseFormId).append(jQuery('<input/>', {
        type : 'hidden',
        name : i,
        value : data[i]
      }));
    }

    jQuery('#' + loopfuseFormId).submit();

    var i = 200; // set limited iterations - interrupt after the 20
    // seconds
    var afterSubmitHandler = window.setInterval(function() {
      if (!(i--) || isLoopfuseResponseReceived(loopfuseOutputIframeId)) {
        window.clearInterval(afterSubmitHandler);
        if (afterSubmitCallback) {
          afterSubmitCallback();
        }

        jQuery('#' + loopfuseOutputIframeId).attr('src', ""); // clear
        // iframe
      }
    }, 100);
  }
}

function isLoopfuseResponseReceived(iframeId) {
  try {
    return !document.getElementById(iframeId)
        || document.getElementById(iframeId).contentWindow.location.href != "about:blank";
  } catch (e) {
    // check for permission of
    return true;
  }
}

var tenants = new Tenants();
