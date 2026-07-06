package com.aratechmoveis.login.subscriber;

import com.aratechmoveis.login.dto.FuncionarioDTO;
import com.aratechmoveis.login.mapper.FuncionarioMapper;
import com.aratechmoveis.login.service.FuncionarioService;
import com.aratechmoveis.login.subscriber.representation.FuncionarioRepresentation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class FuncionarioSubscriber {

    private final ObjectMapper objectMapper;
    private final FuncionarioMapper funcionarioMapper;
    private final FuncionarioService funcionarioService;

    @KafkaListener(groupId = "aratech-recursoshumanos",
        topics = "${aratech.config.kafka.topics.login-funcionario}")
    public void listenFuncionariosAdicionados(String json) {
        try {
            log.info("Recebendo novo funcionario para criação de login");
            var representation = objectMapper.readValue(json, FuncionarioRepresentation.class);
            FuncionarioDTO novoFuncionario = funcionarioMapper.map(representation);
            funcionarioService.adicionarFuncionario(novoFuncionario);
            log.info("Funcionário criado com sucesso.");
        } catch (Exception e) {
            log.error("Erro na consumação do tópico de login-funcionario");
        }
    }
}
