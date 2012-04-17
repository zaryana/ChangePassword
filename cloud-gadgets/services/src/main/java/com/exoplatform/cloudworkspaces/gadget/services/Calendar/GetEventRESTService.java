package com.exoplatform.cloudworkspaces.gadget.services.Calendar;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import javax.ws.rs.core.CacheControl;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.webservice.cs.bean.EventData;
import org.exoplatform.services.rest.resource.ResourceContainer;

@Path("/calendar")
public class GetEventRESTService implements ResourceContainer{
	
  private Log log = ExoLogger.getExoLogger("calendar.webservice");

  static CacheControl cc = new CacheControl();
  static {
	    cc.setNoCache(true);
	    cc.setNoStore(true);
  }
	
  @GET
  @Path("/events/personal/{calids}/{from}/{to}/{limit}")
  @Produces(MediaType.APPLICATION_JSON)

  public Response getEvents(@PathParam("calids") String calids, @PathParam("from") long from, @PathParam("to") long to, @PathParam("limit") long limit) {

    CalendarService calendarService = (CalendarService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
    if(calendarService == null) {
      return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
    }
    List<String> calList = new LinkedList<String>();
    for (String s : calids.split(",")) {
      if (s.trim().length() > 0)
        calList.add(s);
    }
    String username = ConversationState.getCurrent().getIdentity().getUserId();
    EventQuery eventQuery = new EventQuery();
    java.util.Calendar calendar = java.util.Calendar.getInstance();
    calendar.setTimeInMillis(from);
    eventQuery.setFromDate(calendar);
    calendar = java.util.Calendar.getInstance();
    calendar.setTimeInMillis(to);
    eventQuery.setToDate(calendar);
    eventQuery.setLimitedItems(limit);
    eventQuery.setOrderBy(new String[]{Utils.EXO_FROM_DATE_TIME});
    if (calList.size() > 0)
      eventQuery.setCalendarId(calList.toArray(new String[calList.size()]));
    try {
      List<CalendarEvent> events = calendarService.getUserEvents(username, eventQuery);
      EventData data = new EventData();
      data.setInfo(events);
      return Response.ok(data, MediaType.APPLICATION_JSON_TYPE).cacheControl(cc).build();
    } catch (Exception e) {
      if (log.isWarnEnabled()) log.warn(String.format("Getting events for user %s from %s to %s failed", username, from, to), e);
      return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cc).build();
    }
 }
 
  @GET
  @Path("/calendar-link")
 
  public String getCalendarLink() {
	  String calendarLink = "";
	  try{
		  calendarLink = System.getProperty("cs.chatbar.shortcut.baseUrl");
	  } catch(Exception e){
		  log.warn("Cannot get baseUrl property");
	  }
	  return calendarLink;
  }
}
