package com.nasser.etiqueta.gui;

import com.nasser.etiqueta.model.*;
import com.nasser.etiqueta.service.ElginPrinterService;
import com.nasser.etiqueta.service.PersistenceService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller principal da interface JavaFX (MVC).
 * Conecta toda a lógica de negócio, modelos, persistência e serviços de impressão
 * ao design da marca Nasser Esfihas.
 */
public class MainController {

    @FXML private StackPane mainStackPane;
    @FXML private TextField tfSearch;
    @FXML private StackPane backgroundPane;
    @FXML private BorderPane mainLayout;
    @FXML private VBox headerPane;
    @FXML private Label lblOperador;
    @FXML private Button btnTrocarOperador;
    @FXML private Button btnAddCategory;
    @FXML private Button btnAddProduct;
    @FXML private ComboBox<Preset> cbPresets;
    @FXML private Button btnApplyPreset;
    @FXML private Button btnSavePreset;
    @FXML private Button btnDeletePreset;
    @FXML private Button btnConfigLayout;
    @FXML private ImageView cameloImageView;
    @FXML private ImageView solImageView;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox categoriesContainer;
    @FXML private Region ledPrinterStatus;
    @FXML private Label lblPrinterName;
    @FXML private Label lblStatus;
    @FXML private Button btnPrintSelected;

    private String responsavelAtual;
    private PersistenceService persistenceService;
    private ElginPrinterService printerService;
    private Stage stage;

    private List<CategoriaProduto> categorias = new ArrayList<>();
    private List<Preset> presets = new ArrayList<>();
    private final Map<String, Integer> quantitiesByProductId = new HashMap<>();
    private final Map<String, ProductCardComponent> cardComponents = new HashMap<>();

    @FXML
    private void initialize() {
        mainStackPane.setStyle("-fx-background-color: #E2DDD6;");
    }

    public void init(String responsavelInicial, PersistenceService persistence, ElginPrinterService printer, Stage stage) {
        this.responsavelAtual = responsavelInicial;
        this.persistenceService = persistence;
        this.printerService = printer;
        this.stage = stage;

        stage.setOpacity(1.0);
        mainLayout.setOpacity(1.0);
        configureEventLayers();
        loadBrandImages();

        this.categorias = persistenceService.loadCategorias();
        this.presets = persistenceService.loadPresets();

        lblOperador.setText("Responsável: " + responsavelAtual);

        setupSearchFilter();
        refreshPresetsCombo();
        updatePrinterStatus();
        rebuildProductList();
    }

    private void configureEventLayers() {
        backgroundPane.setMouseTransparent(true);
        backgroundPane.setPickOnBounds(false);
        headerPane.setMouseTransparent(false);
        headerPane.setPickOnBounds(true);
    }

    /** Carrega ativos da marca pelo classpath, inclusive quando empacotados no JAR. */
    private void loadBrandImages() {
        loadImage(cameloImageView, "/img/Camelo.png");
        loadImage(solImageView, "/img/Sol.png");
        solImageView.setMouseTransparent(true);
        solImageView.setPickOnBounds(false);
    }

    private void loadImage(ImageView target, String resourcePath) {
        if (target == null) {
            System.err.println("ERRO: ImageView não foi injetado para " + resourcePath);
            return;
        }
        try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                System.err.println("ERRO: Resource " + resourcePath + " não foi encontrado no classpath!");
                return;
            }

            Image image = new Image(stream);
            if (image.isError()) {
                throw new IllegalStateException("Não foi possível decodificar " + resourcePath, image.getException());
            }

            target.setImage(image);
            target.setPreserveRatio(true);
            target.setSmooth(true);
            if ("/img/Camelo.png".equals(resourcePath)) {
                target.setFitHeight(38);
            } else {
                target.setFitWidth(1300);
                target.setOpacity(0.35);
                StackPane.setAlignment(target, Pos.BOTTOM_RIGHT);
                target.setTranslateX(400);
                target.setTranslateY(-150);
            }
        } catch (Exception ex) {
            System.err.println("ERRO ao carregar recurso " + resourcePath + ": " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void setupSearchFilter() {
        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> rebuildProductList());
    }

    private void updatePrinterStatus() {
        String printerName = persistenceService.getSelectedPrinter();
        if (printerName == null || printerName.isBlank()) {
            lblPrinterName.setText("Impressora: Não configurada");
            ledPrinterStatus.getStyleClass().removeAll("led-online");
            if (!ledPrinterStatus.getStyleClass().contains("led-offline")) {
                ledPrinterStatus.getStyleClass().add("led-offline");
            }
        } else {
            lblPrinterName.setText("Impressora: " + printerName);
            ledPrinterStatus.getStyleClass().removeAll("led-offline");
            if (!ledPrinterStatus.getStyleClass().contains("led-online")) {
                ledPrinterStatus.getStyleClass().add("led-online");
            }
        }
    }

    private void refreshPresetsCombo() {
        Preset selected = cbPresets.getValue();
        cbPresets.setItems(FXCollections.observableArrayList(presets));
        if (selected != null && presets.contains(selected)) {
            cbPresets.setValue(selected);
        } else if (!presets.isEmpty()) {
            cbPresets.setValue(presets.get(0));
        }
    }

    /**
     * Reconstrói a árvore de categorias e cards de produtos de forma reativa,
     * respeitando o filtro de busca em tempo real.
     */
    public void rebuildProductList() {
        categoriesContainer.getChildren().clear();
        cardComponents.clear();

        String query = (tfSearch.getText() != null) ? tfSearch.getText().trim().toLowerCase() : "";

        if (categorias.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(60, 20, 60, 20));

            Label lblEmpty = new Label("Nenhuma pasta encontrada. Clique em '+ Nova Pasta' para começar.");
            lblEmpty.setStyle("-fx-text-fill: #6E665E; -fx-font-size: 14px; -fx-font-weight: bold;");

            emptyBox.getChildren().add(lblEmpty);
            categoriesContainer.getChildren().add(emptyBox);
            return;
        }

        for (int catIdx = 0; catIdx < categorias.size(); catIdx++) {
            CategoriaProduto cat = categorias.get(catIdx);

            // Filtro de produtos
            List<Produto> filteredProds = new ArrayList<>();
            for (Produto p : cat.getProdutos()) {
                if (query.isEmpty() ||
                        p.getNome().toLowerCase().contains(query) ||
                        (p.getSif() != null && p.getSif().toLowerCase().contains(query)) ||
                        (p.getArmazenamento() != null && p.getArmazenamento().toLowerCase().contains(query))) {
                    filteredProds.add(p);
                }
            }

            if (!query.isEmpty() && filteredProds.isEmpty()) {
                continue; // Pula categoria se nada coincidir com a busca
            }

            VBox catCard = createCategoryCard(cat, catIdx, filteredProds, !query.isEmpty());
            categoriesContainer.getChildren().add(catCard);
        }
    }

    private VBox createCategoryCard(CategoriaProduto cat, int catIdx, List<Produto> productsToShow, boolean forceExpand) {
        VBox catCard = new VBox(0);
        catCard.getStyleClass().add("category-card");
        catCard.setCache(true);
        catCard.setCacheHint(javafx.scene.CacheHint.SPEED);

        boolean isExpanded = forceExpand || cat.isExpanded();

        // 1. Header do Accordion
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("category-header");
        if (!isExpanded) {
            header.getStyleClass().add("category-header-collapsed");
        }

        Label lblTitle = new Label(cat.getNome());
        lblTitle.getStyleClass().add("category-title");

        Label lblCount = new Label("(" + productsToShow.size() + " produto" + (productsToShow.size() == 1 ? "" : "s") + ")");
        lblCount.getStyleClass().add("category-counter");

        HBox titleGroup = new HBox(10, lblTitle, lblCount);
        titleGroup.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleGroup, Priority.ALWAYS);

        // Ações da Pasta (Adicionar Produto, Renomear, Excluir)
        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getStyleClass().add("category-actions");

        Button btnAddProd = new Button("Add Produto");
        btnAddProd.getStyleClass().addAll("button", "category-action");
        btnAddProd.setOnAction(e -> handleAddProductToCategory(cat));

        Button btnRename = new Button("Renomear");
        btnRename.getStyleClass().addAll("button", "category-action");
        btnRename.setOnAction(e -> handleRenameCategory(cat));

        Button btnDelete = new Button("Excluir");
        btnDelete.getStyleClass().addAll("button", "btn-danger", "category-action");
        btnDelete.setOnAction(e -> handleDeleteCategory(cat, catIdx));

        actions.getChildren().addAll(btnAddProd, btnRename, btnDelete);
        header.getChildren().addAll(titleGroup, actions);

        // Clique no Header para alternar expandir/recolher
        titleGroup.setOnMouseClicked(e -> {
            cat.setExpanded(!cat.isExpanded());
            rebuildProductList();
        });

        catCard.getChildren().add(header);

        // 2. Conteúdo dos Produtos (quando expandido)
        if (isExpanded) {
            VBox prodsBox = new VBox(8);
            prodsBox.setPadding(new Insets(10, 12, 12, 12));

            if (productsToShow.isEmpty()) {
                Label lblEmpty = new Label("Esta pasta está vazia. Clique em 'Add Produto' para inserir.");
                lblEmpty.setStyle("-fx-text-fill: #6E665E; -fx-font-style: italic; -fx-font-size: 12px; -fx-padding: 8px;");
                prodsBox.getChildren().add(lblEmpty);
            } else {
                for (int prodIdx = 0; prodIdx < productsToShow.size(); prodIdx++) {
                    Produto p = productsToShow.get(prodIdx);
                    int currentQty = quantitiesByProductId.getOrDefault(p.getId(), 0);

                    ProductCardComponent card = new ProductCardComponent(
                            p,
                            currentQty,
                            catIdx,
                            prodIdx,
                            this::handleEditProduct,
                            this::handleDeleteProduct,
                            this::handleQuantityChanged,
                            this::handleDropProduct
                    );
                    cardComponents.put(p.getId(), card);
                    prodsBox.getChildren().add(card);
                }
            }

            catCard.getChildren().add(prodsBox);
        }

        return catCard;
    }

    private void handleQuantityChanged(Produto p, int newQty) {
        quantitiesByProductId.put(p.getId(), newQty);
    }

    private void handleAddProductToCategory(CategoriaProduto targetCat) {
        Optional<Produto> result = ProdutoDialogFX.exibir(stage, null);
        result.ifPresent(produto -> {
            targetCat.getProdutos().add(produto);
            persistenceService.saveCategorias(categorias);
            rebuildProductList();
            setStatus("Produto '" + produto.getNome() + "' adicionado em '" + targetCat.getNome() + "'.", false);
        });
    }

    private void handleEditProduct(Produto p) {
        Optional<Produto> result = ProdutoDialogFX.exibir(stage, p);
        result.ifPresent(produto -> {
            persistenceService.saveCategorias(categorias);
            rebuildProductList();
            setStatus("Produto '" + produto.getNome() + "' atualizado.", false);
        });
    }

    private void handleDeleteProduct(Produto p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(stage);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Deseja realmente excluir o produto?");
        alert.setContentText(p.getNome());
        styleDialog(alert);

        Optional<ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            for (CategoriaProduto cat : categorias) {
                cat.getProdutos().remove(p);
            }
            quantitiesByProductId.remove(p.getId());
            persistenceService.saveCategorias(categorias);
            rebuildProductList();
            setStatus("Produto '" + p.getNome() + "' excluído.", false);
        }
    }

    private void handleRenameCategory(CategoriaProduto cat) {
        TextInputDialog dialog = new TextInputDialog(cat.getNome());
        dialog.initOwner(stage);
        dialog.setTitle("Renomear Pasta");
        dialog.setHeaderText("Informe o novo nome da pasta:");
        styleDialog(dialog);
        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.trim().isEmpty()) {
                cat.setNome(newName.trim());
                persistenceService.saveCategorias(categorias);
                rebuildProductList();
                setStatus("Pasta renomeada para '" + newName.trim() + "'.", false);
            }
        });
    }

    private void handleDeleteCategory(CategoriaProduto cat, int catIdx) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(stage);
        alert.setTitle("Confirmar Exclusão de Pasta");
        alert.setHeaderText("Excluir pasta '" + cat.getNome() + "'?");
        alert.setContentText("Todos os produtos contidos nela também serão removidos.");
        styleDialog(alert);

        Optional<ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            categorias.remove(catIdx);
            persistenceService.saveCategorias(categorias);
            rebuildProductList();
            setStatus("Pasta '" + cat.getNome() + "' excluída.", false);
        }
    }

    private void handleDropProduct(String payload, int targetCatIdx) {
        try {
            String[] parts = payload.split(":");
            int sourceCatIdx = Integer.parseInt(parts[0]);
            int sourceProdIdx = Integer.parseInt(parts[1]);

            if (sourceCatIdx >= 0 && sourceCatIdx < categorias.size() && targetCatIdx >= 0 && targetCatIdx < categorias.size()) {
                List<Produto> srcList = categorias.get(sourceCatIdx).getProdutos();
                if (sourceProdIdx >= 0 && sourceProdIdx < srcList.size()) {
                    Produto moved = srcList.remove(sourceProdIdx);
                    categorias.get(targetCatIdx).getProdutos().add(moved);
                    persistenceService.saveCategorias(categorias);
                    rebuildProductList();
                    setStatus("Produto '" + moved.getNome() + "' movido para '" + categorias.get(targetCatIdx).getNome() + "'.", false);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleTrocarOperador() {
        Optional<String> novoUser = LoginDialogFX.solicitarOperador(stage);
        novoUser.ifPresent(u -> {
            this.responsavelAtual = u;
            lblOperador.setText("Responsável: " + responsavelAtual);
            setStatus("Responsável alterado para '" + responsavelAtual + "'.", false);
        });
    }

    @FXML
    private void handleAddCategory() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.initOwner(stage);
        dialog.setTitle("Criar Nova Pasta");
        dialog.setHeaderText("Informe o nome da nova pasta / categoria:");
        styleDialog(dialog);
        dialog.showAndWait().ifPresent(nome -> {
            if (!nome.trim().isEmpty()) {
                CategoriaProduto newCat = new CategoriaProduto(nome.trim());
                categorias.add(newCat);
                persistenceService.saveCategorias(categorias);
                rebuildProductList();
                setStatus("Pasta '" + nome.trim() + "' criada.", false);
            }
        });
    }

    @FXML
    private void handleAddProduct() {
        if (categorias.isEmpty()) {
            categorias.add(new CategoriaProduto("Nasser Esfihas"));
        }

        CategoriaProduto target = categorias.get(0);
        if (categorias.size() > 1) {
            List<String> names = categorias.stream().map(CategoriaProduto::getNome).toList();
            ChoiceDialog<String> dialog = new ChoiceDialog<>(names.get(0), names);
            dialog.initOwner(stage);
            dialog.setTitle("Adicionar Produto");
            dialog.setHeaderText("Selecione a pasta de destino:");
            styleDialog(dialog);
            Optional<String> choice = dialog.showAndWait();
            if (choice.isEmpty()) return;
            for (CategoriaProduto c : categorias) {
                if (c.getNome().equalsIgnoreCase(choice.get())) {
                    target = c;
                    break;
                }
            }
        }

        handleAddProductToCategory(target);
    }

    @FXML
    private void handleConfigLayout() {
        new LayoutEditorDialogFX(stage, persistenceService, printerService);
        updatePrinterStatus();
        rebuildProductList();
    }

    @FXML
    private void handleSavePreset() {
        Map<String, Integer> presetMap = new LinkedHashMap<>();
        for (CategoriaProduto cat : categorias) {
            for (Produto p : cat.getProdutos()) {
                int q = quantitiesByProductId.getOrDefault(p.getId(), 0);
                if (q > 0) presetMap.put(p.getId(), q);
            }
        }

        if (presetMap.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Preset Vazio", "Informe ao menos uma quantidade maior que zero antes de salvar.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.initOwner(stage);
        dialog.setTitle("Salvar Preset");
        dialog.setHeaderText("Informe o nome do preset:");
        styleDialog(dialog);
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                String norm = name.trim();
                presets.removeIf(p -> p.getNome().equalsIgnoreCase(norm));
                presets.add(new Preset(norm, presetMap));
                persistenceService.savePresets(presets);
                refreshPresetsCombo();
                setStatus("Preset '" + norm + "' salvo com sucesso.", false);
            }
        });
    }

    @FXML
    private void handleApplyPreset() {
        Preset preset = cbPresets.getValue();
        if (preset == null) {
            showAlert(Alert.AlertType.WARNING, "Nenhum Preset Selecionado", "Por favor, selecione um preset para aplicar.");
            return;
        }

        quantitiesByProductId.clear();
        quantitiesByProductId.putAll(preset.getQuantidadesPorProduto());

        // Atualiza os cards visíveis
        for (Map.Entry<String, ProductCardComponent> entry : cardComponents.entrySet()) {
            int q = quantitiesByProductId.getOrDefault(entry.getKey(), 0);
            entry.getValue().setQuantity(q);
        }

        rebuildProductList();
        setStatus("Preset '" + preset.getNome() + "' aplicado com sucesso.", false);
    }

    @FXML
    private void handleDeletePreset() {
        Preset preset = cbPresets.getValue();
        if (preset == null) {
            showAlert(Alert.AlertType.WARNING, "Nenhum Preset Selecionado", "Por favor, selecione um preset para excluir.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(stage);
        alert.setTitle("Confirmar Exclusão de Preset");
        alert.setHeaderText("Deseja realmente excluir o preset '" + preset.getNome() + "'?");
        styleDialog(alert);

        Optional<ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            presets.remove(preset);
            persistenceService.savePresets(presets);
            refreshPresetsCombo();
            setStatus("Preset '" + preset.getNome() + "' excluído.", false);
        }
    }

    @FXML
    private void handlePrintSelected() {
        String printerName = persistenceService.getSelectedPrinter();
        if (printerName == null || printerName.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Impressora Não Selecionada",
                    "Nenhuma impressora Elgin foi configurada como padrão.\nConfigure em 'Configuração do Layout'.");
            return;
        }

        List<ElementoLayout> layoutElements = persistenceService.loadLayoutElements();
        if (layoutElements.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Layout Vazio",
                    "O arquivo de layout visual não possui elementos cadastrados.");
            return;
        }

        Optional<LocalDate> selectedDate = DateSelectionDialog.show(stage);
        if (selectedDate.isEmpty()) {
            setStatus("Impressão cancelada.", false);
            return;
        }

        boolean imprimiuAlgum = false;
        int totalImpressos = 0;
        StringBuilder erros = new StringBuilder();

        LocalDateTime fabricacao = selectedDate.get().atStartOfDay();

        for (CategoriaProduto cat : categorias) {
            for (Produto p : cat.getProdutos()) {
                int qty = quantitiesByProductId.getOrDefault(p.getId(), 0);
                if (qty > 0) {
                    try {
                        LocalDateTime validade = fabricacao.plusDays(p.getDiasValidade());

                        EtiquetaData data = new EtiquetaData(
                                p.getNome(),
                                (p.getSif() == null || p.getSif().isBlank() || p.getSif().trim().equalsIgnoreCase("N/A")) ? "" : p.getSif().trim(),
                                (p.getArmazenamento() == null || p.getArmazenamento().isBlank()) ? "Geral" : p.getArmazenamento(),
                                fabricacao,
                                validade,
                                responsavelAtual
                        );

                        printerService.printVisualLabel(printerName, layoutElements, data, qty);
                        imprimiuAlgum = true;
                        totalImpressos += qty;
                    } catch (ElginPrinterService.ElginPrintException ex) {
                        erros.append("Erro ao imprimir ").append(p.getNome()).append(": ").append(ex.getMessage()).append("\n");
                    }
                }
            }
        }

        if (imprimiuAlgum) {
            // Zera contadores
            quantitiesByProductId.clear();
            for (ProductCardComponent card : cardComponents.values()) {
                card.setQuantity(0);
            }

            if (erros.length() > 0) {
                setStatus("Impressão concluída com alguns avisos.", true);
                showAlert(Alert.AlertType.WARNING, "Retorno da Impressão",
                        "Impressão concluída (" + totalImpressos + " etiquetas) com avisos:\n" + erros);
            } else {
                setStatus("Sucesso: " + totalImpressos + " etiqueta(s) impressa(s).", false);
                showAlert(Alert.AlertType.INFORMATION, "Sucesso",
                        "Impressão em lote enviada à Elgin L42 com sucesso!\nTotal de etiquetas: " + totalImpressos);
            }
        } else {
            if (erros.length() > 0) {
                showAlert(Alert.AlertType.ERROR, "Erro na Operação",
                        "Falha ao realizar a impressão em lote:\n" + erros);
            } else {
                showAlert(Alert.AlertType.WARNING, "Seleção Vazia",
                        "Nenhum produto selecionado (quantidade > 0).");
            }
        }
    }

    private void setStatus(String msg, boolean isError) {
        lblStatus.setText(msg);
        lblStatus.setStyle(isError ? "-fx-text-fill: #C82B28; -fx-font-style: italic;" : "-fx-text-fill: #EAA225; -fx-font-style: italic;");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        styleDialog(alert);
        alert.showAndWait();
    }

    /**
     * Aplica o tema da marca Nasser Esfihas a qualquer dialog padrão do JavaFX.
     */
    private void styleDialog(javafx.scene.control.Dialog<?> dialog) {
        javafx.scene.control.DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-background-color: #EFEBE6;");
        try {
            String css = java.util.Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm();
            pane.getStylesheets().add(css);
        } catch (Exception ignored) {}
        // Aplica título escuro no Windows ao stage do dialog
        dialog.setOnShown(e -> {
            javafx.stage.Window w = pane.getScene().getWindow();
            if (w instanceof Stage s) {
                WindowUtils.applyAppIcon(s);
                pane.getScene().setFill(javafx.scene.paint.Color.web("#E2DDD6"));
                WindowsDarkThemeHelper.applyDarkTitleBar(s);
            }
        });
    }
}
