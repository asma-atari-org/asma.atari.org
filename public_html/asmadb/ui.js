"use strict";

function UI() {
 this.dummy="";
}

UI.getElementById = function(id){
 let element = document.getElementById(id);
 if (element === undefined || element === null ){
  throw "Element with id '"+id+"' dose not exist";
 }
 return element;
};

UI.openTabbedPaneTab = function(tabbedPaneName, evt, tabID) {
 var i, tabs, contents;
 tabs = document.getElementsByClassName(tabbedPaneName + "_tab");
 for (i = 0; i < tabs.length; i++) {
   tabs[i].className = tabs[i].className.replace(" tab_active", "");
 }
 contents = document.getElementsByClassName(tabbedPaneName+"_content");
 for (i = 0; i < contents.length; i++) {
   contents[i].style.display = "none";
 }

 UI.getElementById(tabID).style.display = "block";
 evt.currentTarget.className += " tab_active";
};

UI.encodeHTML = function(text){
 return text.replace(/[\u00A0-\u9999<>\&]/g, ((i) => `&#${i.charCodeAt(0)};`));
};

// Assume a div named "...Result" containing a sortable table "...Table" with the default sort header column "...TableSortHeader"
UI.initResult = function(resultPrefix, resultHTML, descending){
 let resultElement = UI.getElementById(resultPrefix+"Result");
 resultElement.innerHTML=resultHTML;
 let newTableObject = UI.getElementById(resultPrefix+"Table");
 sorttable.makeSortable(newTableObject);
 UI.getElementById(resultPrefix+"TableSortHeader").click();
 if (descending === true){
  UI.getElementById(resultPrefix+"TableSortHeader").click();
 }
};

UI.setEnabled= function(elementID, enabled){
 let element = UI.getElementById(elementID);
 element.disabled = !enabled;
};


UI.setVisible = function(elementID, visible){
 let element = UI.getElementById(elementID);
   if (visible === true) {
    element.style.display = "";
  } else {
    element.style.display = "none";
  }
};

UI.hideModalDialog = function(dialog){
 dialog.style.display = "none";
}

UI.showModalDialog = function(dialog, closeCallbackFunction, closeCallbackFunctionParameter){
 // Get the <span> element that closes the modal
 var closeIcon = UI.getElementById(dialog.id+"CloseIcon");
 closeIcon.classList="close";
 closeIcon.innerHTML="&times;";

// When the user clicks on <span> (x), close the modal 
 closeIcon.onclick = function() {
  UI.hideModalDialog(dialog);
  if (closeCallbackFunction != undefined){
   closeCallbackFunction(closeCallbackFunctionParameter);
  }
 }

 dialog.style.display = "block";
}

