package com.aratech.moveis.embalagem.dto;

import java.time.LocalDate;
public record TarefaEsteira(long etapaId,long loteId,long versao,int sequencia,String modelo,
        int quantidade,LocalDate previsao,String prioridade,String nome,String instrucoes,
        String status,String operador,String motivoPausa,long segundosAtivos,boolean ultima) {}
