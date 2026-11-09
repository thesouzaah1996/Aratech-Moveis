package com.aratech.moveis.embalagem.dto;

import com.aratech.moveis.fabricacao.dto.ModeloGuardaRoupa;
import java.time.*;
import java.util.List;
public record LoteEmbalagem(long id,long origemId,String status,long versao,int quantidade,
        LocalDate previsao,String prioridade,String observacoes,Integer volumes,
        Instant criadoEm,Instant concluidoEm,ModeloGuardaRoupa modelo,
        List<EtapaEmbalagem> etapas,List<EventoEmbalagem> historico) {}
