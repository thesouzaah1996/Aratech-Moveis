package com.aratechmoveis.portaria.controle_acesso.publisher;

import com.aratechmoveis.portaria.controle_acesso.entity.SetorResponsavel;
import com.aratechmoveis.portaria.controle_acesso.publisher.representation.RegistroChegadaRepresentation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistroChegadaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${aratech.config.kafka.topics.portaria-registro-chegada-almoxarifado}")
    private String topicoAlmoxarifado;

    @Value("${aratech.config.kafka.topics.portaria-registro-chegada-carregamento}")
    private String topicoCarregamento;

    @Value("${aratech.config.kafka.topics.portaria-registro-chegada-pcp}")
    private String topicoPcp;

    public void publicarRegistroChegada(
            TipoRegistroChegada tipo,
            String notaFiscal,
            String empresa,
            String nomeMotorista,
            String placa,
            String descricaoCarga,
            SetorResponsavel setorResponsavel
    ) {
        try {
            var registroChegadaRepresentation = new RegistroChegadaRepresentation(
                    tipo,
                    notaFiscal,
                    empresa,
                    nomeMotorista,
                    placa,
                    descricaoCarga,
                    setorResponsavel);
            String json = objectMapper.writeValueAsString(registroChegadaRepresentation);

            var topico = registroChegadaRepresentation.setorResponsavel();

            switch (topico) {
                case ALMOXARIFADO -> {
                    kafkaTemplate.send(topicoAlmoxarifado, "dadosEntrega", json);
                    log.info("Caminhão se deslocando para almoxarifado");
                }

                case CARREGAMENTO -> {
                    kafkaTemplate.send(topicoCarregamento, "dadosEntrega", json);
                    log.info("Caminhão se deslocando para carregamento");
                }

                case PCP -> {
                    kafkaTemplate.send(topicoPcp, "dadosEntrega", json);
                    log.info("Caminhão se deslocando para descarga de chapas");
                }

                default -> throw new IllegalArgumentException("Tópico inválido: " + topico);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
