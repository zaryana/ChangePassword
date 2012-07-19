<%@page contentType="text/html; charset=UTF-8" language="java" import="com.exoplatform.cloud.multitenancy.TenantNameResolver" %>
<%
   String tenantName = TenantNameResolver.getTenantName(request.getRequestURL().toString());
   if (tenantName == null)
   {
%>
<%@ include file="home.jsp"%>
<%
   }
   else
   {
      response.sendRedirect("/portal/intranet/home");
   }
%>
