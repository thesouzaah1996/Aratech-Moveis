package com.aratech.moveis.rh.solicitacoes.service.imp;

import com.aratech.moveis.rh.solicitacoes.dto.NovaSolicitacao;
import com.aratech.moveis.rh.solicitacoes.dto.Solicitacao;
import com.aratech.moveis.rh.solicitacoes.repository.SolicitacaoRhRepository;
import com.aratech.moveis.rh.solicitacoes.service.SolicitacaoRhService;
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
public class SolicitacaoRhServiceImp implements SolicitacaoRhService {
    private final OperacaoService base;
    private final SolicitacaoRhRepository repository;

    public List<Opcao> funcionarios() {
        return repository.listarFuncionariosAtivos();
    }

    private void ativo(long id) {
        if (repository.bloquearFuncionarioAtivo(id).isEmpty())
            throw new AuthException(404, "Funcionário ativo não encontrado.");
    }

    public Pagina<Solicitacao> solicitacoes(int pagina, int tamanho, String busca) {
        return repository.listarSolicitacoes(pagina, tamanho, busca);
    }

    @Transactional
    public Solicitacao solicitar(NovaSolicitacao dto) {
        return base.comando(
                dto.chaveOperacao(),
                "rh:solicitar",
                dto,
                Solicitacao.class,
                () -> {
                    ativo(dto.funcionarioId());
                    Long id =
                            repository.inserirSolicitacao(
                                    dto.funcionarioId(), dto.tipo().trim(), dto.descricao().trim());
                    base.auditar(
                            "RH",
                            "CADASTRAR",
                            "SOLICITACAO",
                            id,
                            "Solicitação do funcionário #" + dto.funcionarioId());
                    return repository.buscarSolicitacao(id);
                });
    }

    @Transactional
    public Solicitacao solicitacaoAcao(long id, String acao, Comando dto) {
        return base.comando(
                dto.chaveOperacao(),
                "rh:solicitacao:" + id + ":" + acao,
                dto,
                Solicitacao.class,
                () -> {
                    var p = repository.bloquearSolicitacao(id);
                    base.versao(p.versao(), dto.versao());
                    String destino =
                            switch (acao) {
                                case "iniciar" -> "EM_ANALISE";
                                case "aprovar" -> "APROVADO";
                                case "reprovar" -> "RECUSADO";
                                default -> throw new AuthException(400, "Ação inválida.");
                            };
                    base.exigir(
                            acao.equals("iniciar")
                                    ? p.status().equals("PENDENTE")
                                    : Set.of("PENDENTE", "EM_ANALISE").contains(p.status()),
                            "Solicitação já decidida ou incompatível.");
                    repository.registrarResposta(destino, base.motivo(dto.motivo()), id);
                    base.auditar(
                            "RH",
                            acao.toUpperCase(Locale.ROOT),
                            "SOLICITACAO",
                            id,
                            "Resposta registrada pelo RH");
                    return repository.buscarSolicitacao(id);
                });
    }
}
