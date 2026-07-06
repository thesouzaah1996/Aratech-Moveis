package com.aratechmoveis.login.service.imp;

import com.aratechmoveis.login.response.Response;
import com.aratechmoveis.login.dto.FuncionarioDTO;
import com.aratechmoveis.login.entity.Funcionario;
import com.aratechmoveis.login.exceptions.RecursoJaExistenteException;
import com.aratechmoveis.login.repository.FuncionarioRepository;
import com.aratechmoveis.login.service.FuncionarioService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FuncionarioServiceImp implements FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response adicionarFuncionario(FuncionarioDTO funcionarioDTO) {
        Funcionario novoFuncionarioDTO = funcionarioRepository.findById(funcionarioDTO.getId())
                .orElseThrow(() -> new RecursoJaExistenteException("Funcionario Já existe no sistema."));

        Funcionario novoFuncionario = modelMapper.map(novoFuncionarioDTO, Funcionario.class);

        funcionarioRepository.save(novoFuncionario);

        return Response.builder()
                .status(201)
                .mensagem("Usuário adicionado com sucesso.")
                .build();
    }

    @Override
    public Response editarFuncionario(FuncionarioDTO funcionario) {
        return null;
    }
}
