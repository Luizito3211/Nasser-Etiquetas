package com.nasser.etiqueta.gui;

import com.nasser.etiqueta.model.CategoriaProduto;
import com.nasser.etiqueta.model.EtiquetaData;
import com.nasser.etiqueta.model.Produto;
import com.nasser.etiqueta.model.ElementoLayout;
import com.nasser.etiqueta.model.Preset;
import com.nasser.etiqueta.service.ElginPrinterService;
import com.nasser.etiqueta.service.PersistenceService;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Tela principal do sistema em Tema Dark com Suporte a Pastas/Categorias,
 * Sanfona Accordion Expandível/Recolhível e Drag-and-Drop.
 */
public class MainFrame extends JFrame {

    private final PersistenceService persistenceService;
    private final ElginPrinterService printerService;

    // Estado da Sessão e Dados
    private String responsavelAtual;
    private List<CategoriaProduto> categorias;
    private final Map<Produto, JTextField> quantityFields = new HashMap<>();
    private final Map<String, Integer> quantitiesByProductId = new HashMap<>();
    private List<Preset> presets;

    // Estado de Drag and Drop
    private int dragCatIdx = -1;
    private int dragProdIdx = -1;
    private JPanel currentHoveredCatPanel = null;
    private final List<JPanel> categoryHeaderPanels = new ArrayList<>();
    private final List<JPanel> productCardPanels = new ArrayList<>();
    private JComponent currentDropTarget;
    private Border currentDropTargetBorder;

    // Componentes de Interface
    private JLabel lblUser;
    private JButton btnAlterarUser;
    private JButton btnAddCategory;
    private JButton btnAddProduct;
    private JButton btnConfigLayout;
    private JComboBox<Preset> cbPresets;
    private JButton btnSavePreset;
    private JButton btnApplyPreset;
    private JButton btnDeletePreset;

    private JPanel productsContainer;
    private JScrollPane scrollPane;
    private JLabel lblStatus;

    private JButton btnImprimirSelecionados;
    private JLabel lblImpressoraPadrane;

    public MainFrame(String responsavelInicial, PersistenceService persistence, ElginPrinterService printer) {
        this.responsavelAtual = responsavelInicial;
        this.persistenceService = persistence;
        this.printerService = printer;

        setTitle("Nasser Etiquetas - Painel de Gestão");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(950, 680));
        setLocationRelativeTo(null);

        // Estiliza o JFrame
        DarkThemeHelper.styleFrame(this);

        this.categorias = persistenceService.loadCategorias();
        this.presets = persistenceService.loadPresets();

        initComponents();
        setupLayout();
        setupEvents();

        rebuildProductList();
        updatePrinterStatus();
    }

    private void initComponents() {
        // Cabeçalho
        btnAddCategory = new JButton("+ Nova Pasta");
        btnAddCategory.setFont(new Font("SansSerif", Font.BOLD, 13));
        DarkThemeHelper.styleButton(btnAddCategory, DarkThemeHelper.ACCENT_BLUE, Color.WHITE);
        btnAddCategory.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnAddProduct = new JButton("+ Novo Produto");
        btnAddProduct.setFont(new Font("SansSerif", Font.BOLD, 13));
        DarkThemeHelper.styleButton(btnAddProduct, DarkThemeHelper.GREEN_ACCENT, Color.WHITE);
        btnAddProduct.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnConfigLayout = new JButton("⚙ Configuração do Layout");
        btnConfigLayout.setFont(new Font("SansSerif", Font.BOLD, 12));
        DarkThemeHelper.styleButton(btnConfigLayout, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.TEXT_PRIMARY);
        btnConfigLayout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        cbPresets = new JComboBox<>();
        cbPresets.setFont(new Font("SansSerif", Font.PLAIN, 12));
        DarkThemeHelper.styleComboBox(cbPresets);
        refreshPresetSelector();

        btnSavePreset = new JButton("Salvar como Preset");
        btnSavePreset.setFont(new Font("SansSerif", Font.PLAIN, 12));
        DarkThemeHelper.styleButton(btnSavePreset, DarkThemeHelper.ACCENT_BLUE, Color.WHITE);

        btnApplyPreset = new JButton("Aplicar Preset");
        btnApplyPreset.setFont(new Font("SansSerif", Font.PLAIN, 12));
        DarkThemeHelper.styleButton(btnApplyPreset, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.TEXT_PRIMARY);

        btnDeletePreset = new JButton("Excluir Preset");
        btnDeletePreset.setFont(new Font("SansSerif", Font.PLAIN, 12));
        DarkThemeHelper.styleButton(btnDeletePreset, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.RED_ACCENT);

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
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        DarkThemeHelper.stylePanel(headerPanel, DarkThemeHelper.PANEL_BG);

        JPanel leftHeaderGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftHeaderGroup.setOpaque(false);
        leftHeaderGroup.add(btnAddCategory);
        leftHeaderGroup.add(btnAddProduct);
        headerPanel.add(leftHeaderGroup, BorderLayout.WEST);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        userPanel.setOpaque(false);
        userPanel.add(lblUser);
        userPanel.add(btnAlterarUser);
        headerPanel.add(userPanel, BorderLayout.CENTER);

        headerPanel.add(btnConfigLayout, BorderLayout.EAST);
        JPanel presetsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        presetsPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        DarkThemeHelper.stylePanel(presetsPanel, DarkThemeHelper.PANEL_BG);
        JLabel lblPresets = new JLabel("Presets de Impressão:");
        DarkThemeHelper.styleLabel(lblPresets, DarkThemeHelper.TEXT_PRIMARY, Font.BOLD, 12);
        presetsPanel.add(lblPresets);
        cbPresets.setPreferredSize(new Dimension(200, 30));
        presetsPanel.add(cbPresets);
        presetsPanel.add(btnSavePreset);
        presetsPanel.add(btnApplyPreset);
        presetsPanel.add(btnDeletePreset);
        JPanel topPanel = new JPanel(new BorderLayout(0, 6));
        topPanel.setOpaque(false);
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(presetsPanel, BorderLayout.SOUTH);
        mainPanel.add(topPanel, BorderLayout.NORTH);

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
        // Nova Categoria / Pasta
        btnAddCategory.addActionListener(e -> {
            String nome = JOptionPane.showInputDialog(this, "Nome da Nova Pasta / Categoria:", "Criar Pasta",
                    JOptionPane.PLAIN_MESSAGE);
            if (nome != null && !nome.trim().isEmpty()) {
                CategoriaProduto newCat = new CategoriaProduto(nome.trim());
                categorias.add(newCat);
                persistenceService.saveCategorias(categorias);
                rebuildProductList();
                setStatus("Pasta '" + nome.trim() + "' criada.", false);
            }
        });

        // Novo Produto
        btnAddProduct.addActionListener(e -> {
            if (categorias.isEmpty()) {
                categorias.add(new CategoriaProduto("Nasser Esfihas"));
            }

            CategoriaProduto targetCat = categorias.get(0);
            if (categorias.size() > 1) {
                String[] names = categorias.stream().map(CategoriaProduto::getNome).toArray(String[]::new);
                String selected = (String) JOptionPane.showInputDialog(this,
                        "Selecione a Pasta de destino:", "Adicionar Produto",
                        JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
                if (selected == null)
                    return;
                for (CategoriaProduto c : categorias) {
                    if (c.getNome().equalsIgnoreCase(selected)) {
                        targetCat = c;
                        break;
                    }
                }
            }

            ProdutoDialog dialog = new ProdutoDialog(this);
            dialog.setVisible(true);
            if (dialog.wasSaved() && dialog.getProdutoCriado() != null) {
                targetCat.getProdutos().add(dialog.getProdutoCriado());
                persistenceService.saveCategorias(categorias);
                rebuildProductList();
                setStatus("Produto '" + dialog.getProdutoCriado().getNome() + "' adicionado em '" + targetCat.getNome()
                        + "'.", false);
            }
        });

        // Configuração de Layout Visual
        btnConfigLayout.addActionListener(e -> {
            LayoutEditorDialog dialog = new LayoutEditorDialog(this, persistenceService, printerService);
            dialog.setVisible(true);
            updatePrinterStatus();
            rebuildProductList();
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
        btnSavePreset.addActionListener(e -> salvarPresetAtual());
        btnApplyPreset.addActionListener(e -> aplicarPresetSelecionado());
        btnDeletePreset.addActionListener(e -> excluirPresetSelecionado());
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
        captureVisibleQuantities();
        productsContainer.removeAll();
        quantityFields.clear();
        categoryHeaderPanels.clear();
        productCardPanels.clear();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 15, 8, 15);

        if (categorias.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setOpaque(false);

            JLabel lblMsg = new JLabel("Nenhuma pasta encontrada. Clique em '+ Nova Pasta'.");
            DarkThemeHelper.styleLabel(lblMsg, DarkThemeHelper.TEXT_MUTED, Font.BOLD, 14);
            emptyPanel.add(lblMsg);

            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            productsContainer.add(emptyPanel, gbc);
        } else {
            for (int catIdx = 0; catIdx < categorias.size(); catIdx++) {
                CategoriaProduto cat = categorias.get(catIdx);
                JPanel catSection = createCategoryPanel(cat, catIdx);
                productsContainer.add(catSection, gbc);
                gbc.gridy++;
            }

            // Spacer final
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            JPanel spacer = new JPanel();
            spacer.setOpaque(false);
            productsContainer.add(spacer, gbc);
        }

        productsContainer.revalidate();
        productsContainer.repaint();
    }

    private JPanel createCategoryPanel(CategoriaProduto cat, int catIdx) {
        JPanel sectionPanel = new JPanel(new BorderLayout(0, 6));
        sectionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DarkThemeHelper.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        DarkThemeHelper.stylePanel(sectionPanel, DarkThemeHelper.PANEL_BG);

        // Header da Categoria (Accordion Header)
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        categoryHeaderPanels.add(headerPanel);
        headerPanel.putClientProperty("categoryIndex", catIdx);

        // Esquerda: Alternador (► / ▼) + Nome da Pasta + Qtd
        String toggleIcon = cat.isExpanded() ? "▼ " : "► ";
        JLabel lblFolderTitle = new JLabel(
                toggleIcon + "📁  " + cat.getNome() + "  (" + cat.getProdutos().size() + " produtos)");
        DarkThemeHelper.styleLabel(lblFolderTitle, DarkThemeHelper.TEXT_PRIMARY, Font.BOLD, 15);

        headerPanel.add(lblFolderTitle, BorderLayout.WEST);

        // Direita: Ações da Pasta
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionsPanel.setOpaque(false);

        JButton btnAddProdCat = new JButton("+ Add Produto");
        btnAddProdCat.setFont(new Font("SansSerif", Font.PLAIN, 11));
        DarkThemeHelper.styleButton(btnAddProdCat, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.GREEN_ACCENT);

        JButton btnRenameCat = new JButton("Renomear");
        btnRenameCat.setFont(new Font("SansSerif", Font.PLAIN, 11));
        DarkThemeHelper.styleButton(btnRenameCat, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.TEXT_PRIMARY);

        JButton btnDeleteCat = new JButton("Excluir Pasta");
        btnDeleteCat.setFont(new Font("SansSerif", Font.PLAIN, 11));
        DarkThemeHelper.styleButton(btnDeleteCat, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.RED_ACCENT);

        actionsPanel.add(btnAddProdCat);
        actionsPanel.add(btnRenameCat);
        actionsPanel.add(btnDeleteCat);
        headerPanel.add(actionsPanel, BorderLayout.EAST);

        sectionPanel.add(headerPanel, BorderLayout.NORTH);

        // Clique no Header para Expandir / Recolher
        lblFolderTitle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cat.setExpanded(!cat.isExpanded());
                rebuildProductList();
            }
        });

        btnAddProdCat.addActionListener(e -> {
            ProdutoDialog dialog = new ProdutoDialog(this);
            dialog.setVisible(true);
            if (dialog.wasSaved() && dialog.getProdutoCriado() != null) {
                cat.getProdutos().add(dialog.getProdutoCriado());
                persistenceService.saveCategorias(categorias);
                rebuildProductList();
                setStatus(
                        "Produto '" + dialog.getProdutoCriado().getNome() + "' adicionado em '" + cat.getNome() + "'.",
                        false);
            }
        });

        btnRenameCat.addActionListener(e -> {
            String novoNome = (String) JOptionPane.showInputDialog(this,
                    "Novo nome para a pasta:", "Renomear Pasta",
                    JOptionPane.PLAIN_MESSAGE, null, null, cat.getNome());
            if (novoNome != null && !novoNome.trim().isEmpty()) {
                cat.setNome(novoNome.trim());
                persistenceService.saveCategorias(categorias);
                rebuildProductList();
                setStatus("Pasta renomeada para '" + novoNome.trim() + "'.", false);
            }
        });

        btnDeleteCat.addActionListener(e -> {
            int op = JOptionPane.showConfirmDialog(this,
                    "Deseja realmente excluir a pasta '" + cat.getNome() + "' e todos os seus produtos?",
                    "Confirmar Exclusão de Pasta",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (op == JOptionPane.YES_OPTION) {
                categorias.remove(catIdx);
                persistenceService.saveCategorias(categorias);
                rebuildProductList();
                setStatus("Pasta '" + cat.getNome() + "' excluída.", false);
            }
        });

        // Se a pasta estiver expandida, renderiza os cards dos produtos
        if (cat.isExpanded()) {
            JPanel itemsContainer = new JPanel(new GridBagLayout());
            itemsContainer.setOpaque(false);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(4, 8, 4, 8);

            if (cat.getProdutos().isEmpty()) {
                JLabel lblEmpty = new JLabel(
                        "Esta pasta está vazia. Arraste um produto para cá ou clique em '+ Add Produto'.");
                DarkThemeHelper.styleLabel(lblEmpty, DarkThemeHelper.TEXT_MUTED, Font.ITALIC, 12);
                lblEmpty.setBorder(new EmptyBorder(8, 12, 8, 12));
                itemsContainer.add(lblEmpty, gbc);
            } else {
                for (int prodIdx = 0; prodIdx < cat.getProdutos().size(); prodIdx++) {
                    Produto p = cat.getProdutos().get(prodIdx);
                    JPanel card = createProductCard(p, catIdx, prodIdx);
                    itemsContainer.add(card, gbc);
                    gbc.gridy++;
                }
            }
            sectionPanel.add(itemsContainer, BorderLayout.CENTER);
        }

        return sectionPanel;
    }

    private JPanel createProductCard(Produto p, int catIdx, int prodIdx) {
        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DarkThemeHelper.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        DarkThemeHelper.stylePanel(card, DarkThemeHelper.CARD_BG);
        card.putClientProperty("categoryIndex", catIdx);
        card.putClientProperty("productIndex", prodIdx);
        productCardPanels.add(card);

        // Arraste: Handle visual de arrastar
        JLabel lblDragHandle = new JLabel(" ⠿ ");
        lblDragHandle.setFont(new Font("SansSerif", Font.BOLD, 18));
        DarkThemeHelper.styleLabel(lblDragHandle, DarkThemeHelper.TEXT_MUTED, Font.BOLD, 18);
        lblDragHandle.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        lblDragHandle.setToolTipText("Clique e arraste para mover esta mercadoria entre pastas");

        // Esquerda: Informações
        JPanel detailsPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        detailsPanel.setOpaque(false);

        JLabel lblName = new JLabel(p.getNome());
        DarkThemeHelper.styleLabel(lblName, DarkThemeHelper.TEXT_PRIMARY, Font.BOLD, 17);

        boolean temSif = p.getSif() != null && !p.getSif().isBlank() && !p.getSif().trim().equalsIgnoreCase("N/A");
        String subtitle = String.format("%sValidade: %d dias  |  %s",
                temSif ? "S.I.F.: " + p.getSif().trim() + "  |  " : "",
                p.getDiasValidade(),
                (p.getArmazenamento() == null || p.getArmazenamento().isBlank()) ? "Geral" : p.getArmazenamento());
        JLabel lblSub = new JLabel(subtitle);
        DarkThemeHelper.styleLabel(lblSub, DarkThemeHelper.TEXT_MUTED, Font.PLAIN, 12);

        detailsPanel.add(lblName);
        detailsPanel.add(lblSub);

        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftGroup.setOpaque(false);
        leftGroup.add(lblDragHandle);
        leftGroup.add(detailsPanel);
        card.add(leftGroup, BorderLayout.WEST);

        // Direita: Controles e CRUD
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        rightPanel.setOpaque(false);

        JPanel crudPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        crudPanel.setOpaque(false);

        JButton btnEdit = new JButton("Editar");
        btnEdit.setFont(new Font("SansSerif", Font.PLAIN, 12));
        DarkThemeHelper.styleButton(btnEdit, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.TEXT_PRIMARY);
        btnEdit.setPreferredSize(new Dimension(70, 36));

        JButton btnDelete = new JButton("Excluir");
        btnDelete.setFont(new Font("SansSerif", Font.PLAIN, 12));
        DarkThemeHelper.styleButton(btnDelete, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.RED_ACCENT);
        btnDelete.setPreferredSize(new Dimension(70, 36));

        crudPanel.add(btnEdit);
        crudPanel.add(btnDelete);

        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        qtyPanel.setOpaque(false);

        JButton btnMinus = new JButton("-");
        btnMinus.setFont(new Font("SansSerif", Font.BOLD, 18));
        btnMinus.setPreferredSize(new Dimension(42, 36));
        DarkThemeHelper.styleButton(btnMinus, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.TEXT_PRIMARY);

        JTextField tfQty = new JTextField(String.valueOf(quantitiesByProductId.getOrDefault(p.getId(), 0)), 3);
        tfQty.setHorizontalAlignment(JTextField.CENTER);
        tfQty.setFont(new Font("SansSerif", Font.BOLD, 16));
        tfQty.setPreferredSize(new Dimension(42, 36));
        DarkThemeHelper.styleTextField(tfQty);

        quantityFields.put(p, tfQty);

        JButton btnPlus = new JButton("+");
        btnPlus.setFont(new Font("SansSerif", Font.BOLD, 18));
        btnPlus.setPreferredSize(new Dimension(42, 36));
        DarkThemeHelper.styleButton(btnPlus, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.TEXT_PRIMARY);

        qtyPanel.add(btnMinus);
        qtyPanel.add(tfQty);
        qtyPanel.add(btnPlus);

        rightPanel.add(crudPanel);
        rightPanel.add(qtyPanel);
        card.add(rightPanel, BorderLayout.EAST);

        // Ações CRUD
        btnMinus.addActionListener(e -> {
            try {
                int val = Integer.parseInt(tfQty.getText().trim());
                if (val > 0) {
                    tfQty.setText(String.valueOf(val - 1));
                    quantitiesByProductId.put(p.getId(), val - 1);
                }
            } catch (NumberFormatException ignored) {
            }
        });

        btnPlus.addActionListener(e -> {
            try {
                int val = Integer.parseInt(tfQty.getText().trim());
                tfQty.setText(String.valueOf(val + 1));
                quantitiesByProductId.put(p.getId(), val + 1);
            } catch (NumberFormatException ignored) {
            }
        });

        btnEdit.addActionListener(e -> {
            ProdutoDialog dialog = new ProdutoDialog(this, p);
            dialog.setVisible(true);
            if (dialog.wasSaved() && dialog.getProdutoCriado() != null) {
                persistenceService.saveCategorias(categorias);
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
                categorias.get(catIdx).getProdutos().remove(prodIdx);
                persistenceService.saveCategorias(categorias);
                rebuildProductList();
                setStatus("Produto '" + p.getNome() + "' excluído.", false);
            }
        });

        // Drag & Drop Handler (reordena dentro da pasta ou move para outra pasta)
        MouseAdapter dragAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragCatIdx = catIdx;
                dragProdIdx = prodIdx;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragCatIdx != -1) {
                    Point screenPt = e.getLocationOnScreen();
                    JPanel targetCard = findProductCardAt(screenPt);
                    setDropTargetHighlight(targetCard != null ? targetCard : findCategoryHeaderAt(screenPt));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragCatIdx != -1) {
                    Point screenPt = e.getLocationOnScreen();
                    JPanel targetCard = findProductCardAt(screenPt);
                    Integer targetCatIdx = targetCard != null
                            ? (Integer) targetCard.getClientProperty("categoryIndex")
                            : findCategoryIndexAt(screenPt);
                    Integer targetProdIdx = targetCard != null
                            ? (Integer) targetCard.getClientProperty("productIndex")
                            : null;
                    clearDropTargetHighlight();

                    if (targetCatIdx != null && targetCatIdx >= 0 && targetCatIdx < categorias.size()) {
                        int insertionIndex = targetProdIdx != null
                                ? targetProdIdx
                                : categorias.get(targetCatIdx).getProdutos().size();
                        moveProduct(dragCatIdx, dragProdIdx, targetCatIdx, insertionIndex);
                    }
                    dragCatIdx = -1;
                    dragProdIdx = -1;
                }
            }
        };

        installProductDragHandler(dragAdapter, card, leftGroup, detailsPanel, lblDragHandle, lblName, lblSub);

        return card;
    }

    private JPanel findCategoryHeaderAt(Point screenPt) {
        for (JPanel header : categoryHeaderPanels) {
            if (header.isShowing()) {
                Point p = header.getLocationOnScreen();
                Rectangle bounds = new Rectangle(p.x, p.y, header.getWidth(), header.getHeight());
                if (bounds.contains(screenPt)) {
                    return header;
                }
            }
        }
        return null;
    }

    private JPanel findProductCardAt(Point screenPt) {
        for (JPanel card : productCardPanels) {
            if (card.isShowing()) {
                Point location = card.getLocationOnScreen();
                Rectangle bounds = new Rectangle(location.x, location.y, card.getWidth(), card.getHeight());
                if (bounds.contains(screenPt)) return card;
            }
        }
        return null;
    }

    private void installProductDragHandler(MouseAdapter adapter, Component... components) {
        for (Component component : components) {
            component.addMouseListener(adapter);
            component.addMouseMotionListener(adapter);
        }
    }

    private void setDropTargetHighlight(JComponent target) {
        if (target == currentDropTarget) return;
        clearDropTargetHighlight();
        if (target != null) {
            currentDropTarget = target;
            currentDropTargetBorder = target.getBorder();
            target.setBorder(BorderFactory.createLineBorder(DarkThemeHelper.ACCENT_BLUE, 2, true));
        }
    }

    private void clearDropTargetHighlight() {
        if (currentDropTarget != null) {
            currentDropTarget.setBorder(currentDropTargetBorder);
            currentDropTarget = null;
            currentDropTargetBorder = null;
        }
        currentHoveredCatPanel = null;
    }

    private void moveProduct(int sourceCatIdx, int sourceProdIdx, int targetCatIdx, int targetProdIdx) {
        if (sourceCatIdx < 0 || sourceCatIdx >= categorias.size()
                || sourceProdIdx < 0 || sourceProdIdx >= categorias.get(sourceCatIdx).getProdutos().size()) {
            return;
        }

        List<Produto> sourceProducts = categorias.get(sourceCatIdx).getProdutos();
        Produto moved = sourceProducts.remove(sourceProdIdx);
        List<Produto> targetProducts = categorias.get(targetCatIdx).getProdutos();
        if (sourceCatIdx == targetCatIdx && sourceProdIdx < targetProdIdx) targetProdIdx--;
        targetProdIdx = Math.max(0, Math.min(targetProdIdx, targetProducts.size()));
        targetProducts.add(targetProdIdx, moved);

        persistenceService.saveCategorias(categorias);
        rebuildProductList();
        String action = sourceCatIdx == targetCatIdx ? "reordenado" : "movido para a pasta '" + categorias.get(targetCatIdx).getNome() + "'";
        setStatus("Produto '" + moved.getNome() + "' " + action + ".", false);
    }

    private Integer findCategoryIndexAt(Point screenPt) {
        JPanel header = findCategoryHeaderAt(screenPt);
        if (header != null) {
            Object obj = header.getClientProperty("categoryIndex");
            if (obj instanceof Integer) {
                return (Integer) obj;
            }
        }
        return null;
    }

    /**
     * Coleta as quantidades maiores que 0 de todas as pastas (expandidas ou
     * recolhidas),
     * calcula datas no momento do clique, mescla no template dinâmico e envia em
     * lote.
     */
    private void dispararImpressaoLote() {
        captureVisibleQuantities();
        String printerName = persistenceService.getSelectedPrinter();
        if (printerName == null || printerName.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Nenhuma impressora Elgin foi configurada como padrão.\nPor favor, configure em '⚙ Configuração do Layout'.",
                    "Impressora Não Selecionada",
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

        // Data e hora do momento da impressão
        LocalDateTime fabricacao = LocalDateTime.now();

        // Varre todas as pastas (expandidas ou recolhidas)
        for (CategoriaProduto cat : categorias) {
            for (Produto p : cat.getProdutos()) {
                Integer quantity = quantitiesByProductId.get(p.getId());
                JTextField visibleField = quantityFields.get(p);
                if (quantity != null || visibleField != null) {
                    try {
                        int qty = visibleField != null
                                ? Integer.parseInt(visibleField.getText().trim())
                                : quantity;
                        if (qty > 0) {
                            LocalDateTime validade = fabricacao.plusDays(p.getDiasValidade());

                            EtiquetaData data = new EtiquetaData(
                                    p.getNome(),
                                    (p.getSif() == null || p.getSif().isBlank()
                                            || p.getSif().trim().equalsIgnoreCase("N/A")) ? "" : p.getSif().trim(),
                                    (p.getArmazenamento() == null || p.getArmazenamento().isBlank()) ? "Geral"
                                            : p.getArmazenamento(),
                                    fabricacao,
                                    validade,
                                    responsavelAtual);

                            // Envia impressão binarizada
                            printerService.printVisualLabel(printerName, layoutElements, data, qty);

                            imprimiuAlgum = true;
                            totalImpressos += qty;
                        }
                    } catch (NumberFormatException ex) {
                        erros.append("Quantidade inválida para o produto: ").append(p.getNome()).append("\n");
                    } catch (ElginPrinterService.ElginPrintException ex) {
                        erros.append("Erro ao imprimir ").append(p.getNome()).append(": ").append(ex.getMessage())
                                .append("\n");
                    }
                }
            }
        }

        if (imprimiuAlgum) {
            // Zera contadores de todas as caixas
            quantitiesByProductId.clear();
            for (JTextField tf : quantityFields.values()) tf.setText("0");

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

    private void captureVisibleQuantities() {
        for (Map.Entry<Produto, JTextField> entry : quantityFields.entrySet()) {
            try {
                int quantity = Integer.parseInt(entry.getValue().getText().trim());
                quantitiesByProductId.put(entry.getKey().getId(), Math.max(0, quantity));
            } catch (NumberFormatException ignored) {
                // Keep the last valid value; printing will continue to show the existing validation behavior.
            }
        }
    }

    private void refreshPresetSelector() {
        if (cbPresets == null) return;
        Preset selected = (Preset) cbPresets.getSelectedItem();
        cbPresets.removeAllItems();
        for (Preset preset : presets) cbPresets.addItem(preset);
        if (selected != null) cbPresets.setSelectedItem(selected);
    }

    private void salvarPresetAtual() {
        captureVisibleQuantities();
        Map<String, Integer> quantities = new LinkedHashMap<>();
        for (CategoriaProduto category : categorias) {
            for (Produto product : category.getProdutos()) {
                int quantity = quantitiesByProductId.getOrDefault(product.getId(), 0);
                if (quantity > 0) quantities.put(product.getId(), quantity);
            }
        }
        if (quantities.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe ao menos uma quantidade maior que zero antes de salvar.", "Preset vazio", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String name = JOptionPane.showInputDialog(this, "Nome do preset:", "Salvar Preset", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;
        String normalizedName = name.trim();
        presets.removeIf(preset -> preset.getNome().equalsIgnoreCase(normalizedName));
        presets.add(new Preset(normalizedName, quantities));
        persistenceService.savePresets(presets);
        refreshPresetSelector();
        cbPresets.setSelectedItem(presets.get(presets.size() - 1));
        setStatus("Preset '" + normalizedName + "' salvo.", false);
    }

    private void aplicarPresetSelecionado() {
        Preset preset = (Preset) cbPresets.getSelectedItem();
        if (preset == null) {
            JOptionPane.showMessageDialog(this, "Selecione um preset para aplicar.", "Nenhum preset selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        captureVisibleQuantities();
        quantitiesByProductId.clear();
        quantitiesByProductId.putAll(preset.getQuantidadesPorProduto());
        for (Map.Entry<Produto, JTextField> entry : quantityFields.entrySet()) {
            entry.getValue().setText(String.valueOf(quantitiesByProductId.getOrDefault(entry.getKey().getId(), 0)));
        }
        rebuildProductList();
        setStatus("Preset '" + preset.getNome() + "' aplicado. Ajuste as quantidades antes de imprimir.", false);
    }

    private void excluirPresetSelecionado() {
        Preset preset = (Preset) cbPresets.getSelectedItem();
        if (preset == null) {
            JOptionPane.showMessageDialog(this, "Selecione um preset para excluir.", "Nenhum preset selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int option = JOptionPane.showConfirmDialog(this, "Excluir o preset '" + preset.getNome() + "'?", "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (option != JOptionPane.YES_OPTION) return;
        presets.remove(preset);
        persistenceService.savePresets(presets);
        refreshPresetSelector();
        setStatus("Preset '" + preset.getNome() + "' excluído.", false);
    }
}
