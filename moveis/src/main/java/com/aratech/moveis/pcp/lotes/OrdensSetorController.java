package com.aratech.moveis.pcp.lotes;

import static com.aratech.moveis.pcp.lotes.ProducaoDtos.*;
import com.aratech.moveis.shared.auditoria.annotation.AuditadoNoServico;
import com.aratech.moveis.shared.dto.Pagina;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor @AuditadoNoServico
@RequestMapping({"/aratech/corte/ordens","/aratech/almoxarifado/separacoes","/aratech/producao/furacao","/aratech/producao/montagem","/aratech/embalagem/ordens"})
@Tag(name="Produção — Ordens por setor",description="Filtra setor no servidor pelo caminho e permissão própria: CORTE_LOTES, ALMOXARIFADO_REQUISICOES, PRODUCAO_FURACAO, PRODUCAO_MONTAGEM ou EMBALAGEM_LOTES. CONSULTAR lê, ALTERAR inicia e FINALIZAR conclui/confirma consumo. Não retorna ficha ou linhas de outros setores.")
public class OrdensSetorController {
 final LotesProducaoService service;
 private Setor setor(HttpServletRequest r,String acao){return service.setor(r.getRequestURI().substring(r.getContextPath().length()),acao);}
 @GetMapping @Operation(summary="Consultar fila exclusiva do setor",description="CONSULTAR no recurso do setor. 12 por página, busca por produto/ID do lote. Só ordens efetivamente liberadas para este setor; cada uma traz OS, lote, produto, variante, quantidade planejada e suas próprias linhas calculadas. 400 filtros; 403 permissão.")
 public Pagina<Ordem> fila(HttpServletRequest r,@RequestParam(defaultValue="0")int pagina,@RequestParam(defaultValue="")String busca){return service.fila(setor(r,"CONSULTAR"),pagina,busca);}
 @GetMapping("/{id}") @Operation(summary="Consultar ordem pelo seu identificador próprio",description="CONSULTAR. ID de outro setor retorna 404 mesmo se existir; sem acesso ao recurso 403. Dados de revisão e linhas permanecem congelados.")
 public Ordem detalhe(HttpServletRequest r,@PathVariable long id){return service.ordem(setor(r,"CONSULTAR"),id);}
 @PostMapping("/{id}/iniciar") @Operation(summary="Iniciar ordem liberada do setor",description="ALTERAR. LIBERADA → EM_ANDAMENTO com versão atual. Repetir quando já em andamento retorna a mesma ordem; conflito de estado/versão 409; inexistente/no outro setor 404.")
 public Ordem iniciar(HttpServletRequest r,@PathVariable long id,@Valid @RequestBody Acao d){return service.acao(setor(r,"ALTERAR"),id,"iniciar",d);}
 @PostMapping("/{id}/concluir") @Operation(summary="Concluir ordem sem concluir setores pendentes",description="FINALIZAR. EM_ANDAMENTO → CONCLUIDA. Separação exige todos os consumos confirmados. Repetir concluída é idempotente. Conflito 409; inexistente/no outro setor 404. Não conclui lote com outros setores pendentes nem atualiza expedição/pedido automaticamente.")
 public Ordem concluir(HttpServletRequest r,@PathVariable long id,@Valid @RequestBody Acao d){return service.acao(setor(r,"FINALIZAR"),id,"concluir",d);}
 @PostMapping("/{id}/confirmar") @Operation(summary="Confirmar consumo de material separado",description="FINALIZAR. Apenas setor SEPARACAO e ordem EM_ANDAMENTO. Usa ProdutoService.saida e suas validações de saldo/ativo; transação registra material uma única vez por ordem/item. Repetição não baixa novamente. Preserva a unidade da ficha; usa quantidadeEstoque, calculada por conversão explícita e exata para unidades inteiras do produto. Conversão fracionária retorna 409 sem arredondar; setor operacional 400; item de outra ordem 404. Liberação/início não baixam estoque.")
 public Ordem consumir(HttpServletRequest r,@PathVariable long id,@Valid @RequestBody Consumo d){return service.consumir(setor(r,"FINALIZAR"),id,d);}
}
