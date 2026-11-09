package com.aratech.moveis.shared.apoio.repository;

import com.aratech.moveis.shared.apoio.dto.FuncionarioPonto;
import com.aratech.moveis.shared.dto.Opcao;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface ApoioRepository {
    List<Opcao> categorias();

    List<Opcao> fornecedores();

    List<Opcao> mecanicos();

    List<FuncionarioPonto> ponto();
}
