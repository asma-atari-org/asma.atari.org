@echo off
setlocal
set SITES_FOLDER=C:\jac\system\WWW\Sites
set SITE=asma.atari.org
set SITE_FOLDER=%SITES_FOLDER%\%SITE%
cd %SITE_FOLDER%\

if "%ASMA_USER%" == "" (
 echo ERROR: Evironment variable ASMA_USER for FTP access to %SITE% is not set to the user name.
 pause
 goto :eof
)
if "%ASMA_PASSWORD%" == "" (
 echo ERROR: Evironment variable ASMA_PASSWORD for FTP access to %SITE% is not set to the URL encoded password.
 pause
 goto :eof
)

rem Use "public_html" folder by default. Use "." to sync all folders.
set FOLDER=public_html
if not [%1]==[] set FOLDER=%1

set WINSCP=C:\jac\system\Windows\Tools\NET\WinSCP\WinSCP.com

mode con:cols=320 lines=800
echo Synchronizing folder %FOLDER%.

"%WINSCP%" ^
  /ini=nul ^
  /loglevel=2 ^
  /command ^
    "open ftp://%ASMA_USER%:%ASMA_PASSWORD%@asma.atari.org/ -hostkey=""ssh-rsa 2048 LeEFmKBjiWH8P0uMaCYmDkXUncdNrfvRqCf+KVCZS4M""" ^
    "lcd %SITE_FOLDER%\%FOLDER%" ^
    "cd /%FOLDER%" ^
    "synchronize -delete remote" ^
    "exit"

echo Done.
