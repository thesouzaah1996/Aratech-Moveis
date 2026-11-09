package com.aratech.moveis.rh.perfis.service;

import com.aratech.moveis.rh.perfis.dto.UsuarioPerfilDTO;

import org.springframework.data.domain.*;

public interface AtribuicaoPerfilService {

    Page<UsuarioPerfilDTO> buscar(String busca, int pagina, int tamanho);

    UsuarioPerfilDTO atribuir(Long usuarioId, String nome);
}
