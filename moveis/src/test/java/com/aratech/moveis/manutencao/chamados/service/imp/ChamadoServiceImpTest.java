package com.aratech.moveis.manutencao.chamados.service.imp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.aratech.moveis.manutencao.chamados.dto.ChamadoDTO;
import com.aratech.moveis.manutencao.chamados.dto.ChamadoResumoDTO;
import com.aratech.moveis.manutencao.chamados.enums.OrigemChamado;
import com.aratech.moveis.manutencao.chamados.enums.PrioridadeChamado;
import com.aratech.moveis.manutencao.chamados.enums.StatusChamado;
import com.aratech.moveis.manutencao.chamados.enums.TipoManutencao;
import com.aratech.moveis.manutencao.chamados.exception.ChamadoException;
import com.aratech.moveis.manutencao.chamados.model.Chamado;
import com.aratech.moveis.manutencao.chamados.repository.ChamadoRepository;
import com.aratech.moveis.manutencao.chamados.validation.ChamadoValidator;

import org.junit.jupiter.api.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.*;

class ChamadoServiceImpTest {
    final ChamadoRepository repository = mock(ChamadoRepository.class);
    final ChamadoServiceImp service = new ChamadoServiceImp(repository, new ChamadoValidator());
    final UUID chave = UUID.randomUUID();

    ChamadoDTO dto() {
        return new ChamadoDTO(
                "Prensa 01",
                "CORRETIVA",
                "ALTA",
                "Ana Silva",
                "Producao",
                "(43) 99999-1234",
                "Motor parado",
                chave);
    }

    @BeforeEach
    void setup() {
        when(repository.saveAndFlush(any()))
                .thenAnswer(
                        i -> {
                            Chamado c = i.getArgument(0);
                            c.setId(42L);
                            return c;
                        });
    }

    Chamado existente() {
        return Chamado.builder()
                .id(42L)
                .equipamento("Prensa 01")
                .tipo(TipoManutencao.CORRETIVA)
                .prioridade(PrioridadeChamado.ALTA)
                .solicitante("Ana Silva")
                .setor("Producao")
                .telefone("43999991234")
                .descricao("Motor parado")
                .dataAbertura(Instant.now().minusSeconds(30))
                .chaveOperacao(chave)
                .build();
    }

    @Test
    void criaAbertaComIdEHorarioDoServidor() {
        var inicio = Instant.now().minusMillis(1);
        var resposta = service.solicitar(dto());
        assertEquals(201, resposta.getStatus());
        assertEquals(42L, resposta.getChamado().id());
        assertEquals(StatusChamado.ABERTA, resposta.getChamado().status());
        assertFalse(resposta.getChamado().dataAbertura().isBefore(inicio));
        assertEquals(0, resposta.getChamado().dataAbertura().getNano() % 1000);
        assertEquals("43999991234", resposta.getChamado().telefone());
    }

    @Test
    void repeticaoNaoDuplicaNemMudaHorarioOuStatus() {
        var c = existente();
        c.setStatus(StatusChamado.CONCLUIDA);
        when(repository.findByChaveOperacao(chave)).thenReturn(Optional.of(c));
        var r = service.solicitar(dto());
        assertEquals(42L, r.getChamado().id());
        assertEquals(c.getDataAbertura(), r.getChamado().dataAbertura());
        assertEquals(StatusChamado.CONCLUIDA, r.getChamado().status());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void chaveReutilizadaComOutrosDadosRetornaConflito() {
        var c = existente();
        c.setEquipamento("Outra prensa");
        when(repository.findByChaveOperacao(chave)).thenReturn(Optional.of(c));
        assertEquals(
                HttpStatus.CONFLICT,
                assertThrows(ChamadoException.class, () -> service.solicitar(dto())).getStatus());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void dadosInvalidosNaoAcessamRepositorio() {
        assertThrows(ChamadoException.class, () -> service.solicitar(null));
        verifyNoInteractions(repository);
    }

    @Test
    void consultaIdAusenteRetorna404() {
        assertEquals(
                HttpStatus.NOT_FOUND,
                assertThrows(ChamadoException.class, () -> service.buscar(99L)).getStatus());
        assertThrows(ChamadoException.class, () -> service.buscar(-1L));
        verify(repository, never()).findById(-1L);
    }

    @Test
    void consultaDevolveMesmoChamadoPersistido() {
        var c = existente();
        when(repository.findById(42L)).thenReturn(Optional.of(c));
        assertEquals(ChamadoResumoDTO.from(c), service.buscar(42L).getChamado());
    }

    @Test
    void paginaMantemTotalGlobalEFiltraStatusNoBanco() {
        var pageable = PageRequest.of(1, 8, Sort.by(Sort.Direction.DESC, "dataAbertura", "id"));
        when(repository.findByOrigemAndStatus(
                        OrigemChamado.MANUTENCAO, StatusChamado.ABERTA, pageable))
                .thenReturn(new PageImpl<>(List.of(existente()), pageable, 17));
        var r = service.listar(1, 8, "ABERTA");
        assertEquals(17L, r.getTotal());
        assertEquals(1, r.getChamados().size());
        verify(repository)
                .findByOrigemAndStatus(OrigemChamado.MANUTENCAO, StatusChamado.ABERTA, pageable);
        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    void listaSemFiltroERejeitaPaginacaoInvalida() {
        when(repository.findByOrigem(eq(OrigemChamado.MANUTENCAO), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(0, 8)));
        assertTrue(service.listar(0, 8, null).getChamados().isEmpty());
        assertThrows(ChamadoException.class, () -> service.listar(0, 101, null));
    }
}
