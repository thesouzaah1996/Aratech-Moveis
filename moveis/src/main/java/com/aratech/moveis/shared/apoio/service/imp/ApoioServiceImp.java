package com.aratech.moveis.shared.apoio.service.imp;

import com.aratech.moveis.shared.apoio.dto.FuncionarioPonto;
import com.aratech.moveis.shared.apoio.dto.OpcoesProduto;
import com.aratech.moveis.shared.apoio.repository.ApoioRepository;
import com.aratech.moveis.shared.apoio.service.ApoioService;
import com.aratech.moveis.shared.dto.Opcao;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApoioServiceImp implements ApoioService {
    private final ApoioRepository repository;

    public OpcoesProduto produto() {
        return new OpcoesProduto(repository.categorias(), repository.fornecedores());
    }

    public List<FuncionarioPonto> ponto() {
        return repository.ponto();
    }

    public List<Opcao> mecanicos() {
        return repository.mecanicos();
    }
}
