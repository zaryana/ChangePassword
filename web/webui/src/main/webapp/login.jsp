<%@page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
  <head>
    <% String pageName = "Help eXo Cloud Workspace"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
  </head>
  <body onLoad="tenants.init();">
    <div class="UIPageContainer">
      <!--begin header-->
      <%@ include file="common/header.jsp"%>

	<div class="UIPageBody FormPages">
		<form class="UIForm UIFormBox SigninForm" name="cloud-workspaces-profile" id="signinForm" method="POST" action="javascript:void(0);" >
			<h1 class="TitleForm">Login to [TENANT]</h1>
			<div class="TenantFormMsg" id="messageString"></div>
			<table cols="2">
				<tbody>
					<tr>
						<td class="Field">Username:</td><td> <input type="text" onclick="this.value='';" value="" id="email" name="" class="required InputText"></td>
					</tr>
					<tr>
						<td class="Field">Password:</td><td> <input type="password" id="password" name="password" class="required InputText"><a class="ForgotPass" href="/reset-password.jsp">Forgot Password?</a></td>
					</tr>
					<tr>
						<td class="Field"></td><td> <input type="submit" onclick="tenants.doLogin();" value="Login" id="t_submit" class="Button"></td>
					</tr>
				</tbody>
			</table>
			<div class="TenantFormMsg" id="messageString"></div>
			<input type="hidden" name="vid" value="8ac8104b-6f84-48b1-ac69-32580abd80ca"><input type="hidden" name="lf_cid" value="LF_df197061">
		</form>
	</div>
	<!--end code body here-->
		
	<!--begin Footer-->
      <%@ include file="common/footer.jsp"%>  
    </div>
    

    <script type="text/javascript" src="/js/trackers.js"></script>    
  </body>
</html>
