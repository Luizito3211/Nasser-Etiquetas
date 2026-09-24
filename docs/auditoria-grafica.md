# Relatório de Auditoria da Camada Gráfica — Nasser Etiquetas

**Data**: 24/09/2026  
**Branch de Trabalho**: `chore/limpeza-grafica`  
**Objetivo**: Mapear todos os elementos da camada gráfica (CSS, FXML, código Java, recursos visuais e dependências) para garantir que apenas o tema atual da marca (vermelho, branco e amarelo, com sol e camelo) e os componentes realmente utilizados permaneçam no projeto.

---

## 1. Como o Tema Atual é Aplicado (Diagnóstico)

A aplicação utiliza uma arquitetura híbrida de estilização:
1. **Base do Tema**: O framework **AtlantaFX** com o tema `CupertinoDark` é configurado na inicialização em [MainApp.java](file:///c:/Users/User/Documents/Etiqueta/src/main/java/com/nasser/etiqueta/MainApp.java#L41) via `Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet())`. Ele fornece os skins modernos dos controles JavaFX e a base das variáveis CSS (`-color-bg-default`, `-color-fg-default`, `-color-accent-emphasis`).
2. **Identidade Visual da Marca (Nasser Esfihas)**: O arquivo [src/main/resources/css/style.css](file:///c:/Users/User/Documents/Etiqueta/src/main/resources/css/style.css) sobrescreve a base do AtlantaFX com a paleta oficial da marca:
   - **Fundo / Base**: `#E2DDD6` (areia/bege claro característico)
   - **Destaque Primário**: `#C82B28` (vermelho Nasser)
   - **Destaque Secundário**: `#EAA225` / `#FFF0D4` (amarelo/dourado da marca para validade e alertas)
   - **Tipografia**: `#23201D` (marrom escuro/grafite) e `#6E665E` (texto atenuado)
3. **Ativos da Marca**: A marca d'água do Sol ([Sol.png](file:///c:/Users/User/Documents/Etiqueta/src/main/resources/img/Sol.png)) e o logotipo do Camelo ([Camelo.png](file:///c:/Users/User/Documents/Etiqueta/src/main/resources/img/Camelo.png)) são carregados via classpath no [MainController.java](file:///c:/Users/User/Documents/Etiqueta/src/main/java/com/nasser/etiqueta/gui/MainController.java#L97-L136).
4. **Resquícios de "darkBlackMatte"**: Não foi encontrado nenhum arquivo `.css` separado do tema antigo. O tema atual substituiu integralmente os estilos anteriores no próprio `style.css`. O que restou foram **comentários desatualizados no código Java** que ainda mencionavam *"Apple Dark Matte"* e *"ACCENT BLUE"*, bem como referências javadoc a ícones vetoriais Ikonli que foram substituídos por botões de texto.

---

## 2. Tabelas de Auditoria por Categoria

### 2.1 Dependências Maven (`pom.xml`)

| Item | Tipo | Status | Evidência |
| :--- | :--- | :--- | :--- |
| `org.openjfx:javafx-controls:21.0.5` | Módulo JavaFX | **USADO** | Componentes de UI fundamentais (`Button`, `TextField`, `ComboBox`, etc.) utilizados em todo o projeto. |
| `org.openjfx:javafx-fxml:21.0.5` | Módulo JavaFX | **USADO** | Utilizado para carregar [MainView.fxml](file:///c:/Users/User/Documents/Etiqueta/src/main/resources/fxml/MainView.fxml) através de `FXMLLoader` em [MainApp.java](file:///c:/Users/User/Documents/Etiqueta/src/main/java/com/nasser/etiqueta/MainApp.java#L60). |
| `org.openjfx:javafx-graphics:21.0.5` | Módulo JavaFX | **USADO** | `Stage`, `Scene`, `Color`, `Canvas`, renderização gráfica e carregamento de imagens. |
| `io.github.mkpaz:atlantafx-base:2.0.1` | Tema UI | **USADO** | Inicializado em [MainApp.java](file:///c:/Users/User/Documents/Etiqueta/src/main/java/com/nasser/etiqueta/MainApp.java#L41) (`CupertinoDark`) e estendido pelo CSS da marca. |
| `org.kordamp.ikonli:ikonli-javafx:12.3.1` | Biblioteca de Ícones | **OBSOLETO** | Nenhuma classe Ikonli (`FontIcon`, `Ikon`, etc.) é importada, referenciada ou instanciada em nenhum arquivo Java ou FXML. |
| `org.kordamp.ikonli:ikonli-feather-pack:12.3.1` | Pacote de Ícones | **OBSOLETO** | Nenhuma referência no código. Gera peso desnecessário no Fat JAR. |
| `org.kordamp.ikonli:ikonli-fontawesome5-pack:12.3.1` | Pacote de Ícones | **OBSOLETO** | Nenhuma referência no código. Gera peso desnecessário no Fat JAR. |
| `<ikonli.version>12.3.1</ikonli.version>` | Propriedade Maven | **OBSOLETO** | Propriedade em [pom.xml](file:///c:/Users/User/Documents/Etiqueta/pom.xml#L21) que se torna órfã com a remoção das dependências Ikonli. |
| `net.java.dev.jna:jna:5.14.0` | Biblioteca Nativa | **USADO** | Utilizada por [WindowsDarkThemeHelper.java](file:///c:/Users/User/Documents/Etiqueta/src/main/java/com/nasser/etiqueta/gui/WindowsDarkThemeHelper.java) para interagir com o DWM do Windows. |
| `net.java.dev.jna:jna-platform:5.14.0` | Biblioteca Nativa | **USADO** | Utilizada por [WindowsDarkThemeHelper.java](file:///c:/Users/User/Documents/Etiqueta/src/main/java/com/nasser/etiqueta/gui/WindowsDarkThemeHelper.java) (`HWND`, `HRESULT`). |

---

### 2.2 Folhas de Estilo (CSS) e Temas

| Item | Tipo | Status | Evidência |
| :--- | :--- | :--- | :--- |
| `src/main/resources/css/style.css` | Stylesheet | **USADO** | Único CSS do projeto. Contém o design system completo da marca (Nasser Esfihas Warm Classic). |
| Regras globais `.root` e variáveis de cor | CSS | **USADO** | Define fontes, `-fx-base: #E2DDD6`, `-fx-accent: #C82B28`, cores de texto e variáveis AtlantaFX. |
| Regras de layout (`.app-root`, `.sun-watermark`, etc.) | CSS | **USADO** | Mapeadas diretamente aos nós definidos em [MainView.fxml](file:///c:/Users/User/Documents/Etiqueta/src/main/resources/fxml/MainView.fxml). |
| Classes de componentes (`.category-card`, `.product-card`, etc.) | CSS | **USADO** | Utilizadas dinamicamente em [ProductCardComponent.java](file:///c:/Users/User/Documents/Etiqueta/src/main/java/com/nasser/etiqueta/gui/ProductCardComponent.java) e [MainController.java](file:///c:/Users/User/Documents/Etiqueta/src/main/java/com/nasser/etiqueta/gui/MainController.java). |
| Classes de botões (`.btn-primary`, `.btn-success`, etc.) | CSS | **USADO** | Utilizadas em botões de ação de impressão, salvamento e exclusão em toda a interface. |
| Estilos de diálogos (`.date-selection-dialog`, etc.) | CSS | **USADO** | Utilizadas nos modais [DateSelectionDialog.java](file:///c:/Users/User/Documents/Etiqueta/src/main/java/com/nasser/etiqueta/gui/DateSelectionDialog.java), [ProdutoDialogFX.java](file:///c:/Users/User/Documents/Etiqueta/src/main/java/com/nasser/etiqueta/gui/ProdutoDialogFX.java), etc. |

---

### 2.3 Código Java Gráfico

| Item | Tipo | Status | Evidência |
| :--- | :--- | :--- | :--- |
| `com.nasser.etiqueta.MainApp` | Classe Java | **USADO** | Ponto de entrada JavaFX (`Application`), configura cena, tema e exibe janela principal. |
| `com.nasser.etiqueta.Launcher` | Classe Java | **USADO** | Ponto de entrada configurado no [pom.xml](file:///c:/Users/User/Documents/Etiqueta/pom.xml) para execução a partir de Fat JARs. |
| `com.nasser.etiqueta.gui.MainController` | Classe Java | **USADO** | Controller MVC do FXML principal. |
| `com.nasser.etiqueta.gui.ProductCardComponent` | Classe Java | **USADO** | Componente visual dos cards de produto com drag-and-drop e controle de quantidade. |
| `com.nasser.etiqueta.gui.LoginDialogFX` | Classe Java | **USADO** | Diálogo modal de login e identificação do operador. |
| `com.nasser.etiqueta.gui.ProdutoDialogFX` | Classe Java | **USADO** | Diálogo modal para criação e edição de produtos. |
| `com.nasser.etiqueta.gui.DateSelectionDialog` | Classe Java | **USADO** | Diálogo modal de agendamento de data de fabricação para impressão. |
| `com.nasser.etiqueta.gui.LayoutEditorDialogFX` | Classe Java | **USADO** | Editor interativo de layout da etiqueta Elgin L42 Pro com visualização em Canvas. |
| `com.nasser.etiqueta.gui.WindowsDarkThemeHelper`| Classe Java | **USADO** | Integração nativa via JNA para título escuro da janela no Windows 10/11. |
| `com.nasser.etiqueta.gui.WindowUtils` | Classe Java | **USADO** | Configura o ícone da marca em todos os estágios (`stage.getIcons()`). |
| Imports não utilizados em `MainController.java` | Código Java | **OBSOLETO** | `java.time.LocalDate`, `java.time.LocalDateTime`, `javafx.collections.FXCollections`. |
| Imports não utilizados em `LayoutEditorDialogFX.java` | Código Java | **OBSOLETO** | `java.io.FileInputStream`, `javafx.scene.text.FontWeight`. |
| Comentários que citam *"Apple Dark Matte"* e *"ACCENT BLUE"* | Código/FXML | **OBSOLETO** | Javadocs e comentários remanescentes da refatoração anterior que conflitam com o nome do tema da marca. |
| `com.nasser.etiqueta.service.ElginPrinterService` (`java.awt.*`) | Serviço | **USADO (Impressão)** | O uso de AWT (`Graphics2D`, `BufferedImage`) é estritamente de impressão (rasterização off-screen para comando ZPL `^GF`), e não interface gráfica. Nenhum Swing existe no código. |

---

### 2.4 Arquivos FXML

| Item | Tipo | Status | Evidência |
| :--- | :--- | :--- | :--- |
| `src/main/resources/fxml/MainView.fxml` | FXML | **USADO** | Tela principal da aplicação. Todos os seus 22 nós identificados por `fx:id` e métodos de evento `onAction` estão ativos e conectados a [MainController.java](file:///c:/Users/User/Documents/Etiqueta/src/main/java/com/nasser/etiqueta/gui/MainController.java). |

---

### 2.5 Recursos Visuais (Imagens e Ícones)

| Item | Tipo | Status | Evidência |
| :--- | :--- | :--- | :--- |
| `src/main/resources/img/LogoStage.Jpeg` | Imagem (640x640) | **USADO** | Carregada por [WindowUtils.java](file:///c:/Users/User/Documents/Etiqueta/src/main/java/com/nasser/etiqueta/gui/WindowUtils.java) para o ícone de todas as janelas do aplicativo. |
| `src/main/resources/img/Camelo.png` | Imagem (360x360) | **USADO** | Carregada por [MainController.java](file:///c:/Users/User/Documents/Etiqueta/src/main/java/com/nasser/etiqueta/gui/MainController.java#L98) para o cabeçalho superior. |
| `src/main/resources/img/Sol.png` | Imagem (350x350) | **USADO** | Carregada por [MainController.java](file:///c:/Users/User/Documents/Etiqueta/src/main/java/com/nasser/etiqueta/gui/MainController.java#L99) como marca d'água semitransparente no fundo da tela. |
| `packaging/icone.ico` | Ícone Multi-resolução | **USADO** | Utilizado pelo `jpackage` em [packaging/build.ps1](file:///c:/Users/User/Documents/Etiqueta/packaging/build.ps1) para criar o `.exe` nativo do Windows. |
| `packaging/generate_icon.py` | Script Python | **USADO** | Script utilitário para regeneração do `packaging/icone.ico` a partir dos recursos do projeto. |

---

### 2.6 Outros Arquivos no Repositório

| Item | Tipo | Status | Evidência |
| :--- | :--- | :--- | :--- |
| `com.nasser.etiqueta.Main` | Classe Java | **INCERTO** | Classe idêntica a `Launcher.java`, delegando para `MainApp.main(args)`. Foi substituída pelo `Launcher` na etapa anterior. O `pom.xml` e scripts usam `Launcher`. |
| `MANIFEST.MF` (na raiz do projeto) | Arquivo de Manifesto | **INCERTO** | Aponta para `com.nasser.etiqueta.Main`. O Maven gera automaticamente seu próprio manifest dentro do JAR (`target/`), tornando este arquivo da raiz desnecessário para o build atual. |
| Pasta raiz `img/` (`Camelo.png`, `LogoStage.jpg`, `Sol.png`) | Diretório de Imagens | **INCERTO** | Arquivos fora da pasta `src/main/resources/img/`. São duplicatas criadas na época do Swing v1 quando o app lia imagens do diretório de trabalho local. Não são empacotadas no JAR do Maven. |
| Pasta raiz `game-hub/` | Submódulo / Diretório | **INCERTO** | Gitlink/pasta contendo jogos web em JavaScript (`snake.js`, `clicker.js`, `index.html`) adicionada acidentalmente no commit v2.0 (`e5f4419`). Não tem nenhuma relação com o sistema Nasser Etiquetas. |
| `legacy/Nasser Etiquetas.bat` | Script de inicialização | **USADO (Histórico)** | Preservado intencionalmente na pasta `legacy/` para rastreabilidade histórica. |

---

## 3. Decisões que Precisam de Você (Itens INCERTOS)

Antes de qualquer remoção destes itens, solicitamos sua decisão formal:

1. **`src/main/java/com/nasser/etiqueta/Main.java` e `MANIFEST.MF` da raiz**:
   - *Contexto*: A classe [Launcher.java](file:///c:/Users/User/Documents/Etiqueta/src/main/java/com/nasser/etiqueta/Launcher.java) é o ponto de entrada oficial configurado no `pom.xml` e no `jpackage`. A classe [Main.java](file:///c:/Users/User/Documents/Etiqueta/src/main/java/com/nasser/etiqueta/Main.java) e o `MANIFEST.MF` da raiz são cópias antigas.
   - *Pergunta*: Podemos remover `Main.java` e `MANIFEST.MF` da raiz para evitar duplicidade de launchers?

2. **Diretório raiz `img/` (`img/Camelo.png`, `img/LogoStage.jpg`, `img/Sol.png`)**:
   - *Contexto*: Todas as imagens da interface são carregadas a partir de `src/main/resources/img/` via classpath dentro do JAR. A pasta `img/` na raiz do projeto não faz parte do build do Maven nem é distribuída pelo `jpackage`.
   - *Pergunta*: Podemos remover a pasta `img/` da raiz do repositório?

3. **Diretório raiz `game-hub/`**:
   - *Contexto*: Trata-se de um projeto web de minijogos (Snake, Clicker, Parkour) inserido por engano no repositório no commit da versão 2.0. Não possui nenhuma chamada ou vínculo com o código Java.
   - *Pergunta*: Podemos remover o diretório `game-hub/` do repositório?
