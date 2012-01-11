<%@page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
	<head>
    <% String pageName = "Sign Up Done"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
        <!-- load ThickBox to display video (http://jquery.com/demo/thickbox/) -->
        <link rel="stylesheet" href="/thickbox/thickbox.css" type="text/css" media="screen" />
        <script src="/thickbox/thickbox.js" type="text/javascript"></script>
        <script type="text/javascript">var tb_pathToImage = "/background/img_video.png";</script>
	</head>
	<body>
		<!--begin header-->
    <%@ include file="common/header.jsp"%>
    
		<!--begin ThanksPages-->	
		 <div class="UIPageBody ThanksPages ClearFix">
			<h1>Thank you for your interest in eXo Cloud Workspaces</h1>
			<p class="FillProfileInfo AlignLeftTxt">We've received your request to participate in the private beta. When your Cloud Workspaces account is available, we'll notify you via email. In the meantime, you can learn more about Cloud Workspaces by checking out these helpful resources.</p>
			<div class="LeftContent FL">
				<h3>Watch the Getting Started video:</h3>
				<a href="#TB_inline?height=420&width=670&inlineId=videoLeft" class="thickbox">
				<img src="/background/img_video.png" alt=""/>
				</a>
			</div>
			<div class="RightContent FL">
				<h3>Even more to explore:</h3>
				<a href="about.jsp">Read an introduction to the key features of eXo Cloud Workspaces</a>
				<a href="http://community.exoplatform.com">Join the eXo Community to connect with other users, access documentation, forums and more</a>
				<a href="http://exoplatform.com/company/en/products">Want to host your own social intranet on-premise or in a private cloud? Learn more about eXo Platform 3.5</a>
			</div>
			<center><a class="BackIcon" href="/index.jsp">Back</a></center>
		</div>
		
		<!--begin Footer-->	
    <%@ include file="common/footer.jsp"%>
    
       <!-- Video panel -->
       <div class="video_display" id="videoLeft" name="videoLeft" style="display: none;">
        <object width="640" height="350">
         <param name="allowfullscreen" value="true" />
         <param name="allowscriptaccess" value="always" />
         <param name="movie" value="http://vimeo.com/moogaloop.swf?clip_id=33936181&amp;server=vimeo.com&amp;show_title=0&amp;show_byline=0&amp;show_portrait=0&amp;color=00adef&amp;fullscreen=1&amp;autoplay=0&amp;loop=0" />
         <embed src="http://vimeo.com/moogaloop.swf?clip_id=33936181&amp;server=vimeo.com&amp;show_title=0&amp;show_byline=0&amp;show_portrait=0&amp;color=00adef&amp;fullscreen=1&amp;autoplay=0&amp;loop=0" type="application/x-shockwave-flash" allowfullscreen="true" allowscriptaccess="always" width="640" height="350"></embed>
        </object>
        <object>
          <p style="font-size: 15px; font-family: arial, tahoma, verdana, sans-serif; color: #575757;">
          View more resources in the <a style="color: #00AABF;" href="http://www.exoplatform.com/company/en/resource-center" target="_blank">eXo Resource Center</a>.
          </p>
        </object>
        </div>
     <!-- BEGIN: Google Analytics TRACKING -->
       <script type="text/javascript" src="http://www.google-analytics.com/ga.js"></script>
     <!-- END: Google Analytics TRACKING -->
     <!-- BEGIN: LOOPFUSE TRACKING -->
       <script type="text/javascript" src="http://lfov.net/webrecorder/js/listen.js"></script>
     <!-- END: LOOPFUSE TRACKING -->
       <script type="text/javascript" src="/js/trackers.js"></script>
  </body>
</html>
