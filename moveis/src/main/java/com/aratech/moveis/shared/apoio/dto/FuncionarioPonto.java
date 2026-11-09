package com.aratech.moveis.shared.apoio.dto;

import org.springframework.web.bind.annotation.*;

public record FuncionarioPonto(Long id, String nome, String cargo, String setor) {}
