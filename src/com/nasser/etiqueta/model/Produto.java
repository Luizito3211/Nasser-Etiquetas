package com.nasser.etiqueta.model;

/**
 * Classe que representa um modelo de produto cadastrado no sistema.
 * Contém informações básicas necessárias para gerar as etiquetas de validade.
 */
public class Produto {
    private String nome;
    private String sif;
    private String armazenamento;
    private int diasValidade;

    public Produto() {
    }

    public Produto(String nome, String sif, String armazenamento, int diasValidade) {
        this.nome = nome;
        this.sif = sif;
        this.armazenamento = armazenamento;
        this.diasValidade = diasValidade;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSif() {
        return sif;
    }

    public void setSif(String sif) {
        this.sif = sif;
    }

    public String getArmazenamento() {
        return armazenamento;
    }

    public void setArmazenamento(String armazenamento) {
        this.armazenamento = armazenamento;
    }

    public int getDiasValidade() {
        return diasValidade;
    }

    public void setDiasValidade(int diasValidade) {
        this.diasValidade = diasValidade;
    }

    @Override
    public String toString() {
        return nome;
    }
}
