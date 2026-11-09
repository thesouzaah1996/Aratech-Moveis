package com.aratech.moveis.rh.funcionarios.service.imp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.aratech.moveis.rh.funcionarios.dto.FuncionarioDTO;
import com.aratech.moveis.rh.funcionarios.enums.Sexo;
import com.aratech.moveis.rh.funcionarios.enums.TipoConta;
import com.aratech.moveis.rh.funcionarios.enums.TipoContrato;
import com.aratech.moveis.rh.funcionarios.enums.TipoFuncionario;
import com.aratech.moveis.rh.funcionarios.enums.UF;
import com.aratech.moveis.rh.funcionarios.exception.CadastroFuncionarioException;
import com.aratech.moveis.rh.funcionarios.model.Funcionario;
import com.aratech.moveis.rh.funcionarios.repository.FuncionarioRepository;
import com.aratech.moveis.rh.funcionarios.service.FuncionarioDocumentoService;
import com.aratech.moveis.rh.funcionarios.validation.CpfValidator;
import com.aratech.moveis.rh.funcionarios.validation.DocumentoValidator;
import com.aratech.moveis.rh.funcionarios.validation.FuncionarioValidator;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class FuncionarioServiceImpTest {

    private final FuncionarioRepository repository = mock(FuncionarioRepository.class);
    private final FuncionarioDocumentoService documentos = mock(FuncionarioDocumentoService.class);
    private static final ValidatorFactory VALIDATORS = Validation.buildDefaultValidatorFactory();
    private final FuncionarioServiceImp service =
            new FuncionarioServiceImp(
                    repository,
                    documentos,
                    new FuncionarioValidator(
                            repository, VALIDATORS.getValidator(), new CpfValidator()),
                    new DocumentoValidator());
    private FuncionarioDTO dto;

    @BeforeEach
    void preparar() {
        dto =
                FuncionarioDTO.builder()
                        .nomeCompleto("Ana Silva")
                        .cpf("52998224725")
                        .email("ANA@EXEMPLO.COM")
                        .pisNitNis("12345678901")
                        .dataNascimento(LocalDate.of(1990, 1, 1))
                        .sexo(Sexo.FEMININO)
                        .nomeMae("Maria Silva")
                        .cep("12345678")
                        .logradouro("Rua das Flores")
                        .numero("10")
                        .bairro("Centro")
                        .cidade("São Paulo")
                        .uf(UF.SP)
                        .dataAdmissao(LocalDate.of(2026, 1, 1))
                        .tipoContrato(TipoContrato.CLT)
                        .cargo("Analista")
                        .tipoFuncionario(TipoFuncionario.ADMINISTRATIVO)
                        .setor("RH")
                        .salario(new BigDecimal("3000.00"))
                        .banco("Banco Exemplo")
                        .agencia("1234")
                        .conta("123456-7")
                        .tipoConta(TipoConta.CORRENTE)
                        .rgOuCnh(arquivo())
                        .ctps(arquivo())
                        .aso(arquivo())
                        .comprovanteResidencia(arquivo())
                        .build();
    }

    private MockMultipartFile arquivo() {
        return new MockMultipartFile(
                "file", "documento.pdf", "application/pdf", "%PDF-1.7\n%%EOF".getBytes());
    }

    @AfterAll
    static void fecharValidadores() {
        VALIDATORS.close();
    }

    @Test
    void rejeitaCpfComDigitoInvalidoMesmoChamandoServiceDiretamente() {
        dto.setCpf("52998224724");
        assertThrows(CadastroFuncionarioException.class, () -> service.addFuncionario(dto));
        verifyNoInteractions(repository, documentos);
    }

    @Test
    void rejeitaCpfComDigitosRepetidos() {
        dto.setCpf("11111111111");
        assertThrows(CadastroFuncionarioException.class, () -> service.addFuncionario(dto));
        verifyNoInteractions(repository, documentos);
    }

    @Test
    void rejeitaAdmissaoAnteriorAoNascimento() {
        dto.setDataAdmissao(dto.getDataNascimento().minusDays(1));
        assertThrows(CadastroFuncionarioException.class, () -> service.addFuncionario(dto));
        verifyNoInteractions(repository, documentos);
    }

    @Test
    void rejeitaNascimentoNoFuturo() {
        dto.setDataNascimento(LocalDate.now().plusDays(1));
        assertThrows(ConstraintViolationException.class, () -> service.addFuncionario(dto));
        verifyNoInteractions(repository, documentos);
    }

    @Test
    void rejeitaSalarioNegativo() {
        dto.setSalario(new BigDecimal("-1"));
        assertThrows(ConstraintViolationException.class, () -> service.addFuncionario(dto));
        verifyNoInteractions(repository, documentos);
    }

    @Test
    void rejeitaCadastroNuloSemAcessarBanco() {
        var erro =
                assertThrows(
                        CadastroFuncionarioException.class, () -> service.addFuncionario(null));
        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatus());
        verifyNoInteractions(repository, documentos);
    }

    @Test
    void cadastraComDadosNormalizadosDocumentosEIdentificacaoNaResposta() {
        var objetos = Map.of("ctps", "funcionarios/id/ctps");
        when(documentos.upload(dto)).thenReturn(objetos);
        when(repository.saveAndFlush(any()))
                .thenAnswer(
                        invocation -> {
                            Funcionario funcionario = invocation.getArgument(0);
                            funcionario.setId(12L);
                            return funcionario;
                        });

        var response = service.addFuncionario(dto);

        var captor = ArgumentCaptor.forClass(Funcionario.class);
        verify(repository).saveAndFlush(captor.capture());
        assertEquals("ana@exemplo.com", captor.getValue().getEmail());
        assertEquals(objetos, captor.getValue().getDocumentos());
        assertTrue(captor.getValue().getAtivo());
        assertEquals(201, response.getStatus());
        assertEquals(12L, response.getFuncionarioId());
        assertEquals("Ana Silva", response.getNomeCompleto());
        assertEquals("Funcionário cadastrado com sucesso.", response.getMensagem());
        assertNull(response.getToken());
    }

    @Test
    void rejeitaCpfDuplicadoAntesDeEnviarDocumentos() {
        when(repository.existsByCpf(dto.getCpf())).thenReturn(true);
        var erro =
                assertThrows(CadastroFuncionarioException.class, () -> service.addFuncionario(dto));
        assertEquals(409, erro.getStatus().value());
        verifyNoInteractions(documentos);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void rejeitaEmailDuplicadoIgnorandoMaiusculas() {
        when(repository.existsByEmailIgnoreCase("ana@exemplo.com")).thenReturn(true);
        assertThrows(CadastroFuncionarioException.class, () -> service.addFuncionario(dto));
        verifyNoInteractions(documentos);
    }

    @Test
    void rejeitaPisDuplicado() {
        when(repository.existsByPisNitNis(dto.getPisNitNis())).thenReturn(true);
        assertThrows(CadastroFuncionarioException.class, () -> service.addFuncionario(dto));
        verifyNoInteractions(documentos);
    }

    @Test
    void naoConsultaPisQuandoNaoFoiInformado() {
        dto.setPisNitNis(null);
        when(documentos.upload(dto)).thenReturn(Map.of());
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        service.addFuncionario(dto);
        verify(repository, never()).existsByPisNitNis(any());
    }

    @Test
    void removeDocumentosSePersistenciaFalharForaDeTransacao() {
        var objetos = Map.of("ctps", "funcionarios/id/ctps");
        when(documentos.upload(dto)).thenReturn(objetos);
        when(repository.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("duplicado"));
        assertThrows(DataIntegrityViolationException.class, () -> service.addFuncionario(dto));
        verify(documentos).remover(objetos);
    }

    @Test
    void naoEnviaNemSalvaQuandoDocumentoObrigatorioEstaAusente() {
        dto.setAso(new MockMultipartFile("aso", new byte[0]));

        var erro =
                assertThrows(CadastroFuncionarioException.class, () -> service.addFuncionario(dto));

        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatus());
        verify(documentos, never()).upload(any());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void naoSalvaFuncionarioQuandoUploadFalha() {
        when(documentos.upload(dto))
                .thenThrow(
                        new CadastroFuncionarioException(
                                HttpStatus.BAD_GATEWAY,
                                "Não foi possível salvar os documentos. Tente novamente."));

        var erro =
                assertThrows(CadastroFuncionarioException.class, () -> service.addFuncionario(dto));

        assertEquals(HttpStatus.BAD_GATEWAY, erro.getStatus());
        verify(repository, never()).saveAndFlush(any());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "documento.exe",
                "documento.svg",
                "documento.html",
                "documento.zip",
                "../documento.pdf",
                "C:\\documento.pdf",
                "pasta/documento.pdf",
                "documento.pdf:script"
            })
    void rejeitaArquivoInvalidoNoAddFuncionario(String nome) {
        dto.setCtps(
                new MockMultipartFile(
                        "ctps", nome, "application/pdf", "%PDF-1.7\n%%EOF".getBytes()));
        var erro =
                assertThrows(CadastroFuncionarioException.class, () -> service.addFuncionario(dto));
        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatus());
        verifyNoInteractions(documentos);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void rejeitaConteudoDisfarcadoDePdfNoAddFuncionario() {
        dto.setCtps(
                new MockMultipartFile(
                        "ctps",
                        "documento.pdf",
                        "application/pdf",
                        "<script>abc</script>".getBytes()));
        assertThrows(CadastroFuncionarioException.class, () -> service.addFuncionario(dto));
        verifyNoInteractions(documentos);
    }

    @Test
    void rejeitaTipoIncompativelNoAddFuncionario() {
        dto.setCtps(
                new MockMultipartFile(
                        "ctps", "documento.pdf", "image/png", "%PDF-1.7\n%%EOF".getBytes()));
        assertThrows(CadastroFuncionarioException.class, () -> service.addFuncionario(dto));
        verifyNoInteractions(documentos);
    }

    @Test
    void rejeitaArquivoOpcionalInvalidoNoAddFuncionario() {
        dto.setReservista(
                new MockMultipartFile(
                        "reservista", "documento.pdf", "application/pdf", new byte[] {1}));
        assertThrows(CadastroFuncionarioException.class, () -> service.addFuncionario(dto));
        verifyNoInteractions(documentos);
    }

    @Test
    void rejeitaArquivoAcimaDe5MbNoAddFuncionario() {
        var arquivo = spy(arquivo());
        doReturn(5L * 1024 * 1024 + 1).when(arquivo).getSize();
        dto.setCtps(arquivo);
        var erro =
                assertThrows(CadastroFuncionarioException.class, () -> service.addFuncionario(dto));
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, erro.getStatus());
        verifyNoInteractions(documentos);
    }

    @Test
    void rejeitaConjuntoAcimaDe20MbNoAddFuncionario() {
        byte[] conteudo = new byte[5 * 1024 * 1024];
        System.arraycopy("%PDF-1.7".getBytes(), 0, conteudo, 0, 8);
        var arquivo = new MockMultipartFile("file", "documento.pdf", "application/pdf", conteudo);
        dto.setRgOuCnh(arquivo);
        dto.setCtps(arquivo);
        dto.setAso(arquivo);
        dto.setComprovanteResidencia(arquivo);
        dto.setTituloEleitor(arquivo);
        var erro =
                assertThrows(CadastroFuncionarioException.class, () -> service.addFuncionario(dto));
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, erro.getStatus());
        verifyNoInteractions(documentos);
    }

    @Test
    void normalizaCamposNoAddFuncionario() {
        dto.setNomeCompleto("  Ana Silva  ");
        dto.setEmail("  ANA@EXEMPLO.COM  ");
        when(documentos.upload(dto)).thenReturn(Map.of());
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var resposta = service.addFuncionario(dto);
        assertEquals("Ana Silva", resposta.getNomeCompleto());
        var captor = ArgumentCaptor.forClass(Funcionario.class);
        verify(repository).saveAndFlush(captor.capture());
        assertEquals("ana@exemplo.com", captor.getValue().getEmail());
    }

    @Test
    void rejeitaAdmissaoNoMesmoDiaDoNascimento() {
        dto.setDataAdmissao(dto.getDataNascimento());

        var erro =
                assertThrows(CadastroFuncionarioException.class, () -> service.addFuncionario(dto));

        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatus());
        verifyNoInteractions(repository, documentos);
    }

    @Test
    void rejeitaSalarioZeroSemEnviarDocumentosOuPersistir() {
        dto.setSalario(BigDecimal.ZERO);

        var erro =
                assertThrows(ConstraintViolationException.class, () -> service.addFuncionario(dto));

        assertTrue(
                erro.getConstraintViolations().stream()
                        .anyMatch(
                                violacao ->
                                        violacao.getPropertyPath().toString().equals("salario")));
        verifyNoInteractions(repository, documentos);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 45})
    void rejeitaJornadaForaDoIntervaloPermitido(int horas) {
        dto.setJornadaSemanal(horas);

        var erro =
                assertThrows(ConstraintViolationException.class, () -> service.addFuncionario(dto));

        assertTrue(
                erro.getConstraintViolations().stream()
                        .anyMatch(
                                violacao ->
                                        violacao.getPropertyPath()
                                                .toString()
                                                .equals("jornadaSemanal")));
        verifyNoInteractions(repository, documentos);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 44})
    void cadastraFuncionarioAtivoNosLimitesDaJornadaSemPisEDocumentosOpcionais(int horas) {
        dto.setJornadaSemanal(horas);
        dto.setPisNitNis("   ");
        var referencias =
                Map.of(
                        "rgOuCnh",
                        "funcionarios/id/rg",
                        "ctps",
                        "funcionarios/id/ctps",
                        "aso",
                        "funcionarios/id/aso",
                        "comprovanteResidencia",
                        "funcionarios/id/residencia");
        when(documentos.upload(dto)).thenReturn(referencias);
        when(repository.saveAndFlush(any()))
                .thenAnswer(
                        invocation -> {
                            Funcionario funcionario = invocation.getArgument(0);
                            funcionario.setId(42L);
                            return funcionario;
                        });

        var resposta = service.addFuncionario(dto);

        var captor = ArgumentCaptor.forClass(Funcionario.class);
        verify(repository).saveAndFlush(captor.capture());
        var salvo = captor.getValue();
        assertAll(
                () -> assertEquals(horas, salvo.getJornadaSemanal()),
                () -> assertNull(salvo.getPisNitNis()),
                () -> assertEquals(dto.getCpf(), salvo.getCpf()),
                () -> assertEquals(dto.getSalario(), salvo.getSalario()),
                () -> assertEquals(dto.getDataAdmissao(), salvo.getDataAdmissao()),
                () -> assertEquals(referencias, salvo.getDocumentos()),
                () -> assertTrue(salvo.getAtivo()),
                () -> assertEquals(201, resposta.getStatus()),
                () -> assertEquals(42L, resposta.getFuncionarioId()),
                () -> assertTrue(resposta.getAtivo()));
        verify(repository, never()).existsByPisNitNis(any());
        verify(documentos).upload(dto);
        verify(documentos, never()).remover(any());
    }

    private Funcionario existente() {
        return Funcionario.builder()
                .id(42L)
                .cpf(dto.getCpf())
                .nomeCompleto("Nome anterior")
                .email("anterior@example.com")
                .cargo("Analista")
                .setor("RH")
                .documentos(
                        new LinkedHashMap<>(
                                Map.of(
                                        "rgOuCnh",
                                        "old/rg",
                                        "ctps",
                                        "old/ctps",
                                        "aso",
                                        "old/aso",
                                        "comprovanteResidencia",
                                        "old/residencia")))
                .build();
    }

    private void prepararAtualizacao(Funcionario atual) {
        when(repository.findByIdForUpdate(42L)).thenReturn(Optional.of(atual));
        when(documentos.uploadAtualizacao(dto)).thenReturn(Map.of());
        when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        dto.setRgOuCnh(null);
        dto.setCtps(null);
        dto.setAso(null);
        dto.setComprovanteResidencia(null);
    }

    @Test
    void rejeitaIdsInvalidosEmTodasAsOperacoesSemAcessarDependencias() {
        for (Long id : new Long[] {null, 0L, -1L}) {
            assertEquals(
                    HttpStatus.BAD_REQUEST,
                    assertThrows(
                                    CadastroFuncionarioException.class,
                                    () -> service.getFuncionarioById(id))
                            .getStatus());
            assertThrows(
                    CadastroFuncionarioException.class, () -> service.updateFuncionario(dto, id));
            assertThrows(CadastroFuncionarioException.class, () -> service.enableEmployee(id));
            assertThrows(CadastroFuncionarioException.class, () -> service.disableEmployee(id));
        }
        verifyNoInteractions(repository, documentos);
    }

    @Test
    void retorna404ParaFuncionarioInexistenteEmTodasAsOperacoes() {
        assertEquals(
                HttpStatus.NOT_FOUND,
                assertThrows(
                                CadastroFuncionarioException.class,
                                () -> service.getFuncionarioById(42L))
                        .getStatus());
        assertEquals(
                HttpStatus.NOT_FOUND,
                assertThrows(
                                CadastroFuncionarioException.class,
                                () -> service.updateFuncionario(dto, 42L))
                        .getStatus());
        assertEquals(
                HttpStatus.NOT_FOUND,
                assertThrows(CadastroFuncionarioException.class, () -> service.enableEmployee(42L))
                        .getStatus());
        assertEquals(
                HttpStatus.NOT_FOUND,
                assertThrows(CadastroFuncionarioException.class, () -> service.disableEmployee(42L))
                        .getStatus());
        verify(repository, never()).saveAndFlush(any());
        verifyNoInteractions(documentos);
    }

    @Test
    void consultaPorIdRetornaResumoDoFuncionario() {
        var atual = existente();
        when(repository.findById(42L)).thenReturn(Optional.of(atual));
        var resposta = service.getFuncionarioById(42L);
        assertEquals(200, resposta.getStatus());
        assertEquals(42L, resposta.getFuncionario().id());
        assertEquals(atual.getNomeCompleto(), resposta.getFuncionario().nomeCompleto());
        verify(repository, never()).saveAndFlush(any());
        verifyNoInteractions(documentos);
    }

    @Test
    void listaPaginadaRetornaTotalEOrdemEstavel() {
        var page =
                org.springframework.data.domain.PageRequest.of(
                        0, 20, org.springframework.data.domain.Sort.by("id").ascending());
        when(repository.findAll(page))
                .thenReturn(
                        new org.springframework.data.domain.PageImpl<>(
                                List.of(existente()), page, 1));
        var resposta = service.getAllFuncionarios();
        assertEquals(1, resposta.getFuncionarios().size());
        assertEquals(1L, resposta.getTotal());
        assertEquals(0, resposta.getPagina());
        assertEquals(20, resposta.getTamanho());
    }

    @Test
    void listaVaziaRetornaColecaoVaziaEmVezDeNull() {
        when(repository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());
        assertTrue(service.getAllFuncionarios().getFuncionarios().isEmpty());
    }

    @Test
    void rejeitaPaginacaoInvalidaOuExcessiva() {
        for (int[] valores :
                new int[][] {{-1, 20}, {0, 0}, {0, 101}, {0, -1}, {Integer.MAX_VALUE, 100}}) {
            assertThrows(
                    CadastroFuncionarioException.class,
                    () -> service.getAllFuncionarios(valores[0], valores[1]));
        }
        verifyNoInteractions(repository, documentos);
    }

    @Test
    void atualizaDadosSemReenviarArquivosPreservandoCpfIdStatusEDocumentos() {
        var atual = existente();
        atual.setAtivo(false);
        var referencias = Map.copyOf(atual.getDocumentos());
        prepararAtualizacao(atual);
        var resposta = service.updateFuncionario(dto, 42L);
        assertEquals(200, resposta.getStatus());
        assertEquals("Ana Silva", atual.getNomeCompleto());
        assertEquals("ana@exemplo.com", atual.getEmail());
        assertEquals(42L, atual.getId());
        assertEquals(dto.getCpf(), atual.getCpf());
        assertFalse(atual.getAtivo());
        assertEquals(referencias, atual.getDocumentos());
        verify(documentos).removerAposConfirmacao(Map.of());
    }

    @Test
    void atualizacaoRejeitaDadosObrigatoriosEmBrancoAntesDeEnviarOuSalvar() {
        var atual = existente();
        prepararAtualizacao(atual);
        dto.setNomeCompleto("   ");
        assertThrows(ConstraintViolationException.class, () -> service.updateFuncionario(dto, 42L));
        verifyNoInteractions(documentos);
        verify(repository, never()).saveAndFlush(any());
        assertEquals("Nome anterior", atual.getNomeCompleto());
    }

    @Test
    void atualizacaoRejeitaDtoNulo() {
        prepararAtualizacao(existente());
        assertThrows(
                CadastroFuncionarioException.class, () -> service.updateFuncionario(null, 42L));
        verifyNoInteractions(documentos);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void atualizacaoRejeitaTrocaDeCpfMesmoQuandoValido() {
        prepararAtualizacao(existente());
        dto.setCpf("11144477735");
        var erro =
                assertThrows(
                        CadastroFuncionarioException.class,
                        () -> service.updateFuncionario(dto, 42L));
        assertEquals(HttpStatus.CONFLICT, erro.getStatus());
        verifyNoInteractions(documentos);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void atualizacaoRejeitaEmailOuPisDeOutroFuncionario() {
        prepararAtualizacao(existente());
        when(repository.existsByEmailIgnoreCaseAndIdNot("ana@exemplo.com", 42L)).thenReturn(true);
        assertEquals(
                HttpStatus.CONFLICT,
                assertThrows(
                                CadastroFuncionarioException.class,
                                () -> service.updateFuncionario(dto, 42L))
                        .getStatus());
        when(repository.existsByEmailIgnoreCaseAndIdNot("ana@exemplo.com", 42L)).thenReturn(false);
        when(repository.existsByPisNitNisAndIdNot(dto.getPisNitNis(), 42L)).thenReturn(true);
        assertEquals(
                HttpStatus.CONFLICT,
                assertThrows(
                                CadastroFuncionarioException.class,
                                () -> service.updateFuncionario(dto, 42L))
                        .getStatus());
        verifyNoInteractions(documentos);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void substituiDocumentoPreservandoOsDemaisERemoveAntigoAposConfirmacao() {
        var atual = existente();
        prepararAtualizacao(atual);
        dto.setCtps(arquivo());
        when(documentos.uploadAtualizacao(dto)).thenReturn(Map.of("ctps", "new/ctps"));
        service.updateFuncionario(dto, 42L);
        assertEquals("new/ctps", atual.getDocumentos().get("ctps"));
        assertEquals("old/rg", atual.getDocumentos().get("rgOuCnh"));
        var ordem = inOrder(repository, documentos);
        ordem.verify(repository).saveAndFlush(atual);
        ordem.verify(documentos).removerAposConfirmacao(Map.of("ctps", "old/ctps"));
    }

    @Test
    void naoPermiteAtualizacaoQueDeixeDocumentosObrigatoriosAusentes() {
        var atual = existente();
        atual.getDocumentos().remove("aso");
        prepararAtualizacao(atual);
        assertEquals(
                HttpStatus.BAD_REQUEST,
                assertThrows(
                                CadastroFuncionarioException.class,
                                () -> service.updateFuncionario(dto, 42L))
                        .getStatus());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void atualizacaoLimpaSomenteNovosUploadsQuandoBancoFalha() {
        var atual = existente();
        prepararAtualizacao(atual);
        when(documentos.uploadAtualizacao(dto)).thenReturn(Map.of("ctps", "new/ctps"));
        when(repository.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("falha"));
        assertThrows(
                DataIntegrityViolationException.class, () -> service.updateFuncionario(dto, 42L));
        verify(documentos).remover(Map.of("ctps", "new/ctps"));
        verify(documentos, never()).removerAposConfirmacao(any());
    }

    @Test
    void atualizacaoRejeitaArquivoInseguroAntesDoUpload() {
        prepararAtualizacao(existente());
        dto.setCtps(new MockMultipartFile("ctps", "shell.php.jpg", "image/jpeg", new byte[] {1}));
        assertThrows(CadastroFuncionarioException.class, () -> service.updateFuncionario(dto, 42L));
        verifyNoInteractions(documentos);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void ativaEDesativaPreservandoOsDemaisDados() {
        var atual = existente();
        when(repository.findByIdForUpdate(42L)).thenReturn(Optional.of(atual));
        when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        assertFalse(service.disableEmployee(42L).getAtivo());
        assertTrue(service.enableEmployee(42L).getAtivo());
        verify(repository, times(2)).saveAndFlush(atual);
        assertEquals("Nome anterior", atual.getNomeCompleto());
        assertEquals("old/ctps", atual.getDocumentos().get("ctps"));
        verifyNoInteractions(documentos);
    }

    @Test
    void repetirAtivacaoOuDesativacaoNaoRealizaGravacoesDesnecessarias() {
        var atual = existente();
        when(repository.findByIdForUpdate(42L)).thenReturn(Optional.of(atual));
        assertTrue(service.enableEmployee(42L).getAtivo());
        atual.setAtivo(false);
        assertFalse(service.disableEmployee(42L).getAtivo());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void rejeitaStatusNuloSemAlterarBanco() {
        var atual = existente();
        atual.setAtivo(null);
        when(repository.findByIdForUpdate(42L)).thenReturn(Optional.of(atual));
        assertEquals(
                HttpStatus.CONFLICT,
                assertThrows(CadastroFuncionarioException.class, () -> service.enableEmployee(42L))
                        .getStatus());
        verify(repository, never()).saveAndFlush(any());
    }
}
