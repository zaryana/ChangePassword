/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
/**
 * Created by The eXo UA SAS.
 *
 * @author <a href="dmitry.ndp@gmail.com">Dmytro Nochevnov</a>
 * @version $Id:
 *
 */

/**
 * --------------------- Sends data to LOOPFUSE tracker and Google Analytics if this is page from domain with targetDomainNameForTracking 
 */

var targetDomainNameForTracking = "cloud-workspaces.com";

if (testDomainPrefix(targetDomainNameForTracking))
{
   sendDataToLoopfuseTracker();
   sendDataToGoogleAnalyticsTracker();
}

/**
 *  Send data to LOOPFUSE
 */
function sendDataToLoopfuseTracker()
{
   _lf_cid = "LF_df197061";
   _lf_remora();
}

/**
 *  Send data to Google Analytics
 */
function sendDataToGoogleAnalyticsTracker()
{
   var _gaq = _gaq || [];
   _gaq.push(['_setAccount', 'UA-1292368-30']);
   _gaq.push(['_setDomainName', 'cloud-workspaces.com']);
   _gaq.push(['_trackPageview']);

   (function() {
     var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
     ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
     var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
   })();
}

/**
 * Return true if document has href with certain domain prefix, for example "cloud-ide.com" prefix in href "http://tenant1.cloud-ide.com:8080/cloud/profile.jsp"
 * SYNTAX RULES FOR DOMAIN NAMES: http://www.nic.cl/CL-IDN-policy.html 
 * @param {Object} domainPrefix
 */
function testDomainPrefix(domainPrefix)
{
   var pattern = new RegExp("^http[s]?:\/\/([a-z0-9]([a-z0-9\-]{0,61}[a-z0-9])[.])*" + domainPrefix + ".*$", "i");
   return pattern.test(document.URL); 
}