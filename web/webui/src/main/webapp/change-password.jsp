<%@page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
  <head>
    <% String pageName = "Change password"; %>
    <%@ include file="common/headStyle.jsp"%>
    <%@ include file="common/headScript.jsp"%>
  </head>
  <body onLoad="tenants.initChange();">
    <div class="UIPageContainer">
      <!--begin header-->
      <%@ include file="common/header.jsp"%>
  
      <!--begin FormPages-->
      <div class="UIPageBodyContainer">
        <div class="UIPageBody UIChangePassword">
          <form class="UIFormBox" action="javascript:void(0);"  method="POST" id="changeForm">
            <h1 class="TitleForm">Reset Password</h1>
            <div class="TenantFormMsg" id="messageString"></div>
            <table>
              <tbody>
                <tr>
                  <td class="Field">New Password:</td><td><input type="password" id="password" name="password" class="required InputText" /><span class="Star">*</span></td>
                </tr>
                <tr>
                  <td class="Field">Confirm New Password:</td><td><input type="password" id="password2" name="password2" class="required InputText" /><span class="Star">*</span></td>
                </tr>
                <tr>
                  <td class="Field">&nbsp;</td>
                  <td>
                    <input type="submit" id="submitButton" value="Submit" class="Button" onClick="tenants.doChange();" />
                  </td>
                </tr>
              </tbody>
            </table>
            <input type="hidden"  id="id" name="id"/>
          </form>
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
