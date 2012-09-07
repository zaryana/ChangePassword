<%@page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
  <head>
    <% String pageName = "Reset password"; %>
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
        <div class="UIPageBody UIResetPassword">
          <form class="UIFormBox" action="javascript:void(0);" id="resetPasswordForm" method="post">
            <h1 class="TitleForm">Reset Password</h1>
            <div class="TenantFormMsg" id="messageString"></div>
            <table id="tbl_reset">
              <tr>
                <td class="Field">Your Email:</td><td><input class="required InputText" id="email" type="text" value="Enter Your Email to Reset Your Password" onclick="this.value='';" /><span class="Star">*</span></td>
              </tr>
              <tr>
                <td class="Field">&nbsp;</td><td><input class="Button" type="submit" id="t_submit" value="Change my password" /></td>
              </tr>
            </table>
          </form>
        </div>
      </div>
        
      <!--begin Footer-->
      <%@ include file="common/footer.jsp"%>
    </div>
  </body>
</html>
