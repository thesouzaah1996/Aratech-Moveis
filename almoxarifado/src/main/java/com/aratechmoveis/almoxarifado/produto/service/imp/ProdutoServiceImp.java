package com.aratechmoveis.almoxarifado.produto.service.imp;

import com.aratechmoveis.almoxarifado.response.Response;
import com.aratechmoveis.almoxarifado.categoria.modelo.Categoria;
import com.aratechmoveis.almoxarifado.categoria.repository.CategoriaRepository;
import com.aratechmoveis.almoxarifado.fornecedor.model.Fornecedor;
import com.aratechmoveis.almoxarifado.fornecedor.repository.FornecedorRepository;
import com.aratechmoveis.almoxarifado.exceptions.NotFoundException;
import com.aratechmoveis.almoxarifado.exceptions.RecursoJaExistenteException;
import com.aratechmoveis.almoxarifado.produto.dto.ProdutoDTO;
import com.aratechmoveis.almoxarifado.produto.modelo.Produto;
import com.aratechmoveis.almoxarifado.produto.repository.ProdutoRepository;
import com.aratechmoveis.almoxarifado.produto.service.ProdutoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProdutoServiceImp implements ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ModelMapper modelMapper;
    private final CategoriaRepository categoriaRepository;
    private final FornecedorRepository fornecedorRepository;

    @Override
    @Transactional
    public Response addProduto(ProdutoDTO produtoDTO) {

        if (produtoRepository.existsBySku(produtoDTO.getSku())) {
            throw new RecursoJaExistenteException(
                    "Já existe um produto cadastrado com o SKU: " + produtoDTO.getSku()
            );
        }

        Categoria categoria = categoriaRepository.findById(produtoDTO.getCategoriaID())
                .orElseThrow(() -> new NotFoundException(
                        "Categoria não encontrada. Para adicionar um produto, sua categoria deve estar cadastrada."
                ));

        Fornecedor fornecedorExistente = fornecedorRepository.findById(produtoDTO.getFornecedorID())
                .orElseThrow(() -> new NotFoundException(
                        "Fornecedor não encontrado. Para adicionar um produto, o fornecedor deve estar cadastrado."
                ));

        Produto produtoParaSalvar = modelMapper.map(produtoDTO, Produto.class);
        produtoParaSalvar.setCategoria(categoria);
        produtoParaSalvar.setFornecedor(fornecedorExistente);

        if (produtoParaSalvar.getLocalArmazenamento() != null) {
            produtoParaSalvar.setLocalArmazenamento(
                    produtoParaSalvar.getLocalArmazenamento().trim().toLowerCase().replaceAll("\\s+", "")
            );
        }

        produtoRepository.save(produtoParaSalvar);
        ProdutoDTO produto = modelMapper.map(produtoParaSalvar, ProdutoDTO.class);

        return Response.builder()
                .status(201)
                .produto(produto)
                .message("Produto criado com sucesso")
                .build();
    }

    @Override
    public Response updateProduto(Long id, ProdutoDTO produtoDTO) {
        Produto produtoExistente = produtoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado"));

        if (produtoDTO.getCategoriaID() != null && produtoDTO.getCategoriaID() > 0) {
            Categoria categoria = categoriaRepository.findById(produtoDTO.getCategoriaID())
                    .orElseThrow(() -> new NotFoundException("Categoria não encontrada"));
            produtoExistente.setCategoria(categoria);
        }

        if (produtoDTO.getFornecedorID() != null && produtoDTO.getFornecedorID() > 0) {
            Fornecedor fornecedor = fornecedorRepository.findById(produtoDTO.getFornecedorID())
                    .orElseThrow(() -> new NotFoundException("Fornecedor não encontrado"));
            produtoExistente.setFornecedor(fornecedor);
        }

        if (produtoDTO.getNome() != null && !produtoDTO.getNome().isBlank()) {
            produtoExistente.setNome(produtoDTO.getNome());
        }

        if (produtoDTO.getDescricao() != null && !produtoDTO.getDescricao().isBlank()) {
            produtoExistente.setDescricao(produtoDTO.getDescricao());
        }

        if (produtoDTO.getLocalArmazenamento() != null && !produtoDTO.getLocalArmazenamento().isBlank()) {
            String local = produtoDTO.getLocalArmazenamento().trim().toLowerCase().replaceAll("\\s+", "");
            if (produtoRepository.existsByLocalArmazenamentoAndIdNot(local, id)) {
                throw new RecursoJaExistenteException(
                        "Já existe um produto armazenado no local: " + local
                );
            }
            produtoExistente.setLocalArmazenamento(local);
        }

        if (produtoDTO.getQuantidade() != null && produtoDTO.getQuantidade() >= 0) {
            produtoExistente.setQuantidade(produtoDTO.getQuantidade());
        }

        produtoRepository.save(produtoExistente);
        ProdutoDTO produto = modelMapper.map(produtoExistente, ProdutoDTO.class);

        return Response.builder()
                .status(200)
                .produto(produto)
                .message("Produto atualizado com sucesso")
                .build();
    }

    @Override
    @Transactional
    public Response getProdutos() {
        List<Produto> produtos = produtoRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<ProdutoDTO> produtoDTOS = modelMapper.map(produtos, new TypeToken<List<ProdutoDTO>>() {
        }.getType());

        return Response.builder()
                .status(200)
                .message("Produtos listados com sucesso")
                .produtos(produtoDTOS)
                .build();
    }

    @Override
    @Transactional
    public Response getProdutoById(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado, confira se o id está correto"));

        ProdutoDTO produtoDTO = modelMapper.map(produto, ProdutoDTO.class);

        return Response.builder()
                .status(200)
                .message("Produto listado com sucesso")
                .produto(produtoDTO)
                .build();
    }

    @Override
    public Response deleteProduto(Long id) {
        produtoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado, para deletar, confira se o id está correto"));

        produtoRepository.deleteById(id);

        return Response.builder()
                .status(204)
                .message("Produto deletado com sucesso")
                .build();
    }
}
