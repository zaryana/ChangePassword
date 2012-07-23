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
var mktURL = "http://learn.cloud-workspaces.com/index.php/leadCapture/save";
var loopfuseURL = "http://lfov.net/webrecorder/f";

if (location.port) {
  prefixUrl += ':' + location.port;
}
var user;
var auth = null;

/* Init function */

Tenants.prototype.init = function() {
  accessUrl = prefixUrl + '/rest/cloud-admin';
  accessSecureUrl = prefixUrl + '/rest/private/cloud-admin';
  tenantServicePath = accessUrl + "/cloudworkspaces/tenant-service";
  tenantSecureServicePath = accessSecureUrl + "/cloudworkspaces/tenant-service";
  infoServicePath = accessSecureUrl + "/info-service/";
  $.extend({
  getUrlVars: function(){
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
      hash = hashes[i].split('=');
      vars.push(hash[0]);
      vars[hash[0]] = hash[1];
    }
   return vars;
   },
    getUrlVar: function(name){
       return $.getUrlVars()[name];
    }
   });

   jQuery.validator.addMethod(
     'regexp',
    function(value, element, regexp) {
    var re = new RegExp(regexp);
    return this.optional(element) || re.test(value);
    },
   "Such name cannot be used."
   );
}

Tenants.prototype.initRegistrationPage = function() {
  tenants.init();
  if (queryString != null && queryString != "") {
    var email;
    uuid = $.getUrlVar('id');
    var checkURL = tenantServicePath + "/uuid/" + uuid;
    $.ajax({
      url : checkURL,
      success : function(data) {
            // alert(data);
            email = data;
            if (email != null && email != "") {
              var split = email.split('@');
              var prefix = split[0];
              $('#email').val(email);
              $('#username').val(prefix);
              if (prefix.indexOf('.') > -1) {
                var splittedName = prefix.split('.');
                $('#first_name').val(splittedName[0].capitalize());
                $('#last_name').val(splittedName[1].capitalize());
              } else {
                $('#first_name').val(prefix);
              }
            } else {
              $("#messageString").html("Application error: email is not found. Please contact support.");
            }
            if (uuid != "")
              $('#confirmation-id').val(uuid);
            else
              $("#messageString").html("Application error: damaged form confirmation-id. Please contact support.")
          },
       dataType : 'text',
       error : function(request, status, error) {
            email = $.getUrlVar('email');
            if (email != null && email != "") {
              var split = email.split('@');
              var prefix = split[0];
              $('#email').val(email);
              $('#username').val(prefix);
              if (prefix.indexOf('.') > -1) {
                var splittedName = prefix.split('.');
                $('#first_name').val(splittedName[0]);
                $('#last_name').val(splittedName[1]);
              } else {
                $('#first_name').val(prefix.capitalize());
              }
            } else {
              $("#messageString").html("Warning! You are using broken link to the Registration Page. Please sign up again.");
            }
            if (uuid != "")
              $('#confirmation-id').val(uuid);
            else
              $('#messageString').html("Application error: damaged form confirmation-id. Please contact support.")
          }
        });
  }
}

Tenants.prototype.initDonePage = function() {
  tenants.init();
  var checkURL = tenantServicePath + "/status/" + tName;
  $.ajax({
    url : checkURL,
    success : function(data) {
          var search = "ONLINE";
          if (data.substring(0, search.length) === search) {
            var checkURL = tenantServicePath + "/isuserexist/" + tName + "/" + split[0];
            $.ajax({
              url : checkURL,
              success : function(data) {
                    var search = "true";
                    if (data.substring(0, search.length) === search) {
                      $("#sign_link").html("You can now <span style=\"color:#19BBE7;\"><u>sign-in</u></span> the "
                          + tName + "  Workspace.");
                      $("#sign_link").attr("href", "/signin.jsp?email=" + email);
                    } else {
                      $("#sign_link").html("<span style=\"color:#b81919;\">We cannot add you to the "
                          + tName
                          + " Workspace at the moment. The Workspace administrator has been notified of your attempt to join.</span>");
                    }
                  },
                  error : function(request, status, error) {
                    $("#sign_link").html("The " + tName
                        + " Workspace is beind created.<br/> We will inform you by email when ready.");
                  },
                  dataType : 'text'
                });

          } else {
            $("#sign_link").html("The " + tName
                + " Workspace is beind created.<br/> We will inform you by email when ready.");
          }
        },
        error : function(request, status, error) {
          $("#sign_link").html("The " + tName
              + " Workspace is beind created.<br/> We will inform you by email when ready.");
        },
        dataType : 'text'
      });
}


Tenants.prototype.initJoinPage = function() {
  tenants.init();
  var email;
  if (queryString != null && queryString != "") {
    rfid = $.getUrlVar('rfid');
    if (rfid == null) {
      $("#joinForm").html("<br><center><a class=\"BackIcon\" href=\"/index.jsp\">Home</a></center>");
      $("#messageString").html("Sorry, your registration link has expired. Please <a class=\"TenantFormMsg\" href=\"index.jsp\"><u>sign up</u></a> again.");
      return;
    }

    var checkURL = tenantServicePath + "/uuid/" + rfid;
    $.ajax({
          url : checkURL,
          success : function(data) {
            // alert(data);
            email = data;
            if (email != null && email != "") {
              userinfo = getUserMailInfo(email);
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
          },
          error : function(request, status, error) {
            $("#joinForm").html("<br><center><a class=\"BackIcon\" href=\"/index.jsp\">Home</a></center>");
            $("#messageString").html(request.responseText);
          },
          dataType : 'text'
        });
  }
}

function setCookie(name,value,minutes) {
    if (minutes) {
        var date = new Date();
        date.setTime(date.getTime()+(minutes*60*1000));
        var expires = "; expires="+date.toGMTString();
    }
    else var expires = "";
    document.cookie = name+"="+value+expires+"; path=/";
}

function getCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for(var i=0;i < ca.length;i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1,c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
    }
    return null;
}

Tenants.prototype.initTryAgainPage = function() {
  tenants.init();
  var tryagainMessage = getCookie("tryagainMessage");
  if (tryagainMessage != null) {
    $('#tryAgainNote').html("Sorry, we really need a company email address.");
  }
}

Tenants.prototype.initSignInPage = function() {
  tenants.init();
  if (queryString != null && queryString != "") {
    email = $.getUrlVar('email');
    if (email != null && email != "") {
      $('#email').val(email);
      $('#workspace').val(getUserMailInfo(email).tenant);
    }
  }
}

Tenants.prototype.initChange = function() {
  tenants.init();
  if (queryString != null && queryString != "") {
    id = $.getUrlVar('id');
    if (id != null && id != "") {
      $('#id').val(id);
    } else {
      $("#messageString").html("Your link to this page is broken. Please, re-request it.");
    }
  } else {
     $("#messageString").html("Your link to this page is broken. Please, re-request it.");
  }
}

Tenants.prototype.initResumingHidePage = function() {
  tenants.init();
  var workspace = $("#tenantname").text();
  $("#li1").html("If you are already a member of the " + workspace
      + " social intranet, please <a href=\"javascript:void(0);\" onClick=\"window.location.reload()\">try again</a> in a few minutes.");
  $("#li2").html("If you are trying to join the " + workspace
      + " social intranet, check you mail box for an invitation.");
  tryResume(workspace);
}

function tryResume(workspace) {
  $.ajax({
    url : accessUrl + "/tenant-service/resume?tenant=" + workspace,
    async : true,
    success : function(data) {
      waitReadyWorkspace(workspace, function() {
        window.location = document.URL;
      });
    },
    error : function(response, status, error) {
      if (response.status != 0) {
        if (response.responseText.indexOf("Starting failed... not available space on application servers") != -1) {
          setTimeout(function() {
            tryResume(workspace);
          }, 5 * 60 * 1000);
        }
        else {
          $("#messageString").html("Your tenant cannot be resumed in time. This was reported to administrators. Try again later.");
        }
      }
    }
  });
}

function waitReadyWorkspace(workspace, ready) {
  var host = location.hostname.indexOf("www") == 0 ? location.hostname.substring(4) :location.hostname;
  var isReadyUrl = location.protocol + '//' + host + '/rest/cloud-agent/info-service/is-ready/' + workspace;
  $.ajax({
    url : isReadyUrl,
    async : false,
    success : function(data) {
      if (data === "true")
        ready();
      else
        setTimeout(function() {
          waitReadyWorkspace(workspace, ready);
        }, 5 * 1000);
    },
    error : function(response, status, error) {
      setTimeout(function() {
        waitReadyWorkspace(workspace, ready);
      }, 5 * 1000);
    }
  });
}

Tenants.prototype.initResumingPage = function() {
  tenants.init();
  if (queryString != null && queryString != "") {
    email = $.getUrlVar('email');
    var workspace = getUserMailInfo(email).tenant;
    $("#li1").html("If you are already a member of the " + workspace
        + " Workspace, please <a href=\"/signin.jsp?email=" + email + "\">try again</a> in few minutes.");
    $("#li2").html("If you are trying to join " + workspace
        + " Workspace, check you mail box for an invitation");
  }
  isOnline();

  }

  function isOnline(){
    var email = $.getUrlVar('email');
    var workspace = getUserMailInfo(email).tenant;
    var host = location.hostname.indexOf("www") == 0 ? location.hostname.substring(4) :location.hostname;
    var login_redirect = location.protocol + '//' + workspace + '.' + host + '/portal/dologin?&initialURI=/portal/intranet/home';
    var signup_redirect = location.protocol + '//' + host + '/signup-done.jsp';
    var reset_redirect = location.protocol + '//' + host + '/reset-password.jsp';
    var checkURL = tenantServicePath + "/status/" + workspace;
    var search = "ONLINE";
      $.ajax({
        url : checkURL,
        async: false,
        success : function(data) {
          if (data.substring(0, search.length) === search){
             if  ($.getUrlVar('action') == 'signup')
               window.location = signup_redirect;
             else if  ($.getUrlVar('action') == 'reset')
               window.location = reset_redirect;
             else
               window.location = login_redirect;
            }
          else {
            setTimeout("isOnline()", 5000);
            }
        },
        error : function(request, status, error) {
            return;
        },
        dataType : 'text'
      });
  }



/* Login redirect */
Tenants.prototype.doLogin = function() {
  var tname = $('#workspace').val();
  var login = $('#email').val();
  var pass = $('#password').val();

  jQuery.validator.setDefaults({
    errorPlacement : function(error, element) {
      error.appendTo(element.next());
    },
  });
  var valid = $('#signinForm').valid();
  if (!valid)
    return;

  var username = getUserMailInfo(login).username;
  var host = location.hostname.indexOf("www") == 0 ? location.hostname.substring(4) :location.hostname;
  var redirect = location.protocol + '//' + tname + '.' + host;
  redirect += '/portal/login?username=';
  redirect += username;
  redirect += '&password=';
  redirect += pass;
  redirect += '&initialURI=/portal/intranet/home';
  var checkURL = tenantServicePath + "/status/" + tname;
  var search = "ONLINE";
  $.ajax({
    url : checkURL,
    success : function(data) {
      if (data.substring(0, search.length) === search)
        window.location = redirect;
      else
        $("#messageString").html("The workspace " + tname + " does not exist or unreachable.");
    },
    error : function(request, status, error) {
        $("#messageString").html(request.responseText);
    },
    dataType : 'text'
  });
}

/* Sending signup request */
Tenants.prototype.doSingupRequest = function() {
  var url = tenantServicePath + "/signup";

  if ($('#email').val().length == 0) {
    $("#messageString").html("Please, indicate your email.");
    return;
  }

  if ($('#email').val().indexOf('%') > -1) {
      $("#messageString").html("Your email contains disallowed characters.");
    return;
  }

  $("#t_submit").val("Wait...");
  $("#t_submit").attr('disabled', 'disabled');
  tenants.xmlhttpPost(url, tenants.handleSignupResponse, tenants.getquerystringSignup, null);

}

Tenants.prototype.doSignupRequestOnTryAgain = function() {
  var url = tenantServicePath + "/signup";

  if ($('#email').val().length == 0) {
    $("#messageString").html("Please, indicate your email.");
    return;
  }

  if ($('#email').val().indexOf('%') > -1) {
      $("#messageString").html("Your email contains disallowed characters.");
    return;
  }

  $("#t_submit").val("Wait...");
  $("#t_submit").attr('disabled', 'disabled');
  tenants.xmlhttpPost(url, tenants.handleSignupResponse, tenants.getquerystringSignup, function() {
    setCookie("tryagainMessage", "true", 5);
  });

}

/* Sending creation request */
Tenants.prototype.doCreationRequest = function() {
  var url = tenantServicePath + "/create";

  if ($("#confirmation-id").val().length == 0) {
      $("#messageString").html("Cannot process request, confirmation ID is not set.");
    return;
  }

  jQuery.validator.setDefaults({
    errorPlacement : function(error, element) {
      error.appendTo(element.next());
    },
  });

  $("#registrationForm").validate({
    rules : {
      password : {
        required : true,
        minlength : 6,
      },
      password2 : {
        required : true,
        minlength : 6,
        equalTo : "#password"
      },
      company : {
        required : true,
        regexp: "^[A-Za-z][\u0000-\u007F\u0080-\u00FFa-zA-Z0-9 '&-.]*[A-Za-z0-9]$"
      },
      first_name : {
        required : true,
        regexp: "^[A-Za-z][\u0000-\u007F\u0080-\u00FFa-zA-Z0-9 '&-.]*[A-Za-z0-9]$"
      },
      last_name : {
        required : true,
        regexp: "^[A-Za-z][\u0000-\u007F\u0080-\u00FFa-zA-Z0-9 '&-.]*[A-Za-z0-9]$"
      }
    }
  });

  var valid = $("#registrationForm").valid();
  if (!valid)
    return;

  $("#t_submit").val("Wait...");
  $("#t_submit").attr('disabled', 'disabled');
  tenants.xmlhttpPost(url, tenants.handleCreationResponse, tenants.getquerystringCreate, null);

}

/* Sending join request */
Tenants.prototype.doJoinRequest = function() {
  var url = tenantServicePath + "/join";

  jQuery.validator.setDefaults({
    errorPlacement : function(error, element) {
      error.appendTo(element.next());
    },
  });

  $("#joinForm").validate({
    rules : {
      password : {
        required : true,
        minlength : 6,
      },
      password2 : {
        required : true,
        minlength : 6,
        equalTo : "#password"
      },
      first_name : {
        required : true,
        regexp: "^[A-Za-z][\u0000-\u007F\u0080-\u00FFa-zA-Z0-9 '&-.]*[A-Za-z0-9]$"
      },
      last_name : {
         required : true,
         regexp: "^[A-Za-z][\u0000-\u007F\u0080-\u00FFa-zA-Z0-9 '&-.]*[A-Za-z0-9]$"
      }
    }
  });

  var valid = $("#joinForm").valid();
  if (!valid)
    return;

  $("#t_submit").val("Wait...");
  $("#t_submit").attr('disabled', 'disabled');
  tenants.xmlhttpPost(url, tenants.handleJoinResponse, tenants.getquerystringJoin, null);
}

Tenants.prototype.doContactRequest = function() {
  var url = tenantServicePath + "/contactus";
  var valid = $("#mycontactForm").valid();
  if (!valid)
    return;
  tenants.xmlhttpPost(url, tenants.handleContactResponse, tenants.getquerystringContactUs, null);

  // document.getElementById(CONTACT_US_CONTAINER_ID).style.display = "none";
  // document.getElementById(MASK_LAYER_ID).style.display = "none";
  $("#submitButton").val("Wait...");
  $("$cancelButton").attr('disabled', 'disabled');
}

Tenants.prototype.doChange = function() {

  $("#submitButton").val("Wait...");
  var url = tenantServicePath + "/passconfirm";
  jQuery.validator.setDefaults({
    errorPlacement : function(error, element) {
      error.appendTo(element.next());
    },
  });

  $("#changeForm").validate({
    rules : {
      password : {
        required : true,
        minlength : 6,
      },
      password2 : {
        required : true,
        minlength : 6,
        equalTo : "#password"
      }
    }
  });
  var valid = $("#changeForm").valid();
  if (!valid)
    return;

  var fdata = "uuid=" + $("#id").val() + "&password=" + jQuery.trim($("#password").val());
  $.ajax({
        url : url,
        type : 'POST',
        data : fdata,
        dataType : 'text',
        processData : false,
        success : function(data) {
          $("#submitButton").val("Submit");
          $("#messageString").html("<span style=\"color:#19BBE7;\">Success. You can now <a href='/signin.jsp'>login</a> with your new password.</span>");
        },
        error : function(request, status, error) {
          $("#messageString").html(request.responseText);
          $("#submitButton").val("Submit");
        }
      });
}

Tenants.prototype.doReset = function() {
  $("#submitButton").val("Wait...");
  var url = tenantServicePath + "/passrestore";
  jQuery.validator.setDefaults({
    errorPlacement : function(error, element) {
      error.appendTo(element.next());
    },
  });

  $("#resetForm").validate({
    rules : {
      email : {
        required : true,
      }
    },
  });
  var valid = $("#resetForm").valid();
  if (!valid) {
    $("#submitButton").val("Change my password");
    return;
  }
  var checkURL = url + "/" + $("#email").val();

  $.ajax({
        url : checkURL,
        success : function(data) {
          $("#submitButton").val("Change my password");
          $("#submitButton").hide();
          $("#messageString").html("<span style=\"color:#19BBE7;\">Request completed, check your email for instructions.</span>");
        },
        error : function(request, status, error) {
          if (request.status == 309) { // Custom status to handle
            // redirects;
            window.location = request.getResponseHeader("Location");
            return;
          }
          $("#messageString").html(request.responseText);
          $("#submitButton").val("Change my password");
        },
        dataType : 'text'
      });
  $("#submitButton").val("Wait...");
}

/* Handle signup response */
Tenants.prototype.handleSignupResponse = function(resp) {
  if (resp == "") {
    sendDataToMarketo({
      "Email" : $('#email').val(),
      "Cloud_Workspaces_User__c" : "Yes",
      "Search_Engine__c" : $('input[name=Search_Engine__c]').val(),
      "Search_String__c" : $('input[name=Search_String__c]').val(),
      "Pay_Per_Click_Keyword__c" : $('input[name=Pay_Per_Click_Keyword__c]').val(),
      "sfga" : $('input[name=sfga]').val(),
      "lpId": $('input[name=lpId]').val(),
      "subId": $('input[name=subId]').val(),
      "formid": $('input[name=formid]').val(),
      "_mkt_trk": $('input[name=_mkt_trk]').val()
    }, function() {
      window.location = prefixUrl + "/signup-done.jsp";
    });
  } else {
    sendDataToMarketo({
      "Email" : $('#email').val(),
      "Cloud_Workspaces_User__c" : "No",
      "Search_Engine__c" : $('input[name=Search_Engine__c]').val(),
      "Search_String__c" : $('input[name=Search_String__c]').val(),
      "Pay_Per_Click_Keyword__c" : $('input[name=Pay_Per_Click_Keyword__c]').val(),
      "sfga" : $('input[name=sfga]').val(),
      "LeadSource" : $('#LeadSource').val(),
      "lpId": $('input[name=lpId]').val(),
      "subId": $('input[name=subId]').val(),
      "formid": $('input[name=formid]').val(),
      "_mkt_trk": $('input[name=_mkt_trk]').val()
    }, function() {
     });
     $("#messageString").html('<span class="WarningIcon">' + resp + '</span>');
  }
  $("#t_submit").removeAttr('disabled');
  $("#t_submit").val("Sign Up");
}

/* Handle creation response */
Tenants.prototype.handleCreationResponse = function(resp) {
  if (resp == "") {
    sendDataToMarketo({
      "Email" : $('#email').val(),
      "FirstName" : $('#first_name').val(),
      "LastName" : $('#last_name').val(),
      "Company" : $('#company').val(),
      "Phone" : $('#phone_work').val(),
      "Cloud_Workspaces_User__c" : "Yes",
      "lpId": $('input[name=lpId]').val(),
      "subId": $('input[name=subId]').val(),
      "formid": $('input[name=formid]').val(),
      "_mkt_trk": $('input[name=_mkt_trk]').val()
    }, function() {
      window.location = prefixUrl + "/registration-done.jsp";
    });
  } else {
    $("#messageString").html(resp);
  }
  $("#t_submit").removeAttr('disabled');
  $("#t_submit").val("Create");
}

/* Handle join response */
Tenants.prototype.handleJoinResponse = function(resp) {
  if (resp == "") {
    sendDataToMarketo({
      "Email" : $('#email').val(),
      "FirstName" : $('#first_name').val(),
      "LastName" : $('#last_name').val(),
      "Cloud_Workspaces_User__c" : "Yes",
      "lpId": $('input[name=lpId]').val(),
      "subId": $('input[name=subId]').val(),
      "formid": $('input[name=formid]').val(),
      "_mkt_trk": $('input[name=_mkt_trk]').val()
    }, function() {
      var tname = $('#workspace').val();
      var login = $('#email').val();
      var pass = $('#password').val();
      var username = getUserMailInfo(login).username;
      var host = location.hostname.indexOf("www") == 0 ? location.hostname.substring(4) :location.hostname;
      var redirect = location.protocol + '//' + tname + '.' + host;
      redirect += '/portal/login?username=';
      redirect += username;
      redirect += '&password=';
      redirect += pass;
      redirect += '&initialURI=/portal/intranet/welcome';
      window.location = redirect;
    });
  } else {
    $("#messageString").html(resp);
  }

  $("#t_submit").removeAttr('disabled');
  $("#t_submit").val("Sign In");
}

Tenants.prototype.handleContactResponse = function(resp) {
  var splitName = tenants.splitName($('#name').val());
  if (resp == "") {
    sendDataToMarketo({
      "FirstName" : splitName.getFirstName(),
      "LastName" : splitName.getLastName(),
      "Email" : $('#email').val(),
      "Cloud_Workspaces_Contact_Us_Subject__c" : $('#subject').val(),
      "Cloud_Workspaces_Contact_Us_Message__c" : $('#ContactUs_Message__c').val(),
      "Cloud_Workspaces_User__c" : "Yes",
      "lpId": $('input[name=lpId]').val(),
      "subId": $('input[name=subId]').val(),
      "formid": $('input[name=formid]').val(),
      "_mkt_trk": $('input[name=_mkt_trk]').val()
    }, function() {
      window.location = "/contact-us-done.jsp";
    });
  } else {
    $("#" + CONTACT_US_CONTAINER_ID).hide();
    $("#" + MASK_LAYER_ID).hide();
    $("#messageString").html(resp);
  }
}

Tenants.prototype.xmlhttpPost = function(strURL, handler, paramsMapper, redirectHandler) {
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
      if (self.xmlHttpReq.status == 309) { // Custom status to handle
        // redirects;
        if (redirectHandler != null)
          redirectHandler();
        window.location = self.xmlHttpReq.getResponseHeader("Location");
        return;
      }

      handler(self.xmlHttpReq.responseText);
    }
  }
  self.xmlHttpReq.send(paramsMapper());
}

Tenants.prototype.getquerystringSignup = function() {
  qstr = 'user-mail=' + jQuery.trim($('#email').val());
  return qstr;
}

Tenants.prototype.getquerystringJoin = function() {
  qstr = 'user-mail=' + jQuery.trim($('#email').val());
  qstr += '&first-name=' + jQuery.trim($('#first_name').val());
  qstr += '&last-name=' + jQuery.trim($('#last_name').val());
  qstr += '&password=' + jQuery.trim($('#password').val());
  qstr += '&confirmation-id=' + jQuery.trim($('#rfid').val());
  return encodeURI(qstr);
}

Tenants.prototype.getquerystringCreate = function() {
  qstr = 'user-mail=' + jQuery.trim($('#email').val());
  qstr += '&first-name=' + jQuery.trim($('#first_name').val());
  qstr += '&last-name=' + jQuery.trim($('#last_name').val());
  qstr += '&password=' + jQuery.trim($('#password').val());
  qstr += '&phone=' + jQuery.trim($('#phone_work').val());
  qstr += '&company-name=' + jQuery.trim($('#company').val());
  qstr += '&confirmation-id=' + jQuery.trim($('#confirmation-id').val());
  return encodeURI(qstr);
}

Tenants.prototype.getquerystringContactUs = function() {
  qstr = 'user-mail=' + jQuery.trim($('#email').val());
  qstr += '&first-name=' + jQuery.trim($('#name').val());
  qstr += '&subject=' + jQuery.trim($('#subject').val());
  qstr += '&text=' + jQuery.trim($('#ContactUs_Message__c').val());
  return encodeURI(qstr);
}

function onlyNumbers(evt) {
  var charCode = (evt.which) ? evt.which : evt.keyCode;

  if (charCode > 31
      && ((charCode < 48 || charCode > 57) && charCode != 45 && charCode != 40 && charCode != 41 && charCode != 43))
    return false;

  return true;
}

function sendDataToMarketo(data, afterSubmitCallback) {
  var mktOutputIframeId = "mktOutput";
  var mktOutputIframeName = "mktOutput";
  var mktFormId = "mktForm";
  var mktFormName = "cloud-workspaces-profile";
  var commonData = {
   "LeadSource" : "Web - Cloud Workspaces",
   "marketo_comments": "",
   "kw": "",
   "cr": "",
   "searchstr": "",
   "lpurl": "http://learn.cloud-workspaces.com/Cloud-Workspaces-Sign-Up-English.html?cr={creative}&kw={keyword}",
   "returnURL": "",
   "retURL": "",
   "_mkt_disp": "return"
  }

  if (jQuery && document.getElementById(mktOutputIframeId)) {
      jQuery('#' + mktFormId).remove();
      jQuery('#' + mktOutputIframeId).attr('src', ""); // clear iframe

      jQuery('body').append(jQuery('<form/>', {
        id : mktFormId,
      name : mktFormName,
    method : 'POST',
    action : mktURL,
    target : mktOutputIframeName,
    enctype: 'application/x-www-form-urlencoded'
    }));

    for ( var i in data) {
      jQuery('#' + mktFormId).append(jQuery('<input/>', {
        type : 'hidden',
        name : i,
       value : data[i]
    }));
   }

   for ( var i in commonData) {
     jQuery('#' + mktFormId).append(jQuery('<input/>', {
     type : 'hidden',
     name : i,
    value : commonData[i]
    }));
   }
   Mkto.formSubmit(document.getElementById(mktFormId));

   var i = 200;  // set limited iterations - interrupt after the 20 seconds
   var afterSubmitHandler = window.setInterval(function() {
       if (!(i--) || isMarketoResponseReceived(mktOutputIframeId))
      {
        window.clearInterval(afterSubmitHandler);
        if (afterSubmitCallback)
        {
          afterSubmitCallback();
        }
       jQuery('#' + mktFormId).remove();
       jQuery('#' + mktOutputIframeId).attr('src', ""); // clear iframe  
      }
   }, 10);
   }
 }

function isMarketoResponseReceived(iframeId) {
  try {
    return !document.getElementById(iframeId)
      || document.getElementById(iframeId).contentWindow.location.href != "about:blank";
  } catch (e) {
    // check for permission of
    return true;
  }
}

Tenants.prototype.splitName = function(name) {
  // extract firstName and lastName from name
  var firstName = "";
  var lastName = "";

 // trim name, remove duplicated spaces, split by spaces
 var splittedName = jQuery.trim(name).replace(/\s+/g, " ").split(" ");
 if (splittedName && splittedName.length > 0){
   if (splittedName.length == 1){   
     // parse case like "John"
     firstName = splittedName[0];
     lastName = "";
   } else {
  // parse case like "John  Smith", or "John Entony Smith"
  lastName = splittedName.pop();
  firstName = splittedName.join(" ");
  }
 }

 name = encodeURIComponent(name);

 return {
     getName: function(){
       return name;
     },
     getFirstName: function(){
       return firstName;
     },
     getLastName: function(){
       return lastName;
     }
  }
}

//function  getTenantName(email) {
function getUserMailInfo(email) {
      var checkURL = tenantServicePath + "/usermailinfo/" + email;
      var result;
      $.ajax({
          url : checkURL,
          async: false,
          success : function(data) {
          result =  data;
          },
          error : function(request, status, error) {
             _gel("messageString").innerHTML = "Application error: cannot get tenant name. Please contact support."
           },
          dataType : 'json'
          });

          return result;
}

var keyStr = "ABCDEFGHIJKLMNOP" + "QRSTUVWXYZabcdef" + "ghijklmnopqrstuv" + "wxyz0123456789+/" + "=";

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

    output = output + keyStr.charAt(enc1) + keyStr.charAt(enc2) + keyStr.charAt(enc3) + keyStr.charAt(enc4);
    chr1 = chr2 = chr3 = "";
    enc1 = enc2 = enc3 = enc4 = "";
  } while (i < input.length);

  return output;
}

var tenants = new Tenants();

/**
 * --------------------- ajax library
 */
 String.prototype.capitalize = function() {
     return this.charAt(0).toUpperCase() + this.slice(1);
 }


function sendRequest(parameters) {
  if (!parameters.url) {
    return null;
  }

  var url = parameters.url;
  var method = parameters.method || "GET";
  var handler = parameters.handler || function() {
  };

  if (parameters.isAssinchronous === false) {
    var isAssinchronous = false
  } else {
    var isAssinchronous = true;
  }

  var body = parameters.body || null;
  var contentType = parameters.contentType || null;

  if (parameters.showLoader === false) {
    var showLoader = false
  } else {
    var showLoader = true;
  }

  if (showLoader) {
    loader.show();
  }

  var request = getRequest();

  if (request === null) {
    return null;
  }

  /* prepare request */
  request.onreadystatechange = wrapperHandler(request, handler, showLoader);
  request.open(method, url, isAssinchronous);

  if (contentType) {
    try {
      request.setRequestHeader("Content-Type", contentType);
    } catch (e) {
      if (showLoader) {
        loader.hide();
      }

      return null;
    }
  }

  if (showLoader) {
    setTimeout(function() {
      request.send(body);

      if (!isAssinchronous) {
        if (showLoader) {
          loader.hide();
        }

        return handler(request, handler, showLoader);
      }

      return;
    }, 0); // to fix error with loader displaying in Google Chrome an IE
    // (CM-357, CLDIDE-79)
  } else {
    request.send(body);

    if (!isAssinchronous) {
      return handler(request, handler, showLoader);
    }
  }
}

function getRequest() {
  // define the Ajax library
  try {
    // Firefox, Opera 8.0+, Safari
    return new XMLHttpRequest();
  } catch (e) {
    // Internet Explorer
    try {
      return new ActiveXObject("Msxml2.XMLHTTP");
    } catch (e) {
      return new ActiveXObject("Microsoft.XMLHTTP");
    }
  }

  alert("Browser does not support HTTP Request");
  return null;
}

function wrapperHandler(request, handler, hideLoader) {
  function Handler() {
    this.execute = function() {
      if (request.readyState == 4) {
        if (hideLoader) {
          loader.hide();
        }

        handler(request);
      };
    };
  };
  return (new Handler().execute);
};

function isSuccess(responseStatus) {
  return (responseStatus >= 200 && responseStatus < 300) || responseStatus == 304 || responseStatus == 1223; // 204 status =
  // 1223 status
  // in IE 8
}

function isRedirect(responseStatus, newLocation) {
  return (newLocation != null) && (responseStatus != "")
      && (responseStatus == 301 || responseStatus == 302 || responseStatus == 303);
}

/**
 * Shows "Contact Us " form.
 */

function showContactUsForm(url) {
  sendRequest({
    url : url,
    method : "GET",
    handler : onReceiveShowContactFormResponse,
    isAssinchronous : false,
    showLoader : false
  });
}

function onReceiveShowContactFormResponse(request) {
  if (isSuccess(request.status)) {
    var container = document.getElementById(CONTACT_US_CONTAINER_ID);
    var maskLayer = document.getElementById(MASK_LAYER_ID);

    var body = document.body, html = document.documentElement;
    var height = Math.max(body.scrollHeight, body.offsetHeight, html.clientHeight, html.scrollHeight,
        html.offsetHeight);
    maskLayer.style.height = height - 140 + "px";

    container.innerHTML = request.responseText;

    maskLayer.style.display = "block";
    container.style.display = "block";
    //Load JS file
    $.getScript("/js/mktFormSupport.js");
    //Fill data for Marketo form
    $.getScript("/js/trackers.js");
  }
}

function hideContactUsForm() {
  var container = document.getElementById(CONTACT_US_CONTAINER_ID);
  container.innerHTML = "";
  container.style.display = "none";
  return false;
}
