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
		
	<div class="UIPageBodyContainer">
		<div class="UIPageBody MobileApplePages">
			<h1>eXo Platform 3.5 Mobile Apps</h1>
			<div class="ClearFix">
				<a href="#" class="FR Iphone"><img alt="" width="200" src="background/Iphone.png"/></a>
				
				<p class="Line_bt"><strong>Connect and collaborate on the go. </strong>Native iPhone, iPad, and Android apps integrate easily and securely with sites built on eXo Platform 3.5. Follow the latest updates from your colleagues and groups, interact with your personal dashboards, browse and edit your files, and more – at any time, from anywhere.</p>
				<div class="ClearFix Content">
					<a class="FL PDTop" href="url"><img alt="" src="background/ForumMB.png"/></a>
					<p class="Right_MB"><strong class="FontMobileCenter"> News Feed:</strong> receive messages from coworkers, follow your group's latest activities, and collaborate in real-time. You can also see when changes are made to the wiki pages, forums, calendars or documents you consider essential to your work. </p>
				</div>
				<div class="ClearFix Content">
					<a class="FL PDTop" href="url"><img alt="" src="background/GraphMB.png"/></a>
					<p class="Right_MB"><strong class="FontMobileCenter"> Dashboards:</strong> your personalized dashboards, which display the content and resources most useful in your social intranet, are automatically available on your mobile apps. Custom gadgets built in eXo Platform's IDE can further extend the rich dashboard experience on your mobile device.</p>
				</div>
				<div class="ClearFix Content">
					<a class="FL PDTop" href="url"><img alt="" src="background/DocMB.png"/></a>
					<p class="Right_MB"><strong class="FontMobileCenter"> Documents:</strong> easily access your documents using your mobile device's native document viewers, for a more user-friendly experience. eXo Platform 3.5's document management capabilities synchronize user permissions across the social intranet. </p>
				</div>
				<div class="ClearFix Content">
					<a class="FL PDTop" href="url"><img alt="" src="background/ProfileMB.png"/></a>
					<p class="Right_MB"><strong class="FontMobileCenter"> ecure Access:</strong> using single sign on (SSO), eXo Platform 3.5 mobile apps ensure the security of mobile access to enterprise information.</p>
				</div>
				
				<div class="ClearFix Content">
					<a class="FL PDTop" href="url"><img alt="" src="background/ToolMB.png"/></a>
					<p class="Right_MB"><strong class="FontMobileCenter"> Familiar User Experience:</strong> designed to leverage the unique features of iPhone, iPad and Android devices, eXo Platform 3.5 native mobile apps lets you access and interact with your social intranet in a familiar environment.</p>
				</div>
				
				<div class="ClearFix Content">
					<a class="FL PDTop" href="url"><img alt="" src="background/VoteMB.png"/></a>
					<p class="Right_MB"><strong class="FontMobileCenter"> Upload Photos:</strong> share a snapshot of a recent whiteboard session or a new product prototype with your coworkers - your images can be added to your eXo Platform 3.5 social intranet directly from your device.</p>
				</div>
			</div>
		</div>
	</div>	
	<!--end code body here-->	
		
<!--begin Footer-->
      <%@ include file="common/footer.jsp"%>  
    </div>
    
    <!-- BEGIN: LOOPFUSE TRACKING -->
    <script type="text/javascript" src="http://lfov.net/webrecorder/js/listen.js"></script>
    <!-- END: LOOPFUSE TRACKING -->
    <script type="text/javascript" src="/js/trackers.js"></script>    
  </body>
</html>
