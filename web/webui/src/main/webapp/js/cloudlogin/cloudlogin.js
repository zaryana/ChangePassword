/**
 * Module used to manage cloud login wizard JS features
 *
 */
var CloudLogin = {};

CloudLogin.WS_SENDMAIL_URL = "/rest/invite-join-ws/send-mail/";
CloudLogin.EMAIL_REGEXP = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
CloudLogin.FLAG_EMAIL_OK = false;

CloudLogin.initCloudLogin = function() {
  // tenants.init();
  
  if (!window.console) console = {};
  console.log = console.log || function(){};
  console.warn = console.warn || function(){};
  console.error = console.error || function(){};
  console.info = console.info || function(){};
  
  // http://textextjs.com
  $('#email').textext({ 
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
  }).bind(
    'isTagAllowed', function(e, data) {
      if(!CloudLogin.EMAIL_REGEXP.test(data.tag))
      {
        alert('email not ok: ' + data.tag);
        data.result = false;
        return;
      }
    });
    
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
  
  // TODO extract hostname from email
  var hostname = "toto.com";
  var mainUrl = CloudLogin.WS_SENDMAIL_URL + email + "/" + hostname;
  
  $.ajax({
    url: mainUrl,
    success: function(status) {
      if (status.indexOf("Message sent") != -1) {
        console.log("mail ok: " + email);
        CloudLogin.FLAG_EMAIL_OK = true;
      }
      else {
        console.log("mail not ok: " + email);
        alert("mail not ok: " + email);


      }
  },
  error: function(request, status, error){
    console.log("mail not ok: " + email +" [Status: " + status + "], [Error: " + error + "]");
    alert("mail not ok: " + email +" [Status: " + status + "], [Error: " + error + "]");


  },
  dataType: 'text'
  });
}

CloudLogin.validateStep1 = function() {
  
  var emails = eval(document.getElementById("emails").value);
  
  for(var i=0; i<emails.length; i++) {
    CloudLogin.sendEmail(emails[i]);
  }
  
  //if(CloudLogin.FLAG_EMAIL_OK == true) {
    // TODO delete
    CloudLogin.showStep(2);
  //}

  //tenants.doLogin();
}

CloudLogin.validateStep2 = function() {
  CloudLogin.showStep(3);
  
  //tenants.doLogin();
}

CloudLogin.validateStep3 = function() {
  
  // Redirect to initialURI
  window.location=document.getElementById("initialURI").value;
  
  //tenants.doLogin();
}