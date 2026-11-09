package com.aratech.moveis.rh.perfis.service.imp;

import com.aratech.moveis.rh.perfis.dto.PerfilDTO;
import com.aratech.moveis.rh.perfis.dto.PerfilResumoDTO;
import com.aratech.moveis.rh.perfis.exception.PerfilException;
import com.aratech.moveis.rh.perfis.model.Perfil;
import com.aratech.moveis.rh.perfis.repository.PerfilRepository;
import com.aratech.moveis.rh.perfis.service.PerfilService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PerfilServiceImp implements PerfilService {
    private final PerfilRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Page<PerfilResumoDTO> listar(int pagina, int tamanho, String busca) {
        if (pagina < 0
                || tamanho < 1
                || tamanho > 100
                || (long) pagina * tamanho > Integer.MAX_VALUE)
            throw invalido("Paginacao invalida.");
        String nome = busca == null ? "" : busca.strip();
        if (nome.length() > 100 || nome.matches("(?s).*[\\p{Cc}\\p{Cf}].*"))
            throw invalido("Busca invalida.");
        return repository
                .findByNomeContainingIgnoreCase(
                        nome, PageRequest.of(pagina, tamanho, Sort.by("id")))
                .map(PerfilResumoDTO::from);
    }

    @Override
    @Transactional
    public PerfilResumoDTO criar(PerfilDTO dto) {
        String nome = validar(dto);
        if (repository.existsByNomeIgnoreCase(nome)) throw conflito();
        return PerfilResumoDTO.from(repository.saveAndFlush(Perfil.builder().nome(nome).build()));
    }

    @Override
    @Transactional
    public PerfilResumoDTO atualizar(Long id, PerfilDTO dto) {
        if (id == null || id <= 0) throw invalido("ID invalido.");
        String nome = validar(dto);
        var perfil =
                repository
                        .findByIdForUpdate(id)
                        .orElseThrow(
                                () ->
                                        new PerfilException(
                                                HttpStatus.NOT_FOUND, "Perfil nao encontrado."));
        if (repository.existsByNomeIgnoreCaseAndIdNot(nome, id)) throw conflito();
        perfil.setNome(nome);
        return PerfilResumoDTO.from(repository.saveAndFlush(perfil));
    }

    private String validar(PerfilDTO dto) {
        if (dto == null
                || dto.nome() == null
                || dto.nome().length() > 100
                || dto.nome().matches("(?s).*[\\p{Cc}\\p{Cf}].*"))
            throw invalido("Informe um nome valido para o perfil.");
        String nome = dto.nome().strip().replaceAll(" +", " ").toUpperCase(Locale.ROOT);
        if (!nome.matches("[\\p{L}\\p{N} _-]{3,100}"))
            throw invalido(
                    "Informe um nome de 3 a 100 caracteres, usando letras, numeros, espacos, hifen"
                            + " ou sublinhado.");
        return nome;
    }

    private PerfilException conflito() {
        return new PerfilException(HttpStatus.CONFLICT, "Perfil ja cadastrado.");
    }

    private PerfilException invalido(String mensagem) {
        return new PerfilException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
