<%@ page import="org.exoplatform.cloudmanagement.multitenancy.TenantNameResolver"%>

<%
   String tenantName = TenantNameResolver.getTenantName(request.getRequestURL().toString());
   if (tenantName == null)
   {
%>
<%@ include file="index.html"%>
<%
   }
   else
   {
      response.sendRedirect("/portal/intranet/home");
   }
%>