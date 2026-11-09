package com.aratech.moveis.almoxarifado.recebimento.service;

import com.aratech.moveis.portaria.controleacesso.dto.ChegadaDTO;
import com.aratech.moveis.portaria.controleacesso.dto.ChegadaResumoDTO;
import com.aratech.moveis.portaria.controleacesso.dto.FilaResumoDTO;

import org.springframework.data.domain.Page;

public interface RecebimentoService {
    Page<ChegadaResumoDTO> fila(int pagina, int tamanho);

    Page<ChegadaResumoDTO> historico(int pagina, int tamanho);

    FilaResumoDTO resumo();

    ChegadaResumoDTO editar(Long id, ChegadaDTO dto);

    ChegadaResumoDTO autorizar(Long id);

    ChegadaResumoDTO finalizar(Long id);
}
