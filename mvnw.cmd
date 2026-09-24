@echo off
set "MAVEN_HOME=%~dp0.maven\apache-maven-3.9.9"
if exist "%MAVEN_HOME%\bin\mvn.cmd" (
	call "%MAVEN_HOME%\bin\mvn.cmd" %*
	exit /b %errorlevel%
)

where mvn >nul 2>&1
if not errorlevel 1 (
	call mvn %*
	exit /b %errorlevel%
)

echo ERRO: Maven nao foi encontrado. Execute setup.bat para instalar o Maven.
exit /b 1
