package com.nasser.etiqueta.service;

import com.nasser.etiqueta.gui.WindowUtils;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

/**
 * Gerenciador central de ambiente, caminhos de persistência e logging do Nasser Etiquetas.
 * Migra dados persistentes para %APPDATA%\Nasser Etiquetas e configura logs com rotação.
 */
public final class AppEnvironment {

    private static final Logger LOGGER = Logger.getLogger(AppEnvironment.class.getName());
    private static final Path APP_DATA_DIR;
    private static final Path CONFIG_DIR;
    private static final Path LOGS_DIR;

    private static volatile boolean initialized = false;
    private static FileHandler fileHandler;

    static {
        String appData = System.getenv("APPDATA");
        Path base;
        if (appData != null && !appData.isBlank()) {
            base = Paths.get(appData, "Nasser Etiquetas");
        } else {
            String userHome = System.getProperty("user.home", ".");
            base = Paths.get(userHome, ".nasser-etiquetas");
        }
        APP_DATA_DIR = base;
        CONFIG_DIR = base.resolve("config");
        LOGS_DIR = base.resolve("logs");
    }

    private AppEnvironment() {
    }

    public static Path getAppDataDir() {
        return APP_DATA_DIR;
    }

    public static Path getConfigDir() {
        return CONFIG_DIR;
    }

    public static Path getLogsDir() {
        return LOGS_DIR;
    }

    public static Path getConfigFile(String fileName) {
        return CONFIG_DIR.resolve(fileName);
    }

    public static Path getLogFile() {
        return LOGS_DIR.resolve("nasser-etiquetas.0.log");
    }

    /**
     * Inicializa a estrutura de diretórios e o sistema de logging em arquivo.
     */
    public static synchronized void init() {
        if (initialized) {
            return;
        }

        try {
            Files.createDirectories(CONFIG_DIR);
            Files.createDirectories(LOGS_DIR);

            setupLogging();
            installUncaughtExceptionHandler();

            initialized = true;
            LOGGER.info("Ambiente Nasser Etiquetas inicializado com sucesso.");
            LOGGER.info("Diretório de dados: " + CONFIG_DIR.toAbsolutePath());
            LOGGER.info("Diretório de logs: " + LOGS_DIR.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Erro ao inicializar AppEnvironment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void setupLogging() {
        try {
            String logPattern = LOGS_DIR.resolve("nasser-etiquetas.%g.log").toString();
            // Rotação: arquivos de até 2MB, mantendo até 5 arquivos históricos
            fileHandler = new FileHandler(logPattern, 2 * 1024 * 1024, 5, true);
            fileHandler.setEncoding(StandardCharsets.UTF_8.name());
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new Formatter() {
                private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

                @Override
                public String format(LogRecord record) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("[").append(LocalDateTime.now().format(dtf)).append("] ");
                    sb.append("[").append(String.format("%-7s", record.getLevel())).append("] ");
                    sb.append("[").append(record.getLoggerName() != null ? record.getLoggerName() : "App").append("] ");
                    sb.append(formatMessage(record));
                    sb.append(System.lineSeparator());
                    if (record.getThrown() != null) {
                        java.io.StringWriter sw = new java.io.StringWriter();
                        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                        record.getThrown().printStackTrace(pw);
                        sb.append(sw);
                    }
                    return sb.toString();
                }
            });

            Logger rootLogger = Logger.getLogger("");
            rootLogger.addHandler(fileHandler);
            rootLogger.setLevel(Level.INFO);
        } catch (IOException e) {
            System.err.println("Não foi possível configurar FileHandler para logging: " + e.getMessage());
        }
    }

    /**
     * Registra o tratador global de exceções não tratadas para registrar no log
     * e exibir uma mensagem amigável sem stack trace ao operador.
     */
    public static void installUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Logger appLogger = Logger.getLogger("com.nasser.etiqueta.UncaughtExceptionHandler");
            appLogger.log(Level.SEVERE, "Exceção não tratada na thread '" + thread.getName() + "': " + throwable.getMessage(), throwable);

            try {
                if (Platform.isFxApplicationThread()) {
                    showErrorDialog(throwable);
                } else {
                    Platform.runLater(() -> showErrorDialog(throwable));
                }
            } catch (Throwable t) {
                System.err.println("Falha ao exibir diálogo de erro fatal: " + t.getMessage());
            }
        });
    }

    private static void showErrorDialog(Throwable throwable) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro Inesperado - Nasser Etiquetas");
            alert.setHeaderText("Ocorreu uma falha no sistema");

            String errorMsg = throwable != null && throwable.getMessage() != null && !throwable.getMessage().isBlank()
                    ? throwable.getMessage()
                    : "Falha interna no processamento de interface ou impressão.";

            alert.setContentText(
                    "Ocorreu um erro inesperado durante a execução da operação.\n\n"
                    + "Detalhes da ocorrência:\n"
                    + "• " + errorMsg + "\n\n"
                    + "Os detalhes técnicos completos foram gravados no log:\n"
                    + getLogsDir().toAbsolutePath() + "\n\n"
                    + "Se a falha persistir, por favor contate o suporte técnico."
            );

            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            if (stage != null) {
                WindowUtils.applyAppIcon(stage);
            }
            alert.showAndWait();
        } catch (Throwable ignored) {
        }
    }
}
