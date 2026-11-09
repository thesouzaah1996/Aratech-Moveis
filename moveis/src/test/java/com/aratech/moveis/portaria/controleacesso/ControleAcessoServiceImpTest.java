package com.aratech.moveis.portaria.controleacesso;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.aratech.moveis.portaria.controleacesso.dto.ChegadaDTO;
import com.aratech.moveis.portaria.controleacesso.enums.StatusAcesso;
import com.aratech.moveis.portaria.controleacesso.exception.ControleAcessoException;
import com.aratech.moveis.portaria.controleacesso.model.RegistroChegada;
import com.aratech.moveis.portaria.controleacesso.repository.RegistroChegadaRepository;
import com.aratech.moveis.portaria.controleacesso.service.imp.ControleAcessoServiceImp;
import com.aratech.moveis.portaria.controleacesso.validation.ChegadaValidator;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

class ControleAcessoServiceImpTest {
    final RegistroChegadaRepository repository = mock(RegistroChegadaRepository.class);
    final ControleAcessoServiceImp service =
            new ControleAcessoServiceImp(repository, new ChegadaValidator());
    final UUID chave = UUID.randomUUID();

    ChegadaDTO dto() {
        return new ChegadaDTO(
                "12345",
                "Empresa Exemplo",
                "Motorista Exemplo",
                "abc-1234",
                "Carga de madeira",
                "ALMOXARIFADO",
                chave);
    }

    @BeforeEach
    void setup() {
        when(repository.saveAndFlush(any()))
                .thenAnswer(
                        i -> {
                            RegistroChegada r = i.getArgument(0);
                            if (r.getId() == null) r.setId(1L);
                            return r;
                        });
    }

    RegistroChegada registro(StatusAcesso status) {
        var r =
                RegistroChegada.builder()
                        .id(1L)
                        .notaFiscal("12345")
                        .empresa("Empresa Exemplo")
                        .nomeMotorista("Motorista Exemplo")
                        .placa("ABC1234")
                        .descricaoCarga("Carga de madeira")
                        .setorResponsavel("ALMOXARIFADO")
                        .chaveOperacao(chave)
                        .status(status)
                        .dataChegada(Instant.now().minusSeconds(100))
                        .build();
        if (status != StatusAcesso.AGUARDANDO) r.setDataAutorizacao(Instant.now().minusSeconds(60));
        if (status == StatusAcesso.EM_RECEBIMENTO || status == StatusAcesso.FINALIZADO)
            r.setDataEntrada(Instant.now().minusSeconds(30));
        if (status == StatusAcesso.FINALIZADO) r.setDataFinalizacao(Instant.now().minusSeconds(10));
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(r));
        return r;
    }

    @Test
    void registraNaFilaAguardandoComHorarioDoServidor() {
        var inicio = Instant.now().truncatedTo(ChronoUnit.MICROS);
        var r = service.registrar(dto());
        assertEquals(StatusAcesso.AGUARDANDO, r.status());
        assertEquals("ABC1234", r.placa());
        assertFalse(r.dataChegada().isBefore(inicio));
        assertEquals(0, r.dataChegada().getNano() % 1000);
        assertNull(r.dataAutorizacao());
        assertNull(r.dataEntrada());
        assertNull(r.dataFinalizacao());
    }

    @Test
    void salvaEDevolveOperacaoDaChegada() {
        var carga =
                new ChegadaDTO(
                        "12345",
                        "Empresa Exemplo",
                        "Motorista Exemplo",
                        "ABC1234",
                        "Carga de madeira",
                        "CARREGAMENTO",
                        chave,
                        "CARREGAR");
        assertEquals("CARREGAR", service.registrar(carga).tipoOperacao());
        var r = registro(StatusAcesso.AGUARDANDO);
        r.setSetorResponsavel("CARREGAMENTO");
        r.setTipoOperacao("CARREGAR");
        when(repository.findByChaveOperacao(chave)).thenReturn(Optional.of(r));
        assertThrows(
                ControleAcessoException.class,
                () ->
                        service.registrar(
                                new ChegadaDTO(
                                        "12345",
                                        "Empresa Exemplo",
                                        "Motorista Exemplo",
                                        "ABC1234",
                                        "Carga de madeira",
                                        "CARREGAMENTO",
                                        chave,
                                        "DESCARREGAR")));
        var editado =
                service.editarSetor(
                        "CARREGAMENTO",
                        1L,
                        new ChegadaDTO(
                                "12345",
                                "Empresa Exemplo",
                                "Motorista Exemplo",
                                "ABC1234",
                                "Carga de madeira",
                                "CARREGAMENTO",
                                null,
                                "DESCARREGAR"));
        assertEquals("DESCARREGAR", editado.tipoOperacao());
    }

    @Test
    void repeticaoDaMesmaChaveNaoDuplicaNemReabreRegistro() {
        var r = registro(StatusAcesso.FINALIZADO);
        when(repository.findByChaveOperacao(chave)).thenReturn(Optional.of(r));
        assertEquals(StatusAcesso.FINALIZADO, service.registrar(dto()).status());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void mesmaChaveComDadosDiferentesRetornaConflito() {
        var r = registro(StatusAcesso.AGUARDANDO);
        r.setEmpresa("Outra Empresa");
        when(repository.findByChaveOperacao(chave)).thenReturn(Optional.of(r));
        assertEquals(
                HttpStatus.CONFLICT,
                assertThrows(ControleAcessoException.class, () -> service.registrar(dto()))
                        .getStatus());
    }

    @Test
    void notaFiscalDaMesmaEmpresaNaoPodeSerDuplicada() {
        when(repository.existsByNotaFiscalAndEmpresaIgnoreCaseAndIdNot(
                        "12345", "Empresa Exemplo", 0L))
                .thenReturn(true);
        assertThrows(ControleAcessoException.class, () -> service.registrar(dto()));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void naoPermiteDuasChegadasAtivasParaMesmaPlaca() {
        when(repository.existsByPlacaAndStatusNotAndIdNot("ABC1234", StatusAcesso.FINALIZADO, 0L))
                .thenReturn(true);
        assertThrows(ControleAcessoException.class, () -> service.registrar(dto()));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void fluxoCompletoCompartilhaRegistroEntrePortariaEAlmoxarifado() {
        registro(StatusAcesso.AGUARDANDO);
        var autorizado = service.autorizar(1L);
        assertEquals(StatusAcesso.AUTORIZADO, autorizado.status());
        assertNotNull(autorizado.dataAutorizacao());
        var entrada = service.confirmarEntrada(1L);
        assertEquals(StatusAcesso.EM_RECEBIMENTO, entrada.status());
        assertNotNull(entrada.dataEntrada());
        var finalizado = service.finalizar(1L);
        assertEquals(StatusAcesso.FINALIZADO, finalizado.status());
        assertNotNull(finalizado.dataFinalizacao());
        assertEquals(0, finalizado.dataFinalizacao().getNano() % 1000);
        assertEquals(autorizado.id(), finalizado.id());
    }

    @Test
    void carregamentoAutorizaEPortariaConfirmaEntrada() {
        var r = registro(StatusAcesso.AGUARDANDO);
        r.setSetorResponsavel("CARREGAMENTO");
        assertEquals(StatusAcesso.AUTORIZADO, service.autorizarSetor("CARREGAMENTO", 1L).status());
        assertEquals(StatusAcesso.EM_RECEBIMENTO, service.confirmarEntrada(1L).status());
        assertEquals(StatusAcesso.FINALIZADO, service.finalizarSetor("CARREGAMENTO", 1L).status());
    }

    @Test
    void carregamentoNaoAutorizaAlmoxarifado() {
        registro(StatusAcesso.AGUARDANDO);
        assertThrows(
                ControleAcessoException.class, () -> service.autorizarSetor("CARREGAMENTO", 1L));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void entradaAntesDaAutorizacaoEFinalizacaoAntesDaEntradaSaoBloqueadas() {
        registro(StatusAcesso.AGUARDANDO);
        assertTrue(
                assertThrows(ControleAcessoException.class, () -> service.confirmarEntrada(1L))
                        .getMessage()
                        .contains("setor responsavel"));
        assertThrows(ControleAcessoException.class, () -> service.finalizar(1L));
        registro(StatusAcesso.AUTORIZADO);
        assertThrows(ControleAcessoException.class, () -> service.finalizar(1L));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void repetirTransicaoNaoAlteraHorariosNemGravaNovamente() {
        var r = registro(StatusAcesso.AUTORIZADO);
        Instant horario = r.getDataAutorizacao();
        service.autorizar(1L);
        assertEquals(horario, r.getDataAutorizacao());
        registro(StatusAcesso.EM_RECEBIMENTO);
        service.confirmarEntrada(1L);
        registro(StatusAcesso.FINALIZADO);
        service.finalizar(1L);
        verify(repository, never()).saveAndFlush(any());
    }

    @ParameterizedTest
    @EnumSource(
            value = StatusAcesso.class,
            names = {"AUTORIZADO", "EM_RECEBIMENTO", "FINALIZADO"})
    void naoEditaAposAutorizacao(StatusAcesso status) {
        registro(status);
        assertThrows(ControleAcessoException.class, () -> service.editar(1L, dto()));
    }

    @Test
    void editaSomenteEnquantoAguardaSemMudarIdentidadeNemData() {
        var r = registro(StatusAcesso.AGUARDANDO);
        var data = r.getDataChegada();
        var editado =
                service.editar(
                        1L,
                        new ChegadaDTO(
                                "12345",
                                "Empresa Exemplo",
                                "Outro Motorista",
                                "ABC1234",
                                "Outra carga",
                                "ALMOXARIFADO",
                                null));
        assertEquals("Outro Motorista", editado.nomeMotorista());
        assertEquals(data, editado.dataChegada());
        assertEquals(chave, r.getChaveOperacao());
    }

    @Test
    void registroFinalizadoNaoVoltaParaFila() {
        registro(StatusAcesso.FINALIZADO);
        assertThrows(ControleAcessoException.class, () -> service.autorizar(1L));
        assertThrows(ControleAcessoException.class, () -> service.confirmarEntrada(1L));
    }

    @Test
    void naoTrataRegistroDeOutroSetorComoRecebimentoDoAlmoxarifado() {
        var r = registro(StatusAcesso.AGUARDANDO);
        r.setSetorResponsavel("CARREGAMENTO");
        assertThrows(
                ControleAcessoException.class, () -> service.autorizarSetor("ALMOXARIFADO", 1L));
    }

    @Test
    void rejeitaIdsAusentesInvalidosOuInexistentes() {
        assertEquals(
                HttpStatus.BAD_REQUEST,
                assertThrows(ControleAcessoException.class, () -> service.autorizar(0L))
                        .getStatus());
        assertEquals(
                HttpStatus.NOT_FOUND,
                assertThrows(ControleAcessoException.class, () -> service.autorizar(2L))
                        .getStatus());
    }

    @Test
    void paginacaoUsaTotalRealDoBanco() {
        var r = registro(StatusAcesso.AGUARDANDO);
        var pagina = PageRequest.of(1, 8, Sort.by("dataChegada", "id"));
        when(repository.findAll(
                        org.mockito.ArgumentMatchers.<Specification<RegistroChegada>>any(),
                        eq(pagina)))
                .thenReturn(new PageImpl<>(List.of(r), pagina, 17));
        var resultado = service.fila(1, 8);
        assertEquals(17, resultado.getTotalElements());
        assertEquals(3, resultado.getTotalPages());
    }

    @Test
    void rejeitaPaginacaoExcessiva() {
        assertThrows(ControleAcessoException.class, () -> service.fila(-1, 8));
        assertThrows(ControleAcessoException.class, () -> service.fila(0, 101));
        assertThrows(ControleAcessoException.class, () -> service.fila(Integer.MAX_VALUE, 8));
    }

    @Test
    void resumoPortariaIncluiTodosOsSetores() {
        var r = registro(StatusAcesso.AGUARDANDO);
        r.setSetorResponsavel("CARREGAMENTO");
        when(repository.findFirstByOrderByIdDesc()).thenReturn(Optional.of(r));
        when(repository.countByStatus(StatusAcesso.AGUARDANDO)).thenReturn(25L);
        assertEquals(25, service.resumo().aguardando());
        assertEquals(1L, service.resumo().ultimaChegadaId());
    }

    @Test
    void resumoTemContagemGlobalParaNotificarMesmoForaDaPaginaAtual() {
        var r = registro(StatusAcesso.AGUARDANDO);
        when(repository.findFirstBySetorResponsavelOrderByIdDesc("ALMOXARIFADO"))
                .thenReturn(Optional.of(r));
        when(repository.countBySetorResponsavelAndStatus("ALMOXARIFADO", StatusAcesso.AGUARDANDO))
                .thenReturn(20L);
        var resumo = service.resumoSetor("ALMOXARIFADO");
        assertEquals(20, resumo.aguardando());
        assertEquals(1L, resumo.ultimaChegadaId());
        assertEquals("Empresa Exemplo", resumo.ultimaEmpresa());
    }
}
