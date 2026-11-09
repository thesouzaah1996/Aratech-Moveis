package com.aratech.moveis.fabricacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Ficha técnica do guarda-roupa, com até 200 componentes livres. Código único sem diferenciar maiúsculas. Atualização exige versão atual; fotos são enviadas separadamente. A ficha não executa otimização de chapas nem gera programa de máquina.")
public record FichaGuardaRoupa(
        @NotBlank @Size(max=60) String codigo,
        @NotBlank @Size(max=150) String nome,
        @Size(max=2000) String descricao,
        @NotNull @DecimalMin("1") @DecimalMax("10000") BigDecimal larguraMm,
        @NotNull @DecimalMin("1") @DecimalMax("10000") BigDecimal alturaMm,
        @NotNull @DecimalMin("1") @DecimalMax("10000") BigDecimal profundidadeMm,
        boolean ativo,
        @PositiveOrZero long versao,
        @NotNull @Size(min=1,max=200) List<@NotNull @Valid ItemGuardaRoupa> itens) {}
