package com.aratech.moveis.security.service.imp;

import com.aratech.moveis.security.authorization.Permissoes;
import com.aratech.moveis.security.dto.UsuarioSessao;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.security.repository.GestaoAcessoRepository;
import com.aratech.moveis.security.service.GestaoAcessoService;
import com.aratech.moveis.security.validation.LoginCorporativo;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GestaoAcessoServiceImp implements GestaoAcessoService {
    private final GestaoAcessoRepository repository;

    public void validarConvite(Long id) {
        var u = atuante();
        validarAlvo(u, id);
    }

    private void validarAlvo(UsuarioSessao u, Long id) {
        if (u.administrador()) return;
        boolean administrador = Boolean.TRUE.equals(repository.administrador(id));
        var permissoes = repository.listarPermissoesDoUsuario(id);
        if (administrador || !permissoes.stream().allMatch(p -> Permissoes.permite(u, p)))
            throw new AuthException(
                    403,
                    "Você não pode administrar credenciais de um usuário com permissões superiores"
                            + " às suas.");
    }

    public void validarMudancaEmail(Long id, String novoEmail) {
        var emails = repository.buscarEmailPessoal(id);
        if (novoEmail == null
                || emails.isEmpty()
                || novoEmail.strip().equalsIgnoreCase(emails.getFirst())) return;
        validarAlvo(atuante(), id);
        if (Boolean.TRUE.equals(repository.usaEmailPessoalComoLogin(id)))
            LoginCorporativo.exigir(novoEmail);
    }

    public void validarAlteracaoFuncionario(Long id, boolean desativar) {
        if (!Boolean.TRUE.equals(repository.administrador(id))) return;
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UsuarioSessao u) || !u.administrador())
            throw new AuthException(
                    403, "Somente um administrador pode alterar outro administrador.");
        if (desativar && repository.contarAdministradoresAtivos() <= 1)
            throw new AuthException(409, "Mantenha pelo menos um administrador ativo.");
    }

    public UsuarioSessao atuante() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                || !(auth.getPrincipal() instanceof UsuarioSessao u)
                || !u.administrador() && !u.permissoes().contains("ACESSOS.GERENCIAR"))
            throw new AuthException(403, "Você não possui permissão para gerenciar acessos.");
        return u;
    }

    public Set<String> consultar(Long perfilId) {
        if (!Boolean.TRUE.equals(repository.existePerfil(perfilId)))
            throw new AuthException(404, "Perfil não encontrado.");
        return new TreeSet<>(repository.listarPermissoesDoPerfil(perfilId));
    }

    private void validarConcessao(Set<String> permissoes) {
        var u = atuante();
        if (!u.administrador() && !permissoes.stream().allMatch(p -> Permissoes.permite(u, p)))
            throw new AuthException(403, "Você não pode conceder permissões superiores às suas.");
    }

    public void validarPerfilPorNome(String nome) {
        if (nome == null) return;
        var ids = repository.bloquearPerfilPorNome(nome.strip().replaceAll(" +", " "));
        if (!ids.isEmpty()) validarConcessao(consultar(ids.getFirst()));
    }

    @Transactional
    public Set<String> salvar(Long perfilId, Set<String> permissoes) {
        if (permissoes == null || !Permissoes.catalogo().containsAll(permissoes))
            throw new AuthException(400, "Permissões inválidas.");
        for (String permissao : permissoes) {
            String modulo = permissao.split("\\.")[0];
            if (!modulo.equals("ACESSOS") && !permissoes.contains(modulo + ".CONSULTAR"))
                throw new AuthException(
                        400, "Inclua a permissão de consultar ao habilitar ações de um módulo.");
        }
        // O bloqueio serializa alterações simultâneas no mesmo perfil.
        if (repository.bloquearPerfilPorId(perfilId).isEmpty())
            throw new AuthException(404, "Perfil não encontrado.");
        validarConcessao(consultar(perfilId));
        validarConcessao(permissoes);
        repository.removerPermissoesDoPerfil(perfilId);
        for (String permissao : permissoes) repository.inserirPermissao(perfilId, permissao);
        return consultar(perfilId);
    }

    @Transactional
    public void remover(Long usuarioId, Long perfilId) {
        validarConcessao(consultar(perfilId));
        repository.removerVinculo(usuarioId, perfilId);
    }
}
