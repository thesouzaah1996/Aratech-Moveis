package com.aratech.moveis.shared.apoio.service;

import com.aratech.moveis.shared.apoio.dto.FuncionarioPonto;
import com.aratech.moveis.shared.apoio.dto.OpcoesProduto;
import com.aratech.moveis.shared.dto.Opcao;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface ApoioService {

    OpcoesProduto produto();

    List<FuncionarioPonto> ponto();

    List<Opcao> mecanicos();
}
