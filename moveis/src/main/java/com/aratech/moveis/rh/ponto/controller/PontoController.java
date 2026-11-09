package com.aratech.moveis.rh.ponto.controller;

import com.aratech.moveis.rh.ponto.dto.MarcacaoPontoDTO;

import com.aratech.moveis.rh.ponto.dto.DiaPontoDTO;
import com.aratech.moveis.rh.ponto.dto.EscalaPontoDTO;
import com.aratech.moveis.rh.ponto.dto.EdicaoPontoDTO;
import com.aratech.moveis.rh.ponto.dto.FuncionarioPontoDTO;
import com.aratech.moveis.rh.ponto.dto.HistoricoPontoDTO;
import com.aratech.moveis.rh.ponto.service.PontoService;
import com.aratech.moveis.security.dto.UsuarioSessao;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(
        name = "RH — Controle de ponto",
        description =
                "Competência no formato YYYY-MM, entre 1900 e 2100. Retorna somente dias"
                    + " persistidos; dias sem registros são gerados pela interface e não significam"
                    + " falta. Cada dia contém 6 horários (3 pares entrada/saída), HH:mm ou string"
                    + " vazia. Preencher em sequência, em ordem crescente e no mesmo dia. Não"
                    + " aceita data futura. Correção exige perfil # e RH_PONTO.ALTERAR, motivo de 5 a 500 caracteres"
                    + " e versao atual; conflito concorrente retorna 409. Guarda antes/depois,"
                    + " autor e instante. Consultas meu-ponto usam o funcionário da sessão, sem"
                    + " exigir RH.CONSULTAR. Ainda não há integração de importação de relógio de"
                    + " ponto.")
@RestController
@RequiredArgsConstructor
public class PontoController {
    private final PontoService service;

    @Operation(summary = "Consultar relógio para marcação", description = "Retorna o instante e a data do servidor em America/Sao_Paulo. Requer sessão; não exige permissão de RH.")
    @GetMapping("/aratech/auth/meu-ponto/relogio")
    public java.util.Map<String, String> relogio() {
        var agora = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Sao_Paulo"));
        return java.util.Map.of("instante", agora.toInstant().toString(), "data", agora.toLocalDate().toString());
    }

    @Operation(summary = "Consultar escala semanal de ponto", description = "Requer RH_PONTO.CONSULTAR. Retorna dias de segunda (1) a domingo (7), seis horários e minutos extras autorizados. Lista vazia indica escala ainda não definida; não presume horários corretos.")
    @GetMapping("/aratech/rh/ponto/{id}/escala")
    public List<com.aratech.moveis.rh.ponto.dto.EscalaPontoDTO> escala(@PathVariable Long id) { return service.escala(id); }

    @Operation(summary = "Definir escala semanal de ponto", description = "Exige perfil # e RH_PONTO.ALTERAR. Envia sete dias únicos (1 a 7), com seis horários HH:mm ou vazios em pares crescentes no mesmo dia. Folga usa seis vazios. Soma semanal não pode exceder jornadaSemanal do cadastro. Horas extras requerem autorização explícita de 0 a 120 minutos por dia. Escala atual é usada na avaliação do espelho, inclusive em consultas anteriores. Dados inválidos retornam 400; funcionário inexistente 404.")
    @PutMapping("/aratech/rh/ponto/{id}/escala")
    public void salvarEscala(@PathVariable Long id,
            @RequestBody List<@jakarta.validation.Valid EscalaPontoDTO> dias,
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UsuarioSessao autor) {
        service.salvarEscala(id, dias, autor);
    }

    @Operation(summary = "Marcar meu ponto agora", description = "Requer sessão ativa e CSRF. O funcionário e o autor são obtidos exclusivamente da sessão validada; não recebe ID, e-mail ou senha. Usa data e hora do servidor em America/Sao_Paulo, preenchendo o próximo dos seis horários. Data diferente de hoje retorna 400; sessão inválida 401; CSRF inválido ou conta técnica sem vínculo de funcionário 403; versão desatualizada ou repetição no mesmo minuto 409. Conta inativa revoga a sessão, podendo invalidar também o CSRF. A marcação aparece imediatamente no controle de ponto e no histórico.")
    @PostMapping("/aratech/auth/meu-ponto/marcacoes")
    public DiaPontoDTO marcar(
            @jakarta.validation.Valid @RequestBody MarcacaoPontoDTO dto,
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal UsuarioSessao usuario) {
        return service.marcar(dto, usuario);
    }

    @Operation(
            summary = "Listar registros de ponto",
            description =
                    "Consulta paginada com os filtros disponíveis nos parâmetros. Não altera"
                            + " dados.")
    @GetMapping("/aratech/rh/ponto/{id}")
    public List<DiaPontoDTO> listar(@PathVariable Long id, @RequestParam String mes) {
        return service.listar(id, mes);
    }

    @Operation(
            summary = "Consultar identificação do funcionário",
            description =
                    "Retorna somente ID, nome, cargo e setor para o cabeçalho do espelho de ponto.")
    @GetMapping("/aratech/rh/ponto/{id}/funcionario")
    public FuncionarioPontoDTO funcionario(@PathVariable Long id) {
        return service.funcionario(id);
    }

    @Operation(
            summary = "Consultar histórico de registro de ponto",
            description =
                    "Retorna o histórico persistido, preservando operações anteriores. Veja os"
                            + " filtros e a paginação disponíveis.")
    @GetMapping("/aratech/rh/ponto/{id}/{data}/historico")
    public List<HistoricoPontoDTO> historico(@PathVariable Long id, @PathVariable LocalDate data) {
        return service.historico(id, data);
    }

    @Operation(
            summary = "Corrigir marcações de um dia",
            description =
                    "Exige perfil # e RH_PONTO.ALTERAR, inclusive para administradores. Envia os seis horários, motivo e versão obtida na consulta (0 se o dia ainda"
                        + " não existe). Não altera o saldo de horas diretamente: a interface"
                        + " calcula pares completos. Gera histórico imutável pela API. Limita-se a"
                        + " horários crescentes no mesmo dia; não representa turno que atravesse a"
                        + " meia-noite.")
    @PutMapping("/aratech/rh/ponto/{id}/{data}")
    public DiaPontoDTO salvar(
            @PathVariable Long id,
            @PathVariable LocalDate data,
            @RequestBody EdicaoPontoDTO dto,
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal
                    UsuarioSessao autor) {
        return service.salvar(id, data, dto, autor);
    }

    @Operation(
            summary = "Consultar histórico do meu ponto",
            description =
                    "Consulta alterações de uma data somente para o funcionário autenticado. Não"
                            + " exige permissão de RH.")
    @GetMapping("/aratech/auth/meu-ponto/{data}/historico")
    public List<HistoricoPontoDTO> meuHistorico(
            @PathVariable LocalDate data,
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal
                    UsuarioSessao usuario) {
        return service.historico(funcionarioDaSessao(usuario), data);
    }

    @Operation(
            summary = "Consultar meu ponto no mês",
            description =
                    "Identidade obtida exclusivamente da sessão; não há parâmetro de ID. Não exige"
                            + " permissão do RH. A conta técnica sem vínculo retorna 403 com orientação para usar uma conta de funcionário.")
    @GetMapping("/aratech/auth/meu-ponto")
    public List<DiaPontoDTO> meuPonto(
            @RequestParam String mes,
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) @AuthenticationPrincipal
                    UsuarioSessao usuario) {
        return service.listar(funcionarioDaSessao(usuario), mes);
    }

    private Long funcionarioDaSessao(UsuarioSessao usuario) {
        if (usuario.id() == null || usuario.id() <= 0)
            throw new com.aratech.moveis.security.exception.AuthException(403,
                    "A conta técnica de administração não possui ponto. Entre com a conta de um funcionário.");
        return usuario.id();
    }
}
