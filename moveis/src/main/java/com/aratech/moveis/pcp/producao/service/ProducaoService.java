package com.aratech.moveis.pcp.producao.service;

import com.aratech.moveis.pcp.producao.dto.Carga;
import com.aratech.moveis.pcp.producao.dto.Linha;
import com.aratech.moveis.pcp.producao.dto.NovaLinha;
import com.aratech.moveis.pcp.producao.dto.NovaOrdem;
import com.aratech.moveis.pcp.producao.dto.Ordem;
import com.aratech.moveis.shared.dto.Comando;
import com.aratech.moveis.shared.dto.Indicador;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;

import java.math.*;
import java.time.*;
import java.time.LocalDate;
import java.util.*;

public interface ProducaoService {

    Pagina<Linha> linhas(int pagina, int tamanho, String busca);

    Pagina<Ordem> ordens(String filtro, int pagina, int tamanho, String busca);

    List<Opcao> opcoesLinhas();

    Ordem buscar(long id);

    Linha linha(Long id, NovaLinha dto);

    Ordem salvar(Long id, NovaOrdem dto);

    Ordem acao(long id, String acao, Comando dto);

    List<Carga> capacidade();

    List<Indicador> indicadores();

    void criarEncomenda(long pedidoId, String produto, int quantidade, LocalDate entrega);

    void cancelarEncomenda(long pedidoId);
}
