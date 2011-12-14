<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">


	<head>
    <% String pageName = "Sign Up to Cloud Workspaces"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
	</head>
  
  
  
	<body onLoad="tenants.init();">
  
		<!--begin header-->
    <%@ include file="common/header.jsp"%>
    
		<!--begin banner-->		
		<div class="UIBanner">
			<div class="MainContent" >
				<h2>Sign up for your free social intranet</h2>
				<div class="FormContainer ClearFix">
					<div class="FR BoxText">
						<h3>Features</h3>
						<ul>
							<li><a href="#Connect-Collaborate">Connect and collaborate in a private enterprise social network</a> </li>
							<li><a href="#Status-Update">More than status updates: built-in wiki, forum and content management tools</a></li>
							<li><a href="#Customize-Social">Build gadgets with eXo Cloud IDE to customize your dashboards</a></li>
						</ul>
						<h3>Resources</h3>
						<p><a href="http://www.exoplatform.com/company/en/resource-center" target="_blank">Take a video tour, try some tutorials or browse the documentation.</a></p>
						<h3>Get Help</h3>
						<p><a href="http://community.exoplatform.org" target="_blank">Ask a question in the forum, submit your feedback, or contact our sales team.	</a></p>
					</div>
					<div class="FormBox">
						<div class="BoxContent">
							
							<form class="UIForm" action="javascript:void(0);"   method="POST" name="cloud-workspaces-profile">
								<p class="SignUpInfo">eXo Cloud Workspaces is currently in private beta. To participate, register with your work email address. We'll keep you updated when your company's social intranet is ready.</p>
								<div id="messageString" class="TenantFormMsg"></div>
								<input class="InputText" type="text" name="email" id="email" value="Enter your professional email" onclick="this.value='';" />
								<input class="Button" type="submit" id="t_submit"  value="Sign Up" onClick="tenants.doSingupRequest();" />
								<input type="hidden" id="formid" name="formid" value="147cd324-b524-4679-bcad-5f5708ad5beb" />
								<input type="hidden" id="cid" name="cid" value="LF_df197061" />
								<div id="signin"><a class="SignIn" href="/jsp/signin.jsp">Already registered? Sign In</a></div>
								<iframe id="loopfuseOutput" name="loopfuseOutput" style='display:none; visibility:hidden'></iframe>
							</form>
							<div class="Social FR">
								<a href="http://twitter.com/exoplatform" class="twitterLink" target="_blank"><img src="background/icon-twiter.png" alt=""/></a>
								<a href="#"><img src="background/icon-tweet.png" alt=""/></a>
							</div>						
						</div>
					</div>
				</div>
			</div>
		</div>
		
		<!--begin IntroBox-->		
		<div class="UI-Intro">
			<div class="MainContent ClearFix" >
				<div class="Cols LCol FL">
					<h3>About eXo Cloud Workspaces</h3>
					<p><b>eXo Cloud Workspaces</b> is a private social intranet that makes it easy to connect and collaborate with your coworkers.</p> 
					<p>Going beyond simple status updates, eXo Cloud Workspaces provides tools that let you capture, organize and act on your team's internal knowledge, from a full-featured enterprise wiki, rich content management system, forums, chat and more.</p>
					<a href="/page?to=about" class="Readmore">Learn more</a>
				</div>
				<div class="Cols MCol FL">
					<h3>What is a Social Intranet?</h3>
					<a href=""><img src="background/img_video.png" alt=""/></a>
				</div>
				<div class="Cols RCol FR">
					<ul>
						<li><a href="http://exoplatform.com/company/en/products/mobile"><img src="background/Stay-connect-mobile-apps.png" alt="Stay Connected with Mobile Apps for Cloud Workspaces" /></a> </li>
						<li><a href="http://exoplatform.com/exo-platform-3-trial/eXoPlatform-3.zip"><img src="background/SocialIntranet-on-premise.png" alt="Want an On-Premise Social Intranet?" /></a></li>
					</ul>
				</div>
			</div>
		</div>
		
		<!--IndexPage-->	
		 <div class="UIPageBody IndexPage">
			<div class="MainContent" >
				<h3>Features Overview</h3>
				<div class="ClearFix RowOdd">
					<div class="FL BoxImg"><a href="#"><img src="background/01-mini.png" alt=""/></a></div>
					<div class="BoxText">
						<h4 class="SpecialTit"><a name="Connect-Collaborate">Connect with your colleagues for an instant enterprise social network</a></h4>
						<p>Support for OpenSocial allows Cloud Workspaces users to create and connect rich user profiles, share activity streams, and collaborate in real-time. When you sign up with your professional email, we'll create a private domain for your company. When anyone else joins <a href="http://cloud-workspaces.com/">cloud-workspaces.com</a> using your company's email address, they will automatically be added to your company's network.</p>
					</div>
				</div>
				<div class="ClearFix RowEven">
					<div class="FR BoxImg"><a href="#"><img src="background/02-mini.png" alt=""/></a></div>
					<div class="BoxText">
						<h4 class="SpecialTit"><a name="Status-Update">More than a private social network: enterprise wiki, forums, calendars, and more</a></h4>
						<p>Following updates and comments from your coworkers is a great way to stay informed, but it's only the first step towards productive collaboration. Cloud Workspaces combines the benefits of a social network with the tools that support your online work. You can share documents, build workflows to manage business processes automatically, interact in forums and add team and project calendars. There's also a full-featured enterprise wiki built into every workspace, so your team can create, edit, store and easily find content and resources.</p>
					</div>
				</div>
				
				<div class="ClearFix RowOdd">
					<div class="FL BoxImg"><a href="#"><img src="background/03-mini.png" alt=""/></a></div>
					<div class="BoxText">
						<h4 class="SpecialTit">Personalized dashboards provide a work start page for every user</h4>
						<p>Users can easily create their own personalized dashboards with simple drag-and-drop controls, so your Cloud Workspaces start page will display the most relevant, useful information about your work.</p>
					</div>
				</div>
				<div class="ClearFix RowEven">
					<div class="FR BoxImg"><a href="#"><img src="background/04-mini.png" alt=""/></a></div>
					<div class="BoxText">
						<h4 class="SpecialTit"><a name="Customize-Social">Extend and customize your social intranet with the built-in Cloud IDE</a></h4>
						<p>Toggling between your company social network and other sites you regularly use, such as web analytics or project management tools, can be distracting and inefficient. Third party web apps can be integrated directly in your Cloud Workspaces as gadgets. eXo Cloud IDE, a full-featured development environment for building and integrating applications, gadgets and mashups, is available within every Cloud Workspaces account.</p>
						<p>Reuse or share gadgets with other Cloud Workspaces or Cloud IDE users - just drag-and-drop to add them in your personal dashboard or any of your company's private workspaces.</p>
					</div>
				</div>
				<div class="ClearFix RowOdd">
					<div class="FL BoxImg"><a href="#"><img src="background/05-mini.png" alt=""/></a></div>
					<div class="BoxText">
						<h4 class="SpecialTit"><a name="Direct-PaaS">Your personal dashboard on-the-go: native mobile apps for Cloud Workspaces</a></h4>
						<p>Native iPhone, iPad, and Android apps let you securely and easily view your personal dashboard, including any of the custom gadgets you've added. Post status updates and share photos, stay updated with your colleagues' activities, access your internal document repository - all the key functions of your social intranet, right in your pocket.</p>
					</div>
				</div>
			</div>
		</div>
		
		<!--begin footer-->
    <%@ include file="common/footer.jsp"%>
    
	</body>
</html>
