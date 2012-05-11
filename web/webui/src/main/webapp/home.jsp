<%@page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
  <head>
    <% String pageName = "Sign Up to Cloud Workspaces"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
    <link rel="stylesheet" href="/lightbox/css/lightbox.css" type="text/css" media="screen" />
    <script type="text/javascript" src="/lightbox/jquery.lightbox.js"></script>
    <script type="text/javascript">
     $(function()
      {
       $('.lightbox').lightbox({
        fitToScreen : true,
        imageClickClose : false,
        fileLoadingImage : '/lightbox/images/loading.gif',
        fileBottomNavCloseImage : '/lightbox/images/closelabel.gif'
      });

     // preload images to display in lightbox immediately
     var links = document.getElementsByTagName("a");
     for ( var i in links)
      {
       var link = links[i];
       if (link.className && link.className === "lightbox" && link.href)
        {
         preload(link.href);
       }
      }

     function preload(imageSrc)
     {
      var imageObj = new Image();
      imageObj.src = imageSrc;
     }
    });
</script>
    
    <!-- load ThickBox to display video (http://jquery.com/demo/thickbox/) -->
    <link rel="stylesheet" href="/thickbox/thickbox.css" type="text/css" media="screen" />
    <script src="/thickbox/thickbox.js" type="text/javascript"></script>    
    <script type="text/javascript">var tb_pathToImage = "/background/img_video.png";</script>
    <script type="text/javascript" src="https://apis.google.com/js/plusone.js"></script>
  </head>

  <body onLoad="tenants.init();">
    <div class="UIPageContainer">
      <!--begin header-->
      <%@ include file="common/header.jsp"%>
  
      <div class="UIPageBodyContainer">
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
                <p><a href="http://community.exoplatform.org" target="_blank">Ask a question in the forum, submit your feedback, or contact our sales team.  </a></p>
              </div>
              <div class="FormBox">
                <div class="BoxContent ClearFix">
    
                  <form class="UIForm" action="javascript:void(0);"   method="POST" name="cloud-workspaces-profile" id="signupForm">
                   <!-- Marketo input hidden fields -->
                   <input type="hidden" name="lpId" value="1047" />
                   <input type="hidden" name="subId" value="46" />
                   <input type="hidden" name="formid" value="1035" />
                    <p class="SignUpInfo">Cloud Workspaces is currently in beta. To participate, register with your work email address. We'll keep you updated when your company's social intranet is ready.</p>
                    <div id="messageString" class="TenantFormMsg"></div>
                    <input class="InputText" type="text" name="email" id="email" value="Enter your professional email" onclick="this.value='';" />
                    <input class="Button" type="submit" id="t_submit"  value="Sign Up" onClick="tenants.doSingupRequest();" />
                    <div id="signin"><a class="SignIn" href="/signin.jsp">Already registered? Sign In</a></div>
                    <iframe id="loopfuseOutput" name="loopfuseOutput" style='display:none; visibility:hidden'></iframe>
                  </form>
                  <div class="SocialBox">
                   
                    <div id="fb-root"></div>
                    <script>(function(d, s, id) {
                        var js, fjs = d.getElementsByTagName(s)[0];
                        if (d.getElementById(id)) return;
                        js = d.createElement(s); js.id = id;
                        js.src = "https://connect.facebook.net/en_US/all.js#xfbml=1";
                        fjs.parentNode.insertBefore(js, fjs);
                      }(document, 'script', 'facebook-jssdk'));
                    </script>
                    <div class="FL">
                    	 
                        <div class="fb-like" data-href="http://www.facebook.com/eXoPlatform" data-send="true" data-layout="button_count" data-show-faces="false">
                        </div>
                     </div>
                     <div class="FR"> 
                           <a href="https://twitter.com/share" class="twitter-share-button" data-url="http://cloud-workspaces.com" data-text="Cloud-Workspaces: The Free Social Intranet for Your Company" data-via="eXoPlatform" data-count="none">Tweet</a>
<script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0];if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src="//platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);}}(document,"script","twitter-wjs");</script>
   
                        <!-- Place this tag where you want the +1 button to render -->
                        <g:plusone size="medium"></g:plusone>
                     </div>
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
              <h3>About Cloud Workspaces</h3>
              <p><b>Cloud Workspaces</b> is a private social intranet that makes it easy to connect and collaborate with your coworkers.</p>
              <p>Going beyond simple status updates, Cloud Workspaces provides tools that let you capture, organize and act on your team's internal knowledge, from a full-featured enterprise wiki, rich content management system, forums and more.</p>
              <a href="/about.jsp" class="Readmore">Learn more</a>
            </div>
            <div class="Cols MCol FL">
              <h3>What Can You Do with Cloud Workspaces?</h3>
              <a href="#TB_inline?height=420&width=670&inlineId=videoLeft" class="thickbox">
              <img src="/background/img_video.png" alt=""/>
              </a>
            </div>
            <div class="Cols RCol FR">
              <ul>
                <li><a href="http://exoplatform.com/company/en/products/mobile" target="_blank"><img src="background/Stay-connect-mobile-apps.png" alt="Stay Connected with Mobile Apps for Cloud Workspaces" /></a> </li>
                <li><a href="http://exoplatform.com/company/en/products/overview" target="_blank"><img src="background/SocialIntranet-on-premise.png" alt="Want an On-Premise Social Intranet?" /></a></li>
              </ul>
            </div>
          </div>
        </div>
    
        <!--IndexPage-->
         <div class="UIPageBody IndexPage">
			<h3>Features Overview</h3>
			<div class="LastestItems ClearFix">
				<div class="Cols LCol FL">
					<div><a class="lightbox" rel="lightbox" href="background/social.png" title="Connect with your colleagues for an instant enterprise social network"><img src="background/01_Mini.png" alt=""/></a></div>
					<h4 class="SpecialTit">Connect with your colleagues for an instant enterprise social network</h4>
					<p>Support for OpenSocial allows Cloud Workspaces users to create and connect rich user profiles, share activity streams, and collaborate in real-time. When you sign up with your professional email, we'll create a private domain for your company.</p>
				</div>
				
				<div class="Cols MCol FL">
					<a class="lightbox" rel="lightbox" href="background/wiki.png" title="More than a private social network: enterprise wiki, forums, calendars, and more"><img src="background/02_Mini.png" alt=""/></a>
					<h4 class="SpecialTit">More than a private social network: enterprise wiki, forums, calendars, and more</h4>
					<p>Following updates and comments from your coworkers is a great way to stay informed, but it's only the first step towards productive collaboration. Cloud Workspaces combines the benefits of a social network with the tools that support your online work.</p>
				</div>
				
				<div class="Cols RCol FR">
					<a class="lightbox" rel="lightbox" href="background/dashboard2.png" title="Personalized dashboards provide a work start page for every user"><img src="background/03_Mini.png" alt=""/></a>
					<h4 class="SpecialTit">Personalized dashboards provide a work start page for every user</h4>
					<p>Users can easily create their own personalized dashboards with simple drag-and-drop controls, so your Cloud Workspaces start page will display the most relevant, useful information about your work.</p>
				</div>
			</div>
			<div class="ClearFix">
				<div class="Cols LCol FL">
					<a class="lightbox" rel="lightbox" href="background/ide.png" title="Extend and customize your social intranet with the built-in Cloud IDE"><img src="background/04_Mini.png" alt=""/></a>
					<h4 class="SpecialTit">Extend and customize your social intranet with the built-in Cloud IDE</h4>
					<p>Toggling between your company social network and other sites you regularly use, such as web analytics or project management tools, can be distracting and inefficient. Third party web apps can be integrated directly in your Cloud Workspaces as gadgets. </p>
				</div>
				
				<div class="Cols BigCol FR">
					<a class="lightbox" rel="lightbox" href="background/iPad-Gadgets.png" title="Your personal dashboard on-the-go: native mobile apps for Cloud Workspaces"><img src="background/05_Mini.png" alt=""/></a>
					<h4 class="SpecialTit">Your personal dashboard on-the-go: native mobile apps for Cloud Workspaces</h4>
					<p>Native iPhone, iPad, and Android apps let you securely and easily view your personal dashboard, including any of the custom gadgets you've added. Post status updates and share photos, stay updated with your colleagues' activities, access your internal document repository - all the key functions of your social intranet, right in your pocket.</p>
				</div>
			</div>
        </div>
      </div>
        
      <!--begin footer-->
      <%@ include file="common/footer.jsp"%>
  
      <!-- Video panel -->
      <div class="video_display" id="videoLeft" name="videoLeft" style="display: none;">
        <object width="640" height="350">
          <param name="allowfullscreen" value="true" />
          <param name="allowscriptaccess" value="always" />
          <param name="movie" value="http://vimeo.com/moogaloop.swf?clip_id=33936181&amp;server=vimeo.com&amp;show_title=0&amp;show_byline=0&amp;show_portrait=0&amp;color=00adef&amp;fullscreen=1&amp;autoplay=1&amp;loop=0" />
          <embed src="http://vimeo.com/moogaloop.swf?clip_id=33936181&amp;server=vimeo.com&amp;show_title=0&amp;show_byline=0&amp;show_portrait=0&amp;color=00adef&amp;fullscreen=1&amp;autoplay=1&amp;loop=0" type="application/x-shockwave-flash" allowfullscreen="true" allowscriptaccess="always" width="640" height="350"></embed>
        </object>
        <object>
          <p style="font-size: 15px; font-family: arial, tahoma, verdana, sans-serif; color: #575757;">
            View more resources in the <a style="color: #00AABF;" href="http://www.exoplatform.com/company/en/resource-center" target="_blank">eXo Resource Center</a>.
          </p>
        </object>
      </div>
    </div>

    <!-- marketo response container -->
    <iframe id="mktOutput" name="mktOutput" style='display:none; visibility:hidden'></iframe>
    <!-- BEGIN: MARKETO TRACKING -->
    <script type="text/javascript" src="/js/mktFormSupport.js"></script>
    <script type="text/javascript" src="/js/trackers.js"></script>
  </body>
</html>
