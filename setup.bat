@echo off
setlocal EnableExtensions
cd /d "%~dp0"
title Nasser Etiquetas - Configuracao

echo Verificando Java 21...
where java >nul 2>&1
if errorlevel 1 (
    where winget >nul 2>&1
    if errorlevel 1 (
        echo ERRO: Java 21 nao foi encontrado e o winget nao esta disponivel.
        echo Instale um JDK 21 e execute este script novamente.
        exit /b 1
    )
    echo Java nao encontrado. Instalando Eclipse Temurin JDK 21 via winget...
    winget install --id EclipseAdoptium.Temurin.21.JDK --exact --accept-source-agreements --accept-package-agreements
    if errorlevel 1 (
        echo ERRO: A instalacao do JDK 21 falhou.
        exit /b 1
    )
    echo Feche e abra este script novamente para atualizar o PATH.
    exit /b 0
)

java -version 2>&1 | findstr /c:"21." >nul
if errorlevel 1 (
    echo ERRO: Este projeto exige Java 21.
    java -version
    exit /b 1
)

for /f "tokens=3" %%V in ('java -version 2^>^&1 ^| findstr /i "version"') do set "JAVA_VERSION=%%~V"
echo Java detectado: %JAVA_VERSION%

if exist "%~dp0.maven\apache-maven-3.9.9\bin\mvn.cmd" (
    set "MVN=mvnw.cmd"
) else (
    where mvn >nul 2>&1
    if errorlevel 1 (
        where winget >nul 2>&1
        if errorlevel 1 (
            echo ERRO: Maven nao foi encontrado e o winget nao esta disponivel.
            echo Instale o Maven e execute este script novamente.
            exit /b 1
        )
        echo Maven nao encontrado. Instalando Apache Maven via winget...
        winget install --id Apache.Maven --exact --accept-source-agreements --accept-package-agreements
        if errorlevel 1 (
            echo ERRO: A instalacao do Maven falhou.
            exit /b 1
        )
        echo Feche e abra este script novamente para atualizar o PATH.
        exit /b 0
    )
    set "MVN=mvn"
)

echo Baixando dependencias Maven...
call "%MVN%" -q dependency:go-offline
if errorlevel 1 (
    echo ERRO: Nao foi possivel baixar as dependencias Maven.
    exit /b 1
)

echo Compilando a aplicacao...
call "%MVN%" -q clean package
if errorlevel 1 (
    echo ERRO: O build falhou.
    exit /b 1
)

echo.
echo Setup concluido com sucesso.
echo Execute iniciar.bat para abrir o aplicativo.
exit /b 0
