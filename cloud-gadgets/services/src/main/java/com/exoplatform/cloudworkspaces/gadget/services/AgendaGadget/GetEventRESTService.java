package com.exoplatform.cloudworkspaces.gadget.services.AgendaGadget;

import java.util.List;
import java.util.ArrayList;
import java.net.URI;
import java.util.Set;
import java.util.Collection;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import javax.ws.rs.core.CacheControl;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.webservice.cs.bean.EventData;
import org.exoplatform.webservice.cs.calendar.CalendarWebservice;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;

@Path("/calendar")
public class GetEventRESTService extends CalendarWebservice {
	
  private Log log = ExoLogger.getExoLogger("calendar.webservice");

  static CacheControl cc = new CacheControl();
  static {
	    cc.setNoCache(true);
	    cc.setNoStore(true);
  }
	
  /**
   * @param calids
   * @param from
   * @param to
   * @param limit
   * @return Events and tasks list created in Personal and Groups Calendar of current user
   */
  
  @GET
  @Path("/events/personal/{from}/{to}/{limit}")
  @Produces(MediaType.APPLICATION_JSON)

  public Response getEvents(@PathParam("from") long from, @PathParam("to") long to, @PathParam("limit") long limit, 
		  					@Context SecurityContext sc, @Context UriInfo uriInfo) {

    CalendarService calendarService = (CalendarService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
    if(calendarService == null) {
      return Response.status(HTTPStatus.UNAVAILABLE).cacheControl(cc).build();
    }
    
    String username = getUserId(sc, uriInfo);
  try {    
    StringBuilder sb1 = new StringBuilder();
    StringBuilder sb2 = new StringBuilder();
    List<org.exoplatform.calendar.service.Calendar> calList = calendarService.getUserCalendars(username, true);
	for(org.exoplatform.calendar.service.Calendar c : calList){
		sb1.append(c.getId()).append(",");
	}
	String[] userCalList = sb1.toString().split(",");
    List<GroupCalendarData> lgcd = calendarService.getGroupCalendars(getUserGroups(username), true, username);
    for(GroupCalendarData g : lgcd) {
    	for(org.exoplatform.calendar.service.Calendar c : g.getCalendars()){
    		sb2.append(c.getId()).append(",");
    	}
    }
    String[] groupCalList = sb2.toString().split(",");
    
    EventQuery eventQuery = new EventQuery();
    java.util.Calendar calendar = java.util.Calendar.getInstance();
    calendar.setTimeInMillis(from);
    eventQuery.setFromDate(calendar);
    calendar = java.util.Calendar.getInstance();
    calendar.setTimeInMillis(to);
    eventQuery.setToDate(calendar);
    eventQuery.setLimitedItems(limit);
    eventQuery.setOrderBy(new String[]{Utils.EXO_FROM_DATE_TIME});
    eventQuery.setCalendarId(userCalList);
    List<CalendarEvent> userEvents = calendarService.getUserEvents(username, eventQuery);
    eventQuery.setCalendarId(groupCalList);
    List<CalendarEvent> groupEvents = calendarService.getPublicEvents(eventQuery);
    userEvents.addAll(groupEvents);
    EventData data = new EventData();
    
    data.setInfo(userEvents);
    
      return Response.ok(data, MediaType.APPLICATION_JSON_TYPE).cacheControl(cc).build();
    } catch (Exception e) {
      if (log.isWarnEnabled()) log.warn(String.format("Getting events for user %s from %s to %s failed", username, from, to), e);
      return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cc).build();
    }
 }
 
  /**
   * 
   * @return prefix url of link to calendar page configured in configuration.properties
   */
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

  /**
   * 
   * @param sc
   * @param uriInfo
   * @return all Personal and Groups Calendar of user
   */
  @GET
  @Path("/get-all-cals")
  public Response getAllCalendars(@Context SecurityContext sc, @Context UriInfo uriInfo){
	  try {
	    String username = getUserId(sc, uriInfo);
	    if (username == null) {
	      return Response.status(Status.UNAUTHORIZED).build();
	    }
	    CalendarService calendarService = (CalendarService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
	    List<org.exoplatform.calendar.service.Calendar> calList = calendarService.getUserCalendars(username, true);
	    List<GroupCalendarData> lgcd = calendarService.getGroupCalendars(getUserGroups(username), true, username);
	    List<String> calIds = new ArrayList<String>();
	    for(GroupCalendarData g : lgcd) {
	    	for(org.exoplatform.calendar.service.Calendar c : g.getCalendars()){
	    		if(!calIds.contains(c.getId())){
	    			calIds.add(c.getId());
	    			calList.add(c);
	    		}
	    	}
	    }
	    EventData data = new EventData();
	    data.setCalendars(calList);
	    return Response.ok(data, MediaType.APPLICATION_JSON).cacheControl(cc).build();
	  } catch(Exception e){
		  return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cc).build();
	  }
  }  
  
/**
 * 
 * @param sc
 * @param uriInfo
 * @return All Personal and Group Calendars that user have edit permission right
 */
  @GET
  @Path("/get-editable-cals")
  public Response getEditableCalendars(@Context SecurityContext sc, @Context UriInfo uriInfo){
	  try {
	    String username = getUserId(sc, uriInfo);
	    if (username == null) {
	      return Response.status(Status.UNAUTHORIZED).build();
	    }
	    CalendarService calendarService = (CalendarService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
	    List<org.exoplatform.calendar.service.Calendar> calList = calendarService.getUserCalendars(username, true);
	    List<GroupCalendarData> lgcd = calendarService.getGroupCalendars(getUserGroups(username), true, username);
	    String[] checkPerms = getCheckPermissionString().split(",");
	    List<String> calIds = new ArrayList<String>();
	    for(GroupCalendarData g : lgcd) {
	    	for(org.exoplatform.calendar.service.Calendar c : g.getCalendars()){
	    		if(hasEditPermission(c.getEditPermission(), checkPerms) && (!calIds.contains(c.getId()))){
	    			calIds.add(c.getId());
	    			calList.add(c);
	    		}
	    	}
	    }
	    EventData data = new EventData();
	    data.setCalendars(calList);
	    return Response.ok(data, MediaType.APPLICATION_JSON).cacheControl(cc).build();
	  } catch(Exception e){
		  return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cc).build();
	  }
  }
  
  public static String getCheckPermissionString() throws Exception {
	    Identity identity = ConversationState.getCurrent().getIdentity();
	    StringBuffer sb = new StringBuffer(identity.getUserId());
	    Set<String> groupsId = identity.getGroups();
	    for (String groupId : groupsId) {
	      sb.append(",").append(groupId).append("/:").append("*.*");
	      sb.append(",").append(groupId).append("/:").append(identity.getUserId());
	    }
	    Collection<MembershipEntry> memberships = identity.getMemberships();
	    for (MembershipEntry membership : memberships) {
	      sb.append(",").append(membership.getGroup()).append("/:").append("*." + membership.getMembershipType());
	    }
	    return sb.toString();
  }
  
  public static boolean hasEditPermission(String[] savePerms, String[] checkPerms) {
	    if(savePerms != null)
	      for(String sp : savePerms) {
	        for (String cp : checkPerms) {
	          if(sp.equals(cp)) {return true ;}      
	        }
	      }
	    return false ;
  }
  
  public static final String[] getUserGroups(String username) throws Exception {
	    OrganizationService organization = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class) ;
	    Object[] objs = organization.getGroupHandler().findGroupsOfUser(username).toArray() ;
	    String[] groups = new String[objs.length] ;
	    for(int i = 0; i < objs.length ; i ++) {
	      groups[i] = ((Group)objs[i]).getId() ;
	    }
	    return groups ;
  }
  
  /**
   * 
   * @param calId
   * @param eTitle
   * @param from
   * @param to
   * @param eventType
   * @param sc
   * @param uriInfo
   * @return add a new task/event
   */
  @POST
  @Path("/addevent")
  public Response addEvent(@FormParam("calId") String calId, @FormParam("eventTitle") String eTitle, 
                           @FormParam("from") long from, @FormParam("to") long to, @FormParam("eventType") String eventType,
                           @Context SecurityContext sc, @Context UriInfo uriInfo) {
	  
    String username = getUserId(sc, uriInfo);
    boolean isGroupCal = true;
    if (username == null) {
      return Response.status(Status.UNAUTHORIZED).build(); // unauthorized
    }
    
    if (from > to || from < 0) {
      return Response.status(Status.BAD_REQUEST).entity("Selected time is not valid.").build(); // bad request
    }
    CalendarService calendarService = (CalendarService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CalendarService.class);
    CalendarEvent event = new CalendarEvent();
    String[] splitCalId = calId.split(":");
    if (splitCalId[0].equals("0")) isGroupCal = false;
    event.setCalendarId(splitCalId[1]);
    event.setSummary(eTitle);
    event.setEventType(eventType);
    java.util.Calendar calendar = java.util.Calendar.getInstance();
    calendar.setTimeInMillis(from);
    event.setFromDateTime(calendar.getTime());
    calendar.setTimeInMillis(to);
    event.setToDateTime(calendar.getTime());
    try {
    	if (isGroupCal) calendarService.savePublicEvent(splitCalId[1], event, true);
    	else calendarService.saveUserEvent(username, splitCalId[1], event, true);
    } catch (Exception e) {
      e.printStackTrace();
      return Response.serverError().build();
    }
    return Response.ok().cacheControl(cc).build();
  }

  private String getUserId(SecurityContext sc, UriInfo uriInfo) {
    try {
        return sc.getUserPrincipal().getName();
    } catch (NullPointerException e) {
        return getViewerId(uriInfo);
    } catch (Exception e) {
        return null;
    }
  }

  private String getViewerId(UriInfo uriInfo) {
    URI uri = uriInfo.getRequestUri();
    String requestString = uri.getQuery();
    if (requestString == null) return null;
       String[] queryParts = requestString.split("&");
    for (String queryPart : queryParts) {
       if (queryPart.startsWith("opensocial_viewer_id")) {
        return queryPart.substring(queryPart.indexOf("=") + 1, queryPart.length());
       }
    }
    return null;
  }
}
