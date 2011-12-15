<%@page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
	<head>
    <% String pageName = "Welcome to Cloud Workspaces"; %>
    <%@ include file="common/headStyle.jsp"%>
	</head>
	<body>
		<!--begin header-->
    <%@ include file="common/header.jsp"%>
		
		<!--begin ThanksPages-->	
		 <div class="UIPageBody ThanksPages ClearFix">
			<h1>Welcome to Tenant-name Workspace</h1>
			<p class="FillProfileInfo">Get started with Cloud Workspaces by checking out these helpful resources.</p>
			<div class="LeftContent FL">
				<h3>Watch the Getting Started video:</h3>
				<div class="Video"><img alt="" src="background/ComingVideo.jpg" /></div>
			</div>
			<div class="RightContent FL">
				<h3>Even more to explore:</h3>
				<a href="/about.jsp">Read an introduction to the key features of eXo Cloud Workspaces</a>
				<a href="http://community.exoplatform.com">Join the eXo Community to connect with other users, access documentation, forums and more</a>
				<a href="http://exoplatform.com/company/en/platform/exo-platform-35">Want to host your own social intranet on-premise or in a private cloud? Learn more about eXo Platform 3.5</a>
			</div>
			<center><a class="BackIcon" href="/index.jsp">Back</a></center>
		</div>
		
		<!--begin Footer-->	
    <%@ include file="common/footer.jsp"%>
	 </body>
</html>
