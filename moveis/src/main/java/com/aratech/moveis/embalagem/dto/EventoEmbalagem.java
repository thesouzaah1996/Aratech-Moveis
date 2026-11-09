package com.aratech.moveis.embalagem.dto;

import java.time.Instant;
public record EventoEmbalagem(long id,Long etapaId,String acao,String detalhe,String autor,Instant criadoEm) {}
