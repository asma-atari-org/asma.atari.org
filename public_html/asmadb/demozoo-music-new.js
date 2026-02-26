// ==UserScript==
// @name         Demozoo - Create Music from URL
// @namespace    http://tampermonkey.net/
// @version      0.01
// @description  Take music production details from URL parameters
// @author       Peter Dell / JAC!
// @match        https://demozoo.org/music/new/*
// @icon         https://www.google.com/s2/favicons?sz=64&domain=demozoo.org
// @grant GM_xmlhttpRequest
// ==/UserScript==

(function() {
    'use strict';

    // productions/new
    const queryString = window.location.search;
    const urlParams = new URLSearchParams(queryString);
    var v = urlParams.get('title');
    if (v != undefined) { document.getElementById("id_title").value = v; }
    v = urlParams.get('release_date');
    if (v != undefined) { document.getElementById("id_release_date").value = v; }
    document.getElementById("id_platform").value = 16;
    v = urlParams.get('url');
    if (v != undefined) { document.getElementById("id_links-0-url").value = v; }

})();