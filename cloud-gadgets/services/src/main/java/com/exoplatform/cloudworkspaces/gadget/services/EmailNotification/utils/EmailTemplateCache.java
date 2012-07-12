package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.utils;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class EmailTemplateCache extends LocaleCache<String> {
	private static Log LOG = ExoLogger.getLogger(EmailTemplateCache.class);
	private String resourcePath;

	public EmailTemplateCache(String resourcePath) {
		super();
		this.resourcePath = resourcePath;
	}

	@Override
	protected String getFromSource(String locale) throws Exception {
		String template = "";

		SessionProvider sProvider = SessionProvider.createSystemProvider();
		try {
			RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
			ManageableRepository currentRepo = repoService.getCurrentRepository();
			Session session = sProvider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo);	
			
			Node resourceNode = session.getRootNode().getNode(resourcePath);
			String templateFileName = locale + ".html";
			if(!resourceNode.hasNode(templateFileName)) templateFileName = "default.html";
			template = resourceNode.getNode(templateFileName).getNode("jcr:content").getProperty("jcr:data").getString();
			cache.put(locale, template);
		}catch(Exception e) {
			LOG.debug(e.getMessage(), e);
		}finally{
			sProvider.close();
		}

		return template;
	}
}

