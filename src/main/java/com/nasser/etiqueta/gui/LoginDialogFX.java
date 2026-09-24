package com.nasser.etiqueta.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Objects;
import java.util.Optional;

/**
 * Diálogo modal moderno de Identificação do Operador.
 * Alinhado ao padrão visual Apple Dark Matte.
 */
public class LoginDialogFX {

    public static Optional<String> solicitarOperador(Window owner) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            stage.initOwner(owner);
        }
        WindowUtils.applyAppIcon(stage);
        stage.setTitle("Identificação do Operador");
        stage.setResizable(false);

        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24, 28, 24, 28));
        root.setStyle("-fx-background-color: #EFEBE6; -fx-background-radius: 12px;");

        Label lblTitle = new Label("Controle de Acesso");
        lblTitle.setStyle("-fx-text-fill: #23201D; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label lblSub = new Label("Informe seu nome para iniciar o turno de etiquetas:");
        lblSub.setStyle("-fx-text-fill: #6E665E; -fx-font-size: 12px;");

        VBox titleBox = new VBox(6, lblTitle, lblSub);
        titleBox.setAlignment(Pos.CENTER);

        // Campo de texto
        TextField tfName = new TextField();
        tfName.setPromptText("Nome do responsável...");
        tfName.setPrefHeight(38);
        tfName.setStyle("-fx-font-size: 14px;");

        // Mensagem de validação
        Label lblError = new Label("");
        lblError.setStyle("-fx-text-fill: #C82B28; -fx-font-size: 11px;");
        lblError.setVisible(false);

        // Botões
        Button btnCancel = new Button("Cancelar");
        btnCancel.getStyleClass().add("button");
        btnCancel.setPrefHeight(36);

        Button btnConfirm = new Button("Acessar / Continuar");
        btnConfirm.getStyleClass().addAll("button", "btn-primary");
        btnConfirm.setPrefHeight(36);

        HBox btnBox = new HBox(10, btnCancel, btnConfirm);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(btnConfirm, Priority.ALWAYS);

        final String[] result = new String[1];

        Runnable submit = () -> {
            String val = tfName.getText().trim();
            if (val.isEmpty()) {
                lblError.setText("Por favor, informe seu nome.");
                lblError.setVisible(true);
                tfName.requestFocus();
                return;
            }
            result[0] = val;
            stage.close();
        };

        btnConfirm.setOnAction(e -> submit.run());
        btnCancel.setOnAction(e -> stage.close());

        tfName.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                submit.run();
            }
        });

        root.getChildren().addAll(titleBox, tfName, lblError, btnBox);

        Scene scene = new Scene(root, 380, 240);
        scene.setFill(javafx.scene.paint.Color.web("#E2DDD6"));
        String css = Objects.requireNonNull(LoginDialogFX.class.getResource("/css/style.css")).toExternalForm();
        scene.getStylesheets().add(css);

        stage.setScene(scene);
        stage.centerOnScreen();
        WindowsDarkThemeHelper.applyDarkTitleBar(stage);
        stage.showAndWait();

        return Optional.ofNullable(result[0]);
    }
}
