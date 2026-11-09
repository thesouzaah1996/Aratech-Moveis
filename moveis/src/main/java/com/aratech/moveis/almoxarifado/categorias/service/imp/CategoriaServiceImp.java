package com.aratech.moveis.almoxarifado.categorias.service.imp;

import com.aratech.moveis.almoxarifado.categorias.dto.CategoriaDTO;
import com.aratech.moveis.almoxarifado.categorias.dto.CategoriaResumoDTO;
import com.aratech.moveis.almoxarifado.categorias.dto.Response;
import com.aratech.moveis.almoxarifado.categorias.exception.CategoriaException;
import com.aratech.moveis.almoxarifado.categorias.model.Categoria;
import com.aratech.moveis.almoxarifado.categorias.repository.CategoriaRepository;
import com.aratech.moveis.almoxarifado.categorias.service.CategoriaService;
import com.aratech.moveis.almoxarifado.categorias.validation.CategoriaValidator;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImp implements CategoriaService {
    private final CategoriaRepository repository;
    private final CategoriaValidator validator;

    @Override
    @Transactional
    public Response addCategoria(CategoriaDTO dto) {
        String nome = validator.validarNome(dto);
        if (repository.existsByNomeIgnoreCase(nome)) {
            throw new CategoriaException(
                    HttpStatus.CONFLICT, "Ja existe uma categoria com este nome.");
        }
        var categoria = repository.saveAndFlush(Categoria.builder().nome(nome).build());
        return resposta(categoria, HttpStatus.CREATED, "Categoria cadastrada com sucesso.");
    }

    @Override
    @Transactional(readOnly = true)
    public Response getAllCategorias() {
        return getAllCategorias(0, 20);
    }

    @Override
    @Transactional(readOnly = true)
    public Response getAllCategorias(int pagina, int tamanho) {
        return getAllCategorias(pagina, tamanho, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Response getAllCategorias(int pagina, int tamanho, String nome, Boolean ativo) {
        validator.validarPaginacao(pagina, tamanho);
        String busca = nome == null ? "" : nome.strip();
        if (busca.length() > 100
                || busca.codePoints()
                        .anyMatch(
                                c ->
                                        Character.isISOControl(c)
                                                || Character.getType(c) == Character.FORMAT)) {
            throw new CategoriaException(
                    HttpStatus.BAD_REQUEST, "Busca deve ter ate 100 caracteres validos.");
        }
        var pageable = PageRequest.of(pagina, tamanho, Sort.by("id").ascending());
        var resultado =
                ativo != null
                        ? repository.findByNomeContainingIgnoreCaseAndAtivo(busca, ativo, pageable)
                        : busca.isEmpty()
                                ? repository.findAll(pageable)
                                : repository.findByNomeContainingIgnoreCase(busca, pageable);
        return Response.builder()
                .status(HttpStatus.OK.value())
                .categorias(resultado.getContent().stream().map(this::resumo).toList())
                .pagina(pagina)
                .tamanho(tamanho)
                .total(resultado.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response getCategoriaById(Long id) {
        validator.validarId(id);
        return resposta(buscar(id, false), HttpStatus.OK, "Categoria encontrada.");
    }

    @Override
    @Transactional
    public Response updateCategoria(CategoriaDTO dto, Long id) {
        validator.validarId(id);
        String nome = validator.validarNome(dto);
        var categoria = buscar(id, true);
        if (repository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new CategoriaException(
                    HttpStatus.CONFLICT, "Ja existe outra categoria com este nome.");
        }
        categoria.setNome(nome);
        return resposta(
                repository.saveAndFlush(categoria),
                HttpStatus.OK,
                "Categoria atualizada com sucesso.");
    }

    @Override
    @Transactional
    public Response enableCategoria(Long id) {
        return alterarStatus(id, true);
    }

    @Override
    @Transactional
    public Response disableCategoria(Long id) {
        return alterarStatus(id, false);
    }

    private Response alterarStatus(Long id, boolean ativo) {
        validator.validarId(id);
        var categoria = buscar(id, true);
        if (categoria.getAtivo() == null) {
            throw new CategoriaException(
                    HttpStatus.CONFLICT, "A categoria possui um status inconsistente.");
        }
        if (categoria.getAtivo() == ativo) {
            return resposta(
                    categoria,
                    HttpStatus.OK,
                    ativo ? "Categoria ja esta ativa." : "Categoria ja esta inativa.");
        }
        categoria.setAtivo(ativo);
        return resposta(
                repository.saveAndFlush(categoria),
                HttpStatus.OK,
                ativo ? "Categoria ativada com sucesso." : "Categoria desativada com sucesso.");
    }

    private Categoria buscar(Long id, boolean bloquear) {
        return (bloquear ? repository.findByIdForUpdate(id) : repository.findById(id))
                .orElseThrow(
                        () ->
                                new CategoriaException(
                                        HttpStatus.NOT_FOUND, "Categoria nao encontrada."));
    }

    private CategoriaResumoDTO resumo(Categoria categoria) {
        return new CategoriaResumoDTO(categoria.getId(), categoria.getNome(), categoria.getAtivo());
    }

    private Response resposta(Categoria categoria, HttpStatus status, String mensagem) {
        return Response.builder()
                .status(status.value())
                .mensagem(mensagem)
                .categoriaId(categoria.getId())
                .nome(categoria.getNome())
                .ativo(categoria.getAtivo())
                .categoria(resumo(categoria))
                .build();
    }
}
