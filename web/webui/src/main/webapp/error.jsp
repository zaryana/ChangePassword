<%@page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
  <head>
    <% String pageName = "Cloud-workspaces error"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
    <script type="text/javascript" data-main="static" src="/js/require-2.0.4.min.js"></script>
  </head>
  <body>
    <div class="UIPageContainer">
      <!--begin header-->
      <div class="UIHeader">
        <div class="MainContent">
          <ul class="UIMenuTop FR">
            <li><a class="Tab BlueTab" href="/about.jsp">About</a></li>
            <li><a class="Tab GreenTab" href="http://community.exoplatform.com" target="_blank">Community</a></li>
            <li><a class="Tab GrayTab" href="http://blog.exoplatform.com" target="_blank">Blog</a></li>
            <li><a class="Tab OrangeTab" href="http://exoplatform.com" target="_blank">eXoplatform.com</a></li>
            <li class="last"><a class="Tab OrangeTab" href="/contact-us.jsp">Contact us</a></li>
          </ul>
          <div class="Logo FL"><a href="/<%=request.getContextPath()%>"><img src="background/logo.png"/></a></div>
          <div class="ClouIntranet FR"><h1>Cloud Workspaces</h1><span>The free Social Intranet for your company</span></div>
        </div>
      </div>
      <div class="UIPageBodyContainer">
        <div class="UIPageBody ErrorPages">
          <p><span class="ErrorIcon">Sorry, we are currently unable to process your request due to exceptionally high traffic. Please try again later.</span></p>
          <center><a class="BackIcon" href="/<%=request.getContextPath()%>">Back</a></center>
        </div>
      </div>
        
      <!--begin Footer-->  
      <div class="UIFooterPortlet">
        <div class="MainContent ClearFix">
          <p class="FL">Cloud Workspaces is Brought to You by <span><a href="http://www.exoplatform.com">eXo</a></span></p>
          <p class="FR">Copyright &copy; 2000-2012. All Rights Reserved, eXo Platform SAS.</p>
        </div>
      </div>
    </div>
  </body>
</html>
