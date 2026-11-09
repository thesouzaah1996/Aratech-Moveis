package com.aratech.moveis.shared.operacao.repository;

import com.aratech.moveis.shared.dto.Pagina;

import org.springframework.jdbc.core.*;

import java.util.*;

public interface OperacaoConsultaRepository {
    <T> T um(String sql, Class<T> tipo, Object... args);

    <T> Pagina<T> pagina(
            String sql, Class<T> tipo, int pagina, int tamanho, String busca, Object... args);
}
