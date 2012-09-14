package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.utils;

import java.io.InputStream;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.EmailNotificationService;

public class EmailTemplateCache extends LocaleCache<String> {
  private static Log LOG = ExoLogger.getLogger(EmailTemplateCache.class);
  private Class cls;

  public EmailTemplateCache(Class cls) {
    super();
    this.cls = cls;
  }

  @Override
  protected String getFromSource(String locale) throws Exception {
    String template = "";

    InputStream is = cls.getResourceAsStream(EmailNotificationService.RESOURCE_DIR + locale + ".html");
    if (null == is)
      is = cls.getResourceAsStream(EmailNotificationService.RESOURCE_DIR + "default.html");

    if (null != is) {
      template = new java.util.Scanner(is).useDelimiter("\\A").next();
      cache.put(locale, template);
      is.close();
    }

    return template;
  }
}
