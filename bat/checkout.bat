@echo off
rem ASAP Wiki and SVN are at "http://asma.scene.pl".
rem SVN download is available at "https://tortoisesvn.net/downloads.html".
rem Ensure to select the "Command Line Tools" during the installation, to install "svn.exe".
rem This script assumes that "svn.exe" is in the path.
rem Global SVN configuration is stored in "%APPDATA%\Subversion\".
rem Make sure to configure using commit time as file date/time.
rem SVN content is checked out into a separate folder structure to separate SVN and Git versioning.
rem A symbolic link is created for the "asma" folder in "public_html/asma" to include it into the website upload.

set SITES_FOLDER=C:\jac\system\WWW\Sites
set WUDSN_SITE_LOCAL_ASMA_FOLDER=%SITES_FOLDER%\www.wudsn.com\tmp\asma
set WUDSN_SITE_HTML_FOLDER=%WUDSN_SITE_LOCAL_ASMA_FOLDER%\public_html
set WINRAR=C:\jac\system\Windows\Tools\FIL\WinRAR\winrar.exe

echo Checking out current revision ASMA from SVN.
set SVN_FOLDER=C:\jac\system\Atari800\Sounds\ASMA\SVN
set SVN_TRUNK_FOLDER=%SVN_FOLDER%\trunk
set SVN_TRUNK_ASMA_FOLDER=%SVN_TRUNK_FOLDER%\asma
set SVN_ASMA_TEXT_FILE=%SVN_TRUNK_ASMA_FOLDER%\asma.txt

set SITE_FOLDER=%SITES_FOLDER%\asma.atari.org
set SITE_HTML_FOLDER=%SITE_FOLDER%\public_html
set SITE_ASMA_FOLDER=%SITE_HTML_FOLDER%\asma

set SITE_ASMADB_FOLDER=%SITE_HTML_FOLDER%\asmadb
set SITE_ASMADB_JS=%SITE_ASMADB_FOLDER%\asmadb.js
set SITE_ASMA_ZIP_FILE=%SITE_ASMADB_FOLDER%\asma.zip

set JAVA_FOLDER=%SITE_FOLDER%\java

cd %SVN_FOLDER%
echo ASMA - Atari SAP Music Archive >%SVN_ASMA_TEXT_FILE%
echo https://asma.atari.org>>%SVN_ASMA_TEXT_FILE%

rem Checkout current ASMA revision.
rem TortoiseSVN has the SVN command line client tools included in the download,
rem but you must explicitly select them during installation.
svn checkout svn://asma.scene.pl/asma/trunk >>%SVN_ASMA_TEXT_FILE%
if ERRORLEVEL 1 (
  echo ERROR: SVN from https://tortoisesvn.net/downloads.html must be in the path.
  goto :eof
 )
echo Checked out on %DATE%. >>%SVN_ASMA_TEXT_FILE%
type %SVN_ASMA_TEXT_FILE%


rem Creating missing directory junctions does not require you to be an administrator
if not exist %SITE_HTML_FOLDER% (
  mkdir %SITE_HTML_FOLDER%
)
if not exist %SITE_ASMA_FOLDER% (
  mklink /J %SITE_ASMA_FOLDER% %SVN_TRUNK_ASMA_FOLDER%
  if ERRORLEVEL 1 (
    echo ERROR: Creation of preview junction failed. 
    exit /b 1
  )
)

rem Create directory junction, so ASMA can also be viewed in local XAMPP via http://127.0.0.1:8080/tmp/asma/public_html/
if not exist %WUDSN_SITE_HTML_FOLDER%. (
  mklink /J %WUDSN_SITE_HTML_FOLDER% %SITE_HTML_FOLDER%
  if ERRORLEVEL 1 (
    echo ERROR: Creation of preview junction failed. 
    exit /b 1
  )
)

echo.
rem Run the exporter to update the JSON database.
rem java --version 
%JDK_HOME%\bin\java -cp "%JAVA_FOLDER%\bin;%JAVA_FOLDER%\lib\asap.jar;%JAVA_FOLDER%\lib\gson-2.9.1.jar" org.atari.asma.ASMAExporter %SVN_TRUNK_ASMA_FOLDER% %SITE_ASMADB_JS%
if ERRORLEVEL 1 (
    echo ERROR: ASMAExporter failed.
    exit /b 1
  )
echo.

rem Create the overall ZIP.
set TARGET=%SITE_ASMA_ZIP_FILE%
echo Creating %TARGET%
if exist %TARGET% del %TARGET%
cd %SVN_TRUNK_FOLDER%
%WINRAR% a -r -afzip %TARGET% asma

cd %SVN_FOLDER%

call %SITE_HTML_FOLDER%\index.bat