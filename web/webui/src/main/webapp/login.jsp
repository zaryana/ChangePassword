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
		
	<div class="UIFooterPortlet">
			<div class="MainContent ClearFix">
				<p class="FL">Cloud Workspaces is Brought to You by <span><a href="http://www.exoplatform.com">eXo</a></span></p>
				<p class="Copyright FR">Copyright &copy; 2000-2012. All Rights Reserved, eXo Platform SAS.</p>
			</div>
		</div>

	</body>
	</html>