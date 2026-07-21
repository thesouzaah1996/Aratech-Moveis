package com.aratechmoveis.login.funcionario.subscriber;

import com.aratechmoveis.login.funcionario.dto.FuncionarioDTO;
import com.aratechmoveis.login.funcionario.mapper.FuncionarioMapper;
import com.aratechmoveis.login.funcionario.service.FuncionarioService;
import com.aratechmoveis.login.funcionario.subscriber.representation.FuncionarioRepresentation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class FuncionarioSubscriber {

    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;
    private final FuncionarioMapper funcionarioMapper;
    private final FuncionarioService funcionarioService;

    @KafkaListener(groupId = "aratech-login",
            topics = "${aratech.config.kafka.topics.login-funcionario}")
    public void listenFuncionariosAdicionados(String json) {
        log.info("Recebendo funcionario para salvar.");
        try {
            var representation = objectMapper.readValue(json, FuncionarioRepresentation.class);
            var funcionario = funcionarioMapper.map(representation);
            FuncionarioDTO funcionarioDTO = modelMapper.map(funcionario, FuncionarioDTO.class);
            funcionarioService.adicionarFuncionario(funcionarioDTO);
            log.info("Funcionário criado com sucesso.");
        } catch (Exception e) {
            log.error("Erro na consumação do tópico de login-funcionario", e);
        }
    }
}
