package com.aratech.moveis.almoxarifado.recebimento.service.imp;

import com.aratech.moveis.almoxarifado.recebimento.service.RecebimentoService;
import com.aratech.moveis.portaria.controleacesso.dto.ChegadaDTO;
import com.aratech.moveis.portaria.controleacesso.dto.ChegadaResumoDTO;
import com.aratech.moveis.portaria.controleacesso.dto.FilaResumoDTO;
import com.aratech.moveis.portaria.controleacesso.service.ControleAcessoService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecebimentoServiceImp implements RecebimentoService {
    private final ControleAcessoService controle;

    @Override
    public Page<ChegadaResumoDTO> fila(int pagina, int tamanho) {
        return controle.filaRecebimento(pagina, tamanho);
    }

    @Override
    public Page<ChegadaResumoDTO> historico(int pagina, int tamanho) {
        return controle.historicoRecebimento(pagina, tamanho);
    }

    @Override
    public FilaResumoDTO resumo() {
        return controle.resumoSetor("ALMOXARIFADO");
    }

    @Override
    public ChegadaResumoDTO editar(Long id, ChegadaDTO dto) {
        return controle.editarSetor("ALMOXARIFADO", id, dto);
    }

    @Override
    public ChegadaResumoDTO autorizar(Long id) {
        return controle.autorizarSetor("ALMOXARIFADO", id);
    }

    @Override
    public ChegadaResumoDTO finalizar(Long id) {
        return controle.finalizarSetor("ALMOXARIFADO", id);
    }
}
