package com.nasser.etiqueta.gui;

import com.nasser.etiqueta.model.Produto;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialogo modal de cadastro/edição de produtos estilizado com o tema Dark.
 */
public class ProdutoDialog extends JDialog {

    private JTextField tfNome;
    private JTextField tfSif;
    private JComboBox<String> cbArmazenamento;
    private JSpinner spinnerDias;
    private JButton btnSalvar;
    private JButton btnCancelar;

    private Produto produtoExistente = null;
    private Produto produtoResult = null;
    private boolean salvou = false;

    public ProdutoDialog(Frame parent) {
        super(parent, "Cadastrar Novo Produto", true);
        initDialog(parent);
    }

    public ProdutoDialog(Frame parent, Produto produtoParaEditar) {
        super(parent, "Editar Produto", true);
        this.produtoExistente = produtoParaEditar;
        initDialog(parent);
        preencherDados();
    }

    private void initDialog(Frame parent) {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Estiliza o JDialog
        DarkThemeHelper.styleDialog(this);

        initComponents();
        setupLayout();
        setupEvents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        tfNome = new JTextField(20);
        tfNome.setFont(new Font("SansSerif", Font.PLAIN, 14));
        DarkThemeHelper.styleTextField(tfNome);

        tfSif = new JTextField(20);
        tfSif.setFont(new Font("SansSerif", Font.PLAIN, 14));
        DarkThemeHelper.styleTextField(tfSif);

        String[] opcoesArmazenamento = {
                "Refrigerado (até 4°C)",
                "Congelado (abaixo de -12°C)",
                "Temperatura Ambiente"
        };
        cbArmazenamento = new JComboBox<>(opcoesArmazenamento);
        cbArmazenamento.setEditable(true);
        cbArmazenamento.setFont(new Font("SansSerif", Font.PLAIN, 14));
        DarkThemeHelper.styleComboBox(cbArmazenamento);

        spinnerDias = new JSpinner(new SpinnerNumberModel(3, 1, 365, 1));
        spinnerDias.setFont(new Font("SansSerif", Font.PLAIN, 14));
        DarkThemeHelper.styleSpinner(spinnerDias);

        btnSalvar = new JButton(produtoExistente == null ? "Salvar Produto" : "Atualizar Produto");
        btnSalvar.setFont(new Font("SansSerif", Font.BOLD, 14));
        DarkThemeHelper.styleButton(btnSalvar, DarkThemeHelper.GREEN_ACCENT, Color.WHITE);
        btnSalvar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("SansSerif", Font.PLAIN, 14));
        DarkThemeHelper.styleButton(btnCancelar, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.TEXT_PRIMARY);
        btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void preencherDados() {
        if (produtoExistente != null) {
            tfNome.setText(produtoExistente.getNome());
            tfSif.setText(produtoExistente.getSif());
            cbArmazenamento.setSelectedItem(produtoExistente.getArmazenamento());
            spinnerDias.setValue(produtoExistente.getDiasValidade());
        }
    }

    private void setupLayout() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        DarkThemeHelper.stylePanel(contentPanel, DarkThemeHelper.BACKGROUND);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        int row = 0;

        JLabel lblTitle = new JLabel(produtoExistente == null ? "Cadastrar Novo Produto" : "Editar Dados do Produto");
        DarkThemeHelper.styleLabel(lblTitle, DarkThemeHelper.TEXT_PRIMARY, Font.BOLD, 16);
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 8, 15, 8);
        contentPanel.add(lblTitle, gbc);

        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridwidth = 1;

        // Nome
        JLabel lblNome = new JLabel("Nome do Produto: *");
        DarkThemeHelper.styleLabel(lblNome, DarkThemeHelper.TEXT_PRIMARY, Font.PLAIN, 13);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        contentPanel.add(lblNome, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        contentPanel.add(tfNome, gbc);
        row++;

        // SIF
        JLabel lblSif = new JLabel("S.I.F. (Opcional):");
        DarkThemeHelper.styleLabel(lblSif, DarkThemeHelper.TEXT_PRIMARY, Font.PLAIN, 13);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        contentPanel.add(lblSif, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        contentPanel.add(tfSif, gbc);
        row++;

        // Armazenamento
        JLabel lblArmaz = new JLabel("Armazenamento:");
        DarkThemeHelper.styleLabel(lblArmaz, DarkThemeHelper.TEXT_PRIMARY, Font.PLAIN, 13);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        contentPanel.add(lblArmaz, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        contentPanel.add(cbArmazenamento, gbc);
        row++;

        // Dias Validade
        JLabel lblVal = new JLabel("Dias de Validade:");
        DarkThemeHelper.styleLabel(lblVal, DarkThemeHelper.TEXT_PRIMARY, Font.PLAIN, 13);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        contentPanel.add(lblVal, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        contentPanel.add(spinnerDias, gbc);
        row++;

        // Botões
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(btnCancelar);
        buttonsPanel.add(btnSalvar);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(20, 8, 0, 8);
        contentPanel.add(buttonsPanel, gbc);

        add(contentPanel);
    }

    private void setupEvents() {
        btnCancelar.addActionListener(e -> dispose());
        btnSalvar.addActionListener(e -> salvarProduto());
    }

    private void salvarProduto() {
        String nome = tfNome.getText().trim();
        String sif = tfSif.getText().trim();
        Object storageObj = cbArmazenamento.getSelectedItem();
        String armazenamento = storageObj != null ? storageObj.toString().trim() : "";
        int dias = (Integer) spinnerDias.getValue();

        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "O nome do produto é um campo obrigatório.",
                    "Erro de Validação",
                    JOptionPane.WARNING_MESSAGE);
            tfNome.requestFocus();
            return;
        }

        if (produtoExistente != null) {
            produtoExistente.setNome(nome);
            produtoExistente.setSif(sif);
            produtoExistente.setArmazenamento(armazenamento);
            produtoExistente.setDiasValidade(dias);
            this.produtoResult = produtoExistente;
        } else {
            this.produtoResult = new Produto(nome, sif, armazenamento, dias);
        }

        this.salvou = true;
        dispose();
    }

    public Produto getProdutoCriado() {
        return produtoResult;
    }

    public boolean wasSaved() {
        return salvou;
    }
}
