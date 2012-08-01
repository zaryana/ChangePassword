<%@page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
  <head>
    <% String pageName = "Cloud-workspaces Contact Us"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
    <script type="text/javascript" data-main="support" src="/js/require-2.0.4.min.js"></script>
  </head>
  <body>
    <div class="UIPageBody" id="Content" style="display:none;"></div>
    <div class="UIPageBody ContactPages" id="ContactForm" style="margin: 0px auto; min-height: 0px;">
    <div style="position: absolute; left: 0px; top: 0px; width: 100%; height: 100%; background: #000000; opacity: 0.5; filter: alpha(opacity = 20); ZOOM: 1"></div>
	  <form class="UIForm UIFormBox" action="javascript:void(0);"  method="post" name="" id="mycontactForm" style="top: 66px;">
		<h1 class="TitleForm">Contact Us</h1>
		<div id="messageString" class="TenantFormMsg"></div>
        <div id="formDisplay">
          <table cols="2">
            <tr>
             <td class="Field">First Name:</td><td> <input class="required InputText" type="text" name="FirstName" id="FirstName" value="" /></td>
           </tr>
            <tr>
             <td class="Field">Last Name:</td><td> <input class="required InputText" type="text" name="LastName" id="LastName" value="" /></td>
           </tr>
           <tr>
            <td class="Field">Your email:</td><td> <input class="required InputText" type="text" name="email" id="email" value="" /></td>
           </tr>
           <tr>
            <td class="Field">Phone:</td><td> <input class="required InputText" type="text" name="Phone" id="Phone" /></td>
          </tr>
           <tr>
            <td class="Field">Subject:</td><td> <input class="required InputText" type="text" name="subject" id="subject" /></td>
           </tr>
           <tr>
            <td class="Field">Message:</td><td> <textarea class="required" type="text" name="ContactUs_Message__c" id="ContactUs_Message__c"></textarea></td>
           </tr>
           <tr>
            <td class="Field"></td>
            <td>
            <input class="Button" type="submit"  id="submitButton" value="Send" onClick="tenants.doContactRequest();" />
            <input class="Button ButtonGray" type="" id="cancelButton" value="Cancel" />
            </td>
           </tr>
          </table>
          <!-- Marketo input hidden fields -->
          <input name="LeadSource" id="LeadSource" type='hidden' value="Web - Cloud Workspaces" />
          <input type="hidden" name="_marketo_comments" value="" />
          <input type="hidden" name="lpId" value="1028" />
          <input type="hidden" name="subId" value="46" />
          <input type="hidden" name="kw" value="" />
          <input type="hidden" name="cr" value="" />
          <input type="hidden" name="searchstr" value="" />
          <input type="hidden" name="lpurl" value="http://learn.cloud-workspaces.com/CloudWorkspaces-Contact-Us-English.html?cr={creative}&kw={keyword}" />
          <input type="hidden" name="formid" value="1024" />
          <input type="hidden" name="returnURL" value="" />
          <input type="hidden" name="retURL" value="" />
          <input type="hidden" name="_mkt_disp" value="return" />
          <input type="hidden" name="_mkt_trk" value="" />
        </div>
	  </form>
    </div>
    <!--  marketo response container  -->
    <iframe id="mktOutput" name="mktOutput" style='display:none; visibility:hidden'></iframe>
  </body>
</html>
