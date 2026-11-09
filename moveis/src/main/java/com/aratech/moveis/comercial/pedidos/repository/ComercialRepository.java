package com.aratech.moveis.comercial.pedidos.repository;

import com.aratech.moveis.comercial.pedidos.dto.Venda;
import com.aratech.moveis.shared.dto.Pagina;

import java.math.*;
import java.math.BigDecimal;
import java.time.*;
import java.time.LocalDate;
import java.util.*;

public interface ComercialRepository {
    Pagina<Venda> listarVendas(int pagina, int tamanho, String busca);

    Venda bloquearPedidoPorId(Long id);

    Long inserirPedidoComercial(
            String cliente,
            String produto,
            Integer quantidade,
            BigDecimal valorUnitario,
            LocalDate entrega,
            LocalDate vencimento);

    int atualizarPedidoComercial(
            String cliente,
            String produto,
            Integer quantidade,
            BigDecimal valorUnitario,
            LocalDate entrega,
            LocalDate vencimento,
            Long id);

    Venda buscarPedidoPorId(Long registro);

    int atualizarPedidoComercialConfirmado(long id);

    int atualizarPedidoComercialCancelado(long id);
}
