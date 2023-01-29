"use strict";

const ComposerStatus = { UNVERIFIED: "UNVERIFIED", VERIFIED : "VERIFIED" };

function ComposerProxy(index, status, composer) {
 this.index = index;
 this.status = status;
 this.composer = composer;
 this.fileCount = 0;

 this.lastName = (composer.lastName != undefined?composer.lastName:"");
 this.firstName = (composer.firstName != undefined?composer.firstName:"");
 this.lastNameUnicode = (composer.lastNameUnicode != undefined?composer.lastNameUnicode:this.lastName);
 this.firstNameUnicode = (composer.firstNameUnicode != undefined?composer.firstNameUnicode:this.firstName);
 this.handles = (composer.handles != undefined?composer.handles:"");

 if (composer.fullName != undefined){
  this.fullName = composer.fullName;
  this.lastNameSortkey = this.getFullName()+"_"+this.getHandles();
 } else {
  this.fullName = this.getFirstName()+" "+this.getLastName();
  this.fullName = this.fullName.trim();
  this.lastNameSortkey = this.getLastName()+"_"+this.getFirstName()+"_"+this.getHandles();
 }

 if (composer.fullNameUnicode != undefined){
  this.fullNameUnicode = composer.fullNameUnicode;
 } else {
  this.fullNameUnicode = this.getFirstNameUnicode()+" "+this.getLastNameUnicode();
  this.fullNameUnicode = this.fullNameUnicode.trim();
 }
 this.country = ((composer.country != undefined && composer.country != "")?composer.country:"Unknown");
 this.demozooID = (composer.demozooID != undefined?composer.demozooID:"");
}

ComposerProxy.prototype.getIndex = function(){
 return this.index;
};

ComposerProxy.prototype.getStatus = function(){
 return this.status;
};

ComposerProxy.prototype.getLastName = function(){
  return this.lastName;
};

ComposerProxy.prototype.getFirstName = function(){
  return this.firstName;

};

ComposerProxy.prototype.getLastNameUnicode = function(){
  return this.lastNameUnicode;
};

ComposerProxy.prototype.getFirstNameUnicode = function(){
  return this.firstNameUnicode;

};

ComposerProxy.prototype.getFullName = function(){
  return this.fullName;
};

ComposerProxy.prototype.getFullNameUnicode = function(){
  return this.fullNameUnicode;
};

ComposerProxy.prototype.getHandles = function(){
  return this.handles;
};

ComposerProxy.prototype.getHandlesArray = function(){
 let handles = this.handles.split(",");
 for (let i=0; i<handles.length;i++){
  handles[i] = handles[i].trim();
 }
 return handles;
}

ComposerProxy.prototype.getLastNameSortKey = function(){
  return this.lastNameSortkey;
};

ComposerProxy.prototype.getFolderName = function(){
  let lastName = this.getLastName();
  let firstName = this.getFirstName();
  let handlesArray = this.getHandles().split(",");
  let handle="";
  if (handlesArray.length > 0){
   handle = handlesArray[0];
  }
  let folderName;
  if (lastName == "" || lastName == "<?>"){
   folderName = handle;
  } else {
   folderName = lastName+"_"+firstName;
  }
  folderName = folderName.replace(".", "");
  folderName = folderName.replace(" ", "_");
  folderName = folderName.replace("-", "_");
  return folderName;
};

ComposerProxy.prototype.getFolderPath = function(){
 let folderName = this.getFolderName();
 if (folderName == ""){
  return "";
 }
 return "Composers/"+folderName;
}

ComposerProxy.prototype.getCountry = function(){
 return this.country;
};

ComposerProxy.prototype.getNormalizedCountry = function(){
  return Util.getNormalizedCountry(this.getCountry());
};

ComposerProxy.prototype.getDemozooID = function(){
  return this.demozooID;
};

ComposerProxy.prototype.getDemozooURL = function(){
 let demozooID = this.getDemozooID();
  if (demozooID != "") {
   return "https://demozoo.org/sceners/"+demozooID;
  }
  return undefined;
};

ComposerProxy.prototype.getDemozooHTML = function(){
  let demozooID = this.getDemozooID();
  if (demozooID != "") {
   return "<a href=\""+this.getDemozooURL()+"\" target=\"blank\" title=\"Open on Demozoo\">"+UI.encodeHTML(demozooID)+"</a>";
  }
  return "";
};

ComposerProxy.prototype.getFileCount = function(){
 return this.fileCount;
};

ComposerProxy.prototype.getFileCountHTML = function(){
 const fileCount = this.getFileCount();
 if (fileCount == 0){
  return "";
 }
 return "<a href=\"javascript:asmaInstance.displayAuthorSongs("+this.getIndex()+")\">"+UI.encodeHTML(fileCount.toString())+"</a>";
}


function ComposerList(composers){
 this.composerProxies = [];
 this.authorProxiesMap = new Map(); // Map author to composer proxy
 this.composerFolderPathProxiesMap = new Map(); // Map "Composer" folder name to composer proxy
 this.countryInfoMap = new Map();

 for(let composer of composers){
  // Create proxy
  let composerProxy = this.addComposerProxy(ComposerStatus.VERIFIED, composer);

  // Enter proxy in lookup maps
  this.authorProxiesMap.set(composerProxy.getFullName(),composerProxy);
  let handlesArray = composerProxy.getHandlesArray();
  if (handlesArray.length > 0) {
   // Create entries with single handles
   for(let handle of handlesArray){
    this.authorProxiesMap.set(composerProxy.getFullName()+" ("+handle+")",composerProxy);
   }
   this.authorProxiesMap.set(composerProxy.getFullName()+" ("+composerProxy.getHandles()+")",composerProxy);

   // Create entry with all hanldes in one
   this.authorProxiesMap.set(composerProxy.getFullName()+" ("+composerProxy.getHandles()+")",composerProxy);
  }
  this.composerFolderPathProxiesMap.set(composerProxy.getFolderPath(),composerProxy);

  // Aggregate known composers per country
  let normalizedCountry = composerProxy.getNormalizedCountry();
  let countryInfo = this.countryInfoMap.get(normalizedCountry);
  if (countryInfo == undefined){
   countryInfo = { normalizedCountry:normalizedCountry, composerCount:0 };
  }
  countryInfo.composerCount+=1;
  this.countryInfoMap.set(normalizedCountry,countryInfo);
 }
};

ComposerList.prototype.addComposerProxy = function(status, composer){
  // Create proxy
  let index = this.composerProxies.length;
  let composerProxy = new ComposerProxy(index, status, composer);
  this.composerProxies[index] = composerProxy;
  return composerProxy;
}

ComposerList.prototype.getComposerProxy = function(index){
 return this.composerProxies[index]
}

// Count the number of matching files and assign the files to their composer
ComposerList.prototype.initFileInfos = function(fileInfos, productionsByFilePathMap){
 for(let fileInfo of fileInfos){

  // Map known composers
  let filePath = fileInfo.filePath;
  let production = productionsByFilePathMap.get(filePath);
  let filePathSegments = filePath.split("/");
  if (filePathSegments.length == 3 && filePathSegments[0]=="Composers"){
    let folderPath = filePathSegments[0]+"/"+filePathSegments[1];
    let composerProxy = this.composerFolderPathProxiesMap.get(folderPath);
    if (composerProxy != undefined){
     fileInfo.addComposerProxy(composerProxy);
     composerProxy.fileCount++;
     
     if (production != undefined && !production.authorIDs.includes(Number(composerProxy.demozooID)) ){
      Logger.log("ERROR: Composer path "+filePath+" demozoo ID \""+composerProxy.demozooID+"\" is not in list of production author IDs \""+production.authorIDs+"\"");
     }
    } else {
     Logger.log("ERROR: No composer for path \""+folderPath+"\"");
    }
  }

  // Map files to authors
  // Split at "&" in case of cooperations
  let authors = fileInfo.getAuthorsArray();

  for (let author of authors){
   if (author != "" && fileInfo.author != "") {
    let authorComposerProxy = this.authorProxiesMap.get(author);
    let lastName = "<?>";
    let firstName = "";
    let fullName = "";
    let handles = "";
    let index = author.indexOf("(");
    if (index >= 0){
     fullName = author.substring(0, index-1).trim();
     let lastIndex = author.lastIndexOf(")");
     handles = author.substring(index+1, lastIndex).trim();
    } else {
     fullName = author;
     handles = "";
    }
    if (authorComposerProxy === undefined){
     authorComposerProxy = this.addComposerProxy( ComposerStatus.UNVERIFIED, { lastName: lastName, firstName: firstName, fullName: fullName, fullNameUnicode: fullName, handles: handles });
     this.authorProxiesMap.set(author, authorComposerProxy);
    }

    fileInfo.addComposerProxy(authorComposerProxy);
    authorComposerProxy.fileCount++;

   }
  }
 }
};
