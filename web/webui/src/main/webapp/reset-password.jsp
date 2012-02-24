<%@page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
	<head>
    <% String pageName = "Reset password"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
	</head>
	<body onLoad="tenants.init();">
		<!--begin header-->
    <%@ include file="common/header.jsp"%>
    
		<!--begin FormPages-->		
		 <div class="UIPageBody UIResetPassword">
			<form class="UIFormBox" action="javascript:void(0);" id="resetForm" method="POST">
				<h1 class="TitleForm">Reset Password</h1>
				<div class="TenantFormMsg" id="messageString"></div>
				<table>
					<tr>
						<td class="Field">Your Email:</td><td><input class="required InputText" id="email" type="text" value="Enter Your Email to Reset Your Password" onclick="this.value='';" /><span class="Star">*</span></td>
					</tr>
					<tr>
						<td class="Field">&nbsp;</td><td><input class="Button" type="submit" id="submitButton" value="Change my password" onClick="tenants.doReset();" /></td>
					</tr>
				</table>
			</form>
		 </div>
		
		<!--begin Footer-->	
    <%@ include file="common/footer.jsp"%>
    <!-- BEGIN: LOOPFUSE TRACKING -->
     <script type="text/javascript" src="http://lfov.net/webrecorder/js/listen.js"></script>
    <!-- END: LOOPFUSE TRACKING -->
     <script type="text/javascript" src="/js/trackers.js"></script>
	 </body>
</html>
