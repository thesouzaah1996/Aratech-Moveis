package com.aratechmoveis.almoxarifado.recebimento.publisher;

import com.aratechmoveis.almoxarifado.recebimento.entity.StatusRecebimento;
import com.aratechmoveis.almoxarifado.recebimento.publisher.representation.RecebimentoRepresentation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecebimentoPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${aratech.config.kafka.topics.almoxarifado-liberar-recebimento}")
    private String topicoLiberarRecebimento;

    public void publicarLiberacaoRecebimento(
            String notaFiscal,
            StatusRecebimento status) {
        try {
            var cargaLiberadaParaDescarga = new RecebimentoRepresentation(notaFiscal, status);
            String json = objectMapper.writeValueAsString(cargaLiberadaParaDescarga);
            kafkaTemplate.send(topicoLiberarRecebimento, "dados", json);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
