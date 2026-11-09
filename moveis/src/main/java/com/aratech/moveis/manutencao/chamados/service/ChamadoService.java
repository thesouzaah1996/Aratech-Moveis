package com.aratech.moveis.manutencao.chamados.service;

import com.aratech.moveis.manutencao.chamados.dto.ChamadoDTO;
import com.aratech.moveis.manutencao.chamados.dto.Response;

public interface ChamadoService {
    Response solicitar(ChamadoDTO dto);

    Response listar(int pagina, int tamanho, String status);

    Response buscar(Long id);
}
