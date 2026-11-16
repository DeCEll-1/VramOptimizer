@echo off
cd /d "%~dp5"
cd ../External/SharpShaders
dotnet build SharpShaders.csproj
if "%CI%"=="true" exit /b %ERRORLEVEL%
Pause