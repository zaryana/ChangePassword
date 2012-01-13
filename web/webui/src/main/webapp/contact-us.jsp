<%@page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
	<head>
    <% String pageName = "Cloud-workspaces Contact Us"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
	</head>
	<body>
		<div class="UIPageBody" id="Content" style="display:none;"></div>
		<div class="UIPageBody ContactPages" id="ContactForm" style="margin: 0px auto; min-height: 0px;">
		<form class="UIForm UIFormBox" action="javascript:void(0);"  method="POST" name="" id="mycontactForm" style="top: 66px;">
		<h1 class="TitleForm">Contact Us</h1>
		<div id="messageString" class="TenantFormMsg"></div>
		<table cols="2">
		  <tr>
		   <td class="Field">Your name:</td><td> <input class="required InputText" type="text" name="name" id="name" value="" /></td>
		 </tr>
		 <tr>
		  <td class="Field">Your email:</td><td> <input class="required InputText" type="text" name="email" id="email" value="" /></td>
		 </tr>
		 <tr>
		  <td class="Field">subject:</td><td> <input class="required InputText" type="text" name="subject" id="subject" /></td>
		 </tr>
		 <tr>
		  <td class="Field">Message:</td><td> <textarea class="required" type="text" name="ContactUs_Message__c" id="ContactUs_Message__c"></textarea></td>
		 </tr>
		 <tr>
		  <td class="Field"></td>
		  <td>
		    <input class="Button" type="submit"  id="" value="Send" onClick="tenants.doContactRequest();" />
		    <input class="Button ButtonGray" type="" id="" value="Cancel" onClick="$('#MaskLayer').hide(); $('#ContactUsContainer').hide();" />
		  </td>
		 </tr>
		</table>
		<input type="hidden" name="service_source" id="service_source" value="Cloud-Workspaces" />
		<input type="hidden"  id="formid" name="formid" value="147cd324-b524-4679-bcad-5f5708ad5beb" />
		<input type="hidden" id="cid" name="cid" value="LF_df197061" />
		</form>
		</div>
		
    <iframe id="loopfuseOutput" name="loopfuseOutput" style='display:none; visibility:hidden'></iframe>
    <!-- BEGIN: Google Analytics TRACKING -->
      <script type="text/javascript" src="http://www.google-analytics.com/ga.js"></script>
    <!-- END: Google Analytics TRACKING -->
    <!-- BEGIN: LOOPFUSE TRACKING -->
      <script type="text/javascript" src="http://lfov.net/webrecorder/js/listen.js"></script>
    <!-- END: LOOPFUSE TRACKING -->
      <script type="text/javascript" src="/js/trackers.js"></script>
	</body>
</html>
