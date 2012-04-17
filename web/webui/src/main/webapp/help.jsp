<%@page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
  <head>
    <% String pageName = "Help eXo Cloud Workspace"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
  </head>
  <body onLoad="tenants.init();">
    <div class="UIPageContainer">
      <!--begin header-->
      <%@ include file="common/header.jsp"%>
  
      <!--begin Help Page-->
      <div class="UIPageBodyContainer">
        <div class="UIPageBody HelpPage">
          <div class="SiderBarLeft" id="SiderBarLeft">
            <ul class="MenuBar">
              <li class="Title">Categories</li>
              <li><a href="#general">General</a></li>
              <li><a href="#people">Connections</a></li>
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
                  <li><a href="#general1">How is the name of my company network selected?</a></li>
                  <li><a href="#general2">How do my coworkers find and join our Cloud Workspaces social intranet?</a></li>
                  <li><a href="#general3">Can I use my personal email address, such as Gmail or Hotmail?</a></li>
                  <li><a href="#general4">What do I need to know about using a Beta version of Cloud Workspaces?</a></li>
                  <li><a href="#general5">Who is the administrator of my Cloud Workspaces social intranet? Can this be changed?</a></li>
                </ul>
              </div>
              <div class="col5 FL">
                <h3><a href="#people">Connections</a></h3>
                 <ul >
                  <li><a href="#people1">How do I share status updates with specific coworkers?</a></li>
                  <li><a href="#people2">How do I find and connect with coworkers in my network?</a></li>
                </ul>
              </div>
            </div>
    
            <div class="RowEven ClearFix ">
              <div class="col5 FL">
                <h3><a href="#spaces">Spaces</a></h3>
                 <ul >
                  <li><a href="spaces1">What is a Space?</a></li>
                  <li><a href="spaces2">What features or apps are available within a Space?</a></li>
                  <li><a href="spaces3">Who can create a Space? Can administrator rights be changed?</a></li>
                  <li><a href="spaces4">Can I control the privacy settings of a Space that I’ve created?</a></li>
                  <li><a href="spaces5">How do I find and join existing Spaces?</a></li>
                </ul>
              </div>
    
              <div class="col5 FL">
                <h3><a href="#documents">Documents</a></h3>
                 <ul>                
                 <li><a href="documents1">What can I do with the eXo mobile apps?</a></li>
                  <li><a href="documents2">Who can view my documents?</a></li>
                  <li><a href="documents3">What type of document can I upload?</a></li>
                </ul>
              </div>
            </div>
    
            <div class="RowEven ClearFix ">
              <div class="col5 FL">
                <h3><a href="#mobile">Mobile</a></h3>
                 <ul >
                  <li><a href="mobile1">How do I download the mobile app?</a></li>
                  <li><a href="mobile2">How do I connect my device to my company’s social intranet?</a></li>
                </ul>
              </div>
            </div>
    
          <h1 id="general">General</h1>
    
          <!-- without screenshot -->
    
          <div class="ClearFix RowOdd">                  
            <h4 class="SpecialTit" id="general1">How is the name of my company network selected?</h4>
            <p>When you sign up for Cloud Workspaces, your username and company network name are automatically determined by your work email address. If your email address is <strong>john@mycompany.com</strong>, your username will be <strong>john</strong> and your network will be called <strong>mycompany</strong>.</p>
          </div>
          
          
          <div class="ClearFix RowOdd">
          <h4 class="SpecialTit" id="general2">How do my coworkers find and join our Cloud Workspaces social intranet?</h4>
            <p>The first person to sign up for Cloud Workspaces with your company’s email address (@mycompany.com) establishes the <strong>mycompany</strong> employee network. When anyone else using this email domain signs up, Cloud Workspaces automatically adds them to your network. You can also send invitations to colleagues once you are logged into your Cloud Workspaces social intranet.</p>
          </div>
          
    
          <div class="ClearFix RowOdd">
            <h4 class="SpecialTit" id="general3">Can I use my personal email address, such as Gmail or Hotmail?</h4>
            <p>Cloud Workspaces is a social intranet for the enterprise. A corporate email address is required to use the service because the email domain determines the name of your company network. Cloud Workspaces will add you to an existing network, or create a new one automatically if you are the first employee to register with your corporate email address. </p>
          </div>
    
          <div class="ClearFix RowOdd">
            <h4 class="SpecialTit" id="general4">What do I need to know about using a Beta version of Cloud Workspaces?</h4>
            <p>Support with guaranteed SLA’s is not provided for Cloud Workspaces Beta. In addition, we will continue to add and improve features, documentation, performance, or make other changes based on user feedback. You might notice these changes or encounter minor issues; if so, please let us know so we can fix them for you. We will notify users of any planned downtime for upgrades, but there is still a chance of unplanned downtime as well.</p>
          </div>
    
          <div class="ClearFix RowOdd">
            <h4 class="SpecialTit" id="general5">Who is the administrator of my Cloud Workspaces social intranet? Can this be changed?</h4>
            <p>The first person to sign up for Cloud Workspaces with your company’s email address (@mycompany.com) is the administrator by default. If you want another user(s) to have administrative rights, the original admin can submit a request via email to <a href="beta@cloud-workspaces.com">beta@cloud-workspaces.com</a>.</p>
          </div>
    
          <h1 id="people">Connections</h1>
    
    
          <div class="ClearFix RowOdd">
            <h4 class="SpecialTit" id="people1">How do I share status updates with specific coworkers?</h4>
            <p>To view another user’s profile and status updates, you must first connect with them. Establishing connections lets you follow the colleagues you want, and keeps distracting noise out of your activity stream.</p>
          </div>
    
    
          <div class="ClearFix RowOdd">
            <h4 class="SpecialTit" id="people2">How do I find and connect with coworkers in my network?</h4>
            <p>You can browse all users in your company network, or search for colleagues by name, job experience, title or department, then send, approve or decline connection requests.</p>
          </div>
    
          <h1 id="spaces">Spaces</h1>
    
          <div class="ClearFix RowOdd">
            <h4 class="SpecialTit" id="spaces1">What is a Space?</h4>
            <p>Spaces are structured work environments dedicated to project, team or cross-departmental collaboration. Spaces can be customized with tools and content that support the processes, organization and communication required by different groups.</p>
          </div>
    
    	<div class="ClearFix RowOdd">
            <h4 class="SpecialTit" id="spaces2">What features or apps are available within a Space?</h4>
            <p>Spaces can be customized by adding or removing built-in apps. Admins can provide members of a Space with an enterprise Wiki, forum, shared calendars, document repository, and more. Updates within apps are published in each space’s dedicated activity stream, where members can view recent team activities and share feedback.</p>
          </div>
          
          <div class="ClearFix RowOdd">
            <h4 class="SpecialTit" id="spaces3">Who can create a Space? Can administrator rights be changed?</h4>
            <p>Any member of your company network can create a space, and by default the creator  will be granted administrator status. The admin can give administrative rights to other members of a Space in the Settings menu.</p>
          </div>
          
          <div class="ClearFix RowOdd">
            <h4 class="SpecialTit" id="spaces4">Can I control the privacy settings of a Space that I’ve created?</h4>
            <p>Visibility and access settings can be customized depending on your requirements; a private space is visible and accessible only to selected invitees, while others can be visible to everyone, with either open or approval-required membership.</p>
          </div>
    		    
          <div class="ClearFix RowOdd">
            <h4 class="SpecialTit" id="spaces5">How do I find and join existing Spaces?</h4>
            <p>Browse your company network’s spaces using the “Find Spaces” function. Open spaces can be joined by any employee, while others may be joined on request with administrator approval.</p>
          </div>
          
    
          <h1 id="documents">Documents</h1>
    
          <div class="ClearFix RowOdd">
            <h4 class="SpecialTit" id="documents1">Who can view my documents?</h4>
            <p>By default any document you add to your personal repository is private and only accessible to you. Documents added to collaboration Spaces can be shared with group members. In addition, you can share documents with all colleagues in your employee network by adding them to the Public folder in the Documents app.</p>
          </div>
    
          <div class="ClearFix RowOdd">
            <h4 class="SpecialTit" id="documents2">What type of document can I upload?</h4>
            <p>You can upload any type of document, but only images, videos, and PDF files can be visualized  from the web interface.</p>
          </div>
    
          <h1 id="mobile">Mobile</h1>
    
    	  <div class="ClearFix RowOdd">
            <h4 class="SpecialTit" id="mobile1">What can I do with the eXo mobile apps?</h4>
            <p>For the full feature list, visit the <a href="/mobile.jsp">mobile apps page</a>.</p>
          </div>
    
          <div class="ClearFix RowOdd">
            <h4 class="SpecialTit" id="mobile2">How do I download the mobile app?</h4>
            <p>You can download the eXo Mobile app on the Apple Store and Android Store under the name "eXo Platform 3.5".</p>
          </div>
    
          <div class="ClearFix RowOdd">
            <h4 class="SpecialTit" id="mobile3">How do I connect my device to my company’s social intranet?</h4>
            <p>After installing and opening eXo Mobile on your device, select the settings tab from the homepage. In the server list, press "Modify Server" and enter your company network’s URL (for example, mycompany.cloud-workspaces.com).</p>
          </div>
    
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
    </div>
    
    <!-- BEGIN: LOOPFUSE TRACKING -->
    <script type="text/javascript" src="http://lfov.net/webrecorder/js/listen.js"></script>
    <!-- END: LOOPFUSE TRACKING -->
    <script type="text/javascript" src="/js/trackers.js"></script>    
  </body>
</html>
