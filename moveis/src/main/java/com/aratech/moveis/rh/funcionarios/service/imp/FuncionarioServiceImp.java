package com.aratech.moveis.rh.funcionarios.service.imp;

import com.aratech.moveis.rh.funcionarios.dto.FuncionarioDTO;
import com.aratech.moveis.rh.funcionarios.dto.FuncionarioResumoDTO;
import com.aratech.moveis.rh.funcionarios.dto.Response;
import com.aratech.moveis.rh.funcionarios.exception.CadastroFuncionarioException;
import com.aratech.moveis.rh.funcionarios.model.Funcionario;
import com.aratech.moveis.rh.funcionarios.repository.FuncionarioRepository;
import com.aratech.moveis.rh.funcionarios.service.FuncionarioDocumentoService;
import com.aratech.moveis.rh.funcionarios.service.FuncionarioService;
import com.aratech.moveis.rh.funcionarios.validation.DocumentoValidator;
import com.aratech.moveis.rh.funcionarios.validation.FuncionarioValidator;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FuncionarioServiceImp implements FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final FuncionarioDocumentoService documentoService;
    private final FuncionarioValidator validator;
    private final DocumentoValidator documentoValidator;

    @Override
    @Transactional
    public Response addFuncionario(FuncionarioDTO dto) {
        validator.validarCadastro(dto);
        String email = dto.getEmail().trim().toLowerCase(Locale.ROOT);
        String pisNitNis =
                dto.getPisNitNis() == null || dto.getPisNitNis().isBlank()
                        ? null
                        : dto.getPisNitNis();
        validator.validarDuplicidade(dto.getCpf(), email, pisNitNis);
        documentoValidator.validarDocumentos(dto);

        var documentos = documentoService.upload(dto);
        Funcionario funcionario =
                Funcionario.builder().documentos(documentos).cpf(dto.getCpf()).build();
        preencherDados(funcionario, dto);
        try {
            Funcionario salvo = funcionarioRepository.saveAndFlush(funcionario);
            return Response.builder()
                    .status(HttpStatus.CREATED.value())
                    .mensagem("Funcionário cadastrado com sucesso.")
                    .funcionarioId(salvo.getId())
                    .nomeCompleto(salvo.getNomeCompleto())
                    .ativo(salvo.getAtivo())
                    .build();
        } catch (RuntimeException e) {
            if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                documentoService.remover(documentos);
            }
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Response getAllFuncionarios() {
        return getAllFuncionarios(0, 20);
    }

    @Override
    @Transactional(readOnly = true)
    public Response getAllFuncionarios(int pagina, int tamanho) {
        validator.validarPaginacao(pagina, tamanho);
        var resultado =
                funcionarioRepository.findAll(
                        PageRequest.of(pagina, tamanho, Sort.by("id").ascending()));
        return Response.builder()
                .status(HttpStatus.OK.value())
                .funcionarios(
                        resultado.getContent().stream().map(FuncionarioResumoDTO::from).toList())
                .pagina(pagina)
                .tamanho(tamanho)
                .total(resultado.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response getFuncionarioById(Long id) {
        validator.validarId(id);
        return resposta(buscar(id, false), "Funcionario encontrado.");
    }

    @Override
    @Transactional
    public Response updateFuncionario(FuncionarioDTO dto, Long id) {
        validator.validarId(id);
        Funcionario atual = buscar(id, true);
        validator.validarAtualizacao(dto, atual);
        documentoValidator.validarDocumentos(dto, false);
        var novos = documentoService.uploadAtualizacao(dto);
        var documentos = new LinkedHashMap<>(atual.getDocumentos());
        var antigos = new LinkedHashMap<String, String>();
        novos.forEach(
                (tipo, chave) -> {
                    String anterior = documentos.put(tipo, chave);
                    if (anterior != null && !anterior.equals(chave)) antigos.put(tipo, anterior);
                });
        try {
            for (String obrigatorio : List.of("rgOuCnh", "ctps", "aso", "comprovanteResidencia")) {
                if (documentos.get(obrigatorio) == null || documentos.get(obrigatorio).isBlank()) {
                    throw new CadastroFuncionarioException(
                            HttpStatus.BAD_REQUEST,
                            "O funcionario deve manter os quatro documentos obrigatorios.");
                }
            }
            preencherDados(atual, dto);
            atual.setDocumentos(documentos);
            var salvo = funcionarioRepository.saveAndFlush(atual);
            documentoService.removerAposConfirmacao(antigos);
            return resposta(salvo, "Funcionario atualizado com sucesso.");
        } catch (RuntimeException e) {
            if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                documentoService.remover(novos);
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public Response enableEmployee(Long id) {
        return alterarStatus(id, true);
    }

    @Override
    @Transactional
    public Response disableEmployee(Long id) {
        return alterarStatus(id, false);
    }

    private Response alterarStatus(Long id, boolean ativo) {
        validator.validarId(id);
        var funcionario = buscar(id, true);
        if (funcionario.getAtivo() == null) {
            throw new CadastroFuncionarioException(
                    HttpStatus.CONFLICT, "O funcionario possui um status inconsistente.");
        }
        if (funcionario.getAtivo() == ativo) {
            return resposta(
                    funcionario,
                    ativo ? "Funcionario ja esta ativo." : "Funcionario ja esta inativo.");
        }
        funcionario.setAtivo(ativo);
        return resposta(
                funcionarioRepository.saveAndFlush(funcionario),
                ativo ? "Funcionario ativado com sucesso." : "Funcionario desativado com sucesso.");
    }

    private Funcionario buscar(Long id, boolean bloquear) {
        var resultado =
                bloquear
                        ? funcionarioRepository.findByIdForUpdate(id)
                        : funcionarioRepository.findById(id);
        return resultado.orElseThrow(
                () ->
                        new CadastroFuncionarioException(
                                HttpStatus.NOT_FOUND, "Funcionario nao encontrado."));
    }

    private Response resposta(Funcionario funcionario, String mensagem) {
        return Response.builder()
                .status(HttpStatus.OK.value())
                .mensagem(mensagem)
                .funcionarioId(funcionario.getId())
                .nomeCompleto(funcionario.getNomeCompleto())
                .ativo(funcionario.getAtivo())
                .funcionario(FuncionarioResumoDTO.from(funcionario))
                .build();
    }

    private void preencherDados(Funcionario funcionario, FuncionarioDTO dto) {
        funcionario.setNomeCompleto(dto.getNomeCompleto());
        funcionario.setRg(dto.getRg());
        funcionario.setPisNitNis(dto.getPisNitNis());
        funcionario.setDataNascimento(dto.getDataNascimento());
        funcionario.setSexo(dto.getSexo());
        funcionario.setEstadoCivil(dto.getEstadoCivil());
        funcionario.setNomeMae(dto.getNomeMae());
        funcionario.setNomePai(dto.getNomePai());
        funcionario.setCep(dto.getCep());
        funcionario.setLogradouro(dto.getLogradouro());
        funcionario.setNumero(dto.getNumero());
        funcionario.setComplemento(dto.getComplemento());
        funcionario.setBairro(dto.getBairro());
        funcionario.setCidade(dto.getCidade());
        funcionario.setUf(dto.getUf());
        funcionario.setTelefoneFixo(dto.getTelefoneFixo());
        funcionario.setCelularWhatsapp(dto.getCelularWhatsapp());
        funcionario.setDataAdmissao(dto.getDataAdmissao());
        funcionario.setTipoContrato(dto.getTipoContrato());
        funcionario.setJornadaSemanal(dto.getJornadaSemanal());
        funcionario.setCargo(dto.getCargo());
        funcionario.setTipoFuncionario(dto.getTipoFuncionario());
        funcionario.setSetor(dto.getSetor());
        funcionario.setSalario(dto.getSalario());
        funcionario.setEmail(dto.getEmail());
        funcionario.setBanco(dto.getBanco());
        funcionario.setAgencia(dto.getAgencia());
        funcionario.setConta(dto.getConta());
        funcionario.setTipoConta(dto.getTipoConta());
        funcionario.setChavePix(dto.getChavePix());
    }
}
