<%@page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
	<head>
    <% String pageName = "Help eXo Cloud Workspace"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
	</head>
	<body onLoad="tenants.init();">
		<!--begin header-->
    <%@ include file="common/header.jsp"%>
    
		<!--begin Help Page-->	
		<div class="UIPageBody HelpPage">
			<div class="SiderBarLeft" id="SiderBarLeft">
				<ul class="MenuBar">
					<li class="Title">Categories</li>
					<li><a href="#general">General</a></li>
					<li><a href="#people">People</a></li>
					<li><a href="#spaces">Spaces</a></li>
					<li><a href="#documents">Documents</a></li>
					<li><a href="#mobile">Mobile</a></li>

				</ul>
				<a title="Ask a Question" href="http://community.exoplatform.org/portal/public/classic/forum/cloud-workspaces/ForumService" target="_blank"><img src="background/ask-a-question.png" alt="Ask a Question"/></a>
			</div>
			<div class="MainContent">
			
			
				<h1>FAQ</h1>
				
				
				<div class=" ClearFix ">				
					<div class="col5 FL">
						<h3><a href="#general">General</a></h3>
						 <ul >
							<li><a href="#general1">Can I choose the name of my workspace?</a></li>
							<li><a href="#general2">Why can't I create a workspace with my Gmail or Hotmail address?</a></li>
							<li><a href="#general3">What are the limitations of the Beta?</a></li>
							<li><a href="#general4">How do I become an administrator</a></li>
			 
						</ul>
					</div>
					<div class="col5 FL">
						<h3><a href="#people">People</a></h3>
						 <ul >
							<li><a href="#people1">What are connections?</a></li>
							<li><a href="#people2">How do I connect with a coworker?</a></li>
						</ul>
					</div>
				</div>
				
				<div class="RowEven ClearFix ">		
					<div class="col5 FL">
						<h3><a href="#spaces">Spaces</a></h3>
						 <ul >
							<li><a href="spaces1">What are Spaces?</a></li>
							<li><a href="spaces2">How do I become Space administrator?</a></li>
						</ul>
					</div>
				
					<div class="col5 FL">
						<h3><a href="#documents">Documents</a></h3>
						 <ul >
							<li><a href="documents1">Who can view my documents?</a></li>
							<li><a href="documents2">What type of document can I upload?</a></li>
						</ul>
					</div>
				</div>
				
				<div class="RowEven ClearFix ">		

					<div class="col5 FL">
						<h3><a href="#mobile">Mobile</a></h3>
						 <ul >
							<li><a href="mobile1">How do I download the mobile app?</a></li>
							<li><a href="mobile2">How do I connect to my workspace?</a></li>
						</ul>
					</div>
					
					
				</div>
			
					
			<h1 id="general">General</h1>
			
			<!-- without screenshot -->
			
			<div class="ClearFix RowOdd">
			<h4 class="SpecialTit" id="general1">Can I choose the name of my workspace?</h4>
			<p>No. You will automatically join the workspace defined by the domain name of your email address. If your email address is <strong>john@acme.com</strong>, your username will be <strong>john</strong> and your workspace's name will be <strong>acme</strong>.</p>
			</div>
			
			<div class="ClearFix RowOdd">
			<h4 class="SpecialTit" id="general2">Why can't I create a workspace with my Gmail or Hotmail address?</h4>
			<p>Cloud Workspaces is a social intranet for the enterprise. A corporate email address is required to use the service as the email determines which workspace you are part of.</p>
			</div>
			
			<div class="ClearFix RowOdd">
			<h4 class="SpecialTit" id="general3">What are the limitations of the Beta?</h4>
			<p>Since we are still experimenting, we might be moving things around and you might encounter minor issues here and there. If you do, please let us know so we can fix them for you.</p>
			</div>
			
			<div class="ClearFix RowOdd">	
			<h4 class="SpecialTit" id="general4">How do I become an administrator?</h4>
			<p>Only the creator of the company's workspace is granted administrative rights. If you want more another user to have administrative rights, have the administrator send us an email at <a href="beta@cloud-workspaces.com">beta@cloud-workspaces.com</a>.</p>
			</div>
	
																
			<h1 id="people">People</h1>
			
			<!-- without screenshot -->
			
			<div class="ClearFix RowOdd">	
			<h4 class="SpecialTit" id="people1">What are connections?</h4>
			<p>To view other's users activity and profile, you must first connect with them. This allows you to only share information with people you want to and reduce the noise in your activity stream.</p>
			</div>
			
			<!-- with screenshot -->
			
			<div class="ClearFix RowOdd">				
			<h4 class="SpecialTit" id="people2">How do I connect with a coworker?</h4>
			<p>Connect to other users using the Find People page, that can be found in the top-bar, under your personal menu. You must wait for the other person to accept the connection request for the connection to take effect.</p>
			</div>
				
							
											
			<h1 id="spaces">Spaces</h1>
			
						
			<div class="ClearFix RowOdd">				
			<h4 class="SpecialTit" id="spaces1">What are Spaces?</h4>
			<p>Spaces are a made for groups of people working on the same team or collaborating on a project. They can be private or public. Once you joined the space, you have access to that Space's apps (such as its Document repository or shared Calendar). Spaces also have their own actvity stream displaying all activity related to the Space. Administrators have control on who joins the space and can add or remove applications within the Space.</p>
			</div>
			
			<div class="ClearFix RowOdd">				
			<h4 class="SpecialTit" id="spaces2">How do I become Space administrator?</h4>
			<p>The Space creator will automatically become a Space administrator. He can then grant administrative rights to other users using the Settings menu.</p>
			</div>	
				
				
				
			<h1 id="documents">Documents</h1>
						
			<div class="ClearFix RowOdd">				
			<h4 class="SpecialTit" id="documents1">Who can view my documents?</h4>
			<p>In the Documents app, you fill find a Public folder. Any file added in this folder will be visible by all your colleagues. Any other document will only be accesible to you.</p>
			</div>
			
			<div class="ClearFix RowOdd">				
			<h4 class="SpecialTit" id="documents2">What type of document can I upload?</h4>
			<p>You can upload any type of document but you can only visualize images, videos, and PDF files from the web interface.</p>
			</div>
								
													
					
			<h1 id="mobile">Mobile</h1>
			
			<div class="ClearFix RowOdd">
			<h4 class="SpecialTit" id="mobile1">How do I download the mobile app?</h4>
			<p>You can the eXo Mobile app on the Apple Store and Android Store under the name "eXo Platform 3.5".</p>
			</div>
						
			<div class="ClearFix RowOdd">
			<h4 class="SpecialTit" id="mobile2">How do I connect to my workspace?</h4>
			<p>On the homepage of the application select "Settings". In the server list, press "Modify Server" and add a new server with your workspace's URL.</p>
			</div>
												
			
			
			
			
				
				
			</div>
			
		</div>
		<!--end code body here-->
		
		
		
		
		<script type="text/javascript">
		   window.onscroll = scroll;    
		function scroll()  {  
		var DivHeight = document.getElementById("UIHeader").offsetHeight;/*get height UIHeader*/
		var FooterHeight = document.getElementById("UIFooter").offsetHeight;
		var bodyscroll=window.pageYOffset;
		var temp=DivHeight-bodyscroll;	
		 document.getElementById("SiderBarLeft").style.top=temp+20+"px";  /*add 20px (UIPageBody has style margin-top=20)*/
		if(bodyscroll>DivHeight){
			document.getElementById("SiderBarLeft").style.top="10px";  /*set defaul 10px if position scroll over UIHeader*/
		}

		}  
		</script>
		
		 <!--end help page-->
		
	<!--begin Footer-->
    <%@ include file="common/footer.jsp"%>
    <!-- BEGIN: LOOPFUSE TRACKING -->
      <script type="text/javascript" src="http://lfov.net/webrecorder/js/listen.js"></script>
     <!-- END: LOOPFUSE TRACKING -->
    <script type="text/javascript" src="/js/trackers.js"></script>
	</body>
</html>
