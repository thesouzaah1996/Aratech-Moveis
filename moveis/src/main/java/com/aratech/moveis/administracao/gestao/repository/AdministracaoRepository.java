package com.aratech.moveis.administracao.gestao.repository;

import com.aratech.moveis.administracao.gestao.dto.Auditoria;
import com.aratech.moveis.administracao.gestao.dto.Parametro;
import com.aratech.moveis.shared.dto.Pagina;

import java.math.BigDecimal;
import java.util.*;
import java.util.List;

public interface AdministracaoRepository {
    Pagina<Auditoria> listarAuditoria(int pagina, int tamanho, String busca);

    List<Parametro> listarParametros();

    Parametro bloquearParametro(String chave);

    int atualizarParametro(Integer valor, String chave);

    Parametro buscarParametro(String chave);

    BigDecimal comprasPendentes();

    BigDecimal comprasSemProvisao();

    BigDecimal ordensAtrasadas();

    BigDecimal producaoProxima(int antecedencia);

    BigDecimal titulosVencidos();

    BigDecimal pagamentosProximos(int antecedencia);

    BigDecimal afastamentosPendentes();
}
