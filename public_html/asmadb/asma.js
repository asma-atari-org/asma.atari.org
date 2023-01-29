"use strict";

const Hardware = { ATARI2600: "ATARI2600", ATARI800: "ATARI800"};

const FileType = { CM3 : ".cm3", CMC : ".cmc", CMR : ".cmr", CMS : ".cms", DLT : ".dlt", DMC : ".dmc", FC : ".fc",
                   MPD : ".mpd", MPT : ".mpt", RMT : ".rmt", SAP : ".sap", TM2 : ".tm2", TM8 : ".tm8", TMC : ".tmc",
                   TTT : ".ttt",
                   XEX : ".xex"
                 };

const SongState = { NONE: "NONE", PLAYING: "PLAYING", STOPPED: "STOPPED" };
const SongNumber = { FIRST: 0, NONE: -1, RANDOM: -2 };

const DEFAULT_WINDOW_TITLE = "Atari SAP Music Archive";

function ASMA() {

}

ASMA.prototype.onWindowPopState = function(state){
 Logger.log("Execute onWindowPopState: "+JSON.stringify(window.location)); // TODO
 this.initFromWindowLocation();
};

ASMA.prototype.initSearchField = function(inputID, defaultValue){
 // Get the input element
 let inputElement = UI.getElementById(inputID);

 let index = this.searchFields.length;
 this.searchFields[index] = { "defaultValue": defaultValue, "element": inputElement,  "fieldName": inputID };

 // Execute a function when the user presses a key on the keyboard
 inputElement.addEventListener("keypress", function(event) {
  // If the user presses the "Enter" key on the keyboard
  if (event.key === "Enter") {
    // Cancel the default action, if needed
    event.preventDefault();
    // Trigger the button element with a click
    UI.getElementById("searchButton").click();
  }
 });
};

ASMA.prototype.initSearchParameter = function(params, searchField){
 let searchValue = params.get(searchField.fieldName);
 if (searchValue != null && searchValue != "") {
   searchField.element.value = searchValue;
   return true;
 } else {
  searchField.element.value = searchField.defaultValue;
 }
 return false;
};

ASMA.prototype.initFromWindowLocation = function(){

 let url = new URL(window.location);
 let hash = url.hash;
 let params = new URLSearchParams(url.search.slice(1)); // Skip "?"

 let startHash = false;
 let startSearch = false;

 if (hash.startsWith('#/')){
   startHash = true;
 }

 for (let searchField of this.searchFields) {
  startSearch |= this.initSearchParameter(params, searchField);
 }

 let debug = params.get("debug");
 if (debug != undefined){
   Logger.setLogElement("consoleLog");
 }

 let detailsMode = (window.innerWidth >= 1024)
 this.setDetailsMode(detailsMode);

 if (startHash) {
  let foundFileInfo = null;
  let filePath = hash.slice(2); // Skip "#/"
  Logger.log("Start song with path \""+filePath+"\"");
  for (let fileInfo of this.fileInfos){
   if (fileInfo.getFilePath() == filePath){
    foundFileInfo = fileInfo;
    break;
   }
  }

  if (foundFileInfo == null) {
    window.alert("There is no matching song in the database for the path \""+filePath+"\".");
  } else {
   if (this.currentFileInfo == null || this.currentFileInfo.getFilePath() != foundFileInfo.getFilePath()) {
    this.stopCurrentSong(); 
    this.playASMA(foundFileInfo.getIndex(), foundFileInfo.getDefaultSong(), false);
    return;
   }
  }
  
 }

 if (startSearch) {
  UI.getElementById("searchButton").click();
 }
};


ASMA.prototype.initInternal = function(){

 this.demozoo = new Demozoo(asma.demozoo.productions);
 this.demozooCheckResult = "";
 this.demozooCheckResult = this.demozoo.initProductions();

 FileInfoList.init(asma.fileInfos);

 this.fileInfos = asma.fileInfos; // "asma" or array of local file

 this.composerList = new ComposerList(asma.composerInfos);
 this.composerList.initFileInfos(this.fileInfos, this.demozoo.getProductionsByFilePathAndSongIndexMap());

 this.detailsMode = false;
 this.shuffleMode = false;

 // Player
 this.currentFileIndex = -1;
 this.currentFileInfo = null;
 this.currentSongState = SongState.NONE;

 // Editing
 this.editSongDialog = null;

 // Search
 this.searchFields = [];
 this.lastSearchParameters = new SearchParameters();

 this.currentSelectedIndex = undefined;

 let index = 0;
 for(let fileInfo of this.fileInfos){
   fileInfo.index = index++;
   if (fileInfo.hardware == undefined){
    fileInfo.hardware = Hardware.ATARI800;
   }
   if (fileInfo.comment == undefined){
    fileInfo.comment = "";
   }
   switch (fileInfo.originalModuleExt){
  case 'ttt':
   fileInfo.originalModuleExtDescription = "TIATracker";
   break;
  default:
   fileInfo.originalModuleExtDescription = ASAPInfo.getExtDescription(fileInfo.originalModuleExt);
   break;
   }
   if (fileInfo.channels == undefined){
     fileInfo.channels = 1;
   }
   if (fileInfo.songs == undefined){
     fileInfo.songs = 1;
   }
   if (fileInfo.defaultSong == undefined){
     fileInfo.defaultSong = 0;
   }
   fileInfo.url = "../asma/"+fileInfo.getFilePath();
 }

 // Setup main window
 window.setInterval(this.refreshCurrentASAPInfo, 500, this);
 window.addEventListener('popstate', (event) => { asmaInstance.onWindowPopState(event.state)});

 // Get controls
 this.displayAuthorDialog = UI.getElementById("displayAuthorDialog");
 this.editSongDialog = UI.getElementById("editSongDialog");
 this.aboutDialog = UI.getElementById("aboutDialog");

 this.initSearchField("searchKeyword", "");
 this.initSearchField("searchFilePath", "");
 this.initSearchField("searchTitle", "");
 this.initSearchField("searchAuthor", "");
 this.initSearchField("searchDate", "");
 this.initSearchField("searchHardware", "1");
 this.initSearchField("searchChannels", "1");
 this.initSearchField("searchFormat", "");

 this.initFromWindowLocation();
}

ASMA.prototype.init = function(){
 try {
  this.initInternal();
 } catch (ex){
   window.alert(ex);
   throw ex
 }
}

ASMA.prototype.setDetailsMode = function(detailsMode){
 this.detailsMode = detailsMode;
 let detailsModeButton = UI.getElementById("detailsModeButton");

 if (this.detailsMode == true){
  detailsModeButton.innerHTML = "Hide Details";
 } else {
  detailsModeButton.innerHTML = "Show Details";
 }

 this.setSearchParameters(this.getSearchParameters());

 // Set visbility for all search fields
 UI.setVisible("hardwareLabel", this.detailsMode);
 UI.setVisible("hardware", this.detailsMode);
 UI.setVisible("originalModuleFormatLabel", this.detailsMode);
 UI.setVisible("originalModuleFormat", this.detailsMode);
 UI.setVisible("replayFrequencyLabel", this.detailsMode);
 UI.setVisible("replayFrequency", this.detailsMode);
 UI.setVisible("addressesLabel", this.detailsMode);
 UI.setVisible("addresses", this.detailsMode);
 UI.setVisible("filePathLabel", this.detailsMode);
 UI.setVisible("filePath", this.detailsMode);
 UI.setVisible("fileSizeLabel", this.detailsMode);
 UI.setVisible("fileSize", this.detailsMode);
 UI.setVisible("demozooIDLabel", this.detailsMode);
 UI.setVisible("demozooID", this.detailsMode);
 UI.setVisible("saveExtensionsLabel", this.detailsMode);
 UI.setVisible("saveExtensions", this.detailsMode);
}


ASMA.prototype.toggleDetailsMode = function(){
 this.setDetailsMode(!this.detailsMode);
 this.clearSearchResult();
}


ASMA.prototype.clearCurrentFile = function(){
 this.setCurrentFileIndexAndSong(-1,-1);
}

ASMA.prototype.clearSearchFields = function(){
 for (let searchField of this.searchFields) {
  searchField.element.value = searchField.defaultValue;
 }

}

ASMA.prototype.clearSearchResult = function(){
 let searchResult = UI.getElementById("searchResult");
 searchResult.innerHTML = "";
 this.currentSelectedIndex = undefined;
}

function SearchParameters() {
 this.searchKeyword ="";
 this.searchFilePath ="";
 this.searchTitle ="";
 this.searchAuthor = "";
 this.searchDate = "";
 this.searchHardware = "1";
 this.searchChannels = "1";
 this.searchFormat = "";
}

ASMA.prototype.getSearchParameters = function(){
  return {
   searchKeyword: UI.getElementById("searchKeyword").value,
   searchFilePath: UI.getElementById("searchFilePath").value,
   searchTitle: UI.getElementById("searchTitle").value,
   searchAuthor: UI.getElementById("searchAuthor").value,
   searchDate: UI.getElementById("searchDate").value,
   searchHardware: UI.getElementById("searchHardware").value,
   searchChannels: UI.getElementById("searchChannels").value,
   searchFormat: UI.getElementById("searchFormat").value
  }
};

ASMA.prototype.setSearchField = function(id, searchParameters){
 let label = document.getElementById(id+"Label");
 UI.setVisible("searchAuthorLabel", this.detailsMode);
 let field = UI.getElementById(id);
 field.placeholder = (this.detailsMode?"":label.innerHTML);
 UI.setVisible("searchAuthor", this.detailsMode || searchParameters[id] != "");
}

ASMA.prototype.setSearchParameters = function(searchParameters){

 UI.setVisible("searchKeywordLabel", this.detailsMode);
 let searchKeyword = UI.getElementById("searchKeyword");
 searchKeyword.placeholder = (this.detailsMode?"":"Keyword");
 UI.setVisible("searchButtonLabel", this.detailsMode);

 UI.setVisible("searchFilePathLabel", false);
 UI.setVisible("searchFilePath", false);

 UI.setVisible("searchTitleLabel", this.detailsMode);
 UI.setVisible("searchTitle", this.detailsMode);
 this.setSearchField("searchAuthor", searchParameters);

 UI.setVisible("searchDateLabel", this.detailsMode);
 UI.setVisible("searchDate", this.detailsMode);

 UI.setVisible("searchHardwareLabel", this.detailsMode);
 UI.setVisible("searchHardware", this.detailsMode);
 UI.setVisible("searchChannelsLabel", this.detailsMode);
 UI.setVisible("searchChannels", this.detailsMode);
 UI.setVisible("searchFormatLabel", this.detailsMode);
 UI.setVisible("searchFormat", this.detailsMode);

 UI.getElementById("searchKeyword").value = searchParameters.searchKeyword;
 UI.getElementById("searchFilePath").value = searchParameters.searchFilePath;
 UI.getElementById("searchTitle").value = searchParameters.searchTitle;
 UI.getElementById("searchAuthor").value = searchParameters.searchAuthor;
 UI.getElementById("searchDate").value = searchParameters.searchDate;
 UI.getElementById("searchHardware").value = searchParameters.searchHardware;
 UI.getElementById("searchChannels").value = searchParameters.searchChannels;
 UI.getElementById("searchFormat").value = searchParameters.searchFormat;
};

ASMA.prototype.search = function(){
 this.searchUsingParameters(this.getSearchParameters(), true);
}


ASMA.prototype.searchUsingParameters = function(searchParameters, recordInHistory){

 let wasPlaying = (asap.context != undefined && asap.context.state == "running");
 if (wasPlaying){
  asap.togglePause();
 }

 this.setSearchParameters(searchParameters);

 let searchKeyword = searchParameters.searchKeyword.toLowerCase();
 let searchFilePath = searchParameters.searchFilePath.toLowerCase();
 let searchTitle = searchParameters.searchTitle.toLowerCase();
 let searchAuthor = searchParameters.searchAuthor.toLowerCase();
 let searchDate = searchParameters.searchDate;
 let searchHardware = searchParameters.searchHardware;
 let searchChannels = searchParameters.searchChannels;
 let searchFormat = searchParameters.searchFormat.toLowerCase();

 let searchResult = UI.getElementById("searchResult");
 let index=-1;
 let foundIndex=0;
 let foundFileInfos=[];
 let fileInfo=null;
 for (fileInfo of this.fileInfos){
   index+=1;
   fileInfo.index = index;
   if (searchKeyword != ""){
    if ((!fileInfo.getFilePath().toLowerCase().includes(searchKeyword)) &&
        (!fileInfo.title.toLowerCase().includes(searchKeyword)) &&
        (!fileInfo.author.toLowerCase().includes(searchKeyword)) &&
        (!fileInfo.date.toLowerCase().includes(searchKeyword))) {
       continue;
    }
   };

   if (searchTitle != ""){
    if (!fileInfo.title.toLowerCase().includes(searchTitle)) {
       continue;
    }
   };

   if (searchFilePath != ""){
    if (!fileInfo.getFilePath().toLowerCase().includes(searchFilePath)) {
       continue;
    }
   };

   if (searchAuthor != ""){
    if (!fileInfo.author.toLowerCase().includes(searchAuthor)) {
       continue;
    }
   };

   if (searchDate != ""){
    if (!fileInfo.date.toLowerCase().includes(searchDate)) {
       continue;
    }
   };

   switch(searchHardware){
       case "1":
         break;
       case "2":
        if (fileInfo.hardware != Hardware.ATARI2600) continue;
         break;
       case "3":
         if (fileInfo.hardware != Hardware.ATARI800) continue;
         break;
   };
   switch(searchChannels){
       case "1":
         break;
       case "2":
        if (fileInfo.channels!="1") continue;
         break;
       case "3":
         if (fileInfo.channels!="2") continue;
         break;
   };

   if (searchFormat != "") {
    if ((!fileInfo.originalModuleExtDescription.toLowerCase().includes(searchFormat)) &&
        (!fileInfo.originalModuleExt.toLowerCase().includes(searchFormat))) {
       continue;
    }
   }
   foundFileInfos[foundIndex++]=fileInfo;
  };

 let resultHTML;
 resultHTML="<p>"+foundIndex+" of "+this.fileInfos.length+" matching files. Click to play.";
 //if (this.detailsMode){
 //  resultHTML+= " If something recent is missing, try clearing the browser cache (CTRL-F5).";
 //}
 resultHTML+="</p>";
 resultHTML+='<table id="searchResultTable" class="search_result_table sortable">';
 resultHTML+="<tr>"
 resultHTML+='<th id="searchResultTableTitleHeader" class="search_result_header sorttable_alpha">Title</th><th class="search_result_header sorttable_alpha">Author</th>';
 if (this.detailsMode){
  resultHTML+='<th class="search_result_header sorttable_alpha">Date</th>';
 }
 if (this.detailsMode){
  resultHTML+='<th class="search_result_header sorttable_alpha">Channels</th><th class="search_result_header">Format</th><th class="search_result_header sorttable_alpha">Songs / Default</th>';
 }
 resultHTML+="</tr>"

 for(fileInfo of foundFileInfos){
  let dateParts = fileInfo.date.split("/").reverse();
  let dateSortKey = dateParts.join("/");
  resultHTML+="<tr id=\"searchResultIndex"+fileInfo.getIndex()+"\" onclick=\"asmaInstance.playOrStopAtIndex("+fileInfo.getIndex()+","+fileInfo.defaultSong+")\">";
  resultHTML+="<td class=\"search_result_cell\" title=\""+fileInfo.getFilePath()+"\" sorttable_customkey=\""+fileInfo.title+"\">"+UI.encodeHTML(fileInfo.title)+"</td>";
  resultHTML+="<td class=\"search_result_cell\">"+UI.encodeHTML(fileInfo.author)+"</td>";
  if (this.detailsMode || searchDate != ""){
   resultHTML+="<td class=\"search_result_cell\" style=\"text-align:right\"sorttable_customkey=\""+dateSortKey+"\">"+UI.encodeHTML(fileInfo.date)+"</td>";
  }
  if (this.detailsMode || searchChannels != "1"){
   resultHTML+="<td class=\"search_result_cell\">"+fileInfo.getChannelsText()+"</td>";
  }
  if (this.detailsMode || searchFormat != ""){
   resultHTML+="<td class=\"search_result_cell\">"+fileInfo.getOriginalModuleFormatText()+"</td>";
  }
  if (this.detailsMode){
   resultHTML+="<td class=\"search_result_cell\">"+fileInfo.getSongsText()+"</td>";
  }
  resultHTML+="</tr>";
 }

 resultHTML+="</table>";
 searchResult.innerHTML=resultHTML;
 let newTableObject = UI.getElementById("searchResultTable");
 sorttable.makeSortable(newTableObject);
 UI.getElementById("searchResultTableTitleHeader").click();

 // Record in history if requested and different from previous value
 // TODO Record search in URL?
 this.lastSearchParameters = searchParameters;

 if (wasPlaying){
  asap.togglePause();
 }

 if (foundIndex == 1){
  this.playASMA(fileInfo.getIndex(),-1, true); // Chrome will not allow audio without user gesture
 }
};


ASMA.prototype.downloadPlayList = function(){

//   header('Content-Type: text/plain; name="playlist.m3u"');

// List file paths
//   header('Pragma: public');
//   header('Cache-Control: no-store, no-cache, must-revalidate'); // HTTP/1.1
//   header('Cache-Control: pre-check=0, post-check=0, max-age=0'); // HTTP/1.1
//   header('Content-Transfer-Encoding: none');
//   header('Content-Type: application/force-download; name="playlist.m3u"'); // This should work for IE & Opera
//  header('Content-Disposition: attachment; filename="playlist.m3u"');
//   header("Content-length: $fsize");
};


ASMA.prototype.setRowClassList = function(index, classList){
 let row = document.getElementById("searchResultIndex"+index);
 if (row != undefined){
  for(let i=0; i<row.cells.length; i++){
   row.cells[i].classList=classList;
  }
 }
}

ASMA.prototype.clearSelectedIndexRow = function(){
 this.setSelectedIndexRow(-1);
}

ASMA.prototype.setSelectedIndexRow = function(index){
 if (this.currentSelectedIndex == index){
  return;
 }
 if (this.currentSelectedIndex != undefined) {
   this.setRowClassList(this.currentSelectedIndex, "");
 }
 this.setRowClassList(index, "search_result_selected_row");
 this.currentSelectedIndex = index;
};

ASMA.prototype.playOrStopAtIndex = function(index,song){
 if (this.currentSongState == SongState.PLAYING && index == this.currentFileIndex && song == this.currentSong) {
  this.stopCurrentSong();
  this.currentSong = null;
 } else {
  this.playASMA(index,song, true);
 }
};

ASMA.prototype.getPlaySongButtonId = function(song){
 return "playSong"+song+"Button";
}

ASMA.prototype.setCurrentFileInfo = function(fileInfo){
 this.currentFileInfo = fileInfo;

 let replayFrequency = UI.getElementById("replayFrequency");
 let addresses = UI.getElementById("addresses");
 let title ="None";
 let author = "None";
 let comment = "";
 let hardware = "";
 let date = "";
 if (this.currentFileInfo == null){
  this.currentSongState = SongState.NONE;
  UI.getElementById("channels").innerHTML = "";
  UI.getElementById("originalModuleFormat").innerHTML = "";
  UI.getElementById("songList").innerHTML = "";
  replayFrequency.innerhtml = "";
  addresses.innerhtml = "";
  UI.getElementById("filePath").innerHTML = "";
  UI.getElementById("fileSize").innerHTML = "";
  UI.getElementById("demozooID").innerHTML = "";
  UI.getElementById("saveExtensions").innerHTML = "";

  UI.getElementById("editButton").disabled = "true";
  UI.getElementById("playButton").disabled = "true";
  UI.getElementById("pauseButton").disabled= "true";
  UI.getElementById("stopButton").disabled = "true";
 } else {

  comment = fileInfo.comment;
  switch(fileInfo.hardware){
   case Hardware.ATARI2600:
    hardware ="Atari 2600";
    break;
   case Hardware.ATARI800:
    hardware ="Atari 8-bit";
    break;
   default:
    throw "Unknown hardware "+fileInfo.hardware;
  }

  let asapInfo = this.currentFileInfo.asapInfo;
  let canPlay = (asapInfo!=null);

  if (asapInfo == null){
   title = fileInfo.title;
   author = fileInfo.author;
   date = fileInfo.date;
   UI.getElementById("channels").innerHTML = this.currentFileInfo.getChannelsText();
   UI.getElementById("originalModuleFormat").innerHTML = this.currentFileInfo.getOriginalModuleFormatText();
   UI.getElementById("songList").innerHTML = "";
   replayFrequency.innerhtml = "";
   addresses.innerhtml = "";
  } else {
   // There is ASAPInfo
   title = asapInfo.getTitle() == ""?fileInfo.title:asapInfo.getTitle();
   author = asapInfo.getAuthor() == ""?fileInfo.author:asapInfo.getAuthor();
   date = asapInfo.getDate() == ""?fileInfo.date:asapInfo.getDate();
   UI.getElementById("channels").innerHTML = this.currentFileInfo.getChannelsText();
   UI.getElementById("originalModuleFormat").innerHTML = this.currentFileInfo.getOriginalModuleFormatText();
   let songListHTML="";
   for (let song = 0; song < this.currentFileInfo.songs; song++){
    let id = this.getPlaySongButtonId(song);
    let songText = (song+1).toString();
    if (this.currentFileInfo.songs > 1 && song == this.currentFileInfo.defaultSong){
     songText += " (Default)";
    }
    songListHTML+= '<input id="'+id+'" type="button" value="'+songText+'" onclick="asmaInstance.playASMA('+this.currentFileIndex+','+song+',true)">';
   }

   UI.getElementById("songList").innerHTML = songListHTML;

   let replayFrequencyString = ( asapInfo.getPlayerRateHz()+" Hz / "+asapInfo.getPlayerRateScanlines()+" scanlines ("+(asapInfo.ntsc?"NTSC":"PAL")+")");
   let addressesString = Util.getAddressHexString(asapInfo.getInitAddress())+" / "+Util.getAddressHexString(asapInfo.getPlayerAddress())+" / "+Util.getAddressHexString(asapInfo.getMusicAddress());
   replayFrequency.innerHTML = replayFrequencyString;
   addresses.innerHTML = ( addressesString );
  }

  let authorsHTML="";
  let composerProxies = fileInfo.getComposerProxies();
  let composerCount = composerProxies.length;
  if (composerCount == 0){
    authorsHTML = UI.encodeHTML(author);
  } else {
   for( let i=0; i<composerCount; i++) {
    let composerProxy = composerProxies.at(i);
    let fullNameUnicode = composerProxy.getFullNameUnicode();

    let author = fullNameUnicode;
    if (composerProxy.getHandles() != ""){
     author += " ("+composerProxy.getHandles()+")";
    }
    let authorHTML = UI.encodeHTML(author);
    authorHTML = "<a href=\"javascript:asmaInstance.displayAuthorDetails("+i+")\">"+authorHTML+"</a>";

    authorsHTML += authorHTML;
    if (i<composerCount-1){
     authorsHTML += " &amp; ";
    }
   }
  }

  UI.getElementById("title").innerHTML = UI.encodeHTML(title);
  UI.getElementById("author").innerHTML = authorsHTML;
  UI.getElementById("date").innerHTML = UI.encodeHTML(date);

  UI.getElementById("hardware").innerHTML = UI.encodeHTML(hardware);
  UI.getElementById("details").innerHTML = UI.encodeHTML(comment);

  UI.getElementById("filePath").innerHTML = fileInfo.getFilePath();
  UI.getElementById("fileSize").innerHTML = fileInfo.getFileSizeText();
  UI.getElementById("demozooID").innerHTML = this.demozoo.getMusicHTMLForFilePath(fileInfo.getFilePath(), this.currentSong);

  let saveExtensionsString = "";
  for(let i = 0; i<fileInfo.saveExtensions.length; i++){
   if (i > 0) { saveExtensionsString += " "; }
   saveExtensionsString += "<span class=\"link\" onclick=\"javascript:asmaInstance.exportFileContentWithExtensionIndex("+i+")\">"+fileInfo.saveExtensions[i].toUpperCase()+'</span>';
  }
  UI.getElementById("saveExtensions").innerHTML = saveExtensionsString;
  // Enable/disable buttons.
  UI.getElementById("editButton").disabled = null;
  UI.getElementById("playButton").disabled = (canPlay ?null:"true");
  UI.getElementById("pauseButton").disabled = (canPlay && this.currentSongState == SongState.PLAYING?null:"true");
  UI.getElementById("stopButton").disabled = (canPlay && this.currentSongState == SongState.PLAYING?null:"true");
 }
};

// Special value of song: =>0 song song number, SongNumber.NONE = don't play,SongNumber.RANDOM = play random
ASMA.prototype.setCurrentFileIndexAndSong = function(index,song){
 if (index == undefined){
  throw("Parameter index is undefined.");
 }
 if (song == undefined){
  throw("Parameter song is undefined.");
 }
 this.currentFileIndex=index;
 this.currentSong=song;

 if (index != SongNumber.NONE){
  let fileInfo = this.fileInfos[index];
  this.currentFileInfo = fileInfo;
  let url = new URL(window.location);
  url.search = "";
  url.hash = "#/" + this.currentFileInfo.getFilePath();
  window.location = url;
  this.loadFileContent(fileInfo, this.onFileContentLoadSuccess, this.onFileContentLoadFailure, song);
 }
 else {
  // document.location.assign("/#/");
  this.setCurrentFileInfo(null);
 }
}

ASMA.prototype.playASMA = function(index,song,recordInHistory){
 this.setCurrentFileIndexAndSong(index,song); // Will call playSong() if song >=0
 this.setSelectedIndexRow(index);
};

ASMA.prototype.playFile = function(file){
 Logger.log("Playing local file \""+file+"\"");
 UI.getElementById("filePath").innerHTML= file.name;

 asap.playFile(file);
};



ASMA.prototype.seekPlayingTime = function(){
 if (this.currentSongState != SongState.PLAYING){
  return;
 }
 let playingTimeSlider = UI.getElementById("playingTimeSlider");
 let position = playingTimeSlider.value;
 asap.asap.seek(position);
}
// asma is passed as parameter, because "this" is the window in case of an interval event handler
ASMA.prototype.refreshCurrentASAPInfo = function(asma){
 asma.onRefreshCurrentASAPInfo();
}

ASMA.prototype.onRefreshCurrentASAPInfo = function(){

 let playingTime = UI.getElementById("playingTime");
 let playingTimeSlider = UI.getElementById("playingTimeSlider");
 let sliderVisible = true;
 let sliderMin = 0;
 let sliderMax = 0;
 let sliderValue = 0;

 let windowTitleString = DEFAULT_WINDOW_TITLE;

 let songTitleString = "";
 if (this.currentFileInfo != null){
   songTitleString = this.currentFileInfo.title+" by "+this.currentFileInfo.author;
 }

 let durationString = null;
 switch(this.currentSongState){
  case SongState.NONE:
   durationString = "";
   sliderVisible = false;
   break;

  case SongState.PLAYING:
   windowTitleString = songTitleString;
   for(let i=0; i<this.currentFileInfo.songs; i++){
    let id = this.getPlaySongButtonId(i);
    let playSongButton = UI.getElementById(id);
    let classList=(i == this.currentSong?'player_song_button_active':'');
    playSongButton.classList = classList;
   }
   const asapInfo = this.currentFileInfo.asapInfo;

   if (asapInfo != undefined){
    const duration = asapInfo.getDuration(this.currentSong);
    const loop = asapInfo.getLoop(this.currentSong);
    let position = asap.asap.getPosition();
    if (duration > 0) {
     // If the defined duration [milli seconds] is reached, play new random song?
     if (this.isShuffleMode() && position > duration){
      this.playRandomSong();
      return;
     }
     position %= duration;
    } else {
     // If the maximum duration [milli seconds] is reached, play new random song?
      if (this.isShuffleMode() && position > 2*60*1000){
      this.playRandomSong();
      return;
     }
    }
    durationString = Util.getDurationString(position);
    if (asap.context.state == "suspended"){
     windowTitleString += " (Paused)";
     durationString += " (Paused)";
    }
    if (duration > 0) {
      durationString += " / " + Util.getDurationString(duration);
      sliderMin =0;
      sliderMax = duration;
      sliderValue = position;
    }
    if (loop){
     durationString += " (Loop)";
    }

   }
   break;

  case SongState.STOPPED:
   windowTitleString = songTitleString+ "(Stopped)";
   durationString ="Stopped";
   break;
  default:
   throw "Invalid song state "+this.currentSongState;
 } // case

 window.document.title = windowTitleString;
 playingTime.innerHTML = durationString;
 playingTimeSlider.style.display = (sliderVisible?"":"none")
 playingTimeSlider.min = sliderMin;
 playingTimeSlider.max = sliderMax;
 playingTimeSlider.value = sliderValue;
};

/*
 * Shuffel Mode Routines
 */

ASMA.prototype.isShuffleMode = function(){
 return this.shuffleMode;
};

ASMA.prototype.setShuffleMode = function(shuffleMode){
  this.shuffleMode = shuffleMode;
  let shuffleModeButton = UI.getElementById("shuffleModeButton");
  shuffleModeButton.style.fontWeight  = shuffleMode?"bold":"normal";
  shuffleModeButton.style.backgroundColor  = shuffleMode?"lightBlue":"";

};

ASMA.prototype.toggleShuffleMode = function(){
  this.setShuffleMode(true);
  this.playRandomSong();
};

ASMA.prototype.playRandomSong = function(){
 let index = Math.floor(Math.random() * this.fileInfos.length);
 this.stopCurrentSong();
 this.playASMA(index,SongNumber.RANDOM);
};

ASMA.prototype.playCurrentSong = function(){
 Logger.log("Playing current song.");
 let song = this.currentSong;
 if (song < 0){
   song = this.currentFileInfo.asapInfo.getDefaultSong();
 }
 this.currentModuleInfo = this.playASMA(this.currentFileIndex, song, true);
};

ASMA.prototype.togglePauseCurrentSong = function(){
 Logger.log("Toggle pause current song.");
 asap.togglePause();
};

ASMA.prototype.setCurrentSongState = function(songState){
 this.currentSongState = songState;
 this.setCurrentFileInfo(this.currentFileInfo);
}

ASMA.prototype.stopCurrentSong = function(){
 if (this.currentSongState == SongState.PLAYING){
  Logger.log("Stopping current song.");
  let playingTime = UI.getElementById("playingTime");
  playingTime.innerHTML = "Stopping";
  asap.stop();
  this.setCurrentSongState(SongState.STOPPED);
  UI.getElementById("playButton").focus();
 }

};

ASMA.prototype.stopCurrentSongAndShuffleMode = function(){
 this.stopCurrentSong();
 this.setShuffleMode(false);
}


ASMA.prototype.setFileInfoContent = function(fileInfo,content){
 fileInfo.content = content;
 if (fileInfo.hardware == Hardware.ATARI800) {
  const localAsap = new ASAP();
  try {
   localAsap.load(fileInfo.getFilePath(), fileInfo.content, fileInfo.content.length);
  } catch (exception){
   throw "Cannot load \""+fileInfo.getFilePath()+"\": "+exception;
  }
  fileInfo.asapInfo = localAsap.getInfo();
  fileInfo.saveExtensions = [];
  ASAPWriter.getSaveExts( fileInfo.saveExtensions, fileInfo.asapInfo, fileInfo.content, fileInfo.content.length);
 } else {
  fileInfo.asapInfo = null;
  fileInfo.saveExtensions = ["ttt"];
 }
}

// Loads the song content either from fileInfo.url or from fileInfo.file
ASMA.prototype.loadFileContent = function(fileInfo, onLoadCompleted, onLoadFailed, parameter){
  if (fileInfo.content == null) {
   if (fileInfo.url != null) {
    const request = new XMLHttpRequest();
    request.open("GET", fileInfo.url, true);
    request.responseType = "arraybuffer";
    request.onload = e => {
     if (request.status == 200 || request.status == 0) {
      try {
       this.setFileInfoContent(fileInfo, new Uint8Array(request.response));
      } catch (exception){
        onLoadFailed(this, fileInfo, parameter);
        return;
      }
      onLoadCompleted(this, fileInfo, parameter);
     } else {
      onLoadFailed(this, fileInfo, parameter);
     }
    };
    request.send();
   } else if (fileInfo.file != null){
    const reader = new FileReader();
    reader.onload = e => {
     try {
       this.setFileInfoContent(fileInfo, new Uint8Array(e.target.result));
     } catch (exception){
      onLoadFailed(this, fileInfo, parameter)
      return;
     }
     onLoadCompleted(this, fileInfo, parameter);
    }
    reader.readAsArrayBuffer(fileInfo.file);
   }
 } else {
   onLoadCompleted(this, fileInfo, parameter);
 }

};


function stripExtensionFromFilePath(filePath){
 let result = filePath;
 let lastPeriodIndex = result.lastIndexOf(".");
 if (lastPeriodIndex >=0) { result = result.substring(0,lastPeriodIndex); }
 return result;
}

function getFileNameFromFilePath(filePath){
 let result = filePath;
 let lastSlashIndex = result.lastIndexOf("/");
 if (lastSlashIndex >=0) { result = result.substr(lastSlashIndex+1); }
 return result;
}

function getFileNameWithoutExtensionFromFilePath(filePath){
 return stripExtensionFromFilePath(getFileNameFromFilePath(filePath));
}


function titleCase(string){
 return string[0].toUpperCase() + string.slice(1).toLowerCase();
}

ASMA.prototype.exportFileContentWithExtensionIndex = function(extensionIndex){
 this.stopCurrentSong();
 let fileInfo = this.currentFileInfo;
 return this.exportFileContentWithExtension(fileInfo, fileInfo.saveExtensions[extensionIndex]);
}

ASMA.prototype.exportFileContentWithExtension = function(fileInfo, extension){
 let targetFilename = getFileNameWithoutExtensionFromFilePath(fileInfo.getFilePath());
 targetFilename += "."+extension;
 Logger.log("Exporting file content of \""+fileInfo.filePath+"\" with extension \""+extension+"\".");
 let output = null;
 if (fileInfo.asapInfo != null) {
  let asapWriter = new ASAPWriter();
  output = new Uint8Array(655636);
  let outputOffset = 0;
  asapWriter.setOutput(output, outputOffset, output.length);

  let tag = true;
  outputOffset = asapWriter.write(targetFilename, fileInfo.asapInfo, fileInfo.content, fileInfo.content.length, tag);
  output = output.slice(0, outputOffset)

 } else {
  output = fileInfo.content;
 }
 var blob = new Blob([output], {type: "application/octet-stream"});
 saveAs(blob, targetFilename);
 return targetFilename;
};


ASMA.prototype.onFileContentLoadSuccess = function(asma, fileInfo, parameter){
 Logger.log("Loaded \""+fileInfo.title+"\" from \""+fileInfo.getFilePath()+"\".");
 asma.setCurrentFileInfo(fileInfo);
 let song = parameter;
 if (song == SongNumber.RANDOM){
  song = Math.floor(Math.random() * fileInfo.songs);
  asma.currentSong = song; // Write back to global state
 }
 if (song >= 0) {
  switch (fileInfo.hardware){
  case Hardware.ATARI800:
   asap.playContent(fileInfo.getFilePath(), fileInfo.content, song);
   asma.setCurrentSongState(SongState.PLAYING);
   break;
  case Hardware.ATARI2600:
   asma.setCurrentSongState(SongState.NONE);
   break;
  }
 }
};

ASMA.prototype.onFileContentLoadFailure = function(asma, fileInfo, parameter){
 window.alert("Loading \""+fileInfo.filePath+"\" failed");
}

ASMA.prototype.playSong = function(song){
 Logger.log("Playing \""+this.currentFileInfo.title+"\", song "+song+".");
 asap.stop(); // Stop audio without change the asma state/display
 this.currentSongState = SongState.STOPPED;
 this.setCurrentFileIndexAndSong(this.currentFileInfo.getIndex(), song);
};

ASMA.prototype.onOpenedFileContentLoadSuccess = function(asma, fileInfo, state){
 const index = asma.fileInfos.length;
 const asapInfo = fileInfo.asapInfo;
 fileInfo.index = index;
 fileInfo.title = asapInfo.getTitle();
 if (fileInfo.title == "") {
  fileInfo.title = titleCase(getFileNameWithoutExtensionFromFilePath(fileInfo.file.name));
 }
 fileInfo.author = asapInfo.getAuthor();
 if (fileInfo.author == "") {
  fileInfo.author = "Unknown";
 }
 fileInfo.date = asapInfo.getDate();
 fileInfo.originalModuleExt = asapInfo.getOriginalModuleExt(fileInfo.content, fileInfo.content.length);
 if (fileInfo.originalModuleExt != null){
  fileInfo.originalModuleExtDescription = ASAPInfo.getExtDescription(fileInfo.originalModuleExt);
 } else {
  fileInfo.originalModuleExt = "???";
  fileInfo.originalModuleExtDescription = "Unknown";
 }
 fileInfo.songs = asapInfo.getSongs();
 fileInfo.defaultSong = asapInfo.getDefaultSong();

 asma.fileInfos[index] = fileInfo;
 state.count -= 1;
 if (state.count == 0){
  asma.clearSearchFields();
  asma.search();
 }
}

ASMA.prototype.onOpenedFileContentLoadFailure = function(asma, fileInfo, state){
 window.alert("Loading \""+fileInfo.getFilePath()+"\" failed");

 state.count -= 1;
 if (state.count == 0){
  asma.clearSearchFields();
  asma.search();
 }
}

ASMA.prototype.openFiles = function(files){

 this.stopCurrentSong();

 if (files.length == 0){
  this.clearCurrentFile();
  this.fileInfos = asma;
  this.clearSearchResult();
 } else {
  this.fileInfos = [];

  let state = {
    count: files.length
  };

  for (const file of files) {
     let fileInfo = {
      hardware: Hardware.ATARI800,
      filePath: file.name,
      file: file,
      title: getFileNameFromFilePath(file.name),
      author: "",
      date: "",
      comment: "",
      originalModuleExt: "",
      originalModuleExtDescription: ""
    }
    this.loadFileContent(fileInfo, this.onOpenedFileContentLoadSuccess, this.onOpenedFileContentLoadFailure, state);
  }
 }
};

ASMA.prototype.displayAuthorDetails = function(index){
 let fileInfo = this.currentFileInfo;
 if (fileInfo == null) {
  return;
 }
 let composerProxies = fileInfo.getComposerProxies();
 if (index >= composerProxies.size){
  return;
 }
 let composerProxy = composerProxies.at(index);
 this.displayComposerDetails(composerProxy);
}

ASMA.prototype.displayComposerDetails = function(composerProxy){

 let dialogTitle = UI.getElementById("displayAuthorDialogTitle");
 let authorFullNameUnicode = UI.getElementById("authorFullNameUnicode");
 let authorHandles = UI.getElementById("authorHandles");
 let authorCountry = UI.getElementById("authorCountry");
 let authorDemozooID = UI.getElementById("authorDemozooID");
 let authorFileCount = UI.getElementById("authorFileCount");
 let authorFolderPath = UI.getElementById("authorFolderPath");

 let title = "Author Details";
 if (composerProxy.getStatus() == ComposerStatus.UNVERIFIED) { title += "(Unverified)"; }
 dialogTitle.innerHTML = title;
 authorFullNameUnicode.innerHTML = UI.encodeHTML(composerProxy.getFullNameUnicode());
 authorHandles.innerHTML = UI.encodeHTML(composerProxy.getHandles());
 authorCountry.innerHTML = Util.getCountryFlagHTML(composerProxy.getCountry());

 let demozoolHTML= composerProxy.getDemozooHTML();
 if (demozoolHTML != "") {
  authorDemozooID.innerHTML = demozoolHTML;
 } else {
  authorDemozooID.innerHTML = "None";
 }

 authorFileCount.innerHTML = "<a href=\"javascript:asmaInstance.displayAuthorSongs("+composerProxy.getIndex()+")\">"+UI.encodeHTML(composerProxy.getFileCount().toString())+"</a>";
 authorFolderPath.innerHTML = UI.encodeHTML(composerProxy.getFolderPath());

 UI.showModalDialog(this.displayAuthorDialog);
}

ASMA.prototype.displayAuthorSongs = function(index){
 UI.hideModalDialog(this.displayAuthorDialog);
 UI.hideModalDialog(this.aboutDialog);

 let searchParameters = new SearchParameters();
 if (index === undefined){
   searchParameters.searchAuthor = this.currentFileInfo.author;
 } else {
   let composerProxy = this.composerList.getComposerProxy(index);
   let author = composerProxy.getFullName();
   if (author == "<?>"){
     author = composerProxy.handles;
   }
   searchParameters.searchAuthor = author;
 }
 this.searchUsingParameters( searchParameters );
}

/*
 * Edit song routine
 */
ASMA.getNTSCPALString = function(fileInfo){
 return fileInfo.asapInfo.isNtsc()?"NTSC":"PAL";
}

ASMA.prototype.editCurrentSong = function(){
 let oldFileInfo = this.currentFileInfo;
 if (oldFileInfo == null || oldFileInfo.asapInfo == null){
  return;
 }
 this.stopCurrentSong();

 let editFilePath = UI.getElementById("editFilePath");
 let editTitle = UI.getElementById("editTitle");
 let editAuthor = UI.getElementById("editAuthor");
 let editDate = UI.getElementById("editDate");
 let editNTSC = UI.getElementById("editNTSC");

 let fileInfo = Object.assign(new FileInfo(), oldFileInfo);
 editFilePath.innerHTML = UI.encodeHTML(fileInfo.getFilePathWithBreaks());
 editTitle.value = this.currentFileInfo.title;
 editAuthor.value = this.currentFileInfo.author;
 editDate.value = this.currentFileInfo.date;
 editNTSC.value = ASMA.getNTSCPALString(this.currentFileInfo);

 // Get the <span> element that closes the modal
 var closeIcon = UI.getElementById("editSongDialogCloseIcon");

// When the user clicks on <span> (x), close the modal
 closeIcon.onclick = function() {
   asmaInstance.closeEditSongDialog();
 }

 this.editSongDialog.style.display = "block";
};

ASMA.prototype.closeEditSongDialog = function(){
 this.editSongDialog.style.display = "none";
}

ASMA.prototype.downloadEditSongDialog = function(){
 this.closeEditSongDialog();
}

ASMA.prototype.getAttributeLine = function(label,newValue,oldValue){
 return `${label} ${newValue==oldValue?" Unchanged":"Changed"}: ${newValue}\n`;
}

// With mail=true it is submit, otherwise it is only download of the modified file
ASMA.prototype.submitEditSongDialog = function(mail){
 let editTitle = UI.getElementById("editTitle");
 let editAuthor = UI.getElementById("editAuthor");
 let editDate = UI.getElementById("editDate");
 let editNTSC = UI.getElementById("editNTSC");
 let title=editTitle.value;
 let author=editAuthor.value;
 let date=editDate.value;
 let ntsc=editNTSC.value;
 let fileInfo = this.currentFileInfo;
 let upload=(fileInfo.file!=undefined);

 let targetFilePath = this.exportFileContentWithExtension(fileInfo, "sap");
 if (mail){
  let emailTo = "asma@wudsn.com";
  let emailSubject = "ASMA: "+(upload?"Upload":"Update ")+" "+fileInfo.getFilePath();
  let emailBody = this.getAttributeLine("Title", title, fileInfo.title)
               + this.getAttributeLine("Author", author, fileInfo.author)
               + this.getAttributeLine("Date", date, fileInfo.date)
               + this.getAttributeLine("NTSC/PAL", ntsc, ASMA.getNTSCPALString(fileInfo))
               + "Please attach the generated \""+targetFilePath+"\" file from your Downloads folder.";
  let mailWindow = window.open(`mailto:${emailTo}?subject=${encodeURI(emailSubject)}&body=${encodeURI(emailBody)}`, '_self');
  if (!mailWindow){
   window.alert("No mail program available.")
  }
  this.closeEditSongDialog();
 }
}

ASMA.prototype.cancelEditSongDialog = function(){
 this.closeEditSongDialog();
}

ASMA.prototype.displayAboutDialog = function(){

 let aboutDialogGeneralContent = UI.getElementById("aboutDialogGeneralContent");
 aboutDialogGeneralContent.innerHTML = "<a href=\"javascript:asmaInstance.checkDemozoo()\">Retrieve Demozoo Productions</a><br>\n"+this.demozooCheckResult;
 

 let resultHTML;
 resultHTML="";
 resultHTML+='<table id="composersByNameTable" class="statistic_table sortable">';
 resultHTML+="<tr>"
 if (this.detailsMode){
  resultHTML+='<th id="composersByNameTableSortHeader" class="statistic_header sorttable_alpha">Last Name</th>';
  resultHTML+='<th class="statistic_header sorttable_alpha">First Name</th>';
  resultHTML+='<th class="statistic_header sorttable_alpha">Handles</th>';
 } else {
  resultHTML+='<th id="composersByNameTableSortHeader" class="statistic_header sorttable_alpha">Full Name (Handles)</th>';
 }
 resultHTML+='<th class="statistic_header sorttable_alpha">Country</th>';
 resultHTML+='<th class="statistic_header sorttable_numeric">Demozoo ID</th>';
 resultHTML+='<th class="statistic_header_numeric sorttable_numeric">File Count</th>';
 resultHTML+="</tr>"
 for(let composerProxy of this.composerList.composerProxies){
  if (composerProxy.getStatus() != ComposerStatus.VERIFIED) { continue; } // Only verified composers

  resultHTML+="<tr>";
  if (this.detailsMode){
   resultHTML+="<td sorttable_customkey=\""+composerProxy.getLastNameSortKey()+"\" title=\""+UI.encodeHTML(composerProxy.getFolderName())+"\">"+UI.encodeHTML(composerProxy.getLastNameUnicode())+"</td>";
   resultHTML+="<td class=\"statistic_cell\">"+UI.encodeHTML(composerProxy.getFirstNameUnicode())+"</td>";
   resultHTML+="<td class=\"statistic_cell\">"+UI.encodeHTML(composerProxy.getHandles())+"</td>";
  } else {
   let fullName = composerProxy.getFullNameUnicode();
   let handles = composerProxy.getHandles();
   if (handles!= "") {
    fullName += " ("+handles+")";
   }
   resultHTML+="<td class=\"statistic_cell\">"+UI.encodeHTML(fullName)+"</td>";
  }
  resultHTML+="<td class=\"statistic_cell\">"+Util.getCountryFlagHTML(composerProxy.getCountry())+"</td>";
  resultHTML+="<td class=\"statistic_cell\" sorttable_customkey=\""+composerProxy.getDemozooID()+"\">"+composerProxy.getDemozooHTML()+"</td>";
  resultHTML+="<td class=\"statistic_cell_numeric\">"+composerProxy.getFileCountHTML()+"</td>";

  resultHTML+="</tr>";
 }

 resultHTML+="</table>";
 UI.initResult("composersByName", resultHTML);

 resultHTML="";
 resultHTML+='<table id="composersByCountryTable" class="statistic_table sortable">';
 resultHTML+="<tr>"
 resultHTML+='<th id="composersByCountryTableSortHeader" class="statistic_header_numeric sorttable_numeric">Composers</th>';
 resultHTML+='<th class="statistic_header sorttable_alpha">Country</th>';
 resultHTML+="</tr>"
 for(let countryInfo of this.composerList.countryInfoMap){
  resultHTML+="<tr>";
  let normalizedCountry = countryInfo[1].normalizedCountry;
  resultHTML+="<td class=\"statistic_cell_numeric\">"+UI.encodeHTML(countryInfo[1].composerCount.toString())+"</td>";
  resultHTML+="<td class=\"statistic_cell\">"+Util.getCountryFlagHTML(normalizedCountry)+"</td>";
  resultHTML+="</tr>";
 }

 resultHTML+="</table>";
 UI.initResult("composersByCountry", resultHTML, true);

 UI.showModalDialog(aboutDialog);
}

ASMA.prototype.checkDemozoo = function(){
 this.demozoo.checkDemozoo();
}



var asmaInstance = new ASMA();
