@echo off
rem Starts the ASMABrowser. Requires a Java 21 (or newer) runtime.
start "ASMABrowser" javaw -jar "%~dp0ASMABrowser.jar" %*
