package com.aratech.moveis.compras.pedidos.service;

import com.aratech.moveis.almoxarifado.produtos.model.Produto;
import com.aratech.moveis.compras.pedidos.dto.Cadastro;
import com.aratech.moveis.compras.pedidos.dto.Decisao;
import com.aratech.moveis.compras.pedidos.dto.Minimo;
import com.aratech.moveis.compras.pedidos.dto.Pedido;
import com.aratech.moveis.manutencao.chamados.model.Chamado;
import com.aratech.moveis.manutencao.estoque.model.PecaEstoque;
import com.aratech.moveis.security.dto.UsuarioSessao;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public interface PedidoCompraService {

    List<Pedido> listar(String origem);

    Minimo minimo(String origem, Long id, BigDecimal valor);

    void produto(Produto p);

    void peca(PecaEstoque p);

    void solicitacao(Chamado c);

    Pedido decidir(Long id, String acao, Decisao dto, UsuarioSessao autor);

    Pedido salvar(Long id, Cadastro dto, UsuarioSessao autor);

    Pedido enviar(Long id, long versao);

    Pedido cancelar(Long id, long versao);
}
