package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class EmailNotificationService {
	private static Log LOG = ExoLogger.getLogger(EmailNotificationService.class);
	
	public static final String HOME = "exo:applications/EmailNotification";
	public static final String PLUGINS = HOME + "/plugins";
	public static final String PREFS = "EmailNotificationPrefs";

	public static final String RESOURCE_DIR = "conf/EmailNotification";
	public static final String PLUGINS_RESOURCE_DIR = RESOURCE_DIR + "/plugins";
	
	private List<EmailNotificationPlugin> plugins;
	
	public EmailNotificationService() {
		plugins = new ArrayList<EmailNotificationPlugin>();
	}
	
	public List<EmailNotificationPlugin> getPlugins() {
		return plugins;
	}

	public void addNotificationPlugin(EmailNotificationPlugin plugin) {
		plugins.add(plugin);
		LOG.info("Plugin added: " + plugin.getName() + " - " + plugin.getDescription());
	}
	
	public void initResourceBundle(String currentRepoName) {
	  SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
      ManageableRepository currentRepo = repoService.getRepository(currentRepoName);
      javax.jcr.Session session = sProvider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo);

      Node rootNode = session.getRootNode().getNode("exo:applications");
      if (!rootNode.hasNode("EmailNotification")) {
        LOG.debug("Initializing email notification resource bundle in tenant: " + currentRepo.getConfiguration().getName());
        Node homeNode = rootNode.addNode("EmailNotification", "nt:folder");
        addFilesFromDirToNode(EmailNotificationService.RESOURCE_DIR, ".html", homeNode, "text/html");
        addFilesFromDirToNode(EmailNotificationService.RESOURCE_DIR, ".properties", homeNode, "text/plain");
        homeNode.addNode("plugins", "nt:folder");
        rootNode.save();
      }

      // init plugins
      Node pluginsNode = session.getRootNode().getNode(EmailNotificationService.PLUGINS);
      EmailNotificationService emailNotificationService = (EmailNotificationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(EmailNotificationService.class);
      for (EmailNotificationPlugin plugin : emailNotificationService.getPlugins()) {
        String pluginName = plugin.getName();
        if (!pluginsNode.hasNode(pluginName)) {
          Node pluginNode = pluginsNode.addNode(pluginName, "nt:folder");
          addFilesFromDirToNode(EmailNotificationService.PLUGINS_RESOURCE_DIR + "/" + pluginName, ".properties", pluginNode, "text/plain");
          pluginsNode.save();
        }
      }

    } catch (Exception e) {
      LOG.debug(e.getMessage(), e);
    } finally {
      sProvider.close();
    }
  }
  
  private static void addFilesFromDirToNode(String dir, String ext, Node node, String mimeType) throws URISyntaxException, IOException, RepositoryException {
    String[] fileNames = getResourceListing(EmailNotificationService.class, dir + "/");
    InputStream is = null;
    for (String fileName : fileNames) {
      if (fileName.endsWith(ext)) {
        is = EmailNotificationJob.class.getResourceAsStream("/" + dir + "/" + fileName);
        Node fileNode = node.addNode(fileName, "nt:file").addNode("jcr:content", "nt:resource");
        fileNode.setProperty("jcr:mimeType", mimeType);
        fileNode.setProperty("jcr:data", is);
        fileNode.setProperty("jcr:lastModified", System.currentTimeMillis());
        is.close();
      }
    }
  }
  
  /**
   * List directory contents for a resource folder. Not recursive.
   * This is basically a brute-force implementation.
   * Works for regular files and also JARs.
   * 
   * @author Greg Briggs
   * @param clazz Any java class that lives in the same place as the resources you want.
   * @param path Should end with "/", but not start with one.
   * @return Just the name of each member item, not the full paths.
   * @throws URISyntaxException 
   * @throws IOException 
   */
  private static String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
    URL dirURL = clazz.getClassLoader().getResource(path);
    if (dirURL != null && dirURL.getProtocol().equals("file")) {
      /* A file path: easy enough */
      return new File(dirURL.toURI()).list();
    }

    if (dirURL == null) {
      /*
       * In case of a jar file, we can't actually find a directory. Have to
       * assume the same jar as clazz.
       */
      String me = clazz.getName().replace(".", "/") + ".class";
      dirURL = clazz.getClassLoader().getResource(me);
    }

    if (dirURL.getProtocol().equals("jar")) {
      /* A JAR path */
      String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); // strip out only the JAR file
      JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
      Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries in jar
      Set<String> result = new HashSet<String>(); // avoid duplicates in case it is a subdirectory
      while (entries.hasMoreElements()) {
        String name = entries.nextElement().getName();
        if (name.startsWith(path)) { // filter according to the path
          String entry = name.substring(path.length());
          int checkSubdir = entry.indexOf("/");
          if (checkSubdir >= 0) {
            // if it is a subdirectory, we just return the directory name
            entry = entry.substring(0, checkSubdir);
          }
          result.add(entry);
        }
      }
      return result.toArray(new String[result.size()]);
    }

    throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
  }

}






