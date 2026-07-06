package com.aratechmoveis.recursoshumanos.funcionarios.publisher;

import com.aratechmoveis.recursoshumanos.perfil.entity.Perfil;
import com.aratechmoveis.recursoshumanos.funcionarios.publisher.representation.FuncionarioRepresentation;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FuncionarioPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${aratech.config.kafka.topics.login-funcionario}")
    private String topico;

    public void publicarLoginFuncionario(
            Long idFuncionario,
            String nomeFuncionario,
            String emailCorporativo,
            List<Perfil> perfis,
            Boolean ativo) {

        try {
            var funcionarioRepresentation = new FuncionarioRepresentation(idFuncionario, nomeFuncionario ,emailCorporativo, perfis, ativo);
            String json = objectMapper.writeValueAsString(funcionarioRepresentation);
            kafkaTemplate.send(topico, "dados para login usuário", json);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
