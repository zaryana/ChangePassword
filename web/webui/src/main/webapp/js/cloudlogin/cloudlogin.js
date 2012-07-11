/**
 * Module used to manage cloud login wizard JS features
 *
 */
var CloudLogin = {};

/*===========================================================================================================*
 *        FRAMEWORK METHODS
 *===========================================================================================================*/
 
CloudLogin.initCloudLogin = function(maxAvatarLength, avatarUploadId, avatarUriPath, profileWsPath) {
  if (!window.console) console = {};
  console.log = console.log || function(){};
  console.warn = console.warn || function(){};
  console.error = console.error || function(){};
  console.info = console.info || function(){};

  // Init some servers constants
  if(maxAvatarLength != undefined) {
    CloudLogin.AVATAR_MAX_LENGTH = maxAvatarLength;
  }
  if(avatarUploadId != undefined) {
    CloudLogin.AVATAR_UPLOAD_ID = avatarUploadId;
  }
  if(avatarUploadId != undefined) {
    CloudLogin.AVATAR_URI_PATH = avatarUriPath;
  }
  if(profileWsPath != undefined) {
    CloudLogin.PROFILE_WS_PATH = profileWsPath;
  }
  
  $("#email").val(CloudLogin.DEFAULT_VALUE);
  
  // Event only for IE
  document.getElementById('email').onclick = function() {document.getElementById('email').value = '';}
  
  // Get Spaces
  CloudLogin.initSpaces();
  
  CloudLogin.initUploadFile();
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
 * Do nothing
 */
CloudLogin.doNothing = function(event) {
  if(event != undefined) {
    event.preventDefault();
  }
}



/*===========================================================================================================*
 *        STEP PROFILE
 *===========================================================================================================*/
 
// Server variables
CloudLogin.AVATAR_MAX_LENGTH = 2000000; // 2Mo by default
CloudLogin.AVATAR_UPLOAD_ID = "cloudloginavatar"; // by default
CloudLogin.AVATAR_URI_PATH = "/rest/jcr/repository/collaboration/exo:applications/cloudlogin/"; // by default
CloudLogin.PROFILE_WS_PATH = "/portal/rest/cloudlogin/setavatar/"; // by default

// Static variables
CloudLogin.AVATAR_EXT_REGEXP = /^(gif|jpeg|jpg|png)$/i;

// Session variables
CloudLogin.AVATAR_FILE_NAME = "";
CloudLogin.PROFILE_FIRST_NAME = "";
CloudLogin.PROFILE_LAST_NAME = "";
CloudLogin.PROFILE_POSITION = "";
CloudLogin.ERROR_MESSAGE = "";

CloudLogin.showStepProfile = function(event) {
  if(event != undefined) {
    event.preventDefault();
  }
  
  $('#StepSpace').hide();
  $('#StepEmail').hide();
  $('#StepProfile').show();
}

CloudLogin.initUploadFile = function() {

  // Set event onto link and browse button about avatar
  /*$("#linkAvatar").click(function(event) {
    event.preventDefault();
    $("#datafile").trigger('click');
  });
  $("#browseAvatar").click(function(event) {
    event.preventDefault();
    $("#datafile").trigger('click');
  });*/

  $(document).ready(function() {
    $('#datafile').fileupload({
      dropZone: $('#fileDropZone'),
      url: "/portal/rest/cloudlogin/uploadavatar?uploadId=cloudloginavatar",
      type: "POST",
      // Redefine add method to filter with size and file type
      add: function (e, data) {
        var fileOk = true;
        var file = data.files[0];
        var ext = CloudLogin.getFileExtension(file.name);
        
        CloudLogin.lockProfile();
        
        if(file.name == CloudLogin.AVATAR_FILE_NAME) {
          fileOk = false;
          console.log("File (" + file.name + ") is yet selected.");
        }
        else {
          if(file.size && file.size >= CloudLogin.AVATAR_MAX_LENGTH) {
            fileOk = false;
            console.log("File size is too large (" + file.size + " > " + max_size + ")");
          }
          if(! CloudLogin.AVATAR_EXT_REGEXP.test(ext)) {
            fileOk = false;
            console.log("File is not in proper format (." + ext + ")");
          }
        }
        
        if(fileOk) {
          data.submit();
        }
        else {
          CloudLogin.unlockProfile();
        }
      },
      done: function (e, data) {
        console.log("upload done !");
        // Upload is done we update uri of image
        var imgUri = CloudLogin.AVATAR_URI_PATH + "/" + data.files[0].name;
        // Save file name
        CloudLogin.AVATAR_FILE_NAME = data.files[0].name;
        // Display avatar
        $("#avatarImage").attr("src", imgUri);
      },
      fail: function (e, data) {
        console.log("upload fails !");
      },
      always: function (e, data) {
        console.log("upload always !");
        CloudLogin.unlockProfile();
      }
    });
  });
}

CloudLogin.validateStepProfile = function(event) {
  if(event != undefined) {
    event.preventDefault();
  }
  
  var firstNameProfile = $("#firstNameProfile").val();
  var lastNameProfile = $("#lastNameProfile").val();
  var posProfile = $("#posProfile").val();
  
  if(firstNameProfile != CloudLogin.PROFILE_FIRST_NAME) {
    CloudLogin.PROFILE_FIRST_NAME = firstNameProfile;
  }
  else {
    firstNameProfile = ""; // we cannot resubmit this data, this is same as previous submit
  }
  
  if(lastNameProfile != CloudLogin.PROFILE_LAST_NAME) {
    CloudLogin.PROFILE_LAST_NAME = lastNameProfile;
  }
  else {
    lastNameProfile = ""; // we cannot resubmit this data, this is same as previous submit
  }
  
  if(posProfile != CloudLogin.PROFILE_POSITION) {
    CloudLogin.PROFILE_POSITION = posProfile;
  }
  else {
    posProfile = ""; // we cannot resubmit this data, this is same as previous submit
  }
  
  // Update only we have new datas
  if((firstNameProfile != undefined && firstNameProfile != "") 
  || (lastNameProfile != undefined && lastNameProfile != "") 
  || (posProfile != undefined && posProfile != "") 
  || (CloudLogin.AVATAR_FILE_NAME != undefined && CloudLogin.AVATAR_FILE_NAME != "")) {
    // Update profile with avatar and others datas
    CloudLogin.updateProfile(CloudLogin.AVATAR_FILE_NAME, firstNameProfile, lastNameProfile, posProfile);
  }
  else {
    console.log("There is no data to send.");
    CloudLogin.finalizeUpdateProfile();
  }
}

CloudLogin.finalizeUpdateProfile = function() {
  if(CloudLogin.ERROR_MESSAGE == "") {
    // Clean data avatar
    CloudLogin.AVATAR_FILE_NAME = "";
  
    // Show next step
    CloudLogin.showStepSpace();
  }
  else {
    alert(CloudLogin.ERROR_MESSAGE);
  }
}

CloudLogin.updateProfile = function(fileName, firstNameProfile, lastNameProfile, posProfile) {
  $.ajax({
    type: "GET",
    url: CloudLogin.PROFILE_WS_PATH,
    data: {fileName: fileName, uploadId: CloudLogin.AVATAR_UPLOAD_ID, firstName: firstNameProfile, lastName: lastNameProfile, position: posProfile},
    success: function(data, textStatus) {
      console.log("profile updated [status=" + textStatus + "]");
      CloudLogin.finalizeUpdateProfile();
    },
    error: function(data, textStatus, error) {
      console.log("profile update in error: [status=" + textStatus + "], [error=" + error + "]");
      CloudLogin.finalizeUpdateProfile();
    }
  });
}

/**
 * lock profile step and update button if is necessary
 */
CloudLogin.lockProfile = function() {
  // lock button Next
  document.getElementById("t_submit_profile").disabled = true;
}

/**
 * unlock profile step and update button if is necessary
 */
CloudLogin.unlockProfile= function() {
  // Unlock button Next
  document.getElementById("t_submit_profile").disabled = false;
}


/*===========================================================================================================*
 *        STEP SPACES
 *===========================================================================================================*/
 
CloudLogin.WS_SPACES_URL = "rest/gadgets/spaces/public/",
CloudLogin.WS_JOIN_SPACE_URL = "rest/gadgets/spaces/join/";
CloudLogin.WS_JOIN_RESPONSE_OK = "Join";
CloudLogin.SPACES_SELECTED = new Array();
CloudLogin.NB_SPACES_OK = 0;
CloudLogin.NB_SPACES_REQUESTED = 0;
CloudLogin.SPACES_BUTTON_DEFAULT_LABEL = "Next";
CloudLogin.SPACES_BUTTON_DEFAULT_WIDTH = "100px";
 
CloudLogin.showStepSpace = function() {
  $('#StepProfile').hide();
  $('#StepEmail').hide();
  $('#StepSpace').show();
}

/**
 * Init spaces. Execute an ajax request to get all spaces the user can join and display it into dom.
 */
CloudLogin.initSpaces = function() {
  var mainUrl = CloudLogin.WS_SPACES_URL;
  
  CloudLogin.lockSpaces();
  
  // Clean selected spaces
  CloudLogin.SPACES_SELECTED = [];
  
  // Clean list
  $('.FL').remove();
  
  $.ajax({
    url: mainUrl,
    success: function(spaces) {
      var listSpaces = $('#SpacesContent');
      var liSpace = inputSpace = undefined;
    
      // Fetch all steps to create list
      $.each(spaces, function(i, space) {
        inputSpace = $(document.createElement('input')).attr({type: 'checkbox', id: space.spaceId}).click(CloudLogin.toggleSpace);
        if(i % 2 === 0) {
          liSpace = $(document.createElement('li')).addClass('FL');
          liSpace.append(inputSpace).append(space.displayName + ' <br />');
        }
        else {
          liSpace.append(inputSpace).append(space.displayName);
        }
        listSpaces.append(liSpace);
      });
      
      CloudLogin.unlockSpaces();
      
      console.info("Spaces initialized !");
    },
    error: function(request, status, error) {
      console.log("Cannot get spaces [Status=" + status + "], [Error=" + error + "]");
      CloudLogin.unlockSpaces();
    },
    dataType: 'json'
  });
}

/**
 * Validate this step "Spaces", Try to join current user to spaces selected. If there is no spaces selected, Go to next step
 */
CloudLogin.validateStepSpace = function(event) {
  if(event != undefined) {
    event.preventDefault();
  }

  CloudLogin.NB_SPACES_OK = 0;
  CloudLogin.NB_SPACES_REQUESTED = 0;
  
  CloudLogin.lockSpaces();

  if(CloudLogin.SPACES_SELECTED.length > 0) {
    // Update text and width of button
    $("#t_submit_space").width("120px");
    $("#t_submit_space").val("Joining...");
  
    for(var i=0; i<CloudLogin.SPACES_SELECTED.length; i++) {
      CloudLogin.joinSpace(CloudLogin.SPACES_SELECTED[i]);
    }
  }
  else {
    CloudLogin.showStepEmail();
  }
}

/**
 * Ajax request to join space
 */
CloudLogin.joinSpace = function(spaceId) {
  var mainUrl = CloudLogin.WS_JOIN_SPACE_URL + spaceId;

  $.ajax({
    url: mainUrl,
    success: function(response) {
      CloudLogin.NB_SPACES_REQUESTED++;
      if(response.indexOf(CloudLogin.WS_JOIN_RESPONSE_OK) != -1) {
        console.log("OK space " + spaceId + " is joined");
        CloudLogin.NB_SPACES_OK++;
      }
      else {
        console.log("Cannot join space " + spaceId + " is not joined: [" + response + "]");
      }
      CloudLogin.finalizeJoinSpaces();
    },
    error: function(request, status, error) {
      CloudLogin.NB_SPACES_REQUESTED++;
      console.log("Cannot join space [Status=" + status + "], [Error=" + error + "]");
      CloudLogin.finalizeJoinSpaces();
    },
    dataType: 'text'
  });
}

/**
 * Called after all spaces are joined (or not)
 */
CloudLogin.finalizeJoinSpaces = function() {

  // Case of Joining is finish but there is some spaces in error
  if(CloudLogin.NB_SPACES_REQUESTED == CloudLogin.SPACES_SELECTED.length) {
    if(CloudLogin.NB_SPACES_OK < CloudLogin.SPACES_SELECTED.length) {
      console.log("Some spaces cannot be joined by user");
      CloudLogin.unlockSpaces();
      CloudLogin.initSpaces();
    }
    else {
      console.log("All spaces are joined by user");
      CloudLogin.unlockSpaces();
      CloudLogin.initSpaces();
      CloudLogin.showStepEmail();
    }
  }
}

/**
 * This method permit to select a space or unselect in memory and update button "Next"
 */
CloudLogin.toggleSpace = function(event) {
  var idSpace = event.target.id;
  
  if(event.target.checked == true) {
    CloudLogin.SPACES_SELECTED.push(idSpace);
  }
  else {
    CloudLogin.removeByValue(CloudLogin.SPACES_SELECTED, idSpace);
  }
  
  CloudLogin.updateButtonJoin();
}

/**
 * Update text and size of button of this step "Spaces"
 */
CloudLogin.updateButtonJoin = function() {

  // Update button
  var nbSpaces = CloudLogin.SPACES_SELECTED.length;
  var textSpaces = "";
  var textSpacesWidth = CloudLogin.SPACES_BUTTON_DEFAULT_WIDTH;
  if(nbSpaces > 1) {
    textSpaces = "Join " + nbSpaces + " spaces";
    textSpacesWidth = "130px";
  }
  else if(nbSpaces > 0) {
    textSpaces = "Join " + nbSpaces + " space";
    textSpacesWidth = "120px";
  }
  else {
    textSpaces = "Next";
    textSpacesWidth = CloudLogin.SPACES_BUTTON_DEFAULT_WIDTH;
  }
  
  $("#t_submit_space").width(textSpacesWidth);
  $("#t_submit_space").val(textSpaces);
}

/**
 * lock spaces and update button if is necessary
 */
CloudLogin.lockSpaces = function() {
  
  // lock button Next
  document.getElementById("t_submit_space").disabled = true;
}

/**
 * unlock spaces and update button if is necessary
 */
CloudLogin.unlockSpaces = function() {
  
  // delete Load image
  $('#SpacesLoader').hide();
  $('#SpacesContainer').show();

  // Unlock button Next
  document.getElementById("t_submit_space").disabled = false;
  
  CloudLogin.updateButtonJoin();
}


/*===========================================================================================================*
 *        STEP EMAIL
 *===========================================================================================================*/

CloudLogin.WS_SENDMAIL_URL = "/rest/invite-join-ws/send-mail/";
CloudLogin.WS_STATUS_RESPONSE_OK = "Message sent";
CloudLogin.EMAIL_REGEXP = /^([a-zA-Z0-9_\.\-])+\@((([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+)$/;
CloudLogin.NB_EMAILS_OK = 0;
CloudLogin.NB_EMAILS_REQUESTED = 0;
CloudLogin.NB_EMAILS = 0;
CloudLogin.ERROR_MESSAGES = new Array();
CloudLogin.DEFAULT_VALUE = "Add email addresses";


/**
 * Initialize jquery module textext (http://textextjs.com)
 */
CloudLogin.initTextExt = function() {
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

/**
 * Show step Email
 */
CloudLogin.showStepEmail = function() {
  $('#StepProfile').hide();
  $('#StepSpace').hide();
  $('#StepEmail').show();
  
  // Init textExt just before displaying
  CloudLogin.initTextExt();
}

/**
 * Validate datas from step Email
 */
CloudLogin.validateStepEmail = function(event) {
  if(event != undefined) {
    event.preventDefault();
  }
  
  CloudLogin.NB_EMAILS_OK = 0;
  CloudLogin.NB_EMAILS_REQUESTED = 0;
  CloudLogin.ERROR_MESSAGES = [];
  
  var emails = eval(document.getElementById("emails").value);
  var emailNotTagged = document.getElementById("email").value;
  
  // only if there is no tags and this is not default value
  if(emails.length == 0 && CloudLogin.DEFAULT_VALUE != emailNotTagged) {
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
  else if(emails.length > 0) {
    $("#t_submit_email").val("Sending...");
  }
  
  // deactivate button
  document.getElementById("t_submit_email").disabled = true;
  
  // lets pass if there isn't emails
  if(emails.length == 0) {
    CloudLogin.exit();
  }
  
  for(var i=0; i<emails.length; i++) {
    CloudLogin.sendEmail(emails[i], i);
  }
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

/**
 * Update button Send. At the first button is "Skip", after it is "Send (x)" x is number of mails
 */
CloudLogin.updateSendButton = function() {
  if(CloudLogin.NB_EMAILS > 0) {
    $("#t_submit_email").val("Send (" + CloudLogin.NB_EMAILS + ")");
  }
  else {
    $("#t_submit_email").val("Skip");
  }
  
  // reactivate button
  document.getElementById("t_submit_email").disabled = false;
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



/*===========================================================================================================*
 *        UNITARY METHODS
 *===========================================================================================================*/
 
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

/**
 * Remove an element by his value
 */
CloudLogin.removeByValue = function(arr, val) {
  for(var i=0; i<arr.length; i++) {
    if(arr[i] == val) {
      arr.splice(i, 1);
      break;
    }
  }
}

CloudLogin.getFileExtension = function(fileName) {
  if(fileName != null && fileName != undefined) {
    return fileName.split('.').pop();
  }
}
