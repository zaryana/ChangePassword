package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.jcr.Node;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
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

  public static final String APP_NAME = "EmailNotification";
  public static final String PREFS = "Prefs";
  public static final String STORAGE = "Storage";
  public static final String RESOURCE_DIR = "/conf/" + APP_NAME + "/";
  public static final String PLUGINS_RESOURCE_DIR = RESOURCE_DIR + "plugins/";
  
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
  
  public Set<Event> getEvents(String plugin, String user) throws Exception {
/*    CacheData<Set<Event>> data = eventsCache.get(new IdentityKey(plugin, user));
    if (data != null && data.build().isEmpty()==false) {
      return (Set<Event>) data.build();
    }*/
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    NodeHierarchyCreator nodeCreator = (NodeHierarchyCreator) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
    Node userAppDataNode = nodeCreator.getUserNode(sProvider, user).getNode("ApplicationData");
    String emailNotificationStorage = EmailNotificationService.APP_NAME + "/" + EmailNotificationService.STORAGE;
    if (userAppDataNode != null && userAppDataNode.hasNode(emailNotificationStorage)) {
      Node storage = userAppDataNode.getNode(emailNotificationStorage);
      Set<Event> events = getEventsFromStorage(storage, plugin);
      eventsCache.put(new IdentityKey(plugin, user), new IdentityData(events));
      return events;
    }
    return new HashSet<Event>();
  }

  public void setEvents(String plugin, String user, Set<Event> events) throws Exception {
    /*   CacheData<Set<Event>> data = eventsCache.get(new IdentityKey(plugin, user));
    if (data != null && data.build().equals(events)) {
      return;
    }*/
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    NodeHierarchyCreator nodeCreator = (NodeHierarchyCreator) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
    Node userAppDataNode = nodeCreator.getUserNode(sProvider, user).getNode("ApplicationData");
    if (userAppDataNode != null) {
      Node emailNotifNode, storageNode;
      if (!userAppDataNode.hasNode(APP_NAME)){
        emailNotifNode = userAppDataNode.addNode(APP_NAME);
        userAppDataNode.save();
      } else {
        emailNotifNode = userAppDataNode.getNode(APP_NAME);
      }
      
      if (!emailNotifNode.hasNode(STORAGE)){
        storageNode = emailNotifNode.addNode(STORAGE);
        emailNotifNode.save();
      } else {
        storageNode = emailNotifNode.getNode(STORAGE);
      }
      
      setEventsToStorage(storageNode, plugin, events);
      eventsCache.put(new IdentityKey(plugin, user), new IdentityData(events));
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
  
  private Set<Event> getEventsFromStorage(Node storage, String plugin) throws Exception {
    if (storage.hasNode(plugin)) {
      Node pluginNode = storage.getNode(plugin);
      InputStream inputStream = pluginNode.getProperty("events").getStream();
      ObjectInputStream oInputStream = new ObjectInputStream(inputStream);
      Set<Event> events = (Set<Event>) oInputStream.readObject();
      oInputStream.close();
      return events;
    } else {
      return new HashSet<Event>();
    }
  }

  private void setEventsToStorage(Node storage, String plugin, Set<Event> events) throws Exception {
    if (!storage.hasNode(plugin)) {
      storage.addNode(plugin);
      storage.save();
    }
    Node pluginNode = storage.getNode(plugin);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream os = new ObjectOutputStream(bos);
    os.writeObject(events);
    os.close();
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    pluginNode.setProperty("events", bis);
    pluginNode.save();
  }
    
}






