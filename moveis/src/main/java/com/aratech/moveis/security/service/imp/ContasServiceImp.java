package com.aratech.moveis.security.service.imp;

import com.aratech.moveis.security.authorization.Permissoes;
import com.aratech.moveis.security.dto.Credencial;
import com.aratech.moveis.security.dto.UsuarioSessao;
import com.aratech.moveis.security.repository.ContasRepository;
import com.aratech.moveis.security.service.ContasService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ContasServiceImp implements ContasService {
    private final ContasRepository repository;

    @Override
    public Optional<Credencial> credencial(String email) {
        var tecnica = repository.consultarAdministrador(email);
        return tecnica.isEmpty()
                ? repository.consultarConta(email).stream().findFirst()
                : Optional.of(tecnica.getFirst());
    }

    @Override
    public UsuarioSessao carregar(Long id, Instant autenticadoEm) {
        var registros =
                id == ADMIN_TECNICO_ID
                        ? repository.consultarAdministradorAtivo()
                        : repository.consultarFuncionarioAtivo(id);
        var conta =
                registros.stream()
                        .findFirst()
                        .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas."));
        Set<String> perfis =
                id == ADMIN_TECNICO_ID
                        ? Set.of("ADMINISTRADOR_SISTEMA")
                        : new TreeSet<>(repository.consultarPerfil(id));
        Set<String> permissoes =
                conta.trocarSenha()
                        ? Set.of()
                        : conta.administrador()
                                ? Permissoes.catalogo()
                                : new TreeSet<>(repository.consultarPermissao(id));
        return new UsuarioSessao(
                conta.id(),
                conta.nome(),
                conta.email(),
                conta.administrador(),
                conta.versao(),
                autenticadoEm,
                Collections.unmodifiableSet(perfis),
                Collections.unmodifiableSet(permissoes),
                conta.trocarSenha());
    }
}
