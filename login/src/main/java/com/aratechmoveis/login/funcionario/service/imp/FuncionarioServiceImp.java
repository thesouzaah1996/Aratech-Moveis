package com.aratechmoveis.login.funcionario.service.imp;

import com.aratechmoveis.login.response.Response;
import com.aratechmoveis.login.funcionario.dto.FuncionarioDTO;
import com.aratechmoveis.login.funcionario.entity.Funcionario;
import com.aratechmoveis.login.funcionario.entity.Perfil;
import com.aratechmoveis.login.exceptions.RecursoJaExistenteException;
import com.aratechmoveis.login.funcionario.repository.FuncionarioRepository;
import com.aratechmoveis.login.funcionario.repository.PerfilRepository;
import com.aratechmoveis.login.funcionario.service.FuncionarioService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FuncionarioServiceImp implements FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final PerfilRepository perfilRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response adicionarFuncionario(Funcionario funcionario) {
        if (funcionarioRepository.existsByIdFuncionario(funcionario.getIdFuncionario())) {
            throw new RecursoJaExistenteException("Funcionario já existe no sistema.");
        }

        if (funcionarioRepository.existsByEmailCorporativo(funcionario.getEmailCorporativo())) {
            throw new RecursoJaExistenteException("Email já existe no sistema.");
        }

        List<Perfil> perfis = funcionario.getPerfis().stream()
                .map(perfil -> perfilRepository.findByNome(perfil.getNome())
                        .orElseGet(() -> perfilRepository.save(Perfil.builder()
                                .nome(perfil.getNome())
                                .ativo(perfil.isAtivo())
                                .build())))
                .toList();
        funcionario.setPerfis(perfis);

        funcionarioRepository.save(funcionario);

        return Response.builder()
                .status(201)
                .mensagem("Usuário adicionado com sucesso.")
                .build();
    }
}
