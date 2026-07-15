package com.nasser.etiqueta.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Utilitário de estilização para o tema Dark Minimalista da aplicação.
 * Fornece cores consistentes e métodos de colorização para componentes Swing.
 */
public class DarkThemeHelper {

    // Paleta de Cores
    public static final Color BACKGROUND = new Color(0x121212);        // Fundo principal escuro
    public static final Color PANEL_BG = new Color(0x1E1E1E);          // Fundo de painéis e cabeçalhos
    public static final Color CARD_BG = new Color(0x252525);           // Fundo de cards
    public static final Color TEXT_PRIMARY = new Color(0xE0E0E0);      // Texto principal (Branco/Cinza claro)
    public static final Color TEXT_MUTED = new Color(0xA0A0A0);        // Texto secundário (Cinza médio)
    
    public static final Color COMPONENT_BG = new Color(0x2D2D2D);      // Fundo de botões, inputs, combos
    public static final Color BORDER_COLOR = new Color(0x3E3E3E);      // Cor de bordas suaves
    
    // Cores de Acento
    public static final Color GREEN_ACCENT = new Color(0x4CAF50);      // Verde suave para confirmação / Imprimir
    public static final Color ACCENT_BLUE = new Color(0x2196F3);       // Azul suave
    public static final Color RED_ACCENT = new Color(0xE53935);        // Vermelho suave para Excluir
    public static final Color ORANGE_ACCENT = new Color(0xFF5722);     // Laranja suave para lotes

    public static final Border SMOOTH_BORDER = new LineBorder(BORDER_COLOR, 1, true);

    public static void styleFrame(JFrame frame) {
        frame.getContentPane().setBackground(BACKGROUND);
    }

    public static void styleDialog(JDialog dialog) {
        dialog.getContentPane().setBackground(BACKGROUND);
    }

    public static void stylePanel(JPanel panel, Color bg) {
        panel.setBackground(bg);
    }

    public static void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorder(SMOOTH_BORDER);
        btn.setFocusPainted(false);
    }

    public static void styleTextField(JTextField tf) {
        tf.setBackground(COMPONENT_BG);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setBorder(SMOOTH_BORDER);
    }

    public static void styleComboBox(JComboBox<?> cb) {
        cb.setBackground(COMPONENT_BG);
        cb.setForeground(TEXT_PRIMARY);
        cb.setBorder(SMOOTH_BORDER);
        // Customiza o renderizador padrão se possível
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setBackground(isSelected ? ACCENT_BLUE : COMPONENT_BG);
                c.setForeground(TEXT_PRIMARY);
                return c;
            }
        });
    }

    public static void styleSpinner(JSpinner spin) {
        spin.setBackground(COMPONENT_BG);
        spin.setForeground(TEXT_PRIMARY);
        JComponent editor = spin.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField ftf = ((JSpinner.DefaultEditor) editor).getTextField();
            ftf.setBackground(COMPONENT_BG);
            ftf.setForeground(TEXT_PRIMARY);
            ftf.setCaretColor(TEXT_PRIMARY);
        }
        spin.setBorder(SMOOTH_BORDER);
    }

    public static void styleLabel(JLabel lbl, Color fg, int fontStyle, int size) {
        lbl.setForeground(fg);
        lbl.setFont(new Font("SansSerif", fontStyle, size));
    }
}
