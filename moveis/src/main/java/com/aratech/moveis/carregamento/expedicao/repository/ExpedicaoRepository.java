package com.aratech.moveis.carregamento.expedicao.repository;

import com.aratech.moveis.pcp.producao.dto.EstadoOrdem;
import com.aratech.moveis.shared.dto.Opcao;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.List;
import java.util.Map;

public interface ExpedicaoRepository {
    EstadoOrdem bloquearOrdemParaExpedicao(long id);

    List<Opcao> listarCaminhoesEmAtendimento();

    List<Map<String, Object>> bloquearCaminhaoEmAtendimento(Long referenciaId);

    int registrarExpedicao(Long referenciaId, long id);

    int atualizarPedidoComercialExpedido(Long pedidoId);
}
