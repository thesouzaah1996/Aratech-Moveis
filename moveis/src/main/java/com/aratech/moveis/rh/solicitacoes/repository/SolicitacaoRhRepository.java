package com.aratech.moveis.rh.solicitacoes.repository;

import com.aratech.moveis.rh.solicitacoes.dto.Solicitacao;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;

import java.util.*;
import java.util.List;
import java.util.Map;

public interface SolicitacaoRhRepository {
    List<Opcao> listarFuncionariosAtivos();

    List<Map<String, Object>> bloquearFuncionarioAtivo(long id);

    Pagina<Solicitacao> listarSolicitacoes(int pagina, int tamanho, String busca);

    Long inserirSolicitacao(Long funcionarioId, String tipo, String descricao);

    Solicitacao buscarSolicitacao(Long id);

    Solicitacao bloquearSolicitacao(long id);

    int registrarResposta(String destino, String valor, long id);
}
