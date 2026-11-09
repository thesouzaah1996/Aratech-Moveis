package com.aratech.moveis.security.config;

import com.aratech.moveis.security.repository.ProvisionarAdministradorTemporarioRepository;
import com.aratech.moveis.security.validation.LoginCorporativo;

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
@ConditionalOnProperty(name = "aratech.security.admin-temporario", havingValue = "true")
public class ProvisionarAdministradorTemporario implements ApplicationRunner {
    private final ProvisionarAdministradorTemporarioRepository repository;
    private final PasswordEncoder encoder;
    private final PlatformTransactionManager transactionManager;
    private final ConfigurableApplicationContext context;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String login = input.readLine(), senha = input.readLine();
        login = LoginCorporativo.exigir(login);
        if (senha == null || senha.length() < 4 || senha.length() > 128)
            throw new IllegalArgumentException(
                    "Informe usuário e senha temporária válidos pela entrada privada.");
        final String loginValidado = login;
        new TransactionTemplate(transactionManager)
                .executeWithoutResult(
                        status -> {
                            repository.bloquearAdministradorParaBootstrap();
                            if (Boolean.TRUE.equals(repository.existeAdministrador()))
                                throw new IllegalStateException(
                                        "A conta técnica já existe. A credencial não foi"
                                                + " alterada.");
                            repository.inserirAdministrador(loginValidado, encoder.encode(senha));
                        });
        System.out.println(
                "Administrador criado; troca de senha obrigatória antes de acessar os módulos.");
        System.exit(SpringApplication.exit(context, () -> 0));
    }
}
