package com.aratech.moveis.financeiro.titulos.service;

import com.aratech.moveis.financeiro.titulos.dto.Caixa;
import com.aratech.moveis.financeiro.titulos.dto.Documento;
import com.aratech.moveis.financeiro.titulos.dto.NovoTitulo;
import com.aratech.moveis.financeiro.titulos.dto.Titulo;
import com.aratech.moveis.security.dto.UsuarioSessao;
import com.aratech.moveis.shared.dto.Comando;
import com.aratech.moveis.shared.dto.Pagina;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public interface FinanceiroService {

    String tipo(String recurso);

    Pagina<Titulo> listar(String recurso, int pagina, int tamanho, String busca);

    Titulo buscar(long id);

    Titulo documentar(String recurso, long id, Documento dto);

    Titulo salvar(String recurso, Long id, NovoTitulo dto);

    Titulo acao(String recurso, long id, String acao, Comando dto);

    void integrar(
            String origem,
            long origemId,
            String tipo,
            String descricao,
            String pessoa,
            BigDecimal valor,
            LocalDate vencimento);

    void integrar(
            String origem,
            long origemId,
            String tipo,
            String descricao,
            String pessoa,
            BigDecimal valor,
            LocalDate vencimento,
            UsuarioSessao autor);

    void cancelarOrigem(String origem, long id);

    Pagina<Caixa> caixa(String mes, int pagina, int tamanho, String busca);
}
