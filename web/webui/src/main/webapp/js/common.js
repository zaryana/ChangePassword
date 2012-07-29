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

var hostName = location.hostname.indexOf("www") == 0 ? location.hostname.substring(4) : location.hostname;
var queryString = location.search;

if (location.port) {
	hostName += ":" + location.port;
}

var prefixUrl = location.protocol + "//" + hostName;

var user;
var auth = null;

// for thickbox
var tb_pathToImage = "/background/img_video.png";

function setCookie(name, value, minutes) {
	if (minutes) {
		var date = new Date();
		date.setTime(date.getTime() + (minutes * 60 * 1000));
		var expires = "; expires=" + date.toGMTString();
	} else {
		var expires = "";
	}
	document.cookie = name + "=" + value + expires + "; path=/";
}

function getCookie(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for ( var i = 0; i < ca.length; i++) {
		var c = ca[i];
		while (c.charAt(0) == ' ') {
			c = c.substring(1, c.length);
		}
		if (c.indexOf(nameEQ) == 0) {
			return c.substring(nameEQ.length, c.length);
		}
	}
	return null;
}

function onlyNumbers(evt) {
	var charCode = (evt.which) ? evt.which : evt.keyCode;

	if (charCode > 31
			&& ((charCode < 48 || charCode > 57) && charCode != 45 && charCode != 40 && charCode != 41 && charCode != 43)) {
		return false;
	}

	return true;
}

var keyStr = "ABCDEFGHIJKLMNOP" + "QRSTUVWXYZabcdef" + "ghijklmnopqrstuv" + "wxyz0123456789+/" + "=";

function encode64(input) {
	var output = "";
	var chr1, chr2, chr3 = "";
	var enc1, enc2, enc3, enc4 = "";
	var i = 0;

	do {
		chr1 = input.charCodeAt(i++);
		chr2 = input.charCodeAt(i++);
		chr3 = input.charCodeAt(i++);

		enc1 = chr1 >> 2;
		enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
		enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
		enc4 = chr3 & 63;

		if (isNaN(chr2)) {
			enc3 = enc4 = 64;
		} else if (isNaN(chr3)) {
			enc4 = 64;
		}

		output = output + keyStr.charAt(enc1) + keyStr.charAt(enc2) + keyStr.charAt(enc3) + keyStr.charAt(enc4);
		chr1 = chr2 = chr3 = "";
		enc1 = enc2 = enc3 = enc4 = "";
	} while (i < input.length);

	return output;
}

/*
 * Log to browser console if can found a such.
 */
function logError(err) {
	// The error has a list of modules that failed
	var failedId = err.requireModules && err.requireModules[0];
	// Log it if we can
	if (typeof console != "undefined" && typeof console.log != "undefined") {
		if (failedId) {
			console.log("Cannot load module " + failedId + ". Error: " + err);
		} else {
			console.log("Error: " + err);
		}
	} // otherwise go silently
}

/* RequireJS config */
var require = {
	baseUrl : "/js",

	paths : {
		"jquery" : "http://ajax.googleapis.com/ajax/libs/jquery/1.7/jquery",
		"jquery.validate" : "jquery.validate.min-1.8.1",
		"jquery.string" : "jQueryString-2.0.2-Min",
		"jquery.cookie" : "jquery.cookie",
		"jquery.lightbox" : "/lightbox/jquery.lightbox",
		"thickbox" : "/thickbox/thickbox",
		"marketo.form" : "mktFormSupport",
		"templates" : "../templates"
	},

	shim : {
		'jquery.validate' : [ "jquery" ],
		'jquery.string' : [ "jquery" ],
		'jquery.cookie' : [ "jquery" ],
		'jquery.lightbox' : [ "jquery" ],
		"thickbox" : [ "jquery" ],
		"marketo.form" : {
			deps : [ "jquery" ],
			exports : "Mkto"
		}
	},

	deps : [ "jquery", "jquery.validate", "jquery.string", "jquery.cookie" ],

	callback : function() {
		// Call after all the dependencies are loaded.
		// Customize jQuery
		$.extend({
			getUrlVars : function() {
				var vars = [], hash;
				var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
				for ( var i = 0; i < hashes.length; i++) {
					hash = hashes[i].split('=');
					vars.push(hash[0]);
					vars[hash[0]] = hash[1];
				}
				return vars;
			},
			getUrlVar : function(name) {
				return $.getUrlVars()[name];
			}
		});

		// jQuery.validator
		$.validator.addMethod('regexp', function(value, element, regexp) {
			var re = new RegExp(regexp);
			return this.optional(element) || re.test(value);
		}, "Such name cannot be used.");
	}
};
