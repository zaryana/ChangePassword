<%@page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
  <head>
    <% String pageName = "Create Your Cloud Workspace"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
    <script type="text/javascript" data-main="user" src="/js/require-2.0.4.min.js"></script>
  </head>
  <body>
    <div class="UIPageContainer">
      <!--begin header-->
      <%@ include file="common/header.jsp"%>
      <!--begin FormPages-->
      <div class="UIPageBodyContainer">
        <div class="UIPageBody FormPages">
          <h1>Create Your Company's Social Intranet</h1>
          <div id="messageString" class="TenantFormMsg"></div>
            <form class="UIForm" action="javascript:void(0);"  method="post" name="cloud-workspaces-profile" id="registrationForm">
              <div id="formDisplay">
                <!-- Marketo input hidden fields -->
                <input type="hidden" name="Cloud_Workspaces_User__c" id="Cloud_Workspaces_User__c" value="yes" />
                <input type="hidden" name="LeadSource" id="LeadSource" value="Web - Cloud Workspaces" />
                <input type="hidden" name="_marketo_comments" value="" />
                <input type="hidden" name="lpId" value="1027" />
                <input type="hidden" name="subId" value="46" />
                <input type="hidden" name="kw" value="" />
                <input type="hidden" name="cr" value="" />
                <input type="hidden" name="searchstr" value="" />
                <input type="hidden" name="lpurl" value="http://http://learn.cloud-workspaces.com/CloudWorkspaces-UserProfile-English.html?cr={creative}&kw={keyword}" />
                <input type="hidden" name="formid" value="1023" />
                <input type="hidden" name="returnURL" value="" />
                <input type="hidden" name="retURL" value="" />
                <input type="hidden" name="_mkt_disp" value="return" />
                <input type="hidden" name="_mkt_trk" value="" />
                <table cols="2">
                  <tr>
                    <td class="Field">Email:</td><td> <input class="required InputText DisabledArea" type="text" name="email" id="email"  disabled value="yourname@yourcompany.com" /></td>
                  </tr>
                  <tr>
                    <td class="Field">First name:</td><td> <input class="required InputText" type="text" name="first_name" id="first_name" /><span class="Star">*</span></td>
                  </tr>
                  <tr>
                    <td class="Field">Last name:</td><td> <input class="required InputText" type="text" name="last_name" id="last_name" /><span class="Star">*</span></td>
                  </tr>
                  <tr>
                    <td class="Field">Phone:</td><td> <input class="required InputText" type="text" name="phone_work" id="phone_work" onkeypress="return onlyNumbers(event);" /><span class="Star">*</span></td>
                  </tr>
                  <tr>
                    <td class="Field">Company:</td><td> <input class="required InputText" type="text" name="company" id="company" /><span class="Star">*</span></td>
                  </tr>
                  <tr>
                    <td class="Field">Username:</td><td> <input class="required InputText DisabledArea" type="text" name="username" disabled id="username" /></td>
                  </tr>
                  <tr>
                    <td class="Field">Password:</td><td> <input class="required InputText" type="password" name="password" id="password" /><span class="Star">*</span></td>
                  </tr>
                  <tr>
                    <td class="Field">Confirm password:</td><td> <input class="required InputText" type="password" name="password2" id="password2" /><span class="Star">*</span></td>
                  </tr>
                  <tr>
                    <td class="Field"></td><td> <input class="Button" type="submit" id="t_submit" value="Create" /></td>
                  </tr>
                </table>
                <input type="hidden" name="confirmation-id" id="confirmation-id" />
            </div>
          </form>
        </div>
      </div>
      <!--begin Footer-->
      <%@ include file="common/footer.jsp"%>
    </div>
    <!--  marketo response container  -->
    <iframe id="mktOutput" name="mktOutput" style='display:none; visibility:hidden'></iframe>
  </body>
</html>
