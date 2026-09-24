package com.nasser.etiqueta.gui;

import com.nasser.etiqueta.model.Produto;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Componente visual do Card de Produto, estilizado no padrão Apple Dark Matte
 * com ícones vetoriais Ikonli, badges de validade/armazenamento e controle de quantidade.
 */
public class ProductCardComponent extends HBox {

    public static final DataFormat DRAG_FORMAT = new DataFormat("application/x-nasser-product-id");

    private final Produto produto;
    private final TextField tfQty;
    private int currentQty = 0;

    public ProductCardComponent(
            Produto produto,
            int initialQty,
            int categoryIndex,
            int productIndex,
            Consumer<Produto> onEdit,
            Consumer<Produto> onDelete,
            BiConsumer<Produto, Integer> onQuantityChange,
            BiConsumer<String, Integer> onDropProductToCategory
    ) {
        this.produto = produto;
        this.currentQty = Math.max(0, initialQty);

        getStyleClass().add("product-card");
        setCache(true);
        setCacheHint(javafx.scene.CacheHint.SPEED);
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(14);
        setPadding(new Insets(10, 16, 10, 16));
        setMinHeight(64);
        setMaxWidth(Double.MAX_VALUE);

        // Informações do Produto (Esquerda)
        VBox infoBox = new VBox(4);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label lblName = new Label(produto.getNome());
        lblName.getStyleClass().add("product-name");

        HBox badgesBox = new HBox(6);
        badgesBox.setAlignment(Pos.CENTER_LEFT);

        if (produto.getSif() != null && !produto.getSif().isBlank() && !produto.getSif().trim().equalsIgnoreCase("N/A")) {
            Label lblSif = new Label("SIF: " + produto.getSif().trim());
            lblSif.getStyleClass().add("product-sif");
            badgesBox.getChildren().add(lblSif);
        }

        Label lblValidade = new Label("Validade: " + produto.getDiasValidade() + " dias");
        lblValidade.getStyleClass().add("product-validity-pill");
        badgesBox.getChildren().add(lblValidade);

        String arm = (produto.getArmazenamento() == null || produto.getArmazenamento().isBlank())
                ? "Geral"
                : produto.getArmazenamento().trim();
        Label lblArmaz = new Label(arm);
        lblArmaz.getStyleClass().add("product-info-pill");
        badgesBox.getChildren().add(lblArmaz);

        infoBox.getChildren().addAll(lblName, badgesBox);

        // 3. Ações CRUD do Card (Editar / Excluir)
        HBox actionsBox = new HBox(6);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.getStyleClass().add("product-actions");

        Button btnEdit = new Button();
        btnEdit.getStyleClass().add("btn-icon-subtle");
        btnEdit.setText("Editar");
        btnEdit.setTooltip(new Tooltip("Editar Produto"));
        btnEdit.setOnAction(e -> {
            if (onEdit != null) onEdit.accept(produto);
        });

        Button btnDelete = new Button();
        btnDelete.getStyleClass().addAll("btn-icon-subtle");
        btnDelete.setText("Excluir");
        btnDelete.setTooltip(new Tooltip("Excluir Produto"));
        btnDelete.setOnAction(e -> {
            if (onDelete != null) onDelete.accept(produto);
        });

        actionsBox.getChildren().addAll(btnEdit, btnDelete);

        // 4. Seção de Ajuste de Quantidade (- [qtd] +)
        HBox qtyBox = new HBox(4);
        qtyBox.setAlignment(Pos.CENTER);
        qtyBox.getStyleClass().add("quantity-control");

        Button btnMinus = new Button("-");
        btnMinus.getStyleClass().add("btn-qty-step");

        tfQty = new TextField(String.valueOf(currentQty));
        tfQty.getStyleClass().add("qty-input");

        Button btnPlus = new Button("+");
        btnPlus.getStyleClass().add("btn-qty-step");

        btnMinus.setOnAction(e -> {
            if (currentQty > 0) {
                currentQty--;
                tfQty.setText(String.valueOf(currentQty));
                if (onQuantityChange != null) onQuantityChange.accept(produto, currentQty);
            }
        });

        btnPlus.setOnAction(e -> {
            currentQty++;
            tfQty.setText(String.valueOf(currentQty));
            if (onQuantityChange != null) onQuantityChange.accept(produto, currentQty);
        });

        tfQty.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                currentQty = 0;
            } else {
                try {
                    int parsed = Integer.parseInt(newVal.trim());
                    currentQty = Math.max(0, parsed);
                } catch (NumberFormatException ex) {
                    tfQty.setText(oldVal);
                    return;
                }
            }
            if (onQuantityChange != null) onQuantityChange.accept(produto, currentQty);
        });

        qtyBox.getChildren().addAll(btnMinus, tfQty, btnPlus);

        getChildren().addAll(infoBox, actionsBox, qtyBox);

        // 5. Configuração de Arraste (Drag and Drop)
        setupDragAndDrop(categoryIndex, productIndex, onDropProductToCategory);
    }

    private void setupDragAndDrop(
            int catIdx,
            int prodIdx,
            BiConsumer<String, Integer> onDropProductToCategory
    ) {
        setOnDragDetected(event -> {
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.put(DRAG_FORMAT, catIdx + ":" + prodIdx + ":" + produto.getId());
            db.setContent(content);
            event.consume();
        });

        setOnDragOver(event -> {
            if (event.getGestureSource() != this && event.getDragboard().hasContent(DRAG_FORMAT)) {
                event.acceptTransferModes(TransferMode.MOVE);
                if (!getStyleClass().contains("product-card-drag-over")) {
                    getStyleClass().add("product-card-drag-over");
                }
            }
            event.consume();
        });

        setOnDragExited(event -> {
            getStyleClass().remove("product-card-drag-over");
            event.consume();
        });

        setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasContent(DRAG_FORMAT)) {
                String payload = (String) db.getContent(DRAG_FORMAT);
                if (onDropProductToCategory != null) {
                    onDropProductToCategory.accept(payload, catIdx);
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    public void setQuantity(int qty) {
        this.currentQty = Math.max(0, qty);
        tfQty.setText(String.valueOf(this.currentQty));
    }

    public int getQuantity() {
        return currentQty;
    }

    public Produto getProduto() {
        return produto;
    }
}
