package com.aratech.moveis.embalagem.dto;

import java.time.Instant;
public record EtapaEmbalagem(long id,long loteId,int sequencia,long esteiraId,String esteira,
        String nome,String instrucoes,String status,String operador,long segundosAtivos,
        Instant iniciadoEm,Instant concluidoEm,String motivoPausa,boolean liberada) {}
