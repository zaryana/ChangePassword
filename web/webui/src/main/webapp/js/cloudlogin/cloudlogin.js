/**
 * Module used to manage cloud login wizard JS features
 *
 */
var CloudLogin = {};

CloudLogin.WS_SENDMAIL_URL = "/rest/invite-join-ws/send-mail/";
CloudLogin.WS_STATUS_RESPONSE_OK = "Message sent";
CloudLogin.EMAIL_REGEXP = /^([a-zA-Z0-9_\.\-])+\@((([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+)$/;
CloudLogin.NB_EMAILS_OK = 0;
CloudLogin.NB_EMAILS_REQUESTED = 0;
CloudLogin.NB_EMAILS = 0;
CloudLogin.ERROR_MESSAGES = new Array();
CloudLogin.DEFAULT_VALUE = "Add email addresses";

CloudLogin.initCloudLogin = function() {
  if (!window.console) console = {};
  console.log = console.log || function(){};
  console.warn = console.warn || function(){};
  console.error = console.error || function(){};
  console.info = console.info || function(){};
  
  $("#email").val(CloudLogin.DEFAULT_VALUE);
  
  // Event only for IE
  document.getElementById('email').onclick = function() {document.getElementById('email').value = '';}

  CloudLogin.initTextExt();
}

CloudLogin.initTextExt = function() {
  // http://textextjs.com
  var textExt = $('#email').textext({ 
    plugins: 'tags',
    ext : {
      itemManager: {
        itemToString: function(item) {
          return item.replace(/@.*$/,"");
        }
      },
      core: {
        trigger: function() {
          // Case of caracters with event KeyUp (13=ENTER, 32=SPACE, 188=COMMA)
          if(arguments != null && arguments[0] === "anyKeyUp" && (arguments[1] === 13 || arguments[1] === 32 || arguments[1] === 188)) {
            var textExtTags = this.tags.apply();
            // delete "," or " " caracter if exist
            var tag = textExtTags.val();
            var lastCar = tag.charAt(tag.length-1);
            if(/,/.test(tag)|| /\s/.test(tag)) {
              textExtTags.val(tag.slice(0, -1));
            }
            // Execute onEnterKeyPress method
            $.fn.textext.TextExtTags.prototype.onEnterKeyPress.apply(textExtTags, arguments);
            textExtTags.val("");
          }
          else {
            // We keep default behavior
            $.fn.textext.TextExt.prototype.trigger.apply(this, arguments);
          }
        }
      },
      tags: {
        removeTag: function(tag) {
          // CASE of tag removed
          CloudLogin.decrementNbMails();
          $.fn.textext.TextExtTags.prototype.removeTag.apply(this, arguments);
        },
        addTags: function(tags) {
          // CASE of tag added
          if(tags != null && tags != undefined) {
            CloudLogin.incrementNbMails();
            $.fn.textext.TextExtTags.prototype.addTags.apply(this, arguments);
          }
        },
        onEnterKeyPress: function(e) {
          e.preventDefault();
        }
      }
    },
    html : {
      hidden: '<input type="hidden" id="emails" />'
    }
  });
  
  textExt.bind(
    'isTagAllowed', function(e, data) {
      var dataTag = data.tag.replace(/^\s+|\s+$/g,'');
      var isValidEmail = CloudLogin.isValidEmail(dataTag);
      if(!isValidEmail && dataTag.length > 0) {
        CloudLogin.displayMessage('email is not valid: "' + dataTag + '"');
      }
      data.result = isValidEmail;
      return;
    }
  );
}

CloudLogin.showStep = function(n) {
  $('#StartedStep1').hide();
  $('#StartedStep2').hide();
  $('#StartedStep3').hide();
  $('#StartedStep' + n).show();
}

/**
 * Ajax call to WS invite-join-ws/send-mail/
 */
CloudLogin.sendEmail = function(email, noEmail) {
  
  var hostname = CloudLogin.getDomainFromEmail(email);
  var mainUrl = CloudLogin.WS_SENDMAIL_URL + email + "/" + hostname;
  
  $.ajax({
      url: mainUrl,
      success: function(status) {
        CloudLogin.NB_EMAILS_REQUESTED++;
        if (status.indexOf(CloudLogin.WS_STATUS_RESPONSE_OK) != -1) {
          console.log("Invitation has been sent to [" + email + "]");
          
          // Test if we pass all emails
          CloudLogin.NB_EMAILS_OK++;
          if(CloudLogin.NB_EMAILS_OK == CloudLogin.NB_EMAILS) {
            CloudLogin.exit();
          }
        }
        else {
          console.log("Invitation cannot be sent to [" + email + "], [Status=" + status + "]");
          CloudLogin.ERROR_MESSAGES[noEmail] = "[" + email + "] " + status;
        }
        CloudLogin.finalizeSendEmails();
      },
      error: function(request, status, error) {
        CloudLogin.NB_EMAILS_REQUESTED++;
        console.log("Invitation cannot be sent to [" + email + "], [Status=" + request.status + ": " + request.statusText + "], [Message=" + request.responseText + "]");
        if(request.status === 404) {
          CloudLogin.ERROR_MESSAGES[noEmail] = "[" + email + "] Cannot contact server";
        }
        else {
          CloudLogin.ERROR_MESSAGES[noEmail] = "[" + email + "] " + request.responseText;
        }
        CloudLogin.finalizeSendEmails();
      },
      dataType: 'text'
    });
}

/**
 * Called after all mails are sent (or not) to display an error message if one or more mails cannot be sent
 */
CloudLogin.finalizeSendEmails = function() {

  if(CloudLogin.NB_EMAILS_REQUESTED == CloudLogin.NB_EMAILS &&  CloudLogin.NB_EMAILS_OK < CloudLogin.NB_EMAILS) {
    var message = CloudLogin.ERROR_MESSAGES.length + " email(s) cannot be sent:<ul>";
    // for
    for(var i=0; i<CloudLogin.ERROR_MESSAGES.length; i++) {
      message = message.concat("<li>").concat(CloudLogin.ERROR_MESSAGES[i]).concat("</li>");
    }
    message = message.concat("</ul>");
    CloudLogin.displayMessage(message);
    
    // Clear email not tagged
    document.getElementById("email").value = "";
    
    // Update Send button to return to initial state and activate button
    CloudLogin.updateSendButton();
  }
  else if(CloudLogin.NB_EMAILS === 0) {
    // Case of one email not tagged but sent, we exit wizard
    CloudLogin.exit();
  }
} 

CloudLogin.validateStep1 = function(event) {
  
  CloudLogin.NB_EMAILS_OK = 0;
  CloudLogin.NB_EMAILS_REQUESTED = 0;
  CloudLogin.ERROR_MESSAGES = [];
  
  var emails = eval(document.getElementById("emails").value);
  var emailNotTagged = document.getElementById("email").value;
  
  // only if there is no tags and this is not default value
  if(emails.length === 0 && CloudLogin.DEFAULT_VALUE != emailNotTagged) {
    if(emailNotTagged != undefined && emailNotTagged.length > 0) {
      // trim
      emailNotTagged = emailNotTagged.replace(/^\s+/g,'').replace(/\s+$/g,'')
      if(CloudLogin.isValidEmail(emailNotTagged)) {
        var list = $('#email').textext();
        var textExtTags = list[0].tags.apply();
        textExtTags.addTags([emailNotTagged]);
        emails.push(emailNotTagged);
      }
      else {
        CloudLogin.displayMessage('email is not valid: "' + emailNotTagged + '"');
      }
      document.getElementById("email").value = "";
      return;
    }
  }
  
  // deactivate button
  CloudLogin.setButtonToSendState();
  
  // lets pass if there isn't emails
  if(emails.length == 0) {
    CloudLogin.exit();
  }
  
  for(var i=0; i<emails.length; i++) {
    CloudLogin.sendEmail(emails[i], i);
  }
}

CloudLogin.validateStep2 = function() {
  CloudLogin.showStep(3);
}

CloudLogin.validateStep3 = function() {
  CloudLogin.exit();
}

CloudLogin.exit = function() {
  $("#CloudExitForm").submit();
}

/**
 * Display red message up to input
 */
CloudLogin.displayMessage = function(message) {
  $("#messageString").show();
  $("#messageString").html(message);
}

/**
 * Clear red message up to input
 */
CloudLogin.clearMessage = function() {
  $("#messageString").hide();
  $("#messageString").text("");
}

/**
 * Update button Send. At the first button is "Skip", after it is "Send (x)" x is number of mails
 */
CloudLogin.updateSendButton = function() {
  if(CloudLogin.NB_EMAILS > 0) {
    $("#t_submit").val("Send (" + CloudLogin.NB_EMAILS + ")");
  }
  else {
    $("#t_submit").val("Skip");
  }
  
  // reactivate button
  document.getElementById("t_submit").disabled = false;
}

/**
 * Update button Send. At the first button is "Skip", after it is "Send (x)" x is number of mails
 */
CloudLogin.setButtonToSendState = function() {
  document.getElementById("t_submit").disabled = true;
  $("#t_submit").val("Sending...");
}

CloudLogin.decrementNbMails = function() {
  CloudLogin.clearMessage();
  CloudLogin.NB_EMAILS--;
  CloudLogin.updateSendButton();
}

CloudLogin.incrementNbMails = function() {
  CloudLogin.clearMessage();
  CloudLogin.NB_EMAILS++;
  CloudLogin.updateSendButton();
}

/**
 * Returns a domain from an email. If email is not correct, returns all email
 */
CloudLogin.getDomainFromEmail = function(email) {
 var domain = email;
 // If is mail
 if(CloudLogin.EMAIL_REGEXP.test(email)) {
   var match = CloudLogin.EMAIL_REGEXP.exec(email);
   if(typeof(match) != "undefined"){
     domain = match[2];
   }
 }
 else {
    console.log("CloudLogin: " + email + " is not valid mail");
 }
 return domain;
}

/**
 * Returns true if email inparameter is valid
 */
CloudLogin.isValidEmail = function(email) {
  var isValidEmail = false;
  if(email != undefined && CloudLogin.EMAIL_REGEXP.test(email)) {
    isValidEmail = true;
  }
  return isValidEmail;
}
