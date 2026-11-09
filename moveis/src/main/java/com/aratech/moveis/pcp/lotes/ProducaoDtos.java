package com.aratech.moveis.pcp.lotes;

import com.aratech.moveis.fabricacao.dto.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public final class ProducaoDtos {
 private ProducaoDtos() {}
 public record Setor(long id,String codigo,String nome,String recurso,String caminho,String natureza,boolean exigeMedidas) {}
 public record Operacao(@Positive long setorId,@NotBlank @Size(max=150) String nome,@Size(max=2000) String instrucoes) {}
 @Schema(description="Um consumo por componente, independente do número de operações. Sem roteamento por nome/tipo do item. produtoEstoqueId é obrigatório na separação. fatorEstoque converte uma unidade da ficha em unidades do produto de estoque (ex.: 1000 quando KG é estocado em gramas); conversão deve ser exata, sem arredondamento.")
 public record Roteiro(@NotNull UUID itemChave,Long consumoSetorId,Long produtoEstoqueId,
   @NotNull @Size(max=20) List<@NotNull @Valid Operacao> operacoes,
   @DecimalMin("0.000001") @DecimalMax("1000000") @Digits(integer=7,fraction=6) BigDecimal fatorEstoque) {
   public Roteiro(UUID itemChave,Long consumoSetorId,Long produtoEstoqueId,List<Operacao> operacoes){this(itemChave,consumoSetorId,produtoEstoqueId,operacoes,BigDecimal.ONE);}
   public BigDecimal fatorEstoque(){return fatorEstoque==null?BigDecimal.ONE:fatorEstoque;}
 }
 @Schema(description="Ficha completa específica da variante (material, cor e medidas), copiada inicialmente do modelo. Cada alteração incrementa revisão. Fichas incompletas podem ser salvas, mas bloqueiam a liberação até correção da variante e atualização explícita da prévia antes de emitir ordens.")
 public record FichaVariante(@Positive long modeloId,@NotBlank @Size(max=60) String codigo,
   @NotBlank @Size(max=100) String nome,boolean ativo,@PositiveOrZero long versao,
   @NotNull @Valid FichaGuardaRoupa ficha,@NotNull @Size(max=200) List<@NotNull @Valid Roteiro> roteiro) {}
 public record Variante(long id,long versao,FichaVariante dados) {}
 public record OpcaoVariante(long id,long modeloId,String produto,String codigo,String nome,long versao) {}
 public record Origem(long id,Long pedidoId,String produto,int quantidade,int disponivel,String status) {}
 public record NovoLote(@Positive long varianteId,Long ordemOrigemId,@Min(1) @Max(10000) int quantidade,
   @NotNull LocalDate previsao,@Size(max=1000) String observacoes,@NotNull UUID chaveOperacao) {}
 public record Liberar(@NotNull @Size(min=1,max=20) Set<@Positive Long> setores,@Min(1) long revisao) {}
 public record Acao(@Min(1) long versao) {}
 public record Consumo(@NotNull UUID itemChave,@NotNull UUID chaveOperacao,@NotBlank @Size(min=5,max=300) String motivo) {}
 @Schema(description="Linha calculada. consomeMaterial marca a única linha que contabiliza material, inclusive quando agregada à primeira OPERACAO no mesmo setor. Outras operações representam trabalho, sem nova baixa. Não contém ficha inteira nem instruções de outros setores. Medidas em mm, quantidades exatas com até três casas; quantidadeEstoque informa a conversão exata. Setores com exigeMedidas recebem especificação também nas linhas de consumo; dados técnicos ausentes bloqueiam a liberação. Não é plano de chapas.")
 public record Linha(UUID itemChave,String natureza,String codigo,String descricao,String unidade,
   BigDecimal porUnidade,BigDecimal total,Long produtoEstoqueId,ItemGuardaRoupa especificacao,
   String operacao,String instrucoes,BigDecimal quantidadeEstoque,boolean consomeMaterial) {}
 public record Grupo(Setor setor,List<Linha> linhas) {}
 public record Calculo(List<Grupo> setores,List<String> pendencias) {}
 public record Resumo(long id,String produto,String variante,int quantidade,LocalDate previsao,long revisao,
   String status,int pendentes) {}
 public record Ordem(long id,long loteId,String codigoLote,long setorId,String setor,String produto,
   String variante,int quantidadePlanejada,long revisao,String status,long versao,List<Linha> linhas,
   List<UUID> consumosConfirmados,Instant liberadoEm) {}
 public record Lote(long id,String codigo,String produto,String variante,int quantidade,LocalDate previsao,
   long revisao,Long ordemOrigemId,String observacoes,String autor,String status,Calculo previa,List<Ordem> ordens) {}
}
