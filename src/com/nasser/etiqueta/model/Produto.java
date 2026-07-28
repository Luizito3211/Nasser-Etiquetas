package com.nasser.etiqueta.model;

import java.util.UUID;

/**
 * Classe que representa um modelo de produto cadastrado no sistema.
 * Contém informações básicas necessárias para gerar as etiquetas de validade.
 */
public class Produto {
    private String id;
    private String nome;
    private String sif;
    private String armazenamento;
    private int diasValidade;

    public Produto() {
        this.id = UUID.randomUUID().toString();
    }

    public Produto(String nome, String sif, String armazenamento, int diasValidade) {
        this(UUID.randomUUID().toString(), nome, sif, armazenamento, diasValidade);
    }

    public Produto(String id, String nome, String sif, String armazenamento, int diasValidade) {
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
        this.nome = nome;
        this.sif = sif;
        this.armazenamento = armazenamento;
        this.diasValidade = diasValidade;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
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
