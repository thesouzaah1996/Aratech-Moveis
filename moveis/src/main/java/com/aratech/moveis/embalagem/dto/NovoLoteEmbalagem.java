package com.aratech.moveis.embalagem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.*;
@Schema(description="Recebimento integral de um lote CORTADO, com confirmação explícita de que as peças passaram pelas etapas necessárias e estão prontas para embalagem. O corte concluído, sozinho, não representa montagem ou acabamento. Roteiro ordenado de 1 a 12 etapas, com esteiras ativas. Não divide nem duplica o lote de origem.")
public record NovoLoteEmbalagem(@Positive long origemId,@NotNull LocalDate previsao,
        @NotNull Prioridade prioridade,@Size(max=1000) String observacoes,
        @AssertTrue boolean prontoParaEmbalagem,@NotNull UUID chaveOperacao,
        @NotNull @Size(min=1,max=12) List<@NotNull @Valid EtapaEntrada> etapas) {
    public enum Prioridade { NORMAL,ALTA,URGENTE }
}
