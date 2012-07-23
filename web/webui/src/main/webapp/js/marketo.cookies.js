//var $jQ = jQuery.noConflict();

$(document).ready(function(){

  //
  //---- CHANGE THESE!!! ----
  //

   // Change this to match domains of referrers you want to ignore.
   // You'll want to ignore referrers from your own domains.
   // Use only the base domain name without subdomains (ex. "company.com")
   // Separate multiple domains with commas (leave the brackets).
 var excludedReferrers = [ "exoplatform.com", "exoplatform.org", "cloud-ide.com", "cloud-workspaces.com" ];

   // Change this to match the base domain of your company's landing pages.
   // Cookies will be created with this domain.
   // Ex. If your landing page domain is "pages.yourcompany.com" then use
   //     "yourcompany.com"
 var cookieDomain = "cloud-workspaces.com";

   // The URL parameter that has your pay-per-click info.
   // Typically "kw" or "keyword" (depends on how you set up your PPC URLs)
 var payPerClickParameter = "keyword";

  //
  //- you probably shouldn't change anything after this -
  //

 var refer = document.referrer;
 var searchString;
 var searchEngine;

     // if there's no referrer, do nothing
 if ( (refer == undefined) || (refer == "") ) {
      ;
  } else {

      // get the domain of the referring website -- http://[[this-thing.com]]/
   var referrerDomain =
        refer.substr(refer.indexOf("\/\/") + 2,
          refer.indexOf("\/",8) - refer.indexOf("\/\/") - 2).toLowerCase();

   var excludedDomainFound = false;
   var i = 0;

     // search the excluded domain list to see if the referrer domain is on it
   while ( (i < excludedReferrers.length) && !excludedDomainFound) {
     var thisExcludedDomain = excludedReferrers[i].toLowerCase();

        // weird semantics here -- indexOf returns "-1" if the search string isnt found.
        // thus excludedDomainFound is true only when indexOf matches an excluded domain (!= -1)
     excludedDomainFound = (referrerDomain.indexOf(thisExcludedDomain) != -1);
      i++;
    }

    // only if the referrer isn't in our excluded domain list...
   if( !excludedDomainFound ) {

      // extract the URL parameters from common search engines
      // To add your own, each engine needs a:
      //  name: how the search engine will appear on your Marketo leads
      //  url: REGEX for matching the engine's referrer.  ex.  /\.google\./i
      //  query: URL parameter that contains the search query - usually "p" or "q"

     var searchEngines = [
       { name: "Yahoo", url: /\.yahoo\.co/i, query: "p" },
       { name: "Google", url: /\.google\./i, query: "q" },
       { name: "Microsoft Live", url: /\.live\.com/i, query: "q" },
       { name: "MSN Search", url: /search\.msn\./i, query: "q" },
       { name: "AOL", url: /\.aol\./i, query: "query" },
       { name: "Bing", url: /\.bing\.com/i, query: "q" },
       { name: "Ask", url: /\.ask\.com/i, query: "q" }
      ];

       // find the referring search engine (if any)
     i = 0;
     while (i < searchEngines.length) {
       if (refer.match(searchEngines[i].url)) {
          searchEngine = searchEngines[i].name;
          searchString = $.getQueryString({ ID: searchEngines[i].query,
            URL: refer, DefaultValue: "" });
         break;
        }
        i++;
      }

       // If no search engine is found, this person probably used a less
       // popular one.  Use the referring doman, then guess the query parameter
     if (i == searchEngines.length) {

         searchEngine = referrerDomain;

        var queries = ["q","p","query"];
        var i = 0;
        while ((i < queries.length) && (searchString == undefined)) {
           searchString = $.getQueryString({ ID: queries[i], URL: refer });
           i++;
         }

         // no search strings found -- use this text instead.
        if (searchString == undefined) {
           searchString = "None";
         }
      }

       // Use the provided URL parameter to get the PPC keyword.
     var payPerClickWord =
        $.getQueryString({ID: payPerClickParameter,
           URL: refer, DefaultValue: "" });

        // Put the info into cookies.  These values will be extracted
        // and put into a Marketo form later. Expires in 2 years.
      $.cookie('mktoPPCKeyword', payPerClickWord,
         {expires: 730, path: '\/', domain: cookieDomain});
      $.cookie('mktoSearchEngine', searchEngine,
         {expires: 730, path: '\/', domain: cookieDomain});
      $.cookie('mktoSearchString', searchString,
         {expires: 730, path: '\/', domain: cookieDomain});
    }
  }
});