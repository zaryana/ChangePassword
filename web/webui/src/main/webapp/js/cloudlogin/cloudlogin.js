/**
 * Module used to manage cloud login wizard JS features
 *
 */
var CloudLogin = {};

CloudLogin.WS_SENDMAIL_URL = "/rest/invite-join-ws/send-mail/";
CloudLogin.WS_STATUS_RESPONSE_OK = "Message sent";
CloudLogin.EMAIL_REGEXP = /^([a-zA-Z0-9_\.\-])+\@((([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+)$/;
CloudLogin.NB_EMAILS_OK = 0;
CloudLogin.NB_EMAILS = 0;

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
      }
    },
    html : {
      hidden: '<input type="hidden" id="emails" />'
    }
  });
  
  textExt.bind(
    'onClick', function(e) {
      console.log("click");
    }
  );
  
  textExt.bind(
    'isTagAllowed', function(e, data) {
      var dataTag = data.tag.replace(/^\s+|\s+$/g,'');
      if(!CloudLogin.EMAIL_REGEXP.test(dataTag))
      {
        alert('email not ok: ' + dataTag);
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
          alert("Invitation cannot be sent to [" + email + "], [Status=" + status + "]");
        }
      },
      error: function(request, status, error) {
        // reactivate button
        document.getElementById("t_submit").disabled = false;
        console.log("Invitation cannot be sent to [" + email + "], [Status=" + status + "], [Error=" + error + "]");
        alert("Invitation cannot be sent to [" + email + "], [Status=" + status + "], [Error=" + error + "]");
      },
      dataType: 'text'
    });
}

CloudLogin.validateStep1 = function() {
  
  var emails = eval(document.getElementById("emails").value);
  
  CloudLogin.NB_EMAILS_OK = 0;
  CloudLogin.NB_EMAILS = emails.length;
  
  // deactivate button
  document.getElementById("t_submit").disabled = true;
  
  // TODO delete this, temporary lets pass if there isn't emails
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
