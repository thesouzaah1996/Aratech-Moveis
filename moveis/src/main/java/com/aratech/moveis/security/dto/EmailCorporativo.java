package com.aratech.moveis.security.dto;

import com.aratech.moveis.security.validation.LoginCorporativo;

import org.springframework.web.bind.annotation.*;

import java.util.*;

public record EmailCorporativo(
        @io.swagger.v3.oas.annotations.media.Schema(
                        description =
                                "Login interno no domínio exato @aratechmoveis. Até 64 caracteres"
                                        + " antes do @. Não cria caixa postal.",
                        example = "nome@aratechmoveis",
                        pattern = LoginCorporativo.REGEX,
                        maxLength = 150,
                        requiredMode =
                                io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED)
                String emailCorporativo) {}
