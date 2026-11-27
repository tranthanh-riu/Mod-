@echo off
title Elden Ring Core - Minecraft 1.20.1 Dev Client
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0"
echo Dang khoi dong Minecraft 1.20.1 Forge (Elden Ring Core)...
call gradlew.bat runClient --no-daemon -Pmodpack
pause
