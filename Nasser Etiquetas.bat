@echo off
setlocal enabledelayedexpansion

REM Fixa o caminho padrao do Windows
set "PATH=%SystemRoot%\system32;%SystemRoot%;%SystemRoot%\System32\Wbem;%SystemRoot%\System32\WindowsPowerShell\v1.0\;%PATH%"

REM Reabre este mesmo launcher maximizado na primeira execucao
if /i not "%~1"=="max" (
    start "" /max "%~f0" max
    exit /b 0
)

cls
chcp 65001 >nul

REM Obtem o caractere ESC para cores ANSI
for /f "delims=" %%A in ('echo prompt $E^| cmd') do set "ESC=%%A"

set "BRANCO=!ESC![97m"
set "VERMELHO=!ESC![91m"
set "RESET=!ESC![0m"

REM --- DESENHO DO CAMELO (BRANCO) ---
<nul set /p "=!BRANCO!"

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
echo.                                                              ##############################################################

REM --- DESENHO NASSER ESFIHA (VERMELHO) ---
<nul set /p "=!VERMELHO!"

echo.                                          MMMM@@        MM##
echo.                                          MMMMMM        MM        MMMMMM        ##MMMMMMMM--    MMMMMMMMMM  @@MMMMMMMMMM++  MMMMMMMMCCCC
echo.                                          MMMMMMMM      MM        MMMMMM        MM      MM    MMMM    ::MM    MMMM    ::##    MMMM  @@MMMM
echo.                                          MMMMMMMM@@    MM        MM@@MM      ::MMMM    ++    MMMMmm    ++    MMMM            MMMM    MMMM
echo.                                          MMMM  MMMM..  MM      MM@@  MMMM      MMMMMM@@    ##MMMMMM        MMMM    MM      MMMM    MMMM
echo.                              ++MM        MMMM  --MMMM  MM      MM    MMMM        MMMMMMMM      ##MMMMMMMM    MMMMMMMMMM      MMMMMMMMMM
echo.                            --MM          MMMM    MMMMMMMM      MM@@@@MMMM            MMMMMM        mmMMMM    MMMM    MM      MMMM@@MM##
echo.                            MMMM          MMMM      MMMMMM    MMMM------MMMM    ##      MMMM  MM        MM--  MMMM            MMMM  MMMM
echo.                            --MMMM        MMMM        MMMM    MM##      MMMM    MM      MMMM  MMMM    ..MM    MMMM      MM    MMMM  --MMMM
echo.                              MMMM      ++MMMM        ##MM  ##MMMM      MMMMMM  MMMMMMMMMM    mmMMMMMMMM    @@MMMMMMMMMMMM  @@MMMM    MMMM@@
echo.                          MMMM++MMMM                                                                                                MM::@@MM##
echo.                        MMMMMMMMMMMM##                                                                                              MMMMMMMMMMMMMM
echo.                          ++++    ##MM        MMMMMMMMMMMM      ++MMMMMM    MMMMMMMMMMMM  MMMMMMMM  MMMMMMMM    MMMMMMMM      MMMMMM          ..MM      ++
echo.                              ##MMMMMM..      MMMMmm  ..MM    @@MM    MMMM    MMMM    MM++  MMMM      MMMM        MMMM        MMMMMM          MMMMMMMM
echo.                              MM    mmMM      mmMMmm          MMMM      @@    MMMM      @@  MMMM      MMMM        MMMM        MM  MM--        MM      MM
echo.                              MM  MM  MM      mmMMmm    ++    MMMMMMMM        MMMM          MMMM      MMMM        MMMM        MM  MMMM        MM  MM  MM
echo.                              MMMM@@  ##      mmMMMMMMMM++      MMMMMMMM      MMMM@@MMMM    MMMM      MMMMMMMMMMMMMMMM        MMMM  MMMM        ##  MMMM@@
echo.                                              mmMMmm    ++        ::MMMMMM    MMMM  ..MM    MMMM      MMMM        MMMM      MM    --MM@@
echo.                                              mmMMmm          MM      MMMM..  MMMM          MMMM      MMMM        MMMM      MMMMMMMMMMMM
echo.                                              mmMMmm      MM  MM        MM    MMMM          MMMM      MMMM        MMMM    MMMM      MMMM
echo.                                              ##MMMM  ::MMMM  MMMM@@MMMM@@    MMMM          MMMM      MMMM        MMMM    MMMM      MMMM@@

<nul set /p "=!RESET!"

REM --- COMPILACAO E EXECUCAO DA APLICACAO ---
cd /d "%~dp0"
if exist "%~dp0target\startup-ready.signal" del /q /f "%~dp0target\startup-ready.signal" >nul 2>&1

echo.
echo ============================================================
echo   Iniciando o sistema Nasser Esfiha... Aguarde um momento.
echo ============================================================
echo.

if exist "%~dp0target\etiqueta-app.jar" (
    echo Executando a aplicacao via JAR compilado...
    powershell.exe -NoProfile -WindowStyle Hidden -Command "Start-Process -FilePath 'javaw.exe' -ArgumentList '-jar','target\etiqueta-app.jar' -WorkingDirectory '%~dp0' -WindowStyle Hidden" >nul 2>&1
) else (
    echo Compilando e iniciando a aplicacao via Maven Wrapper...
    if exist "%~dp0mvnw.cmd" (
        call mvnw.cmd -q javafx:run
    ) else (
        call mvn -q javafx:run
    )
)

echo.
echo ============================================================
echo   Aplicacao iniciada com sucesso!
echo   Este console permanecera aberto para monitoramento.
echo ============================================================
echo.
pause