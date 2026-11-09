package com.aratech.moveis.pcp.lotes;

import static com.aratech.moveis.pcp.lotes.ProducaoDtos.*;
import com.aratech.moveis.shared.auditoria.annotation.AuditadoNoServico;
import com.aratech.moveis.shared.dto.Pagina;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequiredArgsConstructor @AuditadoNoServico
@RequestMapping("/aratech/pcp/lotes/producao")
@Tag(name="PCP — Lotes de produção",description="PCP_LOTES: CONSULTAR consulta prévia e pendências; CADASTRAR planeja; ALTERAR libera setores. Ficha específica da variante e revisão congeladas na primeira liberação, inclusive para liberações posteriores. Lista de peças não é otimização de chapas. Liberação não baixa estoque nem conclui pedido comercial.")
public class PcpProducaoController {
 final LotesProducaoService service;
 @GetMapping @Operation(summary="Listar lotes principais e setores pendentes",description="Paginação de 12, busca por produto ou ID. CONCLUIDO exige todos os setores calculados liberados e todas as suas ordens concluídas; pendências técnicas bloqueiam liberação. CONSULTAR; filtros inválidos 400.")
 public Pagina<Resumo> listar(@RequestParam(defaultValue="0")int pagina,@RequestParam(defaultValue="")String busca){return service.listar(pagina,busca);}
 @GetMapping("/setores") @Operation(summary="Consultar setores e regras de destino",description="CONSULTAR. Cadastro de setores relaciona permissões, natureza (operação/separação) e exigência de medidas; roteamento é definido na ficha por ID, nunca pelo nome do componente.")
 public List<Setor> setores(){return service.setores();}
 @GetMapping("/variantes") @Operation(summary="Selecionar variantes ativas",description="CONSULTAR. Até 100 resultados por busca; apenas modelos e variantes ativos, sem exigir acesso à edição administrativa.")
 public List<OpcaoVariante> variantes(@RequestParam(defaultValue="")String busca){return service.variantes(busca);}
 @GetMapping("/origens") @Operation(summary="Selecionar ordem/pedido de origem",description="CONSULTAR. Ordens PCP existentes, incluindo as criadas pelo pedido comercial, com pedidoId e saldo ainda não planejado em lotes. A associação do modelo é explícita porque produto comercial atualmente é texto livre.")
 public List<Origem> origens(@RequestParam(defaultValue="")String busca){return service.origens(busca);}
 @PostMapping @Operation(summary="Planejar lote de produção e calcular prévia",description="CADASTRAR. 1–10000 unidades, data de hoje/futura em America/Sao_Paulo, variante ativa e UUID estável. Calcula ficha, roteiros, revisão e totais exatos para a prévia. Permite atualizá-la explicitamente antes da primeira liberação, que congela a cópia para todos os setores restantes. Origem opcional bloqueada com saldo validado. Campos inválidos 400; saldo excedido ou UUID com dados divergentes 409; inexistente 404. Setores com exigeMedidas validam medidas, material e acabamento também nas linhas de consumo. Linhas sem setor/dados aparecem em previa.pendencias e impedem liberar.")
 public Lote criar(@Valid @RequestBody NovoLote d){return service.criar(d);}
 @GetMapping("/{id}") @Operation(summary="Consultar prévia agrupada e ordens emitidas",description="CONSULTAR. Retorna linhas de consumo separadas das operações, revisão da prévia (imutável após a primeira ordem), status por ordem e setores calculados sem ordem ainda pendentes. Não estima número de chapas. Inexistente 404.")
 public Lote lote(@PathVariable long id){return service.lote(id);}
 @PostMapping("/{id}/revisar") @Operation(summary="Atualizar prévia antes da primeira liberação",description="PCP_LOTES.ALTERAR. Informe versao igual à revisão da prévia exibida. Recarrega a mesma variante, recalcula quantidades e permite corrigir pendências sem consumir mais saldo da OP. Somente quando nenhuma ordem foi emitida. Primeira liberação congela definitivamente a cópia: tentativa posterior retorna 409. Não modifica quantidade, origem nem ordens existentes; inexistente 404.")
 public Lote revisar(@PathVariable long id,@Valid @RequestBody Acao d){return service.revisar(id,d.versao());}
 @PostMapping("/{id}/liberar") @Operation(summary="Liberar somente os setores selecionados",description="ALTERAR. Informe revisão congelada e IDs dos checkboxes selecionados. Transação, bloqueio do lote e UNIQUE(lote_id,setor_id) garantem idempotência inclusive concorrente. Repetição mantém a mesma ordem. Não selecionados continuam pendentes. Congela apenas linhas do setor na ordem. Sem trabalho 400; pendências ou revisão divergente 409; inexistente 404. Não movimenta estoque.")
 public Lote liberar(@PathVariable long id,@Valid @RequestBody Liberar d){return service.liberar(id,d);}
}
