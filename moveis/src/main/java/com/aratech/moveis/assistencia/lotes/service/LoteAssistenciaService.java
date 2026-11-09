package com.aratech.moveis.assistencia.lotes.service;

import com.aratech.moveis.assistencia.lotes.model.LoteAssistencia;

import java.time.*;
import java.util.List;

public interface LoteAssistenciaService {

    List<LoteAssistencia> listar();

    LoteAssistencia receber(LoteAssistencia entrada);

    LoteAssistencia avancar(Long id, String acao);
}
