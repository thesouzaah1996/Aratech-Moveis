package com.aratech.moveis.documentacao;


import com.aratech.moveis.security.authorization.Permissoes;
import com.aratech.moveis.security.validation.LoginCorporativo;
import com.aratech.moveis.suporte.chamados.enums.PrioridadeTi;
import com.aratech.moveis.suporte.chamados.enums.TipoChamadoTi;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.*;
import io.swagger.v3.oas.models.responses.*;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Configuration
public class ApiDocumentacaoConfig {
    static final String CSRF = "X-XSRF-TOKEN";

    @Bean
    public OpenAPI aratechOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Aratech Móveis — API dos módulos")
                                .version("1.0")
                                .description(
                                        """
Documentação dos endpoints realmente implementados no monólito.

**Autenticação:** cookie de sessão `ARATECHSESSION`, não Bearer/JWT. Entre pela aplicação ou execute POST `/aratech/auth/entrar` na seção Autenticação.
Antes de qualquer escrita, obtenha GET `/aratech/auth/csrf` e envie o token em `X-XSRF-TOKEN`, mantendo o mesmo cookie.
O botão Executar da interface personalizada renova o CSRF automaticamente; login/logout trocam o contexto de segurança.
Cookies HttpOnly são geridos pelo navegador, não devem ser colados manualmente em Authorize.
A sessão expira após 15 minutos sem requisições; a atividade renova esse prazo. O limite absoluto é de 8 horas.
Logout, alteração de credenciais e reinicialização do backend também podem encerrar a sessão.

**Permissões:** cada operação informa a permissão exigida. Administradores passam pela autorização dos módulos,
mas senhas temporárias continuam bloqueadas até a troca e as regras de negócio são sempre validadas.
Permissões detalhadas usam SETOR_TELA.ACAO. A permissão ampla SETOR.ACAO preserva acesso às telas daquele setor; para restringir uma pessoa, atribua somente as permissões detalhadas necessárias.
Consultas e marcações de meu ponto utilizam exclusivamente a identidade da sessão validada. Marcação de ponto não solicita ID, e-mail ou senha e usa o relógio do servidor em America/Sao_Paulo. A conta técnica de administração não possui vínculo de funcionário nem ponto; os endpoints de marcação e consulta de meu ponto retornam 403 para essa conta. Correções de ponto e escala exigem perfil # e RH_PONTO.ALTERAR, inclusive para administradores.

**Formatos:** datas `YYYY-MM-DD`, competência `YYYY-MM`, horário de ponto `HH:mm` e instantes com fuso (`date-time`).
**Embalagem:** EMBALAGEM_LOTES recebe lotes CORTADO com confirmação de prontidão e roteiro sequencial; EMBALAGEM_ESTEIRAS cadastra postos, inicia, pausa, retoma e conclui operações. Capacidade é de 1 a 20 operações simultâneas por esteira, incluindo pausadas. Etapa futura não inicia antes da anterior. Encerramento da última etapa exige volumes e conferências de peças, proteção e identificação. Não presume montagem/acabamento prontos a partir do corte, não controla máquinas, estoque ou expedição. Versão do lote é exigida em todas as operações; conflito retorna 409. Pausa e cancelamento exigem motivo de 5 a 500 caracteres.
**Lotes setoriais:** `/aratech/pcp/lotes/producao` planeja por variante e gera prévia calculada. A primeira liberação congela revisão, roteiro e totais para os setores restantes. Antes de qualquer ordem, atualizar a prévia é permitido explicitamente. Seleção parcial deixa setores pendentes; transação, bloqueio e UNIQUE(lote_id,setor_id) impedem duplicidade. Cada fila verifica sua permissão e retorna somente suas linhas. Destinos com exigeMedidas recebem e validam especificações técnicas também no consumo direto. Consumo por componente não se multiplica pelo número de operações. Separação só baixa estoque após confirmação pelo ProdutoService, com conversão explícita e exata para a unidade inteira existente, sem arredondar. Ordens de origem não concluem com lotes pendentes. Não há otimizador de chapas.
**Fabricação legada:** Administração → Guarda-roupas cadastra fichas e fotos por componente. PCP → Lotes planeja e envia ao Corte, preservando a revisão do modelo. Corte → Lotes recebe e registra início e conclusão do corte. Medidas de peças em mm, quantidades da ficha por móvel; multiplicar pelas unidades do lote. Fotos JPEG/PNG autenticadas de até 5 MB, quatro por componente/capa. A ficha não calcula encaixe otimizado de chapas nem programa de máquina. A consulta de lotes de produção concluída foi movida de `/aratech/administracao/lotes` para `/aratech/pcp/lotes`, com PCP_LOTES.CONSULTAR. Pedidos comerciais antigos permanecem em APIs legadas, sem conversão automática para Corte.
Paginação inicia em zero; o tamanho aceito é de 1 a 100, usando o padrão de cada endpoint.
`chaveOperacao` é um UUID criado pelo cliente para identificar uma operação, não um token de autenticação.

**Respostas:** os módulos mantêm seus envelopes existentes; consulte o schema de cada operação.
Erros de validação podem conter `erros` por campo; segurança usa `status`, `codigo` e `mensagem`.
**Executar grava dados reais:** criar, editar, movimentar, autorizar e finalizar têm os mesmos efeitos da aplicação.
Páginas sem controller implementado não aparecem como se fossem APIs disponíveis. O upload legado é documentado como bloqueado.
"""))
                .servers(
                        List.of(
                                new Server()
                                        .url("/")
                                        .description("Mesmo servidor da documentação")))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "Sessao",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.APIKEY)
                                                .in(SecurityScheme.In.COOKIE)
                                                .name("ARATECHSESSION")
                                                .description(
                                                        "Cookie HttpOnly fornecido pelo login e"
                                                                + " enviado pelo navegador."))
                                .addSecuritySchemes(
                                        "CSRF",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.APIKEY)
                                                .in(SecurityScheme.In.HEADER)
                                                .name(CSRF)
                                                .description(
                                                        "Token obtido de GET /aratech/auth/csrf. A"
                                                                + " interface personalizada renova"
                                                                + " automaticamente antes de"
                                                                + " escrever.")));
    }

    @Bean
    public OpenApiCustomizer regrasEAutenticacao() {
        return api -> {
            api.getComponents()
                    .addSchemas(
                            "ErroApi",
                            new ObjectSchema()
                                    .description(
                                            "Campos de erro variam conforme o módulo e o tipo de"
                                                    + " falha.")
                                    .addProperty("status", new IntegerSchema())
                                    .addProperty("codigo", new StringSchema())
                                    .addProperty("mensagem", new StringSchema())
                                    .addProperty(
                                            "erros",
                                            new MapSchema()
                                                    .additionalProperties(new StringSchema())));
            adicionarLoginESaida(api);
            api.getPaths()
                    .forEach(
                            (path, item) ->
                                    item.readOperationsMap()
                                            .forEach(
                                                    (verbo, op) -> {
                                                        String metodo = verbo.name();
                                                        boolean escrita =
                                                                !Set.of("GET", "HEAD", "OPTIONS")
                                                                        .contains(metodo);
                                                        boolean publico =
                                                                path.equals("/aratech/auth/csrf")
                                                                        || path.equals(
                                                                                "/aratech/auth/entrar")
                                                                        || path.equals(
                                                                                "/aratech/auth/sair");
                                                        op.setSecurity(
                                                                publico
                                                                        ? (escrita
                                                                                ? List.of(
                                                                                        new SecurityRequirement()
                                                                                                .addList(
                                                                                                        "CSRF"))
                                                                                : List.of())
                                                                        : List.of(
                                                                                escrita
                                                                                        ? new SecurityRequirement()
                                                                                                .addList(
                                                                                                        "Sessao")
                                                                                                .addList(
                                                                                                        "CSRF")
                                                                                        : new SecurityRequirement()
                                                                                                .addList(
                                                                                                        "Sessao")));
                                                        String permissao =
                                                                Permissoes.necessaria(
                                                                        path.replaceAll(
                                                                                "\\{[^}]+}", "1"),
                                                                        metodo);
                                                        if (path.startsWith(
                                                                "/aratech/financeiro/titulos/{tipo}")) {
                                                            permissao =
                                                                    String.join(
                                                                            " OU ",
                                                                            List.of(
                                                                                            "pagar",
                                                                                            "receber",
                                                                                            "notas")
                                                                                    .stream()
                                                                                    .map(
                                                                                            tipo ->
                                                                                                    Permissoes
                                                                                                            .necessaria(
                                                                                                                    path.replace(
                                                                                                                                    "{tipo}",
                                                                                                                                    tipo)
                                                                                                                            .replace(
                                                                                                                                    "{id}",
                                                                                                                                    "1"),
                                                                                                                    metodo))
                                                                                    .toList());
                                                            op.setDescription(
                                                                    Objects.toString(
                                                                                    op
                                                                                            .getDescription(),
                                                                                    "")
                                                                            + " O parâmetro tipo"
                                                                            + " define a permissão:"
                                                                            + " pagar →"
                                                                            + " FINANCEIRO_PAGAR,"
                                                                            + " receber →"
                                                                            + " FINANCEIRO_RECEBER,"
                                                                            + " notas →"
                                                                            + " FINANCEIRO_NOTAS;"
                                                                            + " não libera as"
                                                                            + " outras telas.");
                                                        }
                                                        String regra =
                                                                permissao == null
                                                                        ? "Requer sessão"
                                                                              + " autenticada; a"
                                                                              + " identidade é"
                                                                              + " obtida do cookie."
                                                                        : "Permissão exigida: **"
                                                                                + permissao
                                                                                + "** (ou"
                                                                                + " administrador).";
                                                        if (metodo.equals("PUT") && (path.equals("/aratech/rh/ponto/{id}/{data}") || path.equals("/aratech/rh/ponto/{id}/escala")))
                                                            regra = "Exige perfil # e RH_PONTO.ALTERAR, inclusive para administradores.";
                                                        if (publico) regra = "Não exige login.";
                                                        if (path.equals(
                                                                        "/aratech/assistencia/lotes")
                                                                && !escrita)
                                                            regra =
                                                                    "Permissão:"
                                                                        + " **ASSISTENCIA_LOTES.CONSULTAR"
                                                                        + " ou CARREGAMENTO_ASSISTENCIA.CONSULTAR**,"
                                                                        + " ou permissão ampla"
                                                                        + " correspondente, ou"
                                                                        + " administrador.";
                                                        if (path.equals("/bucket"))
                                                            regra =
                                                                    "**Bloqueado pela política de"
                                                                            + " segurança atual,"
                                                                            + " inclusive para"
                                                                            + " administradores.**";
                                                        op.setDescription(
                                                                Objects.toString(
                                                                                op.getDescription(),
                                                                                "")
                                                                        + "\n\n"
                                                                        + regra
                                                                        + (escrita
                                                                                ? " Escrita exige"
                                                                                      + " **X-XSRF-TOKEN**"
                                                                                      + " vinculado"
                                                                                      + " à sessão."
                                                                                : ""));
                                                        op.addExtension(
                                                                "x-permissao",
                                                                path.equals("/bucket")
                                                                        ? "BLOQUEADO"
                                                                        : permissao == null
                                                                                ? (publico
                                                                                        ? "PUBLICO"
                                                                                        : "SESSAO")
                                                                                : permissao);
                                                        if (op.getResponses() == null)
                                                            op.setResponses(new ApiResponses());
                                                        if (!publico)
                                                            erro(
                                                                    op,
                                                                    "401",
                                                                    "Sessão ausente, expirada,"
                                                                        + " revogada ou funcionário"
                                                                        + " inativo.");
                                                        erro(
                                                                op,
                                                                "403",
                                                                "Sem permissão, senha temporária"
                                                                    + " ainda não trocada ou CSRF"
                                                                    + " inválido/ausente.");
                                                        erro(
                                                                op,
                                                                "400",
                                                                "Dados, parâmetros ou formatos"
                                                                    + " inválidos. Consulte as"
                                                                    + " regras da operação e erros"
                                                                    + " por campo quando"
                                                                    + " enviados.");
                                                        if (path.contains("{"))
                                                            erro(
                                                                    op,
                                                                    "404",
                                                                    "Registro não encontrado ou"
                                                                        + " vínculo de funcionário"
                                                                        + " inexistente.");
                                                        if (escrita) {
                                                            erro(
                                                                    op,
                                                                    "409",
                                                                    "Conflito de estado,"
                                                                        + " duplicidade, saldo ou"
                                                                        + " versão concorrente,"
                                                                        + " conforme as regras do"
                                                                        + " módulo.");
                                                            erro(
                                                                    op,
                                                                    "415",
                                                                    "Content-Type incompatível com"
                                                                            + " o contrato da"
                                                                            + " operação.");
                                                        }
                                                        if (path.equals("/aratech/auth/entrar")
                                                                || path.equals(
                                                                        "/aratech/auth/senha")) {
                                                            erro(
                                                                    op,
                                                                    "429",
                                                                    "Limite de tentativas atingido."
                                                                        + " Aguarde a janela de 15"
                                                                        + " minutos.");
                                                            op.getResponses()
                                                                    .get("429")
                                                                    .addHeaderObject(
                                                                            "Retry-After",
                                                                            new io.swagger.v3.oas
                                                                                            .models
                                                                                            .headers
                                                                                            .Header()
                                                                                    .description(
                                                                                            "Quando"
                                                                                                + " enviado,"
                                                                                                + " espera"
                                                                                                + " em segundos.")
                                                                                    .schema(
                                                                                            new IntegerSchema()
                                                                                                    .example(
                                                                                                            900)));
                                                        }
                                                        if (path.equals("/aratech/rh/funcionarios")
                                                                || path.matches(
                                                                        "/aratech/rh/funcionarios/\\{[^}]+}"))
                                                            if (escrita) {
                                                                erro(
                                                                        op,
                                                                        "413",
                                                                        "Arquivo acima de 5 MB ou"
                                                                            + " requisição acima de"
                                                                            + " 21 MB; validação do"
                                                                            + " conjunto de"
                                                                            + " documentos também"
                                                                            + " limita a 20 MB.");
                                                                erro(
                                                                        op,
                                                                        "502",
                                                                        "Falha no armazenamento dos"
                                                                                + " documentos.");
                                                            }
                                                        parametros(op);
                                                        if (criacao201(path, metodo)
                                                                && op.getResponses()
                                                                        .containsKey("200")) {
                                                            var resposta =
                                                                    op.getResponses().remove("200");
                                                            resposta.setDescription(
                                                                    "Cadastro criado (ou a mesma"
                                                                        + " operação idempotente já"
                                                                        + " registrada, quando"
                                                                        + " aplicável).");
                                                            op.getResponses()
                                                                    .addApiResponse(
                                                                            "201", resposta);
                                                        }
                                                    }));
            enriquecerSchemas(api);
        };
    }

    private static void erro(Operation op, String status, String descricao) {
        if (op.getResponses().containsKey(status)) return;
        op.getResponses()
                .addApiResponse(
                        status,
                        new ApiResponse()
                                .description(descricao)
                                .content(
                                        new Content()
                                                .addMediaType(
                                                        "application/json",
                                                        new MediaType()
                                                                .schema(
                                                                        new Schema<>()
                                                                                .$ref(
                                                                                        "#/components/schemas/ErroApi")))));
    }

    private static boolean criacao201(String path, String method) {
        return method.equals("POST")
                && Set.of(
                                "/aratech/almoxarifado/categorias",
                                "/aratech/almoxarifado/fornecedores",
                                "/aratech/almoxarifado/produtos",
                                "/aratech/manutencao/estoque",
                                "/aratech/assistencia/estoque-pecas",
                                "/aratech/manutencao/mecanicos",
                                "/aratech/manutencao/chamados",
                                "/aratech/manutencao/solicitacoes-pecas",
                                "/aratech/suporte/chamados",
                                "/aratech/portaria/chegadas",
                                "/aratech/rh/perfis",
                                "/aratech/rh/funcionarios")
                        .contains(path);
    }

    private static void parametros(Operation op) {
        if (op.getParameters() == null) return;
        for (var p : op.getParameters()) {
            if (p.getSchema() == null) continue;
            switch (p.getName()) {
                case "pagina" -> {
                    p.setDescription("Índice da página, começando em zero.");
                    p.getSchema().setMinimum(BigDecimal.ZERO);
                }
                case "tamanho" -> {
                    p.setDescription(
                            "Itens por página: entre 1 e 100. Veja o valor padrão deste endpoint.");
                    p.getSchema().setMinimum(BigDecimal.ONE);
                    p.getSchema().setMaximum(BigDecimal.valueOf(100));
                }
                case "mes" -> {
                    p.setDescription("Competência no formato YYYY-MM, anos de 1900 a 2100.");
                    p.setExample("2026-11");
                    p.getSchema().setPattern("^(19[0-9]{2}|20[0-9]{2}|2100)-(0[1-9]|1[0-2])$");
                }
                case "data" -> {
                    p.setDescription(
                            "Data no formato YYYY-MM-DD; no ponto, gravação não aceita data"
                                    + " futura.");
                    p.setExample("2026-11-03");
                }
                case "busca", "nome" ->
                        p.setDescription(
                                "Texto de busca opcional, conforme os campos e regras descritos"
                                        + " nesta operação.");
                case "id", "perfilId" -> {
                    p.setDescription("Identificador persistido do recurso indicado na URL.");
                    p.setExample(1);
                }
                case "ativo" ->
                        p.setDescription(
                                "Quando informado, limita aos cadastros ativos (true) ou inativos"
                                        + " (false).");
                case "abaixoMinimo" ->
                        p.setDescription(
                                "true para listar somente peças com saldo no mínimo configurado ou"
                                        + " abaixo dele.");
                default -> {}
            }
        }
    }

    private static void adicionarLoginESaida(OpenAPI api) {
        Schema<?> sessao = new ObjectSchema();
        var sessaoItem = api.getPaths().get("/aratech/auth/sessao");
        if (sessaoItem != null && sessaoItem.getGet() != null) {
            var sucesso = sessaoItem.getGet().getResponses().get("200");
            if (sucesso != null
                    && sucesso.getContent() != null
                    && sucesso.getContent().get("application/json") != null)
                sessao = sucesso.getContent().get("application/json").getSchema();
        }
        var form =
                new ObjectSchema()
                        .addProperty(
                                "emailCorporativo",
                                new StringSchema()
                                        .pattern(LoginCorporativo.REGEX)
                                        .maxLength(150)
                                        .description(
                                                "Login obrigatório no domínio exato @aratechmoveis,"
                                                    + " inclusive administrador técnico. Até 64"
                                                    + " caracteres antes do @; sem espaços ou"
                                                    + " pontos consecutivos. Maiúsculas"
                                                    + " normalizadas. Formato inválido retorna 401"
                                                    + " genérico.")
                                        .example("admin@aratechmoveis"))
                        .addProperty(
                                "senha",
                                new StringSchema()
                                        .format("password")
                                        .writeOnly(true)
                                        .description(
                                                "Senha atual ou temporária. Não informar senhas"
                                                        + " reais em exemplos compartilhados."))
                        .required(List.of("emailCorporativo", "senha"));
        var login =
                new Operation()
                        .operationId("autenticacaoEntrar")
                        .summary("Entrar e criar sessão autenticada")
                        .tags(List.of("Autenticação"))
                        .description(
                                "Login implementado no filtro do Spring Security. Envie"
                                    + " application/x-www-form-urlencoded, não JSON. Exige CSRF"
                                    + " obtido com o mesmo cookie. Resposta 200 inclui trocarSenha;"
                                    + " se true, troque a senha antes dos módulos. Até 10"
                                    + " tentativas por identificador (contagem limpa no sucesso) e"
                                    + " 50 por IP em 15 minutos; erro de credencial é genérico para"
                                    + " não revelar cadastros.")
                        .requestBody(
                                new RequestBody()
                                        .required(true)
                                        .content(
                                                new Content()
                                                        .addMediaType(
                                                                "application/x-www-form-urlencoded",
                                                                new MediaType().schema(form))))
                        .responses(
                                new ApiResponses()
                                        .addApiResponse(
                                                "200",
                                                new ApiResponse()
                                                        .description(
                                                                "Sessão autenticada; navegador"
                                                                        + " recebe novo cookie"
                                                                        + " HttpOnly.")
                                                        .content(
                                                                new Content()
                                                                        .addMediaType(
                                                                                "application/json",
                                                                                new MediaType()
                                                                                        .schema(
                                                                                                sessao)))));
        erro(login, "401", "Usuário, senha ou situação da conta não permitem autenticação.");
        api.path("/aratech/auth/entrar", new PathItem().post(login));
        api.path(
                "/aratech/auth/sair",
                new PathItem()
                        .post(
                                new Operation()
                                        .operationId("autenticacaoSair")
                                        .summary("Encerrar sessão")
                                        .tags(List.of("Autenticação"))
                                        .description(
                                                "Implementado no filtro de logout. Exige CSRF;"
                                                    + " invalida a sessão e remove o cookie. Não"
                                                    + " recebe corpo JSON.")
                                        .responses(
                                                new ApiResponses()
                                                        .addApiResponse(
                                                                "200",
                                                                new ApiResponse()
                                                                        .description(
                                                                                "Sessão encerrada.")
                                                                        .content(
                                                                                new Content()
                                                                                        .addMediaType(
                                                                                                "application/json",
                                                                                                new MediaType()
                                                                                                        .schema(
                                                                                                                new ObjectSchema()
                                                                                                                        .addProperty(
                                                                                                                                "mensagem",
                                                                                                                                new StringSchema()
                                                                                                                                        .example(
                                                                                                                                                "Sessão"
                                                                                                                                                    + " encerrada.")))))))));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void enriquecerSchemas(OpenAPI api) {
        if (api.getComponents().getSchemas() == null) return;
        for (var entry : api.getComponents().getSchemas().entrySet()) {
            Schema schema = entry.getValue();
            if (schema.getProperties() == null) continue;
            Map<String, Schema> props = schema.getProperties();
            props.forEach(
                    (nome, p) -> {
                        if (p.getDescription() != null) return;
                        String descricao =
                                switch (nome) {
                                    case "chaveOperacao" ->
                                            "UUID gerado pelo cliente para idempotência. Repetir o"
                                                + " mesmo conteúdo e chave não duplica a operação;"
                                                + " dados diferentes geram conflito.";
                                    case "horarios" ->
                                            "Exatamente seis strings: entrada1, saída1, entrada2,"
                                                + " saída2, entrada3, saída3. HH:mm ou vazio. Ordem"
                                                + " crescente no mesmo dia, sem lacunas"
                                                + " intermediárias.";
                                    case "versao" ->
                                            "Versão de concorrência recebida na consulta. Para"
                                                    + " criação de ponto ou pedido manual, envie 0;"
                                                    + " versão desatualizada é rejeitada com 409.";
                                    case "motivo" ->
                                            "Justificativa da operação. No controle de ponto,"
                                                    + " obrigatória, com 5 a 500 caracteres; nos"
                                                    + " estoques veja os limites do DTO.";
                                    case "sku" ->
                                            "Identificador único de produto, preservado após o"
                                                    + " cadastro, inclusive entre arquivados.";
                                    case "emailCorporativo" ->
                                            "E-mail usado como login corporativo da aplicação; não"
                                                    + " provisiona caixa postal externa.";
                                    case "senhaTemporaria" ->
                                            "Credencial temporária retornada apenas na criação;"
                                                    + " deve ser entregue ao titular e trocada no"
                                                    + " primeiro login.";
                                    case "novaSenha" ->
                                            "Nova senha de 8 a 128 caracteres, diferente da atual e"
                                                + " aprovada na avaliação local de força. Não há"
                                                + " exigência artificial de tipos de caractere.";
                                    case "senhaAtual" ->
                                            "Senha atual da conta autenticada; obrigatória para"
                                                    + " trocar a senha.";
                                    case "quantidadeMinima" ->
                                            "Limite opcional de reposição. Saldo menor ou igual ao"
                                                + " mínimo dispara pedido em Compras; null desativa"
                                                + " o monitoramento. Produtos usam inteiros; peças,"
                                                + " até três casas decimais.";
                                    case "quantidade" ->
                                            "Quantidade em estoque ou da operação. Produtos usam"
                                                + " inteiros; peças aceitam até três casas"
                                                + " decimais. Movimentações exigem valor positivo.";
                                    case "origemId" ->
                                            "Identificador da solicitação de origem na"
                                                + " administração, usado para evitar duplicação do"
                                                + " lote.";
                                    case "dataEntrada" ->
                                            entry.getKey().contains("LoteAssistencia")
                                                    ? "Data definida no recebimento do lote,"
                                                            + " apresentada como data de saída na"
                                                            + " interface."
                                                    : "Data ou instante de entrada definido pelo"
                                                            + " fluxo do módulo.";
                                    case "trocarSenha" ->
                                            "true indica senha temporária: os módulos ficam"
                                                    + " bloqueados até a troca obrigatória.";
                                    case "ajustado" ->
                                            "No ponto, indica que há um registro persistido para o"
                                                + " dia; atualmente os registros são criados por"
                                                + " correção manual.";
                                    case "acessoAtual" ->
                                            "Instante de autenticação da sessão atual. Não"
                                                + " corresponde a um histórico de último login.";
                                    default -> null;
                                };
                        if (descricao != null) p.setDescription(descricao);
                        if (Set.of("senhaAtual", "novaSenha", "senha").contains(nome)) {
                            p.setFormat("password");
                            p.setWriteOnly(true);
                        }
                        if (nome.equals("horarios")) {
                            p.setMinItems(6);
                            p.setMaxItems(6);
                        }
                    });
        }
        restricoesDosContratos(api);
        respostasExplicitas(api);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void restricoesDosContratos(OpenAPI api) {
        api.getComponents()
                .getSchemas()
                .forEach(
                        (nome, s) -> {
                            if (s.getProperties() == null) return;
                            if (nome.endsWith(".ChegadaDTO")) {
                                s.setRequired(
                                        List.of(
                                                "notaFiscal",
                                                "empresa",
                                                "nomeMotorista",
                                                "placa",
                                                "descricaoCarga",
                                                "setorResponsavel"));
                                campo(s, "notaFiscal")
                                        .setDescription(
                                                "Número de 1 a 9 dígitos diferente de zero, ou"
                                                        + " chave de 44 dígitos com verificador"
                                                        + " válido.");
                                campo(s, "setorResponsavel")
                                        .setEnum(List.of("ALMOXARIFADO", "CARREGAMENTO"));
                                campo(s, "tipoOperacao")
                                        .setEnum(List.of("CARREGAR", "DESCARREGAR"));
                                campo(s, "tipoOperacao")
                                        .setDescription(
                                                "Obrigatório somente para o setor CARREGAMENTO.");
                                campo(s, "chaveOperacao")
                                        .setDescription(
                                                "UUID obrigatório no cadastro; não é obrigatório na"
                                                        + " edição. Reutilizar com dados diferentes"
                                                        + " retorna 409.");
                                texto(s, "empresa", 2, 150);
                                texto(s, "nomeMotorista", 3, 150);
                                texto(s, "descricaoCarga", 3, 1000);
                            }
                            if (nome.endsWith(".ChamadoDTO")) {
                                s.setRequired(
                                        List.of(
                                                "equipamento",
                                                "tipo",
                                                "prioridade",
                                                "solicitante",
                                                "setor",
                                                "descricao",
                                                "chaveOperacao"));
                                campo(s, "tipo").setEnum(List.of("CORRETIVA", "PREVENTIVA"));
                                campo(s, "prioridade").setEnum(List.of("BAIXA", "MEDIA", "ALTA"));
                                texto(s, "equipamento", 2, 150);
                                texto(s, "solicitante", 3, 150);
                                texto(s, "setor", 2, 100);
                                texto(s, "descricao", 3, 3000);
                            }
                            if (nome.endsWith(".SolicitacaoPecaDTO")) {
                                s.setRequired(
                                        List.of(
                                                "nomePeca",
                                                "quantidade",
                                                "unidade",
                                                "equipamento",
                                                "finalidade",
                                                "prioridade",
                                                "solicitante",
                                                "setor",
                                                "observacoes",
                                                "chaveOperacao"));
                                campo(s, "codigo")
                                        .setDescription(
                                                "Código opcional da peça, até 100 caracteres.");
                                campo(s, "finalidade")
                                        .setEnum(
                                                List.of(
                                                        "CORRETIVA",
                                                        "PREVENTIVA",
                                                        "MELHORIA",
                                                        "ESTOQUE"));
                                campo(s, "prioridade").setEnum(List.of("BAIXA", "MEDIA", "ALTA"));
                                campo(s, "unidade")
                                        .setEnum(List.of("un", "kg", "L", "m", "cx", "par"));
                                texto(s, "nomePeca", 2, 150);
                                texto(s, "equipamento", 2, 150);
                                texto(s, "observacoes", 3, 3000);
                                texto(s, "solicitante", 3, 150);
                                texto(s, "setor", 2, 100);
                            }
                            if (nome.endsWith(".ChamadoTiDTO")) {
                                s.setRequired(
                                        List.of(
                                                "tipo",
                                                "prioridade",
                                                "titulo",
                                                "solicitante",
                                                "setor",
                                                "descricao",
                                                "chaveOperacao"));
                                campo(s, "tipo")
                                        .setEnum(
                                                Arrays.stream(TipoChamadoTi.values())
                                                        .map(Object::toString)
                                                        .toList());
                                campo(s, "prioridade")
                                        .setEnum(
                                                Arrays.stream(PrioridadeTi.values())
                                                        .map(Object::toString)
                                                        .toList());
                                campo(s, "contato")
                                        .setDescription(
                                                "Opcional: ramal de 2 a 6 dígitos ou telefone com"
                                                        + " DDD de 10/11 dígitos.");
                                texto(s, "titulo", 3, 150);
                                texto(s, "solicitante", 3, 150);
                                texto(s, "setor", 2, 100);
                                texto(s, "descricao", 3, 3000);
                                texto(s, "tentativasSolucao", 0, 2000);
                            }
                            if (nome.endsWith(".EdicaoPontoDTO")) {
                                s.setRequired(List.of("horarios", "motivo", "versao"));
                                texto(s, "motivo", 5, 500);
                                campo(s, "versao").setMinimum(BigDecimal.ZERO);
                                campo(s, "horarios").setMinItems(6);
                                campo(s, "horarios").setMaxItems(6);
                                if (campo(s, "horarios").getItems() != null)
                                    campo(s, "horarios")
                                            .getItems()
                                            .setPattern("^$|^([01][0-9]|2[0-3]):[0-5][0-9]$");
                            }
                            if (nome.endsWith("security.dto.Alterar")) {
                                s.setRequired(List.of("senhaAtual", "novaSenha"));
                                texto(s, "novaSenha", 8, 128);
                                texto(s, "senhaAtual", 1, 128);
                            }
                            if (nome.endsWith("security.dto.EmailCorporativo")) {
                                s.setRequired(List.of("emailCorporativo"));
                                texto(s, "emailCorporativo", 1, 150);
                                campo(s, "emailCorporativo").setPattern(LoginCorporativo.REGEX);
                            }
                            if (nome.endsWith("perfis.dto.Atribuicao")) {
                                s.setRequired(List.of("nome"));
                                texto(s, "nome", 3, 100);
                            }
                        });
    }

    @SuppressWarnings("rawtypes")
    private static Schema campo(Schema schema, String campo) {
        return (Schema) schema.getProperties().get(campo);
    }

    @SuppressWarnings("rawtypes")
    private static void texto(Schema schema, String nome, int minimo, int maximo) {
        var c = campo(schema, nome);
        if (c != null) {
            c.setMinLength(minimo);
            c.setMaxLength(maximo);
        }
    }

    private static void respostasExplicitas(OpenAPI api) {
        sucesso(
                api,
                "/aratech/auth/csrf",
                PathItem.HttpMethod.GET,
                new ObjectSchema()
                        .addProperty("headerName", new StringSchema().example(CSRF))
                        .addProperty(
                                "token",
                                new StringSchema()
                                        .description(
                                                "Token da sessão; valor obtido em tempo de"
                                                        + " execução.")));
        var temporaria =
                new StringSchema()
                        .description(
                                "Senha temporária aleatória. Não é recuperável posteriormente.");
        sucesso(
                api,
                "/aratech/auth/usuarios/{id}/acesso",
                PathItem.HttpMethod.POST,
                new ObjectSchema().addProperty("senhaTemporaria", temporaria));
        sucesso(
                api,
                "/aratech/auth/usuarios/{id}/email-corporativo",
                PathItem.HttpMethod.POST,
                new ObjectSchema()
                        .addProperty("emailCorporativo", new StringSchema().format("email"))
                        .addProperty("senhaTemporaria", temporaria));
        var perfil = new ObjectSchema();
        for (String p :
                List.of(
                        "nome",
                        "email",
                        "matricula",
                        "telefone",
                        "celular",
                        "cargo",
                        "setor",
                        "tipoContrato",
                        "perfil")) perfil.addProperty(p, new StringSchema());
        perfil.addProperty("dataAdmissao", new StringSchema().format("date"));
        perfil.addProperty("ativo", new BooleanSchema());
        perfil.addProperty("acessoAtual", new StringSchema().format("date-time"));
        sucesso(api, "/aratech/auth/meu-perfil", PathItem.HttpMethod.GET, perfil);
    }

    private static void sucesso(
            OpenAPI api, String path, PathItem.HttpMethod metodo, Schema<?> schema) {
        var item = api.getPaths().get(path);
        if (item == null) return;
        var op = item.readOperationsMap().get(metodo);
        if (op == null || op.getResponses().get("200") == null) return;
        op.getResponses()
                .get("200")
                .setContent(
                        new Content()
                                .addMediaType("application/json", new MediaType().schema(schema)));
    }
}
