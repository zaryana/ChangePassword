package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.mail.Session;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.groovyscript.GroovyTemplate;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.quartz.JobExecutionContext;

import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.utils.EmailTemplateCache;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.utils.MessagesCache;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.utils.MultiTenancyJob;

public class EmailNotificationJob extends MultiTenancyJob {
	private static Log LOG = ExoLogger.getLogger(EmailNotificationJob.class);

	@Override
	public Class<? extends MultiTenancyTask> getTask() {
		return EmailNotificationTask.class;
	}
		
	private static void sendMail(String subject, String content, InternetAddress from, InternetAddress to) throws Exception{
		MailService mailService = (MailService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(MailService.class);
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
	}

	private static void addFilesFromDirToNode(String dir, String ext, Node node, String mimeType) throws URISyntaxException, IOException, RepositoryException{
		String[] fileNames = getResourceListing(EmailNotificationService.class, dir + "/");
		InputStream is = null;
		for(String fileName:fileNames){
			if(fileName.endsWith(ext)){
				is = EmailNotificationJob.class.getResourceAsStream("/" + dir + "/" + fileName);
				Node fileNode = node.addNode(fileName, "nt:file").addNode("jcr:content", "nt:resource");
				fileNode.setProperty("jcr:mimeType", mimeType);
				fileNode.setProperty("jcr:data", is);
				fileNode.setProperty("jcr:lastModified", System.currentTimeMillis());
				is.close();
			}
		}		
	}

	private static long nextDayOf(long date) {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(date);
		now.add(Calendar.DAY_OF_YEAR,1);
		
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		return now.getTimeInMillis();
	}

	private static long nextMondayOf(long date) {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(date);
		int weekday = now.get(Calendar.DAY_OF_WEEK);
		int days = weekday == Calendar.SUNDAY ? 1 : Calendar.SATURDAY - weekday + 2;
		now.add(Calendar.DAY_OF_YEAR, days);

		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		return now.getTimeInMillis();
	}
	
	private static long nextMonthOf(long date) {
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(date);
		now.add(Calendar.MONTH, 1);
		now.set(Calendar.DAY_OF_MONTH, 1);
		
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		return now.getTimeInMillis();
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
			 * In case of a jar file, we can't actually find a directory.
			 * Have to assume the same jar as clazz.
			 */
			String me = clazz.getName().replace(".", "/")+".class";
			dirURL = clazz.getClassLoader().getResource(me);
		}

		if (dirURL.getProtocol().equals("jar")) {
			/* A JAR path */
			String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
			JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
			Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
			Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
			while(entries.hasMoreElements()) {
				String name = entries.nextElement().getName();
				if (name.startsWith(path)) { //filter according to the path
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

		throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
	}


	public class EmailNotificationTask extends MultiTenancyTask{
		public EmailNotificationTask(JobExecutionContext context, String repoName) {
			super(context, repoName);
			LOG.debug("EmailNotificationTask initializing for " + repoName);
			SessionProvider sProvider = SessionProvider.createSystemProvider();
			try {
				RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
				ManageableRepository currentRepo = repoService.getRepository(this.repoName);
				javax.jcr.Session session = sProvider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo);	

				Node rootNode = session.getRootNode().getNode("exo:applications");
				if(!rootNode.hasNode("EmailNotification")){
					Node homeNode = rootNode.addNode("EmailNotification", "nt:folder");
					addFilesFromDirToNode(EmailNotificationService.RESOURCE_DIR, ".html", homeNode, "text/html");
					addFilesFromDirToNode(EmailNotificationService.RESOURCE_DIR, ".properties", homeNode, "text/plain");
					homeNode.addNode("plugins", "nt:folder");
					rootNode.save();
				}
				
				//init plugins
				Node pluginsNode = session.getRootNode().getNode(EmailNotificationService.PLUGINS);
				EmailNotificationService emailNotificationService = (EmailNotificationService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(EmailNotificationService.class);
				for(EmailNotificationPlugin plugin:emailNotificationService.getPlugins()) {
					String pluginName = plugin.getName();
					if(!pluginsNode.hasNode(pluginName)){
						Node pluginNode = pluginsNode.addNode(pluginName, "nt:folder");
						addFilesFromDirToNode(EmailNotificationService.PLUGINS_RESOURCE_DIR + "/" + pluginName, ".properties", pluginNode, "text/plain");
						pluginsNode.save();
					}
				}
				
			} catch(Exception e) {
				LOG.debug(e.getMessage(), e);
			} finally {
				sProvider.close();
			}
		}
		
		@Override
		public void run() {
			LOG.debug("EmailNotificationTask running for " + repoName);
			SessionProvider sProvider = SessionProvider.createSystemProvider();
			try {
				super.run();
				IdentityManager identityManager = (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
				OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
				NodeHierarchyCreator nodeCreator = (NodeHierarchyCreator) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
				EmailNotificationService emailNotificationService = (EmailNotificationService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(EmailNotificationService.class);

				MessagesCache messagesCache = new MessagesCache(EmailNotificationService.HOME);
				EmailTemplateCache templatesCache = new EmailTemplateCache(EmailNotificationService.HOME);
				Map<String, MessagesCache> pluginMessagesCaches = new HashMap<String, MessagesCache>();
				
				for(EmailNotificationPlugin plugin:emailNotificationService.getPlugins()) {
					pluginMessagesCaches.put(plugin.getName(), new MessagesCache(EmailNotificationService.PLUGINS + "/" + plugin.getName()));					
				}

				ListAccess<User> laUsers = organizationService.getUserHandler().findAllUsers();
				
				for (User user:laUsers.load(0, laUsers.getSize())) {
					String userId = user.getUserName();
					String userLocale = organizationService.getUserProfileHandler().findUserProfileByName(userId).getAttribute("user.language");
					Node userPrivateNode = nodeCreator.getUserNode(sProvider, userId).getNode("Private");
					if(userPrivateNode == null) continue;
					
					if(userPrivateNode.hasNode(EmailNotificationService.PREFS)){
						Node emailNotificationPrefs = userPrivateNode.getNode(EmailNotificationService.PREFS);
						String interval = emailNotificationPrefs.getProperty("Interval").getString();
						
						if(!emailNotificationPrefs.hasProperty("LastRun")) {
							emailNotificationPrefs.setProperty("LastRun", System.currentTimeMillis());
							emailNotificationPrefs.save();
						}
						
						long lastRun = emailNotificationPrefs.getProperty("LastRun").getLong();
						long nextRun = lastRun;

						if(interval.equals("never")){
							continue;
						} else if(interval.equals("day")){
							nextRun = nextDayOf(lastRun);
						} else if(interval.equals("week")){
							nextRun = nextMondayOf(lastRun);
						} else if(interval.equals("month")){
							nextRun = nextMonthOf(lastRun);
						}
												
						if(System.currentTimeMillis() < nextRun) {
							continue;
						}
						
						List<String> notificationPlugins = Arrays.asList(emailNotificationPrefs.getProperty("NotificationPlugins").getString().split(","));
						
						StringBuilder builder = new StringBuilder();
						
						Map<String, Object> runningContext = new HashMap<String, Object>();
						runningContext.put("userId", userId);
						runningContext.put("userLocale", userLocale);
						runningContext.put("repoName", this.repoName);
						runningContext.put("lastRun", new Long(lastRun));
						
						for(EmailNotificationPlugin plugin:emailNotificationService.getPlugins()) {
							if(notificationPlugins.contains(plugin.getName())){
								runningContext.put("pluginMessagesCache", pluginMessagesCaches.get(plugin.getName()));
								String pluginNotification = plugin.exec(runningContext);
								if(!pluginNotification.isEmpty()) builder.append(pluginNotification);
							}
						}

						emailNotificationPrefs.setProperty("LastRun", System.currentTimeMillis());
						emailNotificationPrefs.save();

						String notifications = builder.toString();
						if(notifications.isEmpty()) continue;

						GroovyTemplate mailTemplate = new GroovyTemplate(templatesCache.get(userLocale));
						Map<String, String> binding;
						Properties prop = messagesCache.get(userLocale);
						
						binding = new HashMap<String, String>();
						binding.put("user", user.getFirstName());
						binding.put("interval", prop.getProperty(interval));
						binding.put("notifications", notifications);
						binding.put("accountSettingsLink", this.repoName + "." + System.getProperty("tenant.masterhost") + "/" + messagesCache.getDefault().getProperty("accountSettingsLink"));
						
				        Profile userProfile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false).getProfile();
				        InternetAddress userAddr = new InternetAddress(userProfile.getEmail(), userProfile.getFullName());
				        
						String subject = prop.getProperty("subject");
						String fromEmail = prop.getProperty("from.email");
						String fromName = prop.getProperty("from.name");
						
						sendMail(subject, mailTemplate.render(binding), new InternetAddress(fromEmail, fromName), userAddr);						
						LOG.info("Notification mail sent to " + userAddr.getAddress());
					}
				}
			} catch(Exception e) {
				LOG.debug(e.getMessage(), e);
			} finally {
				sProvider.close();
			}
		}

	}
	
}
