package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;

import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.cache.CacheData;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.cache.CacheKey;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.cache.IdentityData;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.cache.IdentityKey;

public class EmailNotificationService {
  private static Log LOG = ExoLogger.getLogger(EmailNotificationService.class);

  public static final String HOME = "exo:applications/EmailNotification";
  public static final String PLUGINS = HOME + "/plugins";
  public static final String PREFS = "EmailNotificationPrefs";

  public static final String NT_UNSTRUCTURED = "nt:unstructured";
  public static final String NT_FOLDER = "nt:folder";
  public static final String NT_FILE = "nt:file";
  public static final String STORAGE = "EmailNotificationStorage";
  public static final String RESOURCE_DIR = "conf/EmailNotification";
  public static final String PLUGINS_RESOURCE_DIR = RESOURCE_DIR + "/plugins";

  private List<EmailNotificationPlugin> plugins;
  private CacheService cacheService;
  private ExoCache<CacheKey, CacheData<Set<Event>>> eventsCache;
  private Executor executor;
  private MailService mailService;

  private static final String EMAIL_NOTIFICATIONS_CACHE = "EmailNotificationsCache";

  public EmailNotificationService(CacheService cacheService, MailService mailService) {
    plugins = new ArrayList<EmailNotificationPlugin>();
    this.cacheService = cacheService;
    this.eventsCache = this.cacheService.getCacheInstance(EMAIL_NOTIFICATIONS_CACHE);
    this.executor = Executors.newCachedThreadPool();
    this.mailService = mailService;
  }
	
	public List<EmailNotificationPlugin> getPlugins() {
		return plugins;
	}

	public void addNotificationPlugin(EmailNotificationPlugin plugin) {
		plugins.add(plugin);
		LOG.info("Plugin added: " + plugin.getName() + " - " + plugin.getDescription());
	}
	
	public Set<Event> getEvents(Plugin plugin, String user) throws Exception {
	  CacheData<Set<Event>> data = eventsCache.get(new IdentityKey(plugin, user));
    if (data != null) {
      return (Set<Event>) data.build();
    }
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    NodeHierarchyCreator nodeCreator = (NodeHierarchyCreator) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
    if (user != null && !user.isEmpty()) {
      Node userPrivateNode = nodeCreator.getUserNode(sProvider, user).getNode("Private");
      if (userPrivateNode != null && userPrivateNode.hasNode(STORAGE)) {
        Node storage = userPrivateNode.getNode(STORAGE);
        Set<Event> events = getEventsFromStorage(storage, plugin);
        eventsCache.put(new IdentityKey(plugin, user), new IdentityData(events));
        return events;
      }
    } else {
      initResourceBundle(null);
      Node emailNotificationNode = nodeCreator.getPublicApplicationNode(sProvider).getNode("EmailNotification");
      if (emailNotificationNode != null && emailNotificationNode.hasNode(STORAGE)) {
        Node storage = emailNotificationNode.getNode(STORAGE);
        Set<Event> events = getEventsFromStorage(storage, plugin);
        eventsCache.put(new IdentityKey(plugin, user), new IdentityData(events));
        return events;
      }
    }
    return new HashSet<Event>();
  }

  public void setEvents(Plugin plugin, String user, Set<Event> events) throws Exception {
    CacheData<Set<Event>> data = eventsCache.get(new IdentityKey(plugin, user));
    if (data != null && data.build().equals(events)) {
      return;
    }
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    NodeHierarchyCreator nodeCreator = (NodeHierarchyCreator) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
    if (user != null && !user.isEmpty()) {
      Node userPrivateNode = nodeCreator.getUserNode(sProvider, user).getNode("Private");
      if (userPrivateNode != null) {
        if (!userPrivateNode.hasNode(STORAGE)) {
          userPrivateNode.addNode(STORAGE, NT_UNSTRUCTURED);
          userPrivateNode.save();
        }
        Node storage = userPrivateNode.getNode(STORAGE);
        setEventsToStorage(storage, plugin, events);
        eventsCache.put(new IdentityKey(plugin, user), new IdentityData(events));
      }
    } else {
      initResourceBundle(null);
      Node emailNotificationNode = nodeCreator.getPublicApplicationNode(sProvider).getNode("EmailNotification");
      if (emailNotificationNode != null) {
        if (!emailNotificationNode.hasNode(STORAGE)) {
          emailNotificationNode.addNode(STORAGE, NT_UNSTRUCTURED);
          emailNotificationNode.save();
        }
        Node storage = emailNotificationNode.getNode(STORAGE);
        setEventsToStorage(storage, plugin, events);
        eventsCache.put(new IdentityKey(plugin, user), new IdentityData(events));
      }
    }
  }
  
  public void sendMail(final String subject, final String content, final InternetAddress from, final InternetAddress to) {
    executor.execute(new Runnable() {
      public void run() {
        try {
          Session mailSession = mailService.getMailSession();
          MimeMessage message = new MimeMessage(mailSession);

          message.setSubject(subject);
          message.setFrom(from);
          message.setRecipient(RecipientType.TO, to);

          MimeMultipart mailContent = new MimeMultipart("alternative");
          MimeBodyPart text = new MimeBodyPart();
          MimeBodyPart html = new MimeBodyPart();
          text.setText(content);
          html.setContent(content, "text/html; charset=ISO-8859-1");
          mailContent.addBodyPart(text);
          mailContent.addBodyPart(html);

          message.setContent(mailContent);
          mailService.sendMessage(message);
        } catch (Exception exception) {
          throw new RuntimeException(exception);
        }
      }
    });
  }
	
	public void initResourceBundle(String currentRepoName) {
	  SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
      if (currentRepoName == null || currentRepoName.isEmpty()) {
        currentRepoName = repoService.getCurrentRepository().getConfiguration().getName();
      }
      ManageableRepository currentRepo = repoService.getRepository(currentRepoName);
      javax.jcr.Session session = sProvider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo);

      Node rootNode = session.getRootNode().getNode("exo:applications");
      if (!rootNode.hasNode("EmailNotification")) {
        LOG.debug("Initializing email notification resource bundle in tenant: " + currentRepo.getConfiguration().getName());
        Node homeNode = rootNode.addNode("EmailNotification", NT_UNSTRUCTURED);
        addFilesFromDirToNode(EmailNotificationService.RESOURCE_DIR, ".html", homeNode, "text/html");
        addFilesFromDirToNode(EmailNotificationService.RESOURCE_DIR, ".properties", homeNode, "text/plain");
        homeNode.addNode("plugins", NT_UNSTRUCTURED);
        rootNode.save();
      }

      // init plugins
      Node pluginsNode = session.getRootNode().getNode(EmailNotificationService.PLUGINS);
      EmailNotificationService emailNotificationService = (EmailNotificationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(EmailNotificationService.class);
      for (EmailNotificationPlugin plugin : emailNotificationService.getPlugins()) {
        String pluginName = plugin.getName();
        if (!pluginsNode.hasNode(pluginName)) {
          Node pluginNode = pluginsNode.addNode(pluginName, NT_UNSTRUCTURED);
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
  
	private Set<Event> getEventsFromStorage(Node storage, Plugin plugin) throws Exception {
    if (storage.hasNode(plugin.name())) {
      Node pluginNode = storage.getNode(plugin.name());
      InputStream inputStream = pluginNode.getProperty("events").getStream();
      ObjectInputStream oInputStream = new ObjectInputStream(inputStream);
      Set<Event> events = (Set<Event>) oInputStream.readObject();
      oInputStream.close();
      return events;
    } else {
      return new HashSet<Event>();
    }
  }

  private void setEventsToStorage(Node storage, Plugin plugin, Set<Event> events) throws Exception {
    if (!storage.hasNode(plugin.name())) {
      storage.addNode(plugin.name(), NT_UNSTRUCTURED);
      storage.save();
    }
    Node pluginNode = storage.getNode(plugin.name());
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream os = new ObjectOutputStream(bos);
    os.writeObject(events);
    os.close();
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    pluginNode.setProperty("events", bis);
    pluginNode.save();
  }
	
  private static void addFilesFromDirToNode(String dir, String ext, Node node, String mimeType) throws URISyntaxException, IOException, RepositoryException {
    String[] fileNames = getResourceListing(EmailNotificationService.class, dir + "/");
    InputStream is = null;
    for (String fileName : fileNames) {
      if (fileName.endsWith(ext)) {
        is = EmailNotificationJob.class.getResourceAsStream("/" + dir + "/" + fileName);
        Node fileNode = node.addNode(fileName, NT_FILE).addNode("jcr:content", "nt:resource");
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






