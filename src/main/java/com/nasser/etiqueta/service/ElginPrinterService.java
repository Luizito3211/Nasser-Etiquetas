package com.nasser.etiqueta.service;

import com.nasser.etiqueta.model.EtiquetaData;
import com.nasser.etiqueta.model.ElementoLayout;
import javax.print.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serviço de integração com a impressora Elgin L42 Pro Full.
 * Renderiza o layout dinâmico e livre como bitmap e converte para comandos gráficos ZPL nativos (^GF).
 */
public class ElginPrinterService {

    private static final Logger LOGGER = Logger.getLogger(ElginPrinterService.class.getName());

    public static class ElginPrintException extends Exception {
        public ElginPrintException(String message) {
            super(message);
        }

        public ElginPrintException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Lista os nomes de todas as impressoras instaladas no sistema operacional.
     */
    public List<String> listPrinters() {
        List<String> printerNames = new ArrayList<>();
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : services) {
            printerNames.add(service.getName());
        }
        return printerNames;
    }

    /**
     * Encontra um serviço de impressão pelo nome.
     */
    public PrintService findPrintService(String printerName) {
        if (printerName == null || printerName.isBlank()) {
            return null;
        }
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : services) {
            if (service.getName().equalsIgnoreCase(printerName.trim())) {
                return service;
            }
        }
        return null;
    }

    /**
     * Renderiza a etiqueta livre em uma imagem bitmap (480x320 pixels).
     *
     * @param elements Elementos cadastrados de forma livre (TEXTO, TAG, LINHA).
     * @param data     Dados reais preenchidos (null para exibir tags puras no editor).
     * @return BufferedImage representando a etiqueta.
     */
    public BufferedImage renderLabelImage(List<ElementoLayout> elements, EtiquetaData data) {
        BufferedImage image = new BufferedImage(480, 320, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 1. Fundo Branco
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 480, 320);

        // 2. Anti-aliasing para qualidade gráfica
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 3. Desenha os Elementos Livres (Textos, Tags e Linhas)
        for (ElementoLayout elem : elements) {
            if ("LINHA".equals(elem.getTipo())) {
                // Desenha linha horizontal customizada
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(elem.getThickness()));
                g.drawLine(elem.getX(), elem.getY(), elem.getX2(), elem.getY());
            } else if ("IMAGEM".equals(elem.getTipo())) {
                // Desenha imagem inserida na etiqueta
                if (elem.getConteudo() != null && !elem.getConteudo().isEmpty()) {
                    try {
                        File imgFile = new File(elem.getConteudo());
                        if (imgFile.exists()) {
                            BufferedImage img = ImageIO.read(imgFile);
                            if (img != null) {
                                int w = elem.getX2() > 0 ? elem.getX2() : img.getWidth();
                                int h = elem.getThickness() > 0 ? elem.getThickness() : img.getHeight();
                                g.drawImage(img, elem.getX(), elem.getY(), w, h, null);
                            }
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Erro ao desenhar imagem na impressão: " + ex.getMessage(), ex);
                    }
                }
            } else {
                // Desenha textos fixos ou tags dinâmicas
                String val = elem.getConteudo();
                
                if (data != null && val != null) {
                    String sifStr = (data.sif() == null || data.sif().trim().equalsIgnoreCase("N/A"))
                            ? "" : data.sif().trim();
                    // Never remove static text: a standalone "S.I.F.:" element remains visible
                    // and a combined "S.I.F.: {Sif}" element keeps its prefix when SIF is empty.
                    val = val.replace("{PRODUTO}", data.nomeProduto() != null ? data.nomeProduto() : "")
                             .replace("{Produto}", data.nomeProduto() != null ? data.nomeProduto() : "")
                             .replace("{SIF}", sifStr)
                             .replace("{Sif}", sifStr)
                             .replace("{ARMAZENAMENTO}", data.armazenamento() != null ? data.armazenamento() : "")
                             .replace("{Armazenamento}", data.armazenamento() != null ? data.armazenamento() : "")
                             .replace("{FABRICACAO}", data.getDataFabricacaoFormatada() != null ? data.getDataFabricacaoFormatada() : "")
                             .replace("{Fabricacao}", data.getDataFabricacaoFormatada() != null ? data.getDataFabricacaoFormatada() : "")
                             .replace("{VALIDADE}", data.getDataValidadeFormatada() != null ? data.getDataValidadeFormatada() : "")
                             .replace("{Validade}", data.getDataValidadeFormatada() != null ? data.getDataValidadeFormatada() : "")
                             .replace("{RESPONSAVEL}", data.responsavel() != null ? data.responsavel() : "")
                             .replace("{Responsavel}", data.responsavel() != null ? data.responsavel() : "");
                }

                if (val == null) {
                    val = "";
                }

                int fontStyle = elem.isBold() ? Font.BOLD : Font.PLAIN;
                g.setFont(new Font("SansSerif", fontStyle, elem.getFontSize()));
                g.setColor(Color.BLACK);
                g.drawString(val, elem.getX(), elem.getY());
            }
        }

        g.dispose();
        return image;
    }

    /**
     * Converte imagem BufferedImage para comandos gráficos nativos ZPL II (^GF).
     */
    public String convertImageToZplGF(BufferedImage image, int quantidade) {
        int width = image.getWidth();
        int height = image.getHeight();
        int bytesPerRow = (width + 7) / 8; // 60 bytes
        int totalBytes = bytesPerRow * height; // 19200 bytes

        StringBuilder sb = new StringBuilder();
        sb.append("^XA\n");
        sb.append("^CI28\n"); // Codificação UTF-8
        sb.append("^PW480\n");
        sb.append("^LL320\n");
        sb.append("^FO0,0^GFA,").append(totalBytes).append(",").append(totalBytes).append(",").append(bytesPerRow).append(",");

        for (int y = 0; y < height; y++) {
            for (int byteIdx = 0; byteIdx < bytesPerRow; byteIdx++) {
                int currentByte = 0;
                for (int bit = 0; bit < 8; bit++) {
                    int x = byteIdx * 8 + bit;
                    if (x < width) {
                        int rgb = image.getRGB(x, y);
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;
                        
                        // Luminância para binarização
                        int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                        if (gray < 180) {
                            currentByte |= (1 << (7 - bit));
                        }
                    }
                }
                sb.append(String.format("%02X", currentByte));
            }
            sb.append("\n");
        }

        sb.append("^FS\n");
        if (quantidade > 1) {
            sb.append("^PQ").append(quantidade).append("\n");
        }
        sb.append("^XZ");
        return sb.toString();
    }

    /**
     * Imprime a etiqueta livre gerando e enviando comandos de imagem.
     */
    public void printVisualLabel(String printerName, List<ElementoLayout> elements, EtiquetaData data, int quantidade) throws ElginPrintException {
        Objects.requireNonNull(elements, "A lista de elementos não pode ser nula.");
        Objects.requireNonNull(data, "Os dados da etiqueta não podem ser nulos.");

        PrintService printService = findPrintService(printerName);
        if (printService == null) {
            String msg = "A impressora '" + printerName + "' não foi encontrada no sistema operacional.";
            LOGGER.warning(msg);
            throw new ElginPrintException(msg);
        }

        try {
            BufferedImage image = renderLabelImage(elements, data);
            String zplCommands = convertImageToZplGF(image, quantidade);

            byte[] bytes = zplCommands.getBytes(StandardCharsets.US_ASCII);
            DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            Doc doc = new SimpleDoc(bytes, flavor, null);

            DocPrintJob job = printService.createPrintJob();
            job.print(doc, null);
            LOGGER.info("Etiqueta impressa com sucesso: " + data.nomeProduto() + " (qtd: " + quantidade + ", impressora: " + printerName + ")");

        } catch (PrintException e) {
            String msg = "Erro ao enviar comandos ZPL de imagem gráfica ao Spooler da impressora '" + printerName + "'. Verifique se a impressora está ligada, conectada e com papel.";
            LOGGER.log(Level.SEVERE, msg, e);
            throw new ElginPrintException(msg, e);
        } catch (Exception e) {
            String msg = "Erro no pipeline de impressão gráfica: " + e.getMessage();
            LOGGER.log(Level.SEVERE, msg, e);
            throw new ElginPrintException(msg, e);
        }
    }
}
