package com.aratechmoveis.manutencao.chamado.service;

import com.aratechmoveis.manutencao.Response;
import com.aratechmoveis.manutencao.chamado.dto.AtribuirMecanicoDTO;
import com.aratechmoveis.manutencao.chamado.dto.ChamadoDTO;
import com.aratechmoveis.manutencao.chamado.entity.StatusChamado;

public interface ChamadoService {
    Response addChamado(ChamadoDTO chamadoDTO);
    Response getChamados(StatusChamado status);
    Response getChamadoById(Long id);
    Response atribuirMecanico(Long id, AtribuirMecanicoDTO atribuirMecanicoDTO);
    Response concluirChamado(Long id);
    Response deleteChamado(Long id);
}
