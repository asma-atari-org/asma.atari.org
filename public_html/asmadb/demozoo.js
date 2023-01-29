"use strict";

function Demozoo(productions) {
 this.productions = productions;
 this.productionsByFilePathAndSongIndexMap = new Map();
};

Demozoo.prototype.initProductions = function(){
 this.productionsByFilePathAndSongIndexMap.clear();
 let result="";
 let linkCount = 0;
 let missingLinkCount = 0;
 for (let production of this.productions){
  if (production.filePath == null){
   missingLinkCount++;
   result+=(this.getMusicHTML(production)+" has no ASMA link<br>\n");
  } else {
   let key = this.getFilePathAndSongIndexKey(production.filePath, production.songIndex);
   this.productionsByFilePathAndSongIndexMap.set(key, production);
   linkCount++;
  }
 }
 result = linkCount+" links found, "+missingLinkCount+" links missing<br>\n"+result;
 return result;
};


Demozoo.prototype.getProductionsByFilePathAndSongIndexMap = function() {
 return this.productionsByFilePathAndSongIndexMap;
};

Demozoo.prototype.getFilePathAndSongIndexKey = function(filePath, songIndex){
 let key = filePath;
 if (songIndex >= 0) {
     key +="?songIndex="+songIndex;
 }
 return key;
};


Demozoo.prototype.getProductionByFilePathAndSongIndex = function(filePath, songIndex){
 let production = this.productionsByFilePathAndSongIndexMap.get(this.getFilePathAndSongIndexKey(filePath, songIndex));
 if (production == undefined){
  production = this.productionsByFilePathAndSongIndexMap.get(this.getFilePathAndSongIndexKey(filePath, -1)); 	
 }
 return production;
};

Demozoo.prototype.getMusicHTMLForFilePath = function(filePath, songIndex){
 let production = this.getProductionByFilePathAndSongIndex(filePath,songIndex);
 if (production != undefined){
    return "<a href=\"https://demozoo.org/music/"+production.id+"\" target=\"blank\">"+production.id+"</a>";
 }
 return "None";
};

Demozoo.prototype.getMusicHTML = function(production){
 return "<a href=\"https://demozoo.org/music/"+production.id+"\" target=\"blank\">"+production.title+"</a>";
};


Demozoo.prototype.checkDemozoo = function(){
 this.productions= new Array();
 this.fetchPage("https://demozoo.org/api/v1/productions/?supertype=music&platform=54&platform=16", 0);
};

Demozoo.prototype.fetchPage = function(url, productionsIndex){
 Logger.log("Fetching page "+url);
 fetch(url)
  .then((response) => response.json())
  .then((data) => this.fetchProductions(data, 0, productionsIndex));
};

Demozoo.prototype.fetchProductions = function(data, resultIndex, productionsIndex){
 if (resultIndex < data.results.length){
   this.fetchProduction(data, resultIndex, productionsIndex);
 } else {
  if (data.next != undefined) {
   this.fetchPage(data.next, productionsIndex);
  } else {
   Logger.log("demozooProductions = ");
   Logger.log(this.productions);
  }
 }
};

Demozoo.prototype.fetchNextProduction = function(productionsData, resultIndex, productionsIndex){
  this.fetchProductions(productionsData, resultIndex+1, productionsIndex+1, productionsIndex+1);
}

Demozoo.prototype.fetchProduction = function(productionsData, resultIndex, productionsIndex){
 let production = productionsData.results.at(resultIndex);
 let url = production.url;

 Logger.log("Fetching production "+(productionsIndex+1)+" of "+productionsData.count+" from "+url);
 fetch(url)
  .then((response) => response.json())
  .then((data) => this.addProduction(productionsData, resultIndex, data, productionsIndex))
  .then(() =>  this.fetchNextProduction(productionsData, resultIndex, productionsIndex))
  .catch((error) => { console.error("Fetch Error:", error);
  });
}


Demozoo.prototype.addProduction = function(productionsData, resultIndex, data, productionsIndex){

 let authorIDs =[];
 for (let author_nick of data.author_nicks){
  authorIDs[authorIDs.length] = author_nick.releaser.id;
 }

 const prefix = "http://asma.atari.org/asma/";
 let filePath = null;
 for (let downloadLink of data.download_links){
   if (downloadLink.link_class == "BaseUrl" && downloadLink.url.startsWith(prefix)){
     filePath = downloadLink.url.substring(prefix.length);
     // TODO Strip ?songIndex? query parts
     break;
   }
 }
 let production = { id: data.id, title: data.title, authorIDs: authorIDs, filePath: filePath };
 this.productions[productionsIndex] = production;
};

