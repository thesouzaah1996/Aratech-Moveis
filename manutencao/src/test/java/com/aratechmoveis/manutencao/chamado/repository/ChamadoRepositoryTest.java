package com.aratechmoveis.manutencao.chamado.repository;

import com.aratechmoveis.manutencao.chamado.entity.Chamado;
import com.aratechmoveis.manutencao.chamado.entity.Prioridade;
import com.aratechmoveis.manutencao.chamado.entity.StatusChamado;
import com.aratechmoveis.manutencao.chamado.entity.TipoManutencao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("ChamadoRepository")
class ChamadoRepositoryTest {

    @Autowired
    private ChamadoRepository chamadoRepository;

    private Chamado umChamado(String equipamento, StatusChamado status) {
        return Chamado.builder()
                .equipamento(equipamento)
                .tipo(TipoManutencao.CORRETIVA)
                .prioridade(Prioridade.ALTA)
                .solicitante("Carlos Ferreira")
                .setor("Produção")
                .descricao("Descrição do chamado")
                .status(status)
                .build();
    }

    @Nested
    @DisplayName("findByStatus")
    class FindByStatus {

        @Test
        @DisplayName("deve retornar apenas os chamados com o status informado")
        void deveRetornarApenasChamadosComStatusInformado() {
            chamadoRepository.save(umChamado("Torno CNC 03", StatusChamado.ABERTA));
            chamadoRepository.save(umChamado("Empilhadeira 02", StatusChamado.EM_MANUTENCAO));
            chamadoRepository.save(umChamado("Prensa Hidráulica", StatusChamado.CONCLUIDA));

            List<Chamado> abertos = chamadoRepository.findByStatus(StatusChamado.ABERTA, Sort.by(Sort.Direction.DESC, "dataAbertura"));

            assertThat(abertos).hasSize(1);
            assertThat(abertos.get(0).getEquipamento()).isEqualTo("Torno CNC 03");
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há chamados com o status informado")
        void deveRetornarListaVaziaQuandoSemChamadosComStatus() {
            chamadoRepository.save(umChamado("Torno CNC 03", StatusChamado.ABERTA));

            List<Chamado> concluidos = chamadoRepository.findByStatus(StatusChamado.CONCLUIDA, Sort.by(Sort.Direction.DESC, "dataAbertura"));

            assertThat(concluidos).isEmpty();
        }
    }
}
