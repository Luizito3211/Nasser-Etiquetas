#Requires -Version 5.1
<#
.SYNOPSIS
    Script de build e empacotamento do Nasser Etiquetas para Windows.
.DESCRIPTION
    1. Lê a versão do pom.xml
    2. Executa build e Fat JAR via mvnw.cmd clean package
    3. Executa jpackage (JDK 21) para gerar app-image e compactar em zip portátil
    4. Gera instalador .exe se o WiX Toolset estiver presente
    5. Armazena os artefatos finais em dist/
#>

[CmdletBinding()]
param(
    [switch]$SkipMvnBuild,
    [switch]$SkipPortableZip,
    [switch]$SkipInstaller
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = (Resolve-Path "$ScriptDir\..").Path
Set-Location $RepoRoot

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "    Nasser Etiquetas - Empacotamento Windows (jpackage)   " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Validação de pré-requisitos
$mvnw = Join-Path $RepoRoot "mvnw.cmd"
if (-not (Test-Path $mvnw)) {
    throw "mvnw.cmd não foi encontrado em $RepoRoot."
}

# Verifica se jpackage está no PATH ou em JAVA_HOME
$jpackageCmd = "jpackage"
if (-not (Get-Command "jpackage" -ErrorAction SilentlyContinue)) {
    if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\jpackage.exe")) {
        $jpackageCmd = "$env:JAVA_HOME\bin\jpackage.exe"
    } else {
        throw "jpackage não foi encontrado. Certifique-se de ter o JDK 21+ instalado e configurado no PATH ou JAVA_HOME."
    }
}
Write-Host "[OK] jpackage detectado: $jpackageCmd" -ForegroundColor Green

# 2. Leitura da versão a partir do pom.xml (fonte única de verdade)
$pomFile = Join-Path $RepoRoot "pom.xml"
if (-not (Test-Path $pomFile)) {
    throw "pom.xml não encontrado em $RepoRoot."
}

[xml]$pomXml = Get-Content -Path $pomFile -Raw
$pomVersion = $pomXml.project.version
if ([string]::IsNullOrWhiteSpace($pomVersion)) {
    throw "Não foi possível extrair a versão do pom.xml."
}
Write-Host "[OK] Versão detectada no pom.xml: $pomVersion" -ForegroundColor Green

# Versão formatada para compatibilidade com jpackage (major.minor.build)
$jpackageVersion = ($pomVersion -split '-')[0]
# Garante pelo menos 3 dígitos numéricos se possível (ex: 1.0 -> 1.0.0)
$parts = $jpackageVersion.Split('.')
if ($parts.Length -eq 1) {
    $jpackageVersion = "$($parts[0]).0.0"
} elseif ($parts.Length -eq 2) {
    $jpackageVersion = "$($parts[0]).$($parts[1]).0"
}

# 3. Compilação e empacotamento Maven
if (-not $SkipMvnBuild) {
    Write-Host "`n>>> Executando build via Maven Wrapper (mvnw.cmd clean package)..." -ForegroundColor Yellow
    $mvnProcess = Start-Process -FilePath $mvnw -ArgumentList "clean", "package" -WorkingDirectory $RepoRoot -NoNewWindow -PassThru -Wait
    if ($mvnProcess.ExitCode -ne 0) {
        throw "Falha no build Maven (código de saída: $($mvnProcess.ExitCode))."
    }
    Write-Host "[OK] Build Maven concluído com sucesso." -ForegroundColor Green
}

# 4. Localização do Fat JAR gerado
$jarCandidate = Join-Path $RepoRoot "target\nasser-etiquetas-$pomVersion.jar"
if (-not (Test-Path $jarCandidate)) {
    # Tenta localizar qualquer jar com prefixo nasser-etiquetas
    $foundJars = Get-ChildItem -Path (Join-Path $RepoRoot "target") -Filter "nasser-etiquetas*.jar" | Where-Object { $_.Name -notlike "original-*" }
    if ($foundJars) {
        $jarCandidate = $foundJars[0].FullName
    } else {
        throw "JAR da aplicação não encontrado em target/."
    }
}
Write-Host "[OK] JAR identificado: $jarCandidate" -ForegroundColor Green

# 5. Preparação dos diretórios de entrada e saída
$distDir = Join-Path $RepoRoot "dist"
if (-not (Test-Path $distDir)) {
    New-Item -ItemType Directory -Path $distDir -Force | Out-Null
}

$inputDir = Join-Path $RepoRoot "target\jpackage-input"
if (Test-Path $inputDir) {
    Remove-Item -Recurse -Force $inputDir | Out-Null
}
New-Item -ItemType Directory -Path $inputDir -Force | Out-Null

$mainJarName = "nasser-etiquetas.jar"
Copy-Item -Path $jarCandidate -Destination (Join-Path $inputDir $mainJarName) -Force

$iconPath = Join-Path $RepoRoot "packaging\icone.ico"
if (-not (Test-Path $iconPath)) {
    throw "Arquivo de ícone não encontrado em $iconPath."
}

# 6. Geração do Artefato Portátil (app-image + zip)
if (-not $SkipPortableZip) {
    Write-Host "`n>>> Gerando imagem de aplicativo portátil (--type app-image)..." -ForegroundColor Yellow
    $tempAppDir = Join-Path $distDir "temp-app-image"
    if (Test-Path $tempAppDir) {
        Remove-Item -Recurse -Force $tempAppDir | Out-Null
    }
    New-Item -ItemType Directory -Path $tempAppDir -Force | Out-Null

    $appImageArgs = @(
        "--type", "app-image",
        "--name", "Nasser Etiquetas",
        "--app-version", $jpackageVersion,
        "--vendor", "Nasser Esfihas",
        "--icon", $iconPath,
        "--input", $inputDir,
        "--main-jar", $mainJarName,
        "--main-class", "com.nasser.etiqueta.Launcher",
        "--java-options", "-Dfile.encoding=UTF-8",
        "--dest", $tempAppDir
    )

    & $jpackageCmd @appImageArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Erro ao executar jpackage para app-image (código: $LASTEXITCODE)."
    }

    $portableZipName = "NasserEtiquetas-$pomVersion-windows-portable.zip"
    $portableZipPath = Join-Path $distDir $portableZipName
    if (Test-Path $portableZipPath) {
        Remove-Item -Force $portableZipPath | Out-Null
    }

    Write-Host "Compactando imagem portátil em $portableZipName..." -ForegroundColor Yellow
    Compress-Archive -Path (Join-Path $tempAppDir "Nasser Etiquetas") -DestinationPath $portableZipPath -Force
    Remove-Item -Recurse -Force $tempAppDir | Out-Null

    Write-Host "[OK] Artefato portátil gerado: $portableZipPath" -ForegroundColor Green
}

# 7. Geração do Instalador Windows (.exe) - Requer WiX Toolset
if (-not $SkipInstaller) {
    Write-Host "`n>>> Verificando disponibilidade do WiX Toolset para geração do instalador .exe..." -ForegroundColor Yellow

    # Adiciona WIX\bin ao PATH atual se configurado na variável WIX
    if ($env:WIX -and (Test-Path "$env:WIX\bin")) {
        $env:PATH = "$env:WIX\bin;$env:PATH"
    }

    $candleFound = [bool](Get-Command "candle.exe" -ErrorAction SilentlyContinue)
    $lightFound  = [bool](Get-Command "light.exe" -ErrorAction SilentlyContinue)

    if ($candleFound -and $lightFound) {
        Write-Host "[OK] WiX Toolset detectado (candle e light). Gerando instalador .exe..." -ForegroundColor Green
        
        $exeArgs = @(
            "--type", "exe",
            "--name", "Nasser Etiquetas",
            "--app-version", $jpackageVersion,
            "--vendor", "Nasser Esfihas",
            "--icon", $iconPath,
            "--input", $inputDir,
            "--main-jar", $mainJarName,
            "--main-class", "com.nasser.etiqueta.Launcher",
            "--java-options", "-Dfile.encoding=UTF-8",
            "--win-shortcut",
            "--win-menu",
            "--win-dir-chooser",
            "--win-per-user-install",
            "--dest", $distDir
        )

        & $jpackageCmd @exeArgs
        if ($LASTEXITCODE -ne 0) {
            throw "Erro ao executar jpackage para instalador exe (código: $LASTEXITCODE)."
        }

        # Localiza o .exe gerado pelo jpackage (nome padrão costuma ser "Nasser Etiquetas-<versao>.exe")
        $generatedExe = Get-ChildItem -Path $distDir -Filter "Nasser Etiquetas*.exe" | Select-Object -First 1
        if ($generatedExe) {
            $finalInstallerName = "NasserEtiquetas-$pomVersion-windows-installer.exe"
            $finalInstallerPath = Join-Path $distDir $finalInstallerName
            if (Test-Path $finalInstallerPath) {
                Remove-Item -Force $finalInstallerPath | Out-Null
            }
            Rename-Item -Path $generatedExe.FullName -NewName $finalInstallerName -Force
            Write-Host "[OK] Instalador gerado: $finalInstallerPath" -ForegroundColor Green
        }
    } else {
        Write-Host "[AVISO] WiX Toolset (candle.exe / light.exe) não foi detectado no sistema." -ForegroundColor Yellow
        Write-Host "         O instalador .exe não será gerado localmente nesta execução." -ForegroundColor Yellow
        Write-Host "         (No GitHub Actions, o WiX está pré-instalado e o .exe será gerado na Release)." -ForegroundColor Cyan
    }
}

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "    Empacotamento concluído! Conteúdo de dist/:           " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Get-ChildItem -Path $distDir | Select-Object Name, Length, LastWriteTime | Format-Table -AutoSize
