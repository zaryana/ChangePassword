<%@page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
  <head>
    <% String pageName = "Thank You for Contacting Us"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
  </head>
  <body onLoad="tenants.init();">
    <div class="UIPageContainer">
      <!--begin header-->
      <%@ include file="common/header.jsp"%>

      <!--Contact-us-done page-->
      <div class="UIPageBodyContainer">
        <div class="UIPageBody ThankyouPage">
          <h1>Thank You for Contacting Us</h1>
          <ul>
            <li><a class="Back" href="/index.jsp" title="Back to Cloud Workspaces Homepage">Back to Cloud Workspaces Homepage</a></li>
            <li><a class="Forward" href="/about.jsp" title="go to eXo Cloud Services">Learn More about eXo Cloud Services</a></li>
            <li><a class="Forward" href="http://www.exoplatform.com/company/en/resource-center" title="Find Cloud Tutorials, Video Demos and More in the eXo Resource Center">Find Cloud Tutorials, Video Demos and More in the eXo Resource Center</a></li>
          </ul>
        </div>
      </div>
      
      <!--begin Footer-->
      <%@ include file="common/footer.jsp"%>
    </div>
    
    <!-- BEGIN: LOOPFUSE TRACKING -->
    <script type="text/javascript" src="http://lfov.net/webrecorder/js/listen.js"></script>
    <!-- END: LOOPFUSE TRACKING -->
    <script type="text/javascript" src="/js/trackers.js"></script>
  </body>
</html>
