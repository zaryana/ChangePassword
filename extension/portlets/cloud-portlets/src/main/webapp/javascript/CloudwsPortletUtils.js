var CloudwsPortletUtils = {};

CloudwsPortletUtils.waitForCondition = function(condition, success, failure, timeout){
    var isTimeout = false;

    var timer = setTimeout(function(){
        isTimeout = true;
    }, timeout);

    var ticker = setInterval(function(){
        eval("waitCond = " + condition);
        if(waitCond) {
            clearInterval(ticker);
            clearTimeout(timer);
            if(success) success();
        } else if(isTimeout) {
            clearInterval(ticker);
            console.log("CloudwsPortletUtils.waitForCondition: timeout after " + timeout + "ms waiting for the condition \"" + condition + "\"");
            if(failure) failure();
        }       
    }, 100);
}

CloudwsPortletUtils.loadIfUndefined = function(objName, scriptSrc, callback, timeout){
    var DEFAULT_TIMEOUT = 5000; //ms    
    timeout = timeout || DEFAULT_TIMEOUT;
    var condition = "typeof " + objName + " != 'undefined'";
    
    var self = this;
    self.waitForCondition(condition, callback,  
        function(){
            console.log("CloudwsPortletUtils.loadIfUndefined: loading " + objName + " from " + scriptSrc + " ...");
            var script = document.createElement("script");
            script.type = "text/javascript";
            script.src = scriptSrc;
            document.getElementsByTagName("head")[0].appendChild(script);    
            self.waitForCondition(condition, callback, undefined, timeout);
        }, timeout);
}
