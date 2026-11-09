package com.aratech.moveis.compras.cotacoes.service;

import com.aratech.moveis.compras.cotacoes.dto.Cotacao;
import com.aratech.moveis.compras.cotacoes.dto.NovaCotacao;
import com.aratech.moveis.shared.dto.Comando;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;

import java.util.*;

public interface CotacaoService {

    Pagina<Cotacao> listar(int pagina, int tamanho, String busca);

    List<Opcao> pedidos();

    Cotacao criar(NovaCotacao dto);

    Cotacao acao(long id, String acao, Comando dto);
}
