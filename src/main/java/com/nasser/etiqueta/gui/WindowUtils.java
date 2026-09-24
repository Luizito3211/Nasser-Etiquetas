package com.nasser.etiqueta.gui;

import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

public final class WindowUtils {

    private WindowUtils() {
    }

    public static void applyAppIcon(Stage stage) {
        if (stage == null) {
            return;
        }

        try (InputStream stream = WindowUtils.class.getResourceAsStream("/img/LogoStage.Jpeg")) {
            if (stream == null) {
                System.err.println("Recurso /img/LogoStage.Jpeg não encontrado para o ícone do Stage.");
                return;
            }

            Image logo = new Image(stream);
            if (logo.isError()) {
                System.err.println("Erro ao decodificar /img/LogoStage.Jpeg: " + logo.getException());
                return;
            }

            stage.getIcons().clear();
            stage.getIcons().add(logo);
        } catch (Exception e) {
            System.err.println("Erro ao carregar ícone do Stage: " + e.getMessage());
        }
    }
}
