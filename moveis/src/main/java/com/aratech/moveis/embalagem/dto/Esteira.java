package com.aratech.moveis.embalagem.dto;

public record Esteira(long id,String codigo,String nome,String atividade,String responsavel,
        int capacidade,boolean ativo,String observacoes,long versao) {}
