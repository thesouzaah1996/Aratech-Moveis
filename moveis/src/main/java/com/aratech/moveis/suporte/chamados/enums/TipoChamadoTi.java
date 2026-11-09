package com.aratech.moveis.suporte.chamados.enums;

public enum TipoChamadoTi {
    ACESSO("Acesso / Permissão"),
    HARDWARE("Hardware"),
    SOFTWARE("Software / Sistema"),
    REDE("Rede / Internet"),
    IMPRESSORA("Impressora"),
    COMUNICACAO("E-mail / Comunicação"),
    OUTROS("Outros");
    private final String rotulo;

    TipoChamadoTi(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
