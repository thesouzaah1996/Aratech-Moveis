package com.aratech.moveis.rh.perfis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PerfilDTO(@NotBlank @Size(min = 3, max = 100) String nome) {}
