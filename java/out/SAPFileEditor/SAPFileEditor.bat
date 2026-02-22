@echo off
setlocal
cd "%~dp0"
set FILE=%1

java -jar SAPFileEditor.jar %FILE%