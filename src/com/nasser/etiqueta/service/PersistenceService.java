package com.nasser.etiqueta.service;

import com.nasser.etiqueta.model.CategoriaProduto;
import com.nasser.etiqueta.model.ElementoLayout;
import com.nasser.etiqueta.model.Produto;
import com.nasser.etiqueta.model.Preset;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Serviço responsável pela persistência local das configurações, produtos e
 * elementos de layout.
 * Não utiliza dependências externas para manter a compatibilidade com Java SE
 * puro.
 */
public class PersistenceService {

    private static final String CONFIG_DIR = "config";
    private static final String PRODUCTS_FILE = CONFIG_DIR + "/produtos.txt";
    private static final String LAYOUT_FILE = CONFIG_DIR + "/layout.zpl";
    private static final String APP_PROPERTIES_FILE = CONFIG_DIR + "/app.properties";
    private static final String LAYOUT_ELEMENTS_FILE = CONFIG_DIR + "/layout_elements.txt";
    private static final String PRESETS_FILE = CONFIG_DIR + "/presets.txt";

    public PersistenceService() {
        init();
    }

    /**
     * Inicializa a pasta de configuração e cria os arquivos padrões caso não
     * existam.
     */
    private void init() {
        try {
            Files.createDirectories(Paths.get(CONFIG_DIR));

            // Cria lista padrão de produtos se o arquivo não existir
            File prodFile = new File(PRODUCTS_FILE);
            if (!prodFile.exists()) {
                writeDefaultProducts();
            }

            // Cria layout padrão ZPL se o arquivo não existir
            File layoutFile = new File(LAYOUT_FILE);
            if (!layoutFile.exists()) {
                writeDefaultLayout();
            }

            // Cria arquivo de propriedades da aplicação
            File appFile = new File(APP_PROPERTIES_FILE);
            if (!appFile.exists()) {
                writeDefaultProperties();
            }

            // Cria arquivo de elementos do layout visual
            File elemFile = new File(LAYOUT_ELEMENTS_FILE);
            if (!elemFile.exists()) {
                writeDefaultLayoutElements();
            } else {
                // Se existe, verifica se é do formato antigo de 5 campos. Se for, força
                // reescrever.
                try {
                    List<String> lines = Files.readAllLines(Paths.get(LAYOUT_ELEMENTS_FILE), StandardCharsets.UTF_8);
                    if (!lines.isEmpty() && lines.get(0).split(";").length < 8) {
                        System.out.println("Formato antigo detectado em layout_elements.txt. Atualizando...");
                        writeDefaultLayoutElements();
                    }
                } catch (Exception ignored) {
                }
            }

        } catch (IOException e) {
            System.err.println("Erro ao inicializar o diretório de configurações: " + e.getMessage());
        }
    }

    private void writeDefaultProducts() throws IOException {
        List<String> defaultProducts = List.of(
                "Carne Temperada;12345/PR-SIF;Refrigerado (até 4°C);3",
                "Queijo;;Refrigerado (até 4°C);2",
                "Frango;;Refrigerado (até 4°C);3",
                "Maionese;;Refrigerado (até 4°C);1",
                "Massa;;Congelado (abaixo de -12°C);5");
        Files.write(Paths.get(PRODUCTS_FILE), defaultProducts, StandardCharsets.UTF_8);
    }

    private void writeDefaultLayout() throws IOException {
        String defaultZpl = """
                ^XA
                ^CI28
                ^PW480
                ^LL320
                ^LH0,0
                ^FO30,30^A0N,28,28^FB420,2,0,C^FD{PRODUTO}^FS
                ^FO30,90^GB420,2,2^FS
                ^FO30,105^A0N,18,18^FDS.I.F.: {SIF}^FS
                ^FO30,130^A0N,18,18^FDArmaz.: {ARMAZENAMENTO}^FS
                ^FO30,155^GB420,1,1^FS
                ^FO30,165^A0N,18,18^FDFabr.: {FABRICACAO}^FS
                ^FO30,195^A0N,24,24^FDVALIDADE: {VALIDADE}^FS
                ^FO30,235^GB420,1,1^FS
                ^FO30,245^A0N,18,18^FDResp.: {RESPONSAVEL}^FS
                ^XZ""";
        Files.writeString(Paths.get(LAYOUT_FILE), defaultZpl, StandardCharsets.UTF_8);
    }

    private void writeDefaultProperties() throws IOException {
        Properties props = new Properties();
        props.setProperty("selected.printer", "");
        try (OutputStream out = new FileOutputStream(APP_PROPERTIES_FILE)) {
            props.store(out, "Configuracoes do Aplicativo");
        }
    }

    private void writeDefaultLayoutElements() throws IOException {
        List<String> defaultElements = List.of(
                "TAG;{Produto};20;48;0;28;true;1",
                "LINHA;;20;65;460;0;false;2",
                "TEXTO;S.I.F.:;20;95;0;16;true;1",
                "TAG;{Sif};85;95;0;16;false;1",
                "TEXTO;Armazenamento:;210;95;0;16;true;1",
                "TAG;{Armazenamento};350;95;0;16;false;1",
                "LINHA;;20;115;460;0;false;1",
                "TEXTO;Fabricação:;20;155;0;18;true;1",
                "TAG;{Fabricacao};150;155;0;18;true;1",
                "TEXTO;Validade:;20;205;0;22;true;1",
                "TAG;{Validade};130;205;0;24;true;1",
                "LINHA;;20;235;460;0;false;1",
                "TEXTO;Responsável:;20;275;0;18;true;1",
                "TAG;{Responsavel};160;275;0;18;false;1");
        Files.write(Paths.get(LAYOUT_ELEMENTS_FILE), defaultElements, StandardCharsets.UTF_8);
    }

    /**
     * Carrega a lista de categorias e seus produtos salvos localmente.
     */
    public List<CategoriaProduto> loadCategorias() {
        List<CategoriaProduto> categorias = new ArrayList<>();
        CategoriaProduto currentCategory = null;
        boolean productIdsMigrated = false;

        try {
            if (Files.exists(Paths.get(PRODUCTS_FILE))) {
                List<String> lines = Files.readAllLines(Paths.get(PRODUCTS_FILE), StandardCharsets.UTF_8);
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty())
                        continue;

                    if (trimmed.startsWith("[CATEGORIA:") && trimmed.endsWith("]")) {
                        String catName = trimmed.substring(11, trimmed.length() - 1).trim();
                        currentCategory = new CategoriaProduto(catName);
                        categorias.add(currentCategory);
                    } else if (trimmed.startsWith("FOLDER:")) {
                        String catName = trimmed.substring(7).trim();
                        currentCategory = new CategoriaProduto(catName);
                        categorias.add(currentCategory);
                    } else {
                        String[] parts = trimmed.split(";", -1);
                        if (parts.length >= 4) {
                            String nome = parts[0];
                            String sif = parts[1];
                            String armazenamento = parts[2];
                            int dias = Integer.parseInt(parts[3]);
                            String id = parts.length >= 5 ? parts[4] : null;
                            if (id == null || id.isBlank()) {
                                productIdsMigrated = true;
                            }
                            Produto p = new Produto(id, nome, sif, armazenamento, dias);

                            if (currentCategory == null) {
                                currentCategory = new CategoriaProduto("Nasser Esfihas");
                                categorias.add(currentCategory);
                            }
                            currentCategory.getProdutos().add(p);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar categorias/produtos: " + e.getMessage());
        }

        if (categorias.isEmpty()) {
            CategoriaProduto defaultCat = new CategoriaProduto("Nasser Esfihas");
            defaultCat.getProdutos().add(new Produto("Carne Temperada", "12345/PR-SIF", "Refrigerado (até 4°C)", 3));
            defaultCat.getProdutos().add(new Produto("Queijo", "", "Refrigerado (até 4°C)", 2));
            defaultCat.getProdutos().add(new Produto("Frango", "", "Refrigerado (até 4°C)", 3));
            defaultCat.getProdutos().add(new Produto("Maionese", "", "Refrigerado (até 4°C)", 1));
            defaultCat.getProdutos().add(new Produto("Massa", "", "Congelado (abaixo de -12°C)", 5));
            categorias.add(defaultCat);
            saveCategorias(categorias);
        } else if (productIdsMigrated) {
            // Upgrade legacy four-field records once so preset product IDs survive restarts.
            saveCategorias(categorias);
        }

        return categorias;
    }

    /**
     * Salva a estrutura de categorias e produtos no arquivo local.
     */
    public void saveCategorias(List<CategoriaProduto> categorias) {
        try {
            List<String> lines = new ArrayList<>();
            for (CategoriaProduto cat : categorias) {
                lines.add("[CATEGORIA:" + cat.getNome() + "]");
                for (Produto p : cat.getProdutos()) {
                    lines.add(String.format("%s;%s;%s;%d;%s",
                            p.getNome(),
                            p.getSif() != null ? p.getSif() : "",
                            p.getArmazenamento() != null ? p.getArmazenamento() : "",
                            p.getDiasValidade(),
                            p.getId()));
                }
            }
            Files.write(Paths.get(PRODUCTS_FILE), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Erro ao salvar categorias e produtos: " + e.getMessage());
        }
    }

    /**
     * Mantém compatibilidade com o carregamento simples de produtos acumulados.
     */
    public List<Produto> loadProdutos() {
        List<Produto> todos = new ArrayList<>();
        for (CategoriaProduto cat : loadCategorias()) {
            todos.addAll(cat.getProdutos());
        }
        return todos;
    }

    public void saveProdutos(List<Produto> produtos) {
        List<CategoriaProduto> cats = loadCategorias();
        if (cats.isEmpty()) {
            cats.add(new CategoriaProduto("Nasser Esfihas", produtos));
        } else {
            cats.get(0).setProdutos(produtos);
        }
        saveCategorias(cats);
    }

    /**
     * Loads presets stored as one UTF-8 line per preset: name<TAB>productId=quantity,...
     */
    public List<Preset> loadPresets() {
        List<Preset> presets = new ArrayList<>();
        try {
            Path path = Paths.get(PRESETS_FILE);
            if (!Files.exists(path)) {
                return presets;
            }
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                String[] parts = line.split("\\t", 2);
                if (parts.length != 2 || parts[0].isBlank()) continue;
                Map<String, Integer> quantities = new LinkedHashMap<>();
                for (String entry : parts[1].split(",")) {
                    String[] quantityParts = entry.split("=", 2);
                    if (quantityParts.length != 2) continue;
                    try {
                        int quantity = Integer.parseInt(quantityParts[1]);
                        if (quantity > 0 && !quantityParts[0].isBlank()) {
                            quantities.put(quantityParts[0], quantity);
                        }
                    } catch (NumberFormatException ignored) {
                        // Ignore an invalid entry while preserving valid presets.
                    }
                }
                presets.add(new Preset(parts[0], quantities));
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar presets: " + e.getMessage());
        }
        return presets;
    }

    public void savePresets(List<Preset> presets) {
        try {
            List<String> lines = new ArrayList<>();
            for (Preset preset : presets) {
                if (preset.getNome() == null || preset.getNome().isBlank()) continue;
                List<String> entries = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : preset.getQuantidadesPorProduto().entrySet()) {
                    if (entry.getKey() != null && !entry.getKey().isBlank() && entry.getValue() != null && entry.getValue() > 0) {
                        entries.add(entry.getKey() + "=" + entry.getValue());
                    }
                }
                lines.add(preset.getNome().trim() + "\\t" + String.join(",", entries));
            }
            Files.write(Paths.get(PRESETS_FILE), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Erro ao salvar presets: " + e.getMessage());
        }
    }

    /**
     * Carrega os elementos visuais do layout de etiquetas.
     */
    public List<ElementoLayout> loadLayoutElements() {
        List<ElementoLayout> list = new ArrayList<>();
        try {
            if (Files.exists(Paths.get(LAYOUT_ELEMENTS_FILE))) {
                List<String> lines = Files.readAllLines(Paths.get(LAYOUT_ELEMENTS_FILE), StandardCharsets.UTF_8);
                for (String line : lines) {
                    if (line.trim().isEmpty())
                        continue;
                    String[] parts = line.split(";", -1);
                    if (parts.length >= 8) {
                        String tipo = parts[0];
                        String conteudo = parts[1];
                        int x = Integer.parseInt(parts[2]);
                        int y = Integer.parseInt(parts[3]);
                        int x2 = Integer.parseInt(parts[4]);
                        int fontSize = Integer.parseInt(parts[5]);
                        boolean bold = Boolean.parseBoolean(parts[6]);
                        int thickness = Integer.parseInt(parts[7]);
                        list.add(new ElementoLayout(tipo, conteudo, x, y, x2, fontSize, bold, thickness));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar elementos do layout: " + e.getMessage());
        }

        if (list.isEmpty()) {
            list = createDefaultLayoutElementsList();
            saveLayoutElements(list);
        }
        return list;
    }

    /**
     * Salva a configuração dos elementos visuais no arquivo local.
     */
    public void saveLayoutElements(List<ElementoLayout> elements) {
        try {
            List<String> lines = new ArrayList<>();
            for (ElementoLayout e : elements) {
                lines.add(String.format("%s;%s;%d;%d;%d;%d;%b;%d",
                        e.getTipo(),
                        e.getConteudo() != null ? e.getConteudo() : "",
                        e.getX(),
                        e.getY(),
                        e.getX2(),
                        e.getFontSize(),
                        e.isBold(),
                        e.getThickness()));
            }
            Files.write(Paths.get(LAYOUT_ELEMENTS_FILE), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Erro ao salvar elementos de layout: " + e.getMessage());
        }
    }

    private List<ElementoLayout> createDefaultLayoutElementsList() {
        List<ElementoLayout> defaultList = new ArrayList<>();
        defaultList.add(new ElementoLayout("TAG", "{Produto}", 20, 48, 0, 28, true, 1));
        defaultList.add(new ElementoLayout("LINHA", "", 20, 65, 460, 0, false, 2));
        defaultList.add(new ElementoLayout("TEXTO", "S.I.F.:", 20, 95, 0, 16, true, 1));
        defaultList.add(new ElementoLayout("TAG", "{Sif}", 85, 95, 0, 16, false, 1));
        defaultList.add(new ElementoLayout("TEXTO", "Armazenamento:", 210, 95, 0, 16, true, 1));
        defaultList.add(new ElementoLayout("TAG", "{Armazenamento}", 350, 95, 0, 16, false, 1));
        defaultList.add(new ElementoLayout("LINHA", "", 20, 115, 460, 0, false, 1));
        defaultList.add(new ElementoLayout("TEXTO", "Fabricação:", 20, 155, 0, 18, true, 1));
        defaultList.add(new ElementoLayout("TAG", "{Fabricacao}", 150, 155, 0, 18, true, 1));
        defaultList.add(new ElementoLayout("TEXTO", "Validade:", 20, 205, 0, 22, true, 1));
        defaultList.add(new ElementoLayout("TAG", "{Validade}", 130, 205, 0, 24, true, 1));
        defaultList.add(new ElementoLayout("LINHA", "", 20, 235, 460, 0, false, 1));
        defaultList.add(new ElementoLayout("TEXTO", "Responsável:", 20, 275, 0, 18, true, 1));
        defaultList.add(new ElementoLayout("TAG", "{Responsavel}", 160, 275, 0, 18, false, 1));
        return defaultList;
    }

    /**
     * Retorna a impressora padrão selecionada anteriormente.
     */
    public String getSelectedPrinter() {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(APP_PROPERTIES_FILE)) {
            props.load(in);
            return props.getProperty("selected.printer", "");
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * Salva a impressora selecionada no arquivo de propriedades.
     */
    public void saveSelectedPrinter(String printerName) {
        Properties props = new Properties();
        try {
            try (InputStream in = new FileInputStream(APP_PROPERTIES_FILE)) {
                props.load(in);
            }
        } catch (IOException e) {
            // Ignora se o arquivo não existir
        }

        props.setProperty("selected.printer", printerName != null ? printerName : "");

        try (OutputStream out = new FileOutputStream(APP_PROPERTIES_FILE)) {
            props.store(out, "Configuracoes do Aplicativo");
        } catch (IOException e) {
            System.err.println("Erro ao salvar impressora selecionada: " + e.getMessage());
        }
    }
}
