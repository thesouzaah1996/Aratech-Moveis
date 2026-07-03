package com.aratechmoveis.recursoshumanos.funcionarios.service.imp;

import com.aratechmoveis.recursoshumanos.exception.NotFoundException;
import com.aratechmoveis.recursoshumanos.exception.RecursoJaExistenteException;
import com.aratechmoveis.recursoshumanos.funcionarios.dto.AtribuirPerfisDTO;
import com.aratechmoveis.recursoshumanos.funcionarios.dto.FuncionarioDTO;
import com.aratechmoveis.recursoshumanos.funcionarios.entity.Funcionario;
import com.aratechmoveis.recursoshumanos.funcionarios.repository.FuncionarioRepository;
import com.aratechmoveis.recursoshumanos.funcionarios.service.FuncionarioService;
import com.aratechmoveis.recursoshumanos.perfil.entity.Perfil;
import com.aratechmoveis.recursoshumanos.perfil.repository.PerfilRepository;
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
    private final PerfilRepository perfilRepository;
    private final ModelMapper modelmapper;

    @Override
    @Transactional
    public Response addFuncionario(FuncionarioDTO funcionario) {

        if (funcionarioRepository.existsByCpf(funcionario.getCpf())) {
            throw new RecursoJaExistenteException("Já existe um funcionário cadastrado com esse CPF");
        }

        if (funcionarioRepository.existsByEmail(funcionario.getEmail())) {
            throw new RecursoJaExistenteException("Já existe um funcionário cadastrado com esse e-mail");
        }

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
    public Response getFuncionariosPorNome(String nome) {

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome para busca não pode estar vazio.");
        }

        List<FuncionarioDTO> funcionarios = funcionarioRepository.findByNomeContainingIgnoreCase(nome.trim()).stream()
                .map(funcionario -> modelmapper.map(funcionario, FuncionarioDTO.class))
                .toList();

        return Response.builder()
                .status(200)
                .mensagem("Funcionarios encontrados com sucesso")
                .funcionarios(funcionarios)
                .build();
    }

    @Override
    @Transactional
    public Response updateFuncionario(Long id, FuncionarioDTO funcionarioDTO) {

        Funcionario funcionarioExistente = funcionarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Funcionário não encontrado, para atualizar o funcionário, por favor verifique o id"));

        if (funcionarioRepository.existsByCpfAndIdNot(funcionarioDTO.getCpf(), id)) {
            throw new RecursoJaExistenteException("Já existe um funcionário cadastrado com esse CPF");
        }

        if (funcionarioRepository.existsByEmailAndIdNot(funcionarioDTO.getEmail(), id)) {
            throw new RecursoJaExistenteException("Já existe um funcionário cadastrado com esse e-mail");
        }

        funcionarioExistente.setNome(funcionarioDTO.getNome());
        funcionarioExistente.setCpf(funcionarioDTO.getCpf());
        funcionarioExistente.setRg(funcionarioDTO.getRg());
        funcionarioExistente.setPis(funcionarioDTO.getPis());
        funcionarioExistente.setDataNascimento(funcionarioDTO.getDataNascimento());
        funcionarioExistente.setSexo(funcionarioDTO.getSexo());
        funcionarioExistente.setEstadoCivil(funcionarioDTO.getEstadoCivil());
        funcionarioExistente.setNomeMae(funcionarioDTO.getNomeMae());
        funcionarioExistente.setNomePai(funcionarioDTO.getNomePai());
        funcionarioExistente.setCep(funcionarioDTO.getCep());
        funcionarioExistente.setLogradouro(funcionarioDTO.getLogradouro());
        funcionarioExistente.setNumero(funcionarioDTO.getNumero());
        funcionarioExistente.setComplemento(funcionarioDTO.getComplemento());
        funcionarioExistente.setBairro(funcionarioDTO.getBairro());
        funcionarioExistente.setCidade(funcionarioDTO.getCidade());
        funcionarioExistente.setUf(funcionarioDTO.getUf());
        funcionarioExistente.setTelefone(funcionarioDTO.getTelefone());
        funcionarioExistente.setCelular(funcionarioDTO.getCelular());
        funcionarioExistente.setDataAdmissao(funcionarioDTO.getDataAdmissao());
        funcionarioExistente.setTipoContrato(funcionarioDTO.getTipoContrato());
        funcionarioExistente.setJornadaHoras(funcionarioDTO.getJornadaHoras());
        funcionarioExistente.setCargo(funcionarioDTO.getCargo());
        funcionarioExistente.setTipoFuncionario(funcionarioDTO.getTipoFuncionario());
        funcionarioExistente.setSetor(funcionarioDTO.getSetor());
        funcionarioExistente.setSalario(funcionarioDTO.getSalario());
        funcionarioExistente.setEmail(funcionarioDTO.getEmail());
        funcionarioExistente.setBanco(funcionarioDTO.getBanco());
        funcionarioExistente.setAgencia(funcionarioDTO.getAgencia());
        funcionarioExistente.setConta(funcionarioDTO.getConta());
        funcionarioExistente.setTipoConta(funcionarioDTO.getTipoConta());
        funcionarioExistente.setPix(funcionarioDTO.getPix());

        funcionarioRepository.save(funcionarioExistente);
        log.info("Funcionario id={} atualizado com sucesso", id);

        return Response.builder()
                .status(200)
                .mensagem("Funcionario atualizado com sucesso")
                .funcionario(modelmapper.map(funcionarioExistente, FuncionarioDTO.class))
                .build();
    }

    @Override
    @Transactional
    public Response enableFuncionario(Long id) {
        Funcionario funcionarioInativo = funcionarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Funcionário não encontrado, para ativar o funcionario, por favor verifique o id"));

        if (funcionarioInativo.isAtivo()) {
            throw new RecursoJaExistenteException("Funcionário já está ativo");
        }

        funcionarioInativo.setAtivo(true);
        funcionarioRepository.save(funcionarioInativo);
        log.info("Funcionario id={} ativado com sucesso", id);

        return Response.builder()
                .status(200)
                .mensagem("Funcionario ativado com sucesso")
                .funcionario(modelmapper.map(funcionarioInativo, FuncionarioDTO.class))
                .build();
    }

    @Override
    @Transactional
    public Response disableFuncionario(Long id) {
        Funcionario funcionarioAtivo = funcionarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Funcionário não encontrado, para desativar o funcionario, por favor verifique o id"));

        if (!funcionarioAtivo.isAtivo()) {
            throw new RecursoJaExistenteException("Funcionário já está inativo");
        }

        funcionarioAtivo.setAtivo(false);
        funcionarioRepository.save(funcionarioAtivo);
        log.info("Funcionario id={} desativado com sucesso", id);

        return Response.builder()
                .status(200)
                .mensagem("Funcionario desativado com sucesso")
                .funcionario(modelmapper.map(funcionarioAtivo, FuncionarioDTO.class))
                .build();
    }

    @Override
    @Transactional
    public Response atribuirPerfis(Long id, AtribuirPerfisDTO atribuirPerfisDTO) {
        Funcionario funcionario = funcionarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Funcionário não encontrado, verifique o id informado"));

        List<Perfil> perfis = perfilRepository.findAllById(atribuirPerfisDTO.getPerfisIds());

        if (perfis.size() != atribuirPerfisDTO.getPerfisIds().size()) {
            throw new NotFoundException("Um ou mais perfis informados não foram encontrados");
        }

        funcionario.setPerfis(perfis);
        funcionarioRepository.save(funcionario);
        log.info("Perfis do funcionario id={} atualizados com sucesso", id);

        return Response.builder()
                .status(200)
                .mensagem("Permissões atualizadas com sucesso")
                .funcionario(modelmapper.map(funcionario, FuncionarioDTO.class))
                .build();
    }
}
