package com.aratech.moveis.manutencao.estoque.service.imp;

import com.aratech.moveis.compras.pedidos.service.PedidoCompraService;
import com.aratech.moveis.manutencao.estoque.dto.MovimentacaoPecaDTO;
import com.aratech.moveis.manutencao.estoque.dto.MovimentacaoPecaResumoDTO;
import com.aratech.moveis.manutencao.estoque.dto.PecaEstoqueAtualizacaoDTO;
import com.aratech.moveis.manutencao.estoque.dto.PecaEstoqueDTO;
import com.aratech.moveis.manutencao.estoque.dto.PecaEstoqueResumoDTO;
import com.aratech.moveis.manutencao.estoque.dto.Response;
import com.aratech.moveis.manutencao.estoque.enums.TipoMovimentacaoPeca;
import com.aratech.moveis.manutencao.estoque.exception.PecaEstoqueException;
import com.aratech.moveis.manutencao.estoque.model.MovimentacaoPeca;
import com.aratech.moveis.manutencao.estoque.model.PecaEstoque;
import com.aratech.moveis.manutencao.estoque.repository.MovimentacaoPecaRepository;
import com.aratech.moveis.manutencao.estoque.repository.PecaEstoqueRepository;
import com.aratech.moveis.manutencao.estoque.service.PecaEstoqueService;
import com.aratech.moveis.manutencao.estoque.validation.PecaEstoqueValidator;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PecaEstoqueServiceImp implements PecaEstoqueService {
    private static final BigDecimal LIMITE = new BigDecimal("999999999999.999");
    private final PecaEstoqueRepository repository;
    private final MovimentacaoPecaRepository movimentos;
    private final PecaEstoqueValidator validator;
    private final PedidoCompraService compras;

    @Override
    @Transactional
    public Response adicionar(PecaEstoqueDTO dto) {
        validator.validar(dto);
        if (repository.existsByCodigoIgnoreCase(dto.getCodigo()))
            throw conflito("Codigo ja cadastrado, inclusive em pecas arquivadas.");
        var p =
                PecaEstoque.builder()
                        .nome(dto.getNome())
                        .codigo(dto.getCodigo())
                        .unidade(dto.getUnidade())
                        .quantidade(dto.getQuantidade())
                        .localizacao(dto.getLocalizacao())
                        .descricao(Objects.requireNonNullElse(dto.getDescricao(), ""))
                        .criadoEm(agora())
                        .build();
        var salvo = repository.saveAndFlush(p);
        compras.peca(salvo);
        registrar(
                salvo,
                TipoMovimentacaoPeca.INICIAL,
                salvo.getQuantidade(),
                BigDecimal.ZERO,
                UUID.randomUUID(),
                "Saldo inicial do cadastro");
        return resposta(salvo, 201, "Peca cadastrada com sucesso.");
    }

    @Override
    @Transactional(readOnly = true)
    public Response listar(int pagina, int tamanho, String busca) {
        validator.validarPaginacao(pagina, tamanho);
        var p =
                repository.buscar(
                        validator.validarBusca(busca),
                        PageRequest.of(pagina, tamanho, Sort.by("id")));
        return Response.builder()
                .status(200)
                .pecas(p.getContent().stream().map(PecaEstoqueResumoDTO::from).toList())
                .pagina(pagina)
                .tamanho(tamanho)
                .total(p.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response buscar(Long id) {
        validator.validarId(id);
        return resposta(buscarRegistro(id, false), 200, "Peca encontrada.");
    }

    @Override
    @Transactional
    public Response atualizar(Long id, PecaEstoqueAtualizacaoDTO dto) {
        validator.validarId(id);
        validator.validar(dto);
        var p = buscarRegistro(id, true);
        validarAtivo(p);
        if (!p.getCodigo().equals(dto.getCodigo()))
            throw conflito("O codigo da peca nao pode ser alterado.");
        if (!p.getUnidade().equals(dto.getUnidade()))
            throw conflito("A unidade nao pode ser alterada para preservar o saldo e o historico.");
        p.setNome(dto.getNome());
        p.setLocalizacao(dto.getLocalizacao());
        p.setDescricao(Objects.requireNonNullElse(dto.getDescricao(), ""));
        var atualizado = repository.saveAndFlush(p);
        compras.peca(atualizado);
        return resposta(atualizado, 200, "Peca atualizada com sucesso.");
    }

    @Override
    @Transactional
    public Response arquivar(Long id) {
        validator.validarId(id);
        var p = buscarRegistro(id, true);
        validarSaldo(p);
        if (p.getQuantidade().signum() != 0)
            throw conflito("Retire todo o estoque antes de arquivar a peca.");
        if (!p.isAtivo()) return resposta(p, 200, "Peca ja esta arquivada.");
        p.setAtivo(false);
        var atualizado = repository.saveAndFlush(p);
        compras.peca(atualizado);
        return resposta(atualizado, 200, "Peca arquivada; historico preservado.");
    }

    @Override
    @Transactional
    public Response entrada(Long id, MovimentacaoPecaDTO dto) {
        return movimentar(id, dto, TipoMovimentacaoPeca.ENTRADA);
    }

    @Override
    @Transactional
    public Response saida(Long id, MovimentacaoPecaDTO dto) {
        return movimentar(id, dto, TipoMovimentacaoPeca.SAIDA);
    }

    private Response movimentar(Long id, MovimentacaoPecaDTO dto, TipoMovimentacaoPeca tipo) {
        validator.validarId(id);
        validator.validar(dto);
        var p = buscarRegistro(id, true);
        var anterior = movimentos.findByChaveOperacao(dto.getChaveOperacao());
        if (anterior.isPresent()) {
            var m = anterior.get();
            if (!m.getPeca().getId().equals(id)
                    || m.getTipo() != tipo
                    || m.getQuantidade().compareTo(dto.getQuantidade()) != 0
                    || !Objects.equals(m.getMotivo(), dto.getMotivo()))
                throw conflito("Chave da operacao ja utilizada com outros dados.");
            return resposta(p, 200, "Operacao ja registrada; saldo nao alterado novamente.");
        }
        validarAtivo(p);
        validarSaldo(p);
        var saldoAnterior = p.getQuantidade();
        var saldo =
                tipo == TipoMovimentacaoPeca.ENTRADA
                        ? saldoAnterior.add(dto.getQuantidade())
                        : saldoAnterior.subtract(dto.getQuantidade());
        if (saldo.signum() < 0) throw conflito("Quantidade maior que o estoque disponivel.");
        if (saldo.compareTo(LIMITE) > 0) throw conflito("O saldo ultrapassa o limite permitido.");
        p.setQuantidade(saldo);
        var salvo = repository.saveAndFlush(p);
        compras.peca(salvo);
        registrar(
                salvo,
                tipo,
                dto.getQuantidade(),
                saldoAnterior,
                dto.getChaveOperacao(),
                dto.getMotivo());
        return resposta(salvo, 200, "Movimentacao registrada com sucesso.");
    }

    @Override
    @Transactional(readOnly = true)
    public Response historico(Long id, int pagina, int tamanho) {
        validator.validarId(id);
        validator.validarPaginacao(pagina, tamanho);
        buscarRegistro(id, false);
        var m =
                movimentos.findByPecaId(
                        id, PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "id")));
        return Response.builder()
                .status(200)
                .movimentacoes(
                        m.getContent().stream().map(MovimentacaoPecaResumoDTO::from).toList())
                .pagina(pagina)
                .tamanho(tamanho)
                .total(m.getTotalElements())
                .build();
    }

    private void registrar(
            PecaEstoque p,
            TipoMovimentacaoPeca tipo,
            BigDecimal quantidade,
            BigDecimal anterior,
            UUID chave,
            String motivo) {
        movimentos.saveAndFlush(
                MovimentacaoPeca.builder()
                        .peca(p)
                        .tipo(tipo)
                        .quantidade(quantidade)
                        .saldoAnterior(anterior)
                        .saldoPosterior(p.getQuantidade())
                        .chaveOperacao(chave)
                        .criadoEm(agora())
                        .responsavelNome("Nao identificado")
                        .motivo(motivo)
                        .build());
    }

    private PecaEstoque buscarRegistro(Long id, boolean bloquear) {
        return (bloquear ? repository.findByIdForUpdate(id) : repository.findById(id))
                .orElseThrow(
                        () ->
                                new PecaEstoqueException(
                                        HttpStatus.NOT_FOUND, "Peca nao encontrada."));
    }

    private void validarAtivo(PecaEstoque p) {
        if (!p.isAtivo()) throw conflito("Peca arquivada.");
    }

    private void validarSaldo(PecaEstoque p) {
        if (p.getQuantidade() == null
                || p.getQuantidade().signum() < 0
                || p.getQuantidade().compareTo(LIMITE) > 0)
            throw conflito("Peca com saldo inconsistente.");
    }

    private Response resposta(PecaEstoque p, int status, String mensagem) {
        return Response.builder()
                .status(status)
                .mensagem(mensagem)
                .peca(PecaEstoqueResumoDTO.from(p))
                .build();
    }

    private Instant agora() {
        return Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    private PecaEstoqueException conflito(String mensagem) {
        return new PecaEstoqueException(HttpStatus.CONFLICT, mensagem);
    }
}
