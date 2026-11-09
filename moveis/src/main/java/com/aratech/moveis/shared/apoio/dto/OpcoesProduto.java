package com.aratech.moveis.shared.apoio.dto;

import com.aratech.moveis.shared.dto.Opcao;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public record OpcoesProduto(List<Opcao> categorias, List<Opcao> fornecedores) {}
