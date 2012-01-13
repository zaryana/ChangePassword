		<div class="UIHeader">
			<div class="MainContent">
				<ul class="UIMenuTop FR">
				  <li><a class="Tab BlueTab" href="<%=request.getContextPath()%>/about.jsp">About</a></li>
				  <li><a class="Tab GreenTab" href="http://community.exoplatform.com" target="_blank">Community</a></li>
				  <li><a class="Tab GrayTab" href="http://blog.exoplatform.com" target="_blank">Blog</a></li>
				  <li><a class="Tab OrangeTab" href="http://www.exoplatform.com" target="_blank">eXoplatform.com</a></li>
				  <li class="last"><a class="Tab OrangeTab" href="#" onclick="showContactUsForm('<%=request.getContextPath()%>/contact-us.jsp');">Contact us</a></li>
				</ul>
				<div class="Logo FL"><a href="/<%=request.getContextPath()%>"><img src="<%=request.getContextPath()%>/background/logo.png"/></a></div>
				<div class="ClouIntranet FR"><h1>eXo Cloud Workspaces</h1><span>The Free Social Intranet for Your Company</span></div>
			</div>
		</div>

		<div class="MarkLayer" id="MaskLayer" style="left: 0px; top: 0px; width: 100%; height: 599px; display: none;"></div>
		<div id="ContactUsContainer" style="display: none;"></div>