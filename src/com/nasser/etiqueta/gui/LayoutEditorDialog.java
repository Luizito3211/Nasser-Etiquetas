package com.nasser.etiqueta.gui;

import com.nasser.etiqueta.model.ElementoLayout;
import com.nasser.etiqueta.service.ElginPrinterService;
import com.nasser.etiqueta.service.PersistenceService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Editor visual de layout de etiquetas de 60x40mm (480x320 pixels).
 * Permite inserção de textos fixos, tags dinâmicas e linhas divisórias com arrastar livre.
 * Estilizado com o tema Dark Minimalista.
 */
public class LayoutEditorDialog extends JDialog {

    private final PersistenceService persistenceService;
    private final ElginPrinterService printerService;

    // Dados
    private final List<ElementoLayout> elements;
    private ElementoLayout selectedElement;
    private boolean updatingControls = false;

    // Componentes de Impressora
    private JComboBox<String> cbPrinters;
    private JButton btnRefreshPrinters;

    // Barra de Ferramentas
    private JButton btnAddText;
    private JButton btnAddTag;
    private JButton btnAddLine;

    // Componentes do Canvas e Editor
    private LabelCanvas canvasPanel;
    private JComboBox<String> cbElementsSelector;
    
    // Controles comuns
    private JSpinner spinX;
    private JSpinner spinY;
    private JButton btnDeleteElement;

    // Controles de Texto
    private JPanel textPropertiesPanel;
    private JTextField tfTextContent;
    private JComboBox<String> cbFontSize;
    private JCheckBox chkBold;

    // Controles de Linha
    private JPanel linePropertiesPanel;
    private JSpinner spinX2;
    private JSpinner spinThickness;

    private JButton btnSalvar;
    private JButton btnFechar;

    public LayoutEditorDialog(Frame parent, PersistenceService persistence, ElginPrinterService printer) {
        super(parent, "Editor de Layout Visual - Elgin L42 Pro", true);
        this.persistenceService = persistence;
        this.printerService = printer;

        // Carrega elementos e inicializa seleção
        this.elements = persistenceService.loadLayoutElements();
        if (!elements.isEmpty()) {
            this.selectedElement = elements.get(0);
        }

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Estilização JDialog
        DarkThemeHelper.styleDialog(this);

        initComponents();
        setupLayout();
        setupEvents();

        // Carrega configurações salvas
        loadConfig();

        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        // Topo: Impressora
        cbPrinters = new JComboBox<>();
        cbPrinters.setFont(new Font("SansSerif", Font.PLAIN, 14));
        DarkThemeHelper.styleComboBox(cbPrinters);

        btnRefreshPrinters = new JButton("Atualizar");
        btnRefreshPrinters.setFont(new Font("SansSerif", Font.PLAIN, 12));
        DarkThemeHelper.styleButton(btnRefreshPrinters, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.TEXT_PRIMARY);

        // Barra de Ferramentas (Adicionar itens)
        btnAddText = new JButton("+ Texto Fixo");
        btnAddText.setFont(new Font("SansSerif", Font.BOLD, 12));
        DarkThemeHelper.styleButton(btnAddText, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.TEXT_PRIMARY);

        btnAddTag = new JButton("+ Tag Dinâmica");
        btnAddTag.setFont(new Font("SansSerif", Font.BOLD, 12));
        DarkThemeHelper.styleButton(btnAddTag, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.TEXT_PRIMARY);

        btnAddLine = new JButton("+ Linha Divisória");
        btnAddLine.setFont(new Font("SansSerif", Font.BOLD, 12));
        DarkThemeHelper.styleButton(btnAddLine, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.TEXT_PRIMARY);

        // O Canvas 480x320
        canvasPanel = new LabelCanvas();

        // Painel de Propriedades Comuns
        String[] tags = new String[elements.size()];
        for (int i = 0; i < elements.size(); i++) {
            tags[i] = elements.get(i).getTipo() + ": " + (elements.get(i).getConteudo() != null ? elements.get(i).getConteudo() : "Linha");
        }
        cbElementsSelector = new JComboBox<>(tags);
        cbElementsSelector.setFont(new Font("SansSerif", Font.PLAIN, 12));
        DarkThemeHelper.styleComboBox(cbElementsSelector);

        spinX = new JSpinner(new SpinnerNumberModel(30, 0, 480, 1));
        DarkThemeHelper.styleSpinner(spinX);
        
        spinY = new JSpinner(new SpinnerNumberModel(30, 0, 320, 1));
        DarkThemeHelper.styleSpinner(spinY);

        btnDeleteElement = new JButton("Excluir Elemento");
        btnDeleteElement.setFont(new Font("SansSerif", Font.BOLD, 12));
        DarkThemeHelper.styleButton(btnDeleteElement, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.RED_ACCENT);

        // --- Painel Propriedades de Texto ---
        textPropertiesPanel = new JPanel(new GridBagLayout());
        textPropertiesPanel.setOpaque(false);

        tfTextContent = new JTextField();
        tfTextContent.setFont(new Font("SansSerif", Font.PLAIN, 12));
        DarkThemeHelper.styleTextField(tfTextContent);

        String[] tamanhosFontes = {"Pequeno (12)", "Médio (16)", "Grande (20)", "Extra Grande (26)"};
        cbFontSize = new JComboBox<>(tamanhosFontes);
        cbFontSize.setFont(new Font("SansSerif", Font.PLAIN, 12));
        DarkThemeHelper.styleComboBox(cbFontSize);

        chkBold = new JCheckBox("Texto em Negrito");
        chkBold.setFont(new Font("SansSerif", Font.BOLD, 12));
        chkBold.setBackground(DarkThemeHelper.BACKGROUND);
        chkBold.setForeground(DarkThemeHelper.TEXT_PRIMARY);

        // --- Painel Propriedades de Linha ---
        linePropertiesPanel = new JPanel(new GridBagLayout());
        linePropertiesPanel.setOpaque(false);

        spinX2 = new JSpinner(new SpinnerNumberModel(450, 0, 480, 1));
        DarkThemeHelper.styleSpinner(spinX2);

        spinThickness = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
        DarkThemeHelper.styleSpinner(spinThickness);

        // Rodapé: Ações
        btnSalvar = new JButton("Salvar Layout Visual");
        btnSalvar.setFont(new Font("SansSerif", Font.BOLD, 14));
        DarkThemeHelper.styleButton(btnSalvar, DarkThemeHelper.ACCENT_BLUE, Color.WHITE);

        btnFechar = new JButton("Fechar");
        btnFechar.setFont(new Font("SansSerif", Font.PLAIN, 14));
        DarkThemeHelper.styleButton(btnFechar, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.TEXT_PRIMARY);
    }

    private void setupLayout() {
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        DarkThemeHelper.stylePanel(contentPanel, DarkThemeHelper.BACKGROUND);

        // 1. Painel Superior: Impressora + Toolbar
        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.setOpaque(false);

        // Impressora
        JPanel printerPanel = new JPanel(new GridBagLayout());
        printerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "1. Impressora Elgin Padrão",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12)
        ));
        DarkThemeHelper.stylePanel(printerPanel, DarkThemeHelper.PANEL_BG);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JLabel lblDisp = new JLabel("Dispositivo:");
        DarkThemeHelper.styleLabel(lblDisp, DarkThemeHelper.TEXT_PRIMARY, Font.PLAIN, 12);
        printerPanel.add(lblDisp, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        printerPanel.add(cbPrinters, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        printerPanel.add(btnRefreshPrinters, gbc);
        topPanel.add(printerPanel, BorderLayout.NORTH);

        // Toolbar de inserção
        JPanel toolsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "2. Ferramentas do Layout",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12)
        ));
        DarkThemeHelper.stylePanel(toolsPanel, DarkThemeHelper.PANEL_BG);
        toolsPanel.add(btnAddText);
        toolsPanel.add(btnAddTag);
        toolsPanel.add(btnAddLine);
        topPanel.add(toolsPanel, BorderLayout.CENTER);

        contentPanel.add(topPanel, BorderLayout.NORTH);

        // 2. Painel Central: Canvas + Propriedades
        JPanel centerPanel = new JPanel(new BorderLayout(15, 0));
        centerPanel.setOpaque(false);

        // Canvas (Esquerda)
        JPanel canvasBorderPanel = new JPanel(new BorderLayout());
        canvasBorderPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Etiqueta 60x40mm (480x320 px)",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12)
        ));
        DarkThemeHelper.stylePanel(canvasBorderPanel, DarkThemeHelper.BACKGROUND);
        canvasBorderPanel.add(canvasPanel, BorderLayout.CENTER);
        centerPanel.add(canvasBorderPanel, BorderLayout.CENTER);

        // Propriedades (Direita)
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "3. Propriedades",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12)
        ));
        controlPanel.setPreferredSize(new Dimension(280, 320));
        DarkThemeHelper.stylePanel(controlPanel, DarkThemeHelper.PANEL_BG);

        GridBagConstraints cGbc = new GridBagConstraints();
        cGbc.fill = GridBagConstraints.HORIZONTAL;
        cGbc.insets = new Insets(6, 6, 6, 6);
        int row = 0;

        // Seletor
        cGbc.gridx = 0;
        cGbc.gridy = row++;
        cGbc.gridwidth = 2;
        JLabel lblSeletor = new JLabel("Elemento:");
        DarkThemeHelper.styleLabel(lblSeletor, DarkThemeHelper.TEXT_PRIMARY, Font.PLAIN, 12);
        controlPanel.add(lblSeletor, cGbc);
        
        cGbc.gridy = row++;
        controlPanel.add(cbElementsSelector, cGbc);

        cGbc.gridwidth = 1;

        // Posição X
        cGbc.gridx = 0;
        cGbc.gridy = row;
        cGbc.weightx = 0.0;
        JLabel lblX = new JLabel("X / Início:");
        DarkThemeHelper.styleLabel(lblX, DarkThemeHelper.TEXT_PRIMARY, Font.PLAIN, 12);
        controlPanel.add(lblX, cGbc);
        
        cGbc.gridx = 1;
        cGbc.weightx = 1.0;
        controlPanel.add(spinX, cGbc);
        row++;

        // Posição Y
        cGbc.gridx = 0;
        cGbc.gridy = row;
        cGbc.weightx = 0.0;
        JLabel lblY = new JLabel("Y / Altura:");
        DarkThemeHelper.styleLabel(lblY, DarkThemeHelper.TEXT_PRIMARY, Font.PLAIN, 12);
        controlPanel.add(lblY, cGbc);
        
        cGbc.gridx = 1;
        cGbc.weightx = 1.0;
        controlPanel.add(spinY, cGbc);
        row++;

        // Subpainel Dinâmico: Texto
        GridBagConstraints tGbc = new GridBagConstraints();
        tGbc.fill = GridBagConstraints.HORIZONTAL;
        tGbc.insets = new Insets(4, 4, 4, 4);

        tGbc.gridx = 0;
        tGbc.gridy = 0;
        tGbc.weightx = 0.0;
        JLabel lblTexto = new JLabel("Texto:");
        DarkThemeHelper.styleLabel(lblTexto, DarkThemeHelper.TEXT_PRIMARY, Font.PLAIN, 12);
        textPropertiesPanel.add(lblTexto, tGbc);
        
        tGbc.gridx = 1;
        tGbc.weightx = 1.0;
        textPropertiesPanel.add(tfTextContent, tGbc);

        tGbc.gridx = 0;
        tGbc.gridy = 1;
        tGbc.gridwidth = 2;
        tGbc.weightx = 1.0;
        textPropertiesPanel.add(cbFontSize, tGbc);

        tGbc.gridy = 2;
        textPropertiesPanel.add(chkBold, tGbc);

        // Subpainel Dinâmico: Linha
        GridBagConstraints lGbc = new GridBagConstraints();
        lGbc.fill = GridBagConstraints.HORIZONTAL;
        lGbc.insets = new Insets(4, 4, 4, 4);

        lGbc.gridx = 0;
        lGbc.gridy = 0;
        lGbc.weightx = 0.0;
        JLabel lblX2 = new JLabel("Fim X:");
        DarkThemeHelper.styleLabel(lblX2, DarkThemeHelper.TEXT_PRIMARY, Font.PLAIN, 12);
        linePropertiesPanel.add(lblX2, lGbc);
        
        lGbc.gridx = 1;
        lGbc.weightx = 1.0;
        linePropertiesPanel.add(spinX2, lGbc);

        lGbc.gridx = 0;
        lGbc.gridy = 1;
        lGbc.weightx = 0.0;
        JLabel lblEsp = new JLabel("Espessura:");
        DarkThemeHelper.styleLabel(lblEsp, DarkThemeHelper.TEXT_PRIMARY, Font.PLAIN, 12);
        linePropertiesPanel.add(lblEsp, lGbc);
        
        lGbc.gridx = 1;
        lGbc.weightx = 1.0;
        linePropertiesPanel.add(spinThickness, lGbc);

        // Adiciona os subpainéis dinâmicos
        cGbc.gridx = 0;
        cGbc.gridy = row++;
        cGbc.gridwidth = 2;
        cGbc.weightx = 1.0;
        controlPanel.add(textPropertiesPanel, cGbc);

        cGbc.gridy = row++;
        controlPanel.add(linePropertiesPanel, cGbc);

        // Excluir Elemento
        cGbc.gridy = row++;
        cGbc.insets = new Insets(15, 6, 6, 6);
        controlPanel.add(btnDeleteElement, cGbc);

        centerPanel.add(controlPanel, BorderLayout.EAST);
        contentPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. Painel Inferior: Ações
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnFechar);
        bottomPanel.add(btnSalvar);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(contentPanel);
    }

    private void setupEvents() {
        btnRefreshPrinters.addActionListener(e -> refreshPrinters());
        btnFechar.addActionListener(e -> dispose());
        btnSalvar.addActionListener(e -> salvarConfig());

        // Ações da Barra de Ferramentas
        btnAddText.addActionListener(e -> {
            String txt = JOptionPane.showInputDialog(this, "Texto Fixo da Etiqueta:", "Adicionar Texto", JOptionPane.PLAIN_MESSAGE);
            if (txt != null && !txt.trim().isEmpty()) {
                adicionarElemento(new ElementoLayout("TEXTO", txt.trim(), 50, 100, 0, 16, false, 1));
            }
        });

        btnAddTag.addActionListener(e -> {
            String[] tagsList = {"{Produto}", "{Sif}", "{Armazenamento}", "{Fabricacao}", "{Validade}", "{Responsavel}"};
            String tag = (String) JOptionPane.showInputDialog(this, "Selecione a Tag Dinâmica:", "Adicionar Tag",
                    JOptionPane.PLAIN_MESSAGE, null, tagsList, tagsList[0]);
            if (tag != null) {
                adicionarElemento(new ElementoLayout("TAG", tag, 50, 100, 0, 16, true, 1));
            }
        });

        btnAddLine.addActionListener(e -> {
            adicionarElemento(new ElementoLayout("LINHA", "", 30, 120, 450, 0, false, 2));
        });

        // Ação de Excluir
        btnDeleteElement.addActionListener(e -> {
            if (selectedElement != null) {
                elements.remove(selectedElement);
                selectedElement = elements.isEmpty() ? null : elements.get(0);
                rebuildSelectorList();
                updateControlsFromSelected();
                canvasPanel.repaint();
            }
        });

        // Troca elemento via ComboBox selector
        cbElementsSelector.addActionListener(e -> {
            if (updatingControls) return;
            int idx = cbElementsSelector.getSelectedIndex();
            if (idx >= 0 && idx < elements.size()) {
                selectedElement = elements.get(idx);
                updateControlsFromSelected();
                canvasPanel.repaint();
            }
        });

        // Eventos dos Controles
        spinX.addChangeListener(e -> {
            if (updatingControls || selectedElement == null) return;
            selectedElement.setX((Integer) spinX.getValue());
            canvasPanel.repaint();
        });

        spinY.addChangeListener(e -> {
            if (updatingControls || selectedElement == null) return;
            selectedElement.setY((Integer) spinY.getValue());
            canvasPanel.repaint();
        });

        spinX2.addChangeListener(e -> {
            if (updatingControls || selectedElement == null) return;
            selectedElement.setX2((Integer) spinX2.getValue());
            canvasPanel.repaint();
        });

        spinThickness.addChangeListener(e -> {
            if (updatingControls || selectedElement == null) return;
            selectedElement.setThickness((Integer) spinThickness.getValue());
            canvasPanel.repaint();
        });

        // Ações de texto em tempo real
        tfTextContent.addCaretListener(e -> {
            if (updatingControls || selectedElement == null) return;
            if ("TEXTO".equals(selectedElement.getTipo())) {
                selectedElement.setConteudo(tfTextContent.getText());
                canvasPanel.repaint();
            }
        });

        cbFontSize.addActionListener(e -> {
            if (updatingControls || selectedElement == null) return;
            selectedElement.setFontSize(getSelectedFontSizeFromCombo());
            canvasPanel.repaint();
        });

        chkBold.addActionListener(e -> {
            if (updatingControls || selectedElement == null) return;
            selectedElement.setBold(chkBold.isSelected());
            canvasPanel.repaint();
        });
    }

    private void adicionarElemento(ElementoLayout elem) {
        elements.add(elem);
        selectedElement = elem;
        rebuildSelectorList();
        updateControlsFromSelected();
        canvasPanel.repaint();
    }

    private void rebuildSelectorList() {
        updatingControls = true;
        cbElementsSelector.removeAllItems();
        for (ElementoLayout elem : elements) {
            cbElementsSelector.addItem(elem.getTipo() + ": " + (elem.getConteudo() != null && !elem.getConteudo().isEmpty() ? elem.getConteudo() : "Linha"));
        }
        updatingControls = false;
    }

    private void loadConfig() {
        refreshPrinters();
        String savedPrinter = persistenceService.getSelectedPrinter();
        if (!savedPrinter.isEmpty()) {
            cbPrinters.setSelectedItem(savedPrinter);
        }
        rebuildSelectorList();
        updateControlsFromSelected();
    }

    private void refreshPrinters() {
        cbPrinters.removeAllItems();
        List<String> list = printerService.listPrinters();
        for (String printer : list) {
            cbPrinters.addItem(printer);
        }
    }

    /**
     * Atualiza os campos do painel de propriedades baseado no tipo do elemento e valores dele.
     */
    private void updateControlsFromSelected() {
        if (selectedElement == null) {
            cbElementsSelector.setEnabled(false);
            spinX.setEnabled(false);
            spinY.setEnabled(false);
            textPropertiesPanel.setVisible(false);
            linePropertiesPanel.setVisible(false);
            btnDeleteElement.setEnabled(false);
            return;
        }

        updatingControls = true;
        
        cbElementsSelector.setEnabled(true);
        cbElementsSelector.setSelectedItem(selectedElement.getTipo() + ": " + (selectedElement.getConteudo() != null && !selectedElement.getConteudo().isEmpty() ? selectedElement.getConteudo() : "Linha"));
        
        spinX.setEnabled(true);
        spinX.setValue(selectedElement.getX());
        
        spinY.setEnabled(true);
        spinY.setValue(selectedElement.getY());
        
        btnDeleteElement.setEnabled(true);

        if ("LINHA".equals(selectedElement.getTipo())) {
            textPropertiesPanel.setVisible(false);
            linePropertiesPanel.setVisible(true);

            spinX2.setValue(selectedElement.getX2());
            spinThickness.setValue(selectedElement.getThickness());
        } else {
            textPropertiesPanel.setVisible(true);
            linePropertiesPanel.setVisible(false);

            tfTextContent.setText(selectedElement.getConteudo());
            tfTextContent.setEnabled("TEXTO".equals(selectedElement.getTipo())); // Bloqueia conteúdo das tags no TF
            
            int size = selectedElement.getFontSize();
            if (size <= 12) cbFontSize.setSelectedIndex(0);
            else if (size <= 16) cbFontSize.setSelectedIndex(1);
            else if (size <= 20) cbFontSize.setSelectedIndex(2);
            else cbFontSize.setSelectedIndex(3);

            chkBold.setSelected(selectedElement.isBold());
        }

        updatingControls = false;
        
        // Revalida os painéis para atualizar a disposição na tela
        textPropertiesPanel.revalidate();
        linePropertiesPanel.revalidate();
        getContentPane().repaint();
    }

    private int getSelectedFontSizeFromCombo() {
        int index = cbFontSize.getSelectedIndex();
        return switch (index) {
            case 0 -> 12;
            case 1 -> 16;
            case 2 -> 20;
            default -> 26;
        };
    }

    private void salvarConfig() {
        String printerName = (String) cbPrinters.getSelectedItem();
        if (printerName == null) printerName = "";

        persistenceService.saveSelectedPrinter(printerName);
        persistenceService.saveLayoutElements(elements);

        JOptionPane.showMessageDialog(this,
                "Layout visual e impressora padrão salvos com sucesso!",
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);

        dispose();
    }

    /**
     * Canvas visual de 480x320 pixels que simula a etiqueta com drag-and-drop livre.
     */
    private class LabelCanvas extends JPanel {

        public LabelCanvas() {
            setPreferredSize(new Dimension(480, 320));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.GRAY));

            MouseAdapter dragHandler = new MouseAdapter() {
                private ElementoLayout dragElement = null;
                private int offsetX = 0;
                private int offsetY = 0;

                @Override
                public void mousePressed(MouseEvent e) {
                    Point clickPt = e.getPoint();
                    
                    // Procura elementos na ordem inversa (do topo para o fundo) para priorizar a seleção
                    for (int i = elements.size() - 1; i >= 0; i--) {
                        ElementoLayout elem = elements.get(i);
                        
                        if ("LINHA".equals(elem.getTipo())) {
                            // Detecção de clique próximo da linha
                            Rectangle bounds = new Rectangle(
                                    elem.getX() - 5,
                                    elem.getY() - 5,
                                    Math.abs(elem.getX2() - elem.getX()) + 10,
                                    elem.getThickness() + 10
                            );
                            if (bounds.contains(clickPt)) {
                                dragElement = elem;
                                selectedElement = elem;
                                offsetX = clickPt.x - elem.getX();
                                offsetY = clickPt.y - elem.getY();
                                
                                updateControlsFromSelected();
                                repaint();
                                break;
                            }
                        } else {
                            // Detecção de clique no texto aproximando sua caixa delimitadora
                            int approxWidth = (elem.getConteudo() != null ? elem.getConteudo().length() : 5) * (elem.getFontSize() / 2);
                            Rectangle bounds = new Rectangle(
                                    elem.getX(),
                                    elem.getY() - elem.getFontSize(),
                                    approxWidth + 6,
                                    elem.getFontSize() + 4
                            );
                            if (bounds.contains(clickPt)) {
                                dragElement = elem;
                                selectedElement = elem;
                                offsetX = clickPt.x - elem.getX();
                                offsetY = clickPt.y - elem.getY();

                                updateControlsFromSelected();
                                repaint();
                                break;
                            }
                        }
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragElement != null) {
                        Point pt = e.getPoint();
                        int deltaX = pt.x - (dragElement.getX() + offsetX);
                        int deltaY = pt.y - (dragElement.getY() + offsetY);

                        if ("LINHA".equals(dragElement.getTipo())) {
                            int length = dragElement.getX2() - dragElement.getX();
                            int newY = pt.y - offsetY;
                            int newX = pt.x - offsetX;

                            // Restringe a linha dentro da etiqueta
                            newY = Math.max(15, Math.min(newY, 305));
                            newX = Math.max(15, Math.min(newX, 465 - length));

                            dragElement.setY(newY);
                            dragElement.setX(newX);
                            dragElement.setX2(newX + length);
                        } else {
                            int newX = pt.x - offsetX;
                            int newY = pt.y - offsetY;

                            // Restringe o texto
                            newX = Math.max(0, Math.min(newX, 480 - 20));
                            newY = Math.max(10, Math.min(newY, 320 - 5));

                            dragElement.setX(newX);
                            dragElement.setY(newY);
                        }

                        // Atualiza controles na tela
                        updatingControls = true;
                        spinX.setValue(dragElement.getX());
                        spinY.setValue(dragElement.getY());
                        if ("LINHA".equals(dragElement.getTipo())) {
                            spinX2.setValue(dragElement.getX2());
                        }
                        updatingControls = false;

                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    dragElement = null;
                }
            };

            addMouseListener(dragHandler);
            addMouseMotionListener(dragHandler);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            // Suavização do Canvas
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Margem de segurança externa (Retângulo preto padrão)
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(3));
            g2.drawRect(15, 15, 450, 290);

            // Renderiza cada elemento livre no Canvas
            for (ElementoLayout elem : elements) {
                if ("LINHA".equals(elem.getTipo())) {
                    // Renderiza linha
                    g2.setStroke(new BasicStroke(elem.getThickness()));
                    
                    // Desenha marcação azul ao redor se selecionada
                    if (elem == selectedElement) {
                        g2.setColor(new Color(33, 150, 243, 80));
                        g2.fillRect(elem.getX() - 3, elem.getY() - 4, (elem.getX2() - elem.getX()) + 6, elem.getThickness() + 8);
                        g2.setColor(Color.BLUE);
                        g2.drawRect(elem.getX() - 3, elem.getY() - 4, (elem.getX2() - elem.getX()) + 6, elem.getThickness() + 8);
                    }
                    
                    g2.setColor(elem == selectedElement ? Color.BLUE : Color.BLACK);
                    g2.drawLine(elem.getX(), elem.getY(), elem.getX2(), elem.getY());
                } else {
                    // Renderiza texto ou tag
                    int fontStyle = elem.isBold() ? Font.BOLD : Font.PLAIN;
                    g2.setFont(new Font("SansSerif", fontStyle, elem.getFontSize()));

                    // Desenha marcação azul ao redor se selecionada
                    if (elem == selectedElement) {
                        int approxWidth = (elem.getConteudo() != null ? elem.getConteudo().length() : 5) * (elem.getFontSize() / 2);
                        g2.setColor(new Color(33, 150, 243, 50));
                        g2.fillRect(elem.getX() - 3, elem.getY() - elem.getFontSize(), approxWidth + 6, elem.getFontSize() + 4);
                        
                        g2.setColor(Color.BLUE);
                        g2.drawRect(elem.getX() - 3, elem.getY() - elem.getFontSize(), approxWidth + 6, elem.getFontSize() + 4);
                    }

                    g2.setColor(elem == selectedElement ? Color.BLUE : Color.BLACK);
                    g2.drawString(elem.getConteudo() != null ? elem.getConteudo() : "", elem.getX(), elem.getY());
                }
            }
        }
    }
}
