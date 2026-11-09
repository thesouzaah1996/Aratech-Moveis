package com.aratech.moveis.assistencia.estoque.service;

import com.aratech.moveis.assistencia.estoque.dto.MovimentoAssistenciaDTO;
import com.aratech.moveis.assistencia.estoque.dto.PecaAssistenciaAtualizacaoDTO;
import com.aratech.moveis.assistencia.estoque.dto.PecaAssistenciaDTO;
import com.aratech.moveis.assistencia.estoque.dto.Response;

public interface PecaAssistenciaService {
    Response adicionar(PecaAssistenciaDTO dto);

    Response listar(int pagina, int tamanho, String busca, boolean abaixoMinimo);

    Response buscar(Long id);

    Response atualizar(Long id, PecaAssistenciaAtualizacaoDTO dto);

    Response arquivar(Long id);

    Response entrada(Long id, MovimentoAssistenciaDTO dto);

    Response saida(Long id, MovimentoAssistenciaDTO dto);

    Response historico(Long id, int pagina, int tamanho);
}
