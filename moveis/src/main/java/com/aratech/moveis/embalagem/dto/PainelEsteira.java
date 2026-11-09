package com.aratech.moveis.embalagem.dto;

import java.util.List;
public record PainelEsteira(Esteira esteira,int ocupadas,List<TarefaEsteira> trabalhando,
        List<TarefaEsteira> fila,int futuras) {}
