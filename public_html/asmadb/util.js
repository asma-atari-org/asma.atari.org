"use strict";

function Logger() {
  consoleLogElement = null;
}

Logger.setLogElement = function(elementName) {
  Logger.consoleLogElement = document.getElementById(elementName); 
  Logger.consoleLogElement.style.display = "block";
  Logger.log("Logging started.");
}

Logger.log = function(text){
  console.log(text);
  if (Logger.consoleLogElement != null ) {
   Logger.consoleLogElement.innerHTML = new Date().toISOString()+" "+text+"<br>"+Logger.consoleLogElement.innerHTML;
  }
}

Logger.logError = function(text){
  Logger.log("ERROR: "+text);  
}

function Util() {

}

Util.getAddressHexString = function(address){
 if (address<0) {
  return "-";
 }
 return "$"+address.toString(16);
};

Util.getBytesString = function(bytes){
 return "$"+bytes.toString(16);
};

Util.getDurationString = function(milliseconds){
 if (milliseconds == undefined || milliseconds < 0){
  return "";
 }
 let seconds = Math.floor(milliseconds / 1000);
 let minutes = Math.floor(seconds / 60);
 let hours = Math.floor(minutes / 60);
 seconds = seconds % 60;
 minutes = minutes % 60;

 let durationString = hours+":"+minutes.toLocaleString('en-US', {minimumIntegerDigits: 2, useGrouping:false})+":"+seconds.toLocaleString('en-US', {minimumIntegerDigits: 2, useGrouping:false});
 return durationString;
}

Util.getNormalizedCountry = function(country){
  country = country.replace("(?)", "").trim();
  let parts = country.split("(");
  country = parts[0].trim();
  if (country == ""){
   country = "Unknown";
  }
  return country;
}

Util.getCountryFlagPath = function(country){

 country = Util.getNormalizedCountry(country);
 
 // From https://en.wikipedia.org/wiki/Regional_indicator_symbol
 let countryFlags = {
  "Argentina": "Emojione_1F1E6-1F1F7.svg",
  "Canada": "Emojione_1F1E8-1F1E6.svg",
  "Chile": "Emojione_1F1E8-1F1F1.svg",
  "Czech Republic": "Emojione_1F1E8-1F1FF.svg",
  "Denmark": "Emojione_1F1E9-1F1F0.svg",
  "Finland": "Emojione_1F1EB-1F1EE.svg",
  "France": "Emojione_1F1EB-1F1F7.svg",
  "Germany": "Emojione_1F1E9-1F1EA.svg",
  "Hungary": "Emojione_1F1ED-1F1FA.svg",
  "Mexico": "Emojione_1F1F2-1F1FD.svg",
  "Netherlands": "Emojione_1F1F3-1F1F1.svg",
  "Norway": "Emojione_1F1F3-1F1F4.svg",
  "Peru": "Emojione_1F1F5-1F1EA.svg",
  "Poland": "Emojione_1F1F5-1F1F1.svg",
  "Romania": "Emojione_1F1F7-1F1F4.svg",
  "Slovakia": "Emojione_1F1F8-1F1F0.svg",
  "Sweden": "Emojione_1F1F8-1F1EA.svg",
  "Thailand": "Emojione_1F1F9-1F1ED.svg",
  "Ukraine": "Emojione_1F1FA-1F1E6.svg",
  "Unknown": "Nuvola_unknown_flag.svg",
  "United Kingdom": "Emojione_1F1EC-1F1E7.svg",
  "USA": "Emojione_1F1FA-1F1F8.svg"
 };

 if (country == ""){
  return "";
 }
 let result = countryFlags[country];
 if (result != undefined && result != ""){
  result="images/"+result;
 } else {
  result="";
  Logger.logError("No flag registered for country "+country);
 }
 return result;
}

Util.getCountryFlagHTML = function(country){
 let countryFlagPath = Util.getCountryFlagPath(country);
 if (countryFlagPath != ""){
  return "<img height=\"13px\" src=\""+countryFlagPath+"\"> <span style=\"vertical-align:top;\">"+UI.encodeHTML(country)+"</span>";
 } else {
  return UI.encodeHTML(country);
 }
}
