package com.nasser.etiqueta;

/**
 * Ponto de entrada independente para execução a partir de Fat JARs (maven-shade-plugin).
 * Evita o erro de inicialização do JavaFX ("JavaFX runtime components are missing")
 * ao não herdar diretamente de javafx.application.Application.
 */
public class Launcher {

    public static void main(String[] args) {
        MainApp.main(args);
    }
}
