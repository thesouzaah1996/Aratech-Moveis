package com.aratechmoveis.almoxarifado.produto.service.imp;

import com.aratechmoveis.almoxarifado.Response;
import com.aratechmoveis.almoxarifado.categoria.entity.Categoria;
import com.aratechmoveis.almoxarifado.categoria.repository.CategoriaRepository;
import com.aratechmoveis.almoxarifado.exceptions.NotFoundException;
import com.aratechmoveis.almoxarifado.exceptions.RecursoJaExistenteException;
import com.aratechmoveis.almoxarifado.fornecedor.entity.Fornecedor;
import com.aratechmoveis.almoxarifado.fornecedor.repository.FornecedorRepository;
import com.aratechmoveis.almoxarifado.produto.dto.ProdutoDTO;
import com.aratechmoveis.almoxarifado.produto.entity.Produto;
import com.aratechmoveis.almoxarifado.produto.repository.ProdutoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProdutoServiceImp")
class ProdutoServiceImpTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private FornecedorRepository fornecedorRepository;

    @InjectMocks
    private ProdutoServiceImp produtoService;

    private ProdutoDTO umProdutoDTO() {
        ProdutoDTO dto = new ProdutoDTO();
        dto.setCategoriaID(1L);
        dto.setFornecedorID(1L);
        dto.setNome("Cadeira de Escritório");
        dto.setSku("SKU-001");
        dto.setQuantidade(10);
        dto.setLocalArmazenamento("Prateleira A01");
        return dto;
    }

    private Categoria umaCategoria() {
        return Categoria.builder().id(1L).nome("Móveis").build();
    }

    private Fornecedor umFornecedor() {
        return Fornecedor.builder().id(1L).nome("Fornecedor Teste").email("teste@email.com").telefone("(11) 1234-5678").ativo(true).build();
    }

    private Produto umProduto() {
        return Produto.builder()
                .id(1L)
                .nome("Cadeira de Escritório")
                .sku("SKU-001")
                .quantidade(10)
                .localArmazenamento("prateleiraa01")
                .categoria(umaCategoria())
                .fornecedor(umFornecedor())
                .build();
    }

    @Nested
    @DisplayName("adicionarProduto")
    class AddProduto {

        @Test
        @DisplayName("deve criar produto com sucesso e retornar status 201")
        void deveCriarProdutoComSucesso() {
            ProdutoDTO dto = umProdutoDTO();
            Categoria categoria = umaCategoria();
            Fornecedor fornecedor = umFornecedor();
            Produto produto = umProduto();

            given(produtoRepository.existsBySku(dto.getSku())).willReturn(false);
            given(categoriaRepository.findById(dto.getCategoriaID())).willReturn(Optional.of(categoria));
            given(fornecedorRepository.findById(dto.getFornecedorID())).willReturn(Optional.of(fornecedor));
            given(modelMapper.map(dto, Produto.class)).willReturn(produto);
            given(produtoRepository.save(produto)).willReturn(produto);
            given(modelMapper.map(produto, ProdutoDTO.class)).willReturn(dto);

            Response response = produtoService.adicionarProduto(dto);

            assertThat(response.getStatus()).isEqualTo(201);
            assertThat(response.getMessage()).isEqualTo("Produto criado com sucesso");
            assertThat(produto.getCategoria()).isEqualTo(categoria);
            assertThat(produto.getFornecedor()).isEqualTo(fornecedor);
            then(produtoRepository).should().save(produto);
        }

        @Test
        @DisplayName("deve normalizar o local de armazenamento antes de salvar")
        void deveNormalizarLocalArmazenamento() {
            ProdutoDTO dto = umProdutoDTO();
            Produto produto = umProduto();
            produto.setLocalArmazenamento("  Prateleira A 01  ");

            given(produtoRepository.existsBySku(dto.getSku())).willReturn(false);
            given(categoriaRepository.findById(dto.getCategoriaID())).willReturn(Optional.of(umaCategoria()));
            given(fornecedorRepository.findById(dto.getFornecedorID())).willReturn(Optional.of(umFornecedor()));
            given(modelMapper.map(dto, Produto.class)).willReturn(produto);
            given(produtoRepository.save(produto)).willReturn(produto);
            given(modelMapper.map(produto, ProdutoDTO.class)).willReturn(dto);

            produtoService.adicionarProduto(dto);

            assertThat(produto.getLocalArmazenamento()).isEqualTo("prateleiraa01");
        }

        @Test
        @DisplayName("não deve lançar exceção quando local de armazenamento vier nulo do mapeamento")
        void naoDeveLancarExcecaoQuandoLocalArmazenamentoNulo() {
            ProdutoDTO dto = umProdutoDTO();
            Produto produto = umProduto();
            produto.setLocalArmazenamento(null);

            given(produtoRepository.existsBySku(dto.getSku())).willReturn(false);
            given(categoriaRepository.findById(dto.getCategoriaID())).willReturn(Optional.of(umaCategoria()));
            given(fornecedorRepository.findById(dto.getFornecedorID())).willReturn(Optional.of(umFornecedor()));
            given(modelMapper.map(dto, Produto.class)).willReturn(produto);
            given(produtoRepository.save(produto)).willReturn(produto);
            given(modelMapper.map(produto, ProdutoDTO.class)).willReturn(dto);

            assertThatCode(() -> produtoService.adicionarProduto(dto)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("deve lançar RecursoJaExistenteException quando SKU já cadastrado")
        void deveLancarExcecaoQuandoSkuDuplicado() {
            ProdutoDTO dto = umProdutoDTO();

            given(produtoRepository.existsBySku(dto.getSku())).willReturn(true);

            assertThatThrownBy(() -> produtoService.adicionarProduto(dto))
                    .isInstanceOf(RecursoJaExistenteException.class)
                    .hasMessageContaining(dto.getSku());

            then(categoriaRepository).should(never()).findById(any());
            then(produtoRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando categoria não encontrada")
        void deveLancarExcecaoQuandoCategoriaNaoEncontrada() {
            ProdutoDTO dto = umProdutoDTO();

            given(produtoRepository.existsBySku(dto.getSku())).willReturn(false);
            given(categoriaRepository.findById(dto.getCategoriaID())).willReturn(Optional.empty());

            assertThatThrownBy(() -> produtoService.adicionarProduto(dto))
                    .isInstanceOf(NotFoundException.class);

            then(fornecedorRepository).should(never()).findById(any());
            then(produtoRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando fornecedor não encontrado")
        void deveLancarExcecaoQuandoFornecedorNaoEncontrado() {
            ProdutoDTO dto = umProdutoDTO();

            given(produtoRepository.existsBySku(dto.getSku())).willReturn(false);
            given(categoriaRepository.findById(dto.getCategoriaID())).willReturn(Optional.of(umaCategoria()));
            given(fornecedorRepository.findById(dto.getFornecedorID())).willReturn(Optional.empty());

            assertThatThrownBy(() -> produtoService.adicionarProduto(dto))
                    .isInstanceOf(NotFoundException.class);

            then(produtoRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("atualizarProduto")
    class UpdateProduto {

        @Test
        @DisplayName("deve atualizar produto com sucesso e retornar status 200")
        void deveAtualizarProdutoComSucesso() {
            Produto produtoExistente = umProduto();
            ProdutoDTO dto = new ProdutoDTO();
            dto.setNome("Cadeira Ergonômica");

            given(produtoRepository.findById(1L)).willReturn(Optional.of(produtoExistente));
            given(produtoRepository.save(produtoExistente)).willReturn(produtoExistente);
            given(modelMapper.map(produtoExistente, ProdutoDTO.class)).willReturn(dto);

            Response response = produtoService.atualizarProduto(1L, dto);

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("Produto atualizado com sucesso");
            assertThat(produtoExistente.getNome()).isEqualTo("Cadeira Ergonômica");
            then(produtoRepository).should().save(produtoExistente);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando produto não encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            given(produtoRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> produtoService.atualizarProduto(99L, new ProdutoDTO()))
                    .isInstanceOf(NotFoundException.class);

            then(produtoRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve atualizar categoria quando categoriaID válido é informado")
        void deveAtualizarCategoriaQuandoInformada() {
            Produto produtoExistente = umProduto();
            Categoria novaCategoria = Categoria.builder().id(2L).nome("Ferramentas").build();
            ProdutoDTO dto = new ProdutoDTO();
            dto.setCategoriaID(2L);

            given(produtoRepository.findById(1L)).willReturn(Optional.of(produtoExistente));
            given(categoriaRepository.findById(2L)).willReturn(Optional.of(novaCategoria));
            given(produtoRepository.save(produtoExistente)).willReturn(produtoExistente);
            given(modelMapper.map(produtoExistente, ProdutoDTO.class)).willReturn(dto);

            produtoService.atualizarProduto(1L, dto);

            assertThat(produtoExistente.getCategoria()).isEqualTo(novaCategoria);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando categoria informada não existe")
        void deveLancarExcecaoQuandoCategoriaInformadaNaoExiste() {
            Produto produtoExistente = umProduto();
            ProdutoDTO dto = new ProdutoDTO();
            dto.setCategoriaID(99L);

            given(produtoRepository.findById(1L)).willReturn(Optional.of(produtoExistente));
            given(categoriaRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> produtoService.atualizarProduto(1L, dto))
                    .isInstanceOf(NotFoundException.class);

            then(produtoRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve atualizar fornecedor quando fornecedorID válido é informado")
        void deveAtualizarFornecedorQuandoInformado() {
            Produto produtoExistente = umProduto();
            Fornecedor novoFornecedor = Fornecedor.builder().id(2L).nome("Novo Fornecedor").email("novo@email.com").telefone("(11) 9999-0000").ativo(true).build();
            ProdutoDTO dto = new ProdutoDTO();
            dto.setFornecedorID(2L);

            given(produtoRepository.findById(1L)).willReturn(Optional.of(produtoExistente));
            given(fornecedorRepository.findById(2L)).willReturn(Optional.of(novoFornecedor));
            given(produtoRepository.save(produtoExistente)).willReturn(produtoExistente);
            given(modelMapper.map(produtoExistente, ProdutoDTO.class)).willReturn(dto);

            produtoService.atualizarProduto(1L, dto);

            assertThat(produtoExistente.getFornecedor()).isEqualTo(novoFornecedor);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando fornecedor informado não existe")
        void deveLancarExcecaoQuandoFornecedorInformadoNaoExiste() {
            Produto produtoExistente = umProduto();
            ProdutoDTO dto = new ProdutoDTO();
            dto.setFornecedorID(99L);

            given(produtoRepository.findById(1L)).willReturn(Optional.of(produtoExistente));
            given(fornecedorRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> produtoService.atualizarProduto(1L, dto))
                    .isInstanceOf(NotFoundException.class);

            then(produtoRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("não deve alterar categoria nem fornecedor quando IDs não são informados")
        void naoDeveAlterarCategoriaNemFornecedorQuandoNaoInformados() {
            Produto produtoExistente = umProduto();
            Categoria categoriaOriginal = produtoExistente.getCategoria();
            Fornecedor fornecedorOriginal = produtoExistente.getFornecedor();
            ProdutoDTO dto = new ProdutoDTO();

            given(produtoRepository.findById(1L)).willReturn(Optional.of(produtoExistente));
            given(produtoRepository.save(produtoExistente)).willReturn(produtoExistente);
            given(modelMapper.map(produtoExistente, ProdutoDTO.class)).willReturn(dto);

            produtoService.atualizarProduto(1L, dto);

            assertThat(produtoExistente.getCategoria()).isEqualTo(categoriaOriginal);
            assertThat(produtoExistente.getFornecedor()).isEqualTo(fornecedorOriginal);
            then(categoriaRepository).should(never()).findById(any());
            then(fornecedorRepository).should(never()).findById(any());
        }

        @Test
        @DisplayName("não deve alterar o nome quando nulo ou vazio")
        void naoDeveAlterarNomeQuandoNuloOuVazio() {
            Produto produtoExistente = umProduto();
            ProdutoDTO dto = new ProdutoDTO();
            dto.setNome("  ");

            given(produtoRepository.findById(1L)).willReturn(Optional.of(produtoExistente));
            given(produtoRepository.save(produtoExistente)).willReturn(produtoExistente);
            given(modelMapper.map(produtoExistente, ProdutoDTO.class)).willReturn(dto);

            produtoService.atualizarProduto(1L, dto);

            assertThat(produtoExistente.getNome()).isEqualTo("Cadeira de Escritório");
        }

        @Test
        @DisplayName("deve atualizar a descrição quando informada")
        void deveAtualizarDescricaoQuandoInformada() {
            Produto produtoExistente = umProduto();
            ProdutoDTO dto = new ProdutoDTO();
            dto.setDescricao("Nova descrição");

            given(produtoRepository.findById(1L)).willReturn(Optional.of(produtoExistente));
            given(produtoRepository.save(produtoExistente)).willReturn(produtoExistente);
            given(modelMapper.map(produtoExistente, ProdutoDTO.class)).willReturn(dto);

            produtoService.atualizarProduto(1L, dto);

            assertThat(produtoExistente.getDescricao()).isEqualTo("Nova descrição");
        }

        @Test
        @DisplayName("não deve alterar a descrição quando nula ou vazia")
        void naoDeveAlterarDescricaoQuandoNulaOuVazia() {
            Produto produtoExistente = umProduto();
            produtoExistente.setDescricao("Descrição original");
            ProdutoDTO dto = new ProdutoDTO();
            dto.setDescricao("");

            given(produtoRepository.findById(1L)).willReturn(Optional.of(produtoExistente));
            given(produtoRepository.save(produtoExistente)).willReturn(produtoExistente);
            given(modelMapper.map(produtoExistente, ProdutoDTO.class)).willReturn(dto);

            produtoService.atualizarProduto(1L, dto);

            assertThat(produtoExistente.getDescricao()).isEqualTo("Descrição original");
        }

        @Test
        @DisplayName("deve normalizar e atualizar o local de armazenamento quando disponível")
        void deveAtualizarLocalArmazenamentoQuandoDisponivel() {
            Produto produtoExistente = umProduto();
            ProdutoDTO dto = new ProdutoDTO();
            dto.setLocalArmazenamento("  Prateleira B 02  ");

            given(produtoRepository.findById(1L)).willReturn(Optional.of(produtoExistente));
            given(produtoRepository.existsByLocalArmazenamentoAndIdNot("prateleirab02", 1L)).willReturn(false);
            given(produtoRepository.save(produtoExistente)).willReturn(produtoExistente);
            given(modelMapper.map(produtoExistente, ProdutoDTO.class)).willReturn(dto);

            produtoService.atualizarProduto(1L, dto);

            assertThat(produtoExistente.getLocalArmazenamento()).isEqualTo("prateleirab02");
        }

        @Test
        @DisplayName("deve lançar RecursoJaExistenteException quando novo local já pertence a outro produto")
        void deveLancarExcecaoQuandoLocalArmazenamentoJaExiste() {
            Produto produtoExistente = umProduto();
            ProdutoDTO dto = new ProdutoDTO();
            dto.setLocalArmazenamento("Prateleira B02");

            given(produtoRepository.findById(1L)).willReturn(Optional.of(produtoExistente));
            given(produtoRepository.existsByLocalArmazenamentoAndIdNot("prateleirab02", 1L)).willReturn(true);

            assertThatThrownBy(() -> produtoService.atualizarProduto(1L, dto))
                    .isInstanceOf(RecursoJaExistenteException.class);

            then(produtoRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("deve atualizar a quantidade quando informada e válida")
        void deveAtualizarQuantidadeQuandoValida() {
            Produto produtoExistente = umProduto();
            ProdutoDTO dto = new ProdutoDTO();
            dto.setQuantidade(25);

            given(produtoRepository.findById(1L)).willReturn(Optional.of(produtoExistente));
            given(produtoRepository.save(produtoExistente)).willReturn(produtoExistente);
            given(modelMapper.map(produtoExistente, ProdutoDTO.class)).willReturn(dto);

            produtoService.atualizarProduto(1L, dto);

            assertThat(produtoExistente.getQuantidade()).isEqualTo(25);
        }

        @Test
        @DisplayName("não deve alterar a quantidade quando negativa")
        void naoDeveAlterarQuantidadeQuandoNegativa() {
            Produto produtoExistente = umProduto();
            ProdutoDTO dto = new ProdutoDTO();
            dto.setQuantidade(-5);

            given(produtoRepository.findById(1L)).willReturn(Optional.of(produtoExistente));
            given(produtoRepository.save(produtoExistente)).willReturn(produtoExistente);
            given(modelMapper.map(produtoExistente, ProdutoDTO.class)).willReturn(dto);

            produtoService.atualizarProduto(1L, dto);

            assertThat(produtoExistente.getQuantidade()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("listarProdutos")
    class GetProdutos {

        @Test
        @DisplayName("deve retornar lista de produtos com status 200")
        void deveRetornarListaDeProdutos() {
            List<Produto> produtos = List.of(umProduto());
            List<ProdutoDTO> produtosDTO = List.of(umProdutoDTO());

            given(produtoRepository.findAll(any(org.springframework.data.domain.Sort.class))).willReturn(produtos);
            given(modelMapper.<List<ProdutoDTO>>map(eq(produtos), any(java.lang.reflect.Type.class))).willReturn(produtosDTO);

            Response response = produtoService.listarProdutos();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getProdutos()).hasSize(1);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há produtos cadastrados")
        void deveRetornarListaVaziaQuandoSemProdutos() {
            given(produtoRepository.findAll(any(org.springframework.data.domain.Sort.class))).willReturn(List.of());
            given(modelMapper.<List<ProdutoDTO>>map(any(), any(java.lang.reflect.Type.class))).willReturn(List.of());

            Response response = produtoService.listarProdutos();

            assertThat(response.getProdutos()).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarProdutoPorId")
    class GetProdutoById {

        @Test
        @DisplayName("deve retornar produto com sucesso quando id existe")
        void deveRetornarProdutoComSucesso() {
            Produto produto = umProduto();
            ProdutoDTO dto = umProdutoDTO();

            given(produtoRepository.findById(1L)).willReturn(Optional.of(produto));
            given(modelMapper.map(produto, ProdutoDTO.class)).willReturn(dto);

            Response response = produtoService.buscarProdutoPorId(1L);

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getProduto()).isEqualTo(dto);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando produto não encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            given(produtoRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> produtoService.buscarProdutoPorId(99L))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("removerProduto")
    class DeleteProduto {

        @Test
        @DisplayName("deve deletar produto com sucesso e retornar status 204")
        void deveDeletarProdutoComSucesso() {
            Produto produto = umProduto();

            given(produtoRepository.findById(1L)).willReturn(Optional.of(produto));

            Response response = produtoService.removerProduto(1L);

            assertThat(response.getStatus()).isEqualTo(204);
            then(produtoRepository).should().deleteById(1L);
        }

        @Test
        @DisplayName("deve lançar NotFoundException quando produto não encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            given(produtoRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> produtoService.removerProduto(99L))
                    .isInstanceOf(NotFoundException.class);

            then(produtoRepository).should(never()).deleteById(any());
        }
    }
}
