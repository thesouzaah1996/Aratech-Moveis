package com.aratechmoveis.almoxarifado.fornecedor.service.imp;

import com.aratechmoveis.almoxarifado.Response;
import com.aratechmoveis.almoxarifado.exceptions.NotFoundException;
import com.aratechmoveis.almoxarifado.exceptions.RecursoJaExistenteException;
import com.aratechmoveis.almoxarifado.fornecedor.dto.FornecedorDTO;
import com.aratechmoveis.almoxarifado.fornecedor.model.Fornecedor;
import com.aratechmoveis.almoxarifado.fornecedor.model.Representante;
import com.aratechmoveis.almoxarifado.fornecedor.repository.FornecedorRepository;
import com.aratechmoveis.almoxarifado.fornecedor.service.FornecedorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FornecedorServiceImp implements FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response addFornecedor(FornecedorDTO fornecedorDTO) {
        fornecedorDTO.setEmail(fornecedorDTO.getEmail().trim().toLowerCase());

        if (fornecedorRepository.existsByEmail(fornecedorDTO.getEmail())) {
            throw new RecursoJaExistenteException("Já existe um fornecedor com este e-mail. Por favor, certifique-se que os dados estão corretos.");
        }

        Fornecedor novoFornecedor = modelMapper.map(fornecedorDTO, Fornecedor.class);
        novoFornecedor.setAtivo(true);

        Fornecedor fornecedorSalvo = fornecedorRepository.save(novoFornecedor);
        log.info("Fornecedor criado com nome={}", fornecedorSalvo.getNome());

        return Response.builder()
                .status(201)
                .message("Fornecedor adicionado com sucesso")
                .fornecedorDTO(modelMapper.map(fornecedorSalvo, FornecedorDTO.class))
                .build();
    }

    @Override
    public Response updateFornecedor(Long id, FornecedorDTO fornecedorDTO) {
        Fornecedor fornecedorExistente = fornecedorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fornecedor não encontrado. Se você quer prosseguir editando o fornecedor, verifique o id informado."));

        if (fornecedorDTO.getNome() != null) {
            fornecedorExistente.setNome(fornecedorDTO.getNome());
        }

        if (fornecedorDTO.getEmail() != null) {
            fornecedorExistente.setEmail(fornecedorDTO.getEmail());
        }

        if (fornecedorDTO.getTelefone() != null) {
            fornecedorExistente.setTelefone(fornecedorDTO.getTelefone());
        }

        if (fornecedorDTO.getRepresentante() != null) {
            fornecedorExistente.setRepresentante(new Representante(
                    fornecedorDTO.getRepresentante().nomeRepresentante(),
                    fornecedorDTO.getRepresentante().telefoneRepresentante(),
                    fornecedorDTO.getRepresentante().emailRepresentante()
            ));

        }

        fornecedorRepository.save(fornecedorExistente);

        FornecedorDTO fornecedorEditado = modelMapper.map(fornecedorExistente, FornecedorDTO.class);

        return Response.builder()
                .status(200)
                .message("Fornecedor atualizado com sucesso")
                .fornecedorDTO(fornecedorEditado)
                .build();

    }

    @Override
    public Response getFornecedores() {
        List<Fornecedor> fornecedores = fornecedorRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<FornecedorDTO> fornecedoresDTO = modelMapper.map(fornecedores, new TypeToken<List<FornecedorDTO>>()
        {}.getType());

        return Response.builder()
                .status(200)
                .message("Fornecedores listados com sucesso")
                .fornecedores(fornecedoresDTO)
                .build();
    }

    @Override
    public Response disableFornecedor(Long id) {
        Fornecedor fornecedorAtivo = fornecedorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fornecedor não encontrado. Por favor, para desativar confira o id."));

        fornecedorAtivo.setAtivo(false);

        fornecedorRepository.save(fornecedorAtivo);

        return Response.builder()
                .status(200)
                .message("Fornecedor desabilitado com sucesso")
                .build();
    }

    @Override
    public Response enableFornecedor(Long id) {
        Fornecedor fornecedorInativo = fornecedorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fornecedor inativo não encontrado. Por favor, para seguir, verifique o id informado"));

        fornecedorInativo.setAtivo(true);

        fornecedorRepository.save(fornecedorInativo);

        return Response.builder()
                .status(200)
                .message("Fornecedor ativado com sucesso.")
                .build();
    }
}
