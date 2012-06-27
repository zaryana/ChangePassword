/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package com.exoplatform.cloudworkspaces.social.webui.activity.plugin;

import java.util.Map;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.social.webui.activity.BaseUIActivityBuilder;

public class NewUserUIActivityBuilder extends BaseUIActivityBuilder {
  
  public static final String NEW_USER = "NEW_USER";
  
  @Override
  protected void extendUIActivity(BaseUIActivity uiActivity, ExoSocialActivity activity) {
    
    UINewUserActivity uiNewUserActivity = (UINewUserActivity) uiActivity;

    Map<String, String> templateParams = activity.getTemplateParams();
    uiNewUserActivity.setNewUserName(templateParams.get(NEW_USER));
   
  }
}
