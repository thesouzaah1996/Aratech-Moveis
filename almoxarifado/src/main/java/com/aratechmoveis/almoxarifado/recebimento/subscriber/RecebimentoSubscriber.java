package com.aratechmoveis.almoxarifado.recebimento.subscriber;

import com.aratechmoveis.almoxarifado.exceptions.SetorResponsavelInvalidoException;
import com.aratechmoveis.almoxarifado.recebimento.dto.RecebimentoDTO;
import com.aratechmoveis.almoxarifado.recebimento.mapper.RecebimentoMapper;
import com.aratechmoveis.almoxarifado.recebimento.service.RecebimentoService;
import com.aratechmoveis.almoxarifado.recebimento.subscriber.representation.RecebimentoRepresentation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecebimentoSubscriber {

    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;
    private final RecebimentoMapper recebimentoMapper;
    private final RecebimentoService recebimentoService;

    @KafkaListener(groupId = "aratech-almoxarifado-recebimento",
        topics = "${aratech.config.kafka.topics.portaria-registro-chegada-almoxarifado}")
    public void listenNovosRecebimentos(String json) {
        try {
            var representation = objectMapper.readValue(json, RecebimentoRepresentation.class);
            var recebimento = recebimentoMapper.map(representation);
            RecebimentoDTO recebimentoDTO = modelMapper.map(recebimento, RecebimentoDTO.class);

            if (!"ALMOXARIFADO".equalsIgnoreCase(recebimentoDTO.getSetorResponsavel())) {
                throw new SetorResponsavelInvalidoException("Esse recebimento não pertence ao setor de almoxarifado: " + recebimentoDTO.getSetorResponsavel());
            }

            switch (representation.tipo()) {
                case CHEGADA_REGISTRADA -> {
                    recebimentoService.adicionarRecebimento(recebimentoDTO);
                }
                case DADOS_CORRIGIDOS -> {
                    recebimentoService.atualizarRecebimento(recebimentoDTO);
                }
            }
        } catch (SetorResponsavelInvalidoException e) {
            log.warn(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
