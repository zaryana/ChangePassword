/*
 * Copyright (C) 2011 eXo Platform SAS.
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

/*  Init function */

Tenants.prototype.init = function() {
  accessUrl = prefixUrl + '/rest/cloud-admin';
  accessSecureUrl = prefixUrl + '/rest/private/cloud-admin';
  tenantServicePath = accessUrl + "/public-tenant-service";
  infoServicePath = accessSecureUrl + "/info-service/";
  refreshInterval = 10000;
  is_chrome = (navigator.userAgent.toLowerCase().indexOf('chrome') > -1 || navigator.userAgent
      .toLowerCase().indexOf('safari') > -1);
  }
  
  Tenants.prototype.initRegistrationPage = function() {
  tenants.init();
  if (queryString != null && queryString != "") {
  var email_start = queryString.indexOf('email=');
  var uuid_start = queryString.indexOf('id=');
  var end = queryString.indexOf('&');
  var uuid = "";
  if (end == -1)
    email = (email_start != -1) ? queryString.substring(email_start + 6) : null;
  else
    email = (email_start != -1) ? queryString.substring(email_start + 6, end) : null;
  if (uuid_start != -1)
    uuid = queryString.substring(uuid_start + 3);
  if (email != null && email != "") {
  var split = email.split('@');
  var prefix = split[0];
   _gel('email').value = email;
   _gel('username').value = prefix;
   if (prefix.indexOf('.') > -1){
     var splittedName = prefix.split('.');
     _gel('first_name').value = splittedName[0];
     _gel('last_name').value = splittedName[1];
   } else {
     _gel('first_name').value = prefix;
   }
  } else {
     _gel("messageString").innerHTML = "<div class=\"Ok\">Application error: email is not found. Please contact support.</div>";
  }
  if (uuid != "")
   _gel('confirmation-id').value = uuid;
   else
   _gel("messageString").innerHTML = "<div class=\"Ok\">Application error: damaged form confirmation-id. Please contact support.</div>"
  }
 }

  Tenants.prototype.initJoinPage = function() {
    tenants.init();
    if (queryString != null && queryString != "") {
    var email_start = queryString.indexOf('email=');
    var uuid_start = queryString.indexOf('id=');
    var end = queryString.indexOf('&');
    if (end == -1)
      email = (email_start != -1) ? queryString.substring(email_start + 6) : null;
    else
      email = (email_start != -1) ? queryString.substring(email_start + 6, end) : null;
    if (email != null && email != "") {
    var split = email.split('@');
     _gel('email').value = email;
     var prefix = split[0];
     _gel('username').value = prefix;
     if (prefix.indexOf('.') > -1){
     var splittedName = prefix.split('.');
     _gel('first_name').value = splittedName[0];
     _gel('last_name').value = splittedName[1];
     } else {
     _gel('first_name').value = prefix;
     }
    _gel('workspace').value = split[1].substring(0, split[1].indexOf('.'));
    }else{
    _gel("messageString").innerHTML = "<div class=\"Ok\">Application error: email is not found. Please contact support.</div>";
    }
    
   }
 }




/* Login redirect */
Tenants.prototype.doLogin = function() {
   var tname = _gel("workspace").value;
   var login = _gel("email").value;
   var pass = _gel("password").value;
   
   if (tname.length == 0|| login.length == 0 || pass.length == 0){
     _gel("messageString").innerHTML = "<div class=\"Ok\">Please fill all form fields.</div>";
     return;
   }
   var redirect = location.protocol + '//' + tname + '.' + location.hostname;
   redirect += '/portal/login?username=';
   redirect += login;
   redirect += '&password=';
   redirect += pass;
   redirect += '&initialURI=/portal/intranet/welcome';
   window.location = redirect;
}


/* Sending signup request */
Tenants.prototype.doSingupRequest = function() {
  var url = tenantServicePath + "/signup";
  
  if (_gel("email").value.length == 0){
    _gel("messageString").innerHTML = "<div class=\"Ok\">Please, indicate your email.</div>";
    return;
    }
  
  _gel("t_submit").value = "Wait..";
  _gel("t_submit").disabled = true;
  tenants.xmlhttpPost(url, tenants.handleSignupResponse,
  tenants.getquerystringSignup);

}

/* Sending creation request */
Tenants.prototype.doCreationRequest = function() {
  var url = tenantServicePath + "/create";
  
  if (_gel("email").value.length == 0){
    _gel("messageString").innerHTML = "<div class=\"Ok\">Cannot process request, email is not set.</div>";
    return;
    }
    
    if (_gel("confirmation-id").value.length == 0){
    _gel("messageString").innerHTML = "<div class=\"Ok\">Cannot process request, confirmation ID is not set.</div>";
    return;
    }
  
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
    
     if (_gel("phone_work").value.length == 0) {
    _gel("messageString").innerHTML = "<div class=\"Ok\">Please, indicate your company phone.</div>";
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

/* Sending join request */
Tenants.prototype.doJoinRequest = function() {
  var url = tenantServicePath + "/join";
  
  if (_gel("email").value.length == 0){
    _gel("messageString").innerHTML = "<div class=\"Ok\">Cannot process request, email is not set.</div>";
    return;
    }
    
    if (_gel("workspace").value.length == 0){
    _gel("messageString").innerHTML = "<div class=\"Ok\">Cannot process request, workspace is not set.</div>";
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

/*  Handle signup response */
Tenants.prototype.handleSignupResponse = function(resp) {

  if (resp == "") {
    sendDataToLoopfuse({
      "email" : _gel('email').value,
      // hidden LoopFuse fields
      "formid" : _gel('formid').value,
      "cid" : _gel('cid').value
    }, function() {
      window.location = prefixUrl + "/signup-done.html";
    });
  } else {
    _gel("messageString").innerHTML = resp;
  }
  _gel("t_submit").disabled = false;
  _gel("t_submit").value = "Sign Up";
}


/*  Handle creation response */
Tenants.prototype.handleCreationResponse = function(resp) {

  if (resp == "") {
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
      window.location = prefixUrl + "/registration-done.html";
    });
  } else {
    _gel("messageString").innerHTML = resp;
  }
  _gel("t_submit").disabled = false;
  _gel("t_submit").value = "Create";
}

/*  Handle join response */
Tenants.prototype.handleJoinResponse = function(resp) {

  if (resp == "") {
    sendDataToLoopfuse({
      "email" : _gel('email').value,
      "first_name" : _gel('first_name').value,
      "last_name" : _gel('last_name').value,
      // hidden LoopFuse fields
      "formid" : _gel('formid').value,
      "cid" : _gel('cid').value
    }, function() {
      window.location = prefixUrl + "/join-done.html";
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
