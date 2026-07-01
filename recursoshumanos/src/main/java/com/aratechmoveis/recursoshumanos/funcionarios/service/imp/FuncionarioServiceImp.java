package com.aratechmoveis.recursoshumanos.funcionarios.service.imp;

import com.aratechmoveis.recursoshumanos.exception.NotFoundException;
import com.aratechmoveis.recursoshumanos.funcionarios.dto.FuncionarioDTO;
import com.aratechmoveis.recursoshumanos.funcionarios.entity.Funcionario;
import com.aratechmoveis.recursoshumanos.funcionarios.repository.FuncionarioRepository;
import com.aratechmoveis.recursoshumanos.funcionarios.service.FuncionarioService;
import com.aratechmoveis.recursoshumanos.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FuncionarioServiceImp implements FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final ModelMapper modelmapper;

    @Override
    @Transactional
    public Response addFuncionario(FuncionarioDTO funcionario) {

        Funcionario novoFuncionario = modelmapper.map(funcionario, Funcionario.class);

        funcionarioRepository.save(novoFuncionario);
        log.info("Usuario ={} criado com sucesso", funcionario.getNome());

        return Response.builder()
                .status(201)
                .mensagem("Usuario criado com sucesso")
                .funcionario(modelmapper.map(novoFuncionario, FuncionarioDTO.class))
                .build();
    }

    @Override
    public Response getFuncionarios() {
        List<FuncionarioDTO> funcionarios = funcionarioRepository.findAll().stream()
                .map(funcionario -> modelmapper.map(funcionario, FuncionarioDTO.class))
                .toList();

        return Response.builder()
                .status(200)
                .mensagem("Funcionarios listados com sucesso.")
                .funcionarios(funcionarios)
                .build();
    }

    @Override
    public Response getFuncionarioByNome(String nome) {
        List<FuncionarioDTO> funcionarios = funcionarioRepository.findByNomeContainingIgnoreCase(nome).stream()
                .map(funcionario -> modelmapper.map(funcionario, FuncionarioDTO.class))
                .toList();

        return Response.builder()
                .status(200)
                .mensagem("Funcionarios encontrados com sucesso")
                .funcionarios(funcionarios)
                .build();
    }

    @Override
    public Response updateFuncionario(Long id, FuncionarioDTO funcionarioDTO) {
        return null;
    }

    @Override
    public Response enableFuncionario(Long id) {
        return null;
    }

    @Override
    public Response disableFuncionario(Long id) {
        return null;
    }
}
