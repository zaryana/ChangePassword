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
package com.exoplatform.cloudworkspaces.initializer.rest;

import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.wiki.mow.api.Attachment;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.service.WikiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Created with IntelliJ IDEA.
 * User: makis
 * Date: 6/27/12
 * Time: 4:09 PM
 *
 */

@Path("/cloud-agent/space-service")
public class WorkspacesSpaceService
{

  private static final Logger LOG = LoggerFactory.getLogger(WorkspacesSpaceService.class);

  protected String defaultSpaceName = "Getting Started";

  protected String lowcasedSpaceName = "getting_started";

  String description = "Get started with your social intranet with a few tips and tricks";

  String forumMessage = "Give us your opinion and feature requests at [email]beta@cloud-workspaces.com[/email]!";

  protected String rootUser = "root";

  private Authenticator authenticator;

  private SpaceService spaceService;

  private ForumService forumService;

  private WikiService wikiService;

  public WorkspacesSpaceService(Authenticator authenticator, SpaceService spaceService, ForumService forumService, WikiService wikiService)
  {
    this.authenticator = authenticator;
    this.spaceService = spaceService;
    this.forumService = forumService;
    this.wikiService = wikiService;
  }

  @POST
  @Path("/create-default")
  @RolesAllowed({"cloud-admin", "cloud-manager"})
  public Response createSpace() throws Exception
  {

    Identity identity = authenticator.createIdentity(rootUser);
    ConversationState.setCurrent(new ConversationState(identity));

    try
    {
      // verify if there is no space already created
      Space space = spaceService.getSpaceByDisplayName(defaultSpaceName);
      if (space == null)
      {
        space = new Space();
        space.setDisplayName(defaultSpaceName);
        space.setPrettyName(defaultSpaceName);
        space.setRegistration(Space.OPEN);
        space.setPriority(Space.LOW_PRIORITY);
        space.setVisibility(Space.PUBLIC);
        space.setGroupId("/platform/users");
        space.setDescription(description);
        //DefaultSpaceApplicationHander is the default implementation of SpaceApplicationHandler. You can create your own by extending SpaceApplicationHandler. The default type is "classic" (DefaultSpaceApplicationHandler.NAME = clasic)
        space.setType(DefaultSpaceApplicationHandler.NAME);
        //Preparing avatar
        InputStream inputStream = getClass().getResourceAsStream("/image/getting-started.png");
        AvatarAttachment avatar = new AvatarAttachment(defaultSpaceName, "getting-started.png", "image/png", inputStream, "social", System.currentTimeMillis());
        space.setAvatarAttachment(avatar);

        //create the space
        space = spaceService.createSpace(space, rootUser);
        spaceService.setManager(space, rootUser, true);
        spaceService.updateSpaceAvatar(space);
      }
    } catch (Exception e) {
      String error = "Cannot create default space: " + e.getMessage();
      LOG.error(error, e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }

    //Forum
    //Forum forum = forumService.getForum("spaces", defaultSpaceName);
    try
    {
      Topic topic = new Topic();
      topic.setLink("/portal/intranet/forum/topic/"+topic.getId());
      topic.setTopicName("What do you think about Cloud Workspaces?");
      topic.setOwner(rootUser);
      topic.setModifiedBy(rootUser);
      topic.setModifiedDate(new Date(System.currentTimeMillis()));
      topic.setLastPostBy(rootUser);
      topic.setLastPostDate(new Date(System.currentTimeMillis()));
      topic.setCreatedDate(new Date(System.currentTimeMillis()));
      topic.setDescription(forumMessage);
      forumService.saveTopic("forumCategoryspaces", "forumSpace" + lowcasedSpaceName, topic, true, false, new MessageBuilder());
    } catch (Exception e) {
      String error = "Cannot create forum topic: " + e.getMessage();
      LOG.error(error, e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }

    //Wiki
    try
    {
      Page page = wikiService.getPageById("group", "/spaces/" + lowcasedSpaceName,  "WikiHome"); //createPage("portal", "intranet", "Getting Started Guide", "WikiHome");
      page.setTitle("Getting Started Guide");
      Attachment content = page.getContent();
      content.setText(readInputStreamAsString(getClass().getResourceAsStream("/wiki/content.txt")));
    } catch (Exception e) {
      String error = "Cannot create wiki page: " + e.getMessage();
      LOG.error(error, e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }

    return Response.ok().build();
  }



  public static String readInputStreamAsString(InputStream in)
    throws IOException
  {
    BufferedInputStream bis = new BufferedInputStream(in);
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    int result = bis.read();
    while(result != -1) {
      byte b = (byte)result;
      buf.write(b);
      result = bis.read();
    }
    return buf.toString();
  }

}
