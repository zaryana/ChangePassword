function DOMUtil(){	
};

DOMUtil.prototype.findNextElementByTagName = function(element, tagName) {
	var nextElement = element.nextSibling ;
	if(!nextElement) return null;
	var nodeName = nextElement.nodeName.toLowerCase();
	if(nodeName != tagName) return null;
	return nextElement ;
} ;

DOMUtil = new DOMUtil();

function eXoEventGadget(){
};

eXoEventGadget.prototype.getCalendars = function(){
  var url = "/portal/rest/cs/calendar/getcalendars";
  eXoEventGadget.ajaxAsyncGetRequest(url,eXoEventGadget.getData);
  if(typeof(requestInterval) == "undefined") requestInterval = setInterval(eXoEventGadget.getCalendars,100000);
}

eXoEventGadget.prototype.getData = function(data){
  var calendarID = "";
  var len = data.calendars.length - 1;
  for(var i=0; i < len ;i++){
        calendarID += data.calendars[i].id + ",";
  }
  calendarID += data.calendars[len].id;
  var subscribeurl = eXoEventGadget.createRequestUrl(calendarID);
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
		var time = 0;
		if (userTimezoneOffset != null) time = parseInt(item.fromDateTime.time) + parseInt(userTimezoneOffset) + (new Date()).getTimezoneOffset()*60*1000;
		else time = parseInt(item.fromDateTime.time);
		var fullDate = eXoEventGadget.getFullTime(new Date(time));
		var calendar_link = $('#addEvent').attr('href');
                html += '<li class="eventType">» <span><b>' + item.eventType + ':</b></span></li><li class="eventTitle"><a href="' + calendar_link + '" target="_parent">' + item.summary + '</a></li>';
                html += '<li class="eventEmpty"></li><li class="eventTime"><span>on ' + fullDate + '</span></li>';
  	}
  	html += '</ul>';
  	cont.innerHTML = html;
	eXoEventGadget.adjustHeight();
}

eXoEventGadget.prototype.getFullTime = function(dateObj) {
	var monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
        var month = monthNames[dateObj.getMonth()];
        var day = dateObj.getDate();
        var hourNum = dateObj.getHours();
        var hour = (hourNum > 9) ? ("" + hourNum):("0" + hourNum);
        var APM = "";
        if (hour >= 12) {
                hour -= 12;
                APM = "PM";
        } else APM = "AM";
        var minuteNum = dateObj.getMinutes();
        var minute = (minuteNum > 9) ? ("" + minuteNum):("0" + minuteNum);
        var fullDate = month + " " + day + " (" + hour + "h" + minute + " " + APM + ")";
        return fullDate;
}

eXoEventGadget.prototype.createRequestUrl = function(calendarID){
  var limit = "10";
  var subscribeurl = "/rest/calendar/events/personal/" ;
  var today = new Date();
  var aWeekAfter = (new Date()).setDate(today.getDate()+7);
  subscribeurl += calendarID + "/" + today.getTime() + "/" + aWeekAfter + "/" + limit;
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
				eXoEventGadget.notifyEvent();
	  	        }
		}
	}					
}

eXoEventGadget.prototype.showDetailEvent = function(obj){
	var detail = DOMUtil.findNextElementByTagName(obj,"div");
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
        eXoEventGadget.init();
	eXoEventGadget.getCalendars();
	eXoEventGadget.adjustHeight();
}

eXoEventGadget.prototype.init = function() {
  var url = "/rest/calendar/calendar-link";
  $.ajax({
       url: url,
       success: function(data) {
	var a = document.getElementById("addEvent");
	if (data != null) a.href = data.toString() + "calendar";
	eXoEventGadget.adjustHeight();
       },
       dataType: 'text'
  });  
} 
  
eXoEventGadget =  new eXoEventGadget();

gadgets.util.registerOnLoadHandler(eXoEventGadget.onLoadHander);
