<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
	<head>
   <link href="css/Style.css" rel="stylesheet" type="text/css" />
	</head>
	<body>
	

<div id="fb-root"></div>
<script>(function(d, s, id) {
  var js, fjs = d.getElementsByTagName(s)[0];
  if (d.getElementById(id)) return;
  js = d.createElement(s); js.id = id;
  js.src = "https://connect.facebook.net/en_US/all.js#xfbml=1";
  fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));</script>
		<div class="UIHeader" id="UIHeader">
			<div class="MainContent">
				<ul class="UIMenuTop FR">
				  <li><a class="Tab BlueTab" href="<%=request.getContextPath()%>/about.jsp">About</a></li>
				  <li><a class="Tab GreenTab" href="http://community.exoplatform.com" target="_blank">Community</a></li>
				  <li><a class="Tab GrayTab" href="http://blog.exoplatform.com" target="_blank">Blog</a></li>
				  <li><a class="Tab OrangeTab" href="http://www.exoplatform.com" target="_blank">eXoplatform.com</a></li>
				  <li class="last"><a class="Tab OrangeTab" onclick="showContactUsForm('<%=request.getContextPath()%>/contact-us.jsp');">Contact us</a></li>
				</ul>
				<div class="Logo FL"><a href="/<%=request.getContextPath()%>"><img src="background/logo.png"/></a></div>
				<div class="ClouIntranet FR"><h1>Cloud Workspaces</h1><span>The Free Social Intranet for Your Company</span></div>
			</div>
		</div>
		<div class="MarkLayer" id="MaskLayer" style="width: 100%; display: none;"></div>
		<div id="ContactUsContainer" style="display: none;"></div> 
		
		<!--code body here-->
		
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
	<!--end code body here-->	
		
<div class="UIFooterPortlet">
			<div class="MainContent ClearFix">
				<p class="FL">Cloud Workspaces is Brought to You by <span><a href="http://www.exoplatform.com">eXo</a></span></p>
				<p class="Copyright FR">Copyright &copy; 2000-2012. All Rights Reserved, eXo Platform SAS.</p>
			</div>
		</div>

	</body>
	</html>