function DateTimeFormater(){
};
DateTimeFormater.prototype.masks = {
	"default":      "ddd mmm dd yyyy HH:MM:ss",
	shortDate:      "mm/dd/yyyy",
	mediumDate:     "mmm d, yyyy",
	longDate:       "mmmm d, yyyy",
	fullDate:       "dddd, mmmm d, yyyy",
	shortTime:      "h:MM TT",
	mediumTime:     "h:MM:ss TT",
	longTime:       "h:MM:ss TT Z",
	isoDate:        "yyyy-mm-dd",
	isoTime:        "HH:MM:ss",
	isoDateTime:    "yyyy-mm-dd'T'HH:MM:ss",
	isoUtcDateTime: "UTC:yyyy-mm-dd'T'HH:MM:ss'Z'"
};
DateTimeFormater.prototype.token = /d{1,4}|m{1,4}|yy(?:yy)?|([HhMsTt])\1?|[LloSZ]|"[^"]*"|'[^']*'/g;
DateTimeFormater.prototype.timezone = /\b(?:[PMCEA][SDP]T|(?:Pacific|Mountain|Central|Eastern|Atlantic) (?:Standard|Daylight|Prevailing) Time|(?:GMT|UTC)(?:[-+]\d{4})?)\b/g;
DateTimeFormater.prototype.timezoneClip = /[^-+\dA-Z]/g;
DateTimeFormater.prototype.pad = function(val, len) {
	val = String(val);
	len = len || 2;
	while (val.length < len) val = "0" + val;
	return val;
};

DateTimeFormater.prototype.i18n = {
	dayNames: [
		"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat",
		"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
	],
	monthNames: [
		"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
		"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
	]
};

DateTimeFormater.prototype.format = function (date, mask, utc) {
	var dF = DateTimeFormater;

	// You can't provide utc if you skip other args (use the "UTC:" mask prefix)
	if (arguments.length == 1 && (typeof date == "string" || date instanceof String) && !/\d/.test(date)) {
		mask = date;
		date = undefined;
	}

	// Passing date through Date applies Date.parse, if necessary
	date = date ? new Date(date) : new Date();
	if (isNaN(date)) throw new SyntaxError("invalid date");

	mask = String(dF.masks[mask] || mask || dF.masks["default"]);

	// Allow setting the utc argument via the mask
	if (mask.slice(0, 4) == "UTC:") {
		mask = mask.slice(4);
		utc = true;
	}

	var	_ = utc ? "getUTC" : "get",
		d = date[_ + "Date"](),
		D = date[_ + "Day"](),
		m = date[_ + "Month"](),
		y = date[_ + "FullYear"](),
		H = date[_ + "Hours"](),
		M = date[_ + "Minutes"](),
		s = date[_ + "Seconds"](),
		L = date[_ + "Milliseconds"](),
		o = utc ? 0 : date.getTimezoneOffset(),
		flags = {
			d:    d,
			dd:   dF.pad(d),
			ddd:  dF.i18n.dayNames[D],
			dddd: dF.i18n.dayNames[D + 7],
			m:    m + 1,
			mm:   dF.pad(m + 1),
			mmm:  dF.i18n.monthNames[m],
			mmmm: dF.i18n.monthNames[m + 12],
			yy:   String(y).slice(2),
			yyyy: y,
			h:    H % 12 || 12,
			hh:   dF.pad(H % 12 || 12),
			H:    H,
			HH:   dF.pad(H),
			M:    M,
			MM:   dF.pad(M),
			s:    s,
			ss:   dF.pad(s),
			l:    dF.pad(L, 3),
			L:    dF.pad(L > 99 ? Math.round(L / 10) : L),
			t:    H < 12 ? "a"  : "p",
			tt:   H < 12 ? "am" : "pm",
			T:    H < 12 ? "A"  : "P",
			TT:   H < 12 ? "AM" : "PM",
			Z:    utc ? "UTC" : (String(date).match(dF.timezone) || [""]).pop().replace(dF.timezoneClip, ""),
			o:    (o > 0 ? "-" : "+") + dF.pad(Math.floor(Math.abs(o) / 60) * 100 + Math.abs(o) % 60, 4),
			S:    ["th", "st", "nd", "rd"][d % 10 > 3 ? 0 : (d % 100 - d % 10 != 10) * d % 10]
		};

	return mask.replace(dF.token, function ($0) {
		return $0 in flags ? flags[$0] : $0.slice(1, $0.length - 1);
	});
};

DateTimeFormater = new DateTimeFormater();

function AgendaUtil(){	
};

AgendaUtil.prototype.findNextElementByTagName = function(element, tagName) {
	var nextElement = element.nextSibling ;
	if(!nextElement) return null;
	var nodeName = nextElement.nodeName.toLowerCase();
	if(nodeName != tagName) return null;
	return nextElement ;
} ;

AgendaUtil = new AgendaUtil();

function eXoEventGadget(){
  this.calendarNames = {};
};

eXoEventGadget.prototype.getCalendarID = function(){
  var url = "/rest/calendar/get-all-cals";
  eXoEventGadget.ajaxAsyncGetRequest(url, eXoEventGadget.getAllCalendars);
  if(typeof(requestInterval) == "undefined") requestInterval = setInterval(eXoEventGadget.getCalendarID,100000);
}

eXoEventGadget.prototype.getAllCalendars = function(data){
  var len = data.calendars.length;
  for(var i=0; i < len ;i++){
    var calendarName = data.calendars[i].name;
    if(calendarName.indexOf("default") != -1) calendarName = "Default Calendar";
    if (data.calendars[i].groups != null) {
	var group = data.calendars[i].groups[0];
	var grpName = " (" + group.substring(group.lastIndexOf("/") + 1) + ")";
	calendarName += grpName;
    }
    eXoEventGadget.calendarNames[data.calendars[i].id] = calendarName;
  }
}

eXoEventGadget.prototype.getCalendars = function(){
  var url = "/rest/calendar/get-editable-cals";
  eXoEventGadget.ajaxAsyncGetRequest(url, eXoEventGadget.listCalendar);
  if(typeof(requestInterval) == "undefined") requestInterval = setInterval(eXoEventGadget.getCalendars,100000);
}

eXoEventGadget.prototype.getData = function(){
  var subscribeurl = eXoEventGadget.createRequestUrl();
  eXoEventGadget.ajaxAsyncGetRequest(subscribeurl, eXoEventGadget.render);
}

eXoEventGadget.prototype.render = function(data){
	var userTimezoneOffset = data.userTimezoneOffset;
	data = data.info;
	
	if(!data || data.length == 0){
		//eXoEventGadget.notifyEvent();
		return;
	}
  	var cont = document.getElementById("eventDiv");	
  	var html = '<ul>';
  	for(var i = 0 ; i < data.length; i++){	
    	        var item = data[i];
    	        var calendarId = item.calendarId;
		var fromtime = parseInt(item.fromDateTime.time);
		var totime = parseInt(item.toDateTime.time);
		var dateOfFrom = parseInt(item.fromDateTime.date);
		var fullDate = eXoEventGadget.getFullTime(fromtime, totime, userTimezoneOffset, dateOfFrom);
                html += '<li class="eventType"><span>» <b>' + item.eventType + ':</b></span></li><li class="eventTitle"><a href="' + $("#calendarlink").html() + '" target="_parent">' + item.summary + ' in ' + eXoEventGadget.calendarNames[calendarId] + '</a><br /><span>on ' + fullDate + '</span></li>';
  	}
  	html += '</ul>';
  	$("#eventDiv").html(html);
	eXoEventGadget.adjustHeight();
}

eXoEventGadget.prototype.getFullTime = function(fromtime, totime, userTimezoneOffset, dateOfFrom) {
        var from = 0;

        if (userTimezoneOffset != null) from = fromtime + parseInt(userTimezoneOffset) + (new Date()).getTimezoneOffset()*60*1000;
        else from = fromtime;
        var fromDate = new Date(from);
	var monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
        var month = monthNames[fromDate.getMonth()];
        var day = fromDate.getDate();
        
        var prefs = new gadgets.Prefs();
        var postfix = "";
        if (fromtime + (24 * 3600 * 1000 - 1) == totime) {
                postfix = prefs.getMsg("allday");
                day = dateOfFrom;
        } else {
                var hourNum = fromDate.getHours();
                var hour = (hourNum > 9) ? ("" + hourNum):("0" + hourNum);
                var APM = "";
                if (hour > 12) {
                        hour -= 12;
                        APM = "PM";
                } else APM = "AM";
                var minuteNum = fromDate.getMinutes();
                var minute = (minuteNum > 9) ? ("" + minuteNum):("0" + minuteNum);
                postfix = hour + "h" + minute + " " + APM;
        }
        var fullDate = month + " " + day + " (" + postfix + ")";
        return fullDate;
}

eXoEventGadget.prototype.createRequestUrl = function(){
  var limit = "10";
  var subscribeurl = "/rest/calendar/events/personal/" ;
  var today = (new Date()).getTime();
  var aWeekAfter = today + 7*24*3600*1000;
  subscribeurl += today + "/" + aWeekAfter + "/" + limit;
  return subscribeurl;
}

eXoEventGadget.prototype.ajaxAsyncGetRequest = function(url, callback){
	
  var request =  parent.eXo.core.Browser.createHttpRequest() ;
  request.open('GET', url, true) ;
  request.setRequestHeader("Cache-Control", "max-age=86400") ;
  request.send(null) ;
	request.onreadystatechange = function(){
		if (request.readyState == 4) {
			if (request.status == 200) {
				var data = gadgets.json.parse(request.responseText);
				callback(data);
			}
			//IE treats a 204 success response status as 1223. This is very annoying
			if (request.status == 404  || request.status == 204  || request.status == 1223) {
				return;
	  	        }
		}
	}					
}

eXoEventGadget.prototype.showDetailEvent = function(obj){
	var detail = AgendaUtil.findNextElementByTagName(obj,"div");
	if(!detail) return;
	var condition = this.lastShowItem && (this.lastShowItem != detail) && (this.lastShowItem.style.display == "block"); 
	if(condition) {
	this.lastShowItem.style.display = "none";
	this.lastShowLink.style.background = "url('/exo-gadget-resources/skin/exo-gadget/images/IconLink.gif') no-repeat left 4px";
	}
	if(detail.style.display == "block") {
	        detail.style.display = "none";
	        obj.style.background = "url('/exo-gadget-resources/skin/exo-gadget/images/IconLink.gif') no-repeat left 4px";
	}
	else {
	        detail.style.display = "block";
	        obj.style.background = "url('/exo-gadget-resources/skin/exo-gadget/images/DownIconLink.gif') no-repeat left 4px";
	}
	this.lastShowItem = detail;
	this.lastShowLink = obj;
	eXoEventGadget.adjustHeight();
}

eXoEventGadget.prototype.adjustHeight = function(){
	setTimeout(function(){
	gadgets.window.adjustHeight($("#CalendarGadget").get(0).offsetHeight);		
	},500);
}

eXoEventGadget.prototype.onLoadHander = function(){
        eXoEventGadget.setCalendarLink();
        eXoEventGadget.getCalendarID();
        eXoEventGadget.init();
	eXoEventGadget.getData();
	eXoEventGadget.adjustHeight();
}

eXoEventGadget.prototype.setCalendarLink = function() {
  var url = "/rest/calendar/calendar-link";
  $.ajax({
       url: url,
       success: function(data) {
	if (data != null) $("#calendarlink").html(data.toString() + "calendar");
       },
       dataType: 'text'
  });  
}

eXoEventGadget.prototype.init = function() {
  eXoEventGadget.getCalendars();
  
  document.getElementById("title").value = "";
  document.getElementById("eventType").value = "event";
  document.getElementById("from").value = DateTimeFormater.format((new Date()),"mm/dd/yyyy");
  var to = document.getElementById("to");
  to.value = DateTimeFormater.format((new Date()),"mm/dd/yyyy");
  
  var hours = ["01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"];
  var APM = ["AM", "PM"];
  var prefs = new gadgets.Prefs();
  $("#eventType").html('<option value="Event">' + prefs.getMsg("eventType") + '</option><option value="Task">' + prefs.getMsg("taskType") + '</option>');
  var msg = prefs.getMsg("allday");
  var html = '<option value="AllDay">' + msg + '</option>';
  var html1 = '';
  for (var i = 0; i < 2; i++)
        for (var j = 0; j < 12; j++) {
                html += '<option value="' + hours[j] + APM[i] + '">' + hours[j] +  " " + APM[i] + '</option>';
                html1 += '<option value="' + hours[j] + APM[i] + '">' + hours[j] +  " " + APM[i] + '</option>';
        }
  $("#fromTime").html(html);
  $("#toTime").html(html1);        
  to.disabled = true;
  document.getElementById("toTime").disabled = true;
          
}

eXoEventGadget.prototype.listCalendar = function(data) {
  var personalCal = '', groupCal = '';
  var calendarName = '';
  for(var i=0,len = data.calendars.length; i < len;i++){
    if(data.calendars[i].name.indexOf("default") != -1) calendarName = "Default Calendar";
    else calendarName = data.calendars[i].name;
    if (data.calendars[i].groups != null) {
      var group = data.calendars[i].groups[0];
      var grpName = " (" + group.substring(group.lastIndexOf("/") + 1) + ")";
      groupCal += '<option value="1:' + data.calendars[i].id + '">' + calendarName + grpName + '</option>';
    }
    else personalCal += '<option value="0:' + data.calendars[i].id + '">' + calendarName + '</option>';
  }
  var html = '<optgroup label="Personal Calendars">' + personalCal + '</optgroup><optgroup label="Group Calendars">' + groupCal + '</optgroup>';
  $("#calendar").html(html);
}
  
eXoEventGadget =  new eXoEventGadget();

gadgets.util.registerOnLoadHandler(eXoEventGadget.onLoadHander);
