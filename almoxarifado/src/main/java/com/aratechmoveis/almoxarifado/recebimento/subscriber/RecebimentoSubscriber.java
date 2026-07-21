package com.aratechmoveis.almoxarifado.recebimento.subscriber;

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
        log.info("Recebendo nova carga para salvar");
        try {
            var representation = objectMapper.readValue(json, RecebimentoRepresentation.class);
            var recebimento = recebimentoMapper.map(representation);

            RecebimentoDTO novoRecebimento = modelMapper.map(recebimento, RecebimentoDTO.class);
            recebimentoService.adicionarRecebimento(novoRecebimento);
            log.info("Recebido com sucesso");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
