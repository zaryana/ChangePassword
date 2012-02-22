/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
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
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Node;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Context;

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
		String strUrl = "http://" + masterhost +"/rest/cloud-admin/public-tenant-service/signup-link";
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
				        String err = "";
				        String line;
				        while ((line = in.readLine()) != null) {
				            err += line;
				        }
				        in.close();
			            String msg = "HTTP status" + connection.getResponseCode() + (err != null ? ". Server error: \r\n" + err + "\r\n" : "");
			            log.error(msg);
			            result += "Error";
			        }
			    }
			
		      catch (MalformedURLException e)
		      {
		         log.error(e.getMessage(), e);
		         result += "Error";
		      }
		      catch (IOException e)
		      {
		         log.error(e.getMessage(), e);
		         result += "Error";
		      }

		      finally
		      {
		         if (connection != null)
		         {
		            connection.disconnect();
		         }
		      }
		
		return result;
		}	
	
	
    @GET
    @Path("send-mail/{mail}/{hostname}")
    public Response sendInvitation(@Context SecurityContext sc, @Context UriInfo uriInfo, @PathParam("mail") String mail, 
    		@PathParam("hostname") String hostname) throws Exception {
      
      if (!isValidEmail(mail)) return Response.status(Status.BAD_REQUEST).entity("Invalid email!").build();
      
      String prefixUrl = "http://" + hostname;
      String masterhost = System.getProperty("tenant.masterhost");
      
      String tail = mail.substring(mail.indexOf("@") + 1);
      String domainName = tail.substring(0,tail.indexOf("."));
      
      String subject = "";
      String mailContent = "";
      
      String blacklist = checkBlacklist(masterhost, mail);
      if (blacklist.contains("TRUE")) return Response.status(Status.BAD_REQUEST).entity("Blacklisted email!").build();
      
      String exist = checkExist(masterhost, domainName);
      
      String registration = regisLink(masterhost, mail);
      
      if (!registration.contains("Error")){
            
      try {
                
         RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
         IdentityManager identityManager = (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
      
         String viewerId = getUserId(sc, uriInfo);
         Identity currentIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, viewerId, true);
         String sender = currentIdentity.getProfile().getFullName();
         String myAvatar = currentIdentity.getProfile().getAvatarUrl();
         if (myAvatar == null || myAvatar.isEmpty()) {
        	 myAvatar = LinkProvider.PROFILE_DEFAULT_AVATAR_URL;
         }
         String myAvatarUrl = prefixUrl + myAvatar;
         
         ManageableRepository repository = repoService.getCurrentRepository();
         String currentTenant = repository.getConfiguration().getName();
         
         if (exist.contains("ONLINE")){
            
         if (currentTenant.equals(domainName)) {
         
                 Session session = repository.login();
                 NodeIterator allUsers = session.getRootNode().getNode("Users").getNodes();
                 long numMember = (allUsers.getSize() - 1);
                 String param1 = "";
                 StringBuilder sb = new StringBuilder();

                 if (numMember > 2) {
                	 param1 += numMember + " of your colleagues are already using the <a href='#' style='font-family:verdana,tahoma,serif;color:#464646;font-size:12px;text-decoration:none;' title='" + currentTenant + "'><strong>" + currentTenant + "</strong> </a>workspace.";
                 }
                 
                 int count = 0;
  
                 while (allUsers.hasNext()){
                    Node userNode = allUsers.nextNode();
                    String userID = userNode.getName();
                    if (userID.contains("root")) continue;
                    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userID, true);
                    //String userName = identity.getProfile().getFullName();
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
                    sb.append("<td width='45' align='left'><img src='" + avatarUrl + "' alt='" + userID + "' /></td>");
            }
        
            String param2 = sb.toString();
                 
            Map<String, String> props = new HashMap<String, String>();
            props.put("user.name", sender);
            props.put("avatar", myAvatarUrl);
            props.put("userId", viewerId);
            props.put("workspace.name", domainName);
            props.put("users.number", param1);
            props.put("users.list", param2);
            props.put("registration.link", registration.trim());
            mailContent = getBody("/html/invite-join-ws.html", props);
                 
            subject += sender + " has invited you to join the " + currentTenant + " Workspace";
            
            } else {
            	
                Map<String, String> props = new HashMap<String, String>();
                props.put("user.name", sender);
                props.put("avatar", myAvatarUrl);
                props.put("userId", viewerId);
                props.put("registration.link", registration.trim());
                mailContent = getBody("/html/invite-try-cw.html", props);
            	
                subject += sender + " has invited you to try eXo Cloud Workspace";
            } 
         } else {
             Map<String, String> props = new HashMap<String, String>();
             props.put("user.name", sender);
             props.put("avatar", myAvatarUrl);
             props.put("userId", viewerId);
             props.put("registration.link", masterhost);
             mailContent = getBody("/html/invite-try-cw.html", props);
        	 
             subject += sender + " has invited you to try eXo Cloud Workspace";        	 
         }
       
		  try{
			  String from = sender + " <noreply@exoplatform.com>";
			  MailService mailService = (MailService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(MailService.class);
			  Message message = new Message();
			  message.setFrom(from);
			  message.setTo(mail);
			  message.setSubject(subject);
			  message.setBody(mailContent);
			  message.setMimeType("text/html");
			  mailService.sendMessage(message);
		    
			  }catch (Exception e){
				  e.printStackTrace();
			  }
         
            return Response.ok("Message sent", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
      } catch (Exception e){
            log.error(e.getMessage(), e);
            return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
      }
      } else {
    	  return Response.status(Status.BAD_REQUEST).entity("User cannot be invited").build();
      }
    }
    
    public String checkExist(String masterhost, String tenantName) throws Exception {
		String strUrl = "http://" + masterhost + "/rest/cloud-admin/public-tenant-service/status/" + tenantName;
		URL url;
		HttpURLConnection connection = null;
		String result="";
		
		try {
		        url = new URL(strUrl);
		        connection = (HttpURLConnection) url.openConnection();
		        connection.setRequestMethod("GET");
		        connection.setDoOutput(true);
		        
		        if (connection.getResponseCode() == HTTP_OK){
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
			        String err = "";
			        String line;
			        while ((line = in.readLine()) != null) {
			            err += line;
			        }
			        in.close();
		            String msg = "HTTP status" + connection.getResponseCode() + (err != null ? ". Server error: \r\n" + err + "\r\n" : "");
		            log.error(msg);
		        }
		    }
		
	      catch (MalformedURLException e)
	      {
	         log.error(e.getMessage(), e);
	      }
	      catch (IOException e)
	      {
	         log.error(e.getMessage(), e);
	      }

	      finally
	      {
	         if (connection != null)
	         {
	            connection.disconnect();
	         }
	      }
		
		return result;    	
    }
    
    public String checkBlacklist(String masterhost, String email) throws Exception {
		String strUrl = "http://" + masterhost + "/rest/cloud-admin/public-tenant-service/blacklisted/" + email;
		URL url;
		HttpURLConnection connection = null;
		String result="";
		
		try {
		        url = new URL(strUrl);
		        connection = (HttpURLConnection) url.openConnection();
		        connection.setRequestMethod("GET");
		        connection.setDoOutput(true);
		        
		        if (connection.getResponseCode() == HTTP_OK){
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
			        String err = "";
			        String line;
			        while ((line = in.readLine()) != null) {
			            err += line;
			        }
			        in.close();
		            String msg = "HTTP status" + connection.getResponseCode() + (err != null ? ". Server error: \r\n" + err + "\r\n" : "");
		            log.error(msg);
		        }
		    }
		
	      catch (MalformedURLException e)
	      {
	         log.error(e.getMessage(), e);
	      }
	      catch (IOException e)
	      {
	         log.error(e.getMessage(), e);
	      }

	      finally
	      {
	         if (connection != null)
	         {
	            connection.disconnect();
	         }
	      }
		
		return result;    	
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
  
        public static boolean isValidEmail(String email){
          boolean result = true;
          try {
             InternetAddress emailAddr = new InternetAddress(email);
             emailAddr.validate();
          }
          catch (AddressException ex){
                result = false;
          }
          return result;
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