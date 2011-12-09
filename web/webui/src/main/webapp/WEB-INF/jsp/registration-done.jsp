<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
	<head>
    <% String pageName = "Registration Request Sent"; %>
    <%@ include file="common/headStyle.jsp"%>
	</head>
	<body>
		<!--begin header-->
    <%@ include file="common/header.jsp"%>
    
		<!--begin ThanksPages-->	
		 <div class="UIPageBody ThanksPages ClearFix">
			<h1>Thank You for Your Interest</h1>
			<p style="text-align:center">We've received your request to participate in the private beta. When your Cloud Workspaces account is available, we'll notify you via email. In the meantime, you can learn more about Cloud Workspaces by checking out these helpful resources.</p>
			<div class="LeftContent">
				<h3>Watch the Getting Started video:</h3>
				<div class="Video"><img alt="" src="background/Video.jpg" /></div>
			</div>
			<div class="RightContent">
				<h3>Even more to explore:</h3>
				<a href="/page?to=about">Read an introduction to the key features of eXo Cloud Workspaces</a>
				<a href="http://community.exoplatform.com">Join the eXo Community to connect with other users, access documentation, forums and more</a>
				<a href="http://exoplatform.com/company/en/platform/exo-platform-35">Want to host your own social intranet on-premise or in a private cloud? Learn more about eXo Platform 3.5</a>
			</div>
			<center><a class="BackIcon" href="/main.jsp">Back</a></center>
		</div>
		
		<!--begin Footer-->	
    <%@ include file="common/footer.jsp"%>
	 </body>
</html>
