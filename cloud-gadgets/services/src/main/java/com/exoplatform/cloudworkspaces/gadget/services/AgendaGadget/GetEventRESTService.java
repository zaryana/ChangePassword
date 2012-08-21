package com.exoplatform.cloudworkspaces.gadget.services.AgendaGadget;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.DataStorage;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.service.impl.JCRDataStorage;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.webservice.cs.bean.EventData;
import org.exoplatform.webservice.cs.calendar.CalendarWebservice;

@Path("/calendar")
public class GetEventRESTService extends CalendarWebservice {
	
  private Log log = ExoLogger.getExoLogger("calendar.webservice");
  
  public static final String CALENDAR_APP_ID       = "cs-calendar:spaces";

  public static final String EVENT_ADDED           = "EventAdded".intern();

  public static final String EVENT_UPDATED         = "EventUpdated".intern();

  public static final String EVENT_ID_KEY          = "EventID".intern();

  public static final String CALENDAR_ID_KEY       = "CalendarID".intern();

  public static final String TASK_ADDED            = "TaskAdded".intern();

  public static final String TASK_UPDATED          = "TaskUpdated".intern();

  public static final String EVENT_TYPE_KEY        = "EventType".intern();

  public static final String EVENT_SUMMARY_KEY     = "EventSummary".intern();

  public static final String EVENT_TITLE_KEY       = "EventTitle".intern();

  public static final String EVENT_DESCRIPTION_KEY = "EventDescription".intern();

  public static final String EVENT_LOCALE_KEY      = "EventLocale".intern();

  public static final String EVENT_STARTTIME_KEY   = "EventStartTime".intern();

  public static final String EVENT_ENDTIME_KEY     = "EventEndTime".intern();
  
  public static final String EVENT_LINK_KEY        = "EventLink";
  
  public static final String INVITATION_DETAIL     = "/invitation/detail/";
  
  public static final String SPACE_CALENDAR_ID_SUFFIX = "_space_calendar";
  

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
	    org.exoplatform.services.security.Identity identity = ConversationState.getCurrent().getIdentity();
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
    if ("0".equals(splitCalId[0])) isGroupCal = false;
    event.setCalendarId(splitCalId[1]);
    event.setSummary(eTitle);
    event.setEventType(eventType);
    java.util.Calendar calendar = java.util.Calendar.getInstance();
    calendar.setTimeInMillis(from);
    event.setFromDateTime(calendar.getTime());
    calendar.setTimeInMillis(to);
    event.setToDateTime(calendar.getTime());
    try {
    	if (isGroupCal) {
    		String calID = splitCalId[1];
  		  NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
  		  RepositoryService reposervice = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
  		  CacheService cservice = (CacheService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CacheService.class);
  		  DataStorage dataStorage = new JCRDataStorage(nodeHierarchyCreator, reposervice, cservice);
  		  
  		  dataStorage.savePublicEvent(calID, event, true);
  		  postPublicEvent(event, calID, username);
    	}
    	else calendarService.saveUserEvent(username, splitCalId[1], event, true);
    } catch (Exception e) {
      log.error("Can not create event",e);
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
  
  private Map<String, String> makeActivityParams(CalendarEvent event, String calendarId, String eventType,  String username) {
    Map<String, String> params = new HashMap<String, String>();
    params.put(EVENT_TYPE_KEY, eventType);
    params.put(EVENT_ID_KEY, event.getId());
    params.put(CALENDAR_ID_KEY, calendarId);
    params.put(EVENT_SUMMARY_KEY, event.getSummary());
    params.put(EVENT_LOCALE_KEY, event.getLocation() != null ? event.getLocation() : "");
    params.put(EVENT_DESCRIPTION_KEY, event.getDescription() != null ? event.getDescription() : "");
    params.put(EVENT_STARTTIME_KEY, String.valueOf(event.getFromDateTime().getTime()));
    params.put(EVENT_ENDTIME_KEY, String.valueOf(event.getToDateTime().getTime()));
    params.put(EVENT_LINK_KEY, makeEventLink(event, username, calendarId));
    return params;
  }
  
  private String makeEventLink(CalendarEvent event, String username, String calendarID) {
    StringBuilder evenLinkStringBuilder= new StringBuilder("");
    try {
      String spaceName = "";
      if (calendarID.contains("_space_calendar")) spaceName = calendarID.substring(0, calendarID.indexOf("_space_calendar"));
      String requestPath = "/" + spaceName + "/calendar";
      String requestSiteName = ":spaces:" + spaceName;
      String portalContainerName = ExoContainerContext.getCurrentContainer().getContext().getName();

      evenLinkStringBuilder.append("/")
        .append(portalContainerName)
        .append("/g/")
        .append(requestSiteName)
        .append(requestPath)
        .append(INVITATION_DETAIL)
        .append(username)
        .append("/").append(event.getId())
        .append("/").append(Calendar.TYPE_PUBLIC);
      
    } catch (Exception e) {
      if (log.isWarnEnabled()) 
        log.warn(String.format("Could not create url for the event %s", event.getId()), e);
      return "";
    }
    return evenLinkStringBuilder.toString();
  }
  
  private void publishActivity(CalendarEvent event, String calendarId, String eventType, String username) {
    try {
      Class.forName("org.exoplatform.social.core.space.spi.SpaceService");
    } catch (ClassNotFoundException e) {
      if (log.isDebugEnabled()) {
        log.debug("eXo Social components not found!", e);
      }
      return;
    }
    if (calendarId == null || calendarId.indexOf(SPACE_CALENDAR_ID_SUFFIX) < 0) {
      return;
    }
    try {
      IdentityManager identityM = (IdentityManager) PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class);
      ActivityManager activityM = (ActivityManager) PortalContainer.getInstance().getComponentInstanceOfType(ActivityManager.class);
      SpaceService spaceService = (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
      String spacePrettyName = calendarId.split(SPACE_CALENDAR_ID_SUFFIX)[0];
      Space space = spaceService.getSpaceByPrettyName(spacePrettyName);
      if (space != null) {
        String userId = ConversationState.getCurrent().getIdentity().getUserId();
        Identity spaceIdentity = identityM.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
        Identity userIdentity = identityM.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false);
        ExoSocialActivity activity = new ExoSocialActivityImpl();
        activity.setUserId(userIdentity.getId());
        activity.setTitle(event.getSummary());
        activity.setBody(event.getDescription());
        activity.setType(CALENDAR_APP_ID);
        activity.setTemplateParams(makeActivityParams(event, calendarId, eventType, username));

        activityM.saveActivityNoReturn(spaceIdentity, activity);
      }
    } catch (Exception e) {
      if (log.isErrorEnabled())
        log.error("Can not record Activity for space when event added ", e);
    }
  }
  
  public void postPublicEvent(CalendarEvent event, String calendarId, String username) {
    String eventType = event.getEventType().equalsIgnoreCase(CalendarEvent.TYPE_EVENT) ? EVENT_ADDED : TASK_ADDED;
    publishActivity(event, calendarId, eventType, username);
  }
}
