package com.aratech.moveis.carregamento.expedicao.service;

import com.aratech.moveis.pcp.producao.dto.Ordem;
import com.aratech.moveis.shared.dto.Comando;
import com.aratech.moveis.shared.dto.Opcao;
import com.aratech.moveis.shared.dto.Pagina;

import java.math.*;
import java.time.*;
import java.util.*;

public interface ExpedicaoService {
    Pagina<Ordem> ordens(String filtro, int pagina, int tamanho, String busca);

    List<Opcao> chegadas();

    Ordem expedir(long id, Comando dto);
}
