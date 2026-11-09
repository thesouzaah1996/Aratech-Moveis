package com.aratech.moveis.security.repository.imp;

import com.aratech.moveis.security.repository.PerfilUsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PerfilUsuarioRepositoryImp implements PerfilUsuarioRepository {
    private final JdbcTemplate jdbc;

    public List<Map<String, Object>> consultarFuncionario(Long id) {
        return jdbc.queryForList(
                "SELECT telefone_fixo AS telefone,celular_whatsapp AS"
                        + " celular,cargo,setor,data_admissao AS admissao,tipo_contrato AS"
                        + " contrato,ativo FROM funcionario WHERE id=?",
                id);
    }
}
