@echo off
setlocal
cd /d "%~dp0"
title Nasser Etiquetas - Iniciando

if exist "target\etiqueta-app.jar" (
    start "" /b javaw -jar "%~dp0target\etiqueta-app.jar" >nul 2>&1
    exit /b 0
)

if exist "mvnw.cmd" (
    start "" /b cmd /d /c ""%~dp0mvnw.cmd" -q javafx:run >nul 2>&1"
    exit /b 0
)

start "" /b cmd /d /c "mvn javafx:run >nul 2>&1"
exit /b 0
