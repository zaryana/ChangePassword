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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: makis
 * Date: 6/27/12
 * Time: 4:09 PM
 *
 */

@Path("/cloud-agent/space-service")
public class WorkspacesSpaceService  {

  private static final Logger LOG = LoggerFactory.getLogger(WorkspacesSpaceService.class);

  protected String defaultSpaceName = "Getting Started";

  protected String lowcasedSpaceName = "getting_started";

  String description = "Get started with your social intranet with a few tips and tricks";

  String forumMessage = "Give us your opinion and feature requests at beta@cloud-workspaces.com!";

  protected String rootUser = "root";

  private Authenticator authenticator;

  private SpaceService spaceService;

  private ForumService forumService;

  public WorkspacesSpaceService(Authenticator authenticator, SpaceService spaceService, ForumService forumService){
    this.authenticator = authenticator;
    this.spaceService = spaceService;
    this.forumService = forumService;
  }

  @POST
  @Path("/create-default")
  @RolesAllowed({ "cloud-admin", "cloud-manager" })
  public Response createSpace() throws Exception {

    Identity identity = authenticator.createIdentity(rootUser);
    ConversationState.setCurrent(new ConversationState(identity));

    try {
    // verify if there is no space already created
    Space space = spaceService.getSpaceByDisplayName(defaultSpaceName);
    if (space == null) {
      space = new Space();
      space.setDisplayName(defaultSpaceName);
      space.setPrettyName(defaultSpaceName);
      space.setRegistration(Space.OPEN);
      space.setPriority(Space.LOW_PRIORITY);
      space.setVisibility(Space.PRIVATE);
      space.setDescription(description);
      //DefaultSpaceApplicationHander is the default implementation of SpaceApplicationHandler. You can create your own by extending SpaceApplicationHandler. The default type is "classic" (DefaultSpaceApplicationHandler.NAME = clasic)
      space.setType(DefaultSpaceApplicationHandler.NAME);
      //Preparing avatar
      InputStream inputStream = getClass().getResourceAsStream("/image/getting-started.png");
      AvatarAttachment avatar = new AvatarAttachment(defaultSpaceName, "getting-started.png","image/png", inputStream, "social", System.currentTimeMillis());
      space.setAvatarAttachment(avatar);

      //create the space
      space = spaceService.createSpace(space, rootUser);
      spaceService.setManager(space, rootUser, true);
      spaceService.updateSpaceAvatar(space);
     }
    }
    catch  (Exception e)
    {
      String error = "Cannot create default space: " + e.getMessage();
      LOG.error(error, e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }

      //Forum
      //Forum forum = forumService.getForum("spaces", defaultSpaceName);
      try {
      Topic topic = new Topic();
      topic.setTopicName("What do you think about Cloud Workspaces?");
      topic.setOwner(rootUser);
      topic.setModifiedBy(rootUser);
      topic.setModifiedDate(new Date(System.currentTimeMillis()));
      topic.setLastPostBy(rootUser);
      topic.setLastPostDate(new Date(System.currentTimeMillis()));
      topic.setCreatedDate(new Date(System.currentTimeMillis()));
      topic.setDescription(forumMessage);
      forumService.saveTopic("forumCategoryspaces", "forumSpace"+ lowcasedSpaceName, topic, true, false, new MessageBuilder());
      }
      catch  (Exception e)
      {
        String error = "Cannot create forum topic: " + e.getMessage();
        LOG.error(error, e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
      }
    return Response.ok().build();
  }
}
