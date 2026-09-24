package com.nasser.etiqueta;

import atlantafx.base.theme.CupertinoDark;
import com.nasser.etiqueta.gui.LoginDialogFX;
import com.nasser.etiqueta.gui.MainController;
import com.nasser.etiqueta.gui.WindowUtils;
import com.nasser.etiqueta.service.ElginPrinterService;
import com.nasser.etiqueta.service.PersistenceService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.Optional;

/**
 * Ponto de entrada JavaFX da aplicação Nasser Etiquetas.
 * Configura o tema AtlantaFX Cupertino Dark e carrega os estilos Apple Dark Matte.
 */
public class MainApp extends Application {

    private PersistenceService persistenceService;
    private ElginPrinterService printerService;

    @Override
    public void init() {
        this.persistenceService = new PersistenceService();
        this.printerService = new ElginPrinterService();
    }

    @Override
    public void start(Stage primaryStage) {
        // Ativa o tema base AtlantaFX Cupertino Dark
        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());

        // Identificação inicial do operador via diálogo moderno
        Optional<String> operadorOpt = LoginDialogFX.solicitarOperador(null);
        if (operadorOpt.isEmpty()) {
            System.out.println("Login cancelado pelo operador. Encerrando.");
            System.exit(0);
            return;
        }

        String operador = operadorOpt.get();

        try {
            // Suporte a decorações integradas / estilo unificado
            try {
                primaryStage.initStyle(javafx.stage.StageStyle.UNIFIED);
            } catch (Exception ignored) {
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1020, 720);
            scene.setFill(javafx.scene.paint.Color.web("#E2DDD6"));

            // Carrega CSS customizado da aplicação
            String css = Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm();
            scene.getStylesheets().add(css);

            primaryStage.setTitle("Nasser Etiquetas - Gestão e Impressão");
            WindowUtils.applyAppIcon(primaryStage);
            primaryStage.setMinWidth(960);
            primaryStage.setMinHeight(640);
            primaryStage.setScene(scene);

            Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
            primaryStage.setX(visualBounds.getMinX());
            primaryStage.setY(visualBounds.getMinY());
            primaryStage.setWidth(visualBounds.getWidth());
            primaryStage.setHeight(visualBounds.getHeight());

            // A cena já está vinculada ao Stage antes de inicializar dados e componentes dinâmicos.
            root.applyCss();
            root.layout();

            MainController controller = loader.getController();
            controller.init(operador, persistenceService, printerService, primaryStage);

            // Recalcula a geometria após a criação dos cards, antes da primeira pintura.
            root.applyCss();
            root.layout();

            // Ativa barra de título escura no Windows
            com.nasser.etiqueta.gui.WindowsDarkThemeHelper.applyDarkTitleBar(primaryStage);

            primaryStage.show();
            Platform.runLater(() -> {
                primaryStage.setMaximized(true);
                scene.getRoot().applyCss();
                scene.getRoot().requestLayout();
                scene.getRoot().layout();
                primaryStage.toFront();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
