package com.aratech.moveis.embalagem.controller;

import com.aratech.moveis.embalagem.dto.*;
import com.aratech.moveis.embalagem.service.EmbalagemService;
import com.aratech.moveis.fabricacao.dto.LoteResumo;
import com.aratech.moveis.fabricacao.service.FabricacaoService;
import com.aratech.moveis.shared.auditoria.annotation.AuditadoNoServico;
import com.aratech.moveis.shared.dto.Pagina;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

@RestController @RequiredArgsConstructor @AuditadoNoServico
@RequestMapping("/aratech/embalagem/lotes")
@Tag(name="Embalagem — Lotes",description="Recebe integralmente lotes do Corte, mediante confirmação de prontidão após etapas necessárias. Mantém ficha congelada, roteiro ordenado por esteiras e histórico. Estados AGUARDANDO → EM_EMBALAGEM ↔ PAUSADO → EMBALADO; cancelamento somente antes de iniciar. Não presume que corte concluído significa produto pronto, nem movimenta estoque ou expedição. EMBALAGEM_LOTES separa CONSULTAR, CADASTRAR e ALTERAR.")
public class LotesEmbalagemController {
    private final EmbalagemService service;private final FabricacaoService fabricacao;
    @GetMapping("/esteiras") @Operation(summary="Consultar esteiras ativas para compor o roteiro",description="Exige EMBALAGEM_LOTES.CONSULTAR, sem exigir acesso ao painel operacional de esteiras. Retorna até 100 esteiras ativas com atividade e capacidade; não inclui tarefas de outros lotes nem permite edição. Não reserva posição até a etapa iniciar.")
    public java.util.List<Esteira> esteiras(){return service.opcoesEsteiras();}
    @GetMapping @Operation(summary="Listar lotes e andamento da embalagem",description="Exige CONSULTAR. Busca por lote, origem CT ou modelo; filtro de situação; página inicial 0, tamanho 1 a 100. Retorna etapa/esteira atual, progresso, previsão, prioridade e volumes quando concluído.")
    public Pagina<ResumoEmbalagem> listar(@RequestParam(defaultValue="0")int pagina,@RequestParam(defaultValue="12")int tamanho,@RequestParam(defaultValue="")String busca,@RequestParam(defaultValue="")String status){return service.lotes(pagina,tamanho,busca,status);}
    @GetMapping("/origens") @Operation(summary="Consultar lotes de Corte disponíveis para embalagem",description="Exige EMBALAGEM_LOTES.CONSULTAR, sem exigir permissão de Corte. Somente CORTADO e sem recebimento não cancelado. Não afirma que montagem/acabamento estejam prontos; operador deve confirmar essa condição no recebimento. Busca por modelo/ID e paginação de 1 a 100.")
    public Pagina<LoteResumo> origens(@RequestParam(defaultValue="0")int pagina,@RequestParam(defaultValue="12")int tamanho,@RequestParam(defaultValue="")String busca){return service.origens(pagina,tamanho,busca);}
    @PostMapping @Operation(summary="Receber lote e distribuir etapas pelas esteiras",description="Exige CADASTRAR. origemId de lote CORTADO, previsão a partir de hoje em America/Sao_Paulo, prioridade, prontoParaEmbalagem=true e roteiro de 1 a 12 etapas com esteiras ativas. Quantidade e ficha vêm da origem. Pode usar esteiras distintas ou repetir uma esteira em momentos diferentes. UUID garante criação única; recebimento duplicado ou esteira inativa retorna 409. Roteiro fica congelado; cancelamento anterior ao início permite novo recebimento. Retorna 200.")
    public LoteEmbalagem receber(@Valid @RequestBody NovoLoteEmbalagem dto){return service.receber(dto);}
    @GetMapping("/{id}") @Operation(summary="Consultar roteiro e histórico de embalagem",description="Exige CONSULTAR. Inclui ficha congelada, etapas, instruções, operadores, tempos ativos sem pausas, motivos, progresso e histórico de eventos. ID inexistente 404. Operações nas etapas exigem permissões próprias de EMBALAGEM_ESTEIRAS.")
    public LoteEmbalagem detalhe(@PathVariable long id){return service.lote(id);}
    @PostMapping("/{id}/cancelar") @Operation(summary="Cancelar recebimento antes de iniciar embalagem",description="Exige ALTERAR, versão atual e motivo de 5 a 500 caracteres. Somente AGUARDANDO → CANCELADO, cancelando etapas e liberando a origem para novo recebimento. Não apaga histórico. Lote iniciado/encerrado ou versão antiga retorna 409.")
    public LoteEmbalagem cancelar(@PathVariable long id,@Valid @RequestBody ComandoEmbalagem dto){return service.cancelar(id,dto);}
    @GetMapping(value="/modelos/{id}/fotos/{foto}",produces={MediaType.IMAGE_JPEG_VALUE,MediaType.IMAGE_PNG_VALUE}) @Operation(summary="Visualizar foto de fabricação na embalagem",description="Exige EMBALAGEM_LOTES.CONSULTAR. Foto JPEG/PNG autenticada, inclusive referências históricas; valida vínculo ao modelo, sem cache público. Não concede edição de catálogo. Foto inexistente retorna 404.")
    public ResponseEntity<byte[]> foto(@PathVariable long id,@PathVariable long foto){var f=fabricacao.foto(id,foto);return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.parseMediaType(f.mime())).header("X-Content-Type-Options","nosniff").body(f.conteudo());}
}
