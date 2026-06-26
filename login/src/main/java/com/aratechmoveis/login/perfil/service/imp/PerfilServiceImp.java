package com.aratechmoveis.login.perfil.service.imp;

import com.aratechmoveis.login.exception.ConflictException;
import com.aratechmoveis.login.exception.NotFoundException;
import com.aratechmoveis.login.perfil.dto.PerfilDTO;
import com.aratechmoveis.login.perfil.model.Perfil;
import com.aratechmoveis.login.perfil.repository.PerfilRepository;
import com.aratechmoveis.login.perfil.service.PerfilService;
import com.aratechmoveis.login.res.Response;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PerfilServiceImp implements PerfilService {

    private final PerfilRepository perfilRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response<PerfilDTO> createPerfil(PerfilDTO perfilDTO) {
        String nome = perfilDTO.getNome().trim().toUpperCase();

        if (perfilRepository.existsByNome(nome)) {
            throw new ConflictException("Já existe um perfil com o nome '" + nome + "'. Por favor, escolha um nome diferente.");
        }

        Perfil novoPerfil = new Perfil();
        novoPerfil.setNome(nome);
        perfilRepository.save(novoPerfil);

        return Response.<PerfilDTO>builder()
                .statusCode(201)
                .mensagem("Perfil criado com sucesso")
                .data(modelMapper.map(novoPerfil, PerfilDTO.class))
                .build();
    }

    @Override
    public Response<PerfilDTO> updatePerfil(Long id, PerfilDTO perfilRequest) {
        Perfil perfil = perfilRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Perfil não encontrado. Por favor, confira o id informado."));

        String novoNome = perfilRequest.getNome().trim().toUpperCase();

        if (novoNome.equals(perfil.getNome())) {
            return Response.<PerfilDTO>builder()
                    .statusCode(200)
                    .mensagem("Nenhuma alteração realizada")
                    .data(modelMapper.map(perfil, PerfilDTO.class))
                    .build();
        }

        if (perfilRepository.existsByNome(novoNome)) {
            throw new ConflictException("Já existe um perfil com o nome '" + novoNome + "'. Por favor, escolha um nome diferente.");
        }

        perfil.setNome(novoNome);
        perfilRepository.save(perfil);

        return Response.<PerfilDTO>builder()
                .statusCode(200)
                .mensagem("Perfil alterado com sucesso")
                .data(modelMapper.map(perfil, PerfilDTO.class))
                .build();
    }

    @Override
    public Response<List<PerfilDTO>> getTodosPerfis() {

        List<Perfil> perfis = perfilRepository.findAll();

        return Response.<List<PerfilDTO>>builder()
                .statusCode(200)
                .mensagem("Perfis listados com sucesso")
                .data(modelMapper.map(perfis, new TypeToken<List<PerfilDTO>>(){}.getType()))
                .build();
    }

    @Override
    public Response<?> enablePerfil(Long id) {

        Perfil perfilDesativado = perfilRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Perfil não encontrado, para ativa-lo, por favor, confira o id."));

        perfilDesativado.setAtivo(true);

        perfilRepository.save(perfilDesativado);

        return Response.<PerfilDTO>builder()
                .statusCode(200)
                .mensagem("Perfil ativado com sucesso")
                .data(modelMapper.map(perfilDesativado, PerfilDTO.class))
                .build();
    }

    @Override
    public Response<?> disablePerfil(Long id) {

        Perfil perfilAtivado = perfilRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Perfil não encontrado, para desativa-lo, por favor, confira o id."));

        perfilAtivado.setAtivo(true);

        perfilRepository.save(perfilAtivado);

        return Response.<PerfilDTO>builder()
                .statusCode(200)
                .mensagem("Perfil ativado com sucesso")
                .data(modelMapper.map(perfilAtivado, PerfilDTO.class))
                .build();
    }
}
