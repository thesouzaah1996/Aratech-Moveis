package com.aratech.moveis.fabricacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;

@Schema(description = "Componente por unidade de guarda-roupa. Medidas são finais de corte em mm, informadas e revisadas pelo projetista; não são calculadas a partir da medida externa. Bordas C1/C2 são os lados do comprimento e L1/L2 os lados da largura.")
public record ItemGuardaRoupa(
        @NotNull UUID chave,
        @NotBlank @Size(max=60) String codigo,
        @NotBlank @Size(max=150) String nome,
        @NotNull Tipo tipo,
        @NotBlank @Size(max=100) String grupo,
        @NotBlank @Size(max=100) String material,
        @Size(max=100) String acabamento,
        @NotNull Unidade unidade,
        @NotNull @DecimalMin("0.001") @DecimalMax("100000") @Digits(integer=6,fraction=3) BigDecimal quantidade,
        boolean cortar,
        @DecimalMin("0.001") @DecimalMax("10000") BigDecimal comprimentoMm,
        @DecimalMin("0.001") @DecimalMax("10000") BigDecimal larguraMm,
        @DecimalMin("0.001") @DecimalMax("200") BigDecimal espessuraMm,
        @NotNull Veio veio,
        @NotNull @Size(max=4) Set<Borda> bordas,
        @Size(max=100) String fitaBorda,
        @Size(max=2000) String furacao,
        @Size(max=2000) String observacoes,
        @NotNull @Size(max=20) Map<@NotBlank @Size(max=60) String,@NotBlank @Size(max=500) String> atributos) {
    public enum Tipo { PAINEL, FERRAGEM, ACESSORIO, ACABAMENTO, EMBALAGEM, OUTRO }
    public enum Unidade { UN, PAR, KIT, M, M2, KG }
    public enum Veio { SEM, LONGITUDINAL, TRANSVERSAL }
    public enum Borda { C1, C2, L1, L2 }
}
