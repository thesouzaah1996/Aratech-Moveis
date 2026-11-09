package com.aratech.moveis.security.authorization;

import com.aratech.moveis.security.dto.UsuarioSessao;

import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

public final class Permissoes {
    private Permissoes() {}

    public static final List<String> MODULOS =
            List.of(
                    "ALMOXARIFADO",
                    "PORTARIA",
                    "CARREGAMENTO",
                    "MANUTENCAO",
                    "ASSISTENCIA",
                    "RH",
                    "PCP",
                    "COMPRAS",
                    "FINANCEIRO",
                    "ADMINISTRACAO",
                    "SUPORTE",
                    "COMERCIAL",
                    "CORTE",
                    "EMBALAGEM", "PRODUCAO");
    public static final Map<String, String> RECURSOS =
            Map.ofEntries(
                    Map.entry("almoxarifado/produtos", "ALMOXARIFADO_ESTOQUE"),
                    Map.entry("almoxarifado/categorias", "ALMOXARIFADO_CATEGORIAS"),
                    Map.entry("almoxarifado/fornecedores", "ALMOXARIFADO_FORNECEDORES"),
                    Map.entry("almoxarifado/recebimentos", "ALMOXARIFADO_RECEBIMENTO"),
                    Map.entry("almoxarifado/chegadas", "ALMOXARIFADO_RECEBIMENTO"),
                    Map.entry("almoxarifado/pedidos", "ALMOXARIFADO_PEDIDOS"),
                    Map.entry("portaria/chegadas", "PORTARIA_CONTROLE"),
                    Map.entry("manutencao/chamados", "MANUTENCAO_CHAMADOS"),
                    Map.entry("manutencao/fila", "MANUTENCAO_FILA"),
                    Map.entry("manutencao/estoque", "MANUTENCAO_ESTOQUE"),
                    Map.entry("manutencao/mecanicos", "MANUTENCAO_MECANICOS"),
                    Map.entry("manutencao/solicitacoes-pecas", "MANUTENCAO_PECAS"),
                    Map.entry("manutencao/pedidos", "MANUTENCAO_PEDIDOS"),
                    Map.entry("assistencia/lotes", "ASSISTENCIA_LOTES"),
                    Map.entry("assistencia/estoque-pecas", "ASSISTENCIA_ESTOQUE"),
                    Map.entry("carregamento/chegadas", "CARREGAMENTO_CHEGADAS"),
                    Map.entry("carregamento/expedicao", "CARREGAMENTO_EXPEDICAO"),
                    Map.entry("rh/funcionarios", "RH_FUNCIONARIOS"),
                    Map.entry("rh/ponto", "RH_PONTO"),
                    Map.entry("rh/afastamentos", "RH_AFASTAMENTOS"),
                    Map.entry("rh/solicitacoes", "RH_SOLICITACOES"),
                    Map.entry("pcp/ordens", "PCP_ORDENS"),
                    Map.entry("pcp/lotes", "PCP_LOTES"),
                    Map.entry("corte/lotes", "CORTE_LOTES"),
                    Map.entry("corte/ordens", "CORTE_LOTES"),
                    Map.entry("almoxarifado/separacoes", "ALMOXARIFADO_REQUISICOES"),
                    Map.entry("producao/furacao", "PRODUCAO_FURACAO"),
                    Map.entry("producao/montagem", "PRODUCAO_MONTAGEM"),
                    Map.entry("embalagem/ordens", "EMBALAGEM_LOTES"),
                    Map.entry("embalagem/lotes", "EMBALAGEM_LOTES"),
                    Map.entry("embalagem/esteiras", "EMBALAGEM_ESTEIRAS"),
                    Map.entry("pcp/programacao", "PCP_PROGRAMACAO"),
                    Map.entry("pcp/linhas", "PCP_CAPACIDADE"),
                    Map.entry("pcp/capacidade", "PCP_CAPACIDADE"),
                    Map.entry("pcp/indicadores", "PCP_INDICADORES"),
                    Map.entry("comercial/pedidos", "COMERCIAL_PEDIDOS"),
                    Map.entry("pcp/requisicoes", "PCP_REQUISICOES"),
                    Map.entry("almoxarifado/requisicoes", "ALMOXARIFADO_REQUISICOES"),
                    Map.entry("compras/pedidos", "COMPRAS_PEDIDOS"),
                    Map.entry("compras/cotacoes", "COMPRAS_COTACOES"),
                    Map.entry("financeiro/titulos/pagar", "FINANCEIRO_PAGAR"),
                    Map.entry("financeiro/titulos/receber", "FINANCEIRO_RECEBER"),
                    Map.entry("financeiro/titulos/notas", "FINANCEIRO_NOTAS"),
                    Map.entry("financeiro/caixa", "FINANCEIRO_CAIXA"),
                    Map.entry("administracao/auditoria", "ADMINISTRACAO_AUDITORIA"),
                    Map.entry("administracao/relatorios", "ADMINISTRACAO_RELATORIOS"),
                    Map.entry("administracao/parametros", "ADMINISTRACAO_PARAMETROS"),
                    Map.entry("administracao/lotes", "ADMINISTRACAO_LOTES"),
                    Map.entry("administracao/guarda-roupas", "ADMINISTRACAO_GUARDA_ROUPAS"),
                    Map.entry("suporte/chamados", "SUPORTE_CHAMADOS"));
    public static final List<String> ACOES =
            List.of("CONSULTAR", "CADASTRAR", "ALTERAR", "FINALIZAR");

    public static Set<String> catalogo() {
        var permissoes = new TreeSet<String>();
        for (String modulo : MODULOS) for (String acao : ACOES) permissoes.add(modulo + "." + acao);
        for (String modulo : new HashSet<>(RECURSOS.values()))
            for (String acao : ACOES) permissoes.add(modulo + "." + acao);
        for (String acao : ACOES) permissoes.add("CARREGAMENTO_ASSISTENCIA." + acao);
        permissoes.add("ACESSOS.GERENCIAR");
        return Collections.unmodifiableSet(permissoes);
    }

    public static String necessaria(HttpServletRequest request) {
        return necessaria(caminho(request), request.getMethod());
    }

    public static String necessaria(String path, String method) {
        if (path.startsWith("/aratech/rh/perfis") || path.startsWith("/aratech/auth/usuarios"))
            return "ACESSOS.GERENCIAR";
        String modulo = null;
        for (String candidato : MODULOS) {
            String segmento = candidato.equals("RH") ? "rh" : candidato.toLowerCase(Locale.ROOT);
            if (path.equals("/aratech/" + segmento)
                    || path.startsWith("/aratech/" + segmento + "/")) {
                modulo = candidato;
                break;
            }
        }
        if (modulo == null) return null;
        // Os mesmos lotes são acompanhados por assistência e carregamento.
        if (path.matches("/aratech/assistencia/lotes/[0-9]+/carregar"))
            return "CARREGAMENTO_ASSISTENCIA.FINALIZAR";
        if (path.equals("/aratech/assistencia/lotes") && method.equals("POST"))
            return "ADMINISTRACAO_LOTES.CADASTRAR";
        if (path.matches("/aratech/administracao/guarda-roupas/[0-9]+/fotos") && method.equals("POST"))
            return "ADMINISTRACAO_GUARDA_ROUPAS.ALTERAR";
        if (path.matches("/aratech/embalagem/esteiras/[0-9]+/etapas/[0-9]+/(pausar|retomar)") && method.equals("POST"))
            return "EMBALAGEM_ESTEIRAS.ALTERAR";
        if (path.matches("/aratech/pcp/lotes/producao/[0-9]+/(liberar|revisar)") && method.equals("POST"))
            return "PCP_LOTES.ALTERAR";
        String relativo = path.replaceFirst("^/aratech/", "");
        for (var recurso : RECURSOS.entrySet())
            if (relativo.equals(recurso.getKey()) || relativo.startsWith(recurso.getKey() + "/")) {
                modulo = recurso.getValue();
                break;
            }
        String acao =
                switch (method) {
                    case "GET", "HEAD" -> "CONSULTAR";
                    case "POST" ->
                            path.matches(
                                            ".*/(finalizar|concluir|entrada|autorizar|carregar|aprovar|reprovar|receber|baixar|confirmar|escolher|atender|devolver)$")
                                    ? "FINALIZAR"
                                    : path.matches(
                                                    ".*/(iniciar|mecanico|entradas|saidas|cancelar|enviar|documentar)$")
                                            ? "ALTERAR"
                                            : "CADASTRAR";
                    case "PUT", "PATCH", "DELETE" -> "ALTERAR";
                    default -> null;
                };
        return acao == null ? null : modulo + "." + acao;
    }

    public static boolean permite(UsuarioSessao usuario, String permissao) {
        if (usuario == null || usuario.trocarSenha() || permissao == null) return false;
        if (usuario.administrador() || usuario.permissoes().contains(permissao)) return true;
        int ponto = permissao.lastIndexOf('.');
        if (ponto < 0) return false;
        String modulo = permissao.substring(0, ponto), acao = permissao.substring(ponto);
        if (modulo.contains("_"))
            return usuario.permissoes().contains(modulo.substring(0, modulo.indexOf('_')) + acao);
        return false;
    }

    public static String caminho(HttpServletRequest request) {
        return request.getServletPath().isEmpty()
                ? request.getRequestURI().substring(request.getContextPath().length())
                : request.getServletPath();
    }
}
