package com.aratech.moveis.almoxarifado.fornecedores.dto;

public record FornecedorResumoDTO(
        Long id,
        String nome,
        String email,
        String telefone,
        RepresentanteDTO representante,
        Boolean ativo) {}
