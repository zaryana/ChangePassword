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

/* Init function */

Tenants.prototype.init = function() {
  accessUrl = prefixUrl + '/rest/cloud-admin';
  accessSecureUrl = prefixUrl + '/rest/private/cloud-admin';
  tenantServicePath = accessUrl + "/cloudworkspaces/tenant-service";
  tenantSecureServicePath = accessSecureUrl + "/cloudworkspaces/tenant-service";
  infoServicePath = accessSecureUrl + "/info-service/";
}

Tenants.prototype.initRegistrationPage = function() {
  tenants.init();
  if (queryString != null && queryString != "") {
    var email_start = queryString.indexOf('email=');
    var uuid_start = queryString.indexOf('id=');
    var uuid = "";
    var email; 
    uuid = (uuid_start != -1) ? queryString.substring(uuid_start + 3) : null;
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
                $('#first_name').val(splittedName[0]);
                $('#last_name').val(splittedName[1]);
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
            email = (email_start != -1) ? queryString.substring(email_start + 6, uuid_start - 1) : null;
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
                $('#first_name').val(prefix);
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

/* Those methods written for pre-moderating tenants on private demo */
Tenants.prototype.initValidationPage = function() {
  tenants.init();
  if (auth) {
    tenants.showValidatioList(true);
  } else {
    tenants.showValidationForm();
  }
}

Tenants.prototype.showValidationList = function(isClearStatus) {
  var form = $("#validationTable").hide();
  if (isClearStatus)
    $("#messageString").html("");
  var checkURL = tenantSecureServicePath + "/requests/";
  var resp;
  $.ajax({
        url : checkURL,
        beforeSend : function(req) {
          req.setRequestHeader('Authorization', 'Basic ' + auth);
        },
        success : function(data) {
          resp = data;
          getElementById("ListTable").style.display = "table";
          var table = getElementById("ListTable");
          // Clear table
          while (table.rows.length > 1) {
            table.deleteRow(-1);
          }

          for (account in resp) {
            if (resp.propertyIsEnumerable(account)) {
              // alert(account); alert(resp[account][0]);
              // alert(resp[account][1]);
              var row = table.insertRow(-1);
              // Tname
              var cell0 = row.insertCell(0);
              cell0.className = "MyFieldLeft";
              cell0.innerHTML = "<b>" + resp[account][0] + "</b>";
              // Name + Email
              var cell1 = row.insertCell(1);
              cell1.className = "MyFieldLeft";
              cell1.innerHTML = resp[account][2] + " &lt;" + resp[account][1] + "&gt;";
              // Date
              var cell2 = row.insertCell(2);
              cell2.className = "MyFieldLeft";
              var date = new Date(parseFloat(account.substring(account.indexOf("_") + 1)));
              // toUTCString is too long for table
              var month = date.getUTCMonth() + 1;
              var mins = date.getUTCMinutes();
              if (mins.length == 1)
                mins = "0" + mins;
              var hours = date.getUTCHours();
              if (hours.length == 1)
                hours = "0" + hours;
              cell2.innerHTML = date.getUTCDate() + "/" + month + "/" + date.getFullYear() + " " + hours
                  + ":" + mins + " UTC";
              // Company
              var cell3 = row.insertCell(3);
              cell3.className = "MyFieldLeft";
              cell3.innerHTML = resp[account][3];
              // Phone
              var cell4 = row.insertCell(4);
              cell4.className = "MyFieldLeft";
              cell4.innerHTML = "<u>" + resp[account][4] + "</u>";
              // Action
              var cell5 = row.insertCell(5);
              cell5.className = "MyFieldLeft";
              cell5.innerHTML = '<a href="#" onClick="tenants.validationAction(\'' + tenantSecureServicePath
                  + '/validate/accept/' + account
                  + '\');">Accept</a>&nbsp;|&nbsp;<a href="#" onClick="tenants.validationAction(\''
                  + tenantSecureServicePath + '/validate/refuse/' + account
                  + '\');">Reject</a>&nbsp;|&nbsp;<a href="#" onClick="tenants.validationAction(\''
                  + tenantSecureServicePath + '/validate/blacklist/' + account + '\');">Blacklist</a>&nbsp;';
            }
          }
          var row1 = table.insertRow(-1);
          var cell_s = row1.insertCell(0);
          cell_s.colSpan = "6";
          cell_s.className = "MyField";
          cell_s.innerHTML = "<a href=\"javascript:void(0);\" onClick=\"tenants.showValidationList(true);\">Refresh</a>";
        },
        error : function(request, status, error) {
          if (request.responseText.indexOf("HTTP Status 401") > -1) {
            tenants.showValidationForm(true);
            $("#messageString").html("Wrong workspaces manager credentials.");
            return;
          } else
            $("#messageString").html(request.responseText);
        },
        dataType : 'json'
      });
}

Tenants.prototype.validationAction = function(url) {
  $.ajax({
        url : url,
        beforeSend : function(req) {
          req.setRequestHeader('Authorization', 'Basic ' + auth);
        },
        success : function(data) {
          if (data == "") {
            $("#messageString").html("<span style=\"color:blue;\">Action successfull.</span>");
            tenants.showValidationList(false);
          } else
            $("#messageString").html(data);
        },
        error : function(request, status, error) {
          $("#messageString").html(request.responseText);
        },
        dataType : 'text'
      });
}

Tenants.prototype.showValidationForm = function() {
  $("#ListTable").hide();
  $("#validationTable").show();
}

Tenants.prototype.validationLogin = function() {
  auth = encode64($("#v_username").val() + ":" + $("#v_pass").val());
  tenants.showValidationList(true);
}

Tenants.prototype.initJoinPage = function() {
  tenants.init();
  var email;
  if (queryString != null && queryString != "") {
    var rfid_start = queryString.indexOf('rfid=');
    rfid = (rfid_start != -1) ? queryString.substring(rfid_start + 5) : null;
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
              var split = email.split('@');
              $('#email').val(email);
              var prefix = split[0];
              $('#username').val(prefix);
              if (prefix.indexOf('.') > -1) {
                var splittedName = prefix.split('.');
                $('#first_name').val(splittedName[0]);
                $('#last_name').val(splittedName[1]);
              } else {
                $('#first_name').val(prefix);
              }
              $('#workspace').val(getTenantName(email));
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

Tenants.prototype.initSignInPage = function() {
  tenants.init();
  if (queryString != null && queryString != "") {
    var email_start = queryString.indexOf('email=');
    email = (email_start != -1) ? queryString.substring(email_start + 6) : null;
    if (email != null && email != "") {
      $('#email').val(email);
      $('#workspace').val(getTenantName(email));
    }
  }
}

Tenants.prototype.initChange = function() {
  tenants.init();
  if (queryString != null && queryString != "") {
    var id_start = queryString.indexOf('id=');
    id = (id_start != -1) ? queryString.substring(id_start + 3) : null;
    if (id != null && id != "") {
      $('#id').val(id);
    } else {
      $("#messageString").html("Your link to this page is broken. Please, re-request it.");
    }
  } else {
     $("#messageString").html("Your link to this page is broken. Please, re-request it.");
  }
}

Tenants.prototype.initResumingPage = function() {
  tenants.init();
  if (queryString != null && queryString != "") {
    var email_start = queryString.indexOf('email=');
    email = (email_start != -1) ? queryString.substring(email_start + 6) : null;
    var split = email.split('@');
    var workspace = split[1].substring(0, split[1].indexOf('.'));
    $("#li1").html("If you are already a member of the " + workspace
        + " Workspace, please <a href=\"/signin.jsp?email=" + email + "\">try again</a> in few miunutes.");
    $("#li2").html("If you are trying to join " + workspace
        + " Workspace, check you mail box for an invitation");
  }
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

  var username = login.substring(0, login.indexOf('@'));
  var host = location.hostname.indexOf("www") == 0 ? location.hostname.substring(4) :location.hostname;
  var redirect = location.protocol + '//' + tname + '.' + host;
  redirect += '/portal/login?username=';
  redirect += username;
  redirect += '&password=';
  redirect += pass;
  redirect += '&initialURI=/portal/intranet/welcome';
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
  tenants.xmlhttpPost(url, tenants.handleSignupResponse, tenants.getquerystringSignup);

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
      }
    }
  });

  var valid = $("#registrationForm").valid();
  if (!valid)
    return;

  $("#t_submit").val("Wait...");
  $("#t_submit").attr('disabled', 'disabled');
  tenants.xmlhttpPost(url, tenants.handleCreationResponse, tenants.getquerystringCreate);

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
      }
    }
  });

  var valid = $("#joinForm").valid();
  if (!valid)
    return;

  $("#t_submit").val("Wait...");
  $("#t_submit").attr('disabled', 'disabled');
  tenants.xmlhttpPost(url, tenants.handleJoinResponse, tenants.getquerystringJoin);
}

Tenants.prototype.doContactRequest = function() {
  var url = tenantServicePath + "/contactus";
  var valid = $("#mycontactForm").valid();
  if (!valid)
    return;
  tenants.xmlhttpPost(url, tenants.handleContactResponse, tenants.getquerystringContactUs);

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

  var fdata = "uuid=" + $("#id").val() + "&password=" + $("#password").val().trim();
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
    sendDataToLoopfuse({
      "email" : $('#email').val(),
      // hidden LoopFuse fields
      "formid" : $('#formid').val(),
      "cid" : $('#cid').val()
    }, function() {
      window.location = prefixUrl + "/signup-done.jsp";
    });
  } else {
    $("#messageString").html(resp);
  }
  $("#t_submit").attr('disabled', '');
  $("#t_submit").val("Sign Up");
}

/* Handle creation response */
Tenants.prototype.handleCreationResponse = function(resp) {

  if (resp == "") {
    sendDataToLoopfuse({
      "email" : $('#email').val(),
      "first_name" : $('#first_name').val(),
      "last_name" : $('#last_name').val(),
      "company" : $('#company').val(),
      "phone_work" : $('#phone_work').val(),
      // hidden LoopFuse fields
      "formid" : $('#formid').val(),
      "cid" : $('cid').val()
    }, function() {
      window.location = prefixUrl + "/registration-done.jsp";
    });
  } else {
     $("#messageString").html(resp);
  }
  $("#t_submit").attr('disabled', '');
  $("#t_submit").val("Create");
}

/* Handle join response */
Tenants.prototype.handleJoinResponse = function(resp) {

  if (resp == "") {
    sendDataToLoopfuse({
      "email" : $('#email').val(),
      "first_name" : $('#first_name').val(),
      "last_name" :  $('#last_name').val(),
      // hidden LoopFuse fields
      "formid" : $('#formid').val(),
      "cid" : $('#cid').val()
    }, function() {
      window.location = prefixUrl + "/join-done.jsp#" + $('#email').val();
    });
  } else {
    $("#messageString").html(resp);
  }

  $("#t_submit").attr('disabled', '');
  $("#t_submit").val("Sign In");

}

Tenants.prototype.handleContactResponse = function(resp) {

  if (resp == "") {
    sendDataToLoopfuse({
      "email" : $('#email').val(),
      "first_name" : $('#name').val(),
      "company" : $('#subject').val(),
      "message" : $('#ContactUs_Message__c').val(),
      // hidden LoopFuse fields
      "formid" : $('#formid').val(),
      "service_source" : $('#service_source').val(),
      "cid" : $('#cid').val()
    }, function() {
      // document.getElementById('Content').innerHTML = "<div
      // class=\"ThanksPages ClearFix\"><h1>Thank you!</h1><p
      // style=\"text-align:center\">Your request has been successfully
      // submitted. We will get back to you soon.</p></div>";
      window.location = "/contact-us-done.jsp";
    });
  } else {
    $("#" + CONTACT_US_CONTAINER_ID).hide();
    $("#" + MASK_LAYER_ID).hide();
    $("#messageString").html(resp);
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
  self.xmlHttpReq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
  self.xmlHttpReq.onreadystatechange = function() {
    if (self.xmlHttpReq.readyState == 4) {
      if (self.xmlHttpReq.status == 309) { // Custom status to handle
        // redirects;
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
  var charCode = (evt.which) ? evt.which : event.keyCode;

  if (charCode > 31
      && ((charCode < 48 || charCode > 57) && charCode != 45 && charCode != 40 && charCode != 41 && charCode != 43))
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

function getTenantName(email) {
      var checkURL = tenantServicePath + "/tenantname/" + email;
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
          dataType : 'text'
          });
          return result;
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
  }
}

function hideContactUsForm() {
  var container = document.getElementById(CONTACT_US_CONTAINER_ID);
  container.innerHTML = "";
  container.style.display = "none";
  return false;
}
