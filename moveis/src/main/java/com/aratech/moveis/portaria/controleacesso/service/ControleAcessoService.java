package com.aratech.moveis.portaria.controleacesso.service;

import com.aratech.moveis.portaria.controleacesso.dto.ChegadaDTO;
import com.aratech.moveis.portaria.controleacesso.dto.ChegadaResumoDTO;
import com.aratech.moveis.portaria.controleacesso.dto.FilaResumoDTO;

import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface ControleAcessoService {
    ChegadaResumoDTO registrar(ChegadaDTO dto);

    ChegadaResumoDTO editar(Long id, ChegadaDTO dto);

    Page<ChegadaResumoDTO> fila(int pagina, int tamanho);

    Page<ChegadaResumoDTO> filaRecebimento(int pagina, int tamanho);

    Page<ChegadaResumoDTO> historicoRecebimento(int pagina, int tamanho);

    Page<ChegadaResumoDTO> historico(int pagina, int tamanho, String notaFiscal, LocalDate data);

    FilaResumoDTO resumo();

    FilaResumoDTO resumoSetor(String setor);

    Page<ChegadaResumoDTO> filaSetor(String setor, int pagina, int tamanho);

    Page<ChegadaResumoDTO> historicoSetor(String setor, int pagina, int tamanho);

    ChegadaResumoDTO editarSetor(String setor, Long id, ChegadaDTO dto);

    ChegadaResumoDTO autorizarSetor(String setor, Long id);

    ChegadaResumoDTO finalizarSetor(String setor, Long id);

    ChegadaResumoDTO autorizar(Long id);

    ChegadaResumoDTO confirmarEntrada(Long id);

    ChegadaResumoDTO finalizar(Long id);
}
