@echo off
setlocal

rem Close all open Chrome windows.
taskkill /F /IM chrome* /T >nul 2>nul

rem Open Chrome with access to local files. This only works if no Chrome window is open yet.
set CHROME=%ProgramFiles%\Google\Chrome\Application\chrome.exe
if not exist "%CHROME%" goto :error_no_chrome

set FILE1=%~dp0%index.html
set FILE2=%~dp0%asmadb\asmadb.html
call :open_file

goto :eof


:open_file
echo Opening "%FILE1%" and  "%FILE2%
"%CHROME%" --process-per-site --allow-file-access-from-files --disable-session-crashed-bubble "%FILE1%" "%FILE2%" 
if ERRORLEVEL 1 echo Could not start %CHROME%
goto :eof

:error_no_chrome
echo ERROR: %CHROME% is not installed.
exit /1
