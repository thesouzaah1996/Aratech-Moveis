package com.aratech.moveis.almoxarifado.fornecedores.service.imp;

import com.aratech.moveis.almoxarifado.fornecedores.dto.FornecedorDTO;
import com.aratech.moveis.almoxarifado.fornecedores.dto.FornecedorResumoDTO;
import com.aratech.moveis.almoxarifado.fornecedores.dto.RepresentanteDTO;
import com.aratech.moveis.almoxarifado.fornecedores.dto.Response;
import com.aratech.moveis.almoxarifado.fornecedores.exception.FornecedorException;
import com.aratech.moveis.almoxarifado.fornecedores.model.Fornecedor;
import com.aratech.moveis.almoxarifado.fornecedores.model.Representante;
import com.aratech.moveis.almoxarifado.fornecedores.repository.FornecedorRepository;
import com.aratech.moveis.almoxarifado.fornecedores.service.FornecedorService;
import com.aratech.moveis.almoxarifado.fornecedores.validation.FornecedorValidator;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FornecedorServiceImp implements FornecedorService {
    private final FornecedorRepository repository;
    private final FornecedorValidator validator;

    @Override
    @Transactional
    public Response addFornecedor(FornecedorDTO dto) {
        validator.validarDados(dto);
        String nome = dto.getNome();
        if (repository.existsByNomeIgnoreCase(nome)) {
            throw new FornecedorException(
                    HttpStatus.CONFLICT, "Ja existe um fornecedor com este nome.");
        }
        if (repository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new FornecedorException(
                    HttpStatus.CONFLICT, "Ja existe um fornecedor com este e-mail.");
        }
        var novo = Fornecedor.builder().build();
        preencher(novo, dto);
        var fornecedor = repository.saveAndFlush(novo);
        return resposta(fornecedor, HttpStatus.CREATED, "Fornecedor cadastrado com sucesso.");
    }

    @Override
    @Transactional(readOnly = true)
    public Response getAllFornecedores() {
        return getAllFornecedores(0, 20);
    }

    @Override
    @Transactional(readOnly = true)
    public Response getAllFornecedores(int pagina, int tamanho) {
        return getAllFornecedores(pagina, tamanho, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Response getAllFornecedores(int pagina, int tamanho, String busca, Boolean ativo) {
        validator.validarPaginacao(pagina, tamanho);
        String termo = validator.validarBusca(busca);
        var pageable = PageRequest.of(pagina, tamanho, Sort.by("id").ascending());
        var resultado = repository.buscar(termo, ativo, pageable);
        return Response.builder()
                .status(HttpStatus.OK.value())
                .fornecedores(resultado.getContent().stream().map(this::resumo).toList())
                .pagina(pagina)
                .tamanho(tamanho)
                .total(resultado.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response getFornecedorById(Long id) {
        validator.validarId(id);
        return resposta(buscar(id, false), HttpStatus.OK, "Fornecedor encontrado.");
    }

    @Override
    @Transactional
    public Response updateFornecedor(FornecedorDTO dto, Long id) {
        validator.validarId(id);
        validator.validarDados(dto);
        String nome = dto.getNome();
        var fornecedor = buscar(id, true);
        if (repository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new FornecedorException(
                    HttpStatus.CONFLICT, "Ja existe outro fornecedor com este nome.");
        }
        if (repository.existsByEmailIgnoreCaseAndIdNot(dto.getEmail(), id)) {
            throw new FornecedorException(
                    HttpStatus.CONFLICT, "Ja existe outro fornecedor com este e-mail.");
        }
        preencher(fornecedor, dto);
        return resposta(
                repository.saveAndFlush(fornecedor),
                HttpStatus.OK,
                "Fornecedor atualizado com sucesso.");
    }

    @Override
    @Transactional
    public Response enableFornecedor(Long id) {
        return alterarStatus(id, true);
    }

    @Override
    @Transactional
    public Response disableFornecedor(Long id) {
        return alterarStatus(id, false);
    }

    private Response alterarStatus(Long id, boolean ativo) {
        validator.validarId(id);
        var fornecedor = buscar(id, true);
        if (fornecedor.getAtivo() == null) {
            throw new FornecedorException(
                    HttpStatus.CONFLICT, "O fornecedor possui um status inconsistente.");
        }
        if (fornecedor.getAtivo() == ativo) {
            return resposta(
                    fornecedor,
                    HttpStatus.OK,
                    ativo ? "Fornecedor ja esta ativo." : "Fornecedor ja esta inativo.");
        }
        fornecedor.setAtivo(ativo);
        return resposta(
                repository.saveAndFlush(fornecedor),
                HttpStatus.OK,
                ativo ? "Fornecedor ativado com sucesso." : "Fornecedor desativado com sucesso.");
    }

    private Fornecedor buscar(Long id, boolean bloquear) {
        return (bloquear ? repository.findByIdForUpdate(id) : repository.findById(id))
                .orElseThrow(
                        () ->
                                new FornecedorException(
                                        HttpStatus.NOT_FOUND, "Fornecedor nao encontrado."));
    }

    private FornecedorResumoDTO resumo(Fornecedor fornecedor) {
        var rep = fornecedor.getRepresentante();
        var representante =
                new RepresentanteDTO(
                        rep.getNomeRepresentante(),
                        rep.getTelefoneRepresentante(),
                        rep.getEmailRepresentante());
        return new FornecedorResumoDTO(
                fornecedor.getId(),
                fornecedor.getNome(),
                fornecedor.getEmail(),
                fornecedor.getTelefone(),
                representante,
                fornecedor.getAtivo());
    }

    private Response resposta(Fornecedor fornecedor, HttpStatus status, String mensagem) {
        return Response.builder()
                .status(status.value())
                .mensagem(mensagem)
                .fornecedorId(fornecedor.getId())
                .nome(fornecedor.getNome())
                .ativo(fornecedor.getAtivo())
                .fornecedor(resumo(fornecedor))
                .build();
    }

    private void preencher(Fornecedor fornecedor, FornecedorDTO dto) {
        fornecedor.setNome(dto.getNome());
        fornecedor.setEmail(dto.getEmail());
        fornecedor.setTelefone(dto.getTelefone());
        var rep = dto.getRepresentante();
        fornecedor.setRepresentante(
                Representante.builder()
                        .nomeRepresentante(rep.getNomeRepresentante())
                        .telefoneRepresentante(rep.getTelefoneRepresentante())
                        .emailRepresentante(rep.getEmailRepresentante())
                        .build());
    }
}
