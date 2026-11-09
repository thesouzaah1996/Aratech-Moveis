package com.aratech.moveis.rh.perfis.service.imp;

import com.aratech.moveis.rh.perfis.dto.PerfilResumoDTO;
import com.aratech.moveis.rh.perfis.dto.UsuarioPerfilDTO;
import com.aratech.moveis.rh.perfis.exception.PerfilException;
import com.aratech.moveis.rh.perfis.repository.AtribuicaoPerfilRepository;
import com.aratech.moveis.rh.perfis.service.AtribuicaoPerfilService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AtribuicaoPerfilServiceImp implements AtribuicaoPerfilService {
    private final AtribuicaoPerfilRepository repository;

    @Transactional(readOnly = true)
    public Page<UsuarioPerfilDTO> buscar(String busca, int pagina, int tamanho) {
        if (pagina < 0
                || tamanho < 1
                || tamanho > 100
                || (long) pagina * tamanho > Integer.MAX_VALUE)
            throw new PerfilException(HttpStatus.BAD_REQUEST, "Paginação inválida.");
        String nome = busca == null ? "" : busca.strip();
        if (nome.length() > 150 || nome.matches("(?s).*[\\p{Cc}\\p{Cf}].*"))
            throw new PerfilException(HttpStatus.BAD_REQUEST, "Busca inválida.");
        // A busca usa texto literal, incluindo % e _, sem tratar o nome como padrão SQL.

        Long total = repository.contarFuncionariosPorBusca(nome, nome);
        var usuarios =
                repository.listarFuncionariosPorBusca(nome, nome, tamanho, (long) pagina * tamanho);
        return new PageImpl<>(usuarios, PageRequest.of(pagina, tamanho), total == null ? 0 : total);
    }

    @Transactional
    public UsuarioPerfilDTO atribuir(Long usuarioId, String nome) {
        if (usuarioId == null
                || usuarioId <= 0
                || nome == null
                || !nome.strip().equals("#") && !nome.strip().matches("[\\p{L}\\p{N} _-]{3,100}"))
            throw new PerfilException(
                    HttpStatus.BAD_REQUEST, "Informe o nome de um perfil cadastrado.");
        var usuarios = repository.bloquearFuncionario(usuarioId);
        if (usuarios.isEmpty())
            throw new PerfilException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        var perfis = repository.buscarPerfilPorNome(nome.strip().replaceAll(" +", " "));
        if (perfis.isEmpty())
            throw new PerfilException(
                    HttpStatus.NOT_FOUND,
                    "Perfil não encontrado. Informe o nome de um perfil cadastrado.");
        int novos = repository.inserirVinculo(usuarioId, perfis.getFirst().id());
        var usuario = usuarios.getFirst();
        return new UsuarioPerfilDTO(
                usuario.id(), usuario.nome(), usuario.email(), usuario.setor(), perfis(usuarioId));
    }

    private List<PerfilResumoDTO> perfis(Long usuarioId) {
        return repository.listarPerfisDoUsuario(usuarioId);
    }
}
