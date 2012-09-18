package com.exoplatform.cloudworkspaces.social.space.statistic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.jcr.RepositoryException;

import javax.jcr.Node;

import org.apache.commons.lang.ArrayUtils;
import org.chromattic.api.ChromatticSession;
import org.chromattic.ext.ntdef.NTFolder;
import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class VisitedSpaceService {

  private static final Log LOG = ExoLogger.getLogger(VisitedSpaceService.class);
  
  private static final String PARENT_RELATIVE_PATH = "SpaceStatistic";
  private final static String CHROMATTIC_LIFECYCLE_NAME = "spacestatistics";
  private final static String LAST_VISITED_SPACES_NODE = "last-visited-spaces";
  private final static String SPACE_ACCESS_LIFECYCLE_ROOT_PATH = "/Users/";
  private final static Integer LAST_VISITED_SPACES_NUMBER_TO_DISPLAY = 5;

  private ChromatticLifeCycle lifeCycle;
  private NodeHierarchyCreator nodeHierarchyCreator;
  private Executor executor;
  private String currentRepo;
  private RepositoryService repoService;
  private SpaceService spaceService;
  
  public VisitedSpaceService(ChromatticManager chromatticManager, NodeHierarchyCreator nodeHierarchyCreator) {
    this.lifeCycle = chromatticManager.getLifeCycle(CHROMATTIC_LIFECYCLE_NAME);
    this.executor = Executors.newCachedThreadPool();
    this.nodeHierarchyCreator = nodeHierarchyCreator;
    this.repoService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    this.spaceService = (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);
  }

  public void saveLastVisitedSpaces(final String spaceId, final String pageUri, final String userId) {
  
    try {
      this.currentRepo = repoService.getCurrentRepository().getConfiguration().getName();
    } catch (RepositoryException e) {
      LOG.error("Repository not found.", e);
    }
    
    executor.execute(new Runnable() {
      public void run() {
        if (lifeCycle.getContext() == null) {
          lifeCycle.openContext();
          
          try {          
            repoService.setCurrentRepositoryName(currentRepo);
          } catch (RepositoryConfigurationException e){
            LOG.error("Impossible to set current repository name.", e);
          }
        }
        
        String parentNodePath = null;
        try {
          parentNodePath = getUserApplicationDataNodePath(userId, true);
        } catch (Exception exception) {
          throw new RuntimeException(exception);
        }
       
        SpaceStatistics visitedSpace = getSession().findByPath(SpaceStatistics.class, parentNodePath + "/" + LAST_VISITED_SPACES_NODE, false);
        if (visitedSpace == null) {
          NTFolder parentNode = getSession().findByPath(NTFolder.class, parentNodePath, false);
          if (parentNode == null) {
            throw new IllegalStateException("User ApplicationData node couldn't be found.");
          }
          visitedSpace = getSession().create(SpaceStatistics.class, LAST_VISITED_SPACES_NODE);
          getSession().persist(parentNode, visitedSpace);
          getSession().save();
          visitedSpace = getSession().findByPath(SpaceStatistics.class, parentNodePath + "/" + LAST_VISITED_SPACES_NODE, false);
        }

        String[] spaces = visitedSpace.getSpaceAccessStatistics();
        String spaceItem = spaceId + "@" + pageUri;
        if (spaces == null || spaces.length == 0) {
          spaces = (String[]) ArrayUtils.add(null, spaceItem);
          visitedSpace.setSpaceAccessStatistics(spaces);
          getSession().save();
          return;
        }
          
        int i = 0;
        while (i < spaces.length) {
          String spaceAccessEntryTmp = spaces[i];
          String prettyName = (spaceAccessEntryTmp.contains("@")) ? spaceAccessEntryTmp.split("@")[0] : spaceAccessEntryTmp;
          if (prettyName.equals(spaceId)){
        	if (i == (spaces.length - 1)) {
        	  spaces[i] = spaceItem;
        	} else {
        	  for (int j=i; j < spaces.length-1 ; j++) {
        	    spaces[j] = spaces[j+1];
        	  }
        	  spaces[spaces.length-1] = spaceItem;
        	}
        	visitedSpace.setSpaceAccessStatistics(spaces);
        	getSession().save();
           	return;        		  
          } 
          i++;
	    }
          
        if (spaces.length == LAST_VISITED_SPACES_NUMBER_TO_DISPLAY) {
          spaces = (String[]) ArrayUtils.remove(spaces, 0);
        }

        spaces = (String[]) ArrayUtils.add(spaces, spaceItem);
        visitedSpace.setSpaceAccessStatistics(spaces);
        getSession().save();
      }
    });
  }
    
  public List<String> getVisitedSpacesList(String userId) {
    String parentNodePath = getUserApplicationDataNodePath(userId, false);
    if (parentNodePath == null) {
      return new ArrayList<String>();
    }
    
    SpaceStatistics visitedSpace = null;
    try {
    	visitedSpace = getSession().findByPath(SpaceStatistics.class, parentNodePath + "/" + LAST_VISITED_SPACES_NODE, false);
    } catch (Exception exception) {
      LOG.error("List of last visited spaces for this user isn't yet created ", exception);
    }
    if (visitedSpace == null || visitedSpace.getSpaceAccessStatistics() == null || visitedSpace.getSpaceAccessStatistics().length == 0) {
      return new ArrayList<String>();
    }
    String[] spaces = visitedSpace.getSpaceAccessStatistics();
    
    List<String> spacesList = new ArrayList<String>();
    for (int i = spaces.length-1; i >= 0 ; i--) {
      String prettyName = (spaces[i].contains("@")) ? spaces[i].split("@")[0] : spaces[i];
      Space space = spaceService.getSpaceByPrettyName(prettyName);
      if (space != null) spacesList.add(spaces[i]);
    } 
    return spacesList;
  }

  private String getUserApplicationDataNodePath(String userId, boolean create) {
    String parentNodePath = null;
    try {
      Node userApplicationNode = nodeHierarchyCreator.getUserApplicationNode(SessionProvider.createSystemProvider(), userId);
      if (!userApplicationNode.hasNode(PARENT_RELATIVE_PATH)) {
        if (create) {
          userApplicationNode = userApplicationNode.addNode(PARENT_RELATIVE_PATH, "nt:folder");
          userApplicationNode.addMixin("mix:referenceable");
          userApplicationNode.getSession().save();
          parentNodePath = userApplicationNode.getPath();
        } else {
          return null;
        }
      } else {
        parentNodePath = userApplicationNode.getPath() + "/" + PARENT_RELATIVE_PATH;
      }
      parentNodePath = parentNodePath.split(SPACE_ACCESS_LIFECYCLE_ROOT_PATH, 2)[1];
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
    return parentNodePath;
  }

  public ChromatticSession getSession() {
    return lifeCycle.getChromattic().openSession();
  }
}
