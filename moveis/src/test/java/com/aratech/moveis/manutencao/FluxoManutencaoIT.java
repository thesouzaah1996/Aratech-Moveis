package com.aratech.moveis.manutencao;

import static org.junit.jupiter.api.Assertions.*;

import com.aratech.moveis.manutencao.chamados.dto.ChamadoDTO;
import com.aratech.moveis.manutencao.chamados.enums.OrigemChamado;
import com.aratech.moveis.manutencao.chamados.enums.StatusChamado;
import com.aratech.moveis.manutencao.chamados.service.ChamadoService;
import com.aratech.moveis.manutencao.fila.dto.AtribuicaoDTO;
import com.aratech.moveis.manutencao.fila.service.FilaChamadosService;
import com.aratech.moveis.manutencao.mecanicos.dto.MecanicoDTO;
import com.aratech.moveis.manutencao.mecanicos.service.MecanicoService;
import com.aratech.moveis.manutencao.pecas.dto.SolicitacaoPecaDTO;
import com.aratech.moveis.manutencao.pecas.service.SolicitacaoPecaService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
@Transactional
class FluxoManutencaoIT {
    @Autowired ChamadoService manutencao;
    @Autowired SolicitacaoPecaService pecas;
    @Autowired FilaChamadosService fila;
    @Autowired MecanicoService mecanicos;

    @Test
    void cadastroFilaAtribuicaoEConclusaoPersistemNaMesmaTransacao() {
        String marca = "IT-" + UUID.randomUUID();
        var m =
                mecanicos
                        .salvar(
                                null,
                                new MecanicoDTO("Mecanico " + marca, List.of("PRENSA"), "MANHA"))
                        .getMecanico();
        var a =
                manutencao
                        .solicitar(
                                new ChamadoDTO(
                                        marca + " manutencao",
                                        "CORRETIVA",
                                        "ALTA",
                                        "Ana Silva",
                                        "Producao",
                                        "",
                                        "Motor parado",
                                        UUID.randomUUID()))
                        .getChamado();
        var dto =
                new SolicitacaoPecaDTO(
                        "Rolamento " + marca,
                        "SKF",
                        new BigDecimal("1.250"),
                        "kg",
                        marca + " peca",
                        "MELHORIA",
                        "ALTA",
                        "Ana Silva",
                        "Producao",
                        "",
                        "Trocar rolamento",
                        UUID.randomUUID());
        var b = pecas.solicitar(dto).getSolicitacao();
        assertNotEquals(a.id(), b.id());
        assertEquals(b.id(), pecas.solicitar(dto).getSolicitacao().id());
        var lista = fila.listar(0, 8, null, marca);
        assertEquals(2L, lista.getTotal());
        assertEquals(
                Set.of(OrigemChamado.MANUTENCAO, OrigemChamado.PECA),
                new HashSet<>(lista.getItens().stream().map(i -> i.origem()).toList()));
        for (Long id : List.of(a.id(), b.id())) {
            var atribuido = fila.atribuir(id, new AtribuicaoDTO(m.id())).getItem();
            assertEquals(StatusChamado.EM_MANUTENCAO, atribuido.status());
            assertEquals(m.id(), atribuido.mecanico().id());
            assertEquals(
                    atribuido.dataInicio(),
                    fila.atribuir(id, new AtribuicaoDTO(m.id())).getItem().dataInicio());
            var concluido = fila.concluir(id).getItem();
            assertEquals(StatusChamado.CONCLUIDA, concluido.status());
            assertFalse(concluido.dataConclusao().isBefore(concluido.dataInicio()));
            assertEquals(concluido.dataConclusao(), fila.concluir(id).getItem().dataConclusao());
        }
        assertEquals(StatusChamado.CONCLUIDA, manutencao.buscar(a.id()).getChamado().status());
        assertEquals(StatusChamado.CONCLUIDA, pecas.buscar(b.id()).getSolicitacao().status());
        assertEquals(0L, fila.listar(0, 8, "ABERTA", marca).getTotal());
        assertEquals(2L, fila.listar(0, 1, "CONCLUIDA", marca).getTotal());
        assertEquals(1, fila.listar(1, 1, "CONCLUIDA", marca).getItens().size());
    }

    @Test
    void prioridadeEPaginacaoSaoGlobais() {
        String marca = "IT-" + UUID.randomUUID();
        var baixa =
                manutencao
                        .solicitar(
                                new ChamadoDTO(
                                        marca + " baixa",
                                        "PREVENTIVA",
                                        "BAIXA",
                                        "Ana Silva",
                                        "Producao",
                                        "",
                                        "Revisar motor",
                                        UUID.randomUUID()))
                        .getChamado();
        var alta =
                pecas.solicitar(
                                new SolicitacaoPecaDTO(
                                        "Rolamento",
                                        "",
                                        BigDecimal.ONE,
                                        "un",
                                        marca + " alta",
                                        "ESTOQUE",
                                        "ALTA",
                                        "Ana Silva",
                                        "Producao",
                                        "",
                                        "Reposicao de peca",
                                        UUID.randomUUID()))
                        .getSolicitacao();
        assertEquals(alta.id(), fila.listar(0, 1, null, marca).getItens().getFirst().id());
        assertEquals(baixa.id(), fila.listar(1, 1, null, marca).getItens().getFirst().id());
        assertEquals(0L, fila.listar(0, 8, null, marca + "%").getTotal());
    }
}
