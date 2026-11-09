package com.aratech.moveis.rh.perfis.service;

import com.aratech.moveis.rh.perfis.dto.PerfilDTO;
import com.aratech.moveis.rh.perfis.dto.PerfilResumoDTO;

import org.springframework.data.domain.Page;

public interface PerfilService {
    Page<PerfilResumoDTO> listar(int pagina, int tamanho, String busca);

    PerfilResumoDTO criar(PerfilDTO dto);

    PerfilResumoDTO atualizar(Long id, PerfilDTO dto);
}
