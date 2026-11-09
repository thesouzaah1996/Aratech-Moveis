package com.aratech.moveis.manutencao.pecas;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.aratech.moveis.compras.pedidos.service.PedidoCompraService;
import com.aratech.moveis.manutencao.chamados.enums.OrigemChamado;
import com.aratech.moveis.manutencao.chamados.enums.PrioridadeChamado;
import com.aratech.moveis.manutencao.chamados.enums.StatusChamado;
import com.aratech.moveis.manutencao.chamados.exception.ChamadoException;
import com.aratech.moveis.manutencao.chamados.model.Chamado;
import com.aratech.moveis.manutencao.chamados.repository.ChamadoRepository;
import com.aratech.moveis.manutencao.chamados.validation.ChamadoValidator;
import com.aratech.moveis.manutencao.pecas.dto.SolicitacaoPecaDTO;
import com.aratech.moveis.manutencao.pecas.enums.FinalidadePeca;
import com.aratech.moveis.manutencao.pecas.service.imp.SolicitacaoPecaServiceImp;
import com.aratech.moveis.manutencao.pecas.validation.SolicitacaoPecaValidator;

import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

class SolicitacaoPecaServiceImpTest {
    final ChamadoRepository repo = mock(ChamadoRepository.class);
    final ChamadoValidator comum = new ChamadoValidator();
    final SolicitacaoPecaServiceImp service =
            new SolicitacaoPecaServiceImp(
                    repo,
                    new SolicitacaoPecaValidator(comum),
                    comum,
                    mock(PedidoCompraService.class));
    final UUID chave = UUID.randomUUID();

    SolicitacaoPecaDTO dto() {
        return new SolicitacaoPecaDTO(
                "Rolamento",
                "SKF",
                new BigDecimal("1.5"),
                "kg",
                "Prensa",
                "MELHORIA",
                "ALTA",
                "Ana Silva",
                "Producao",
                "",
                "Falha no motor",
                chave);
    }

    @BeforeEach
    void setup() {
        when(repo.saveAndFlush(any()))
                .thenAnswer(
                        i -> {
                            Chamado c = i.getArgument(0);
                            c.setId(42L);
                            return c;
                        });
    }

    @Test
    void criaNaMesmaTabelaComOrigemPecaEIdentidadeDoServidor() {
        var r = service.solicitar(dto()).getSolicitacao();
        assertEquals(42L, r.id());
        assertEquals(StatusChamado.ABERTA, r.status());
        assertNotNull(r.criadoEm());
        assertEquals(FinalidadePeca.MELHORIA, r.finalidade());
        var captor = org.mockito.ArgumentCaptor.forClass(Chamado.class);
        verify(repo).saveAndFlush(captor.capture());
        assertEquals(OrigemChamado.PECA, captor.getValue().getOrigem());
        assertNull(captor.getValue().getTipo());
    }

    @Test
    void repetirChaveNaoDuplicaERecusaDadosDiferentes() {
        var c =
                Chamado.builder()
                        .id(42L)
                        .origem(OrigemChamado.PECA)
                        .nomePeca("Rolamento")
                        .codigoPeca("SKF")
                        .quantidade(new BigDecimal("1.500"))
                        .unidade("kg")
                        .equipamento("Prensa")
                        .finalidade(FinalidadePeca.MELHORIA)
                        .prioridade(PrioridadeChamado.ALTA)
                        .solicitante("Ana Silva")
                        .setor("Producao")
                        .telefone("")
                        .descricao("Falha no motor")
                        .chaveOperacao(chave)
                        .dataAbertura(Instant.now())
                        .build();
        when(repo.findByChaveOperacao(chave)).thenReturn(Optional.of(c));
        assertEquals(42L, service.solicitar(dto()).getSolicitacao().id());
        verify(repo, never()).saveAndFlush(any());
        c.setNomePeca("Outra peca");
        assertThrows(ChamadoException.class, () -> service.solicitar(dto()));
    }

    @Test
    void naoPermiteReutilizarChaveDaManutencaoOuConsultarOutroTipo() {
        var c = Chamado.builder().id(10L).build();
        when(repo.findByChaveOperacao(chave)).thenReturn(Optional.of(c));
        assertThrows(ChamadoException.class, () -> service.solicitar(dto()));
        when(repo.findById(10L)).thenReturn(Optional.of(c));
        assertThrows(ChamadoException.class, () -> service.buscar(10L));
    }
}
