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
define(["jquery", "marketo.form"], function(jQuery, Mkto) {

	function Marketo() {
		
		var leadCaptureUrl = "http://learn.cloud-workspaces.com/index.php/leadCapture/save";
		
		var isResponseReceived = function(iframeId) {
			try {
				return !document.getElementById(iframeId) || document.getElementById(iframeId).contentWindow.location.href != "about:blank";
			} catch (e) {
				// check for permission of
				return true;
			}
		};
		
		this.send = function(data, afterSubmitCallback) {
			if (data["_mkt_trk"] == "") {
				afterSubmitCallback();
				return;
			}
			var mktOutputIframeId = "mktOutput";
			var mktOutputIframeName = "mktOutput";
			var mktFormId = "mktForm";
			var mktFormName = "cloud-workspaces-profile";
			var commonData = {
				"LeadSource" : "Web - Cloud Workspaces",
				"marketo_comments" : "",
				"kw" : "",
				"cr" : "",
				"searchstr" : "",
				"lpurl" : "http://learn.cloud-workspaces.com/Cloud-Workspaces-Sign-Up-English.html?cr={creative}&kw={keyword}",
				"returnURL" : "",
				"retURL" : "",
				"_mkt_disp" : "return"
			}

			if (jQuery && document.getElementById(mktOutputIframeId)) {
				jQuery('#' + mktFormId).remove();
				jQuery('#' + mktOutputIframeId).attr('src', "");
				// clear iframe

				jQuery('body').append(jQuery('<form/>', {
					id : mktFormId,
					name : mktFormName,
					method : 'POST',
					action : leadCaptureUrl,
					target : mktOutputIframeName,
					enctype : 'application/x-www-form-urlencoded'
				}));

				for (var i in data) {
					jQuery('#' + mktFormId).append(jQuery('<input/>', {
						type : 'hidden',
						name : i,
						value : data[i]
					}));
				}

				for (var i in commonData) {
					jQuery('#' + mktFormId).append(jQuery('<input/>', {
						type : 'hidden',
						name : i,
						value : commonData[i]
					}));
				}
				Mkto.formSubmit(document.getElementById(mktFormId));

				var i = 200;
				// set limited iterations - interrupt after the 20 seconds
				var afterSubmitHandler = window.setInterval(function() {
					if (!(i--) || isResponseReceived(mktOutputIframeId)) {
						window.clearInterval(afterSubmitHandler);
						if (afterSubmitCallback) {
							afterSubmitCallback();
						}
						jQuery('#' + mktFormId).remove();
						jQuery('#' + mktOutputIframeId).attr('src', "");
						// clear iframe
					}
				}, 10);
			}
		};
	}

	return new Marketo();
});
