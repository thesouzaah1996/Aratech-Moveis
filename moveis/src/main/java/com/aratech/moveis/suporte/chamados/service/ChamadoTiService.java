package com.aratech.moveis.suporte.chamados.service;

import com.aratech.moveis.suporte.chamados.dto.ChamadoTiDTO;
import com.aratech.moveis.suporte.chamados.dto.Response;

public interface ChamadoTiService {
    Response abrir(ChamadoTiDTO dto);

    Response listar(int pagina, int tamanho, String status, String busca);

    Response buscar(Long id);
}
