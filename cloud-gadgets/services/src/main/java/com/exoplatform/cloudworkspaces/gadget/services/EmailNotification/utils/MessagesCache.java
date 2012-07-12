package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.utils;

import java.io.InputStream;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class MessagesCache extends LocaleCache<Properties> {
	private static Log LOG = ExoLogger.getLogger(MessagesCache.class);
	private String resourcePath;

	public MessagesCache(String resourcePath) {
		super();
		this.resourcePath = resourcePath;
	}

	@Override
	protected Properties getFromSource(String locale) throws Exception {
		Properties prop = new Properties();
		
		SessionProvider sProvider = SessionProvider.createSystemProvider();
		try {
			RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
			ManageableRepository currentRepo = repoService.getCurrentRepository();
			Session session = sProvider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo);	
			
			Node resourceNode = session.getRootNode().getNode(resourcePath);
			String propsFileName = locale + ".properties";
			if(!resourceNode.hasNode(propsFileName)) propsFileName = "default.properties";
			InputStream is = resourceNode.getNode(propsFileName).getNode("jcr:content").getProperty("jcr:data").getStream();
			
			prop.load(is);
			cache.put(locale, prop);
			is.close();
			session.logout();
		}catch(Exception e) {
			LOG.debug(e.getMessage(), e);
		}finally{
			sProvider.close();
		}
		
		return prop;
	}
}

