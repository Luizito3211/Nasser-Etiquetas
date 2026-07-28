@echo off
:: Define o título da janela do terminal
title Compilador e Executador - Sistema de Etiquetas

:: Garante que o terminal use UTF-8 para não desconfigurar acentos
chcp 65001 > nul

echo ===================================================
echo   COMPILANDO O SISTEMA DE ETIQUETAS ESIFHARIA...
echo ===================================================
echo.

:: Cria a pasta bin se ela ainda nao existir
if not exist "bin" mkdir bin

:: Compila todos os arquivos Java para a pasta bin
javac -d bin -encoding UTF-8 src/com/nasser/etiqueta/Main.java src/com/nasser/etiqueta/model/*.java src/com/nasser/etiqueta/service/*.java src/com/nasser/etiqueta/gui/*.java

:: Verifica se a compilação teve sucesso (ERRORLEVEL == 0)
if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCESSO] Compilado sem erros!
    echo [INFO] Iniciando a aplicação...
    echo ===================================================
    echo.
    
    :: Executa a classe Principal
    java -cp bin com.nasser.etiqueta.Main
) else (
    echo.
    echo ===================================================
    echo [ERRO] Ocorreu um erro durante a compilacao.
    echo Verifique o codigo acima para corrigir o problema.
    echo ===================================================
    pause
)