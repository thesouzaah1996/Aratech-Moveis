package com.aratech.moveis.administracao.gestao.service;

import com.aratech.moveis.administracao.gestao.dto.Auditoria;
import com.aratech.moveis.administracao.gestao.dto.Parametro;
import com.aratech.moveis.administracao.gestao.dto.ValorParametro;
import com.aratech.moveis.shared.dto.Indicador;
import com.aratech.moveis.shared.dto.Pagina;

import java.util.*;

public interface AdministracaoService {

    Pagina<Auditoria> auditoria(int pagina, int tamanho, String busca);

    List<Parametro> parametros();

    Parametro parametro(String chave, ValorParametro dto);

    List<Indicador> relatorio();
}
