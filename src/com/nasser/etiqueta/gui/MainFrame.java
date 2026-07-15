package com.nasser.etiqueta.gui;

import com.nasser.etiqueta.model.EtiquetaData;
import com.nasser.etiqueta.model.Produto;
import com.nasser.etiqueta.model.ElementoLayout;
import com.nasser.etiqueta.service.ElginPrinterService;
import com.nasser.etiqueta.service.PersistenceService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tela principal do sistema em Tema Dark.
 * Exibe lista dinâmica de produtos em cards cinza escuro, controles rápidos
 * de quantidade e rodapé com botão de impressão em lote destacada.
 */
public class MainFrame extends JFrame {

    private final PersistenceService persistenceService;
    private final ElginPrinterService printerService;

    // Estado da Sessão
    private String responsavelAtual;
    private List<Produto> produtos;
    private final Map<Produto, JTextField> quantityFields = new HashMap<>();

    // Componentes de Interface
    private JLabel lblUser;
    private JButton btnAlterarUser;
    private JButton btnAddProduct;
    private JButton btnConfigLayout;
    
    private JPanel productsContainer;
    private JScrollPane scrollPane;
    private JLabel lblStatus;
    
    private JButton btnImprimirSelecionados;
    private JLabel lblImpressoraPadrane;

    public MainFrame(String responsavelInicial, PersistenceService persistence, ElginPrinterService printer) {
        this.responsavelAtual = responsavelInicial;
        this.persistenceService = persistence;
        this.printerService = printer;

        setTitle("Nasser Etiquetas - Painel Dark");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);

        // Estiliza o JFrame
        DarkThemeHelper.styleFrame(this);

        this.produtos = persistenceService.loadProdutos();

        initComponents();
        setupLayout();
        setupEvents();
        
        rebuildProductList();
        updatePrinterStatus();
    }

    private void initComponents() {
        // Cabeçalho
        btnAddProduct = new JButton("+ Novo Produto");
        btnAddProduct.setFont(new Font("SansSerif", Font.BOLD, 14));
        DarkThemeHelper.styleButton(btnAddProduct, DarkThemeHelper.GREEN_ACCENT, Color.WHITE);
        btnAddProduct.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnConfigLayout = new JButton("⚙ Configuração do Layout");
        btnConfigLayout.setFont(new Font("SansSerif", Font.BOLD, 12));
        DarkThemeHelper.styleButton(btnConfigLayout, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.TEXT_PRIMARY);
        btnConfigLayout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        lblUser = new JLabel("Responsável: " + responsavelAtual);
        DarkThemeHelper.styleLabel(lblUser, DarkThemeHelper.TEXT_PRIMARY, Font.BOLD, 13);

        btnAlterarUser = new JButton("Trocar");
        btnAlterarUser.setFont(new Font("SansSerif", Font.PLAIN, 10));
        DarkThemeHelper.styleButton(btnAlterarUser, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.TEXT_MUTED);
        btnAlterarUser.setMargin(new Insets(2, 5, 2, 5));
        btnAlterarUser.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Container Central
        productsContainer = new JPanel();
        productsContainer.setLayout(new GridBagLayout());
        DarkThemeHelper.stylePanel(productsContainer, DarkThemeHelper.BACKGROUND);

        scrollPane = new JScrollPane(productsContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(DarkThemeHelper.BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Rodapé de Lote
        btnImprimirSelecionados = new JButton("IMPRIMIR SELECIONADOS (LOTE)");
        btnImprimirSelecionados.setFont(new Font("SansSerif", Font.BOLD, 18));
        DarkThemeHelper.styleButton(btnImprimirSelecionados, DarkThemeHelper.ORANGE_ACCENT, Color.WHITE);
        btnImprimirSelecionados.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnImprimirSelecionados.setMargin(new Insets(12, 30, 12, 30));

        lblImpressoraPadrane = new JLabel("Impressora: Não selecionada");
        DarkThemeHelper.styleLabel(lblImpressoraPadrane, DarkThemeHelper.TEXT_MUTED, Font.BOLD, 12);

        // Barra de status
        lblStatus = new JLabel("Pronto");
        DarkThemeHelper.styleLabel(lblStatus, DarkThemeHelper.TEXT_MUTED, Font.ITALIC, 12);
        lblStatus.setBorder(new EmptyBorder(5, 10, 5, 10));
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        DarkThemeHelper.stylePanel(mainPanel, DarkThemeHelper.BACKGROUND);
        
        // 1. Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, DarkThemeHelper.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        DarkThemeHelper.stylePanel(headerPanel, DarkThemeHelper.PANEL_BG);

        headerPanel.add(btnAddProduct, BorderLayout.WEST);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        userPanel.setOpaque(false);
        userPanel.add(lblUser);
        userPanel.add(btnAlterarUser);
        headerPanel.add(userPanel, BorderLayout.CENTER);

        headerPanel.add(btnConfigLayout, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Center Panel
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Panel
        JPanel bottomControlPanel = new JPanel(new BorderLayout(5, 5));
        bottomControlPanel.setOpaque(false);
        
        JPanel batchPanel = new JPanel(new BorderLayout(15, 0));
        batchPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        DarkThemeHelper.stylePanel(batchPanel, DarkThemeHelper.PANEL_BG);
        batchPanel.add(lblImpressoraPadrane, BorderLayout.WEST);
        batchPanel.add(btnImprimirSelecionados, BorderLayout.CENTER);
        
        bottomControlPanel.add(batchPanel, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, DarkThemeHelper.BORDER_COLOR));
        DarkThemeHelper.stylePanel(statusPanel, DarkThemeHelper.PANEL_BG);
        statusPanel.add(lblStatus, BorderLayout.WEST);
        bottomControlPanel.add(statusPanel, BorderLayout.SOUTH);

        mainPanel.add(bottomControlPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void setupEvents() {
        // Novo Produto
        btnAddProduct.addActionListener(e -> {
            ProdutoDialog dialog = new ProdutoDialog(this);
            dialog.setVisible(true);
            if (dialog.wasSaved() && dialog.getProdutoCriado() != null) {
                produtos.add(dialog.getProdutoCriado());
                persistenceService.saveProdutos(produtos);
                rebuildProductList();
                setStatus("Produto '" + dialog.getProdutoCriado().getNome() + "' cadastrado.", false);
            }
        });

        // Configuração de Layout Visual
        btnConfigLayout.addActionListener(e -> {
            LayoutEditorDialog dialog = new LayoutEditorDialog(this, persistenceService, printerService);
            dialog.setVisible(true);
            updatePrinterStatus();
            rebuildProductList(); // Redesenha a lista caso precise carregar de volta
        });

        // Trocar de Operador
        btnAlterarUser.addActionListener(e -> {
            LoginDialog dialog = new LoginDialog(this);
            dialog.setVisible(true);
            if (!dialog.isCancelled() && dialog.getLoggedUser() != null) {
                this.responsavelAtual = dialog.getLoggedUser();
                lblUser.setText("Responsável: " + responsavelAtual);
                setStatus("Responsável alterado para '" + responsavelAtual + "'.", false);
            }
        });

        // Botão Imprimir Lote
        btnImprimirSelecionados.addActionListener(e -> dispararImpressaoLote());
    }

    private void updatePrinterStatus() {
        String printerName = persistenceService.getSelectedPrinter();
        if (printerName == null || printerName.isBlank()) {
            lblImpressoraPadrane.setText("Impressora: Não configurada ❌");
            lblImpressoraPadrane.setForeground(DarkThemeHelper.RED_ACCENT);
        } else {
            lblImpressoraPadrane.setText("Impressora: " + printerName + "  ");
            lblImpressoraPadrane.setForeground(DarkThemeHelper.GREEN_ACCENT);
        }
    }

    private void rebuildProductList() {
        productsContainer.removeAll();
        quantityFields.clear();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 15, 8, 15);

        if (produtos.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setOpaque(false);
            
            JLabel lblMsg = new JLabel("Nenhum produto cadastrado. Clique em '+ Novo Produto'.");
            DarkThemeHelper.styleLabel(lblMsg, DarkThemeHelper.TEXT_MUTED, Font.BOLD, 14);
            emptyPanel.add(lblMsg);
            
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            productsContainer.add(emptyPanel, gbc);
        } else {
            for (Produto p : produtos) {
                JPanel card = createProductCard(p);
                productsContainer.add(card, gbc);
                gbc.gridy++;
            }

            // Spacer
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            JPanel spacer = new JPanel();
            spacer.setOpaque(false);
            productsContainer.add(spacer, gbc);
        }

        productsContainer.revalidate();
        productsContainer.repaint();
    }

    private JPanel createProductCard(Produto p) {
        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DarkThemeHelper.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));
        DarkThemeHelper.stylePanel(card, DarkThemeHelper.CARD_BG);

        // Esquerda: Informações
        JPanel detailsPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        detailsPanel.setOpaque(false);

        JLabel lblName = new JLabel(p.getNome());
        DarkThemeHelper.styleLabel(lblName, DarkThemeHelper.TEXT_PRIMARY, Font.BOLD, 18);
        detailsPanel.add(lblName);

        String subtitle = String.format("S.I.F.: %s  |  Validade: %d dias  |  %s",
                (p.getSif() == null || p.getSif().isBlank()) ? "N/A" : p.getSif(),
                p.getDiasValidade(),
                (p.getArmazenamento() == null || p.getArmazenamento().isBlank()) ? "Geral" : p.getArmazenamento()
        );
        JLabel lblSub = new JLabel(subtitle);
        DarkThemeHelper.styleLabel(lblSub, DarkThemeHelper.TEXT_MUTED, Font.PLAIN, 12);
        detailsPanel.add(lblSub);

        card.add(detailsPanel, BorderLayout.CENTER);

        // Direita: Controles e CRUD
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        rightPanel.setOpaque(false);

        // CRUD
        JPanel crudPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        crudPanel.setOpaque(false);

        JButton btnEdit = new JButton("Editar");
        btnEdit.setFont(new Font("SansSerif", Font.PLAIN, 12));
        DarkThemeHelper.styleButton(btnEdit, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.TEXT_PRIMARY);
        btnEdit.setPreferredSize(new Dimension(75, 38));
        btnEdit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton btnDelete = new JButton("Excluir");
        btnDelete.setFont(new Font("SansSerif", Font.PLAIN, 12));
        DarkThemeHelper.styleButton(btnDelete, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.RED_ACCENT);
        btnDelete.setPreferredSize(new Dimension(75, 38));
        btnDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        crudPanel.add(btnEdit);
        crudPanel.add(btnDelete);

        // Qtd [ - ] [ 0 ] [ + ]
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        qtyPanel.setOpaque(false);

        JButton btnMinus = new JButton("-");
        btnMinus.setFont(new Font("SansSerif", Font.BOLD, 18));
        btnMinus.setPreferredSize(new Dimension(45, 38));
        DarkThemeHelper.styleButton(btnMinus, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.TEXT_PRIMARY);
        btnMinus.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JTextField tfQty = new JTextField("0", 3);
        tfQty.setHorizontalAlignment(JTextField.CENTER);
        tfQty.setFont(new Font("SansSerif", Font.BOLD, 16));
        tfQty.setPreferredSize(new Dimension(45, 38));
        DarkThemeHelper.styleTextField(tfQty);
        
        quantityFields.put(p, tfQty);

        JButton btnPlus = new JButton("+");
        btnPlus.setFont(new Font("SansSerif", Font.BOLD, 18));
        btnPlus.setPreferredSize(new Dimension(45, 38));
        DarkThemeHelper.styleButton(btnPlus, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.TEXT_PRIMARY);
        btnPlus.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        qtyPanel.add(btnMinus);
        qtyPanel.add(tfQty);
        qtyPanel.add(btnPlus);

        rightPanel.add(crudPanel);
        rightPanel.add(qtyPanel);
        card.add(rightPanel, BorderLayout.EAST);

        // Eventos
        btnMinus.addActionListener(e -> {
            try {
                int val = Integer.parseInt(tfQty.getText().trim());
                if (val > 0) {
                    tfQty.setText(String.valueOf(val - 1));
                }
            } catch (NumberFormatException ignored) {}
        });

        btnPlus.addActionListener(e -> {
            try {
                int val = Integer.parseInt(tfQty.getText().trim());
                tfQty.setText(String.valueOf(val + 1));
            } catch (NumberFormatException ignored) {}
        });

        btnEdit.addActionListener(e -> {
            ProdutoDialog dialog = new ProdutoDialog(this, p);
            dialog.setVisible(true);
            if (dialog.wasSaved() && dialog.getProdutoCriado() != null) {
                persistenceService.saveProdutos(produtos);
                rebuildProductList();
                setStatus("Produto '" + p.getNome() + "' editado.", false);
            }
        });

        btnDelete.addActionListener(e -> {
            int op = JOptionPane.showConfirmDialog(this,
                    "Deseja realmente excluir o produto '" + p.getNome() + "'?",
                    "Confirmar Exclusão",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (op == JOptionPane.YES_OPTION) {
                produtos.remove(p);
                persistenceService.saveProdutos(produtos);
                rebuildProductList();
                setStatus("Produto '" + p.getNome() + "' excluído.", false);
            }
        });

        return card;
    }

    /**
     * Coleta as quantidades maiores que 0, calcula datas no momento do clique,
     * mescla no template dinâmico de Graphics2D e envia em lote.
     */
    private void dispararImpressaoLote() {
        String printerName = persistenceService.getSelectedPrinter();
        if (printerName == null || printerName.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Nenhuma impressora padrão selecionada.\nPor favor, configure o dispositivo nas Configurações de Layout.",
                    "Impressora não Configurada",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<ElementoLayout> layoutElements = persistenceService.loadLayoutElements();
        if (layoutElements.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "O arquivo de layout visual não possui elementos cadastrados.",
                    "Layout Vazio",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean imprimiuAlgum = false;
        int totalImpressos = 0;
        StringBuilder erros = new StringBuilder();

        // 3. CÁLCULO DINÂMICO DE DATA E VALIDADE:
        // Obtém a data/hora exata do sistema operacional no momento do clique
        LocalDateTime fabricacao = LocalDateTime.now();

        for (Produto p : produtos) {
            JTextField tfQty = quantityFields.get(p);
            if (tfQty != null) {
                try {
                    int qty = Integer.parseInt(tfQty.getText().trim());
                    if (qty > 0) {
                        // Soma os dias de validade cadastrados no produto à data atual
                        LocalDateTime validade = fabricacao.plusDays(p.getDiasValidade());
                        
                        EtiquetaData data = new EtiquetaData(
                                p.getNome(),
                                (p.getSif() == null || p.getSif().isBlank()) ? "N/A" : p.getSif(),
                                (p.getArmazenamento() == null || p.getArmazenamento().isBlank()) ? "Geral" : p.getArmazenamento(),
                                fabricacao,
                                validade,
                                responsavelAtual
                        );

                        // Envia impressão binarizada
                        printerService.printVisualLabel(printerName, layoutElements, data, qty);
                        
                        imprimiuAlgum = true;
                        totalImpressos += qty;
                    }
                } catch (NumberFormatException ex) {
                    erros.append("Quantidade inválida para o produto: ").append(p.getNome()).append("\n");
                } catch (ElginPrinterService.ElginPrintException ex) {
                    erros.append("Erro ao imprimir ").append(p.getNome()).append(": ").append(ex.getMessage()).append("\n");
                }
            }
        }

        if (imprimiuAlgum) {
            // Zera contadores
            for (JTextField tf : quantityFields.values()) {
                tf.setText("0");
            }
            
            if (erros.length() > 0) {
                setStatus("Impressão concluída com alguns avisos.", true);
                JOptionPane.showMessageDialog(this,
                        "Impressão concluída (" + totalImpressos + " etiquetas) com avisos:\n" + erros,
                        "Retorno da Impressão",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                setStatus("Sucesso: " + totalImpressos + " etiqueta(s) impressa(s).", false);
                JOptionPane.showMessageDialog(this,
                        "Impressão em lote enviada à Elgin L42 com sucesso!\nTotal de etiquetas: " + totalImpressos,
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            if (erros.length() > 0) {
                JOptionPane.showMessageDialog(this,
                        "Falha ao realizar a impressão em lote:\n" + erros,
                        "Erro na Operação",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Nenhum produto selecionado (quantidade > 0).",
                        "Seleção Vazia",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void setStatus(String msg, boolean isError) {
        lblStatus.setText(msg);
        if (isError) {
            lblStatus.setForeground(DarkThemeHelper.RED_ACCENT);
        } else {
            lblStatus.setForeground(DarkThemeHelper.GREEN_ACCENT);
        }
    }
}
