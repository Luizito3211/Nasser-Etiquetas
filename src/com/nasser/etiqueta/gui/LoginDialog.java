package com.nasser.etiqueta.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Dialogo modal de login/identificação inicial.
 * Estilizado com o tema Dark Minimalista.
 */
public class LoginDialog extends JDialog {

    private JTextField tfName;
    private JButton btnLogin;
    private String loggedUser = null;
    private boolean cancelled = true;

    public LoginDialog(Frame parent) {
        super(parent, "Identificação do Operador", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Estiliza JDialog
        DarkThemeHelper.styleDialog(this);

        initComponents();
        setupLayout();
        setupEvents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        tfName = new JTextField(20);
        tfName.setFont(new Font("SansSerif", Font.PLAIN, 16));
        DarkThemeHelper.styleTextField(tfName);
        
        btnLogin = new JButton("Acessar / Continuar");
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 14));
        DarkThemeHelper.styleButton(btnLogin, DarkThemeHelper.ACCENT_BLUE, Color.WHITE);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setMargin(new Insets(8, 15, 8, 15));
    }

    private void setupLayout() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        DarkThemeHelper.stylePanel(contentPanel, DarkThemeHelper.BACKGROUND);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Título/Instrução
        JLabel lblTitle = new JLabel("Controle de Acesso - Cozinha");
        DarkThemeHelper.styleLabel(lblTitle, DarkThemeHelper.TEXT_PRIMARY, Font.BOLD, 18);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(lblTitle, gbc);

        JLabel lblDescription = new JLabel("Informe seu nome para iniciar o turno de etiquetas:");
        DarkThemeHelper.styleLabel(lblDescription, DarkThemeHelper.TEXT_MUTED, Font.PLAIN, 12);
        gbc.gridy = 1;
        contentPanel.add(lblDescription, gbc);

        // Campo de entrada
        JLabel lblName = new JLabel("Responsável:");
        DarkThemeHelper.styleLabel(lblName, DarkThemeHelper.TEXT_PRIMARY, Font.BOLD, 14);
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        contentPanel.add(lblName, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        contentPanel.add(tfName, gbc);

        // Botão de acesso
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        contentPanel.add(btnLogin, gbc);

        add(contentPanel);
    }

    private void setupEvents() {
        btnLogin.addActionListener(e -> attemptLogin());

        tfName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    attemptLogin();
                }
            }
        });
    }

    private void attemptLogin() {
        String name = tfName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, informe o nome do responsável para continuar.",
                    "Campo Obrigatório",
                    JOptionPane.WARNING_MESSAGE);
            tfName.requestFocus();
            return;
        }

        this.loggedUser = name;
        this.cancelled = false;
        dispose();
    }

    public String getLoggedUser() {
        return loggedUser;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
