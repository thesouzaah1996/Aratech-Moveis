package com.aratech.moveis.compras;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.aratech.moveis.almoxarifado.categorias.model.Categoria;
import com.aratech.moveis.almoxarifado.categorias.repository.CategoriaRepository;
import com.aratech.moveis.almoxarifado.fornecedores.model.Fornecedor;
import com.aratech.moveis.almoxarifado.fornecedores.model.Representante;
import com.aratech.moveis.almoxarifado.fornecedores.repository.FornecedorRepository;
import com.aratech.moveis.almoxarifado.produtos.dto.MovimentacaoDTO;
import com.aratech.moveis.almoxarifado.produtos.service.ProdutoService;
import com.aratech.moveis.compras.pedidos.dto.Decisao;
import com.aratech.moveis.compras.pedidos.dto.Pedido;
import com.aratech.moveis.compras.pedidos.service.PedidoCompraService;
import com.aratech.moveis.manutencao.estoque.dto.MovimentacaoPecaDTO;
import com.aratech.moveis.manutencao.estoque.service.PecaEstoqueService;
import com.aratech.moveis.manutencao.pecas.dto.SolicitacaoPecaDTO;
import com.aratech.moveis.manutencao.pecas.service.SolicitacaoPecaService;
import com.aratech.moveis.security.authorization.SessaoAuthorities;
import com.aratech.moveis.security.dto.UsuarioSessao;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.security.service.ContasService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class PedidoCompraIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired PedidoCompraService compras;
    @Autowired ProdutoService produtos;
    @Autowired PecaEstoqueService pecas;
    @Autowired SolicitacaoPecaService solicitacoes;
    @Autowired CategoriaRepository categorias;
    @Autowired FornecedorRepository fornecedores;
    @Autowired ContasService contas;
    @Autowired MockMvc mvc;
    @Autowired JsonMapper mapper;
    Long usuario, produto, peca;

    @BeforeEach
    void preparar() {
        String sufixo = UUID.randomUUID().toString();
        usuario =
                jdbc.queryForObject(
                        "INSERT INTO funcionario(nome_completo,email,ativo) VALUES ('Comprador de"
                                + " teste',?,TRUE) RETURNING id",
                        Long.class,
                        sufixo + "@example.test");
        jdbc.update(
                "INSERT INTO acesso_conta(funcionario_id,senha_hash,administrador) VALUES"
                        + " (?,'hash-ficticio-de-teste',TRUE)",
                usuario);
        var categoria =
                categorias.saveAndFlush(Categoria.builder().nome("Categoria " + sufixo).build());
        var fornecedor =
                fornecedores.saveAndFlush(
                        Fornecedor.builder()
                                .nome("Fornecedor " + sufixo)
                                .email(sufixo + "@fornecedor.test")
                                .telefone("1133334444")
                                .representante(
                                        new Representante(
                                                "Representante Teste",
                                                "1133334444",
                                                sufixo + "@representante.test"))
                                .build());
        produto =
                jdbc.queryForObject(
                        "INSERT INTO"
                            + " produto(nome,sku,quantidade,categoria_id,fornecedor_id,local_armazenamento,criado_em,ativo)"
                            + " VALUES ('Parafuso teste',?,10,?,?,'Prateleira"
                            + " teste',CURRENT_TIMESTAMP,TRUE) RETURNING id",
                        Long.class,
                        "P-" + sufixo.toUpperCase(Locale.ROOT),
                        categoria.getId(),
                        fornecedor.getId());
        peca =
                jdbc.queryForObject(
                        "INSERT INTO"
                            + " manutencao_peca_estoque(nome,codigo,quantidade,unidade,localizacao,descricao,ativo,criado_em)"
                            + " VALUES ('Peça teste',?,3,'kg','Oficina teste','Descrição"
                            + " teste',TRUE,CURRENT_TIMESTAMP) RETURNING id",
                        Long.class,
                        "M-" + sufixo.toUpperCase(Locale.ROOT));
    }

    private UsuarioSessao autor() {
        return contas.carregar(usuario, Instant.now());
    }

    private List<Pedido> pedidos(String origem, Long id) {
        return compras.listar(origem).stream()
                .filter(p -> Objects.equals(p.estoqueId(), id))
                .toList();
    }

    private MovimentacaoDTO movimento(int qtd) {
        return MovimentacaoDTO.builder()
                .quantidade(BigDecimal.valueOf(qtd))
                .motivo("Movimentação de teste")
                .chaveOperacao(UUID.randomUUID())
                .build();
    }

    private MockHttpSession sessao() {
        var u = autor();
        var contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        u, null, SessaoAuthorities.authorities(u)));
        var session = new MockHttpSession();
        session.setAttribute("SPRING_SECURITY_CONTEXT", contexto);
        return session;
    }

    private String csrf(MockHttpSession session) throws Exception {
        return mapper.readTree(
                        mvc.perform(get("/aratech/auth/csrf").session(session))
                                .andReturn()
                                .getResponse()
                                .getContentAsString())
                .get("token")
                .asText();
    }

    @Test
    void estoqueMinimoGeraUmaCompraAtualizaSaldoEReprovacaoNaoDuplica() {
        compras.minimo("ALMOXARIFADO", produto, new BigDecimal("5"));
        assertTrue(pedidos("ALMOXARIFADO", produto).isEmpty());
        produtos.saida(produto, movimento(5));
        var p = pedidos("ALMOXARIFADO", produto).getFirst();
        assertEquals("Aguardando Aprovação", p.status());
        assertEquals(0, p.quantidade().compareTo(BigDecimal.ONE));
        produtos.saida(produto, movimento(2));
        assertEquals(1, pedidos("ALMOXARIFADO", produto).size());
        p = pedidos("ALMOXARIFADO", produto).getFirst();
        assertEquals(0, p.quantidade().compareTo(new BigDecimal("3")));
        compras.decidir(
                p.id(),
                "reprovar",
                new Decisao("Fornecedor indisponível para esta compra", p.versao()),
                autor());
        produtos.saida(produto, movimento(1));
        compras.minimo("ALMOXARIFADO", produto, new BigDecimal("5"));
        assertEquals(1, pedidos("ALMOXARIFADO", produto).size());
        assertEquals("Reprovado", pedidos("ALMOXARIFADO", produto).getFirst().status());
        assertTrue(
                pedidos("ALMOXARIFADO", produto)
                        .getFirst()
                        .motivo()
                        .contains("Fornecedor indisponível"));
        produtos.entrada(produto, movimento(10));
        produtos.saida(produto, movimento(7));
        assertEquals(2, pedidos("ALMOXARIFADO", produto).size());
        assertEquals("Aguardando Aprovação", pedidos("ALMOXARIFADO", produto).getFirst().status());
    }

    @Test
    void manutencaoAceitaMinimoDecimalEReposicaoAcimaDoMinimoCancelaPendente() {
        compras.minimo("MANUTENCAO", peca, new BigDecimal("2.500"));
        pecas.saida(
                peca,
                MovimentacaoPecaDTO.builder()
                        .quantidade(new BigDecimal("0.500"))
                        .motivo("Consumo da oficina")
                        .chaveOperacao(UUID.randomUUID())
                        .build());
        var p = pedidos("MANUTENCAO", peca).getFirst();
        assertEquals(0, p.quantidade().compareTo(BigDecimal.ONE));
        pecas.entrada(
                peca,
                MovimentacaoPecaDTO.builder()
                        .quantidade(BigDecimal.ONE)
                        .motivo("Peça recebida na oficina")
                        .chaveOperacao(UUID.randomUUID())
                        .build());
        assertEquals("Cancelado", pedidos("MANUTENCAO", peca).getFirst().status());
        assertThrows(
                AuthException.class,
                () -> compras.minimo("MANUTENCAO", peca, new BigDecimal("0.0001")));
        assertThrows(
                AuthException.class,
                () -> compras.minimo("ALMOXARIFADO", produto, new BigDecimal("1.5")));
        assertThrows(
                AuthException.class,
                () -> compras.minimo("ALMOXARIFADO", produto, new BigDecimal("-1")));
    }

    @Test
    void solicitacaoDePecaVaiDiretoParaComprasEReenvioNaoDuplica() {
        var dto =
                new SolicitacaoPecaDTO(
                        "Rolamento teste",
                        "SKF",
                        new BigDecimal("2.5"),
                        "kg",
                        "Prensa teste",
                        "ESTOQUE",
                        "ALTA",
                        "Solicitante teste",
                        "Produção",
                        "",
                        "Reposição para equipamento de teste",
                        UUID.randomUUID());
        Long id = solicitacoes.solicitar(dto).getSolicitacao().id();
        solicitacoes.solicitar(dto);
        var ps =
                compras.listar("MANUTENCAO").stream()
                        .filter(p -> Objects.equals(p.solicitacaoId(), id))
                        .toList();
        assertEquals(1, ps.size());
        assertEquals("SOLICITACAO_PECA", ps.getFirst().tipo());
        assertEquals("Aguardando Aprovação", ps.getFirst().status());
        assertEquals(0, ps.getFirst().quantidade().compareTo(new BigDecimal("2.5")));
    }

    @Test
    void decisaoExigeMotivoVersaoEPermissaoENaoCreditaEstoque() throws Exception {
        compras.minimo("ALMOXARIFADO", produto, BigDecimal.TEN);
        var p = pedidos("ALMOXARIFADO", produto).getFirst();
        var session = sessao();
        String uri = "/aratech/compras/pedidos/" + p.id();
        mvc.perform(
                        post(uri + "/reprovar")
                                .session(session)
                                .header("X-XSRF-TOKEN", csrf(session))
                                .contentType("application/json")
                                .content(
                                        mapper.writeValueAsString(
                                                Map.of("motivo", " ", "versao", p.versao()))))
                .andExpect(status().isBadRequest());
        jdbc.update(
                "UPDATE compra_pedido SET"
                    + " valor_unitario=10,data_entrega=CURRENT_DATE,data_vencimento=CURRENT_DATE+15"
                    + " WHERE id=?",
                p.id());
        var aprovado = compras.decidir(p.id(), "aprovar", new Decisao(null, p.versao()), autor());
        assertThrows(
                AuthException.class,
                () ->
                        compras.decidir(
                                p.id(),
                                "reprovar",
                                new Decisao("Decisão antiga", p.versao()),
                                autor()));
        compras.decidir(p.id(), "receber", new Decisao(null, aprovado.versao()), autor());
        assertEquals(
                10,
                jdbc.queryForObject(
                        "SELECT quantidade FROM produto WHERE id=?", Integer.class, produto));
        assertEquals("Recebido", pedidos("ALMOXARIFADO", produto).getFirst().status());
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT count(*) FROM financeiro_titulo WHERE origem='COMPRA' AND"
                                + " origem_id=? AND vencimento=CURRENT_DATE+15",
                        Integer.class,
                        p.id()));
        jdbc.update("UPDATE acesso_conta SET administrador=FALSE WHERE funcionario_id=?", usuario);
        mvc.perform(
                        post(uri + "/aprovar")
                                .session(session)
                                .header("X-XSRF-TOKEN", csrf(session))
                                .contentType("application/json")
                                .content("{\"versao\":0}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void origemConsultaMesmoMotivoSemAcessoGlobalAsCompras() throws Exception {
        compras.minimo("ALMOXARIFADO", produto, BigDecimal.TEN);
        compras.minimo("MANUTENCAO", peca, new BigDecimal("3"));
        var p = pedidos("ALMOXARIFADO", produto).getFirst();
        compras.decidir(
                p.id(),
                "reprovar",
                new Decisao("Compra precisa de nova justificativa", p.versao()),
                autor());
        jdbc.update("UPDATE acesso_conta SET administrador=FALSE WHERE funcionario_id=?", usuario);
        Long perfil =
                jdbc.queryForObject(
                        "INSERT INTO perfil_acesso(nome) VALUES (?) RETURNING id",
                        Long.class,
                        "ALMOX-" + UUID.randomUUID());
        jdbc.update(
                "INSERT INTO funcionario_perfil_acesso(funcionario_id,perfil_id) VALUES (?,?)",
                usuario,
                perfil);
        jdbc.update(
                "INSERT INTO perfil_permissao_acesso(perfil_id,permissao) VALUES"
                        + " (?,'ALMOXARIFADO.CONSULTAR')",
                perfil);
        var session = sessao();
        mvc.perform(
                        get("/aratech/almoxarifado/pedidos")
                                .session(session)
                                .param("origem", "MANUTENCAO"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$[*].origem",
                                org.hamcrest.Matchers.everyItem(
                                        org.hamcrest.Matchers.is("ALMOXARIFADO"))))
                .andExpect(
                        content()
                                .string(
                                        org.hamcrest.Matchers.containsString(
                                                "Compra precisa de nova justificativa")));
        mvc.perform(get("/aratech/compras/pedidos").session(session))
                .andExpect(status().isForbidden());
        mvc.perform(get("/aratech/manutencao/pedidos").session(session))
                .andExpect(status().isForbidden());
    }
}
