package com.aratech.moveis.portaria.controleacesso.dto;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

public record Erro(int status, String mensagem) {}
