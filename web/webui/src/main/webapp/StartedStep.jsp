<%--
    Copyright (C) 2012 eXo Platform SAS.

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
<%@ page import="com.exoplatform.cloudworkspaces.cloudlogin.rest.CloudLoginRestService"%>
<%@ page import="com.exoplatform.cloudworkspaces.cloudlogin.impl.CloudLoginServiceImpl"%>
<%@ page import="org.exoplatform.container.PortalContainer"%>
<%@ page language="java" %>
<%
  String contextPath = request.getContextPath() ;
  String lang = request.getLocale().getLanguage();

  String uri = (String)request.getAttribute(CloudLoginServlet.INITIAL_URI_ATTRIBUTE);
  String firstName = (String)request.getAttribute(CloudLoginServlet.USER_PROFILE_FIRSTNAME);
  String lastName = (String)request.getAttribute(CloudLoginServlet.USER_PROFILE_LASTNAME);
  
  /*CloudLoginService cloudLoginService = (CloudLoginService) PortalContainer.getCurrentInstance(session.getServletContext()).getComponentInstanceOfType(CloudLoginService.class);
  String tenantDomain = cloudLoginService.getCloudTenantDomain();*/
  
  response.setCharacterEncoding("UTF-8"); 
  response.setContentType("text/html; charset=UTF-8");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="<%=lang%>" lang="<%=lang%>" dir="ltr">
  <head>
    <!--script type="text/javascript" src="https://getfirebug.com/firebug-lite-debug.js"></script-->
    <%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <% String pageName = "Welcome to Cloud Workspaces"; %>
    <%@ include file="common/headStyle.jsp"%>
    <link href="<%=request.getContextPath()%>/css/cloudlogin/textext-1.3.0.css" rel="stylesheet" type="text/css" />

    <%@ include file="common/headScript.jsp"%>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery-file-upload/vendor/jquery.ui.widget.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery-file-upload/jquery.iframe-transport.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery-file-upload/jquery.fileupload.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/cloudlogin/textext-1.3.0.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/cloudlogin/cloudlogin.js"></script>

  </head>
  <body onLoad="CloudLogin.initCloudLogin(<%=CloudLoginRestService.MAX_AVATAR_LENGTH%>, '<%=CloudLoginRestService.AVATAR_UPLOAD_ID%>', '<%=CloudLoginServiceImpl.getAvatarUriPath()%>', '<%=CloudLoginRestService.getProfileWsPath()%>');">
  <div class="GetStartedPage">
  <div id="wrapper">
    <div id="mask">

    <div class="UIFormBox StartedStep" style="display: block;" name="" id="StepProfile" >
      <h1 class="StartedBarBG">Welcome to Cloud Workspaces - Get started in 3 easy steps</h1>
      <div class="Steps" id="">
        <span class="StepBG"></span>
        <a href="#StepProfile" onclick="CloudLogin.doNothing(event);" class="StepSelectIcon panel" style="left: 60px;">1</a><a href="#StepSpace" class="StepIcon panel" onclick="CloudLogin.validateStepProfile(event);" style="left: 310px;" >2</a><a href="#StepEmail" onclick="CloudLogin.doNothing(event);" class="StepIcon panel" style="left: 569px;">3</a>
      </div>
      <h3>Step 1: Complete your profile</h3>
      <table class="BorderDot" cols="2">
        <tbody>
          <tr>
            <td class="FieldLabel UserInput">First and Last name:</td>
            <td class="FieldComment FieldMini" colspan='2'>
              <input type="text" id="firstNameProfile" value="<%=(firstName!=null ? firstName : "")%>">
              <input type="text" id="lastNameProfile" value="<%=(lastName!=null ? lastName : "")%>">
            </td>
          </tr>
          <tr>
            <td class="FieldLabel UserInput">Your current position:</td>
            <td class="FieldComment" colspan='2'><input type="text" id="posProfile"></td>
          </tr>
          <tr>
            <td class="FieldLabel UserInput">Add a profile picture:</td>
            <td class="FormInput">
              <div class="ClearFix HelpText" id="fileDropZone">
                <span class="FR"><img alt="" src="<%=request.getContextPath()%>/background/img_avt.png" id="avatarImage" style="width: 80px;height: 77px;" /></span>
                <div class="LeftAvt">
                  <p style="text-align: center;">Drag an image here or</p>
                  <span class="fileinput-button">
                    <div class="BTBrowse">Browse</div>
                    <input type="file" name="datafile" id="datafile" />
                  </span>
                </div>
              </div>
            </td>
            <td class="FormButton"> <input type="button" onclick="CloudLogin.validateStepProfile(event);" value="Next" id="t_submit_profile" class="Button" /></td>
          </tr>
        </tbody>
      </table>
      <div class="ClearFix StartTip">
        <a href="#" class="FR RightStartTip"><img width="264" src="<%=request.getContextPath()%>/background/wizard_1.png" alt=""/></a>
        <p class="LeftStartTip"><strong>Tip:</strong> Find and connect with your colleagues to see their latest updates in your activity stream.</p>
      </div>
      <div class="Link"><a href="#" onclick="CloudLogin.exit();" class="Link">Skip to homepage >></a></div>
    </div>

    <form class="UIFormBox StartedStep" style="display: none;" name="" id="StepSpace" method="POST" action="javascript:void(0);" >
      <h1 class="StartedBarBG">Welcome to Cloud Workspaces - Get started in 3 easy steps</h1>
      <div class="Steps" id="">
        <span class="StepBG"></span>
        <a href="#" onclick="CloudLogin.showStepProfile(event);" class="StartedIcon" style="left: 60px;">1</a><a href="#" onclick="CloudLogin.doNothing(event);" class="StepSelectIcon" style="left: 310px;" >2</a><a href="#" onclick="CloudLogin.validateStepSpace(event);" class="StepIcon" style="left: 569px;">3</a>
      </div>
      <h3>Step 2: Join Spaces</h3>
      <p>Create your own collaboration spaces for teams or specific projects. We've set up your first space to help you get started.</p>
      <table class="BorderDot" cols="2">
        <tbody>
          <tr>
            <td class="FormInput CheckBox" id="SpacesContainer" style="display: none;">
              <ul id="SpacesContent" class="ClearFix">
              </ul>
            </td>
            <td class="FormInput CheckBox" id="SpacesLoader">
              <img src="<%=request.getContextPath()%>/background/loader.gif" />
            </td>
            <td class="FormButton"> <input type="button" onclick="CloudLogin.validateStepSpace(event);" value="Next" id="t_submit_space" class="Button" /></td>
          </tr>
        </tbody>
      </table>
      <div class="ClearFix StartTip">
        <a href="#" class="FR RightStartTip"><img width="264" src="<%=request.getContextPath()%>/background/wizard_2.png" alt=""/></a>
        <p class="LeftStartTip"><strong>Tip:</strong> Spaces provide the tools to make your internal work more productive, including wikis, forums, group calendars and more.</p>
      </div>
      <div class="Link"><a href="#" onclick="CloudLogin.exit();">Skip to homepage &gt;&gt;</a></div>
  </form>

    <form class="UIFormBox StartedStep StepEmail" style="display: none;" name="" id="StepEmail" method="POST" action="javascript:void(0);" >
      <h1 class="StartedBarBG">Welcome to Cloud Workspaces - Get started in 3 easy steps</h1>
      <div class="Steps" id="">
        <span class="StepBG"></span>
        <a href="#" onclick="CloudLogin.doNothing(event);" class="StartedIcon" style="left: 60px;">1</a><a href="#" onclick="CloudLogin.doNothing(event);" class="StartedIcon" style="left: 310px;" >2</a><a href="#" onclick="CloudLogin.doNothing(event);" class="StepSelectIcon" style="left: 569px;">3</a>
      </div>
      <h3>Step 3: Invite Coworkers</h3>
      <p class="ST3"><strong>Send email invitations to your coworkers to connect with them in your social intranet.</strong><br/>(note: Only people with the same email @domain name will be invited to your social intranet. Other addresses will receive an invitation to discover Cloud Workspaces)</p>

      <div id="messageString" class="TenantFormMsg" style="display:none"></div>
      <table class="BorderDot">
        <tbody>
          <tr>
            <td class="FormInput">
              <textarea id="email" name="" class="required InputText"></textarea>
            </td>
            <td class="FormButton"> <input type="button" onclick="CloudLogin.validateStepEmail(event);" value="Finish" id="t_submit_email" class="Button" /></td>
          </tr>
        </tbody>
      </table>
      <div class="ClearFix StartTip">
        <a href="#" class="FR RightStartTip" ><img height="140px" style="padding-right: 60px" src="<%=request.getContextPath()%>/background/wizard_3.png" alt=""/></a>
        <p class="LeftStartTipST3" style="margin-right: 355px"><strong>Tip:</strong> Easily access your documents on your iPhone, iPad or Android device with the eXo mobile app.</p>
      </div>
      <div class="Link"><a href="#" onclick="CloudLogin.exit();" class="Link">Skip to homepage >></a></div>
  </form>

  </div>
  </div>
  </div>
  <!--end code body here-->

  <form class="UIFormBox StartedStep" style="display: none;" name="CloudExitForm" id="CloudExitForm" method="POST" action="<%= CloudLoginServlet.CL_SERVLET_CTX + CloudLoginServlet.CL_SERVLET_URL %>" >
    <% if (uri != null) { %>
    <input type="hidden" name="<%= CloudLoginServlet.CLOUD_REQUESTED_URI %>" id="<%= CloudLoginServlet.CLOUD_REQUESTED_URI %>" value="<%= uri %>" />
    <% } %>
    <input type="hidden" name="<%= CloudLoginServlet.CLOUD_PROCESS_DISPLAYED %>" id="<%= CloudLoginServlet.CLOUD_PROCESS_DISPLAYED %>" value="true" />
  </form>
  </body>
</html>
