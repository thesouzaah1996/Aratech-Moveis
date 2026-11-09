package com.aratech.moveis.rh.perfis.dto;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

public record Erro(int status, String mensagem) {}
