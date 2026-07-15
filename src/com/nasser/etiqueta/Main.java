package com.nasser.etiqueta;

import com.nasser.etiqueta.gui.LoginDialog;
import com.nasser.etiqueta.gui.MainFrame;
import com.nasser.etiqueta.gui.DarkThemeHelper;
import com.nasser.etiqueta.service.ElginPrinterService;
import com.nasser.etiqueta.service.PersistenceService;
import javax.swing.*;
import java.awt.Color;

/**
 * Ponto de entrada (Main) do aplicativo de Etiquetas de Validade.
 * Inicializa a persistência, aplica o Tema Dark do UIManager e gerencia o fluxo.
 */
public class Main {

    public static void main(String[] args) {
        // Inicializa UIManager com o Tema Dark antes de instanciar componentes Swing
        applyDarkThemeUIManager();

        // Executa na Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // Inicializa as camadas de serviço
            PersistenceService persistenceService = new PersistenceService();
            ElginPrinterService printerService = new ElginPrinterService();

            // Abre diálogo de identificação do operador
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);

            if (login.isCancelled() || login.getLoggedUser() == null) {
                System.out.println("Login cancelado pelo operador. Encerrando.");
                System.exit(0);
            }

            // Abre painel de produtos passando o operador ativo
            String operador = login.getLoggedUser();
            MainFrame mainFrame = new MainFrame(operador, persistenceService, printerService);
            mainFrame.setVisible(true);
        });
    }

    /**
     * Aplica overrides de cores escuras no UIManager padrão do Java SE,
     * garantindo que caixas de diálogos, modais e componentes nativos tenham visual dark consistente.
     */
    private static void applyDarkThemeUIManager() {
        UIManager.put("Panel.background", DarkThemeHelper.BACKGROUND);
        UIManager.put("Label.foreground", DarkThemeHelper.TEXT_PRIMARY);
        UIManager.put("Label.background", DarkThemeHelper.BACKGROUND);

        UIManager.put("Button.background", DarkThemeHelper.COMPONENT_BG);
        UIManager.put("Button.foreground", DarkThemeHelper.TEXT_PRIMARY);
        UIManager.put("Button.select", DarkThemeHelper.CARD_BG);
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));

        UIManager.put("TextField.background", DarkThemeHelper.COMPONENT_BG);
        UIManager.put("TextField.foreground", DarkThemeHelper.TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", DarkThemeHelper.TEXT_PRIMARY);

        UIManager.put("ComboBox.background", DarkThemeHelper.COMPONENT_BG);
        UIManager.put("ComboBox.foreground", DarkThemeHelper.TEXT_PRIMARY);

        UIManager.put("Spinner.background", DarkThemeHelper.COMPONENT_BG);
        UIManager.put("Spinner.foreground", DarkThemeHelper.TEXT_PRIMARY);

        UIManager.put("CheckBox.background", DarkThemeHelper.BACKGROUND);
        UIManager.put("CheckBox.foreground", DarkThemeHelper.TEXT_PRIMARY);

        UIManager.put("ScrollPane.background", DarkThemeHelper.BACKGROUND);
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());

        UIManager.put("OptionPane.background", DarkThemeHelper.BACKGROUND);
        UIManager.put("OptionPane.messageForeground", DarkThemeHelper.TEXT_PRIMARY);
        UIManager.put("OptionPane.background", DarkThemeHelper.BACKGROUND);
        
        UIManager.put("TitledBorder.titleColor", DarkThemeHelper.TEXT_PRIMARY);
    }
}
