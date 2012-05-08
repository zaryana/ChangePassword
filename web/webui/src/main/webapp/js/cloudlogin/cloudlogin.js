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
CloudLogin.EMAILS_NOK = new Array();

CloudLogin.initCloudLogin = function() {
  // tenants.init();
  
  if (!window.console) console = {};
  console.log = console.log || function(){};
  console.warn = console.warn || function(){};
  console.error = console.error || function(){};
  console.info = console.info || function(){};
  
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
          // Case of " " or "," caracters with event KeyUp
          if(arguments != null && arguments[0] === "anyKeyUp" && (arguments[1] === 32 || arguments[1] === 188)) {
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
          else if(arguments != null && arguments[0] === "anyKeyUp" && arguments[1] === 13) {
            // Case of "ENTER" caracter, we try to submit form
            CloudLogin.validateStep1();
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
      if(!CloudLogin.EMAIL_REGEXP.test(dataTag))
      {
        CloudLogin.displayMessage('email is not valid: "' + dataTag + '"');
        data.result = false;
        return;
      }
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
CloudLogin.sendEmail = function(email) {
  
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
          // reactivate button
          document.getElementById("t_submit").disabled = false;
          console.log("Invitation cannot be sent to [" + email + "], [Status=" + status + "]");
          //CloudLogin.displayMessage("Invitation cannot be sent to [" + email + "], [Status=" + status + "]");
          CloudLogin.EMAILS_NOK.push(email);
        }
        CloudLogin.finalizeSendEmails();
      },
      error: function(request, status, error) {
        CloudLogin.NB_EMAILS_REQUESTED++;
        // reactivate button
        document.getElementById("t_submit").disabled = false;
        console.log("Invitation cannot be sent to [" + email + "], [Status=" + status + "], [Error=" + error + "]");
        //CloudLogin.displayMessage("Invitation cannot be sent to [" + email + "], [Status=" + status + "], [Error=" + error + "]");
        CloudLogin.EMAILS_NOK.push(email);
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
    var message = CloudLogin.EMAILS_NOK.length + " email(s) cannot be sent: [ ";
    // for
    for(var i=0; i<CloudLogin.EMAILS_NOK.length; i++) {
      message = message.concat(CloudLogin.EMAILS_NOK[i]).concat(" ");
    }
    message = message.concat("]");
    CloudLogin.displayMessage(message);
  }
} 

CloudLogin.validateStep1 = function() {
  
  var emails = eval(document.getElementById("emails").value);
  
  CloudLogin.NB_EMAILS_OK = 0;
  CloudLogin.NB_EMAILS_REQUESTED = 0;
  CloudLogin.EMAILS_NOK = new Array();
  
  // deactivate button
  document.getElementById("t_submit").disabled = true;
  
  // lets pass if there isn't emails
  if(emails.length == 0) {
    CloudLogin.exit();
  }
  
  for(var i=0; i<emails.length; i++) {
    CloudLogin.sendEmail(emails[i]);
  }

  //tenants.doLogin();
}

CloudLogin.validateStep2 = function() {
  CloudLogin.showStep(3);
  
  //tenants.doLogin();
}

CloudLogin.validateStep3 = function() {
  CloudLogin.exit();
  
  //tenants.doLogin();
}

CloudLogin.exit = function() {
  $("#CloudExitForm").submit();
}

/**
 * Display red message up to input
 */
CloudLogin.displayMessage = function(message) {
  $("#messageString").text(message);
}

/**
 * Clear red message up to input
 */
CloudLogin.clearMessage = function() {
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
    console.log("CloudLogin: " + email + "is not valid mail");
 }
 return domain;
}
