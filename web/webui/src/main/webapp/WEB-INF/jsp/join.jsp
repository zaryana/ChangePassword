<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
	<head>
    <% String pageName = "Join Cloud Workspaces"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
	</head>
	<body onLoad="tenants.initJoinPage();">
		<!--begin header-->
    <%@ include file="common/header.jsp"%>
    
		<!--begin FormPages-->		
		 <div class="UIPageBody FormPages">
			<h1>Activate Your Cloud Workspaces Account</h1>
			<p>To participate in the Cloud Workspaces private beta, please complete your profile.</p>
			<div id="messageString" class="TenantFormMsg"></div>
			<form class="UIForm" action="javascript:void(0);"  method="POST" name="cloud-workspaces-profile">
				<table cols="2">
					<tr>
						<td class="Field">Workspace:</td><td> <input class="InputText DisabledArea" type="text" name="workspace" id="workspace" disabled value="mycompany" /></td>
					</tr>
					<tr>
						<td class="Field">Email:</td><td> <input class="InputText  DisabledArea" type="text" name="email" id="email" disabled value="your.name@yourcompany.com" /></td>
					</tr>
					<tr>
						<td class="Field">First name:</td><td> <input class="InputText" type="text" name="first_name" id="first_name" /><span class="Star">*</span></td>
					</tr>
					<tr>
						<td class="Field">Last name:</td><td> <input class="InputText" type="text" name="last_name" id="last_name" /><span class="Star">*</span></td> 
					</tr>
					<tr>
						<td class="Field">Username:</td><td> <input class="InputText DisabledArea" type="text" name="username" disabled id="username" /></td> 
					</tr>
					<tr>
						<td class="Field">Password:</td><td> <input class="InputText" type="password" name="password" id="password" /><span class="Star">*</span></td>
					</tr>
					<tr>
						<td class="Field">Confirm password:</td><td> <input class="InputText" type="password" name="password2" id="password2" /><span class="Star">*</span></td>
					</tr>
					<tr>
						<td class="Field"></td><td> <input class="Button" type="submit"  id="t_submit" value="Sign Up" onClick="tenants.doJoinRequest();" /></td>
					</tr>
				</table>
				
				<input type="hidden"  id="formid" name="formid" value="147cd324-b524-4679-bcad-5f5708ad5beb" />
				<input type="hidden" id="cid" name="cid" value="LF_df197061" />
				<iframe id="loopfuseOutput" name="loopfuseOutput" style='display:none; visibility:hidden'></iframe>
			</form>
		</div>
		
		<!--begin Footer-->	
    <%@ include file="common/footer.jsp"%>
	 </body>
</html>
