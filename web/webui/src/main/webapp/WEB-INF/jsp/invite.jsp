<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
	<head>
    <% String pageName = "Cloud-workspaces Invite"; %>
    <%@ include file="common/headStyle.jsp"%>
	</head>
	<body>
		<!--begin header-->
    <%@ include file="common/header.jsp"%>
		
		<!--begin Body-->		
		<div class="UIPageBody InviPages">
			<div class="UIFormBox ClearFix">
				<h2 class="TitleForm">Invite your friends to the Cloud-workspaces network</h2>
				<div class="FL LeftInvi">
					<p class="Title">Permissions</p>
					<ul class="PermissionsBox">
						<li class="SelectIcon"><a href="#">johnsmith@company.com</a></li>
						<li class="Normal"><a href="#">johnsmith@company.com</a></li>
						<li class="Normal"><a href="#">johnsmith@company.com</a></li>
						<li class="Normal"><a href="#">johnsmith@company.com</a></li>
					</ul>
				</div>
				<form class="Message">
					<table cols="2">
						<tr>
							<td class="Field">Email:</td>
						</tr>
						<tr>
							<td> <input class="InputText" type="text" name="" id="" value="" /></td>
						</tr>
						<tr>
							<td class="Field">Personal Message:</td>
						</tr>
						<tr>
							<td> <input class="InputText" type="text" name="" id="" value="" /></td>
						</tr>
						<tr>
							<td><input class="Button" value="Invite" type="text" /></td>
						</tr>
					</table>
				</form>
			</div>
		</div>
    
		<!--begin Footer-->
    <%@ include file="common/footer.jsp"%>
	</body>
</html>
