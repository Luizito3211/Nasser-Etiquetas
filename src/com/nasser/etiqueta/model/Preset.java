package com.nasser.etiqueta.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A named set of label quantities keyed by the stable product identifier.
 */
public class Preset {
    private final String nome;
    private final Map<String, Integer> quantidadesPorProduto;

    public Preset(String nome, Map<String, Integer> quantidadesPorProduto) {
        this.nome = nome;
        this.quantidadesPorProduto = new LinkedHashMap<>(quantidadesPorProduto);
    }

    public String getNome() {
        return nome;
    }

    public Map<String, Integer> getQuantidadesPorProduto() {
        return new LinkedHashMap<>(quantidadesPorProduto);
    }

    @Override
    public String toString() {
        return nome;
    }
}
