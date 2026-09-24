package com.nasser.etiqueta.gui;

import com.nasser.etiqueta.model.ElementoLayout;
import com.nasser.etiqueta.service.ElginPrinterService;
import com.nasser.etiqueta.service.PersistenceService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * Editor Visual de Layout de Etiquetas (60x40mm - 480x320 pixels) em JavaFX.
 * Permite inserção, edição e arraste interativo de textos fixos, tags dinâmicas,
 * linhas divisórias e imagens para a impressora Elgin L42 Pro.
 */
public class LayoutEditorDialogFX {

    private final PersistenceService persistenceService;
    private final ElginPrinterService printerService;
    private final List<ElementoLayout> elements;
    private ElementoLayout selectedElement = null;

    // Componentes de Interface
    private ComboBox<String> cbPrinters;
    private ComboBox<String> cbElementSelector;
    private Canvas canvas;
    private Spinner<Integer> spinX;
    private Spinner<Integer> spinY;

    // Painéis de Propriedades Específicas
    private VBox textPropsBox;
    private TextField tfTextContent;
    private Spinner<Integer> spinFontSize;
    private CheckBox chkBold;

    private VBox linePropsBox;
    private Spinner<Integer> spinX2;
    private Spinner<Integer> spinThickness;

    private VBox imgPropsBox;
    private Spinner<Integer> spinImgWidth;
    private Spinner<Integer> spinImgHeight;
    private Button btnChangeImg;

    // Cache de imagens para desenho no canvas
    private final Map<String, Image> imageCache = new HashMap<>();

    // Estado de arrasto no canvas
    private double dragStartX, dragStartY;
    private int elementInitialX, elementInitialY;
    private boolean isDragging = false;
    private boolean updatingControls = false;

    public LayoutEditorDialogFX(Window owner, PersistenceService persistence, ElginPrinterService printer) {
        this.persistenceService = persistence;
        this.printerService = printer;
        this.elements = new ArrayList<>(persistenceService.loadLayoutElements());
        if (!elements.isEmpty()) {
            this.selectedElement = elements.get(0);
        }

        exibirJanela(owner);
    }

    private void exibirJanela(Window owner) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) stage.initOwner(owner);
        WindowUtils.applyAppIcon(stage);
        stage.setTitle("Editor de Layout Visual - Elgin L42 Pro");
        stage.setResizable(true);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #E2DDD6;");

        // 1. Topo: Configuração de Impressora + Toolbar de Elementos
        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(14, 20, 10, 20));
        topBox.setStyle("-fx-background-color: #EFEBE6; -fx-border-color: transparent transparent #D1C9BF transparent; -fx-border-width: 0 0 1px 0;");

        // Impressora
        HBox printerBox = new HBox(10);
        printerBox.setAlignment(Pos.CENTER_LEFT);
        Label lblPrinter = new Label("Impressora Padrão:");
        lblPrinter.setStyle("-fx-text-fill: #23201D; -fx-font-weight: bold; -fx-font-size: 12px;");

        cbPrinters = new ComboBox<>();
        cbPrinters.setPrefWidth(280);
        cbPrinters.setPrefHeight(34);
        refreshPrinters();

        Button btnRefreshPrinters = new Button("Atualizar");
        btnRefreshPrinters.getStyleClass().add("button");
        btnRefreshPrinters.setOnAction(e -> refreshPrinters());

        printerBox.getChildren().addAll(lblPrinter, cbPrinters, btnRefreshPrinters);

        // Toolbar de Adicionar
        HBox toolBar = new HBox(8);
        toolBar.setAlignment(Pos.CENTER_LEFT);

        Label lblAdd = new Label("Adicionar:");
        lblAdd.setStyle("-fx-text-fill: #6E665E; -fx-font-size: 12px;");

        Button btnAddText = createToolButton("Texto Fixo", e -> addNewText());
        Button btnAddTag = createToolButton("Tag Dinâmica", e -> addNewTag(stage));
        Button btnAddLine = createToolButton("Linha Divisória", e -> addNewLine());
        Button btnAddImg = createToolButton("Imagem", e -> addNewImage(stage));

        toolBar.getChildren().addAll(lblAdd, btnAddText, btnAddTag, btnAddLine, btnAddImg);
        topBox.getChildren().addAll(printerBox, toolBar);
        root.setTop(topBox);

        // 2. Centro: Canvas da Etiqueta (480 x 320 px)
        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(16, 20, 16, 20));

        Label lblCanvasTitle = new Label("Visualização da Etiqueta (480x320 px - 60x40mm)");
        lblCanvasTitle.setStyle("-fx-text-fill: #6E665E; -fx-font-size: 12px; -fx-font-weight: bold;");

        canvas = new Canvas(480, 320);
        setupCanvasEvents();

        StackPane canvasContainer = new StackPane(canvas);
        canvasContainer.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #C82B28; -fx-border-width: 2px; -fx-border-radius: 4px;");
        canvasContainer.setMaxSize(484, 324);

        centerBox.getChildren().addAll(lblCanvasTitle, canvasContainer);
        root.setCenter(centerBox);

        // 3. Direita: Painel de Propriedades
        VBox propsPanel = new VBox(12);
        propsPanel.setPrefWidth(280);
        propsPanel.setPadding(new Insets(16, 18, 16, 18));
        propsPanel.setStyle("-fx-background-color: #EFEBE6; -fx-border-color: transparent transparent transparent #D1C9BF; -fx-border-width: 0 0 0 1px;");

        Label lblPropsTitle = new Label("Propriedades do Elemento");
        lblPropsTitle.setStyle("-fx-text-fill: #23201D; -fx-font-size: 14px; -fx-font-weight: bold;");

        cbElementSelector = new ComboBox<>();
        cbElementSelector.setMaxWidth(Double.MAX_VALUE);
        cbElementSelector.setPrefHeight(34);
        cbElementSelector.setOnAction(e -> {
            int idx = cbElementSelector.getSelectionModel().getSelectedIndex();
            if (idx >= 0 && idx < elements.size()) {
                selectedElement = elements.get(idx);
                syncControlsFromElement();
                redrawCanvas();
            }
        });

        // Coordenadas comuns
        GridPane coordsGrid = new GridPane();
        coordsGrid.setHgap(8);
        coordsGrid.setVgap(8);

        Label lblX = new Label("Posição X:");
        lblX.setStyle("-fx-text-fill: #6E665E; -fx-font-size: 12px;");
        spinX = new Spinner<>(0, 480, 0);
        spinX.setEditable(true);
        spinX.valueProperty().addListener((obs, old, val) -> {
            if (!updatingControls && selectedElement != null && val != null) {
                selectedElement.setX(val);
                redrawCanvas();
            }
        });

        Label lblY = new Label("Posição Y:");
        lblY.setStyle("-fx-text-fill: #6E665E; -fx-font-size: 12px;");
        spinY = new Spinner<>(0, 320, 0);
        spinY.setEditable(true);
        spinY.valueProperty().addListener((obs, old, val) -> {
            if (!updatingControls && selectedElement != null && val != null) {
                selectedElement.setY(val);
                redrawCanvas();
            }
        });

        coordsGrid.add(lblX, 0, 0);
        coordsGrid.add(spinX, 1, 0);
        coordsGrid.add(lblY, 0, 1);
        coordsGrid.add(spinY, 1, 1);

        // Painel Texto/Tag
        textPropsBox = new VBox(8);
        Label lblContent = new Label("Texto / Tag:");
        lblContent.setStyle("-fx-text-fill: #6E665E; -fx-font-size: 12px;");
        tfTextContent = new TextField();
        tfTextContent.textProperty().addListener((obs, old, val) -> {
            if (!updatingControls && selectedElement != null && val != null) {
                selectedElement.setConteudo(val);
                updateSelectorTitles();
                redrawCanvas();
            }
        });

        Label lblFont = new Label("Tamanho da Fonte:");
        lblFont.setStyle("-fx-text-fill: #6E665E; -fx-font-size: 12px;");
        spinFontSize = new Spinner<>(6, 72, 14);
        spinFontSize.setEditable(true);
        spinFontSize.valueProperty().addListener((obs, old, val) -> {
            if (!updatingControls && selectedElement != null && val != null) {
                selectedElement.setFontSize(val);
                redrawCanvas();
            }
        });

        chkBold = new CheckBox("Texto em Negrito");
        chkBold.setStyle("-fx-text-fill: #23201D; -fx-font-size: 12px;");
        chkBold.selectedProperty().addListener((obs, old, val) -> {
            if (!updatingControls && selectedElement != null && val != null) {
                selectedElement.setBold(val);
                redrawCanvas();
            }
        });
        textPropsBox.getChildren().addAll(lblContent, tfTextContent, lblFont, spinFontSize, chkBold);

        // Painel Linha
        linePropsBox = new VBox(8);
        Label lblX2 = new Label("Fim X (Largura):");
        lblX2.setStyle("-fx-text-fill: #6E665E; -fx-font-size: 12px;");
        spinX2 = new Spinner<>(0, 480, 460);
        spinX2.setEditable(true);
        spinX2.valueProperty().addListener((obs, old, val) -> {
            if (!updatingControls && selectedElement != null && val != null) {
                selectedElement.setX2(val);
                redrawCanvas();
            }
        });

        Label lblThick = new Label("Espessura:");
        lblThick.setStyle("-fx-text-fill: #6E665E; -fx-font-size: 12px;");
        spinThickness = new Spinner<>(1, 15, 2);
        spinThickness.setEditable(true);
        spinThickness.valueProperty().addListener((obs, old, val) -> {
            if (!updatingControls && selectedElement != null && val != null) {
                selectedElement.setThickness(val);
                redrawCanvas();
            }
        });
        linePropsBox.getChildren().addAll(lblX2, spinX2, lblThick, spinThickness);

        // Painel Imagem
        imgPropsBox = new VBox(8);
        Label lblW = new Label("Largura (px):");
        lblW.setStyle("-fx-text-fill: #6E665E; -fx-font-size: 12px;");
        spinImgWidth = new Spinner<>(10, 480, 100);
        spinImgWidth.setEditable(true);
        spinImgWidth.valueProperty().addListener((obs, old, val) -> {
            if (!updatingControls && selectedElement != null && val != null) {
                selectedElement.setX2(val);
                redrawCanvas();
            }
        });

        Label lblH = new Label("Altura (px):");
        lblH.setStyle("-fx-text-fill: #6E665E; -fx-font-size: 12px;");
        spinImgHeight = new Spinner<>(10, 320, 100);
        spinImgHeight.setEditable(true);
        spinImgHeight.valueProperty().addListener((obs, old, val) -> {
            if (!updatingControls && selectedElement != null && val != null) {
                selectedElement.setThickness(val);
                redrawCanvas();
            }
        });

        btnChangeImg = new Button("Alterar Imagem...");
        btnChangeImg.getStyleClass().add("button");
        btnChangeImg.setOnAction(e -> pickImageForSelected(stage));
        imgPropsBox.getChildren().addAll(lblW, spinImgWidth, lblH, spinImgHeight, btnChangeImg);

        // Botão Excluir
        Button btnDeleteElem = new Button("Excluir Elemento");
        btnDeleteElem.getStyleClass().addAll("button", "btn-danger");
        btnDeleteElem.setMaxWidth(Double.MAX_VALUE);
        btnDeleteElem.setOnAction(e -> deleteSelectedElement());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        propsPanel.getChildren().addAll(
                lblPropsTitle,
                cbElementSelector,
                coordsGrid,
                textPropsBox,
                linePropsBox,
                imgPropsBox,
                spacer,
                btnDeleteElem
        );
        ScrollPane propsScroll = new ScrollPane(propsPanel);
        propsScroll.setFitToWidth(true);
        propsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        propsScroll.setPrefWidth(300);
        propsScroll.setMinWidth(280);
        propsScroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent transparent transparent #D1C9BF; -fx-border-width: 0 0 0 1px;");
        root.setRight(propsScroll);

        // 4. Rodapé: Salvar / Cancelar
        HBox footerBox = new HBox(12);
        footerBox.setAlignment(Pos.CENTER_RIGHT);
        footerBox.setPadding(new Insets(12, 20, 12, 20));
        footerBox.setStyle("-fx-background-color: #181818; -fx-border-color: #2C2C2C transparent transparent transparent; -fx-border-width: 1px 0 0 0;");

        Button btnClose = new Button("Fechar");
        btnClose.getStyleClass().add("button");
        btnClose.setOnAction(e -> stage.close());

        Button btnSave = new Button("Salvar Layout e Configurações");
        btnSave.getStyleClass().addAll("button", "btn-primary");
        btnSave.setOnAction(e -> {
            persistenceService.saveLayoutElements(elements);
            if (cbPrinters.getValue() != null) {
                persistenceService.saveSelectedPrinter(cbPrinters.getValue());
            }
            stage.close();
        });

        footerBox.getChildren().addAll(btnClose, btnSave);
        root.setBottom(footerBox);

        // Inicializa dados e tela
        refreshElementSelector();
        syncControlsFromElement();
        redrawCanvas();

        Scene scene = new Scene(root, 1080, 700);
        scene.setFill(javafx.scene.paint.Color.web("#E2DDD6"));
        String css = Objects.requireNonNull(LayoutEditorDialogFX.class.getResource("/css/style.css")).toExternalForm();
        scene.getStylesheets().add(css);

        stage.setScene(scene);
        stage.setMinWidth(960);
        stage.setMinHeight(620);
        stage.centerOnScreen();
        WindowsDarkThemeHelper.applyDarkTitleBar(stage);
        stage.showAndWait();
    }

    private void refreshPrinters() {
        List<String> list = printerService.listPrinters();
        cbPrinters.setItems(FXCollections.observableArrayList(list));
        String selected = persistenceService.getSelectedPrinter();
        if (selected != null && list.contains(selected)) {
            cbPrinters.setValue(selected);
        } else if (!list.isEmpty()) {
            cbPrinters.setValue(list.get(0));
        }
    }

    private Button createToolButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button b = new Button(text);
        b.getStyleClass().add("button");
        b.setOnAction(handler);
        return b;
    }

    private void addNewText() {
        ElementoLayout elem = new ElementoLayout("TEXTO", "Novo Texto", 20, 100, 0, 16, false, 1);
        elements.add(elem);
        selectedElement = elem;
        refreshElementSelector();
        redrawCanvas();
    }

    private void addNewTag(Stage stage) {
        List<String> tags = List.of("{Produto}", "{Sif}", "{Armazenamento}", "{Fabricacao}", "{Validade}", "{Responsavel}");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("{Produto}", tags);
        dialog.setTitle("Adicionar Tag Dinâmica");
        dialog.setHeaderText("Selecione o dado dinâmico da etiqueta:");
        dialog.initOwner(stage);
        // Apple Dark Matte theming
        dialog.getDialogPane().setStyle("-fx-background-color: #EFEBE6;");
        try {
            String dialogCss = Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm();
            dialog.getDialogPane().getStylesheets().add(dialogCss);
        } catch (Exception ignored) {}
        dialog.setOnShown(e -> {
            javafx.stage.Window w = dialog.getDialogPane().getScene().getWindow();
            if (w instanceof Stage s) {
                dialog.getDialogPane().getScene().setFill(Color.web("#E2DDD6"));
                WindowsDarkThemeHelper.applyDarkTitleBar(s);
            }
        });
        dialog.showAndWait().ifPresent(chosen -> {
            ElementoLayout elem = new ElementoLayout("TAG", chosen, 20, 140, 0, 18, true, 1);
            elements.add(elem);
            selectedElement = elem;
            refreshElementSelector();
            redrawCanvas();
        });
    }

    private void addNewLine() {
        ElementoLayout elem = new ElementoLayout("LINHA", "", 20, 160, 460, 0, false, 2);
        elements.add(elem);
        selectedElement = elem;
        refreshElementSelector();
        redrawCanvas();
    }

    private void addNewImage(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecionar Imagem para Etiqueta");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.bmp"));
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            ElementoLayout elem = new ElementoLayout("IMAGEM", file.getAbsolutePath(), 30, 30, 80, 0, false, 80);
            elements.add(elem);
            selectedElement = elem;
            refreshElementSelector();
            redrawCanvas();
        }
    }

    private void pickImageForSelected(Stage stage) {
        if (selectedElement == null || !"IMAGEM".equals(selectedElement.getTipo())) return;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Alterar Imagem");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.bmp"));
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            selectedElement.setConteudo(file.getAbsolutePath());
            imageCache.remove(file.getAbsolutePath());
            redrawCanvas();
        }
    }

    private void deleteSelectedElement() {
        if (selectedElement != null) {
            elements.remove(selectedElement);
            selectedElement = elements.isEmpty() ? null : elements.get(0);
            refreshElementSelector();
            redrawCanvas();
        }
    }

    private void refreshElementSelector() {
        updatingControls = true;
        List<String> items = new ArrayList<>();
        int selectedIndex = -1;
        for (int i = 0; i < elements.size(); i++) {
            ElementoLayout e = elements.get(i);
            String desc = e.getTipo() + ": " + (e.getConteudo() != null ? e.getConteudo() : "");
            items.add(desc);
            if (e == selectedElement) selectedIndex = i;
        }
        cbElementSelector.setItems(FXCollections.observableArrayList(items));
        if (selectedIndex >= 0) {
            cbElementSelector.getSelectionModel().select(selectedIndex);
        }
        updatingControls = false;
        syncControlsFromElement();
    }

    private void updateSelectorTitles() {
        int idx = cbElementSelector.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && selectedElement != null) {
            String desc = selectedElement.getTipo() + ": " + (selectedElement.getConteudo() != null ? selectedElement.getConteudo() : "");
            cbElementSelector.getItems().set(idx, desc);
        }
    }

    private void syncControlsFromElement() {
        if (selectedElement == null) {
            textPropsBox.setVisible(false);
            textPropsBox.setManaged(false);
            linePropsBox.setVisible(false);
            linePropsBox.setManaged(false);
            imgPropsBox.setVisible(false);
            imgPropsBox.setManaged(false);
            return;
        }

        updatingControls = true;
        spinX.getValueFactory().setValue(selectedElement.getX());
        spinY.getValueFactory().setValue(selectedElement.getY());

        String tipo = selectedElement.getTipo();
        boolean isText = "TEXTO".equals(tipo) || "TAG".equals(tipo);
        boolean isLine = "LINHA".equals(tipo);
        boolean isImg = "IMAGEM".equals(tipo);

        textPropsBox.setVisible(isText);
        textPropsBox.setManaged(isText);
        linePropsBox.setVisible(isLine);
        linePropsBox.setManaged(isLine);
        imgPropsBox.setVisible(isImg);
        imgPropsBox.setManaged(isImg);

        if (isText) {
            tfTextContent.setText(selectedElement.getConteudo());
            spinFontSize.getValueFactory().setValue(selectedElement.getFontSize());
            chkBold.setSelected(selectedElement.isBold());
        } else if (isLine) {
            spinX2.getValueFactory().setValue(selectedElement.getX2());
            spinThickness.getValueFactory().setValue(selectedElement.getThickness());
        } else if (isImg) {
            spinImgWidth.getValueFactory().setValue(selectedElement.getX2());
            spinImgHeight.getValueFactory().setValue(selectedElement.getThickness());
        }
        updatingControls = false;
    }

    private void setupCanvasEvents() {
        canvas.setOnMousePressed(e -> {
            double mx = e.getX();
            double my = e.getY();
            // Verifica se clicou em algum elemento (ordem reversa para pegar o que está por cima)
            for (int i = elements.size() - 1; i >= 0; i--) {
                ElementoLayout elem = elements.get(i);
                if (hitTest(elem, mx, my)) {
                    selectedElement = elem;
                    refreshElementSelector();
                    redrawCanvas();
                    dragStartX = mx;
                    dragStartY = my;
                    elementInitialX = elem.getX();
                    elementInitialY = elem.getY();
                    isDragging = true;
                    return;
                }
            }
        });

        canvas.setOnMouseDragged(e -> {
            if (isDragging && selectedElement != null) {
                double dx = e.getX() - dragStartX;
                double dy = e.getY() - dragStartY;
                int newX = Math.max(0, Math.min(480, (int) (elementInitialX + dx)));
                int newY = Math.max(0, Math.min(320, (int) (elementInitialY + dy)));
                selectedElement.setX(newX);
                selectedElement.setY(newY);
                updatingControls = true;
                spinX.getValueFactory().setValue(newX);
                spinY.getValueFactory().setValue(newY);
                updatingControls = false;
                redrawCanvas();
            }
        });

        canvas.setOnMouseReleased(e -> isDragging = false);
    }

    private boolean hitTest(ElementoLayout elem, double mx, double my) {
        String tipo = elem.getTipo();
        if ("LINHA".equals(tipo)) {
            int y = elem.getY();
            int x1 = Math.min(elem.getX(), elem.getX2());
            int x2 = Math.max(elem.getX(), elem.getX2());
            return (my >= y - 6 && my <= y + 6 && mx >= x1 && mx <= x2);
        } else if ("IMAGEM".equals(tipo)) {
            int w = elem.getX2() > 0 ? elem.getX2() : 60;
            int h = elem.getThickness() > 0 ? elem.getThickness() : 60;
            return (mx >= elem.getX() && mx <= elem.getX() + w && my >= elem.getY() && my <= elem.getY() + h);
        } else {
            int h = elem.getFontSize() > 0 ? elem.getFontSize() : 16;
            int w = (elem.getConteudo() != null ? elem.getConteudo().length() : 5) * (h / 2 + 2);
            return (mx >= elem.getX() - 4 && mx <= elem.getX() + w && my >= elem.getY() - h && my <= elem.getY() + 4);
        }
    }

    private void redrawCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // 1. Fundo Branco da Etiqueta
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, 480, 320);

        // 2. Elementos
        for (ElementoLayout elem : elements) {
            boolean isSelected = (elem == selectedElement);

            if ("LINHA".equals(elem.getTipo())) {
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(elem.getThickness());
                gc.strokeLine(elem.getX(), elem.getY(), elem.getX2(), elem.getY());

                if (isSelected) {
                    gc.setStroke(Color.web("#C82B28"));
                    gc.setLineWidth(1);
                    gc.strokeRect(elem.getX() - 2, elem.getY() - 4, (elem.getX2() - elem.getX()) + 4, 8);
                }
            } else if ("IMAGEM".equals(elem.getTipo())) {
                Image img = getImage(elem.getConteudo());
                int w = elem.getX2() > 0 ? elem.getX2() : 80;
                int h = elem.getThickness() > 0 ? elem.getThickness() : 80;
                if (img != null) {
                    gc.drawImage(img, elem.getX(), elem.getY(), w, h);
                } else {
                    gc.setFill(Color.LIGHTGRAY);
                    gc.fillRect(elem.getX(), elem.getY(), w, h);
                    gc.setFill(Color.BLACK);
                    gc.fillText("[Imagem]", elem.getX() + 10, elem.getY() + h / 2.0);
                }

                if (isSelected) {
                    gc.setStroke(Color.web("#C82B28"));
                    gc.setLineWidth(2);
                    gc.strokeRect(elem.getX() - 2, elem.getY() - 2, w + 4, h + 4);
                }
            } else {
                // Texto ou Tag
                Font font = Font.font(
                        "Arial",
                        elem.isBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                        elem.getFontSize() > 0 ? elem.getFontSize() : 14
                );
                gc.setFont(font);
                gc.setFill(Color.BLACK);

                String text = elem.getConteudo() != null ? elem.getConteudo() : "";
                gc.fillText(text, elem.getX(), elem.getY());

                if (isSelected) {
                    gc.setStroke(Color.web("#C82B28"));
                    gc.setLineWidth(1.5);
                    double w = text.length() * (elem.getFontSize() * 0.55);
                    gc.strokeRect(elem.getX() - 3, elem.getY() - elem.getFontSize(), w + 6, elem.getFontSize() + 4);
                }
            }
        }
    }

    private Image getImage(String path) {
        if (path == null || path.isBlank()) return null;
        if (imageCache.containsKey(path)) return imageCache.get(path);
        try {
            File f = new File(path);
            if (f.exists()) {
                Image img = new Image(new FileInputStream(f));
                imageCache.put(path, img);
                return img;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
