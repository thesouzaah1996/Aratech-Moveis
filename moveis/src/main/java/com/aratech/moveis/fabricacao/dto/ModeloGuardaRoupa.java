package com.aratech.moveis.fabricacao.dto;

import java.util.List;
public record ModeloGuardaRoupa(long id, long versao, FichaGuardaRoupa ficha, List<FotoFabricacao> fotos) {}
