package com.aratech.moveis.manutencao.mecanicos.service;

import com.aratech.moveis.manutencao.chamados.dto.Response;
import com.aratech.moveis.manutencao.mecanicos.dto.MecanicoDTO;

public interface MecanicoService {
    Response listar(int pagina, int tamanho, Boolean ativo);

    Response salvar(Long id, MecanicoDTO dto);

    Response status(Long id, boolean ativo);
}
