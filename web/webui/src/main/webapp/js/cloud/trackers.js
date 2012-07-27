/*
 * Copyright (C) 2012 eXo Platform SAS.
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
define([ "jquery", "jquery.string", "cloud/marketo" ], function() {

	function Trackers() {

		/**
		 * Sends data to Marketo and Google Analytics if this is page from domain
		 * with TARGET_DOMAIN_NAME_FOR_TRACKING
		 */
		var MARKETO_TRACKER_ID = "577-PCT-880";
		var TARGET_DOMAIN_NAME_FOR_TRACKING = "cloud-workspaces.com";

		/**
		 * Send data to Google Analytics
		 */
		var loadGoogleAnalyticsTracker = function() {
			(function() {
				var ga = document.createElement('script');
				ga.type = 'text/javascript';
				ga.async = true;
				ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
				var s = document.getElementsByTagName('script')[0];
				s.parentNode.insertBefore(ga, s);
			})();
		};

		/**
		 * Send data to Marketo
		 */
		var loadMarketoTracker = function() {
			$.ajax({
				url : document.location.protocol + '//munchkin.marketo.net/munchkin.js',
				dataType : 'script',
				cache : true,
				success : function() {
					Munchkin.init(MARKETO_TRACKER_ID);
				}
			});
		};

		/**
		 * Return true if document has href with certain domain prefix, for example
		 * "cloud-ide.com" prefix in href
		 * "http://tenant1.cloud-ide.com:8080/cloud/profile.jsp" SYNTAX RULES FOR
		 * DOMAIN NAMES: http://www.nic.cl/CL-IDN-policy.html
		 */
		var testDomainPrefix = function(domainPrefix) {
			var pattern = new RegExp("^http[s]?:\/\/([a-z0-9]([a-z0-9\-]{0,61}[a-z0-9])[.])*" + domainPrefix + ".*$", "i");
			return pattern.test(document.URL);
		}

		// public function to use on a page
		this.load = function() {
			if (true || testDomainPrefix(TARGET_DOMAIN_NAME_FOR_TRACKING)) {
				loadGoogleAnalyticsTracker();
				loadMarketoTracker();
			}
		};
	}
	;

	return new Trackers();
});
