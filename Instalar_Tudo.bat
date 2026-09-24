@echo off
setlocal EnableExtensions EnableDelayedExpansion

rem ============================================================
rem  NASSER ESFIHA - INSTALADOR COMPLETO DE AMBIENTE
rem ============================================================

rem --- MARCA ASCII ---
set "ESC=\x1b"
set "BRANCO=!ESC![97m"
set "VERMELHO=!ESC![91m"
set "RESET=!ESC![0m"
chcp 65001 >nul
<nul set /p "=!BRANCO!"
echo.
echo.                                                                                ++####
echo.                                                                                ########mm                    ########
echo.                                                                              ##########  ##                  ##############
echo.                                                                            ++################--##          ##############
echo.                                                                          ##########################        ######..
echo.                                                                        ############################        ######
echo.                                                                      ################################  mm########
echo.                                                                      ##########################################
echo.                                                                    ##########################################
echo.                                                                    ##########  ############################
echo.                                                                    ##########MM####################
echo.                                                                  ##  ##########  --################
echo.                                                                  ############MM          ####  ####
echo.                                                                    ####  ####            ####    ##
echo.                                                                    ####    ####            ##      ##
echo.                                                                  ##      ####          ####        ##
echo.                                                                  ##      --##          ##          ##
echo.                                                                  ##        ##          ##          ##MM
echo.                                                                  ##        mm##        ##            ##
echo.                                                                @@##          ##        ####            ##
echo.                                                          ##############################################################
<nul set /p "=!RESET!"

echo.
echo ============================================================
echo   INSTALADOR COMPLETO DE AMBIENTE - NASSER ESFIHA
echo ============================================================
echo.

rem --- Auto-elevacao ---
net session >nul 2>&1
if not "%errorlevel%"=="0" (
    echo Solicitando privilegios de Administrador...
    powershell.exe -NoProfile -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
    exit /b 0
)

cd /d "%~dp0"
title Instalador Completo de Ambiente - Nasser Esfiha

rem --- Winget ---
where winget >nul 2>&1
if errorlevel 1 (
    echo ERRO: winget nao foi encontrado.
    echo Instale o App Installer da Microsoft Store e execute novamente.
    pause
    exit /b 1
)

rem --- Java 21+ ---
set "JAVA_OK=0"
where java >nul 2>&1
if not errorlevel 1 (
    set "JAVA_MAJOR="
    for /f "delims=" %%V in ('powershell.exe -NoProfile -Command "$v=(java -version 2^>^&1 | Select-String version).ToString(); if($v -match '([0-9]+)') { $Matches[1] }"') do set "JAVA_MAJOR=%%V"
    if defined JAVA_MAJOR if !JAVA_MAJOR! GEQ 21 set "JAVA_OK=1"
)
if "%JAVA_OK%"=="0" (
    echo [1/3] Instalando Eclipse Temurin JDK 21...
    winget install --id EclipseAdoptium.Temurin.21.JDK --exact --silent --accept-package-agreements --accept-source-agreements
    if errorlevel 1 goto :fail
) else (
    echo [1/3] Java 21 ou superior detectado.
)

rem Atualiza JAVA_HOME para esta sessao e para novos terminais.
for /f "delims=" %%J in ('powershell.exe -NoProfile -Command "$j=(Get-Command java.exe).Source; if($j){(Split-Path (Split-Path $j -Parent) -Parent)}"') do set "JAVA_HOME=%%J"
if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    setx JAVA_HOME "%JAVA_HOME%" >nul 2>&1
)
where java >nul 2>&1
if errorlevel 1 (
    echo ERRO: Java nao ficou disponivel apos a instalacao.
    goto :fail
)

rem --- Maven ---
where mvn >nul 2>&1
if errorlevel 1 (
    echo [2/3] Instalando Apache Maven...
    winget install --id Apache.Maven --exact --silent --accept-package-agreements --accept-source-agreements
    if errorlevel 1 goto :fail
) else (
    echo [2/3] Maven detectado.
)

rem Recarrega PATH do sistema apos o Winget.
for /f "delims=" %%P in ('powershell.exe -NoProfile -Command "[Environment]::GetEnvironmentVariable('Path','Machine')"') do set "PATH=%%P;%PATH%"
where mvn >nul 2>&1
if errorlevel 1 (
    if exist "%~dp0mvnw.cmd" (
        set "MVN=%~dp0mvnw.cmd"
    ) else (
        echo ERRO: Maven nao ficou disponivel apos a instalacao.
        goto :fail
    )
) else (
    set "MVN=mvn"
)

rem --- Build ---
echo [3/3] Baixando dependencias e compilando a aplicacao...
call "%MVN%" clean install -DskipTests
if errorlevel 1 goto :fail

echo.
echo ============================================================
echo   [SUCESSO] Ambiente instalado e projeto compilado.
echo   Execute "Nasser Etiquetas.bat" para iniciar o sistema.
echo ============================================================
pause
exit /b 0

:fail
echo.
echo ============================================================
echo   [ERRO] A instalacao nao foi concluida.
echo   Verifique as mensagens acima e execute novamente.
echo ============================================================
pause
exit /b 1
