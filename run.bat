@echo off
set "MAVEN_PATH=C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.1.1\plugins\maven\lib\maven3\bin\mvn.cmd"
if exist "%MAVEN_PATH%" (
    echo Starting Skillora CRUD...
    "%MAVEN_PATH%" javafx:run
) else (
    echo Maven not found at the expected location.
    echo Please run the project using the "Run" button in your IDE.
    pause
)
