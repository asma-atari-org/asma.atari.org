@echo off
rem Close all open Chrome windows.
taskkill /F /IM chrome* /T
rem Open Chrome with access to local files. This only works if not Chrome window is open yet.
"%ProgramFiles(x86)%\Google\Chrome\Application\chrome.exe" --process-per-site --allow-file-access-from-files --disable-session-crashed-bubble "%~dp0%index.html"