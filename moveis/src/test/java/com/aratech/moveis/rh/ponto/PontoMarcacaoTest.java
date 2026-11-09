package com.aratech.moveis.rh.ponto;

import com.aratech.moveis.rh.ponto.dto.*;
import com.aratech.moveis.rh.ponto.repository.*;
import com.aratech.moveis.rh.ponto.service.imp.PontoServiceImp;
import com.aratech.moveis.security.dto.UsuarioSessao;
import com.aratech.moveis.security.exception.AuthException;
import org.junit.jupiter.api.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PontoMarcacaoTest {
    final PontoRepository repo = mock(PontoRepository.class);
    final EscalaPontoRepository escala = mock(EscalaPontoRepository.class);
    final PontoServiceImp service = new PontoServiceImp(repo, escala);
    final UsuarioSessao usuario = usuario(false, Set.of());
    final LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));

    static UsuarioSessao usuario(boolean admin, Set<String> perfis) {
        return new UsuarioSessao(7L, "Ana", "ana@aratech.com.br", admin, 1, Instant.now(), perfis, Set.of("RH_PONTO.ALTERAR"));
    }
    MarcacaoPontoDTO dto(LocalDate data, long versao) {
        return new MarcacaoPontoDTO(data, versao);
    }
    @BeforeEach void preparar() {
        when(repo.bloquearFuncionario(7L)).thenReturn(List.of(7L));
        when(repo.bloquearDia(eq(7L), any())).thenReturn(List.of());
        when(escala.listar(7L)).thenReturn(List.of());
    }
    @Test void registraHoraDoServidorEHistoricoSemTrocarIdentidade() {
        var dia = service.marcar(dto(hoje, 0), usuario);
        assertEquals(hoje, dia.data());
        assertEquals(1, dia.versao());
        assertTrue(dia.horarios().getFirst().matches("\\d{2}:\\d{2}"));
        assertEquals(1, dia.horarios().stream().filter(h -> !h.isEmpty()).count());
        verify(repo).inserirHistorico(eq(7L), eq(hoje), eq(",,,,,"), anyString(), eq("Marcação pelo funcionário"), eq(7L), eq("Ana"));
    }
    @Test void rejeitaMarcacaoSemSessao() {
        assertEquals(401, assertThrows(AuthException.class, () -> service.marcar(dto(hoje, 0), null)).status());
        verifyNoInteractions(repo);
    }
    @Test void contaTecnicaRecebeOrientacaoSemConsultarFuncionario() {
        var tecnico = new UsuarioSessao(0L, "Administrador do Sistema", "admin@aratechmoveis", true, 1, Instant.now(), Set.of(), Set.of());
        var erro = assertThrows(AuthException.class, () -> service.marcar(dto(hoje, 0), tecnico));
        assertEquals(403, erro.status());
        assertTrue(erro.getMessage().contains("conta técnica"));
        verifyNoInteractions(repo);
    }
    @Test void rejeitaDataDiferenteDoServidor() {
        assertEquals(400, assertThrows(AuthException.class, () -> service.marcar(dto(hoje.minusDays(1), 0), usuario)).status());
        verify(repo, never()).inserirDia(anyLong(), any(), anyString());
    }
    @Test void rejeitaReenvioComVersaoAntiga() {
        when(repo.bloquearDia(7L, hoje)).thenReturn(List.of(Map.entry("08:00,,,,,", 1L)));
        assertEquals(409, assertThrows(AuthException.class, () -> service.marcar(dto(hoje, 0), usuario)).status());
        verify(repo, never()).inserirDia(anyLong(), any(), anyString());
    }
    @Test void administradorSemPerfilEspecialNaoPodeCorrigir() {
        assertEquals(403, assertThrows(AuthException.class, () -> service.salvar(7L, hoje, new EdicaoPontoDTO(List.of("08:00", "12:00", "", "", "", ""), "Correção solicitada", 0), usuario(true, Set.of()))).status());
    }
    @Test void escalaNaoPodeExcederJornadaCadastrada() {
        when(escala.jornada(7L)).thenReturn(40 * 60);
        var dias = new ArrayList<EscalaPontoDTO>();
        for (int i=1;i<=7;i++) dias.add(new EscalaPontoDTO(i, List.of("08:00","16:00","","","",""), 0));
        assertEquals(400, assertThrows(AuthException.class, () -> service.salvarEscala(7L, dias, usuario(false,Set.of("#")))).status());
        verify(escala, never()).salvar(anyLong(), anyList());
    }
    @Test void somaParesEPreservaSaidaComAlertaDeExcesso() {
        when(repo.consultarFuncionario(7L)).thenReturn(List.of(new FuncionarioPontoDTO(7L,"Ana","Analista","RH")));
        when(escala.listar(7L)).thenReturn(List.of(new EscalaPontoDTO(hoje.getDayOfWeek().getValue(),List.of("08:00","12:00","13:00","17:00","",""),120)));
        when(repo.consultarDia(eq(7L),any(),any())).thenReturn(List.of(new DiaPontoDTO(hoje,List.of("08:00","12:00","13:00","20:00","",""),1,true)));
        var dia = service.listar(7L, YearMonth.from(hoje).toString()).getFirst();
        assertEquals(660, dia.totalMinutos());
        assertEquals("FORA_HORARIO", dia.situacao());
        assertTrue(dia.aviso().contains("Limite"));
    }
}
