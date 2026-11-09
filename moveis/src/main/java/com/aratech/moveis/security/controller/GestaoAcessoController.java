package com.aratech.moveis.security.controller;

import com.aratech.moveis.security.authorization.Permissoes;
import com.aratech.moveis.security.dto.EmailCorporativo;
import com.aratech.moveis.security.service.GestaoAcessoService;
import com.aratech.moveis.security.service.SenhasService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(
        name = "Segurança — Gestão de acessos",
        description =
                "Exige ACESSOS.GERENCIAR. Gestores não concedem permissões superiores às próprias"
                    + " nem administram credenciais de usuários mais privilegiados. Ações de módulo"
                    + " exigem também CONSULTAR. Novos acessos geram senha temporária aleatória"
                    + " exibida somente na criação, armazenada como hash e com troca obrigatória."
                    + " Não sobrescreve contas existentes. E-mail corporativo é login da aplicação,"
                    + " separado do e-mail pessoal, e não cria caixa postal externa.")
@RestController
@RequestMapping("/aratech/auth/usuarios")
@RequiredArgsConstructor
public class GestaoAcessoController {
    private final GestaoAcessoService gestao;
    private final SenhasService senhas;

    @Operation(
            summary = "Listar catálogo de permissões",
            description =
                    "Retorna somente os identificadores de permissões reconhecidos pela aplicação.")
    @GetMapping("/permissoes")
    public Set<String> catalogo() {
        return Permissoes.catalogo();
    }

    @Operation(
            summary = "Consultar permissões de um perfil",
            description = "O ID deve corresponder a um perfil existente.")
    @GetMapping("/perfis/{id}/permissoes")
    public Set<String> consultar(@PathVariable Long id) {
        return gestao.consultar(id);
    }

    @Operation(
            summary = "Definir permissões do perfil",
            description =
                    "Substitui o conjunto inteiro de permissões do perfil. Envie array de strings;"
                        + " [] remove todas. Não permite nomes de permissão desconhecidos nem"
                        + " concessão superior à do gestor. Alterações valem na próxima requisição"
                        + " do usuário.")
    @PutMapping("/perfis/{id}/permissoes")
    public Set<String> salvar(@PathVariable Long id, @RequestBody Set<String> permissoes) {
        return gestao.salvar(id, permissoes);
    }

    @Operation(
            summary = "Criar acesso com senha temporária",
            description =
                    "Cria credencial para funcionário ativo usando o e-mail cadastrado como login,"
                            + " somente se estiver no domínio @aratechmoveis. Caso contrário, use a"
                            + " criação de login corporativo separado. Retorna senhaTemporaria uma"
                            + " única vez nesta resposta. 409 se já existe conta. O funcionário só"
                            + " acessa módulos depois de trocar a senha.")
    @PostMapping("/{id}/acesso")
    public org.springframework.http.ResponseEntity<Map<String, String>> criarAcesso(
            @PathVariable Long id) {
        gestao.validarConvite(id);
        return org.springframework.http.ResponseEntity.ok()
                .cacheControl(org.springframework.http.CacheControl.noStore())
                .body(Map.of("senhaTemporaria", senhas.criarAcesso(id)));
    }

    @Operation(
            summary = "Criar login corporativo e senha temporária",
            description =
                    "Recebe emailCorporativo no domínio exato @aratechmoveis, até 64 caracteres"
                        + " antes do @ e 150 no total. Rejeita espaços internos, pontos"
                        + " consecutivos e outros domínios com 400. Normaliza maiúsculas. Preserva"
                        + " o e-mail pessoal. Rejeita e-mail já associado a outro funcionário e"
                        + " conta já existente. Retorna emailCorporativo e senhaTemporaria, com"
                        + " Cache-Control: no-store. Entregue a credencial somente ao funcionário.")
    @PostMapping("/{id}/email-corporativo")
    public org.springframework.http.ResponseEntity<Map<String, String>> criarEmail(
            @PathVariable Long id, @RequestBody EmailCorporativo dto) {
        gestao.validarConvite(id);
        String senha = senhas.criarAcessoCorporativo(id, dto.emailCorporativo());
        return org.springframework.http.ResponseEntity.ok()
                .cacheControl(org.springframework.http.CacheControl.noStore())
                .body(
                        Map.of(
                                "emailCorporativo",
                                dto.emailCorporativo().strip().toLowerCase(Locale.ROOT),
                                "senhaTemporaria",
                                senha));
    }

    @Operation(
            summary = "Remover perfil atribuído ao usuário",
            description =
                    "Remove somente o vínculo especificado, preservando os demais. A autorização"
                            + " passa a refletir a remoção na próxima requisição.")
    @DeleteMapping("/{id}/perfis/{perfilId}")
    public void remover(@PathVariable Long id, @PathVariable Long perfilId) {
        gestao.remover(id, perfilId);
    }
}
