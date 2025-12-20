@echo off
setlocal
set FILE=%~dp0%index.html
echo Opening "%FILE%".
rem Close all open Chrome windows.
taskkill /F /IM chrome* /T >nul 2>nul
rem Open Chrome with access to local files. This only works if no Chrome window is open yet.
set CHROME=%ProgramFiles%\Google\Chrome\Application\chrome.exe
if not exist "%CHROME%" goto :error_no_chrome

"%CHROME%" --process-per-site --allow-file-access-from-files --disable-session-crashed-bubble "%FILE%" 2>nul
if ERRORLEVEL 1 echo Could not start %CHROME%
goto :eof

:error_no_chrome
echo ERROR: %CHROME% is not installed.
exit /1
