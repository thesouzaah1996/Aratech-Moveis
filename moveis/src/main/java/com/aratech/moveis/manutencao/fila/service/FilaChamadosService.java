package com.aratech.moveis.manutencao.fila.service;

import com.aratech.moveis.manutencao.chamados.dto.Response;
import com.aratech.moveis.manutencao.fila.dto.AtribuicaoDTO;

public interface FilaChamadosService {
    Response listar(int pagina, int tamanho, String status, String busca);

    Response buscar(Long id);

    Response atribuir(Long id, AtribuicaoDTO dto);

    Response concluir(Long id);
}
