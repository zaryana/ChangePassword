<%@page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
	<head>
    <% String pageName = "Create Your Cloud Workspace"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
	</head>
	<body onLoad="tenants.initRegistrationPage();">
		<!--begin header-->
    <%@ include file="common/header.jsp"%>
    
		<!--begin FormPages-->	
		<div class="UIPageBody UIChangePassword">
			<form class="UIFormBox" method="POST">
				<h1 class="TitleForm">Change Password</h1>
				<div class="TenantFormMsg" id="messageString"></div>
				<table>
					<tbody>
						<tr>
							<td class="Field">&nbsp;</td><td><input class="required InputText  DisabledArea" type="text" disabled="disabled" value="your.name@yourcompany.com" /></td>
						</tr>
						<tr>
							<td class="Field">New Password:</td><td><input type="password" id="password" name="password" class="required InputText" /></td>
						</tr>
						<tr>
							<td class="Field">Confirm New Password:</td><td><input type="password" id="password" name="re-password" class="required InputText" /></td>
						</tr>
						<tr>
							<td class="Field">&nbsp;</td>
							<td>
								<input type="submit" value="Submit" class="Button" />
							</td>
						</tr>
					</tbody>
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
