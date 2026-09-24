# Nasser Etiquetas 🏷️

Sistema desktop em Java para automação e gestão da impressão de etiquetas de validade para a **Nasser Esfihas**, integrado a impressoras térmicas (ex: Elgin L42 Pro). Desenvolvido com **Java 21**, **JavaFX 21** e tema **AtlantaFX (Cupertino Dark)**.

<!-- TODO: adicionar screenshots em docs/img/ -->

---

## 🚀 Para Usuários (Lojas / Operadores)

### Como Baixar e Usar

1. Acesse a aba [**Releases**](https://github.com/Luizito3211/Nasser-Etiquetas/releases) deste repositório.
2. Na versão mais recente, clique na seção **Assets** e baixe uma das duas opções:
   - **Instalador Automático (`NasserEtiquetas-<versao>-windows-installer.exe`)**:
     - Dê um duplo clique no arquivo baixado e siga o assistente.
     - O instalador criará atalhos na Área de Trabalho e no Menu Iniciar automaticamente.
   - **Versão Portátil (`NasserEtiquetas-<versao>-windows-portable.zip`)**:
     - Baixe e extraia a pasta em qualquer diretório (ex: `C:\NasserEtiquetas` ou Área de Trabalho).
     - Abra a pasta extraída e dê um duplo clique em **`Nasser Etiquetas.exe`**.

### Requisitos do Sistema
- **Nenhum pré-requisito!** Não é necessário instalar Java, Maven, configurar variáveis de ambiente (`PATH`) ou ter permissões de administrador. O programa já inclui seu próprio runtime Java isolado, ícone oficial e todas as dependências embutidas.

### 📁 Local dos Dados e Logs

- **Configurações e Dados:** `%APPDATA%\Nasser Etiquetas\config`
  - Armazena a lista de produtos (`produtos.txt`), layout ZPL (`layout.zpl`), preferências gerais (`app.properties`), configuração visual (`layout_elements.txt`) e presets de impressão (`presets.txt`).
  - Em outros sistemas operacionais ou quando `%APPDATA%` não estiver definido, utiliza o diretório `~/.nasser-etiquetas/config`.
- **Logs da Aplicação:** `%APPDATA%\Nasser Etiquetas\logs`
  - Arquivos rotativos de log (`nasser-etiquetas.0.log`), limitados a 2 MB por arquivo e mantendo até 5 arquivos históricos para diagnóstico de eventuais problemas de impressão ou falhas inesperadas.

---

## ⚠️ Atenção: Por que não baixar o zip do código-fonte?

Na página de Releases, o GitHub sempre exibe links automáticos chamados **"Source code (zip)"** e **"Source code (tar.gz)"**.
- **Não baixe esses arquivos** se o seu objetivo for apenas usar o sistema.
- Eles contêm apenas o código-fonte em desenvolvimento (arquivos `.java`, `.xml`), que não funcionam sem ambiente de compilação configurado.
- Para usar o programa pronto, baixe sempre os arquivos da seção **Assets** descritos acima (`.exe` ou `.zip` portátil).

---

## 🛠️ Para Desenvolvedores

### Pré-requisitos
- **JDK 21** (recomendado: [Eclipse Adoptium Temurin 21](https://adoptium.net/temurin/releases/?version=21))
- Git
- *(Opcional para instalador .exe local)*: [WiX Toolset v3.11](https://wixtoolset.org/releases/) (no GitHub Actions o WiX já vem pré-instalado).

### Estrutura e Ponto de Entrada
- O ponto de entrada principal do JavaFX é a classe `com.nasser.etiqueta.MainApp`.
- Para execução a partir de Fat JARs (empacotados pelo `maven-shade-plugin`), foi criada a classe `com.nasser.etiqueta.Launcher` (que não estende `Application`), prevenindo o erro *"JavaFX runtime components are missing"*.
- A versão única e oficial do sistema é definida na tag `<version>` do `pom.xml`.

### Como Executar em Desenvolvimento
Usando o Maven Wrapper embutido no projeto:

```powershell
# Execução direta via JavaFX plugin
.\mvnw.cmd javafx:run
```

Ou compilar o Fat JAR com dependências integradas:
```powershell
.\mvnw.cmd clean package
java -jar target/nasser-etiquetas-1.0.0.jar
```

### Como Gerar o Pacote Localmente (Windows)
Para gerar os artefatos de distribuição idênticos aos de produção:

```cmd
packaging\build.bat
```
*(ou via PowerShell: `powershell -ExecutionPolicy Bypass -File packaging/build.ps1`)*

O script realiza automaticamente:
1. Leitura da versão a partir do `pom.xml`.
2. Compilação e empacotamento do Fat JAR via `mvnw.cmd clean package`.
3. Execução do `jpackage` para gerar a imagem de aplicativo (`app-image`) com o runtime Java 21 isolado e ícone oficial.
4. Compressão da pasta em `dist/NasserEtiquetas-<versao>-windows-portable.zip`.
5. Detecção do WiX Toolset: se presente, gera o instalador `dist/NasserEtiquetas-<versao>-windows-installer.exe`. Se não estiver instalado na máquina local, pula essa etapa sem erros informando o desenvolvedor.

### Ícones da Aplicação
- O ícone nativo do Windows está localizado em `packaging/icone.ico` e contém as resoluções **16, 32, 48, 64, 128 e 256 px**.
- O script utilitário `packaging/generate_icon.py` documenta e permite regenerar o `.ico` a qualquer momento a partir da imagem original em `src/main/resources/img/LogoStage.Jpeg`.
- Na interface gráfica, todos os diálogos e janelas utilizam `WindowUtils.applyAppIcon(stage)` para carregar a identidade visual da Nasser Esfihas.

---

## 🏷️ Como Lançar uma Nova Versão (CI/CD Automático)

O repositório possui uma pipeline automatizada no GitHub Actions (`.github/workflows/release.yml`) que compila, testa a versão e gera a Release com os artefatos anexados.

Para lançar uma nova versão:

1. Atualize a `<version>` no [pom.xml](file:///c:/Users/User/Documents/Etiqueta/pom.xml) (exemplo: de `1.0.0` para `2.1.0`).
2. Comite a alteração:
   ```bash
   git commit -am "chore(release): v2.1.0"
   ```
3. Crie a tag com a mesma versão e envie para o GitHub:
   ```bash
   git tag v2.1.0 && git push origin main --tags
   ```

O GitHub Actions irá:
- Validar se a tag corresponde estritamente à `<version>` do `pom.xml` (falhando caso haja divergência).
- Empacotar o Fat JAR com Java 21, JavaFX e AtlantaFX.
- Gerar o zip portátil (`.zip`) e o instalador Windows com atalhos de desktop e menu (`.exe`).
- Criar a Release no GitHub e anexar os binários com changelog automático.

---

## 📜 Histórico e Script Legado

Anteriormente, o sistema era executado por um arquivo `.bat` localizado na raiz do projeto (`Nasser Etiquetas.bat`), que tentava compilar em tempo de execução ou requeria Java instalado localmente no computador da loja. 

Esse script foi movido para a pasta `legacy/Nasser Etiquetas.bat` para preservação do histórico de desenvolvimento. Ele foi substituído pela cadeia de build moderna baseada em `jpackage`, que empacota o runtime do Java de forma 100% autocontida e dispensa qualquer configuração manual nas máquinas das lojas.
