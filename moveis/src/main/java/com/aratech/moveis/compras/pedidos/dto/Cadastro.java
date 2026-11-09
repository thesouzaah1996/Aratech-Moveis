package com.aratech.moveis.compras.pedidos.dto;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public record Cadastro(
        @io.swagger.v3.oas.annotations.media.Schema(
                        requiredMode =
                                io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED,
                        maxLength = 150)
                String fornecedor,
        @io.swagger.v3.oas.annotations.media.Schema(
                        requiredMode =
                                io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED,
                        minLength = 2,
                        maxLength = 150)
                String item,
        @io.swagger.v3.oas.annotations.media.Schema(
                        requiredMode =
                                io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED,
                        description =
                                "Quantidade positiva, até 999999999999.999, no máximo três casas"
                                        + " decimais.")
                BigDecimal quantidade,
        @io.swagger.v3.oas.annotations.media.Schema(
                        requiredMode =
                                io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED,
                        description =
                                "Preço unitário positivo, até 9999999999999.99, no máximo duas"
                                        + " casas decimais.")
                BigDecimal valorUnitario,
        @io.swagger.v3.oas.annotations.media.Schema(
                        requiredMode =
                                io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
                LocalDate dataPedido,
        @io.swagger.v3.oas.annotations.media.Schema(
                        requiredMode =
                                io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
                LocalDate dataEntrega,
        long versao,
        @io.swagger.v3.oas.annotations.media.Schema(
                        requiredMode =
                                io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED,
                        description =
                                "Vencimento negociado para pagamento, independente da entrega; não"
                                        + " anterior ao pedido.")
                LocalDate dataVencimento) {}
