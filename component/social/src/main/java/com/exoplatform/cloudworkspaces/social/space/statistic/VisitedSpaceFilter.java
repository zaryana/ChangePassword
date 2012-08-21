package com.exoplatform.cloudworkspaces.social.space.statistic;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.web.filter.Filter;

public class VisitedSpaceFilter implements Filter {

  private VisitedSpaceService visitedSpaceService;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    String uri = httpServletRequest.getRequestURI();
    if (!uri.contains(":spaces:")) {
      chain.doFilter(request, response);
      return;
    }
    String spaceId = uri.split(":spaces:")[1];
    spaceId = spaceId.split("/", 2)[0];
    spaceId = spaceId.replace(":", "/");
    spaceId = new StringBuffer().append("spaces/").append(spaceId).toString();

    getVisitedSpaceService().saveLastVisitedSpaces(spaceId, httpServletRequest.getRemoteUser());
    chain.doFilter(request, response);
  }

  public VisitedSpaceService getVisitedSpaceService() {
    if (this.visitedSpaceService == null) {
      visitedSpaceService = (VisitedSpaceService) PortalContainer.getInstance().getComponentInstanceOfType(VisitedSpaceService.class);
    }
    return this.visitedSpaceService;
  }
}
