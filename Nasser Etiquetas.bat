@echo off
setlocal enabledelayedexpansion

:: ==================================================
:: PARTE 1: INICIALIZAÇÃO SILENCIOSA E TELA CHEIA
:: ==================================================

:: Tenta maximizar a janela do prompt de comando ao iniciar
if not "%1"=="max" (
    start "" /max "%~0" max
    exit /b
)

:: Tenta forçar o modo de tela cheia real (ALT+ENTER)
powershell -NoProfile -Command "$wshell = New-Object -ComObject WScript.Shell; $wshell.SendKeys('{ALT}{ENTER}')" >nul 2>&1

:: Limpa a tela imediatamente
cls

:: UTF-8 para garantir a exibição correta
chcp 65001 >nul

:: Obtém o caractere ESC para aplicação das cores ANSI
for /f "delims=" %%A in ('echo prompt $E^| cmd') do set "ESC=%%A"

:: Define as cores
set "BRANCO=!ESC![97m"
set "VERMELHO=!ESC![91m"
set "RESET=!ESC![0m"

:: ==================================================
:: PARTE 2: EXIBIÇÃO DA IMAGEM COLORIDA (Branco/Vermelho)
:: ==================================================

:: Aplica cor BRANCA (Camelo)
<nul set /p "=!BRANCO!"

:: Arte ASCII - Camelo (Branco)
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
echo.                                                                  ####    ####            ##      ####                                  
echo.                                                                  ##      ####          ####        ##                                  
echo.                                                                  ##      --##          ##          ##                                  
echo.                                                                  ##        ##          ##          ##MM                                
echo.                                                                  ##        mm##        ##            ##                                
echo.                                                                @@##          ##        ####            ##                              
echo.                                                          ##############################################################            

:: Aplica cor VERMELHA (Nasser Esfiha)
<nul set /p "=!VERMELHO!"

:: Arte ASCII - Nasser Esfiha (Vermelho)
echo.                                          MMMM@@        MM##                                                        
echo.                                          MMMMMM        MM        MMMMMM        ##MMMMMMMM--    MMMMMMMMMM  @@MMMMMMMMMM++  MMMMMMMMCCCC  
echo.                                          MMMMMMMM      MM        MMMMMM        MM      MM    MMMM    ::MM    MMMM    ::##    MMMM  @@MMMM  
echo.                                          MMMMMMMM@@    MM        MM@@MM      ::MMMM    ++    MMMMmm    ++    MMMM            MMMM    MMMM  
echo.                                          MMMM  MMMM..  MM      MM@@  MMMM      MMMMMM@@      ##MMMMMM        MMMM    MM      MMMM    MMMM  
echo.                              ++MM        MMMM  --MMMM  MM      MM    MMMM        MMMMMMMM      ##MMMMMMMM    MMMMMMMMMM      MMMMMMMMMM  
echo.                            --MM          MMMM    MMMMMMMM      MM@@@@MMMM            MMMMMM        mmMMMM    MMMM    MM      MMMM@@MM##  
echo.                            MMMM          MMMM      MMMMMM    MMMM------MMMM    ##      MMMM  MM        MM--  MMMM            MMMM  MMMM  
echo.                            --MMMM        MMMM        MMMM    MM##      MMMM    MM      MMMM  MMMM    ..MM    MMMM      MM    MMMM  --MMMM  
echo.                              MMMM      ++MMMM        ##MM  ##MMMM      MMMMMM  MMMMMMMMMM    mmMMMMMMMM    @@MMMMMMMMMMMM  @@MMMM    MMMM@@  
echo.                          MMMM++MMMM                                                                                                      MM::@@MM## 
echo.                        MMMMMMMMMMMM##                                                                                                  MMMMMMMMMMMMMM 
echo.                          ++++    ##MM        MMMMMMMMMMMM      ++MMMMMM    MMMMMMMMMMMM  MMMMMMMM  MMMMMMMM    MMMMMMMM      MMMMMM          ..MM      ++  
echo.                              ##MMMMMM..      MMMMmm  ..MM    @@MM    MMMM    MMMM    MM++  MMMM      MMMM        MMMM        MMMMMM          MMMMMMMM    
echo.                              MM    mmMM      mmMMmm          MMMM      @@    MMMM      @@  MMMM      MMMM        MMMM        MM  MM--        MM      MM  
echo.                              MM  MM  MM      mmMMmm    ++    MMMMMMMM        MMMM          MMMM      MMMM        MMMM        MM  MMMM        MM  MM  MM  
echo.                              MMMM@@  ##      mmMMMMMMMM++      MMMMMMMM      MMMM@@MMMM    MMMM      MMMMMMMMMMMMMMMM        MMMM  MMMM        ##  MMMM@@  
echo.                                              mmMMmm    ++        ::MMMMMM    MMMM  ..MM    MMMM      MMMM        MMMM      MM    --MM@@        
echo.                                              mmMMmm          MM      MMMM..  MMMM          MMMM      MMMM        MMMM      MMMMMMMMMMMM        
echo.                                              mmMMmm      MM  MM        MM    MMMM          MMMM      MMMM        MMMM    MMMM      MMMM        
echo.                                              ##MMMM  ::MMMM  MMMM@@MMMM@@    MMMM          MMMM      MMMM        MMMM    MMMM      MMMM@@      

:: Reseta a cor para o padrão do terminal
<nul set /p "=!RESET!"

:: ==================================================
:: PARTE 3: COMPILAÇÃO E EXECUÇÃO 100% SILENCIOSA
:: ==================================================

:: 3.1: Configuração de Pastas
if not exist "bin" mkdir bin

:: 3.2: Compilação Silenciosa (Saída e Erros Redirecionados)
javac -d bin -encoding UTF-8 src/com/nasser/etiqueta/Main.java src/com/nasser/etiqueta/model/*.java src/com/nasser/etiqueta/service/*.java src/com/nasser/etiqueta/gui/*.java >nul 2>&1

:: 3.3: Execução Silenciosa
start /b javaw -cp bin com.nasser.etiqueta.Main >nul 2>&1

:: Pequeno delay antes de fechar o console
timeout /t 2 /nobreak >nul

:: Fecha a janela do terminal
exit