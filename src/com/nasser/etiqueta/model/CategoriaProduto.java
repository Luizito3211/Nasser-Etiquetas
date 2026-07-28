package com.nasser.etiqueta.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma pasta/categoria de produtos no sistema de etiquetas.
 */
public class CategoriaProduto {
    private String nome;
    private List<Produto> produtos;
    private boolean expanded = true;

    public CategoriaProduto() {
        this.produtos = new ArrayList<>();
    }

    public CategoriaProduto(String nome) {
        this.nome = nome;
        this.produtos = new ArrayList<>();
    }

    public CategoriaProduto(String nome, List<Produto> produtos) {
        this.nome = nome;
        this.produtos = produtos != null ? produtos : new ArrayList<>();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Produto> getProdutos() {
        return produtos;
    }

    public void setProdutos(List<Produto> produtos) {
        this.produtos = produtos;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public String toString() {
        return nome;
    }
}
