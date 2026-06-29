package com.aratechmoveis.login.perfil.service.imp;

import com.aratechmoveis.login.Response;
import com.aratechmoveis.login.exceptions.NotFoundException;
import com.aratechmoveis.login.exceptions.RecursoJaExistenteException;
import com.aratechmoveis.login.perfil.dto.PerfilDTO;
import com.aratechmoveis.login.perfil.modelo.Perfil;
import com.aratechmoveis.login.perfil.repository.PerfilRepository;
import com.aratechmoveis.login.perfil.service.PerfilService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerfilServiceImp implements PerfilService {

    private final PerfilRepository perfilRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response criarPerfil(PerfilDTO perfilDTO) {
        if (perfilRepository.existsByNomeIgnoreCase(perfilDTO.getNome())) {
            throw new RecursoJaExistenteException("Desculpe, mas esse perfil já existe");
        }

        Perfil perfilParaSalvar = modelMapper.map(perfilDTO, Perfil.class);
        perfilRepository.save(perfilParaSalvar);
        log.info("Perfil criado com nome={}", perfilParaSalvar.getNome());

        PerfilDTO perfilCriadoDTO = modelMapper.map(perfilParaSalvar, PerfilDTO.class);

        return Response.builder()
                .status(201)
                .message("Perfil criado com sucesso")
                .perfil(perfilCriadoDTO)
                .build();
    }

    @Override
    public Response getPerfis() {
        List<Perfil> perfis = perfilRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<PerfilDTO> perfisDTO = modelMapper.map(perfis, new TypeToken<List<PerfilDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("Perfis listados com sucesso")
                .perfis(perfisDTO)
                .build();
    }

    @Override
    @Transactional
    public Response updatePerfil(Long id, PerfilDTO perfilDTO) {
        Perfil perfilExistente = perfilRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Perfil não encontrado, para prosseguir com a atualização, confira o id informado"));

        if (perfilDTO.getNome() != null && !perfilDTO.getNome().isBlank()) {
            if (perfilRepository.existsByNomeIgnoreCase(perfilDTO.getNome())
                    && !perfilExistente.getNome().equalsIgnoreCase(perfilDTO.getNome())) {
                throw new RecursoJaExistenteException("Já existe um perfil com esse nome");
            }
            perfilExistente.setNome(perfilDTO.getNome());
        }

        perfilRepository.save(perfilExistente);
        log.info("Perfil com id={} alterado com sucesso", id);

        PerfilDTO novoPerfil = modelMapper.map(perfilExistente, PerfilDTO.class);

        return Response.builder()
                .status(200)
                .message("Perfil alterado com sucesso")
                .perfil(novoPerfil)
                .build();
    }

    @Override
    @Transactional
    public Response ativarPerfil(Long id) {
        Perfil perfil = perfilRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Perfil não encontrado, verifique o id informado"));

        perfil.setAtivo(true);
        perfilRepository.save(perfil);
        log.info("Perfil com id={} ativado com sucesso", id);

        return Response.builder()
                .status(200)
                .message("Perfil ativado com sucesso")
                .build();
    }

    @Override
    @Transactional
    public Response desativarPerfil(Long id) {
        Perfil perfil = perfilRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Perfil não encontrado, verifique o id informado"));

        perfil.setAtivo(false);
        perfilRepository.save(perfil);
        log.info("Perfil com id={} desativado com sucesso", id);

        return Response.builder()
                .status(200)
                .message("Perfil desativado com sucesso")
                .build();
    }
}
