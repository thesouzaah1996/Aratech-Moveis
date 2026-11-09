package com.aratech.moveis.operacional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.aratech.moveis.administracao.gestao.dto.ValorParametro;
import com.aratech.moveis.administracao.gestao.service.AdministracaoService;
import com.aratech.moveis.almoxarifado.categorias.model.Categoria;
import com.aratech.moveis.almoxarifado.categorias.repository.CategoriaRepository;
import com.aratech.moveis.almoxarifado.fornecedores.model.Fornecedor;
import com.aratech.moveis.almoxarifado.fornecedores.model.Representante;
import com.aratech.moveis.almoxarifado.fornecedores.repository.FornecedorRepository;
import com.aratech.moveis.assistencia.lotes.model.LoteAssistencia;
import com.aratech.moveis.assistencia.lotes.service.LoteAssistenciaService;
import com.aratech.moveis.carregamento.expedicao.service.ExpedicaoService;
import com.aratech.moveis.comercial.pedidos.dto.NovaVenda;
import com.aratech.moveis.comercial.pedidos.service.ComercialService;
import com.aratech.moveis.compras.cotacoes.dto.NovaCotacao;
import com.aratech.moveis.compras.cotacoes.service.CotacaoService;
import com.aratech.moveis.financeiro.titulos.dto.Documento;
import com.aratech.moveis.financeiro.titulos.dto.NovoTitulo;
import com.aratech.moveis.financeiro.titulos.service.FinanceiroService;
import com.aratech.moveis.pcp.producao.dto.NovaLinha;
import com.aratech.moveis.pcp.producao.dto.NovaOrdem;
import com.aratech.moveis.pcp.producao.service.ProducaoService;
import com.aratech.moveis.pcp.requisicoes.dto.NovaRequisicao;
import com.aratech.moveis.pcp.requisicoes.service.RequisicaoService;
import com.aratech.moveis.portaria.controleacesso.service.ControleAcessoService;
import com.aratech.moveis.rh.afastamentos.dto.NovoAfastamento;
import com.aratech.moveis.rh.afastamentos.service.AfastamentoService;
import com.aratech.moveis.rh.solicitacoes.service.SolicitacaoRhService;
import com.aratech.moveis.security.authorization.SessaoAuthorities;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.security.service.ContasService;
import com.aratech.moveis.shared.dto.Comando;

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
class OperacaoFabricaIT {
    @Autowired ProducaoService producao;
    @Autowired ComercialService comercial;
    @Autowired ExpedicaoService expedicao;
    @Autowired FinanceiroService financeiro;
    @Autowired AfastamentoService afastamentos;
    @Autowired SolicitacaoRhService solicitacoes;
    @Autowired CotacaoService cotacoes;
    @Autowired AdministracaoService administracao;
    @Autowired JdbcTemplate jdbc;
    @Autowired RequisicaoService requisicoes;
    @Autowired LoteAssistenciaService assistencia;
    @Autowired CategoriaRepository categorias;
    @Autowired FornecedorRepository fornecedores;
    @Autowired ContasService contas;
    @Autowired MockMvc mvc;
    @Autowired JsonMapper mapper;
    @Autowired ControleAcessoService portaria;
    long usuario;
    String sufixo;
    LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));

    @BeforeEach
    void preparar() {
        sufixo = UUID.randomUUID().toString();
        usuario =
                jdbc.queryForObject(
                        "INSERT INTO funcionario(nome_completo,email,setor,ativo) VALUES ('Operador"
                                + " teste',?,'PCP',TRUE) RETURNING id",
                        Long.class,
                        sufixo + "@example.test");
        jdbc.update(
                "INSERT INTO acesso_conta(funcionario_id,senha_hash,administrador) VALUES"
                        + " (?,'hash-exclusivo-teste',TRUE)",
                usuario);
        autenticar();
    }

    private void autenticar() {
        var u = contas.carregar(usuario, Instant.now());
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        u, null, SessaoAuthorities.authorities(u)));
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    private Comando comando(long versao) {
        return new Comando(
                UUID.randomUUID(), versao, "Conferência operacional de teste", hoje, null, null);
    }

    private void conflito(Runnable operacao) {
        assertEquals(409, assertThrows(AuthException.class, operacao::run).status());
    }

    @Test
    void recebimentoAssistenciaPreservaEstadoDoServidorERejeitaReferenciaDivergente() {
        var entrada = new LoteAssistencia();
        entrada.setOrigemId(UUID.randomUUID().getMostSignificantBits() >>> 1);
        entrada.setCodigo("RET-" + sufixo);
        entrada.setProduto("Cadeira");
        entrada.setQuantidade(3);
        entrada.setStatus("Finalizado");
        entrada.setDefeito("Reparo de acabamento");
        var lote = assistencia.receber(entrada);
        assertEquals("Não iniciado", lote.getStatus());
        assertEquals(lote.getId(), assistencia.receber(entrada).getId());
        entrada.setQuantidade(5);
        assertEquals(
                409,
                assertThrows(
                                org.springframework.web.server.ResponseStatusException.class,
                                () -> assistencia.receber(entrada))
                        .getStatusCode()
                        .value());
    }

    @Test
    void encomendaConfirmadaGeraUmaOrdemEUmTituloECancelamentoSincroniza() {
        var venda =
                comercial.venda(
                        null,
                        new NovaVenda(
                                "Cliente teste",
                                "Mesa",
                                2,
                                new BigDecimal("250.00"),
                                hoje.plusDays(4),
                                hoje.plusDays(10),
                                UUID.randomUUID(),
                                0));
        var dto = comando(venda.versao());
        var confirmada = comercial.vendaAcao(venda.id(), "confirmar", dto);
        assertEquals(confirmada, comercial.vendaAcao(venda.id(), "confirmar", dto));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT count(*) FROM pcp_ordem WHERE pedido_id=?",
                        Integer.class,
                        venda.id()));
        assertEquals(
                new BigDecimal("500.00"),
                jdbc.queryForObject(
                        "SELECT valor FROM financeiro_titulo WHERE origem='VENDA' AND origem_id=?",
                        BigDecimal.class,
                        venda.id()));
        comercial.vendaAcao(venda.id(), "cancelar", comando(confirmada.versao()));
        assertEquals(
                "CANCELADA",
                jdbc.queryForObject(
                        "SELECT status FROM pcp_ordem WHERE pedido_id=?",
                        String.class,
                        venda.id()));
        assertEquals(
                "CANCELADO",
                jdbc.queryForObject(
                        "SELECT status FROM financeiro_titulo WHERE origem='VENDA' AND origem_id=?",
                        String.class,
                        venda.id()));
    }

    @Test
    void capacidadeImpedeSuperlotacaoEProducaoConcluidaExigeCaminhaoPresente() {
        var linha =
                producao.linha(
                        null,
                        new NovaLinha(
                                "Linha " + sufixo, new BigDecimal("8"), UUID.randomUUID(), 0));
        var ordem =
                producao.salvar(
                        null,
                        new NovaOrdem(
                                "Cadeira",
                                10,
                                hoje,
                                hoje,
                                "Operador",
                                linha.id(),
                                new BigDecimal("8"),
                                UUID.randomUUID(),
                                0));
        conflito(
                () ->
                        producao.salvar(
                                null,
                                new NovaOrdem(
                                        "Mesa",
                                        2,
                                        hoje,
                                        hoje,
                                        "Operador",
                                        linha.id(),
                                        BigDecimal.ONE,
                                        UUID.randomUUID(),
                                        0)));
        var iniciada = producao.acao(ordem.id(), "iniciar", comando(ordem.versao()));
        var pronta = producao.acao(ordem.id(), "concluir", comando(iniciada.versao()));
        assertEquals("FINALIZADA", pronta.status());
        assertNotNull(pronta.concluidoEm());
        Long chegada =
                jdbc.queryForObject(
                        "INSERT INTO"
                            + " registro_chegada(nota_fiscal,empresa,nome_motorista,placa,descricao_carga,setor_responsavel,status,chave_operacao,data_chegada,tipo_operacao)"
                            + " VALUES ('123','Transportadora','Motorista','ABC1D23','Carga"
                            + " teste','CARREGAMENTO','AGUARDANDO',?,CURRENT_TIMESTAMP,'CARREGAR')"
                            + " RETURNING id",
                        Long.class,
                        UUID.randomUUID());
        conflito(
                () ->
                        expedicao.expedir(
                                pronta.id(),
                                new Comando(
                                        UUID.randomUUID(),
                                        pronta.versao(),
                                        "Carga conferida",
                                        hoje,
                                        chegada,
                                        null)));
        portaria.autorizarSetor("CARREGAMENTO", chegada);
        portaria.confirmarEntrada(chegada);
        var dto =
                new Comando(
                        UUID.randomUUID(), pronta.versao(), "Carga conferida", hoje, chegada, null);
        var expedida = expedicao.expedir(pronta.id(), dto);
        assertNotNull(expedida.expedidoEm());
        assertEquals(expedida, expedicao.expedir(pronta.id(), dto));
        assertEquals(
                "EM_RECEBIMENTO",
                jdbc.queryForObject(
                        "SELECT status FROM registro_chegada WHERE id=?", String.class, chegada));
    }

    @Test
    void baixaIntegralNaoDuplicaCaixaNemAceitaDataFutura() {
        var titulo =
                financeiro.salvar(
                        "receber",
                        null,
                        new NovoTitulo(
                                "Venda teste",
                                "Cliente",
                                new BigDecimal("123.45"),
                                hoje,
                                null,
                                null,
                                null,
                                UUID.randomUUID(),
                                0));
        var dto = comando(titulo.versao());
        var baixado = financeiro.acao("receber", titulo.id(), "baixar", dto);
        assertEquals(baixado, financeiro.acao("receber", titulo.id(), "baixar", dto));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT count(*) FROM gestao_auditoria WHERE entidade='TITULO' AND"
                                + " registro_id=? AND acao='BAIXAR'",
                        Integer.class,
                        titulo.id()));
        assertTrue(
                financeiro.caixa(YearMonth.from(hoje).toString(), 0, 100, "").itens().stream()
                        .anyMatch(c -> c.id().equals(titulo.id())));
        conflito(
                () ->
                        financeiro.acao(
                                "receber", titulo.id(), "cancelar", comando(baixado.versao())));
        var outro =
                financeiro.salvar(
                        "pagar",
                        null,
                        new NovoTitulo(
                                "Despesa teste",
                                "Fornecedor",
                                BigDecimal.TEN,
                                hoje,
                                null,
                                null,
                                null,
                                UUID.randomUUID(),
                                0));
        assertEquals(
                400,
                assertThrows(
                                AuthException.class,
                                () ->
                                        financeiro.acao(
                                                "pagar",
                                                outro.id(),
                                                "baixar",
                                                new Comando(
                                                        UUID.randomUUID(),
                                                        outro.versao(),
                                                        "Pagamento teste",
                                                        hoje.plusDays(1),
                                                        null,
                                                        null)))
                        .status());
    }

    @Test
    void documentoDeCompraVinculaAoTituloSemDuplicarObrigacao() {
        long origem =
                jdbc.queryForObject(
                        "INSERT INTO compra_pedido(origem,tipo,item,quantidade,unidade,status)"
                            + " VALUES ('COMPRAS','MANUAL','MDF',2,'un','Recebido') RETURNING id",
                        Long.class);
        financeiro.integrar(
                "COMPRA",
                origem,
                "PAGAR",
                "Compra de MDF",
                "Fornecedor",
                new BigDecimal("100"),
                hoje.plusDays(10));
        var titulo =
                financeiro.listar("pagar", 0, 100, "Compra de MDF").itens().stream()
                        .filter(t -> Objects.equals(t.origemId(), origem))
                        .findFirst()
                        .orElseThrow();
        var dto = new Documento("NF-" + sufixo, hoje, UUID.randomUUID(), titulo.versao());
        var documentado = financeiro.documentar("pagar", titulo.id(), dto);
        assertEquals(documentado, financeiro.documentar("pagar", titulo.id(), dto));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT count(*) FROM financeiro_titulo WHERE origem='COMPRA' AND"
                                + " origem_id=?",
                        Integer.class,
                        origem));
        assertTrue(
                financeiro.listar("notas", 0, 100, sufixo).itens().stream()
                        .anyMatch(t -> t.id().equals(titulo.id())));
    }

    @Test
    void afastamentosValidadosContraFuncionarioEPeriodoEParametrosTêmEfeito() {
        var p =
                afastamentos.criarAfastamento(
                        new NovoAfastamento(
                                usuario,
                                "FERIAS",
                                hoje.plusDays(2),
                                hoje.plusDays(8),
                                "Férias programadas",
                                UUID.randomUUID(),
                                0));
        conflito(
                () ->
                        afastamentos.criarAfastamento(
                                new NovoAfastamento(
                                        usuario,
                                        "LICENCA",
                                        hoje.plusDays(3),
                                        hoje.plusDays(5),
                                        "Licença solicitada",
                                        UUID.randomUUID(),
                                        0)));
        assertEquals(
                "APROVADO",
                afastamentos.afastamentoAcao(p.id(), "aprovar", comando(p.versao())).status());
        var parametro =
                administracao.parametros().stream()
                        .filter(x -> x.chave().equals("pcp.antecedencia_dias"))
                        .findFirst()
                        .orElseThrow();
        assertEquals(
                7,
                administracao
                        .parametro(
                                parametro.chave(),
                                new ValorParametro(7, UUID.randomUUID(), parametro.versao()))
                        .valor());
        assertFalse(administracao.relatorio().isEmpty());
    }

    @Test
    void cotacaoEscolhidaAtualizaPedidoSemAprovarOuCriarDuplicata() {
        long pedido =
                jdbc.queryForObject(
                        "INSERT INTO compra_pedido(origem,tipo,item,quantidade,unidade,status)"
                                + " VALUES ('COMPRAS','MANUAL','Dobradiça',10,'un','Aguardando"
                                + " Aprovação') RETURNING id",
                        Long.class);
        var c =
                cotacoes.criar(
                        new NovaCotacao(
                                pedido,
                                "Fornecedor A",
                                new BigDecimal("12"),
                                "Fornecedor B",
                                new BigDecimal("10"),
                                null,
                                null,
                                UUID.randomUUID(),
                                0));
        var dto =
                new Comando(UUID.randomUUID(), c.versao(), "Proposta mais adequada", null, null, 2);
        var escolhida = cotacoes.acao(c.id(), "escolher", dto);
        assertEquals(escolhida, cotacoes.acao(c.id(), "escolher", dto));
        assertEquals(
                "Fornecedor B",
                jdbc.queryForObject(
                        "SELECT fornecedor FROM compra_pedido WHERE id=?", String.class, pedido));
        assertEquals(
                "Aguardando Aprovação",
                jdbc.queryForObject(
                        "SELECT status FROM compra_pedido WHERE id=?", String.class, pedido));
    }

    @Test
    void requisicaoBaixaSaldoUmaVezEDevolucaoIntegralPermiteCancelar() {
        var categoria = categorias.saveAndFlush(Categoria.builder().nome("Cat " + sufixo).build());
        var fornecedor =
                fornecedores.saveAndFlush(
                        Fornecedor.builder()
                                .nome("Fornecedor " + sufixo)
                                .email(sufixo + "@fornecedor.test")
                                .telefone("1133334444")
                                .representante(
                                        new Representante(
                                                "Representante",
                                                "1133334444",
                                                sufixo + "@representante.test"))
                                .build());
        long produto =
                jdbc.queryForObject(
                        "INSERT INTO"
                            + " produto(nome,sku,quantidade,categoria_id,fornecedor_id,local_armazenamento,criado_em,ativo)"
                            + " VALUES ('Chapa teste',?,10,?,?,'Prateleira"
                            + " A1',CURRENT_TIMESTAMP,TRUE) RETURNING id",
                        Long.class,
                        "T-" + sufixo.toUpperCase(Locale.ROOT),
                        categoria.getId(),
                        fornecedor.getId());
        var linha =
                producao.linha(
                        null, new NovaLinha("Req " + sufixo, BigDecimal.TEN, UUID.randomUUID(), 0));
        var ordem =
                producao.salvar(
                        null,
                        new NovaOrdem(
                                "Mesa",
                                1,
                                hoje,
                                hoje,
                                "Operador",
                                linha.id(),
                                BigDecimal.ONE,
                                UUID.randomUUID(),
                                0));
        var req =
                requisicoes.criar(
                        new NovaRequisicao(
                                ordem.id(), produto, 3, "Produção de mesa", UUID.randomUUID(), 0));
        conflito(() -> producao.acao(ordem.id(), "iniciar", comando(ordem.versao())));
        var dto = comando(req.versao());
        var atendida = requisicoes.acao(req.id(), "atender", dto);
        assertEquals(atendida, requisicoes.acao(req.id(), "atender", dto));
        assertEquals(
                7,
                jdbc.queryForObject(
                        "SELECT quantidade FROM produto WHERE id=?", Integer.class, produto));
        conflito(() -> producao.acao(ordem.id(), "cancelar", comando(ordem.versao())));
        requisicoes.acao(req.id(), "devolver", comando(atendida.versao()));
        assertEquals(
                10,
                jdbc.queryForObject(
                        "SELECT quantidade FROM produto WHERE id=?", Integer.class, produto));
        assertEquals(
                "CANCELADA",
                producao.acao(ordem.id(), "cancelar", comando(ordem.versao())).status());
    }

    @Test
    void permissaoDetalhadaNaoLiberaOutrasTelasOuEscrita() throws Exception {
        jdbc.update("UPDATE acesso_conta SET administrador=FALSE WHERE funcionario_id=?", usuario);
        long perfil =
                jdbc.queryForObject(
                        "INSERT INTO perfil_acesso(nome) VALUES (?) RETURNING id",
                        Long.class,
                        "PCP-" + sufixo);
        jdbc.update(
                "INSERT INTO funcionario_perfil_acesso(funcionario_id,perfil_id) VALUES (?,?)",
                usuario,
                perfil);
        jdbc.update(
                "INSERT INTO perfil_permissao_acesso(perfil_id,permissao) VALUES"
                        + " (?,'PCP_ORDENS.CONSULTAR')",
                perfil);
        autenticar();
        var sessao = new MockHttpSession();
        sessao.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
        mvc.perform(get("/aratech/pcp/ordens").session(sessao)).andExpect(status().isOk());
        mvc.perform(get("/aratech/pcp/indicadores").session(sessao))
                .andExpect(status().isForbidden());
        mvc.perform(get("/aratech/financeiro/titulos/pagar").session(sessao))
                .andExpect(status().isForbidden());
        var token = mvc.perform(get("/aratech/auth/csrf").session(sessao)).andReturn();
        String csrf =
                mapper.readTree(token.getResponse().getContentAsString()).path("token").asText();
        mvc.perform(
                        post("/aratech/pcp/ordens")
                                .session(sessao)
                                .header("X-XSRF-TOKEN", csrf)
                                .contentType("application/json")
                                .content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/aratech/pcp/ordens/linhas").session(sessao)).andExpect(status().isOk());
    }
}
