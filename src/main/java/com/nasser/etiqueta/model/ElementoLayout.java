package com.nasser.etiqueta.model;

/**
 * Representa um elemento livre na etiqueta de 60x40mm.
 * Pode ser um Texto Fixo, uma Tag Dinâmica ou uma Linha Divisória.
 */
public class ElementoLayout {
    private String tipo;      // "TEXTO", "TAG", "LINHA"
    private String conteudo;  // Texto fixo (ex: "Validade:") ou tag (ex: "{Validade}") ou vazio para linhas
    private int x;            // Coordenada X (início)
    private int y;            // Coordenada Y (baseline para texto, posição Y para linha horizontal)
    private int x2;           // Coordenada X2 (fim para linha horizontal) ou comprimento
    private int fontSize;     // Tamanho da fonte (12, 16, 20, 26)
    private boolean bold;     // Negrito
    private int thickness;    // Espessura da linha horizontal (ex: 1, 2, 3)

    public ElementoLayout() {
    }

    public ElementoLayout(String tipo, String conteudo, int x, int y, int x2, int fontSize, boolean bold, int thickness) {
        this.tipo = tipo;
        this.conteudo = conteudo;
        this.x = x;
        this.y = y;
        this.x2 = x2;
        this.fontSize = fontSize;
        this.bold = bold;
        this.thickness = thickness;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public int getThickness() {
        return thickness;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
    }
}
