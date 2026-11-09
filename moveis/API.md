# Documentação Swagger / OpenAPI

## Embalagem: lotes e esteiras

`EMBALAGEM_LOTES` e `EMBALAGEM_ESTEIRAS` são recursos independentes. Referência de pesquisa, roteiro, capacidade e uso estão em [Embalagem e esteiras](docs/embalagem-esteiras.md).

- `GET/POST /aratech/embalagem/esteiras` consulta o painel/cadastra esteira. `PUT .../{id}` exige versão atual. Capacidade significa 1–20 operações simultâneas, contando pausadas; não se inativa esteira com etapas pendentes nem se reduz capacidade abaixo da ocupação. Código único; conflitos 409.
- `GET /aratech/embalagem/lotes/origens` lista CORTADO sem recebimento não cancelado; `GET .../lotes/esteiras` fornece opções ativas para o roteiro, usando apenas EMBALAGEM_LOTES.CONSULTAR. Não exige acesso às telas de Corte ou Esteiras.
- `GET/POST /aratech/embalagem/lotes` consulta/recebe lote; `GET .../{id}` retorna ficha, etapas e histórico. Recebimento exige `origemId`, `previsao`, `prioridade` (NORMAL/ALTA/URGENTE), `prontoParaEmbalagem=true`, `chaveOperacao` UUID e 1–12 `etapas` com `esteiraId`, `nome` e `instrucoes`. Quantidade/ficha vêm da origem. O operador confirma prontidão após as etapas necessárias, sem presumir que corte equivale a montagem/acabamento. Recebimento duplicado 409; repetição do mesmo UUID/conteúdo devolve resposta original.
- `POST /aratech/embalagem/esteiras/{id}/etapas/{etapa}/iniciar|pausar|retomar|concluir` recebe versão atual do **lote**. Iniciar/pausar/retomar exigem EMBALAGEM_ESTEIRAS.ALTERAR; concluir exige FINALIZAR. Etapas respeitam sequência e capacidade sob bloqueio transacional. Pausar exige `motivo` (5–500 caracteres). Última conclusão exige `volumes` (1–100000), `pecasConferidas`, `protecaoAplicada` e `identificacaoConferida` verdadeiros. Conferências inválidas 400; versão/estado/capacidade em conflito 409.
- `POST /aratech/embalagem/lotes/{id}/cancelar` exige ALTERAR, `{versao,motivo}` e lote ainda AGUARDANDO. Preserva histórico e libera origem para novo recebimento.
- `GET /aratech/embalagem/lotes/modelos/{id}/fotos/{foto}` fornece JPEG/PNG autenticado com EMBALAGEM_LOTES.CONSULTAR, mantendo referências históricas.

Fluxo do lote: AGUARDANDO → EM_EMBALAGEM ↔ PAUSADO → EMBALADO; cancelamento antes do início. O painel atualiza a cada 15 segundos fora de formulários/operações, com prioridade, previsão, operador, tempo ativo e filas prontas/futuras. Não controla máquinas, estoque, expedição nem mede OEE. Escritas mantêm sessão, CSRF, auditoria e verificação de versão.

## Fabricação: Guarda-roupas, PCP e Corte

O acesso principal **Comercial** foi substituído por **Corte → Lotes**. Dados e APIs de encomendas anteriores permanecem como legado, com suas permissões anteriores. **Lotes de produção concluída** foi movido da Administração para PCP: o endpoint antigo `/aratech/administracao/lotes` foi removido e a consulta usa `GET /aratech/pcp/lotes`, com `PCP_LOTES.CONSULTAR`.

- `GET/POST /aratech/administracao/guarda-roupas`, `GET/PUT .../{id}`: catálogo e ficha técnica, com `ADMINISTRACAO_GUARDA_ROUPAS.CONSULTAR`, `CADASTRAR` ou `ALTERAR`. Código único, 1–200 componentes, medidas em mm, quantidades por móvel, materiais, acabamentos, bordas, veio, furações e até 20 atributos livres por componente. Atualizações exigem versão atual; inativar usa PUT com `ativo=false`.
- `POST .../{id}/fotos` exige ALTERAR e multipart `arquivo`, `versao`, `legenda` e `itemChave` opcional (omitido para capa). JPEG/PNG reais até 5 MB, 6000 px por lado e 16 MP, recodificados; quatro fotos por componente/capa. `DELETE .../{id}/fotos/{foto}?versao=...` retira a referência atual sem romper lotes históricos. Upload/retirada incrementam a revisão.
- `GET /aratech/pcp/lotes/modelos` consulta modelos ativos sem exigir acesso administrativo. `GET/POST /aratech/pcp/lotes/corte` consulta/cria lotes com PCP_LOTES. Criação recebe `modeloId`, `quantidade` (1–10000), `previsao` (a partir de hoje), `observacoes` e `chaveOperacao` UUID. Congela ficha, revisão e referências de fotos. Repetir UUID/conteúdo devolve resposta original; divergência 409. Exige modelo ativo com painel de corte válido.
- `GET /aratech/pcp/lotes/corte/{id}` retorna a ficha congelada. `POST .../{id}/enviar` e `.../{id}/cancelar` exigem ALTERAR e `{versao}`. Somente PLANEJADO pode ser enviado ou cancelado.
- `GET /aratech/corte/lotes` e `.../{id}` exigem CORTE_LOTES.CONSULTAR; mostram apenas ENVIADO_CORTE, EM_CORTE e CORTADO. `POST .../{id}/iniciar` exige ALTERAR; `.../{id}/finalizar` exige FINALIZAR; recebem `{versao}`. Fluxo `PLANEJADO → ENVIADO_CORTE → EM_CORTE → CORTADO`; versão antiga/transição repetida retorna 409. Não baixa estoque nem conclui outras etapas.
- Fotos autenticadas: `GET /aratech/administracao/guarda-roupas/{id}/fotos/{foto}`, `/aratech/pcp/lotes/modelos/{id}/fotos/{foto}` e `/aratech/corte/lotes/modelos/{id}/fotos/{foto}`, com a permissão CONSULTAR de cada recurso. Resposta binária JPEG/PNG sem cache público e com vínculo modelo/foto conferido.

A interface multiplica componentes pelas unidades do lote, soma área geométrica das peças, exporta CSV e imprime a ficha. Não há otimização de chapas, perdas de serra ou geração de programa de máquina. Todos os novos comandos registram auditoria. A migração preserva a consulta dos lotes movidos para perfis existentes; Corte não herda permissões comerciais automaticamente. O catálogo de permissões é registrado antes das migrações que transferem vínculos de perfis.

Veja [campos, pesquisa de componentes e fluxo de fabricação](docs/ficha-guarda-roupa.md).

O processo HTTP apenas valida as migrações e o mapeamento JPA ao iniciar. Alterações de estrutura são executadas por uma tarefa Flyway separada, com conta própria; o usuário da aplicação tem acesso aos dados e leitura do histórico, sem DDL nem escrita em `flyway_schema_history`. Os contratos HTTP e as permissões dos usuários não foram alterados. Veja [operação das migrações](docs/migracoes-flyway.md).

Para orientações sem termos técnicos, use **Documentação do Sistema** no menu da aplicação (`/documentacao` no frontend). O banco é versionado pelo Flyway; veja [migrações e adoção de bancos existentes](docs/migracoes-flyway.md). A mudança de inicialização não altera endpoints, permissões ou formatos da API.

Com o backend local iniciado, abra **http://localhost:8082/documentacao/index.html**.

A interface tem identidade visual da Aratech, busca por módulo, schemas dos corpos e respostas, regras em português sem formulário de login próprio. O JSON exportável está em **http://localhost:8082/v3/api-docs** e o YAML em **http://localhost:8082/v3/api-docs.yaml**. A interface padrão do Swagger também está em `/swagger-ui/index.html`; para renovação automática de CSRF, use a interface personalizada.

## Operação integrada

Os módulos seguem as camadas `controller`, `dto`, `model`, `repository`, `service` e `service/imp`, conforme a [arquitetura do backend](docs/arquitetura-backend.md). SQL é responsabilidade dos repositories. A refatoração preserva as rotas e os campos JSON; os identificadores de schema dos DTOs extraídos acompanham seus novos pacotes.

PCP, Comercial, requisições de materiais, expedição, cotações, Financeiro, férias/solicitações do RH e Administração possuem contratos persistentes. Veja [operação da fábrica](docs/operacao-fabrica.md) e [implantação](docs/implantacao-producao.md). Permissões detalhadas por tela são documentadas em cada operação; permissões amplas legadas continuam válidas.

## Cobertura

O OpenAPI cobre as operações dos controllers atuais, além de login e logout implementados nos filtros do Spring Security. `ApiDocumentacaoIT` confronta as rotas registradas em execução com a especificação e verifica descrição, tags, permissões, respostas de erro, CSRF e referências dos schemas. A contagem de regressão atual é de **255 operações dos controllers**; a especificação também inclui os dois contratos dos filtros de autenticação.

| Área | Contratos documentados |
| --- | --- |
| Almoxarifado | Categorias, fornecedores, produtos, movimentações e recebimentos |
| Portaria | Chegadas, edição, confirmação de entrada, fila, resumo e histórico |
| Carregamento | Fila de chegadas, autorizações, finalização e histórico |
| Assistência técnica | Lotes, início/finalização, confirmação no caminhão e estoque de peças |
| Manutenção | Chamados, solicitações de peças, fila, atribuição de mecânicos, conclusão, mecânicos e estoque |
| RH | Funcionários, documentos de admissão, perfis, atribuições e controle de ponto |
| Comercial e PCP | Pedidos de venda, ordens, linhas, capacidade, programação, indicadores e requisições |
| Produção por setor | Variantes, roteiros, lotes, prévia, liberação parcial, ordens de Corte/Furação/Montagem/Embalagem e separação de materiais |
| Catálogo e corte | Modelos, fichas técnicas, fotos e fluxo anterior de lotes exclusivos de corte |
| Embalagem e esteiras | Recebimento, roteiro, fila, capacidade, pausa, retomada, conclusão e volumes |
| Compras e Financeiro | Pedidos, cotações, aprovação, recebimento, provisões, documentos, baixas e caixa realizado |
| RH e Administração | Férias, afastamentos, solicitações, auditoria, parâmetros e relatórios |
| Segurança | CSRF, login, sessão, troca de senha, logout, permissões e criação de credenciais corporativas |
| Minha conta | Dados do usuário logado, próprio ponto e histórico de correções |
| Infraestrutura | Contrato legado de upload, explicitamente identificado como bloqueado pela segurança |

Páginas apenas de frontend não são apresentadas como endpoints inexistentes. A documentação descreve o backend atual; filtros ou ordenações exclusivamente visuais são identificados como tal. Por exemplo, a API de lotes ordena por antiguidade, enquanto a prioridade visual por status no carregamento é aplicada pela interface.

Os testes unitários e de integração têm finalidades distintas. A matriz dos módulos, os cenários verificados e os comandos de execução estão em [Testes do ERP](docs/testes.md). A presença de testes em um serviço não significa cobertura de 100% de linhas ou de todos os caminhos possíveis.

## Autenticação e execução

A sessão usa 15 minutos de inatividade, renovados a cada requisição, com limite absoluto de 8 horas. Logout, mudança de credencial e reinicialização do backend podem encerrá-la antes desse prazo. As sessões permanecem em memória nesta instalação.

1. Entre pela aplicação no mesmo navegador, ou execute POST `/aratech/auth/entrar` na seção Autenticação do Swagger. A documentação não possui formulário de login próprio.
2. O servidor cria o cookie HttpOnly `ARATECHSESSION`. O navegador mantém esse cookie e envia nas requisições ao mesmo servidor.
3. Antes de cada escrita executada pela interface personalizada, o JavaScript obtém um CSRF atualizado em `/aratech/auth/csrf` e envia `X-XSRF-TOKEN`.
4. A senha temporária exige troca em `/aratech/auth/senha` ou na tela da aplicação. A troca revoga a sessão; entre novamente.
5. Cada operação informa a permissão efetiva. Ver a documentação não concede autorização para executar a API.

Login usa `application/x-www-form-urlencoded`, não JSON. API não aceita Bearer/JWT. Não copie o cookie HttpOnly para exemplos ou arquivos. A página não persiste senha ou autorização no armazenamento do navegador e não carrega bibliotecas de CDNs.

**Executar grava dados reais.** Use uma base apropriada para testes ao cadastrar, movimentar estoque, corrigir ponto ou avançar etapas. Nenhuma autorização, validação ou proteção CSRF é dispensada pelo Swagger.

## Disponibilidade

No perfil `dev`, a documentação fica habilitada por padrão. Nos demais perfis, fica desabilitada. A variável `ARATECH_DOCS_ENABLED=true` habilita explicitamente a documentação, inclusive fora de desenvolvimento; `false` a desabilita. Quando habilitada, a documentação é consultável sem login; operações da aplicação continuam protegidas.

Reinicie o backend depois de alterar a configuração ou as classes. A cadeia de segurança da documentação permite somente GET para seus próprios recursos, com CSP própria; a política restritiva da API continua separada.

## Manutenção

- `@Tag` nos controllers contém as regras do módulo; `@Operation` descreve cada ação.
- `ApiDocumentacaoConfig` registra a autenticação dos filtros, os contratos de respostas dinâmicas, os erros comuns, a permissão efetiva e descrições de campos compartilhados.
- Os schemas são gerados dos tipos Java e das restrições Bean Validation. Nomes completos evitam colisões entre classes `Response` de módulos diferentes.
- `Permissoes.necessaria` é compartilhado pela autorização e pela documentação para reduzir divergências.
- `ApiDocumentacaoIT` compara todas as rotas dos controllers com a especificação, verifica schemas referenciados e comprova que disponibilizar documentação não libera a API.

Validação: `./mvnw.cmd '-Dtest=ApiDocumentacaoIT,SegurancaSessaoIT' test`.

Referências de implementação: [springdoc e compatibilidade com Spring Boot](https://springdoc.org/), [configuração do Swagger UI](https://swagger.io/docs/open-source-tools/swagger-ui/usage/configuration/).

## Compras e reposição por estoque mínimo

- Produtos do Almoxarifado e peças da Manutenção possuem mínimo opcional, editável com duplo clique na célula. Vazio desativa; zero acompanha o estoque zerado.
- A rotina é transacional no próprio backend, acionada por movimentos e pela configuração do mínimo. Não depende de Python, página aberta ou tarefa agendada.
- Com saldo menor ou igual ao mínimo, cria pedido `Aguardando Aprovação`. Quantidade sugerida = mínimo − saldo + 1, para superar o limite em uma unidade.
- Uma ocorrência de estoque baixo gera somente um pedido. Enquanto ele estiver pendente, novas quedas atualizam a quantidade sugerida; repor acima do mínimo ou desativar o mínimo cancela apenas o pedido automático ainda pendente. Pedidos aprovados não são cancelados automaticamente.
- Uma reprovação não gera outro pedido continuamente. Novo episódio automático exige o saldo superar o mínimo e depois voltar a cair. O motivo de reprovação permanece disponível no módulo de origem.
- `Solicitar peça` cria o chamado e o pedido em Compras na mesma transação. A chave idempotente preserva um único pedido por solicitação, sem depender do monitoramento de mínimo.
- Aprovar e reprovar exigem `COMPRAS.FINALIZAR`, versão atual e estado `Aguardando Aprovação`; reprovação exige motivo de 5 a 1000 caracteres. Almoxarifado e Manutenção consultam somente seus próprios pedidos, com suas permissões de consulta.
- A tela `/compras/pedidos` centraliza todos os status, aprovações e reprovações; o módulo separado de Aprovações foi removido. O endereço antigo `/compras/aprovacoes` redireciona para Pedidos de Compra. Novas páginas `/almoxarifado/pedidos` e `/manutencao/pedidos` reutilizam a tabela e os componentes visuais existentes. A lista é atualizada a cada 15 segundos enquanto a página está aberta.
- Cadastro manual começa em `Rascunho`; enviar muda para `Aguardando Aprovação`. Remover rascunho manual o cancela, preservando o registro. Edição antes da decisão exige quantidade positiva, fornecedor, valor unitário positivo e entrega igual ou posterior à data do pedido.
- `Recebido` encerra o acompanhamento da compra aprovada. O recebimento de compra gera uma conta a pagar com vencimento negociado. Nenhuma decisão de compra credita ou baixa estoque: o recebimento físico deve ser registrado pela entrada de estoque do módulo correspondente.

## Formato do login corporativo

Todos os logins, inclusive o administrador técnico, devem usar `nome@aratechmoveis` (sem `.com`). A parte anterior ao @ aceita até 64 caracteres, sem espaços, ponto inicial/final ou pontos consecutivos. Maiúsculas são normalizadas. Login inválido retorna 401 genérico; criação de credencial inválida retorna 400. O e-mail pessoal do funcionário mantém sua validação própria. Logins antigos fora desse padrão precisam ser adequados antes da atualização; não são renomeados automaticamente. A regra não cria caixas postais externas.


### Marcação de ponto pelo funcionário

No menu do usuário, **Marcação de ponto** abre relógio sincronizado com o servidor (America/Sao_Paulo), sem link de volta ao dashboard nem formulário de credenciais. O menu mostra apenas o primeiro e o último nome; o nome completo permanece no cadastro e na sessão. `GET /aratech/auth/meu-ponto/relogio` fornece `instante` e `data`. `POST /aratech/auth/meu-ponto/marcacoes` recebe somente `{data,versao}` e exige sessão ativa e CSRF. O ID e o nome do funcionário e autor vêm exclusivamente da sessão validada pelo filtro, que verifica se a conta continua ativa. Não recebe nem exige ID, e-mail ou senha e não cria nem troca a sessão. O servidor preenche o próximo dos seis horários do dia e guarda histórico; não aceita data escolhida, repetição no mesmo minuto, versão desatualizada ou mais de seis horários. Sessão inválida: 401; data inválida: 400; conflito: 409. Conta inativa revoga a sessão e pode invalidar o CSRF, retornando 403 com CSRF_INVALIDO. A conta técnica de administração (ID 0) não representa um funcionário: a tela informa essa condição sem consultar registros ou oferecer marcação. Marcações, consulta mensal e histórico do próprio ponto retornam 403 para essa conta, com orientação para usar uma conta de funcionário, sem a mensagem “Funcionário não encontrado”.

`GET/PUT /aratech/rh/ponto/{id}/escala` consulta/define a escala de sete dias (1=segunda, 7=domingo), com `horarios` (seis strings HH:mm ou vazias) e `extraMinutos` (0 a 120, autorização explícita). Soma de pares semanais deve respeitar `jornadaSemanal` do funcionário. Pares devem ser completos, sequenciais e no mesmo dia; folga tem seis strings vazias. Sem escala o sistema informa **Escala não definida**; não presume jornada diária a partir da semanal. A escala atual é usada nas comparações de todos os dias consultados, inclusive passados. Não representa turnos que cruzam meia-noite.

Correção de horários e definição de escala exigem **perfil `#` e RH_PONTO.ALTERAR**, inclusive para administradores. A migração cria o perfil sem atribuí-lo a ninguém. Gestores de acessos podem atribuí-lo pelo nome. As consultas continuam com as permissões existentes.

O espelho soma pares completos por dia e mês. `situacao` informa `SEM_MARCACAO`, `SEM_ESCALA`, `NO_HORARIO` ou `FORA_HORARIO`; comparação exata ao minuto, sem tolerância automática. Verde claro para horários previstos, vermelho claro para divergências, branco para dias sem marcação. `totalMinutos` exclui intervalos; `aviso` informa excesso da jornada ou das extras autorizadas. A saída é preservada mesmo ao ultrapassar o limite, com alerta ao funcionário. A configuração de até 120 minutos segue a regra geral do art. 59 da CLT e depende dos acordos aplicáveis; não concede horas extras automaticamente.
# Lotes de produção por setor (V17)

PCP → Lotes usa `/aratech/pcp/lotes/producao` para planejar uma variante e liberar apenas os setores selecionados na prévia. Ficha, revisão e totais ficam congelados na primeira liberação; atualizar explicitamente a prévia só é permitido antes de emitir qualquer ordem. Reenvios e concorrência não duplicam ordens (transação + bloqueio + unicidade lote/setor). Cada destino consulta somente suas linhas e permissões. Consumo é único por componente, independente de quantas operações ele percorra. Almoxarifado recebe uma tarefa de separação; a baixa continua no `ProdutoService.saida`, somente após confirmação explícita, com conversão exata para a unidade de estoque existente. Lotes com destinos pendentes não concluem; ordens/pedidos de origem com lotes vinculados têm proteções de conclusão/cancelamento/reprogramação. Destinos que exigem medidas validam e recebem especificação também nas linhas de consumo direto; informações ausentes bloqueiam a liberação. Não estima chapas.

Contratos, suposições, arquivos e reprodução pela interface/API: [PCP — lotes setoriais](docs/pcp-lotes-setoriais.md). O fluxo anterior de lotes exclusivos de Corte permanece acessível para o histórico e a integração existente com esteiras.

