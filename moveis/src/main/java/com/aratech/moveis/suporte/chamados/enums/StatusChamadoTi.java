package com.aratech.moveis.suporte.chamados.enums;

public enum StatusChamadoTi {
    ABERTO("Aberto"),
    EM_ATENDIMENTO("Em Atendimento"),
    CONCLUIDO("Concluído"),
    CANCELADO("Cancelado");
    private final String rotulo;

    StatusChamadoTi(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
