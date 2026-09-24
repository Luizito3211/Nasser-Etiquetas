package com.nasser.etiqueta.gui;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Utilitário nativo para ativar a barra de título escura no Windows 10 e 11,
 * eliminando a barra branca nativa do sistema operacional através do DWM.
 */
public final class WindowsDarkThemeHelper {

    private WindowsDarkThemeHelper() {}

    private interface Dwmapi extends StdCallLibrary {
        Dwmapi INSTANCE = Native.load("dwmapi", Dwmapi.class, W32APIOptions.DEFAULT_OPTIONS);

        HRESULT DwmSetWindowAttribute(HWND hwnd, int dwAttribute, IntByReference pvAttribute, int cbAttribute);
    }

    private interface User32Extra extends StdCallLibrary {
        User32Extra INSTANCE = Native.load("user32", User32Extra.class, W32APIOptions.DEFAULT_OPTIONS);

        HWND FindWindow(String lpClassName, String lpWindowName);
    }

    private static final int DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1 = 19;
    private static final int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;

    /**
     * Aplica o tema escuro na barra de título do Stage se estiver em ambiente Windows.
     */
    public static void applyDarkTitleBar(Stage stage) {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("win")) {
            return;
        }

        stage.showingProperty().addListener((obs, oldVal, showing) -> {
            if (showing) {
                Platform.runLater(() -> apply(stage));
            }
        });

        if (stage.isShowing()) {
            Platform.runLater(() -> apply(stage));
        }
    }

    private static void apply(Stage stage) {
        try {
            String title = stage.getTitle();
            if (title == null || title.isBlank()) {
                return;
            }

            HWND hwnd = User32Extra.INSTANCE.FindWindow(null, title);
            if (hwnd != null) {
                IntByReference darkMode = new IntByReference(1);
                // Windows 11 e Windows 10 (20H1+) usam atributo 20
                HRESULT res = Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, darkMode, 4);
                if (res != null && res.intValue() != 0) {
                    // Windows 10 (1809 - 1909) usam atributo 19
                    Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1, darkMode, 4);
                }
            }
        } catch (Throwable ignored) {
            // Falha silenciosa caso não tenha privilégios ou esteja em outra versão
        }
    }
}
