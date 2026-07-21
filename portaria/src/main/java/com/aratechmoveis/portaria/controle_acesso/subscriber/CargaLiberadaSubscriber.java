package com.aratechmoveis.portaria.controle_acesso.subscriber;

import com.aratechmoveis.portaria.controle_acesso.publisher.representation.CargaLiberadaRepresentation;
import com.aratechmoveis.portaria.controle_acesso.service.RegistroChegadaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class CargaLiberadaSubscriber {

    private final ObjectMapper objectMapper;
    private final RegistroChegadaService registroChegadaService;

    @KafkaListener(groupId = "aratech-portaria-carga-liberada",
        topics = "${aratech.config.kafka.topics.almoxarifado-liberar-recebimento}",
        containerFactory = "kafkaListener")
    public void listenCargaLiberada(String json) {
        log.info("Recebendo liberação de carga do almoxarifado");
        try {
            var representation = objectMapper.readValue(json, CargaLiberadaRepresentation.class);
            registroChegadaService.autorizarRecebimento(representation.notaFiscal());
            log.info("Carga liberada com sucesso");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
