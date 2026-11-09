package com.aratech.moveis.rh.solicitacoes.service;

import com.aratech.moveis.rh.solicitacoes.dto.NovaSolicitacao;
import com.aratech.moveis.rh.solicitacoes.dto.Solicitacao;
import com.aratech.moveis.shared.dto.Comando;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;

import java.util.*;

public interface SolicitacaoRhService {

    List<Opcao> funcionarios();

    Pagina<Solicitacao> solicitacoes(int pagina, int tamanho, String busca);

    Solicitacao solicitar(NovaSolicitacao dto);

    Solicitacao solicitacaoAcao(long id, String acao, Comando dto);
}
