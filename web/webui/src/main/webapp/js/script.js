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

var CONTACT_US_CONTAINER_ID = "ContactUsContainer";
var MASK_LAYER_ID = "MaskLayer";


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
  tenantSecureServicePath = accessSecureUrl + "/public-tenant-service";
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
  var uuid = "";
  var email; // = (email_start != -1) ? queryString.substring(email_start + 6, uuid_start-1) : null;
  uuid =  (uuid_start != -1) ? queryString.substring(uuid_start + 3) : null;
  var checkURL = tenantServicePath + "/uuid/" + uuid;
  $.ajax({
  url: checkURL,
   success: function(data){
 //       alert(data);
  email = data;
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
  },
  dataType: 'text',
  error: function(request, status, error)
   {
    email = (email_start != -1) ? queryString.substring(email_start + 6, uuid_start-1) : null; 
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
       _gel("messageString").innerHTML = "<div class=\"Ok\">Warning! You are using broken link to the Registration Page. Please sign up again.</div>";
      }
       if (uuid != "")
        _gel('confirmation-id').value = uuid;
       else
        _gel("messageString").innerHTML = "<div class=\"Ok\">Application error: damaged form confirmation-id. Please contact support.</div>"

   }
  });
 }
}

Tenants.prototype.initDonePage = function() {
   tenants.init();
   var checkURL = tenantServicePath + "/status/" + tName;
   $.ajax({
     url: checkURL,
     success: function(data){
       var search = "ONLINE";
       if (data.substring(0, search.length) === search){
          var checkURL = tenantServicePath + "/isuserexist/" + tName + "/" + split[0];
          $.ajax({
             url: checkURL,
             success: function(data){
             var search = "TRUE";
             if (data.substring(0, search.length) === search){
                   _gel("sign_link").innerHTML = "You can now <span style=\"color:#19BBE7;\"><u>sign-in</u></span> the "+ tName+ "  Workspace.";
                   _gel("sign_link").setAttribute("href","/signin.jsp?email=" + email);
              }
              else {
//                    _gel("sign_link").innerHTML="We cannot add you to the " + tName +" Workspace at the moment. Please check that the Workspace hasn't reached its user limit. The Workspace administrator has been notified of your attempt to join.";
                    _gel("sign_link").innerHTML="<span style=\"color:#b81919;\">We cannot add you to the " + tName +" Workspace at the moment. The Workspace administrator has been notified of your attempt to join.</span>";
               }
              },
             error: function (request, status, error) {
               _gel("sign_link").innerHTML="The " + tName + " Workspace is beind created.<br/> We will inform you by email when ready.";
              },
           dataType: 'text'});
       
       }
       else {
        _gel("sign_link").innerHTML="The " + tName + " Workspace is beind created.<br/> We will inform you by email when ready.";
       }
    },
    error: function (request, status, error) {
      _gel("sign_link").innerHTML="The " + tName + " Workspace is beind created.<br/> We will inform you by email when ready.";
    },
  dataType: 'text'});
}



/*Those methods written for pre-moderating tenants on private demo*/
 Tenants.prototype.initValidationPage = function() {
      tenants.init();
      if (auth){
      tenants.showValidatioList(true);
      } else {
      tenants.showValidationForm();
      }
}

  Tenants.prototype.showValidationList = function(isClearStatus) {
      var form = _gel("validationTable");
      form.style.display="none";
      if (isClearStatus)
      _gel("messageString").innerHTML="";
      var checkURL = tenantSecureServicePath + "/requests/";
      var resp;
      $.ajax({
 		url: checkURL, 
 		beforeSend : function(req) {
 	        req.setRequestHeader('Authorization', 'Basic ' + auth);
 	    },
        success: function(data){
    	  resp = data;
    	  _gel("ListTable").style.display="table";
         var table = _gel("ListTable");
         //Clear table
         while (table.rows.length > 1){
         table.deleteRow(-1);
         }

         for (account in resp)
          {
           if (resp.propertyIsEnumerable(account))
           {
//              alert(account);              alert(resp[account][0]);         alert(resp[account][1]);
              var row = table.insertRow(-1);
              //Tname
              var cell0 = row.insertCell(0);
              cell0.className="MyFieldLeft";
              cell0.innerHTML= "<b>" +  resp[account][0] + "</b>";
              //Name + Email
              var cell1 = row.insertCell(1);
              cell1.className="MyFieldLeft";
              cell1.innerHTML=resp[account][2] + " &lt;" + resp[account][1] + "&gt;";
              //Date
              var cell2 = row.insertCell(2);
              cell2.className="MyFieldLeft";
              var date = new Date (parseFloat(account.substring(account.indexOf("_")+1)));
              //toUTCString is too long for table
              var month = date.getUTCMonth()+1;
              cell2.innerHTML=date.getUTCDate() + "/" + month + "/" + date.getFullYear() + " " + date.getUTCHours() + ":" + date.getUTCMinutes() + " UTC";
              //Company
              var cell3 = row.insertCell(3);
              cell3.className="MyFieldLeft";
              cell3.innerHTML=resp[account][3];
              //Phone
              var cell4 = row.insertCell(4);
              cell4.className="MyFieldLeft";
              cell4.innerHTML="<u>" + resp[account][4]+ "</u>";
              //Action
              var cell5 = row.insertCell(5);
              cell5.className="MyFieldLeft";
              cell5.innerHTML='<a href="#" onClick="tenants.validationAction(\''+tenantSecureServicePath +'/validate/accept/'+account +'\');">Accept</a>&nbsp;|&nbsp;<a href="#" onClick="tenants.validationAction(\''+tenantSecureServicePath+'/validate/refuse/'+account+'\');">Reject</a>&nbsp;|&nbsp;<a href="#" onClick="tenants.validationAction(\''+tenantSecureServicePath +'/validate/blacklist/'+account +'\');">Blacklist</a>&nbsp;';
            }
          }
          var row1 = table.insertRow(-1);
          var cell_s = row1.insertCell(0);
          cell_s.colSpan="6";
          cell_s.className="MyField";
          cell_s.innerHTML="<a href=\"javascript:void(0);\" onClick=\"tenants.showValidationList(true);\">Refresh</a>";
        }, 
    	error: function (request, status, error) {
    		if (request.responseText.indexOf("HTTP Status 401") > -1){
    	    	  tenants.showValidationForm(true);
    	    	  _gel("messageString").innerHTML = "<div class=\"Ok\">Wrong workspaces manager credentials.</div>";
    	    	  return;
    		} else
    	    _gel("messageString").innerHTML = "<div class=\"Ok\">" + request.responseText + "</div>";
    	},
    	dataType: 'json'});
 }
 
    Tenants.prototype.validationAction = function(url){
    	$.ajax({
    	url: url, 
    	beforeSend : function(req) {
 	        req.setRequestHeader('Authorization', 'Basic ' + auth);
 	    },
    	success: function(data){
          if (data == "") {
          _gel("messageString").innerHTML = "<div class=\"Ok\"><span style=\"color:blue;\">Action successfull.</span></div>";
           tenants.showValidationList(false); 
          }
          else
           _gel("messageString").innerHTML = "<div class=\"Ok\">" + data + "</div>";
       }, 
       error: function (request, status, error) {
           _gel("messageString").innerHTML = "<div class=\"Ok\">" + request.responseText + "</div>";
       },
       dataType: 'text'});
    }

   Tenants.prototype.showValidationForm = function() {
    var table = _gel("ListTable"); 
    table.style.display="none";
    _gel("validationTable").style.display="block";
   }
   
   Tenants.prototype.validationLogin = function(){
     auth = encode64(_gel("v_username").value + ":" + _gel("v_pass").value);
     tenants.showValidationList(true);
   }


  Tenants.prototype.initJoinPage = function() {
    tenants.init();
    var email;
    if (queryString != null && queryString != "") {
    var rfid_start = queryString.indexOf('rfid=');
     rfid = (rfid_start != -1) ? queryString.substring(rfid_start + 5) : null;
     if (rfid == null){
     _gel("joinForm").innerHTML="<br><center><a class=\"BackIcon\" href=\"/index.jsp\">Home</a></center>";
     _gel("messageString").innerHTML = "<div class=\"Ok\">Sorry, your registration link has expired. Please <a class=\"TenantFormMsg\" href=\"index.jsp\"><u>sign up</u></a> again.</div>";
     return;
     }
     
     var checkURL = tenantServicePath + "/uuid/" + rfid;
     $.ajax({
       url: checkURL,
       success: function(data){
//       alert(data);
         email = data;
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
        _gel('rfid').value = rfid;
         }else{
        _gel("messageString").innerHTML = "<div class=\"Ok\">Application error: email is not found. Please contact support.</div>";
         }
       },
        error: function (request, status, error) {
        _gel("joinForm").innerHTML="<br><center><a class=\"BackIcon\" href=\"/index.jsp\">Home</a></center>";
        _gel("messageString").innerHTML = "<div class=\"Ok\">" + request.responseText + "</div>";
      },
     dataType: 'text'});
   }
 }


Tenants.prototype.initSignInPage = function() {
 tenants.init();
  if (queryString != null && queryString != "") {
  var email_start = queryString.indexOf('email=');
  email = (email_start != -1) ? queryString.substring(email_start + 6) : null;
   if (email != null && email != "") {
   _gel("email").value = email;
    var split = email.split('@');
   _gel('workspace').value = split[1].substring(0, split[1].indexOf('.'));
  }
 }
}

Tenants.prototype.initChange = function() {
  tenants.init();
  if (queryString != null && queryString != "") {
  var id_start = queryString.indexOf('id=');
  id = (id_start != -1) ? queryString.substring(id_start + 3) : null;
  if (id != null && id != "") {
   _gel("id").value = id;
   } else {
   _gel("messageString").innerHTML = "<div class=\"Ok\">Your link to this page is broken. Please, re-request it.</div>";
   }
  } else {
  _gel("messageString").innerHTML = "<div class=\"Ok\">Your link to this page is broken. Please, re-request it.</div>";   
  }
}

Tenants.prototype.initResumingPage = function() {
 tenants.init();
 if (queryString != null && queryString != "") {
   var email_start = queryString.indexOf('email=');
   email = (email_start != -1) ? queryString.substring(email_start + 6) : null;
   var split = email.split('@');
   var workspace = split[1].substring(0, split[1].indexOf('.'));
   _gel("li1").innerHTML = "If you are already a member of the " + workspace + " Workspace, please <a href=\"/signin.jsp?email=" + email + "\">try again</a> in few miunutes.";
   _gel("li2").innerHTML = "If you are trying to join " + workspace +  " Workspace, check you mail box for an invitation";
  }
}


/* Login redirect */
Tenants.prototype.doLogin = function() {
   var tname = _gel("workspace").value;
   var login = _gel("email").value;
   var pass = _gel("password").value;
   
   jQuery.validator.setDefaults(
   {
    errorPlacement: function(error, element)
      {
        error.appendTo(element.next());
      },
     });
    var valid = $("#signinForm").valid();
    if (!valid) return;
   
   var username = login.substring(0, login.indexOf('@'));
   var redirect = location.protocol + '//' + tname + '.' + location.hostname;
   redirect += '/portal/login?username=';
   redirect += username;
   redirect += '&password=';
   redirect += pass;
   redirect += '&initialURI=/portal/intranet/welcome';
   var checkURL = tenantServicePath + "/status/" + tname;
   var search = "ONLINE";
   $.ajax({
	url: checkURL, 
	success: function(data){
	   if (data.substring(0, search.length) === search)
	    window.location = redirect;
	   else
   	   _gel("messageString").innerHTML = "<div class=\"Ok\">The workspace " + tname + " does not exist or unreachable.</div>";
   }, 
   error: function (request, status, error) {
       _gel("messageString").innerHTML = "<div class=\"Ok\">" + request.responseText + "</div>";
   },
   dataType: 'text'});
}


/* Sending signup request */
Tenants.prototype.doSingupRequest = function() {
  var url = tenantServicePath + "/signup";
  
  if (_gel("email").value.length == 0){
    _gel("messageString").innerHTML = "<div class=\"Ok\">Please, indicate your email.</div>";
    return;
    }
    
    if (_gel("email").value.indexOf('%') > -1){
     _gel("messageString").innerHTML = "<div class=\"Ok\">Your email contains disallowed characters.</div>";
    return;
    }
  
  _gel("t_submit").value = "Wait...";
  _gel("t_submit").disabled = true;
  tenants.xmlhttpPost(url, tenants.handleSignupResponse,
  tenants.getquerystringSignup);

}

/* Sending creation request */
Tenants.prototype.doCreationRequest = function() {
  var url = tenantServicePath + "/create";
    
    if (_gel("confirmation-id").value.length == 0){
    _gel("messageString").innerHTML = "<div class=\"Ok\">Cannot process request, confirmation ID is not set.</div>";
    return;
    }
  
    jQuery.validator.setDefaults(
   {
    errorPlacement: function(error, element)
      {
        error.appendTo(element.next());
      },
     });
     
 
     $("#registrationForm").validate({
       rules: {
         password: {
           required: true,
           minlength: 6,
       },
       password2: {
           required: true,
           minlength: 6,
           equalTo: "#password"
       }
    }
   });

    var valid = $("#registrationForm").valid();
    if (!valid) return;


  _gel("t_submit").value = "Wait...";
  _gel("t_submit").disabled = true;
  tenants.xmlhttpPost(url, tenants.handleCreationResponse,
      tenants.getquerystringCreate);

}

/* Sending join request */
Tenants.prototype.doJoinRequest = function() {
  var url = tenantServicePath + "/join";
  
  jQuery.validator.setDefaults(
   {
    errorPlacement: function(error, element)
      {
        error.appendTo(element.next());
      },
     });
     
 
     $("#joinForm").validate({
       rules: {
         password: {
           required: true,
           minlength: 6,
       },
       password2: {
           required: true,
           minlength: 6,
           equalTo: "#password"
       }
    }
   });
   
   var valid = $("#joinForm").valid();
    if (!valid) return;


  _gel("t_submit").value = "Wait...";
  _gel("t_submit").disabled = true;
  tenants.xmlhttpPost(url, tenants.handleJoinResponse,
      tenants.getquerystringJoin);
}


Tenants.prototype.doContactRequest = function() {
    var url = tenantServicePath + "/contactus";
    var valid = $("#mycontactForm").valid();
    if (!valid) return;
    tenants.xmlhttpPost(url, tenants.handleContactResponse,
     tenants.getquerystringContactUs);

//    document.getElementById(CONTACT_US_CONTAINER_ID).style.display = "none";
//    document.getElementById(MASK_LAYER_ID).style.display = "none";
      _gel("submitButton").value = "Wait...";
      _gel("cancelButton").disabled = true;
}

Tenants.prototype.doChange = function() {

  _gel("submitButton").value = "Wait...";
  var url = tenantServicePath + "/passconfirm";
  jQuery.validator.setDefaults(
  {
   errorPlacement: function(error, element)
     {
      error.appendTo(element.next());
     },
   });

   $("#changeForm").validate({
    rules: {
      password: {
        required: true,
        minlength: 6,
      },
      password2: {
        required: true,
        minlength: 6,
        equalTo: "#password"
      }
     }
    });
   var valid = $("#changeForm").valid();
   if (!valid) return;
   
   var fdata = "uuid="+_gel("id").value + "&password=" +_gel("password").value.trim();
   $.ajax({
     url: url,
     type: 'POST',
     data: fdata,
     dataType: 'text',
     processData: false,
     success: function(data){
      _gel("submitButton").value = "Submit";
      _gel("messageString").innerHTML = "<div class=\"Ok\"><span style=\"color:#19BBE7;\">Success. You can now <a href='/signin.jsp'>login</a> with your new password.</span></div>";
     },
     error: function (request, status, error) {
      _gel("messageString").innerHTML = "<div class=\"Ok\">" + request.responseText + "</div>";
     _gel("submitButton").value = "Submit";
    }});
}



Tenants.prototype.doReset = function() {
    _gel("submitButton").value = "Wait...";
    var url = tenantServicePath + "/passrestore";
    jQuery.validator.setDefaults(
    {
      errorPlacement: function(error, element)
      {
       error.appendTo(element.next());
      },
    });
    
     $("#resetForm").validate({
       rules: {
         email: {
         required: true,
       }
      },
      });
    var valid = $("#resetForm").valid();
    if (!valid){_gel("submitButton").value = "Change my password"; return;}
    var checkURL = url + "/" + _gel("email").value;
    
    $.ajax({
    url: checkURL,
    success: function(data){
         _gel("submitButton").value = "Change my password";
         _gel("submitButton").style.display = "none"; 
        _gel("messageString").innerHTML = "<div class=\"Ok\"><span style=\"color:#19BBE7;\">Request completed, check your email for instructions.</span></div>";
      },
     error: function (request, status, error) {
       _gel("messageString").innerHTML = "<div class=\"Ok\">" + request.responseText + "</div>";
       _gel("submitButton").value = "Change my password";
       },
      dataType: 'text'});                                                                                                                                                                                                                                            _gel("submitButton").value = "Wait...";                                                                                                                                                         
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
      window.location = prefixUrl + "/signup-done.jsp";
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
      window.location = prefixUrl + "/registration-done.jsp";
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
      window.location = prefixUrl + "/join-done.jsp#"+_gel('email').value;
    });
  } else {
    _gel("messageString").innerHTML = resp;
  }

  _gel("t_submit").disabled = false;
  _gel("t_submit").value = "Sign In";

}

Tenants.prototype.handleContactResponse = function(resp) {

	  if (resp == "") {
		  sendDataToLoopfuse({
		      "email" : _gel('email').value,
		      "first_name" : _gel('name').value,
		      "company" : _gel('subject').value,
		      "message" : _gel('ContactUs_Message__c').value,
		      // hidden LoopFuse fields
		      "formid" : _gel('formid').value,
		      "service_source" : _gel('service_source').value,
		      "cid" : _gel('cid').value
		    }, function() {
		     // document.getElementById('Content').innerHTML = "<div class=\"ThanksPages ClearFix\"><h1>Thank you!</h1><p style=\"text-align:center\">Your request has been successfully submitted. We will get back to you soon.</p></div>";
		     window.location = "/contact-us-done.jsp";
		      });
	  } else {
	    _gel(CONTACT_US_CONTAINER_ID).style.display = "none";
	    _gel(MASK_LAYER_ID).style.display = "none";
	    _gel("messageString").innerHTML = resp;
	  }
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
      if (self.xmlHttpReq.status == 309){ //Custom status to handle redirects;
      window.location = self.xmlHttpReq.getResponseHeader("Location");
      return;
    }

      handler(self.xmlHttpReq.responseText);
    }
  }
  self.xmlHttpReq.send(paramsMapper());
}

Tenants.prototype.getquerystringSignup = function() {
  qstr = 'user-mail=' + jQuery.trim(_gel('email').value);
  return qstr;
}

Tenants.prototype.getquerystringJoin = function() {
  qstr = 'user-mail=' + jQuery.trim(_gel('email').value);
  qstr += '&first-name=' + jQuery.trim(_gel('first_name').value);
  qstr += '&last-name=' + jQuery.trim(_gel('last_name').value);
  qstr += '&password=' + jQuery.trim(_gel('password').value);
  qstr += '&rfid=' + jQuery.trim(_gel('rfid').value);
  return encodeURI(qstr);
}

Tenants.prototype.getquerystringCreate = function() {
  qstr = 'user-mail=' + jQuery.trim(_gel('email').value);
  qstr += '&first-name=' + jQuery.trim(_gel('first_name').value);
  qstr += '&last-name=' + jQuery.trim(_gel('last_name').value);
  qstr += '&password=' + jQuery.trim(_gel('password').value);
  qstr += '&phone=' + jQuery.trim(_gel('phone_work').value);
  qstr += '&company-name=' + jQuery.trim(_gel('company').value);
  qstr += '&confirmation-id=' + jQuery.trim(_gel('confirmation-id').value);
  return encodeURI(qstr);
}


Tenants.prototype.getquerystringContactUs = function() {
	  qstr = 'user-mail=' + jQuery.trim(_gel('email').value);
	  qstr += '&first-name=' + jQuery.trim(_gel('name').value);
	  qstr += '&subject=' + jQuery.trim(_gel('subject').value);
	  qstr += '&text=' + jQuery.trim(_gel('ContactUs_Message__c').value);
	  return encodeURI(qstr);
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

 var keyStr = "ABCDEFGHIJKLMNOP" +
              "QRSTUVWXYZabcdef" +
              "ghijklmnopqrstuv" +
              "wxyz0123456789+/" +
              "=";


   function encode64(input) {
    var output = "";
    var chr1, chr2, chr3 = "";
    var enc1, enc2, enc3, enc4 = "";
    var i = 0;

      do {
      chr1 = input.charCodeAt(i++);
      chr2 = input.charCodeAt(i++);
      chr3 = input.charCodeAt(i++);

      enc1 = chr1 >> 2;
      enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
      enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
      enc4 = chr3 & 63;

      if (isNaN(chr2)) {
       enc3 = enc4 = 64;
      } else if (isNaN(chr3)) {
       enc4 = 64;
      }

      output = output +
      keyStr.charAt(enc1) +
      keyStr.charAt(enc2) +
      keyStr.charAt(enc3) +
      keyStr.charAt(enc4);
      chr1 = chr2 = chr3 = "";
      enc1 = enc2 = enc3 = enc4 = "";
      } while (i < input.length);
      
     return output;
   }

var tenants = new Tenants();



/**
 * --------------------- ajax library
 */

function sendRequest(parameters)
{
   if (! parameters.url)
   {
      return null;
   }
   
   var url = parameters.url;   
   var method = parameters.method || "GET";
   var handler = parameters.handler || function(){};   
   
   if (parameters.isAssinchronous === false)
   {
      var isAssinchronous = false
   }
   else
   {
      var isAssinchronous = true;
   }
   
   var body = parameters.body || null;
   var contentType = parameters.contentType || null;

   if (parameters.showLoader === false)
   {
      var showLoader = false
   }
   else
   {
      var showLoader = true;   
   }
   
   if (showLoader)
   {
      loader.show();
   }
   
   var request = getRequest();
   
   if (request === null) 
   {
      return null;
   }
   
   /* prepare request */
   request.onreadystatechange = wrapperHandler(request, handler, showLoader);
   request.open(method, url, isAssinchronous);
   
   if (contentType) 
   {
      try 
      {
	 request.setRequestHeader("Content-Type", contentType);
      } 
      catch (e) 
      {
	 if (showLoader)
	 {
	    loader.hide();
	 }
	 
	 return null;
      }
   }
   
   if (showLoader)
   {
      setTimeout(function() {
	 request.send(body);
	 
	 if (!isAssinchronous) 
	 {
	    if (showLoader)
	    {
	       loader.hide();
	    }
	    
	    return handler(request, handler, showLoader);
	 }
	 
	 return;
      }, 0);  // to fix error with loader displaying in Google Chrome an IE (CM-357, CLDIDE-79)
   }
   else
   {
      request.send(body);
      
      if (!isAssinchronous) 
      {
	 return handler(request, handler, showLoader);
      }
   }   
}

function getRequest()
{
   // define the Ajax library
   try 
   {
      // Firefox, Opera 8.0+, Safari
      return new XMLHttpRequest();
   } 
   catch (e) 
   {
      // Internet Explorer
      try 
      {
	 return new ActiveXObject("Msxml2.XMLHTTP");
      } 
      catch (e) 
      {
	 return new ActiveXObject("Microsoft.XMLHTTP");
      }
   }
   
   alert("Browser does not support HTTP Request");
   return null;
}


function wrapperHandler(request, handler, hideLoader)
{
   function Handler()
   {
      this.execute = function()
      {
	 if (request.readyState == 4) 
	 {
	    if (hideLoader)
	    {
	       loader.hide();
	    }
	    
	    handler(request);
	 };
      };
   };
   return (new Handler().execute);
};

function isSuccess(responseStatus)
{
   return (responseStatus >= 200 && responseStatus < 300) || responseStatus == 304 
	     || responseStatus == 1223 ;  // 204 status = 1223 status in IE 8
}

function isRedirect(responseStatus, newLocation)
{
   return (newLocation != null) && (responseStatus != "") 
	   && (responseStatus == 301 || responseStatus == 302 || responseStatus == 303);
}



/**
 * Shows "Contact Us " form.
 */

function showContactUsForm(url) {
   sendRequest({
      url: url,
      method: "GET", 
      handler: onReceiveShowContactFormResponse,
      isAssinchronous: false,
      showLoader: false
   });
}


function onReceiveShowContactFormResponse(request){
   if (isSuccess(request.status)) 
   {
      var container = document.getElementById(CONTACT_US_CONTAINER_ID);
      var maskLayer = document.getElementById(MASK_LAYER_ID);

      var body = document.body, html = document.documentElement;
      var height = Math.max(body.scrollHeight, body.offsetHeight, html.clientHeight, html.scrollHeight, html.offsetHeight);
      maskLayer.style.height = height-140 + "px";

      container.innerHTML = request.responseText;
      
      maskLayer.style.display = "block";
      container.style.display = "block";
   }
}

function hideContactUsForm() {
    var container = document.getElementById(CONTACT_US_CONTAINER_ID);
    container.innerHTML = "";
    container.style.display = "none";
    return false;
}

