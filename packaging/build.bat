@echo off
setlocal
set "SCRIPT_DIR=%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%build.ps1" %*
set "EXIT_CODE=%ERRORLEVEL%"
if %EXIT_CODE% neq 0 (
    echo [ERRO] O processo de build falhou com codigo %EXIT_CODE%.
    exit /b %EXIT_CODE%
)
exit /b 0
