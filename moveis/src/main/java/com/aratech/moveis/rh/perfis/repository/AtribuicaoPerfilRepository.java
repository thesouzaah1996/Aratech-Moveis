package com.aratech.moveis.rh.perfis.repository;

import com.aratech.moveis.rh.perfis.dto.PerfilResumoDTO;
import com.aratech.moveis.rh.perfis.dto.UsuarioPerfilDTO;

import org.springframework.data.domain.*;

import java.util.List;

public interface AtribuicaoPerfilRepository {
    Long contarFuncionariosPorBusca(String nome, String nome2);

    List<UsuarioPerfilDTO> listarFuncionariosPorBusca(
            String nome, String nome2, int tamanho, long valor);

    List<UsuarioPerfilDTO> bloquearFuncionario(Long usuarioId);

    List<PerfilResumoDTO> buscarPerfilPorNome(String replaceAll);

    int inserirVinculo(Long usuarioId, Long id);

    List<PerfilResumoDTO> listarPerfisDoUsuario(Long usuarioId);
}
