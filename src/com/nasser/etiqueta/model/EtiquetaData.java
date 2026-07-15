package com.nasser.etiqueta.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Record que representa os dados a serem mesclados na etiqueta física.
 */
public record EtiquetaData(
        String nomeProduto,
        String sif,
        String armazenamento,
        LocalDateTime dataFabricacao,
        LocalDateTime dataValidade,
        String responsavel
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public String getDataFabricacaoFormatada() {
        return dataFabricacao != null ? dataFabricacao.format(FORMATTER) : "";
    }

    public String getDataValidadeFormatada() {
        return dataValidade != null ? dataValidade.format(FORMATTER) : "";
    }
}
