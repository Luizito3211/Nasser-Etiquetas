package com.nasser.etiqueta.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

public final class DateSelectionDialog {

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DateSelectionDialog() {
    }

    public static Optional<LocalDate> show(Window owner) {
        LocalDate today = LocalDate.now();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            stage.initOwner(owner);
        }
        WindowUtils.applyAppIcon(stage);
        stage.setTitle("Data de Fabricação das Etiquetas");
        stage.setResizable(false);

        VBox root = new VBox(14);
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(22));
        root.getStyleClass().add("date-selection-dialog");

        Label title = new Label("Data de Fabricação das Etiquetas");
        title.getStyleClass().add("date-selection-title");
        Label subtitle = new Label("Deseja imprimir com a data de hoje ou agendar para outra data?");
        subtitle.getStyleClass().add("date-selection-subtitle");

        Button todayButton = new Button("Hoje (" + today.format(DISPLAY_FORMATTER) + ")");
        todayButton.getStyleClass().addAll("button", "btn-primary");

        DatePicker datePicker = new DatePicker(today);
        datePicker.setPromptText("Selecione uma data");
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(today));
            }
        });

        todayButton.setOnAction(event -> datePicker.setValue(today));

        Button cancelButton = new Button("Cancelar");
        cancelButton.getStyleClass().add("button");
        Button confirmButton = new Button("Confirmar e Imprimir");
        confirmButton.getStyleClass().addAll("button", "btn-primary");

        final LocalDate[] result = new LocalDate[1];
        confirmButton.setOnAction(event -> {
            LocalDate selected = datePicker.getValue();
            if (selected != null && !selected.isBefore(today)) {
                result[0] = selected;
                stage.close();
            }
        });
        cancelButton.setOnAction(event -> stage.close());

        HBox dateRow = new HBox(10, todayButton, datePicker);
        dateRow.setAlignment(Pos.CENTER_LEFT);
        HBox actions = new HBox(10, cancelButton, confirmButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(title, subtitle, dateRow, actions);

        Scene scene = new Scene(root, 470, 205);
        scene.setFill(Color.web("#E2DDD6"));
        String css = Objects.requireNonNull(DateSelectionDialog.class.getResource("/css/style.css")).toExternalForm();
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.centerOnScreen();
        WindowsDarkThemeHelper.applyDarkTitleBar(stage);
        stage.showAndWait();

        return Optional.ofNullable(result[0]);
    }
}
