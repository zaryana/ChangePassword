<%@page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
	<head>
    <% String pageName = "Try Again"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
  </head>
  <body onLoad="tenants.init();">
    <div class="UIPageContainer">
      <!--begin header-->
      <%@ include file="common/header.jsp"%>
  		
			<div class="UIBanner BlacklistedBanner">
				<div class="MainContent">
					<h2>Sorry!</h2>
					<p>We can't complete your registration with a personal email address. To create a private social intranet for your company, or to know which existing network to add you to, a professional email address is required.<br/><br/></p>
					<p>Enter your corporate email address below to get started with Cloud Workspaces</p>
					<div class="FormBox">
						<input id="email" class="InputText" type="text" onclick="this.value='';" value="Enter your work email" name="email">
						<input id="t_submit" class="Button" type="submit" onclick="tenants.doSingupRequest();" value="Sign Up">
					</div>
					<p class="Note">Sorry, we really need a company email address.</p>
				</div>
			</div>
			<div class="UIPageBody BlacklistEmailsPage">
				<div class="Item ClearFix">
					<a class="Button FR" href="javascript:void(0);" onclick="showContactUsForm('/contact-us.jsp');"><img alt="" src="background/contact-us-button.png" /></a>
					<h3>Want to see Cloud Workspaces in action, but don't need your own account?</h3>
					<p>Contact our sales team for a demo.</p>
				</div>
				<div class="Item ClearFix">
					<a class="Button FR" href="http://www.exoplatform.com/company/en/products"><img alt="" src="background/download-exoplatform.png" /></a>
					<h3>Or, download the 30-day trial of eXo Platform 3.5</h3>
					<p>Cloud Workspaces is based on eXo Platform 3.5 enterprise portal and user experience platform. To discover Cloud Workspaces social intranet features, and much more, download the on-premises edition of eXo Platform 3.5. </p>
				</div>
			</div>
      <!--begin Footer-->
      <%@ include file="common/footer.jsp"%>
	</div>
		
	<iframe id="mktOutput" name="mktOutput" style='display:none; visibility:hidden'></iframe>
    <script type="text/javascript" src="/js/mktFormSupport.js"></script>
    <script type="text/javascript" src="/js/trackers.js"></script>
  </body>
</html>
