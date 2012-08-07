package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.plugins.UserJoinTenant;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class UserJoinTenantLog {
	private static Log LOG = ExoLogger.getLogger(UserJoinTenantLog.class);

	//private static final String PLUGIN_HOME = EmailNotificationService.PLUGINS + "/UserJoinTenantNotificationPlugin";
	//private static final String LOG_NODE = PLUGIN_HOME + "/UserJoinTenantLog";
	// TODO: Cannot add node or property to UserJoinTenantNotificationPlugin node, currently adding to root
	private static final String PLUGIN_HOME = "";
	private static final String LOG_NODE = PLUGIN_HOME + "UserJoinTenantLog";

	public List<UserJoinTenantLogEntry> getLog() {
		List<UserJoinTenantLogEntry> log = new ArrayList<UserJoinTenantLogEntry>();
		SessionProvider sProvider = SessionProvider.createSystemProvider();
		try {
			RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
			ManageableRepository currentRepo = repoService.getCurrentRepository();
			Session session = sProvider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo);	

			//Node pluginNode = session.getRootNode().getNode(PLUGIN_HOME);
			Node pluginNode = session.getRootNode();
			if(!pluginNode.hasNode("UserJoinTenantLog")) {
				Node logNode = pluginNode.addNode("UserJoinTenantLog");
				// hide this node
				if(logNode.canAddMixin("exo:hiddenable")){
					logNode.addMixin("exo:hiddenable");
				}
				pluginNode.save();
				setLog(new ArrayList<UserJoinTenantLogEntry>());
			}

			Node logNode = session.getRootNode().getNode(LOG_NODE);
			InputStream is = logNode.getProperty("log").getStream();
			ObjectInputStream ois = new ObjectInputStream(is);
			log = (List<UserJoinTenantLogEntry>)ois.readObject();
			ois.close();
			is.close();
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
		} finally {
			sProvider.close();
		}

		return log;
	}

	public void setLog(List<UserJoinTenantLogEntry> newValue) {
		SessionProvider sProvider = SessionProvider.createSystemProvider();
		try {
			RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
			ManageableRepository currentRepo = repoService.getCurrentRepository();
			javax.jcr.Session session = sProvider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo);	
			Node logNode = session.getRootNode().getNode(LOG_NODE);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(new ArrayList<UserJoinTenantLogEntry>(newValue));
			oos.flush();
			oos.close();

			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			logNode.setProperty("log", bis);
			logNode.save();
			bis.close();
			bos.close();
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
		} finally {
			sProvider.close();
		}
	}

	public void addEntry(String userId, long joinDate) {
		List<UserJoinTenantLogEntry> log = getLog();
		long now = System.currentTimeMillis();
		// remove entries that older than 1 month
		Iterator<UserJoinTenantLogEntry> it = log.iterator();
		while(it.hasNext()){
			UserJoinTenantLogEntry entry = it.next();
			if(now - entry.getJoinDate() > 31*24*60*60*1000L) {
				LOG.debug(entry.getUserId() + " will be removed");
				it.remove();
			}
		}
		log.add(new UserJoinTenantLogEntry(userId, joinDate));
		setLog(log);
	}

}
