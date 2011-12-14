<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
	<head>
    <% String pageName = "Cloud-workspaces login"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
	</head>
	<body onLoad="tenants.initSignInPage();">
		<!--begin header-->
    <%@ include file="common/header.jsp"%>
    
		<!--FormPages-->		
		 <div class="UIPageBody FormPages">
			<h1>Cloud Workspaces: Sign In</h1>
			<div id="messageString" class="TenantFormMsg"></div>
			<form class="UIForm" action="javascript:void(0);"  method="POST" name="cloud-workspaces-profile">
				<table cols="2">
					<tr>
						<td class="Field">Email:</td><td> <input class="InputText" type="text" name="email" id="email"  value="yourname@yourcompany.com" onclick="this.value='';" /><span class="Star">*</span></td>
					</tr>
						<td class="Field">Password:</td><td> <input class="InputText" type="password" name="password" id="password" /><span class="Star">*</span></td>
					</tr>
					<tr>
						<td class="Field">Workspace:</td><td> <input class="InputText" type="text" name="workspace" id="workspace" value="mycompany" onclick="this.value='';" /><span class="Star">*</span></td>
					</tr>
					<tr>
						<td class="Field"></td><td> <div id="" class="info">If you don't remember your workspace name, look in your registration mailbox.</div></td>
					</tr>
					<tr>
						<td class="Field"></td><td> <input class="Button" type="submit"  id="t_submit" value="Submit" onClick="tenants.doLogin();" /></td>
					</tr>
				</table>
				<div id="messageString" class="TenantFormMsg"></div>
			</form>
		</div>
		
		<!--begin Footer-->	
    <%@ include file="common/footer.jsp"%>
	 </body>
</html>
