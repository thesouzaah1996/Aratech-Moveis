package com.aratech.moveis.security.service.imp;

import com.aratech.moveis.security.dto.UsuarioSessao;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.security.repository.PerfilUsuarioRepository;
import com.aratech.moveis.security.service.ContasService;
import com.aratech.moveis.security.service.PerfilUsuarioService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PerfilUsuarioServiceImp implements PerfilUsuarioService {
    private final PerfilUsuarioRepository repository;

    public Map<String, Object> meuPerfil(UsuarioSessao usuario) {
        Map<String, Object> dados = new LinkedHashMap<>();
        if (usuario.id() != ContasService.ADMIN_TECNICO_ID) {
            var linhas = repository.consultarFuncionario(usuario.id());
            if (linhas.isEmpty()) throw new AuthException(404, "Funcionário não encontrado.");
            var f = linhas.getFirst();
            for (String campo : List.of("telefone", "celular", "cargo", "setor", "ativo"))
                dados.put(campo, f.get(campo));
            dados.put(
                    "dataAdmissao",
                    f.get("admissao") == null ? null : f.get("admissao").toString());
            dados.put("tipoContrato", f.get("contrato"));
        } else {
            dados.put("ativo", true);
        }
        dados.put("nome", usuario.nome());
        dados.put("email", usuario.email());
        dados.put("matricula", usuario.id().toString());
        dados.put(
                "perfil",
                usuario.administrador() ? "Administrador" : String.join(", ", usuario.perfis()));
        dados.put("acessoAtual", usuario.autenticadoEm().toString());
        return dados;
    }
}
