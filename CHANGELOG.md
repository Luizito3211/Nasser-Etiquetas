# Changelog

Todas as alterações notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [2.1.0] - 2026-09-24

### Adicionado
- Migração completa da interface para JavaFX 21 com AtlantaFX (Cupertino Dark) e identidade visual da marca Nasser Esfihas (vermelho, branco e amarelo).
- Ícone nativo de alta resolução (`packaging/icone.ico`) com resoluções de 16x16 até 256x256.
- Suporte a empacotamento autocontido para Windows com `jpackage` gerando ZIP portátil e instalador `.exe` (WiX Toolset).
- Pipeline de integração contínua (CI/CD) no GitHub Actions para geração automática de releases em tags git.
- Isolamento dos dados persistentes em `%APPDATA%\Nasser Etiquetas\config`, com migração automática transparente de versões anteriores.
- Sistema de logs rotativos baseado em `java.util.logging` gravado em `%APPDATA%\Nasser Etiquetas\logs`.
- Tratador global de exceções não capturadas com diálogo amigável ao operador da loja.
- Suporte completo a templates visuais e renderização gráfica ZPL para impressão térmica Elgin L42 Pro.

### Modificado
- Ponto de entrada ajustado para classe `Launcher` independente, garantindo compatibilidade com Fat JARs empacotados pelo `maven-shade-plugin`.
- Atualizado serviço de persistência para gerenciar caminhos via `AppEnvironment`.

### Removido
- Removidos arquivos obsoletos da fase Swing/protótipo (`Main.java`, `MANIFEST.MF` da raiz, pasta `img/` da raiz e diretório `game-hub/`).
- Removidas dependências de ícones não utilizadas (`ikonli`).
- Movido script de execução em lote antigo para `legacy/`.
