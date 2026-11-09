package com.aratech.moveis.pcp.requisicoes.repository;

import com.aratech.moveis.pcp.requisicoes.dto.Requisicao;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;

import java.util.*;
import java.util.List;
import java.util.Map;

public interface RequisicaoRepository {
    Pagina<Requisicao> listarRequisicoes(int pagina, int tamanho, String busca);

    List<Opcao> listarOrdensAbertas();

    List<Opcao> listarMateriaisDisponiveis();

    List<Map<String, Object>> bloquearOrdemAberta(Long ordemId);

    List<Map<String, Object>> buscarMaterialAtivo(Long produtoId);

    Long inserirRequisicao(Long ordemId, Long produtoId, Integer quantidade, String valor);

    Requisicao buscarRequisicao(Long id);

    Requisicao bloquearRequisicao(long id);

    List<Map<String, Object>> bloquearOrdemPlanejada(Long ordemId);

    int registrarDecisao(String destino, String motivo, long id);
}
