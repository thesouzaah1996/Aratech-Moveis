package com.aratech.moveis.embalagem.dto;

import java.time.LocalDate;
public record ResumoEmbalagem(long id,long origemId,String modelo,int quantidade,
        LocalDate previsao,String prioridade,String status,long versao,int concluidas,int totalEtapas,
        String etapaAtual,String esteiraAtual,Integer volumes) {}
