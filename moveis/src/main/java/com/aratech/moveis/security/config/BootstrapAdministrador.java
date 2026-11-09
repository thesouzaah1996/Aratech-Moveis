package com.aratech.moveis.security.config;

import com.aratech.moveis.security.repository.BootstrapAdministradorRepository;
import com.aratech.moveis.security.validation.LoginCorporativo;
import com.aratech.moveis.security.validation.SenhaValidator;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
@Order(100)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "aratech.security.bootstrap", havingValue = "true")
public class BootstrapAdministrador implements ApplicationRunner {
    private final BootstrapAdministradorRepository repository;
    private final PasswordEncoder encoder;
    private final PlatformTransactionManager transactionManager;
    private final ConfigurableApplicationContext context;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Entrada privada via pipe do script local; nunca pela URL, argumento ou log.
        var input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String id = input.readLine(), senha = input.readLine();
        if (id == null || !id.matches("[1-9][0-9]{0,17}"))
            throw new IllegalArgumentException("Informe o ID de um funcionário ativo.");
        SenhaValidator.validarSenha(senha);
        new TransactionTemplate(transactionManager)
                .executeWithoutResult(
                        status -> {
                            repository.bloquearContasParaBootstrap();
                            if (Boolean.TRUE.equals(repository.administradorConfigurado()))
                                throw new IllegalStateException(
                                        "O administrador inicial já foi configurado. Use a gestão"
                                                + " de acessos.");
                            Long usuarioId = Long.valueOf(id);
                            var emails =
                                    repository.buscarLoginDoFuncionarioAtivoComBloqueio(usuarioId);
                            if (!emails.isEmpty()) LoginCorporativo.exigir(emails.getFirst());
                            if (repository.bloquearFuncionario(usuarioId).isEmpty())
                                throw new IllegalArgumentException(
                                        "Funcionário ativo não encontrado.");
                            repository.inserirConta(usuarioId, encoder.encode(senha));
                        });
        System.out.println(
                "Administrador inicial configurado. A senha não foi registrada em logs.");
        System.exit(SpringApplication.exit(context, () -> 0));
    }
}
