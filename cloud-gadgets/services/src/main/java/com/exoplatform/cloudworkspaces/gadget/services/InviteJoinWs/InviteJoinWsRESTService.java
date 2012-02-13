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
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;
import javax.jcr.NodeIterator;
//import javax.jcr.Session;
import javax.jcr.Node;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Context;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
//import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;
import java.util.Properties;
import java.net.URI;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;

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
	
	@GET
    @Path("get-masterhost")
    public String getMasterName(@Context SecurityContext sc, @Context UriInfo uriInfo) {
		    String masterHost = System.getProperty("tenant.masterhost");
		    return masterHost;
		}      
	
    @GET
    @Path("send-mail/{mail}/{protocol}/{hostname}/{masterhost}/{exist}/{adminProperties}/{uuid}")
    public String hello(@Context SecurityContext sc, @Context UriInfo uriInfo, @PathParam("mail") String mail, 
    		@PathParam("protocol") String protocol, @PathParam("hostname") String hostname, @PathParam("masterhost") String masterhost, 
    		@PathParam("exist") String exist, @PathParam("adminProperties") String adminProperties, @PathParam("uuid") String uuid) throws Exception {
      
      if (!isValidEmail(mail)) return "Invalid email";

      String prefixUrl = protocol + "//" + hostname;
      
      String tail = mail.substring(mail.indexOf("@") + 1);
      String domainName = tail.substring(0,tail.indexOf("."));
      
      String subject = "";
      String mailContent = "";
            
      try {
                
         RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
         IdentityManager identityManager = (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
      
         String viewerId = getUserId(sc, uriInfo);
         Identity currentIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, viewerId, true);
         String sender = currentIdentity.getProfile().getFullName();
         
         ManageableRepository repository = repoService.getCurrentRepository();
         String currentTenant = repository.getConfiguration().getName();
         
         if (!exist.contains("NOT_FOUND")){
            
         if (currentTenant.equals(domainName)) { //truong hop sender va receiver co cung domain
         
                 javax.jcr.Session session = repository.login();
                 NodeIterator allUsers = session.getRootNode().getNode("Users").getNodes();
                 long numMember = (allUsers.getSize() - 1);

                 mailContent = "<p><b>" + sender + "</b> has invited you to join the <b>" + currentTenant + "</b> Workspace. Join this social workspace to collaborate with your co-workers in Wikis, Forums, Calendars, Gadgets and more. ";
                 if (numMember > 2) {
                	 mailContent += "<br/>" + numMember + " of your colleagues are already using the <b>" + currentTenant + "</b> Workspace. ";
                 }
                 mailContent += "<a href='http://" + masterhost + "/registration.jsp?email=" + mail + "?id=" + uuid + "'>Join them now</a>.</p>";
                 mailContent += "<p><table><tr>";
                 int count = 0;
  
                 while (allUsers.hasNext()){
                    Node userNode = allUsers.nextNode();
                    String userID = userNode.getName();
                    if (userID.contains("root")) continue;
                    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userNode.getName(), true);
                    String userName = identity.getProfile().getFullName();
                    String avatar = identity.getProfile().getAvatarUrl();
                    if (avatar == null || avatar.isEmpty()) {
                            avatar = LinkProvider.PROFILE_DEFAULT_AVATAR_URL;
                    }
                    String avatarUrl = prefixUrl + avatar;
                    count++;
                    if (count > 4) {
                            mailContent += "</tr><tr>";
                            count = 0;
                    }
                    mailContent += "<td><span><img src='" + avatarUrl + "' alt='" + userNode.getName() + "' width='60' height='60' /></span><span> " + userName + " </span></td>";
            }
        
            mailContent += "</tr></table></p>";
            subject += sender + " has invited you to join the " + currentTenant + " Workspace";
            
            } else { //truong hop sender va receiver khac domain
                mailContent += "<p><b>" + sender + "</b> has invited you to try eXo Cloud Workspace.</p>";
                mailContent += "<p><a href='http://" + masterhost + "/registration.jsp?email=" + mail + "?id=" + uuid + "'> Join your co-workers</a> in the <b>" + domainName + "</b> social workspace to collaborate in Wikis, Forums, Calendars, Gadgets and more.</p>";
                subject += sender + " has invited you to try eXo Cloud Workspace";
            } 
         } else {
        	 
             mailContent += "<p>" + sender + " has invited you to try eXo Cloud Workspace. Create your private social workspace to collaborate with your co-workers in Wikis, Forums, Calendars, Gadgets and more. ";
             mailContent += "<a href='http://" + masterhost + "'>Start now</a> by creating the <b>" + domainName + "</b> workspace.</p>";
             subject += sender + " has invited you to try eXo Cloud Workspace";        	 
         }
       
            //Send mail
            String status = sendMail(sender, mail, subject, mailContent, adminProperties);
        
            return status;
      } catch (Exception e){
            log.error(e.getMessage(), e);
            return "Exception";
      }
    }
  
        public boolean isValidEmail(String email){
          boolean result = true;
          try {
             InternetAddress emailAddr = new InternetAddress(email);
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
  
     public String sendMail(String sender, String email, String subject, String body, String properties)
        {

        String[] myProperties = properties.split(";");

      String from = sender + " <" + myProperties[7] + ">";

      Properties props = new Properties();

      // SMTP protocol properties
      props.put("mail.transport.protocol", myProperties[0]);
      props.put("mail.smtp.host", myProperties[1]);
      props.put("mail.smtp.port", myProperties[2]);
      props.put("mail.smtp.auth", myProperties[3]);

      javax.mail.Session mailSession;
      if (Boolean.parseBoolean(props.getProperty("mail.smtp.auth")))
      {
         props.put("mail.smtp.socketFactory.port",myProperties[4]);
         props.put("mail.smtp.socketFactory.class",myProperties[5]);
         props.put("mail.smtp.socketFactory.fallback",myProperties[6]);

         final String mailUserName = myProperties[7];
         final String mailPassword = myProperties[8];

         mailSession = javax.mail.Session.getInstance(props, new Authenticator()
         {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
               return new PasswordAuthentication(mailUserName, mailPassword);
            }
         });
      }
      else
      {
         mailSession = javax.mail.Session.getInstance(props);
      }

      try
      {
         MimeMessage message = new MimeMessage(mailSession);
         message.setContent(body, "text/html");
         message.setSubject(subject);
         message.setFrom(new InternetAddress(from));
         message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));

         Transport.send(message, message.getRecipients(Message.RecipientType.TO));
         return "Message sent";
      }
      catch (NoSuchProviderException e)
      {
             log.error(e.getMessage(), e);
             return "Sending error";
      }
      catch (MessagingException e)
      {
             log.error(e.getMessage(), e);
             return "Sending error";
      }  
   }    
}   
