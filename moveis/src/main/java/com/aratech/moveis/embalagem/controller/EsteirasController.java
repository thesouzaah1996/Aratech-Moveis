package com.aratech.moveis.embalagem.controller;

import com.aratech.moveis.embalagem.dto.*;
import com.aratech.moveis.embalagem.service.EmbalagemService;
import com.aratech.moveis.shared.auditoria.annotation.AuditadoNoServico;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequiredArgsConstructor @AuditadoNoServico
@RequestMapping("/aratech/embalagem/esteiras")
@Tag(name="Embalagem — Esteiras",description="Postos de trabalho configuráveis com atividade, responsável, fila e capacidade de 1 a 20 operações simultâneas. EM_EXECUCAO e PAUSADA ocupam capacidade. Só a primeira etapa não concluída do lote fica liberada; esteiras diferentes podem trabalhar lotes distintos em paralelo. EMBALAGEM_ESTEIRAS separa CONSULTAR, CADASTRAR, ALTERAR e FINALIZAR. Não controla máquinas nem mede OEE.")
public class EsteirasController {
    private final EmbalagemService service;
    @GetMapping @Operation(summary="Consultar o que cada esteira está fazendo",description="Exige CONSULTAR. Retorna até 100 esteiras por nome, com operações em execução/pausadas, fila pronta (até 100), contagem de etapas futuras e posições ocupadas. Busca por código, nome, atividade e responsável. ativas=true oculta inativas. Segundos ativos excluem pausas. A fila usa prioridade URGENTE/ALTA/NORMAL, previsão e ID; não é agendamento automático.")
    public List<PainelEsteira> painel(@RequestParam(defaultValue="")String busca,@RequestParam(defaultValue="false")boolean ativas){return service.painel(busca,ativas);}
    @PostMapping @Operation(summary="Cadastrar esteira de embalagem",description="Exige CADASTRAR. Código único sem diferenciar maiúsculas, nome e atividade obrigatórios, capacidade de 1 a 20 e versão inicial 0. Não cria esteiras fictícias nem integra equipamentos. Duplicidade retorna 409, validações 400. Retorna 200.")
    public Esteira criar(@Valid @RequestBody EsteiraEntrada dto){return service.salvarEsteira(null,dto);}
    @PutMapping("/{id}") @Operation(summary="Editar atividade, responsável e capacidade da esteira",description="Exige ALTERAR e versão atual. Não reduz capacidade abaixo das operações ocupadas nem inativa esteira com etapas aguardando, em execução ou pausadas. Conflito retorna 409. Mudança de atividade não reescreve instruções de etapas já planejadas.")
    public Esteira editar(@PathVariable long id,@Valid @RequestBody EsteiraEntrada dto){return service.salvarEsteira(id,dto);}
    @PostMapping("/{id}/etapas/{etapa}/iniciar") @Operation(summary="Iniciar etapa liberada nesta esteira",description="Exige ALTERAR e versão atual do lote em {versao}. AGUARDANDO → EM_EXECUCAO, somente após concluir etapas anteriores e se houver posição livre na esteira ativa. Registra operador e instante; conflito de versão, sequência ou capacidade retorna 409. Atualiza o lote para EM_EMBALAGEM.")
    public LoteEmbalagem iniciar(@PathVariable long id,@PathVariable long etapa,@Valid @RequestBody ComandoEmbalagem dto){return service.operar(id,etapa,"iniciar",dto);}
    @PostMapping("/{id}/etapas/{etapa}/pausar") @Operation(summary="Pausar operação com motivo",description="Exige ALTERAR, versão atual do lote e motivo de 5 a 500 caracteres. EM_EXECUCAO → PAUSADA; pausa não libera posição. Acumula tempo ativo e registra histórico. Lote fica PAUSADO; conflito 409 e motivo inválido 400.")
    public LoteEmbalagem pausar(@PathVariable long id,@PathVariable long etapa,@Valid @RequestBody ComandoEmbalagem dto){return service.operar(id,etapa,"pausar",dto);}
    @PostMapping("/{id}/etapas/{etapa}/retomar") @Operation(summary="Retomar etapa pausada",description="Exige ALTERAR e versão atual do lote. PAUSADA → EM_EXECUCAO na mesma esteira ativa, mantendo posição reservada. Retoma contagem de tempo ativo e registra operador. Conflito retorna 409.")
    public LoteEmbalagem retomar(@PathVariable long id,@PathVariable long etapa,@Valid @RequestBody ComandoEmbalagem dto){return service.operar(id,etapa,"retomar",dto);}
    @PostMapping("/{id}/etapas/{etapa}/concluir") @Operation(summary="Concluir etapa e liberar próxima operação",description="Exige FINALIZAR e versão atual do lote. Apenas EM_EXECUCAO → CONCLUIDA. Libera posição e a próxima etapa do roteiro. Na última etapa exige volumes de 1 a 100000 e pecasConferidas, protecaoAplicada e identificacaoConferida verdadeiros; só então o lote vira EMBALADO. Não gera expedição nem baixa estoque. Conferências ausentes 400; conflitos 409. Tempo e histórico são preservados.")
    public LoteEmbalagem concluir(@PathVariable long id,@PathVariable long etapa,@Valid @RequestBody ComandoEmbalagem dto){return service.operar(id,etapa,"concluir",dto);}
}
