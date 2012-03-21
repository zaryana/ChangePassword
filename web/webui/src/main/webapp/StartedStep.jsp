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
		
		<!--HelpPage-->	
		<script type="text/javascript"><!--script calculate position menu left when scrollbar trigger -->
		window.onscroll = scroll;    
		function scroll()  {  
		var DivHeight = document.getElementById("UIHeader").offsetHeight;/*get height UIHeader*/
		
		
		if(typeof(window.pageYOffset)=='number') {
			bodyscroll=window.pageYOffset;
		}
		else {
			   bodyscroll=document.documentElement.scrollTop;
		}
		var temp=DivHeight-bodyscroll;	
		 document.getElementById("SiderBarLeft").style.top=temp+20+"px";  /*add 20px (UIPageBody has style margin-top=20)*/
		if(bodyscroll>DivHeight){
			document.getElementById("SiderBarLeft").style.top="10px";  /*set defaul 10px if position scroll over UIHeader*/
		}
		}  
		</script> 

	<div class="UIPageBody">
		<form class="UIForm UIFormBox StartedStep" style="display: block;" name="" id="" method="POST" action="javascript:void(0);" >
			<h1 class="StartedBarBG">Welcome to Cloud Workspaces - Get started in 3 easy steps</h1>
			<div class="Steps" id="">
				<span class="StepBG"></span>
				<a href="#" class="StepSelectIcon" style="left: 60px;">1</a><a href="#" class="StepIcon" style="left: 310px;" >2</a><a href="#" class="StepIcon" style="left: 569px;">3</a>
			</div>
			<h3>Step 1:  Invite Coworkers</h3>
			<p><strong>Invite colleagues to join your company’s</strong><br/>(note: Only @exoplatform.com email addresses will be invited to your workspace. Other addresses will receive an invitation to discover Cloud Workspaces)</p>
			<table class="BorderDot" cols="2">
				<tbody>
					<tr>
						
						<td class="FormInput"> <input type="text" onclick="this.value='';" value="Your email" id="email" name="" class="required InputText"></td>
						<td class="FormButton"> <input type="submit" onclick="tenants.doLogin();" value="Next" id="t_submit" class="Button"></td>
					</tr>
				</tbody>
			</table>
			<div class="ClearFix StartTip">
				<a href="#" class="FR RightStartTip"><img width="264" src="background/img_st.png" alt=""/></a>
				<p class="LeftStartTip"><strong>Tip:</strong> Find and connect with your colleagues to see their latest updates in your activity stream.</p>
			</div>
			<div class="Link"><a href="#" class="Link">Skip to homepage >></a></div>
		</form>
		
		<form class="UIForm UIFormBox StartedStep" style="display: none;" name="" id="" method="POST" action="javascript:void(0);" >
			<h1 class="StartedBarBG">Welcome to Cloud Workspaces - Get started in 3 easy steps</h1>
			<div class="Steps" id="">
				<span class="StepBG"></span>
				<a href="#" class="StartedIcon" style="left: 60px;">1</a><a href="#" class="StepSelectIcon" style="left: 310px;" >2</a><a href="#" class="StepIcon" style="left: 569px;">3</a>
			</div>
			<h3>Step 2:  Add Documents</h3>
			<p><strong>Securely manage your work files in the cloud - add a few to get started.</strong></p>
			<table class="BorderDot" cols="2">
				<tbody>
					<tr>
						<td class="FormInput"><div class="HelpText">Drag and drop a document to add it to your private folder</div></td>
						<td class="FormButton"> <input type="submit" onclick="tenants.doLogin();" value="Next" id="t_submit" class="Button"></td>
					</tr>
				</tbody>
			</table>
			<div class="ClearFix StartTip">
				<a href="#" class="FR RightStartTip"><img width="264" src="background/img_st.png" alt=""/></a>
				<p class="LeftStartTip"><strong>Tip:</strong> Easily access your documents on your iPhone, iPad or Android device with the eXo mobile app. You can keep files private, share them with specific coworkers or publish them in a dedicated space.</p>
			</div>
			<div class="Link"><a href="#" class="Link">Skip to homepage >></a></div>
		</form>
		
		<form class="UIForm UIFormBox StartedStep" style="display: none;" name="" id="" method="POST" action="javascript:void(0);" >
			<h1 class="StartedBarBG">Welcome to Cloud Workspaces - Get started in 3 easy steps</h1>
			<div class="Steps" id="">
				<span class="StepBG"></span>
				<a href="#" class="StartedIcon" style="left: 60px;">1</a><a href="#" class="StartedIcon" style="left: 310px;" >2</a><a href="#" class="StepSelectIcon" style="left: 569px;">3</a>
			</div>
			<h3>Step 3:  Join the Welcome Space</h3>
			<p>Create your own dedicated collaboration spaces for your team or specific projects. We’ve set up your first space to help you get started.</p>
			<table class="BorderDot" cols="2">
				<tbody>
					<tr>
						<td class="FormInput CheckBox">
							<ul class="ClearFix">
								<li class="FL">
									<input type="checkbox"/>Maketting <br/>
									<input type="checkbox"/>Sales
								</li>
								<li class="FL">
									<input type="checkbox"/>Finance<br/>
									<input type="checkbox"/>IT 
								</li>
								<li class="FL">
									<input type="checkbox"/>Accountant<br/>
									<input type="checkbox"/>Design
								</li>
							</ul>
						</td>
						<td class="FormButton"> <input type="submit" onclick="tenants.doLogin();" value="Next" id="t_submit" class="Button"></td>
					</tr>
				</tbody>
			</table>
			<div class="ClearFix StartTip">
				<a href="#" class="FR RightStartTip"><img width="264" src="background/img_st.png" alt=""/></a>
				<p class="LeftStartTip"><strong>Tip:</strong> Easily access your documents on your iPhone, iPad or Android device with the eXo mobile app. You can keep files private, share them with specific coworkers or publish them in a dedicated space.</p>
			</div>
			<div class="Link"><a href="#" class="Link">Skip to homepage >></a></div>
		</form>
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