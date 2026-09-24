package com.nasser.etiqueta.gui;

import com.nasser.etiqueta.model.Produto;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Objects;
import java.util.Optional;

/**
 * Diálogo modal para Cadastro e Edição de Produtos em JavaFX.
 * Estilizado com o tema da marca Nasser Esfihas.
 */
public class ProdutoDialogFX {

    public static Optional<Produto> exibir(Window owner, Produto produtoParaEditar) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            stage.initOwner(owner);
        }
        WindowUtils.applyAppIcon(stage);
        boolean isEdit = (produtoParaEditar != null);
        stage.setTitle(isEdit ? "Editar Dados do Produto" : "Cadastrar Novo Produto");
        stage.setResizable(false);

        VBox root = new VBox(18);
        root.setPadding(new Insets(24, 28, 24, 28));
        root.setStyle("-fx-background-color: #EFEBE6; -fx-background-radius: 12px;");

        // Cabeçalho do Modal
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label lblTitle = new Label(isEdit ? "Editar Produto" : "Novo Produto");
        lblTitle.setStyle("-fx-text-fill: #23201D; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label lblSubtitle = new Label(isEdit ? "Atualize as informações do item" : "Preencha os campos para adicionar ao catálogo");
        lblSubtitle.setStyle("-fx-text-fill: #6E665E; -fx-font-size: 11px;");
        titleBox.getChildren().addAll(lblTitle, lblSubtitle);

        header.getChildren().add(titleBox);

        // Grid de Formulário
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(8, 0, 8, 0));

        // Nome
        Label lblNome = new Label("Nome do Produto: *");
        lblNome.setStyle("-fx-text-fill: #23201D; -fx-font-weight: bold; -fx-font-size: 12px;");
        TextField tfNome = new TextField(isEdit ? produtoParaEditar.getNome() : "");
        tfNome.setPromptText("Ex: Esfiha de Carne");
        tfNome.setPrefHeight(36);
        GridPane.setHgrow(tfNome, Priority.ALWAYS);

        // S.I.F.
        Label lblSif = new Label("S.I.F. (Opcional):");
        lblSif.setStyle("-fx-text-fill: #23201D; -fx-font-size: 12px;");
        TextField tfSif = new TextField(isEdit && produtoParaEditar.getSif() != null ? produtoParaEditar.getSif() : "");
        tfSif.setPromptText("Ex: 12345/PR-SIF");
        tfSif.setPrefHeight(36);

        // Armazenamento
        Label lblArmaz = new Label("Armazenamento:");
        lblArmaz.setStyle("-fx-text-fill: #23201D; -fx-font-size: 12px;");
        ComboBox<String> cbArmaz = new ComboBox<>(FXCollections.observableArrayList(
                "Refrigerado (até 4°C)",
                "Congelado (abaixo de -12°C)",
                "Temperatura Ambiente"
        ));
        cbArmaz.setEditable(true);
        cbArmaz.setPrefHeight(36);
        cbArmaz.setMaxWidth(Double.MAX_VALUE);
        if (isEdit && produtoParaEditar.getArmazenamento() != null && !produtoParaEditar.getArmazenamento().isBlank()) {
            cbArmaz.setValue(produtoParaEditar.getArmazenamento());
        } else {
            cbArmaz.setValue("Refrigerado (até 4°C)");
        }

        // Dias de Validade
        Label lblValidade = new Label("Dias de Validade:");
        lblValidade.setStyle("-fx-text-fill: #23201D; -fx-font-size: 12px;");
        Spinner<Integer> spinDias = new Spinner<>(1, 365, isEdit ? produtoParaEditar.getDiasValidade() : 3);
        spinDias.setEditable(true);
        spinDias.setPrefHeight(36);
        spinDias.setMaxWidth(Double.MAX_VALUE);

        grid.add(lblNome, 0, 0);
        grid.add(tfNome, 1, 0);

        grid.add(lblSif, 0, 1);
        grid.add(tfSif, 1, 1);

        grid.add(lblArmaz, 0, 2);
        grid.add(cbArmaz, 1, 2);

        grid.add(lblValidade, 0, 3);
        grid.add(spinDias, 1, 3);

        Label lblError = new Label("");
        lblError.setStyle("-fx-text-fill: #C82B28; -fx-font-size: 11px;");
        lblError.setVisible(false);

        // Botões de Ação
        Button btnCancel = new Button("Cancelar");
        btnCancel.getStyleClass().add("button");
        btnCancel.setPrefHeight(36);

        Button btnSalvar = new Button(isEdit ? "Atualizar Produto" : "Salvar Produto");
        btnSalvar.getStyleClass().addAll("button", isEdit ? "btn-primary" : "btn-success");
        btnSalvar.setPrefHeight(36);

        HBox btnBox = new HBox(10, btnCancel, btnSalvar);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        final Produto[] resultado = new Produto[1];

        Runnable salvarAction = () -> {
            String nome = tfNome.getText().trim();
            if (nome.isEmpty()) {
                lblError.setText("O nome do produto é obrigatório.");
                lblError.setVisible(true);
                tfNome.requestFocus();
                return;
            }

            String sif = tfSif.getText().trim();
            String armazenamento = cbArmaz.getValue() != null ? cbArmaz.getValue().trim() : "";
            int dias = spinDias.getValue();

            if (isEdit) {
                produtoParaEditar.setNome(nome);
                produtoParaEditar.setSif(sif);
                produtoParaEditar.setArmazenamento(armazenamento);
                produtoParaEditar.setDiasValidade(dias);
                resultado[0] = produtoParaEditar;
            } else {
                resultado[0] = new Produto(nome, sif, armazenamento, dias);
            }

            stage.close();
        };

        btnSalvar.setOnAction(e -> salvarAction.run());
        btnCancel.setOnAction(e -> stage.close());

        root.getChildren().addAll(header, grid, lblError, btnBox);

        Scene scene = new Scene(root, 460, 320);
        scene.setFill(javafx.scene.paint.Color.web("#E2DDD6"));
        String css = Objects.requireNonNull(ProdutoDialogFX.class.getResource("/css/style.css")).toExternalForm();
        scene.getStylesheets().add(css);

        stage.setScene(scene);
        stage.centerOnScreen();
        WindowsDarkThemeHelper.applyDarkTitleBar(stage);
        stage.showAndWait();

        return Optional.ofNullable(resultado[0]);
    }
}
