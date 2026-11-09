package com.aratech.moveis.rh.afastamentos.service.imp;

import com.aratech.moveis.rh.afastamentos.dto.Afastamento;
import com.aratech.moveis.rh.afastamentos.dto.NovoAfastamento;
import com.aratech.moveis.rh.afastamentos.repository.AfastamentoRepository;
import com.aratech.moveis.rh.afastamentos.service.AfastamentoService;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.shared.dto.Comando;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;
import com.aratech.moveis.shared.operacao.service.OperacaoService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AfastamentoServiceImp implements AfastamentoService {
    private final OperacaoService base;
    private final AfastamentoRepository repository;

    public List<Opcao> funcionarios() {
        return repository.listarFuncionariosAtivos();
    }

    private void ativo(long id) {
        if (repository.bloquearFuncionarioAtivo(id).isEmpty())
            throw new AuthException(404, "Funcionário ativo não encontrado.");
    }

    public Pagina<Afastamento> afastamentos(int pagina, int tamanho, String busca) {
        return repository.listarAfastamentos(pagina, tamanho, busca);
    }

    @Transactional
    public Afastamento criarAfastamento(NovoAfastamento dto) {
        if (dto.fim().isBefore(dto.inicio()) || dto.fim().isAfter(dto.inicio().plusDays(729)))
            throw new AuthException(400, "Confira período, com até 730 dias.");
        return base.comando(
                dto.chaveOperacao(),
                "afastamento:criar",
                dto,
                Afastamento.class,
                () -> {
                    ativo(dto.funcionarioId());
                    base.exigir(
                            repository.contarSobreposicoesAtivas(
                                            dto.funcionarioId(), dto.fim(), dto.inicio())
                                    == 0,
                            "Há outro afastamento pendente ou aprovado neste período.");
                    Long id =
                            repository.inserirAfastamento(
                                    dto.funcionarioId(),
                                    dto.tipo(),
                                    dto.inicio(),
                                    dto.fim(),
                                    base.motivo(dto.motivo()));
                    base.auditar(
                            "RH",
                            "CADASTRAR",
                            "AFASTAMENTO",
                            id,
                            "Solicitação de afastamento do funcionário #" + dto.funcionarioId());
                    return repository.buscarAfastamento(id);
                });
    }

    @Transactional
    public Afastamento afastamentoAcao(long id, String acao, Comando dto) {
        return base.comando(
                dto.chaveOperacao(),
                "afastamento:" + id + ":" + acao,
                dto,
                Afastamento.class,
                () -> {
                    var estado = repository.bloquearAfastamento(id);
                    base.versao(estado.versao(), dto.versao());
                    base.exigir(
                            estado.status().equals("PENDENTE"),
                            "Somente solicitações pendentes podem ser decididas.");
                    String destino =
                            switch (acao) {
                                case "aprovar" -> "APROVADO";
                                case "reprovar" -> "RECUSADO";
                                case "cancelar" -> "CANCELADO";
                                default -> throw new AuthException(400, "Ação inválida.");
                            };
                    if (acao.equals("aprovar")) {
                        ativo(estado.funcionarioId());
                        base.exigir(
                                repository.contarAprovadosSobrepostos(
                                                estado.funcionarioId(),
                                                id,
                                                estado.fim(),
                                                estado.inicio())
                                        == 0,
                                "Existe afastamento aprovado sobreposto.");
                    }
                    repository.registrarDecisao(destino, base.motivo(dto.motivo()), id);
                    base.auditar(
                            "RH",
                            acao.toUpperCase(Locale.ROOT),
                            "AFASTAMENTO",
                            id,
                            "Decisão registrada, sem reproduzir dados médicos na auditoria");
                    return repository.buscarAfastamento(id);
                });
    }
}
