@echo off
REM Must run from this folder so Maven loads pom.xml (javafx plugin + classpath).
cd /d "%~dp0"
echo Running Skillora from: %CD%
echo.
mvn javafx:run
if errorlevel 1 pause
