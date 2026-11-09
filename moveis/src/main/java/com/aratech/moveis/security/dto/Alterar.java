package com.aratech.moveis.security.dto;

import org.springframework.web.bind.annotation.*;

import java.util.*;

public record Alterar(String senhaAtual, String novaSenha) {}
