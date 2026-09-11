@echo off
@REM check for admin perms
NET SESSION >nul 2>&1
if %errorlevel% NEQ 0 (
    echo You need to run this as admin for it to enable debugging
    pause
    exit /b
)

set "SCRIPT_DIR=%~dp0"
@REM since the %~dp0 includes a trailing \ at the end, it escapes the " causing it to get fucked, so slice the last character
set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"
@REM Configure Windows Error Reporting (WER)
reg add "HKLM\SOFTWARE\Microsoft\Windows\Windows Error Reporting\LocalDumps\DDSCreator.exe" /v DumpFolder /t REG_EXPAND_SZ /d "%SCRIPT_DIR%" /f
reg add "HKLM\SOFTWARE\Microsoft\Windows\Windows Error Reporting\LocalDumps\DDSCreator.exe" /v DumpType /t REG_DWORD /d 1 /f
reg add "HKLM\SOFTWARE\Microsoft\Windows\Windows Error Reporting\LocalDumps\DDSCreator.exe" /v CustomDumpCount /t REG_DWORD /d 5 /f
pause
