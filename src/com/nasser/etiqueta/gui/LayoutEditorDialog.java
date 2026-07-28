package com.nasser.etiqueta.gui;

import com.nasser.etiqueta.model.ElementoLayout;
import com.nasser.etiqueta.service.ElginPrinterService;
import com.nasser.etiqueta.service.PersistenceService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private JButton btnAddImage;

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
    private JSpinner spinFontSize;
    private JCheckBox chkBold;

    // Controles de Linha
    private JPanel linePropertiesPanel;
    private JSpinner spinX2;
    private JSpinner spinThickness;

    // Controles de Imagem
    private JPanel imagePropertiesPanel;
    private JSpinner spinImgWidth;
    private JSpinner spinImgHeight;
    private JButton btnChangeImage;

    // Cache de imagens em memória para performance do canvas
    private final Map<String, BufferedImage> imageCache = new HashMap<>();

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

        btnAddImage = new JButton("+ Imagem");
        btnAddImage.setFont(new Font("SansSerif", Font.BOLD, 12));
        DarkThemeHelper.styleButton(btnAddImage, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.TEXT_PRIMARY);

        // O Canvas 480x320
        canvasPanel = new LabelCanvas();

        // Painel de Propriedades Comuns
        String[] tags = new String[elements.size()];
        for (int i = 0; i < elements.size(); i++) {
            tags[i] = elements.get(i).getTipo() + ": " + (elements.get(i).getConteudo() != null ? elements.get(i).getConteudo() : "Elemento");
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

        spinFontSize = new JSpinner(new SpinnerNumberModel(12, 6, 72, 1));
        DarkThemeHelper.styleSpinner(spinFontSize);

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

        // --- Painel Propriedades de Imagem ---
        imagePropertiesPanel = new JPanel(new GridBagLayout());
        imagePropertiesPanel.setOpaque(false);

        spinImgWidth = new JSpinner(new SpinnerNumberModel(100, 10, 480, 1));
        DarkThemeHelper.styleSpinner(spinImgWidth);

        spinImgHeight = new JSpinner(new SpinnerNumberModel(100, 10, 320, 1));
        DarkThemeHelper.styleSpinner(spinImgHeight);

        btnChangeImage = new JButton("Alterar Imagem...");
        btnChangeImage.setFont(new Font("SansSerif", Font.PLAIN, 12));
        DarkThemeHelper.styleButton(btnChangeImage, DarkThemeHelper.COMPONENT_BG, DarkThemeHelper.TEXT_PRIMARY);

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
        toolsPanel.add(btnAddImage);
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
        tGbc.gridwidth = 1;
        tGbc.weightx = 0.0;
        JLabel lblFontSize = new JLabel("Fonte (pt):");
        DarkThemeHelper.styleLabel(lblFontSize, DarkThemeHelper.TEXT_PRIMARY, Font.PLAIN, 12);
        textPropertiesPanel.add(lblFontSize, tGbc);

        tGbc.gridx = 1;
        tGbc.weightx = 1.0;
        textPropertiesPanel.add(spinFontSize, tGbc);

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

        // Subpainel Dinâmico: Imagem
        GridBagConstraints imGbc = new GridBagConstraints();
        imGbc.fill = GridBagConstraints.HORIZONTAL;
        imGbc.insets = new Insets(4, 4, 4, 4);

        imGbc.gridx = 0;
        imGbc.gridy = 0;
        imGbc.weightx = 0.0;
        JLabel lblWidth = new JLabel("Largura (px):");
        DarkThemeHelper.styleLabel(lblWidth, DarkThemeHelper.TEXT_PRIMARY, Font.PLAIN, 12);
        imagePropertiesPanel.add(lblWidth, imGbc);

        imGbc.gridx = 1;
        imGbc.weightx = 1.0;
        imagePropertiesPanel.add(spinImgWidth, imGbc);

        imGbc.gridx = 0;
        imGbc.gridy = 1;
        imGbc.weightx = 0.0;
        JLabel lblHeight = new JLabel("Altura (px):");
        DarkThemeHelper.styleLabel(lblHeight, DarkThemeHelper.TEXT_PRIMARY, Font.PLAIN, 12);
        imagePropertiesPanel.add(lblHeight, imGbc);

        imGbc.gridx = 1;
        imGbc.weightx = 1.0;
        imagePropertiesPanel.add(spinImgHeight, imGbc);

        imGbc.gridx = 0;
        imGbc.gridy = 2;
        imGbc.gridwidth = 2;
        imagePropertiesPanel.add(btnChangeImage, imGbc);

        // Adiciona os subpainéis dinâmicos
        cGbc.gridx = 0;
        cGbc.gridy = row++;
        cGbc.gridwidth = 2;
        cGbc.weightx = 1.0;
        controlPanel.add(textPropertiesPanel, cGbc);

        cGbc.gridy = row++;
        controlPanel.add(linePropertiesPanel, cGbc);

        cGbc.gridy = row++;
        controlPanel.add(imagePropertiesPanel, cGbc);

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

        btnAddImage.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Selecionar Imagem para a Etiqueta");
            fc.setFileFilter(new FileNameExtensionFilter("Imagens (PNG, JPG, BMP)", "png", "jpg", "jpeg", "bmp"));
            int res = fc.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                int w = 100;
                int h = 100;
                try {
                    BufferedImage img = ImageIO.read(file);
                    if (img != null) {
                        w = img.getWidth();
                        h = img.getHeight();
                        if (w > 150 || h > 150) {
                            double scale = Math.min(150.0 / w, 150.0 / h);
                            w = Math.max(20, (int) (w * scale));
                            h = Math.max(20, (int) (h * scale));
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Erro ao ler dimensões da imagem: " + ex.getMessage());
                }
                adicionarElemento(new ElementoLayout("IMAGEM", file.getAbsolutePath(), 180, 100, w, 0, false, h));
            }
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

        spinImgWidth.addChangeListener(e -> {
            if (updatingControls || selectedElement == null) return;
            selectedElement.setX2((Integer) spinImgWidth.getValue());
            canvasPanel.repaint();
        });

        spinImgHeight.addChangeListener(e -> {
            if (updatingControls || selectedElement == null) return;
            selectedElement.setThickness((Integer) spinImgHeight.getValue());
            canvasPanel.repaint();
        });

        btnChangeImage.addActionListener(e -> {
            if (selectedElement == null || !"IMAGEM".equals(selectedElement.getTipo())) return;
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Alterar Imagem da Etiqueta");
            fc.setFileFilter(new FileNameExtensionFilter("Imagens (PNG, JPG, BMP)", "png", "jpg", "jpeg", "bmp"));
            int res = fc.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                selectedElement.setConteudo(file.getAbsolutePath());
                try {
                    BufferedImage img = ImageIO.read(file);
                    if (img != null) {
                        int w = img.getWidth();
                        int h = img.getHeight();
                        if (w > 150 || h > 150) {
                            double scale = Math.min(150.0 / w, 150.0 / h);
                            w = Math.max(20, (int) (w * scale));
                            h = Math.max(20, (int) (h * scale));
                        }
                        selectedElement.setX2(w);
                        selectedElement.setThickness(h);
                        spinImgWidth.setValue(w);
                        spinImgHeight.setValue(h);
                    }
                } catch (Exception ex) {
                    System.err.println("Erro ao alterar imagem: " + ex.getMessage());
                }
                rebuildSelectorList();
                canvasPanel.repaint();
            }
        });

        // Ações de texto em tempo real
        tfTextContent.addCaretListener(e -> {
            if (updatingControls || selectedElement == null) return;
            if ("TEXTO".equals(selectedElement.getTipo())) {
                selectedElement.setConteudo(tfTextContent.getText());
                canvasPanel.repaint();
            }
        });

        spinFontSize.addChangeListener(e -> {
            if (updatingControls || selectedElement == null) return;
            selectedElement.setFontSize((Integer) spinFontSize.getValue());
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
            String label = switch (elem.getTipo()) {
                case "LINHA" -> "Linha";
                case "IMAGEM" -> "Imagem: " + (elem.getConteudo() != null ? new File(elem.getConteudo()).getName() : "Sem arquivo");
                default -> (elem.getConteudo() != null && !elem.getConteudo().isEmpty() ? elem.getConteudo() : "Texto");
            };
            cbElementsSelector.addItem(elem.getTipo() + ": " + label);
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
            imagePropertiesPanel.setVisible(false);

            spinX2.setValue(selectedElement.getX2());
            spinThickness.setValue(selectedElement.getThickness());
        } else if ("IMAGEM".equals(selectedElement.getTipo())) {
            textPropertiesPanel.setVisible(false);
            linePropertiesPanel.setVisible(false);
            imagePropertiesPanel.setVisible(true);

            spinImgWidth.setValue(selectedElement.getX2());
            spinImgHeight.setValue(selectedElement.getThickness());
        } else {
            textPropertiesPanel.setVisible(true);
            linePropertiesPanel.setVisible(false);
            imagePropertiesPanel.setVisible(false);

            tfTextContent.setText(selectedElement.getConteudo());
            tfTextContent.setEnabled("TEXTO".equals(selectedElement.getTipo())); // Bloqueia conteúdo das tags no TF
            
            spinFontSize.setValue(selectedElement.getFontSize());
            chkBold.setSelected(selectedElement.isBold());
        }

        updatingControls = false;
        
        // Revalida os painéis para atualizar a disposição na tela
        textPropertiesPanel.revalidate();
        linePropertiesPanel.revalidate();
        imagePropertiesPanel.revalidate();
        getContentPane().repaint();
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
                        } else if ("IMAGEM".equals(elem.getTipo())) {
                            // Detecção de clique na caixa da imagem
                            int w = elem.getX2() > 0 ? elem.getX2() : 50;
                            int h = elem.getThickness() > 0 ? elem.getThickness() : 50;
                            Rectangle bounds = new Rectangle(elem.getX(), elem.getY(), w, h);
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
                        } else if ("IMAGEM".equals(dragElement.getTipo())) {
                            int w = dragElement.getX2() > 0 ? dragElement.getX2() : 50;
                            int h = dragElement.getThickness() > 0 ? dragElement.getThickness() : 50;
                            int newX = Math.max(0, Math.min(pt.x - offsetX, 480 - w));
                            int newY = Math.max(0, Math.min(pt.y - offsetY, 320 - h));

                            dragElement.setX(newX);
                            dragElement.setY(newY);
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
                        } else if ("IMAGEM".equals(dragElement.getTipo())) {
                            spinImgWidth.setValue(dragElement.getX2());
                            spinImgHeight.setValue(dragElement.getThickness());
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
                } else if ("IMAGEM".equals(elem.getTipo())) {
                    // Renderiza imagem
                    String path = elem.getConteudo();
                    BufferedImage img = null;
                    if (path != null && !path.isEmpty()) {
                        img = imageCache.get(path);
                        if (img == null) {
                            try {
                                File imgFile = new File(path);
                                if (imgFile.exists()) {
                                    img = ImageIO.read(imgFile);
                                    if (img != null) {
                                        imageCache.put(path, img);
                                    }
                                }
                            } catch (Exception ex) {
                                System.err.println("Erro ao carregar imagem no canvas: " + ex.getMessage());
                            }
                        }
                    }

                    int w = elem.getX2() > 0 ? elem.getX2() : 80;
                    int h = elem.getThickness() > 0 ? elem.getThickness() : 80;

                    if (img != null) {
                        g2.drawImage(img, elem.getX(), elem.getY(), w, h, null);
                    } else {
                        // Desenha quadro indicador se a imagem não puder ser lida
                        g2.setColor(new Color(240, 240, 240));
                        g2.fillRect(elem.getX(), elem.getY(), w, h);
                        g2.setColor(Color.GRAY);
                        g2.drawRect(elem.getX(), elem.getY(), w, h);
                        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                        g2.drawString("[Imagem]", elem.getX() + 4, elem.getY() + (h / 2));
                    }

                    // Marcação azul ao redor se selecionada
                    if (elem == selectedElement) {
                        g2.setColor(new Color(33, 150, 243, 60));
                        g2.fillRect(elem.getX() - 3, elem.getY() - 3, w + 6, h + 6);
                        g2.setColor(Color.BLUE);
                        g2.drawRect(elem.getX() - 3, elem.getY() - 3, w + 6, h + 6);
                    }
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
