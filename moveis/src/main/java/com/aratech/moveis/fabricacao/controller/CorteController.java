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
@RequestMapping("/aratech/corte/lotes")
@Tag(name="Corte — Lotes",description="Fila recebida exclusivamente do PCP. CORTE_LOTES.CONSULTAR lista e abre fichas/fotos; ALTERAR inicia; FINALIZAR conclui. Estados ENVIADO_CORTE → EM_CORTE → CORTADO. Peças e medidas vêm da revisão congelada no planejamento, sem alteração pela operação de corte.")
public class CorteController {
    private final FabricacaoService service;
    @GetMapping
    @Operation(summary="Listar lotes recebidos para cortar chapas",description="Somente ENVIADO_CORTE, EM_CORTE ou CORTADO. Em corte primeiro, depois recebidos e concluídos; dentro da situação, previsão e ID. Busca por ID/código/nome, filtro de status e paginação. Não revela planejamentos não enviados ou cancelados.")
    public Pagina<LoteResumo> listar(@RequestParam(defaultValue="0")int pagina,@RequestParam(defaultValue="12")int tamanho,@RequestParam(defaultValue="")String busca,@RequestParam(defaultValue="")String status){return service.lotes(pagina,tamanho,busca,true,status);}
    @GetMapping("/{id}")
    @Operation(summary="Abrir lista de peças e ficha de corte do lote",description="Exige CONSULTAR. Retorna ficha congelada com dimensões em mm, veio, bordas, furações, fotos e quantidades por guarda-roupa; multiplique pela quantidade do lote para fabricar. Planejado/cancelado ou inexistente retorna 404. Não é um plano otimizado de aproveitamento de chapas.")
    public LoteCorte detalhe(@PathVariable long id){return service.lote(id,true);}
    @PostMapping("/{id}/iniciar")
    @Operation(summary="Iniciar corte do lote recebido",description="Exige ALTERAR e versão atual. Somente ENVIADO_CORTE → EM_CORTE. Define iniciadoEm e registra auditoria; repetição ou versão antiga retorna 409.")
    public LoteCorte iniciar(@PathVariable long id,@Valid @RequestBody AcaoLoteCorte dto){return service.transicao(id,dto.versao(),"iniciar");}
    @PostMapping("/{id}/finalizar")
    @Operation(summary="Concluir corte integral das peças do lote",description="Exige FINALIZAR e versão atual. Somente EM_CORTE → CORTADO. Define concluidoEm e auditoria. Não movimenta estoque nem finaliza outras etapas de fabricação; conflito retorna 409.")
    public LoteCorte finalizar(@PathVariable long id,@Valid @RequestBody AcaoLoteCorte dto){return service.transicao(id,dto.versao(),"finalizar");}
    @GetMapping(value="/modelos/{id}/fotos/{foto}",produces={"image/jpeg","image/png"})
    @Operation(summary="Visualizar foto de peça no Corte",description="Exige CORTE_LOTES.CONSULTAR. JPEG/PNG autenticado com vínculo ao modelo, incluindo fotos preservadas em revisões congeladas. Não concede acesso às operações do cadastro. Inexistente retorna 404.")
    public ResponseEntity<byte[]> foto(@PathVariable long id,@PathVariable long foto){return GuardaRoupaController.respostaFoto(service,id,foto);}
}
