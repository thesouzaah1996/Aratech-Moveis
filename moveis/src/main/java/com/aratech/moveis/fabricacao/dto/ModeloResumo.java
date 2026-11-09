package com.aratech.moveis.fabricacao.dto;

public record ModeloResumo(long id, String codigo, String nome, boolean ativo, long versao,
        int itens, Long capaId) {}
