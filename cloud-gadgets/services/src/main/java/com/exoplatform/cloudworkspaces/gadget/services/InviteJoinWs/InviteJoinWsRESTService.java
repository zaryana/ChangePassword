/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.exoplatform.cloudworkspaces.gadget.services.InviteJoinWs;

import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Context;

import javax.jcr.RepositoryException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.ext.RuntimeDelegate;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;

import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map.Entry;
import java.util.Scanner;
import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.OutputStreamWriter;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_CREATED;

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.commons.utils.ListAccess;

@Path("invite-join-ws/")
public class InviteJoinWsRESTService implements ResourceContainer {
	private static final Log log = ExoLogger.getLogger(InviteJoinWsRESTService.class);

	private static final CacheControl cacheControl;
	static {
		RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
		cacheControl = new CacheControl();
		cacheControl.setNoCache(true);
		cacheControl.setNoStore(true);
	}
	
    public String regisLink(String masterhost, String email) {
		String strUrl = "http://" + masterhost +"/rest/cloud-admin/cloudworkspaces/tenant-service/signup-link";
		URL url;
		HttpURLConnection connection = null;
		String result = "";
		String params = "user-mail=" + email;

		try {
		        url = new URL(strUrl);
		        connection = (HttpURLConnection) url.openConnection();
		        connection.setRequestMethod("POST");
		        connection.setDoOutput(true);
		        
		        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
		        writer.write(params.toString());
		        writer.flush();
		        writer.close();
		        

		        if ((connection.getResponseCode() == HTTP_OK) || (connection.getResponseCode() == HTTP_CREATED)){
			        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			        String decodedString;
			        while ((decodedString = in.readLine()) != null) {
			            result += decodedString;
			        }
			        in.close();
			        } 
			    else 
			        {
			        	BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
				        String line;
				        while ((line = in.readLine()) != null) {
				            result += line;
				        }
				        in.close();
			        }
			    }
			
		      catch (MalformedURLException e)
		      {
		         log.error(e.getMessage(), e);
		         result += e.getMessage();
		      }
		      catch (IOException e)
		      {
		         log.error(e.getMessage(), e);
		         result += e.getMessage();
		      }

		      finally
		      {
		         if (connection != null)
		         {
		            connection.disconnect();
		         }
		      }
		log.info("Get registration link for email address " + email);
		log.info(result);
		return result;
		}	
	
	
    @GET
    @Path("send-mail/{mail}/{hostname}")
    public Response sendInvitation(@Context SecurityContext sc, @Context UriInfo uriInfo, @PathParam("mail") String mail, 
    		@PathParam("hostname") String hostname) throws Exception {
      
      String prefixUrl = "http://" + hostname;
      
      String masterhost = "", senderLabel = "";
      try {
    	  masterhost = System.getProperty("tenant.masterhost");
      } catch(NullPointerException e){
    	  log.warn("Property tenant.masterhost not found.");
      }
      
      try {
    	  senderLabel = System.getProperty("gatein.mail.smtp.from");
      } catch(NullPointerException e){
    	  log.warn("Property gatein.mail.smtp.from not found.");
      }      
      
      String tail = mail.substring(mail.indexOf("@") + 1);
      String domainName = tail.substring(0,tail.indexOf("."));
      
      String subject = "";
      String mailContent = "";
      
      String registration = regisLink(masterhost, mail);
      
      if (registration.contains("Invalid email")) return Response.status(Status.BAD_REQUEST).entity("Please enter a valid email address.").build();
      if (registration.contains("Require work email")) return Response.status(Status.BAD_REQUEST).entity("Please use a corporate email address.").build();
      
      if (registration.contains("http")){
            
      try {
                
         RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
         IdentityManager identityManager = (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
      
         String viewerId = getUserId(sc, uriInfo);
         Identity currentIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, viewerId, true);
         String sender = "";
         String myAvatar = "";
         if (currentIdentity != null) {
            sender = currentIdentity.getProfile().getFullName();
            myAvatar = currentIdentity.getProfile().getAvatarUrl();
         } else {
        	 log.warn("Can not get identity of current user");
        	 return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to send invitation email. Please contact support.").build();
         }
         if (myAvatar == null || myAvatar.isEmpty()) {
        	 myAvatar = LinkProvider.PROFILE_DEFAULT_AVATAR_URL;
         }
         String myAvatarUrl = prefixUrl + myAvatar;
         
         ManageableRepository repository = repoService.getCurrentRepository();
         String currentTenant = repository.getConfiguration().getName();
         
         if (currentTenant.equals(domainName)) {
        	 
 /*            	 OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
             	 ListAccess<User> usersList = organizationService.getUserHandler().findAllUsers();
             	 long numMember = usersList.getSize() - 1;
             	 User[] allUsers = usersList.load(0, usersList.getSize());
                 
                 String param1 = "";
                 String param2 = "";
                 StringBuilder sb = new StringBuilder();
                 if (numMember <= 10) {
                 if (numMember > 2) {
                	 param1 += "<strong>" + numMember + "</strong> of your colleagues are already using the <a href='#' style='font-family:verdana,tahoma,serif;color:#464646;font-size:12px;text-decoration:none;' title='" + currentTenant + "'><strong>" + currentTenant + "</strong> </a>workspace.";
                 }
                 int count = 0;
  
                 for (User anUser: allUsers){
                    String userID = anUser.getUserName();
                    if (userID.contains("root")) continue;
                    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userID, true);
                    String avatar = identity.getProfile().getAvatarUrl();
                    if (avatar == null || avatar.isEmpty()) {
                            avatar = LinkProvider.PROFILE_DEFAULT_AVATAR_URL;
                    }
                    String avatarUrl = prefixUrl + avatar;
                    count++;
                    if (count > 6) {
                    	sb.append("</tr><tr height='60' valign='middle'>");
                            count = 0;
                    }
                    sb.append("<td width='45' align='left' style='margin:2px 5px'><img width='30' height='30' src='" + avatarUrl + "' alt='" + userID + "' /></td>");
            }
            }
        
            if (sb != null ) param2 = sb.toString();*/
            
            Map<String, String> props = new HashMap<String, String>();
            props.put("user.name", sender);
            props.put("avatar", myAvatarUrl);
            props.put("userId", viewerId);
            props.put("workspace.name", domainName);
            //props.put("users.number", param1);
            //props.put("users.list", param2);
            props.put("registration.link", registration.trim());
            mailContent = getBody("/html/invite-join-ws.html", props);
            subject = new StringBuilder().append(sender)
                                         .append(" has invited you to join the ")
                                         .append(currentTenant)
                                         .append(" social intranet")
                                         .toString();
            
            } else {
            	
                Map<String, String> props = new HashMap<String, String>();
                props.put("user.name", sender);
                props.put("avatar", myAvatarUrl);
                props.put("userId", viewerId);
                props.put("registration.link", registration.trim());
                mailContent = getBody("/html/invite-try-cw.html", props);
            	
                subject = new StringBuilder().append(sender)
                                             .append(" has invited you to try Cloud Workspace")
                                             .toString();
            } 
       
		  try{
			  String from = new StringBuilder().append(sender)
			                                   .append(" <")
			                                   .append(senderLabel)
			                                   .append(">")
			                                   .toString();
			  MailService mailService = (MailService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(MailService.class);
			  Message message = new Message();
			  message.setFrom(from);
			  message.setTo(mail);
			  message.setSubject(subject);
			  message.setBody(mailContent);
			  message.setMimeType("text/html");
			  mailService.sendMessage(message);
	          return Response.ok("Message sent", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
		    
			  }catch (Exception e){
				  log.error(e.getMessage(), e);
				  return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to send invitation email. Please contact support.").build();
			  }
         
      } catch (RepositoryException e){
    	  log.error(e.getMessage(), e);
    	  return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to send invitation email. Please contact support.").build();
      } catch (Exception e){
          log.error(e.getMessage(), e);
          return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to send invitation email. Please contact support.").build();
      }
      } else {
    	  return Response.status(Status.BAD_REQUEST).entity("User already signed up or the social intranet is not ready. Please try again later.").build();
      }
    }
    
    public String getBody(String fileTemplate, Map<String, String> templateProperties){
    	
    	InputStream is =  this.getClass().getResourceAsStream(fileTemplate);
    	String body;
    	try {
    		body = resolveTemplate(is, templateProperties);
    	} catch(FileNotFoundException ex){
    		ex.printStackTrace();
    		body = "";
    	}
    	return body;
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
     
  public String resolveTemplate(InputStream is, Map<String, String> properties) throws FileNotFoundException
     {
        Scanner scanner = new Scanner(is);
        StringBuilder sb = new StringBuilder();

        try
        {
           while (scanner.hasNextLine())
           {
              sb.append(scanner.nextLine()).append(System.getProperty("line.separator"));
           }
        }
        finally
        {
           scanner.close();
        }

        String templateContent = sb.toString();
        if (templateContent != null)
        {
           for (Entry<String, String> property : properties.entrySet())
           {
              templateContent = templateContent.replace("${" + property.getKey() + "}", property.getValue());
           }
        }
        return templateContent;
     }
}
