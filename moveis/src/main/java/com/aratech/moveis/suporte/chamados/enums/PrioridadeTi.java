package com.aratech.moveis.suporte.chamados.enums;

public enum PrioridadeTi {
    BAIXA("Baixa"),
    MEDIA("Média"),
    ALTA("Alta"),
    CRITICA("Crítica");
    private final String rotulo;

    PrioridadeTi(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
