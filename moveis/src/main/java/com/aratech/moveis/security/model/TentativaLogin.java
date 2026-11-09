package com.aratech.moveis.security.model;

import java.time.Instant;

public record TentativaLogin(Instant janela, int contagem) {}
