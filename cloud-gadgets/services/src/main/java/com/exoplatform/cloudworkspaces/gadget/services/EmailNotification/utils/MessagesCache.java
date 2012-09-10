package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.utils;

import java.io.InputStream;
import java.util.Properties;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.EmailNotificationPlugin;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.EmailNotificationService;

public class MessagesCache extends LocaleCache<Properties> {
  private static Log LOG = ExoLogger.getLogger(MessagesCache.class);
  private Class<EmailNotificationPlugin> cls;

  public MessagesCache(Class cls) {
    super();
    this.cls = cls;
  }

  @Override
  protected Properties getFromSource(String locale) throws Exception {
    Properties prop = new Properties();

    String baseDir = (cls.getSuperclass().equals(EmailNotificationPlugin.class)) ? EmailNotificationService.PLUGINS_RESOURCE_DIR + cls.getSimpleName() + "/": EmailNotificationService.RESOURCE_DIR;
    InputStream is = cls.getResourceAsStream(baseDir + locale + ".properties");
    if (null == is)
      is = cls.getResourceAsStream(baseDir + "default.properties");

    if (null != is) {
      prop.load(is);
      cache.put(locale, prop);
      is.close();
    }

    return prop;
  }
}
