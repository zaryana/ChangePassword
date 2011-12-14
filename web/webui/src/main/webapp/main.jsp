<%@ page contentType="text/html; charset=UTF-8" language="java" import="org.exoplatform.cloudmanagement.multitenancy.TenantNameResolver" %>
<%
   String tenantName = TenantNameResolver.getTenantName(request.getRequestURL().toString());
   if (tenantName == null)
   {
%>
<%@ include file="jsp/index.jsp"%>
<%
   }
   else
   {
      response.sendRedirect("/portal/intranet/home");
   }
%>
