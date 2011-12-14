<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
	<head>
    <% String pageName = "Cloud-workspaces Contact Us"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
    <script language="javascript" type="text/javascript">
     function SendData(){
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
     // hideForm();
     // document.getElementById('Content').innerHTML = "<div class=\"ThanksPages ClearFix\"><h1>Thank you!</h1><p style=\"text-align:center\">Your request has been successfully submitted. We will get back to you soon.</p></div>";
     window.location = "/contact-us-done.jsp";
      });
    }

    function hideForm(){
     document.getElementById("MarkLayer").style.display="none";
     document.getElementById("ContactForm").style.display="none";
     document.getElementById("Content").style.display="block";
    }
    
    function cancel() {
      window.location = "/index.jsp";
    }

    </script>
	</head>
	<body>
		<div class="MarkLayer" id="MarkLayer" style="width: 100%; height: 630px;"><span></span></div>
		<!--begin header-->
    <%@ include file="common/header.jsp"%>
		
		<div class="UIPageBody" id="Content" style="display:none;"></div>
		<div class="UIPageBody ContactPages" id="ContactForm">
		<form class="UIForm UIFormBox" action="javascript:void(0);"  method="POST" name="">
		<h1 class="TitleForm">Contact Us</h1>
		<table cols="2">
		  <tr>
		   <td class="Field">Your name:</td><td> <input class="InputText" type="text" name="name" id="name" value="" /></td>
		 </tr>
		 <tr>
		  <td class="Field">Your email:</td><td> <input class="InputText" type="text" name="email" id="email" value="" /></td>
		 </tr>
		 <tr>
		  <td class="Field">subject:</td><td> <input class="InputText" type="text" name="subject" id="subject" /></td>
		 </tr>
		 <tr>
		  <td class="Field">Message:</td><td> <textarea type="text" name="ContactUs_Message__c" id="ContactUs_Message__c"></textarea></td>
		 </tr>
		 <tr>
		  <td class="Field"></td>
		  <td>
		    <input class="Button" type="submit"  id="" value="Send" onClick="SendData();" />
		    <input class="Button ButtonGray" type="" id="" value="Cancel" onClick="cancel();" />
		  </td>
		 </tr>
		</table>
		<input type="hidden" name="service_source" id="service_source" value="Cloud-Workspaces" />
		<input type="hidden"  id="formid" name="formid" value="147cd324-b524-4679-bcad-5f5708ad5beb" />
		<input type="hidden" id="cid" name="cid" value="LF_df197061" />
		</form>
		</div>
		
		<!--begin Footer-->
    <%@ include file="common/footer.jsp"%>
    <iframe id="loopfuseOutput" name="loopfuseOutput" style='display:none; visibility:hidden'></iframe>
	</body>
</html>
