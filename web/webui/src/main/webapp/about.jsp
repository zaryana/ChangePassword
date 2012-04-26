<%@page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
  <head>
    <% String pageName = "About eXo Cloud Workspace"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
  </head>
  <body onLoad="tenants.init();">
    <div class="UIPageContainer">
      <!--begin header-->
      <%@ include file="common/header.jsp"%>
  
      <!--AboutPages-->
      <div class="UIPageBodyContainer">
        <div class="UIPageBody AboutPages">
          <div class="SocialBox ClearFix">
            <h1 class="FL">About Cloud Workspaces</h1>
            <div id="fb-root"></div>
            <script>
              (function(d, s, id) {
                var js, fjs = d.getElementsByTagName(s)[0];
                if (d.getElementById(id)) return;
                js = d.createElement(s); js.id = id;
                js.src = "https://connect.facebook.net/en_US/all.js#xfbml=1";
                fjs.parentNode.insertBefore(js, fjs);
              }(document, 'script', 'facebook-jssdk'));
            </script>
            <div class="SocialInside FL">            
				<div class="fb-like" data-href="http://www.facebook.com/eXoPlatform" data-send="true" data-layout="button_count" data-show-faces="false"></div>                            
            </div>
          </div>
          <h3>Cloud Workspaces: Your Company's Social Intranet in the Cloud</h3>
          <p>Cloud Workspaces is a free cloud-based social intranet. It provides tools that let you capture, organize and act on your team's internal knowledge, from a full-featured enterprise wiki, powerful document management system, forums, calendars and more.
          </p>
          <p>Cloud Workspaces lets you go beyond the basic chatter of activity streams, giving employees the tools to customize their working environment. Individual users can create personalized dashboards to act as the starting point for all their online work, while teams and projects can have their own dedicated workspace to connect, share resources and collaborate. Finally, developers can build and deploy custom apps with the embedded web-based IDE.
          </p>
          <h3>Availability</h3>
          <p>Cloud Workspaces is currently in beta. Support with guaranteed SLA’s is not provided for Cloud Workspaces Beta. In addition, we will continue to add and improve features, documentation, performance, or make other changes based on user feedback. You might notice these changes or encounter minor issues; if so, please let us know so we can fix them for you. We will notify users of any planned downtime for upgrades, but there is still a chance of unplanned downtime as well.</p>
          <h3>Features & Benefits</h3>
          <div class="ClearFix">
            <ul class="LeftContent">
            <li><span>Activity Streams:</span> Share and follow updates from your connections; view latest forum posts, wiki or content changes, calendar events and other activities happening within each space.</li>
              <li><span>Personalized Dashboards:</span> Arrange gadgets to support your daily work. Share and reply to status updates, view documents, discussions and wiki pages you've bookmarked, see your to-do list and calendar events, all in one view.</li>
              
              <li><span>Mobile:</span> Native iPhone, iPad, and Android apps let you take your intranet with you. Access your personalized gadget dashboard, activity stream, document repository and more.</li>
              <li><span>Cloud IDE:</span> A full-featured development environment for building and integrating applications, gadgets and mashups.</li>
            </ul>
            <ul class="RightContent">
              <li><span>Document repository:</span> Store and manage internal documents, and build workflows to manage document-related processes.</li>
              <li><span>Forums:</span> Enable discussions, capture feeddback and create polls, with answers published in real-time activity streams.</li>
              <li><span>Enterprise Wiki:</span> Publish and organize content that members of a workspace can browse, search, edit and share, with wysiwyg, watches, macros, permissions, versioning and more.</li>
              <li><span>Calendars:</span> Every workspace has a calendar available for capturing team meeting schedules and project milestones. You can even synchronize with remote calendars (Gmail) so all your work, personal and team events are viewable from your dashboard.</li>
              
            </ul>
          </div>
          <h3>Core Technology</h3>
          <p>Cloud Workspaces is powered by the upcoming <a href="http://exoplatform.com/company/en/products/overview" title="eXo Platform 3.5"> eXo Platform 3.5</a>, the first and only cloud-ready enterprise Java portal and user experience platform. eXo Platform 3.5 is designed for on-premise or private cloud deployments.
          </p>
        </div>
      </div>
        
      <!--begin Footer-->
      <%@ include file="common/footer.jsp"%>
    </div>


    <script type="text/javascript" src="/js/trackers.js"></script>
  </body>
</html>
