package com.aratech.moveis.security.service.imp;

import com.aratech.moveis.security.dto.UsuarioSessao;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.security.repository.SenhasRepository;
import com.aratech.moveis.security.service.ContasService;
import com.aratech.moveis.security.service.LimiteLoginService;
import com.aratech.moveis.security.service.SenhasService;
import com.aratech.moveis.security.validation.LoginCorporativo;
import com.aratech.moveis.security.validation.SenhaValidator;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;

@Service
public class SenhasServiceImp implements SenhasService {

    private final SenhasRepository repository;
    private final PasswordEncoder encoder;
    private final LimiteLoginService limite;
    private final SecureRandom random = new SecureRandom();

    public SenhasServiceImp(
            SenhasRepository repository, PasswordEncoder encoder, LimiteLoginService limite) {
        this.repository = repository;
        this.encoder = encoder;
        this.limite = limite;
    }

    @Transactional
    public String criarAcessoCorporativo(Long id, String email) {
        return criarCredencial(id, LoginCorporativo.exigir(email));
    }

    @Transactional
    public String criarAcesso(Long id) {
        return criarCredencial(id, null);
    }

    private String criarCredencial(Long id, String corporativo) {
        var emails = repository.buscarEmailDoFuncionarioAtivoComBloqueio(id);
        if (emails.isEmpty()) throw new AuthException(404, "Funcionário ativo não encontrado.");
        if (Boolean.TRUE.equals(repository.existeConta(id)))
            throw new AuthException(409, "O usuário já possui acesso.");
        String login =
                corporativo == null
                        ? emails.getFirst().strip().toLowerCase(Locale.ROOT)
                        : corporativo;
        LoginCorporativo.exigir(login);
        // Serializa novas credenciais com o mesmo identificador.
        repository.bloquearIdentificadorLogin(login);
        boolean ocupado = Boolean.TRUE.equals(repository.loginOcupado(login, id, login));
        if (ocupado)
            throw new AuthException(409, "Este e-mail já está associado a outro funcionário.");
        byte[] bytes = new byte[18];
        random.nextBytes(bytes);
        String temporaria = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        repository.inserirConta(id, encoder.encode(temporaria), corporativo);
        return temporaria;
    }

    @Transactional
    public void alterar(UsuarioSessao usuario, String atual, String nova) {
        if (nova != null && nova.equals(atual))
            throw new AuthException(400, "Escolha uma senha diferente da atual.");
        limite.consumir(
                "troca-senha",
                usuario.id() == ContasService.ADMIN_TECNICO_ID
                        ? "tecnico:" + usuario.email()
                        : usuario.id().toString(),
                10);
        SenhaValidator.validarSenha(nova);
        if (usuario.id() == ContasService.ADMIN_TECNICO_ID) {
            var hash = repository.buscarHashDoAdministradorComBloqueio();
            if (atual == null
                    || atual.length() > 128
                    || hash.isEmpty()
                    || !encoder.matches(atual, hash.getFirst()))
                throw new AuthException(400, "A senha atual não confere.");
            repository.alterarSenhaDoAdministrador(encoder.encode(nova));
            return;
        }
        var hashes = repository.buscarHashDaContaComBloqueio(usuario.id());
        if (atual == null
                || atual.length() > 128
                || hashes.isEmpty()
                || !encoder.matches(atual, hashes.getFirst()))
            throw new AuthException(400, "A senha atual não confere.");
        repository.alterarSenhaDaConta(encoder.encode(nova), usuario.id());
    }
}
