package com.aratech.moveis.shared.operacao.repository.imp;

import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.shared.dto.Pagina;
import com.aratech.moveis.shared.operacao.repository.OperacaoConsultaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class OperacaoConsultaRepositoryImp implements OperacaoConsultaRepository {
    private final JdbcTemplate jdbc;

    public <T> T um(String sql, Class<T> tipo, Object... args) {
        return jdbc.query(sql, new DataClassRowMapper<>(tipo), args).stream()
                .findFirst()
                .orElseThrow(() -> new AuthException(404, "Registro não encontrado."));
    }

    public <T> Pagina<T> pagina(
            String sql, Class<T> tipo, int pagina, int tamanho, String busca, Object... args) {
        if (pagina < 0
                || pagina > 1000000
                || tamanho < 1
                || tamanho > 100
                || busca != null && busca.length() > 150)
            throw new AuthException(
                    400, "Confira página, tamanho (1 a 100) e busca (até 150 caracteres).");
        var parametros = new ArrayList<Object>(Arrays.asList(args));
        parametros.add("%" + (busca == null ? "" : busca.trim().toLowerCase(Locale.ROOT)) + "%");
        String filtrada =
                "SELECT * FROM (" + sql + ") consulta WHERE lower(CAST(consulta AS text)) LIKE ?";
        long total =
                Objects.requireNonNull(
                        jdbc.queryForObject(
                                "SELECT count(*) FROM (" + filtrada + ") total",
                                Long.class,
                                parametros.toArray()));
        parametros.add(tamanho);
        parametros.add((long) pagina * tamanho);
        return new Pagina<>(
                jdbc.query(
                        filtrada + " ORDER BY id DESC LIMIT ? OFFSET ?",
                        new DataClassRowMapper<>(tipo),
                        parametros.toArray()),
                total,
                pagina,
                tamanho);
    }
}
