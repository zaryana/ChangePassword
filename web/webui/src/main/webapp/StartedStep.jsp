<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
  <head>
    <% String pageName = "Help eXo Cloud Workspace"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
  </head>
  <body onLoad="tenants.init();">
	<div class="GetStartedPage">
		<form class="UIFormBox StartedStep" style="display: block;" name="" id="" method="POST" action="javascript:void(0);" >
			<h1 class="StartedBarBG">Welcome to Cloud Workspaces - Get started in 3 easy steps</h1>
			<div class="Steps" id="">
				<span class="StepBG"></span>
				<a href="#" class="StepSelectIcon" style="left: 60px;">1</a><a href="#" class="StepIcon" style="left: 310px;" >2</a><a href="#" class="StepIcon" style="left: 569px;">3</a>
			</div>
			<h3>Step 1:  Invite Coworkers</h3>
			<p><strong>Invite colleagues to join your company’s</strong><br/>(note: Only @exoplatform.com email addresses will be invited to your workspace. Other addresses will receive an invitation to discover Cloud Workspaces)</p>
			<table class="BorderDot">
				<tbody>
					<tr>
						<td class="FormInput"> <input type="text" onclick="this.value='';" value="Your email" id="email" name="" class="required InputText" /></td>
						<td class="FormButton"> <input type="submit" onclick="tenants.doLogin();" value="Next" id="t_submit" class="Button" /></td>
					</tr>
				</tbody>
			</table>
			<div class="ClearFix StartTip">
				<a href="#" class="FR RightStartTip"><img width="264" src="background/img_st.png" alt=""/></a>
				<p class="LeftStartTip"><strong>Tip:</strong> Find and connect with your colleagues to see their latest updates in your activity stream.</p>
			</div>
			<div class="Link"><a href="#" class="Link">Skip to homepage >></a></div>
		</form>
		
		<form class="UIFormBox StartedStep" style="display: none;" name="" id="" method="POST" action="javascript:void(0);" >
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
						<td class="FormButton"> <input type="submit" onclick="tenants.doLogin();" value="Next" id="t_submit" class="Button" /></td>
					</tr>
				</tbody>
			</table>
			<div class="ClearFix StartTip">
				<a href="#" class="FR RightStartTip"><img width="264" src="background/img_st.png" alt=""/></a>
				<p class="LeftStartTip"><strong>Tip:</strong> Easily access your documents on your iPhone, iPad or Android device with the eXo mobile app. You can keep files private, share them with specific coworkers or publish them in a dedicated space.</p>
			</div>
			<div class="Link"><a href="#" class="Link">Skip to homepage >></a></div>
		</form>
		
		<form class="UIFormBox StartedStep" style="display: none;" name="" id="" method="POST" action="javascript:void(0);" >
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
									<input type="checkbox" />Maketting <br/>
									<input type="checkbox" />Sales
								</li>
								<li class="FL">
									<input type="checkbox" />Finance<br/>
									<input type="checkbox" />IT 
								</li>
								<li class="FL">
									<input type="checkbox" />Accountant<br/>
									<input type="checkbox" />Design
								</li>
							</ul>
						</td>
						<td class="FormButton"> <input type="submit" onclick="tenants.doLogin();" value="Next" id="t_submit" class="Button" /></td>
					</tr>
				</tbody>
			</table>
			<div class="ClearFix StartTip">
				<a href="#" class="FR RightStartTip"><img width="264" src="background/img_st.png" alt=""/></a>
				<p class="LeftStartTip"><strong>Tip:</strong> Easily access your documents on your iPhone, iPad or Android device with the eXo mobile app. You can keep files private, share them with specific coworkers or publish them in a dedicated space.</p>
			</div>
			<div class="Link"><a href="#">Skip to homepage &gt;&gt;</a></div>
		</form>
	</div>
	<!--end code body here-->
		
    
    <!-- BEGIN: LOOPFUSE TRACKING -->
    <script type="text/javascript" src="http://lfov.net/webrecorder/js/listen.js"></script>
    <!-- END: LOOPFUSE TRACKING -->
    <script type="text/javascript" src="/js/trackers.js"></script>    
  </body>
</html>