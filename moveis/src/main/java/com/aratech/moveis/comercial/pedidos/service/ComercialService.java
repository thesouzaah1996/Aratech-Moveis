package com.aratech.moveis.comercial.pedidos.service;

import com.aratech.moveis.comercial.pedidos.dto.NovaVenda;
import com.aratech.moveis.comercial.pedidos.dto.Venda;
import com.aratech.moveis.shared.dto.Comando;
import com.aratech.moveis.shared.dto.Pagina;

import java.math.*;
import java.time.*;
import java.util.*;

public interface ComercialService {

    Pagina<Venda> vendas(int pagina, int tamanho, String busca);

    Venda venda(Long id, NovaVenda dto);

    Venda vendaAcao(long id, String acao, Comando dto);
}
