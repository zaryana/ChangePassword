<%@page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
	<head>
    <% String pageName = "Try Again"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
    <script type="text/javascript" data-main="signup" src="/js/require-2.0.4.min.js"></script>
  </head>
  <body>
    <div class="UIPageContainer">
      <!--begin header-->
      <%@ include file="common/header.jsp"%>
  		
			<div class="UIBanner BlacklistedBanner">
				<div class="MainContent">
					<h2>Sorry!</h2>
					<p>We can't complete your registration with a personal email address. To create a private social intranet for your company, or to know which existing network to add you to, a professional email address is required.<br/><br/></p>
					<p>Enter your corporate email address below to get started with Cloud Workspaces</p>
					<div class="FormBox">
                       <form class="UIForm" action="javascript:void(0);"   method="POST" name="cloud-workspaces-profile" id="signupForm">
						 <input id="email" class="InputText" type="text" onclick="this.value='';" value="Enter your work email" name="email">
						 <input id="t_submit" class="Button" type="submit" value="Sign Up">
                       </form>
					</div>
					<p id="tryAgainNote" class="Note"></p>
				</div>
			</div>
			<div class="UIPageBodyContainer">
				<div class="UIPageBody BlacklistEmailsPage">
					<div class="Item ClearFix">
						<a class="Button FR" href="javascript:void(0);" id="contactUsButton"><img alt="" src="background/contact-us-button.png" /></a>
						<h3>Want to see Cloud Workspaces in action, but don't need your own account?</h3>
						<p>Contact our sales team for a demo.</p>
					</div>
					<div class="Item ClearFix">
						<a class="Button FR" href="http://exoplatform.com/company/en/join-exo-community" target="_blank"><img alt="" src="background/download-exoplatform.png" /></a>
						<h3>Or, download the 30-day trial of eXo Platform 3.5</h3>
						<p>Cloud Workspaces is based on eXo Platform 3.5 enterprise portal and user experience platform. To discover Cloud Workspaces social intranet features, and much more, download the on-premises edition of eXo Platform 3.5. </p>
					</div>
				</div>
			</div>
      <!--begin Footer-->
      <%@ include file="common/footer.jsp"%>
	</div>
		
	<iframe id="mktOutput" name="mktOutput" style='display:none; visibility:hidden'></iframe>
  
    <!-- Google Code for Cloud Workspaces - Adwords Conversion Conversion Page -->
    <script type="text/javascript">
      /*           */
      var google_conversion_id = 1017182568;
      var google_conversion_language = "en";
      var google_conversion_format = "3";
      var google_conversion_color = "ffffff";
      var google_conversion_label = "sQn0CPifrxIQ6PKD5QM";
      var google_conversion_value = 0;
      /*     */
    </script>
    <script type="text/javascript" src="http://www.googleadservices.com/pagead/conversion.js"></script>
    <noscript>
      <div style="display: inline;">
        <img height="1" width="1" style="border-style: none;" alt=""
          src="http://www.googleadservices.com/pagead/conversion/1017182568/?value=0&amp;label=sQn0CPifrxIQ6PKD5QM&amp;guid=ON&amp;script=0" />
      </div>
    </noscript>
  </body>
</html>
