package com.aratech.moveis.manutencao.estoque.service;

import com.aratech.moveis.manutencao.estoque.dto.MovimentacaoPecaDTO;
import com.aratech.moveis.manutencao.estoque.dto.PecaEstoqueAtualizacaoDTO;
import com.aratech.moveis.manutencao.estoque.dto.PecaEstoqueDTO;
import com.aratech.moveis.manutencao.estoque.dto.Response;

public interface PecaEstoqueService {
    Response adicionar(PecaEstoqueDTO dto);

    Response listar(int pagina, int tamanho, String busca);

    Response buscar(Long id);

    Response atualizar(Long id, PecaEstoqueAtualizacaoDTO dto);

    Response arquivar(Long id);

    Response entrada(Long id, MovimentacaoPecaDTO dto);

    Response saida(Long id, MovimentacaoPecaDTO dto);

    Response historico(Long id, int pagina, int tamanho);
}
