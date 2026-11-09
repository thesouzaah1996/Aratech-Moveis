package com.aratech.moveis.compras.pedidos.dto;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record Pedido(
        Long id,
        String numero,
        String origem,
        String tipo,
        Long estoqueId,
        Long solicitacaoId,
        String item,
        String codigo,
        BigDecimal quantidade,
        String unidade,
        String fornecedor,
        BigDecimal valorUnitario,
        LocalDate dataPedido,
        LocalDate dataEntrega,
        String status,
        String motivo,
        String solicitante,
        String decididoNome,
        Instant decididoEm,
        long versao,
        LocalDate dataVencimento) {}
