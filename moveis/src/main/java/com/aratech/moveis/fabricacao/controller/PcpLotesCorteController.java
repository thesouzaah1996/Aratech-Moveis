package com.aratech.moveis.fabricacao.controller;

import com.aratech.moveis.fabricacao.dto.*;
import com.aratech.moveis.fabricacao.service.FabricacaoService;
import com.aratech.moveis.shared.auditoria.annotation.AuditadoNoServico;
import com.aratech.moveis.shared.dto.Pagina;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@AuditadoNoServico
@RequestMapping("/aratech/pcp/lotes")
@Tag(name="PCP — Lotes para corte",description="PCP_LOTES governa criação e envio. Fluxo PLANEJADO → ENVIADO_CORTE → EM_CORTE → CORTADO; cancelamento apenas em PLANEJADO. Cópia imutável da ficha por lote. Não baixa estoque, não calcula aproveitamento de chapas e não conclui automaticamente ordens de produção.")
public class PcpLotesCorteController {
    private final FabricacaoService service;
    @GetMapping("/corte")
    @Operation(summary="Consultar lotes planejados e enviados ao corte",description="Exige CONSULTAR. Busca por ID, código ou nome do modelo, filtro de status e paginação de 1 a 100. Lista metadados; ficha completa no detalhe. Inclui planejados e cancelados, invisíveis à fila do Corte.")
    public Pagina<LoteResumo> listar(@RequestParam(defaultValue="0")int pagina,@RequestParam(defaultValue="12")int tamanho,@RequestParam(defaultValue="")String busca,@RequestParam(defaultValue="")String status){return service.lotes(pagina,tamanho,busca,false,status);}
    @PostMapping("/corte")
    @Operation(summary="Planejar lote a partir de um guarda-roupa",description="Exige CADASTRAR. Modelo ativo com pelo menos um painel de corte válido, 1 a 10000 unidades, previsão a partir de hoje em America/Sao_Paulo e UUID estável. Congela ficha, revisão e referências de fotos. Quantidades da ficha são por guarda-roupa e devem ser multiplicadas pelas unidades do lote. Mesmo UUID/conteúdo retorna a resposta original; divergência 409. Retorna 200.")
    public LoteCorte criar(@Valid @RequestBody NovoLoteCorte dto){return service.criarLote(dto);}
    @GetMapping("/corte/{id}")
    @Operation(summary="Consultar lote e ficha congelada",description="Exige CONSULTAR. Inclui fotos, peças, quantidades por unidade, revisão do modelo, autor e instantes. Alterações posteriores do catálogo não afetam este conteúdo. Inexistente retorna 404.")
    public LoteCorte detalhe(@PathVariable long id){return service.lote(id,false);}
    @PostMapping("/corte/{id}/enviar")
    @Operation(summary="Enviar lote para a fila do Corte",description="Exige ALTERAR e versão atual. Apenas PLANEJADO; registra enviadoEm e ENVIADO_CORTE. A fila do Corte passa a enxergar a ficha congelada. Repetição ou conflito de versão retorna 409.")
    public LoteCorte enviar(@PathVariable long id,@Valid @RequestBody AcaoLoteCorte dto){return service.transicao(id,dto.versao(),"enviar");}
    @PostMapping("/corte/{id}/cancelar")
    @Operation(summary="Cancelar planejamento ainda não enviado",description="Exige ALTERAR e versão atual. Apenas PLANEJADO → CANCELADO. Depois do envio não permite cancelamento por esta API. Conflito retorna 409; preserva ficha e auditoria.")
    public LoteCorte cancelar(@PathVariable long id,@Valid @RequestBody AcaoLoteCorte dto){return service.transicao(id,dto.versao(),"cancelar");}
    @GetMapping("/modelos")
    @Operation(summary="Consultar modelos ativos para planejar lote",description="Exige PCP_LOTES.CONSULTAR; não exige acesso ao cadastro administrativo. Retorna somente modelos ativos, códigos, nomes, revisão, quantidade de itens e referência da capa, em páginas de 1 a 100. Ficha é congelada pelo servidor na criação.")
    public Pagina<ModeloResumo> modelos(@RequestParam(defaultValue="0")int pagina,@RequestParam(defaultValue="12")int tamanho,@RequestParam(defaultValue="")String busca){return service.modelos(pagina,tamanho,busca,true);}
    @GetMapping(value="/modelos/{id}/fotos/{foto}",produces={"image/jpeg","image/png"})
    @Operation(summary="Visualizar foto de fabricação pelo PCP",description="Exige PCP_LOTES.CONSULTAR. JPEG/PNG autenticado, inclusive referências preservadas de lotes; valida vínculo ao modelo. Não concede alteração de catálogo. Inexistente retorna 404.")
    public ResponseEntity<byte[]> foto(@PathVariable long id,@PathVariable long foto){return GuardaRoupaController.respostaFoto(service,id,foto);}
}
