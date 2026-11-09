package com.aratech.moveis.manutencao.mecanicos.dto;

import java.util.List;

public record MecanicoDTO(String nome, List<String> especialidades, String turno) {}
