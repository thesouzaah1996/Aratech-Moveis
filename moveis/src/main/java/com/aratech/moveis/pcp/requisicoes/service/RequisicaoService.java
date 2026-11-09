package com.aratech.moveis.pcp.requisicoes.service;

import com.aratech.moveis.pcp.requisicoes.dto.NovaRequisicao;
import com.aratech.moveis.pcp.requisicoes.dto.Requisicao;
import com.aratech.moveis.shared.dto.Comando;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;

import java.util.*;

public interface RequisicaoService {

    Pagina<Requisicao> listar(int pagina, int tamanho, String busca);

    List<Opcao> ordens();

    List<Opcao> materiais();

    Requisicao criar(NovaRequisicao dto);

    Requisicao acao(long id, String acao, Comando dto);
}
