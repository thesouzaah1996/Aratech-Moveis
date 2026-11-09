package com.aratech.moveis.embalagem.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description="Versão atual do lote é exigida em todas as operações. Pausar exige motivo de 5 a 500 caracteres. Concluir a última etapa exige volumes e as três conferências verdadeiras; demais etapas dispensam essas informações.")
public record ComandoEmbalagem(@Min(1) long versao,@Size(max=500) String motivo,
        @Min(1) @Max(100000) Integer volumes,boolean pecasConferidas,
        boolean protecaoAplicada,boolean identificacaoConferida) {}
