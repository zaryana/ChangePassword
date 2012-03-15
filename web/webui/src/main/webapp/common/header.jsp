		<div class="UIHeader" id="UIHeader">
			<div class="MainContent">
				<ul class="UIMenuTop FR">
				  <li><a class="Tab BlueTab" href="<%=request.getContextPath()%>/about.jsp">About</a></li>
				  <li><a class="Tab GreenTab" href="http://community.exoplatform.org/portal/public/classic/forum/cloud-workspaces/ForumService" target="_blank">Forum</a></li>
				  <li><a class="Tab GrayTab" href="<%=request.getContextPath()%>/help.jsp" target="_blank">Help</a></li>
				  <li><a class="Tab OrangeTab" href="http://www.exoplatform.com" target="_blank">eXoplatform.com</a></li>
				  <li class="last"><a class="Tab OrangeTab" onclick="showContactUsForm('<%=request.getContextPath()%>/contact-us.jsp');">Contact us</a></li>
				</ul>
				<div class="Logo FL"><a href="/<%=request.getContextPath()%>"><img src="<%=request.getContextPath()%>/background/logo.png"/></a></div>
				<div class="ClouIntranet FR"><h1>Cloud Workspaces</h1><span>The Free Social Intranet for Your Company</span></div>
			</div>
		</div>

		<div class="MarkLayer" id="MaskLayer" style="width: 100%; display: none;"></div>
		<div id="ContactUsContainer" style="display: none;"></div>