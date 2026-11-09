package com.aratech.moveis.shared.apoio.controller;

import com.aratech.moveis.shared.apoio.dto.FuncionarioPonto;
import com.aratech.moveis.shared.apoio.dto.OpcoesProduto;
import com.aratech.moveis.shared.apoio.service.ApoioService;
import com.aratech.moveis.shared.auditoria.annotation.AuditadoNoServico;
import com.aratech.moveis.shared.dto.Opcao;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Consultas de apoio — Escopo da tarefa",
        description =
                "Seletores devolvem apenas os campos necessários à tarefa autorizada; não exigem"
                    + " liberar a tela de cadastro do outro setor nem expõem documentos, salários"
                    + " ou contatos.")
@AuditadoNoServico
@RestController
@RequiredArgsConstructor
public class ApoioController {
    private final ApoioService service;

    @Operation(
            summary = "Opções de cadastro de produto",
            description =
                    "Exige consulta de estoque. Somente id e nome de categorias e fornecedores"
                            + " ativos; não libera consulta de dados completos de fornecedores.")
    @GetMapping("/aratech/almoxarifado/produtos/opcoes")
    public OpcoesProduto produto() {
        return service.produto();
    }

    @Operation(
            summary = "Funcionários para controle de ponto",
            description =
                    "Exige consulta de ponto. Identificador, nome, cargo e setor de funcionários"
                            + " ativos; não expõe dados pessoais completos do cadastro de RH.")
    @GetMapping("/aratech/rh/ponto/funcionarios")
    public List<FuncionarioPonto> ponto() {
        return service.ponto();
    }

    @Operation(
            summary = "Mecânicos disponíveis para atribuição na fila",
            description =
                    "Exige consulta da fila. Retorna somente identificador e nome de mecânicos"
                            + " ativos, sem conceder acesso ao cadastro de mecânicos.")
    @GetMapping("/aratech/manutencao/fila/mecanicos")
    public List<Opcao> mecanicos() {
        return service.mecanicos();
    }
}
