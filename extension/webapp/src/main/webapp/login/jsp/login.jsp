<%@ page import="java.net.URLEncoder"%>
<%@ page import="javax.servlet.http.Cookie"%>
<%@ page import="org.exoplatform.container.PortalContainer"%>
<%@ page import="org.exoplatform.services.resources.ResourceBundleService"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page import="org.exoplatform.web.login.InitiateLoginServlet"%>
<%@ page import="org.gatein.common.text.EntityEncoder"%>
<%@ page language="java" %>
<%
  String contextPath = request.getContextPath() ;
  String masterhost = System.getProperty("tenant.masterhost");
  String requestUrl = request.getRequestURL().toString();
  String hostName = requestUrl.substring(0, requestUrl.indexOf(masterhost) + masterhost.length());  
  String serverName = request.getServerName();
  String tname ="";
  if(serverName.indexOf(".")!= -1)
  { 
    tname = serverName.substring(0,serverName.indexOf("."));
  }
  else
  {
    tname = serverName;
  }
  String masterhostUrl = hostName.replace(serverName, masterhost);  

  String username = request.getParameter("j_username");

  if(username == null) username = "";
 	String password = request.getParameter("j_password");
 	if(password == null) password = "";

  ResourceBundleService service = (ResourceBundleService) PortalContainer.getCurrentInstance(session.getServletContext())
  														.getComponentInstanceOfType(ResourceBundleService.class);
  ResourceBundle res = service.getResourceBundle(service.getSharedResourceBundleNames(), request.getLocale()) ;
  
  Cookie cookie = new Cookie(InitiateLoginServlet.COOKIE_NAME, "");
	cookie.setPath(request.getContextPath());
	cookie.setMaxAge(0);
	response.addCookie(cookie);

  String uri = (String)request.getAttribute("org.gatein.portal.login.initial_uri");

  response.setCharacterEncoding("UTF-8"); 
  response.setContentType("text/html; charset=UTF-8");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>Login</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

    <link rel="shortcut icon" type="image/x-icon"  href="<%=contextPath%>/favicon.ico" />   

    <link href="/css/Style.css" rel="stylesheet" type="text/css" />

    <!-- load trackers and contact-us form from CW site modules -->
    <script type="text/javascript" src="/js/common.js"></script>
    <script type="text/javascript" data-main="static" src="/js/require-2.0.4.min.js"></script>
    
    <script type="text/javascript" src="/eXoResources/javascript/eXo.js"></script>
    <script type="text/javascript" src="/eXoResources/javascript/eXo/portal/UIPortalControl.js"></script>
    <style>
        .UIPageBody .LoginForm {
          margin: auto;
          font-family: arial, tahoma, serif;
        }
        
        .UIFormBox .FieldLoginPage {
          float: left;
          width: 190px;
          text-align: right;
          color: #414141;
          font-size: 18px;
          line-height: 40px;
          padding-right: 20px;
        }
        
        .UIPageBody a.LoginPage {
          font-size: 15px;
        }
        
        .UIPageBody .LabelLoginPage {
          font-size: 16px;
          margin-left: 8px;
          color: #414141;
        }
        
        .UIPageBody a.LoginPage:hover {
	  text-decoration: underline;
        }
    </style>
  </head>
  
  <body onload="init();">
    <div id="fb-root"></div>
    <script>
        (function(d, s, id) {
          var js, fjs = d.getElementsByTagName(s)[0];
          if (d.getElementById(id)) return;
          js = d.createElement(s); js.id = id;
          js.src = "https://connect.facebook.net/en_US/all.js#xfbml=1";
          fjs.parentNode.insertBefore(js, fjs);
        }(document, 'script', 'facebook-jssdk'));
        
        function init() {
          if (window.name == "true") {
                  document.getElementById("messageString").style.display = "block";
                  window.name = "false";
          } else {
                  document.getElementById("messageString").style.display = "none";
                  document.getElementById("username").value = "";
          }

          document.getElementById("password").value = "";
        }
    </script>
    <div class="UIPageContainer">       
	         
	    <div class="UIHeader" id="UIHeader">
			  <div class="MainContent">
				  <ul class="UIMenuTop FR">
				    <li><a class="Tab BlueTab" href="<%=hostName%>/about.jsp" target="_parent">About</a></li>
				    <li><a class="Tab GreenTab" href="http://community.exoplatform.org/portal/public/classic/forum/cloud-workspaces/ForumService" target="_parent">Forum</a></li>
				    <li><a class="Tab GrayTab" href="<%=hostName%>/help.jsp" target="_parent">Help</a></li>
				    <li><a class="Tab OrangeTab" href="http://www.exoplatform.com" target="_parent">eXoplatform.com</a></li>
				    <li class="last"><a class="Tab OrangeTab" id="showContactUs">Contact us</a></li>
				  </ul>
				  <div class="Logo FL"><a href="http://cloud-workspaces.com"><img src="/background/logo.png"/></a></div>
				  <div class="ClouIntranet FR"><h1>Cloud Workspaces</h1><span>The Free Social Intranet for Your Company</span></div>
			  </div>
		  </div>
		  <div class="MarkLayer" id="maskLayer" style="width: 100%; display: none;"></div>
		  <div id="contactUsContainer" style="display: none;"></div>  
		       
	    <div class="UIPageBodyContainer">
	      <div class="UIPageBody FormPages">
	        <form class="LoginForm UIFormBox SigninForm" name="signinForm" method="post" action="<%= contextPath + "/login"%>" >
	          <h1 class="TitleForm" id="titleForm">
              Login to <strong>  <%=tname%>  </strong>
	          </h1>
	          <div class="TenantFormMsg" id="messageString" style="display: none">
	            <%
	              if(username.length() > 0 || password.length() > 0) {
	              EntityEncoder encoder = EntityEncoder.FULL;
	              username = encoder.encode(username);
	            %>
	            <%=res.getString("UILoginForm.label.SigninFail")%><%}%>
	          </div>
              <div id="formDisplay">
                <% if (uri != null) { %>
                <input type="hidden" name="initialURI" value="<%=uri%>"/>
                <% } %>			 
                <table cols="2">
                  <tbody>
                    <tr>
                      <td class="FieldLoginPage">Username:</td><td><input type="text" id="username" name="username" class="required InputText" value="<%=username%>" /></td><td></td>
                    </tr>
                    <tr>
                      <td class="FieldLoginPage">Password:</td><td id="UIPortalLoginFormControl" onkeypress="eXo.portal.UIPortalControl.onEnterPress(event);"><input type="password" id="password" name="password" class="required InputText" value="" /></td>
                      <td><a class="LoginPage" style="margin-left: 10px;" href="<%=masterhostUrl%>/reset-password.jsp">Forgot Password?</a></td>
                    </tr>
                    <tr onkeypress="eXo.portal.UIPortalControl.onEnterPress(event);">
                            <td class="FieldLoginPage"></td><td><input type="checkbox" name="rememberme" id="rememberme" value="true"/><label for="rememberme" class="LabelLoginPage">Remember Me</label></td><td></td>
                    </tr>
                    <tr>
                      <td class="FieldLoginPage"></td><td><input type="submit" onclick="login();" value="Login" id="UIPortalLoginFormAction" class="Button"><div style="margin: 10px 5px;"><span style="font-size: 16px; margin-left: 14px; color: #414141;">or </span><a class="LoginPage" href="<%=masterhostUrl%>/">Join Cloud Workspaces</a></div></td>
                      <td></td>
                    </tr>
                  </tbody>
                </table>
                <script type='text/javascript'>			            
                function login() {
                        window.name = "true";	            
                  var username = document.getElementById("username").value;
                  var index = username.indexOf('@');
                  if (index != -1) {
                    username = username.substring(0, index);
                  }
                  document.getElementById("username").value = username;					        
                  document.signinForm.submit();                   
                }
                </script>
	          </div>
	        </form>      
	      </div>
	    </div>
	      
		<div class="UIFooterPortlet">
		  <div class="MainContent ClearFix">
		    <p class="FL">Cloud Workspaces is Brought to You by <span><a href="http://www.exoplatform.com">eXo</a></span></p>
		    <p class="Copyright FR">Copyright &copy; 2000-2012. All Rights Reserved, eXo Platform SAS.</p>
		  </div>
		</div>
    </div>
  </body>
</html>
