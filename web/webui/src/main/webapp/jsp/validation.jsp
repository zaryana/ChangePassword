<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
	<head>
    <% String pageName = "Cloud-workspaces Invite"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
    <script type="text/javascript" src="/js/json2.js"></script>
	</head>
	<body onLoad="tenants.initValidationPage();">
		<!--begin header-->
    <%@ include file="common/header.jsp"%>
		
		<!--begin Body-->		
		<div class="UIPageBody InviPages" style="width:1200px;">
		<div id="messageString" class="TenantFormMsg"></div>
			<div class="UIFormBox ClearFix" style="width:1200px;">
				<h2 class="TitleForm">Validate cloud workspaces.</h2>
					<table cols="6" id="ListTable" width="100%" style="display:none;">
						<tr>
							<td class="MyFieldCenter">Tenant name:</td><td class="MyFieldCenter">Requestor:</td><td class="MyFieldCenter">Date:</td><td class="MyFieldCenter">Company:</td><td class="MyFieldCenter">Phone:</td><td class="MyFieldCenter">Action:</td>
						</tr>
					</table>
					<form name="validationForm" id="validationForm" method="POST" action="javascript:void(0);">
					 <table id="validationTable" cols="2">
					    <tr>
					      <td class="MyField">Manager username:</td><td class="MyField"><input type="text" id="v_username"></td>
					   </tr>
					   <tr>
					      <td class="MyField">Manager password:</td><td class="MyField"><input type="password" id="v_pass"></td>
					   </tr>
					   <tr>
					      <td class="MyField">&nbsp;</td><td class="MyField"><input type="Submit" onClick="tenants.validationLogin();"></td>
					   </tr>
					 </table>
					</form>
			</div>
		</div>
		<!--begin Footer-->	
    <%@ include file="common/footer.jsp"%>
	</body>
</html>
