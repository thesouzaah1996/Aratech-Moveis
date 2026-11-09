package com.aratech.moveis.rh.afastamentos.service;

import com.aratech.moveis.rh.afastamentos.dto.Afastamento;
import com.aratech.moveis.rh.afastamentos.dto.NovoAfastamento;
import com.aratech.moveis.shared.dto.Comando;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;

import java.util.*;

public interface AfastamentoService {

    List<Opcao> funcionarios();

    Pagina<Afastamento> afastamentos(int pagina, int tamanho, String busca);

    Afastamento criarAfastamento(NovoAfastamento dto);

    Afastamento afastamentoAcao(long id, String acao, Comando dto);
}
