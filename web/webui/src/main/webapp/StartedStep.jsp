<%--

    Copyright (C) 2009 eXo Platform SAS.
    
    This is free software; you can redistribute it and/or modify it
    under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation; either version 2.1 of
    the License, or (at your option) any later version.
    
    This software is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    Lesser General Public License for more details.
    
    You should have received a copy of the GNU Lesser General Public
    License along with this software; if not, write to the Free
    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
    02110-1301 USA, or see the FSF site: http://www.fsf.org.

--%>
<%@ page import="com.exoplatform.cloudworkspaces.cloudlogin.CloudLoginServlet"%>
<%@ page import="com.exoplatform.cloudworkspaces.cloudlogin.CloudLoginService"%>
<%@ page import="org.exoplatform.container.PortalContainer"%>
<%@ page language="java" %>
<%
  String contextPath = request.getContextPath() ;
  String lang = request.getLocale().getLanguage();

  String uri = (String)request.getAttribute(CloudLoginServlet.INITIAL_URI_ATTRIBUTE);
  
  CloudLoginService cloudLoginService = (CloudLoginService) PortalContainer.getCurrentInstance(session.getServletContext()).getComponentInstanceOfType(CloudLoginService.class);
  String tenantDomain = cloudLoginService.getCloudTenantDomain();
  
  response.setCharacterEncoding("UTF-8"); 
  response.setContentType("text/html; charset=UTF-8");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="<%=lang%>" lang="<%=lang%>" dir="ltr">
  <head>
    <%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <% String pageName = "Welcome to Cloud Workspaces"; %>
    <%@ include file="common/headStyle.jsp"%>
    <link href="<%=request.getContextPath()%>/css/cloudlogin/textext-1.3.0.css" rel="stylesheet" type="text/css" />
    
    <%@ include file="common/headScript.jsp"%>
    
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/cloudlogin/textext-1.3.0.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/cloudlogin/cloudlogin.js"></script>
    
  </head>
  <body onLoad="CloudLogin.initCloudLogin();">
  
  <div class="GetStartedPage">
  
    <form class="UIFormBox StartedStep" style="display: block;" name="" id="StepProfile" method="POST" action="javascript:void(0);" >
      <h1 class="StartedBarBG">Welcome to Cloud Workspaces - Get started in 3 easy steps</h1>
      <div class="Steps" id="">
        <span class="StepBG"></span>
        <a href="#" onclick="CloudLogin.doNothing();" class="StepSelectIcon" style="left: 60px;">1</a><a href="#" onclick="CloudLogin.showStepSpace();" class="StartedIcon" style="left: 310px;" >2</a><a href="#" onclick="CloudLogin.showStepEmail();" class="StepIcon" style="left: 569px;">3</a>
      </div>
      <h3>Step 1: Complete your profile</h3>
      <p><strong>Securely manage your work files in the cloud - add a few to get started.</strong></p>
      <table class="BorderDot" cols="2">
        <tbody>
          <tr>
            <td class="FormInput"><div class="HelpText">Drag and drop a document to add it to your private folder</div></td>
            <td class="FormButton"> <input type="button" onclick="CloudLogin.validateStepProfile();" value="Next" id="t_submit_profile" class="Button" /></td>
          </tr>
        </tbody>
      </table>
      <div class="ClearFix StartTip">
        <a href="#" class="FR RightStartTip"><img width="264" src="<%=request.getContextPath()%>/background/img_st.png" alt=""/></a>
        <p class="LeftStartTip"><strong>Tip:</strong> Easily access your documents on your iPhone, iPad or Android device with the eXo mobile app. You can keep files private, share them with specific coworkers or publish them in a dedicated space.</p>
      </div>
      <div class="Link"><a href="#" onclick="CloudLogin.exit();" class="Link">Skip to homepage >></a></div>
    </form>
  
    <form class="UIFormBox StartedStep" style="display: none;" name="" id="StepSpace" method="POST" action="javascript:void(0);" >
      <h1 class="StartedBarBG">Welcome to Cloud Workspaces - Get started in 3 easy steps</h1>
      <div class="Steps" id="">
        <span class="StepBG"></span>
        <a href="#" onclick="CloudLogin.showStepProfile();" class="StartedIcon" style="left: 60px;">1</a><a href="#" onclick="CloudLogin.doNothing();" class="StepSelectIcon" style="left: 310px;" >2</a><a href="#" onclick="CloudLogin.showStepEmail();" class="StartedIcon" style="left: 569px;">3</a>
      </div>
      <h3>Step 2:  Join Spaces</h3>
      <p>Create your own dedicated collaboration spaces for your team or specific projects. We've set up your first space to help you get started.</p>
      <table class="BorderDot" cols="2">
        <tbody>
          <tr>
            <td class="FormInput CheckBox">
              <ul class="ClearFix">
                <li class="FL">
                  <input type="checkbox" />Marketing <br/>
                  <input type="checkbox" />Sales
                </li>
                <li class="FL">
                  <input type="checkbox" />Finance<br/>
                  <input type="checkbox" />IT 
                </li>
                <li class="FL">
                  <input type="checkbox" />Accountant<br/>
                  <input type="checkbox" />Design
                </li>
              </ul>
            </td>
            <td class="FormButton"> <input type="button" onclick="CloudLogin.validateStepSpace();" value="Next" id="t_submit_space" class="Button" /></td>
          </tr>
        </tbody>
      </table>
      <div class="ClearFix StartTip">
        <a href="#" class="FR RightStartTip"><img width="264" src="<%=request.getContextPath()%>/background/img_st.png" alt=""/></a>
        <p class="LeftStartTip"><strong>Tip:</strong> Easily access your documents on your iPhone, iPad or Android device with the eXo mobile app. You can keep files private, share them with specific coworkers or publish them in a dedicated space.</p>
      </div>
      <div class="Link"><a href="#" onclick="CloudLogin.exit();">Skip to homepage &gt;&gt;</a></div>
    </form>
  
    <form class="UIFormBox StartedStep" style="display: none;" name="" id="StepEmail" method="POST" action="javascript:void(0);" >
      <h1 class="StartedBarBG">Welcome to Cloud Workspaces - Get started in 3 easy steps</h1>
      <div class="Steps" id="">
        <span class="StepBG"></span>
        <a href="#" onclick="CloudLogin.showStepProfile();" class="StepIcon" style="left: 60px;">1</a><a href="#" onclick="CloudLogin.showStepSpace();" class="StepIcon" style="left: 310px;" >2</a><a href="#" onclick="CloudLogin.doNothing();" class="StepSelectIcon" style="left: 569px;">3</a>
      </div>
      <h3>Step 3: Invite Coworkers</h3>
      <p><strong>Send email invitations to your coworkers to connect with them in your social intranet.</strong><br/>(note: Only @<%=tenantDomain%> email addresses will be invited to your workspace. Other addresses will receive an invitation to discover Cloud Workspaces)</p>
      
      <div id="messageString" class="TenantFormMsg" style="display:none"></div>
      <table class="BorderDot">
        <tbody>
          <tr>
            <td class="FormInput">
              <textarea id="email" name="" class="required InputText"></textarea>
            </td>
            <td class="FormButton"> <input type="button" onclick="CloudLogin.validateStepEmail();" value="Skip" id="t_submit_email" class="Button" /></td>
          </tr>
        </tbody>
      </table>
      <div class="ClearFix StartTip">
        <a href="#" class="FR RightStartTip"><img width="264" src="<%=request.getContextPath()%>/background/img_st.png" alt=""/></a>
        <p class="LeftStartTip"><strong>Tip:</strong> Connecting with your colleagues will allow you to share documents, calendars, wiki pages and view their latest updates in your activity stream.</p>
      </div>
      <div class="Link"><a href="#" onclick="CloudLogin.exit();" class="Link">Skip to homepage >></a></div>
    </form>
    

    
    
  </div>
  <!--end code body here-->
  
  <form class="UIFormBox StartedStep" style="display: none;" name="CloudExitForm" id="CloudExitForm" method="POST" action="<%= CloudLoginServlet.CL_SERVLET_CTX + CloudLoginServlet.CL_SERVLET_URL %>" >
    <% if (uri != null) { %>
    <input type="hidden" name="<%= CloudLoginServlet.CLOUD_REQUESTED_URI %>" id="<%= CloudLoginServlet.CLOUD_REQUESTED_URI %>" value="<%= uri %>" />
    <% } %>
    <input type="hidden" name="<%= CloudLoginServlet.CLOUD_PROCESS_DISPLAYED %>" id="<%= CloudLoginServlet.CLOUD_PROCESS_DISPLAYED %>" value="true" />
  </form>
 
  <script type="text/javascript" src="/js/trackers.js"></script>
     
  </body>
</html>
