@echo off
rem Starts the SAP file editor. Requires a Java 21 (or newer) runtime.
start "SAPFileEditor" javaw -jar "%~dp0SAPFileEditor.jar" %*
