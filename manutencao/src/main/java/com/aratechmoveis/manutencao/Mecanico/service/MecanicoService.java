package com.aratechmoveis.manutencao.Mecanico.service;

import com.aratechmoveis.manutencao.Mecanico.dto.MecanicoDTO;
import com.aratechmoveis.manutencao.Response;

public interface MecanicoService {
    Response adicionarMecanico(MecanicoDTO mecanicoDTO);
    Response listarMecanicos();
    Response atualizarMecanico(Long id, MecanicoDTO mecanicoDTO);
    Response desativarMecanico(Long id);
    Response ativarMecanico(Long id);
    Response buscarOpcoesMecanico();
}
