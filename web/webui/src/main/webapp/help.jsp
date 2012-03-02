<%@page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
	<head>
    <% String pageName = "About eXo Cloud Workspace"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
	</head>
	<body onLoad="tenants.init();">
		<!--begin header-->
    <%@ include file="common/header.jsp"%>
    
		<!--begin Help Page-->	
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
		<div class="UIPageBody HelpPage">
		<div class="SiderBarLeft" id="SiderBarLeft">
			<ul class="MenuBar">
				<li class="Title">all category</li>
				<li><a href="">category 1</a></li>
				<li><a href="">category 2</a></li>
				<li><a href="">category 3</a></li>
				<li><a href="">category 4</a></li>
				<li><a href="">category 5</a></li>
				<li><a href="">category 6</a></li>
				<li><a href="">category 7</a></li>
			</ul>
			<a href="#" title="Ask a Question"><img src="background/ask-a-question.png" alt="Ask a Question"/></a>
		</div>
		<div class="MainContent">				
			<div class="ClearFix RowOdd"><h4 class="SpecialTit"><a name="Connect-Collaborate">Connect with your colleagues for an instant enterprise social network</a></h4>
				<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus consequat nisl id augue consequat consectetur. Vivamus et augue quis ante ultricies mollis.</p>
				<div class="FL BoxImg"><a title="Connect with your colleagues for an instant enterprise social network" href="background/social.png" rel="lightbox" class="lightbox-enabled"><img alt="" src="background/01-mini.png"></a></div>
				<div class="BoxText">			
					<p>Fusce magna dolor, malesuada a tristique non, interdum at odio. Nullam ullamcorper, risus nec vulputate elementum, mi felis condimentum dui, sed volutpat lacus lacus a purus. Donec in mi nisi. </p>
					<p>	Curabitur bibendum elementum nulla quis placerat. Maecenas lacinia, lacus a malesuada rutrum, urna erat tincidunt dui</p>
				</div>
			</div>					
			<h4 class="SpecialTit"><a name="Status-Update" href="background/wiki.png">More than a private social network: enterprise wiki, forums, calendars, and more</a></h4>
			<p>Following updates and comments from your coworkers is a great way to stay informed, but it's only the first step towards productive collaboration. Cloud Workspaces combines the benefits of a social network with the tools that support your online work. You can share documents, build workflows to manage business processes automatically, interact in forums and add team and project calendars. There's also a full-featured enterprise wiki built into every workspace, so your team can create, edit, store and easily find content and resources.</p>
			<div class="ClearFix RowOdd"><h4 class="SpecialTit">Personalized dashboards provide a work start page for every user</h4>
				<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus consequat nisl id augue consequat consectetur. Vivamus et augue quis ante ultricies mollis.</p>
				<div class="FL BoxImg"><a title="Personalized dashboards provide a work start page for every user" rel="lightbox" class="lightbox-enabled" href="background/dashboard2.png"><img alt="" src="background/03-mini.png"></a></div>
				<div class="BoxText">			
					<p>Fusce magna dolor, malesuada a tristique non, interdum at odio. Nullam ullamcorper, risus nec vulputate elementum, mi felis condimentum dui, sed volutpat lacus lacus a purus. Donec in mi nisi. </p>
					<p>	Curabitur bibendum elementum nulla quis placerat. Maecenas lacinia, lacus a malesuada rutrum, urna erat tincidunt dui</p>
				</div>
			</div>	
			<hr/>
			<div class="RowEven ClearFix ">				
				<div class="col5 FL">
					<h3>Category Name 1</h3>
					 <ul >
						<li><a href="">- How to Phasellus at ipsum a nulla semper dapibus. </a></li>
						<li><a href="">- How to Lorem ipsum dolor sit amet</a></li>
						<li><a href="">- How to Consectetur adipiscing elit. </a></li>		 
					</ul>
				</div>
				<div class="col5 FL">
					<h3>Category Name 1</h3>
					 <ul >
						<li><a href="">- How to Phasellus at ipsum a nulla semper dapibus. </a></li>
						<li><a href="">- How to Lorem ipsum dolor sit amet</a></li>
						<li><a href="">- How to Consectetur adipiscing elit. </a></li>		 
					</ul>
				</div>
			</div>
			<div class="RowEven ClearFix ">		
				<div class="col5 FL">
					<h3>Category Name 1</h3>
					 <ul >
						<li><a href="">- How to Phasellus at ipsum a nulla semper dapibus. </a></li>
						<li><a href="">- How to Lorem ipsum dolor sit amet</a></li>
						<li><a href="">- How to Consectetur adipiscing elit. </a></li>		 
					</ul>
				</div>
				<div class="col5 FL">
					<h3>Category Name 1</h3>
					 <ul >
						<li><a href="">- How to Phasellus at ipsum a nulla semper dapibus. </a></li>
						<li><a href="">- How to Lorem ipsum dolor sit amet</a></li>
						<li><a href="">- How to Consectetur adipiscing elit. </a></li>		 
					</ul>
				</div>
			</div>
			<div class="RowEven ClearFix ">		
				<div class="col5 FL">
					<h3>Category Name 1</h3>
					 <ul >
						<li><a href="">- How to Phasellus at ipsum a nulla semper dapibus. </a></li>
						<li><a href="">- How to Lorem ipsum dolor sit amet</a></li>
						<li><a href="">- How to Consectetur adipiscing elit. </a></li>		 
					</ul>
				</div>
				<div class="col5 FL">
					<h3>Category Name 1</h3>
					 <ul >
						<li><a href="">- How to Phasellus at ipsum a nulla semper dapibus. </a></li>
						<li><a href="">- How to Lorem ipsum dolor sit amet</a></li>
						<li><a href="">- How to Consectetur adipiscing elit. </a></li>		 
					</ul>
				</div>
			</div>
		</div>
	</div> <!--end help page-->
		
	<!--begin Footer-->
    <%@ include file="common/footer.jsp"%>
    <!-- BEGIN: LOOPFUSE TRACKING -->
      <script type="text/javascript" src="http://lfov.net/webrecorder/js/listen.js"></script>
     <!-- END: LOOPFUSE TRACKING -->
    <script type="text/javascript" src="/js/trackers.js"></script>
	</body>
</html>
