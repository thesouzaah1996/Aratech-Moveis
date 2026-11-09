package com.aratech.moveis.security.provider;

import com.aratech.moveis.security.authorization.SessaoAuthorities;
import com.aratech.moveis.security.dto.Credencial;
import com.aratech.moveis.security.service.ContasService;
import com.aratech.moveis.security.service.LimiteLoginService;
import com.aratech.moveis.security.validation.IdentificadorHash;
import com.aratech.moveis.security.validation.LoginCorporativo;

import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Component
public class LoginProvider implements AuthenticationProvider {
    private final ContasService contas;
    private final LimiteLoginService limite;
    private final PasswordEncoder encoder;
    private final String hashFicticio;

    public LoginProvider(ContasService contas, LimiteLoginService limite, PasswordEncoder encoder) {
        this.contas = contas;
        this.limite = limite;
        this.encoder = encoder;
        this.hashFicticio = encoder.encode(UUID.randomUUID().toString());
    }

    @Override
    public Authentication authenticate(Authentication request) {
        String email = request.getName().strip().toLowerCase(Locale.ROOT);
        String senha = request.getCredentials() == null ? "" : request.getCredentials().toString();
        String ip =
                request.getDetails() instanceof WebAuthenticationDetails details
                        ? details.getRemoteAddress()
                        : "local";
        limite.consumir("ip", ip, 50);
        if (!LoginCorporativo.valido(email) || senha.length() > 128 || senha.isEmpty())
            throw new BadCredentialsException("Credenciais inválidas.");
        limite.consumir("conta", email, 10);
        var credencial = contas.credencial(email);
        boolean correta =
                encoder.matches(senha, credencial.map(Credencial::hash).orElse(hashFicticio));
        if (!correta || credencial.isEmpty()) {
            org.slf4j.LoggerFactory.getLogger(getClass())
                    .warn(
                            "Login rejeitado; identificador={}, origem={}",
                            IdentificadorHash.hash(email),
                            IdentificadorHash.hash(ip));
            throw new BadCredentialsException("Credenciais inválidas.");
        }
        var usuario = contas.carregar(credencial.get().id(), Instant.now());
        limite.limparConta(email);
        org.slf4j.LoggerFactory.getLogger(getClass())
                .info("Login concluído; funcionarioId={}", usuario.id());
        return UsernamePasswordAuthenticationToken.authenticated(
                usuario, null, SessaoAuthorities.authorities(usuario));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
