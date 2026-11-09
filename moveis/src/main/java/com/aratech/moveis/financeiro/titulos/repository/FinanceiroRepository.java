package com.aratech.moveis.financeiro.titulos.repository;

import com.aratech.moveis.financeiro.titulos.dto.Caixa;
import com.aratech.moveis.financeiro.titulos.dto.Titulo;
import com.aratech.moveis.shared.dto.Pagina;

import java.math.BigDecimal;
import java.time.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public interface FinanceiroRepository {
    Pagina<Titulo> listarTitulos(
            String recurso, int pagina, int tamanho, String busca, String valor);

    Titulo buscarTitulo(long id);

    Titulo bloquearTituloPorId(long id);

    int vincularDocumento(String documento, LocalDate emissao, long id);

    Long inserirTitulo(
            String tipo,
            String descricao,
            String pessoa,
            BigDecimal valor,
            LocalDate vencimento,
            String documento,
            LocalDate emissao,
            String referencia);

    int atualizarTituloManual(
            String descricao,
            String pessoa,
            BigDecimal valor,
            LocalDate vencimento,
            String documento,
            LocalDate emissao,
            String referencia,
            Long registro);

    int atualizarTituloBaixado(LocalDate data, long id);

    int atualizarTituloCancelado(long id);

    List<Long> inserirProvisao(
            String tipo,
            String descricao,
            String pessoa,
            BigDecimal valor,
            LocalDate vencimento,
            String origem,
            long origemId);

    List<String> bloquearTitulosDaOrigem(String origem, long id);

    int cancelarPendentesDaOrigem(String origem, long id);

    Pagina<Caixa> listarCaixa(int pagina, int tamanho, String busca, LocalDate atDay);
}
