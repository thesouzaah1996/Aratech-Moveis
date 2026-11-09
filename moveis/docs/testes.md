# Testes do ERP

Os testes unitários ficam em `src/test/java`, com sufixo `Test`. Instanciam os serviços sem subir Spring ou banco e substituem persistência e integrações externas por mocks. Os testes com sufixo `IT` validam banco, HTTP, segurança, transações e concorrência. Os testes Angular ficam em `*.spec.ts`.

## Resultado verificado em 09/11/2026

Foram acrescentadas **24 suítes unitárias, com 88 casos**, completando a presença de testes unitários para os **39 serviços concretos** do backend. A execução final partiu de `clean`, sem classes antigas no classpath.

| Suíte | Classes/suítes | Casos | Falhas / erros / ignorados |
| --- | ---: | ---: | --- |
| Backend unitário | 58 | 519 | 0 / 0 / 0 |
| Backend integração | 17 | 75 | 0 / 0 / 0 |
| Backend total | 75 | 594 | 0 / 0 / 0 |
| Frontend Angular | 52 arquivos spec | 300 | Nenhuma falha |

`ApiDocumentacaoIT` e as quatro verificações de `CamadasTest` passaram na execução completa. As pendências de organização foram corrigidas mantendo as verificações: serviços de fabricação, lotes e embalagem usam interfaces e implementações separadas; SQL dos lotes e do callback Flyway está em repositories. Não foram modificados contratos HTTP nem migrações SQL aplicadas.

O teste Angular da área de corte foi atualizado para verificar a área e a quantidade, sem exigir o texto de apresentação removido por solicitação do usuário. A validação da carga DEMO, após as integrações, confirmou as mesmas contagens e vínculos, sem novos inserts permanentes.

As suítes adicionadas são `AdministracaoServiceImpTest`, `RecebimentoServiceImpTest`, `PecaAssistenciaServiceImpTest`, `BucketServiceImpTest`, `ExpedicaoServiceImpTest`, `ComercialServiceImpTest`, `CotacaoServiceImpTest`, `PedidoCompraServiceImpTest`, `EmbalagemServiceTest`, `FabricacaoServiceImpTest`, `FinanceiroServiceImpTest`, `ProducaoServiceImpTest`, `RequisicaoServiceImpTest`, `AfastamentoServiceImpTest`, `SolicitacaoRhServiceImpTest`, `AtribuicaoPerfilServiceImpTest`, `ContasServiceImpTest`, `GestaoAcessoServiceImpTest`, `LimiteLoginServiceImpTest`, `PerfilUsuarioServiceImpTest`, `SenhasServiceImpTest`, `OperacaoServiceImpTest`, `ApoioServiceImpTest` e `ChamadoTiServiceImpTest`.

## Matriz por módulo

| Módulo | Casos unitários principais |
| --- | --- |
| Almoxarifado | Cadastro e validação de categorias/fornecedores/produtos, saldos, movimentações; recebimento restrito ao setor e propagação de falhas da Portaria |
| Comercial | Confirmação cria encomenda e provisão pelo valor total; cancelamento sincroniza origens; estados incompatíveis, entrega passada e limite financeiro são rejeitados |
| PCP | Capacidade por dia, requisições pendentes, conclusão condicionada aos lotes, sincronização comercial e bloqueio de cancelamento com materiais entregues |
| Requisições | Quantidade e UUID da saída, rejeição de entrega repetida, devolução integral somente antes de iniciar a OP |
| Lotes e fabricação | Multiplicação dos componentes, fracionários, roteamento, especificações, consumo único e validações da ficha |
| Embalagem | Etapa futura bloqueada, ocupação por pausas, limites de capacidade, prontidão explícita, volumes e conferências na última etapa |
| Compras | Permissão de decisão, estados/versões, provisão pelo total e geração/atualização de reposição no estoque mínimo |
| Cotações | Propostas completas e fornecedores distintos, escolha por preço, bloqueio de alteração após decisão do pedido |
| Financeiro | Data real de baixa, títulos integrados, documento sem duplicar obrigação, provisão repetida e competência do caixa |
| Carregamento/Portaria | Validações de chegada, estados, autorização e entrada; expedição apenas com produção concluída e caminhão em atendimento |
| Manutenção | Abertura e validação de chamados/peças, fila, mecânicos, estoque e integração com compras |
| Assistência | Transições dos lotes, estoque fracionário, saldo insuficiente, reenvio idêntico, UUID divergente e arquivamento com saldo |
| RH | Funcionários, documentos, CPF, ponto, perfis; afastamento inativo/sobreposto, decisões e solicitações de RH |
| Perfis e acessos | Atribuição apenas de perfil existente, normalização, consulta literal, limites de busca, proteção contra concessão superior e último administrador |
| Segurança e minha conta | Sessão sem permissões durante troca obrigatória, conjuntos imutáveis, credenciais, senha temporária somente como hash, limite de tentativas e dados pessoais restritos |
| Administração | Versão do parâmetro, auditoria de alteração e relatórios com antecedências configuradas |
| Operações compartilhadas | Resposta idempotente sem repetir efeitos, conflito de escopo/dados, falha sem resposta persistida, versão, motivo e identidade |
| Apoio e armazenamento | Seletores separados por categoria/fornecedor e propagação de falha de armazenamento sem URL fictícia |

As novas suítes complementam as existentes, sem substituir testes de integração por mocks. `OperacaoUnit` reutiliza as validações reais de versão, motivo e condição; isola somente a execução persistente do comando. `OperacaoServiceImpTest` testa separadamente a idempotência real do serviço.

## Executar

Na pasta `moveis`:

```powershell
# Unitários: não precisam de PostgreSQL ou aplicação iniciada.
.\mvnw.cmd '-Dtest=*Test' test

# Integração: exige PostgreSQL dev, migrações aplicadas e configuração privada local.
.\mvnw.cmd '-Dtest=*IT' test

# Todas as suítes backend, incluindo integração explicitamente.
.\mvnw.cmd '-Dtest=*Test,*Tests,*IT' test
```

`mvn test` usa a seleção padrão do Surefire e não inclui automaticamente os arquivos `*IT`. Os testes de integração existentes fazem rollback, limpam seus próprios registros ou usam schemas isolados; não devem ser apontados para homolog/prod.

Na pasta `aratechFrontend/aratech-movies-frontend`:

```powershell
npm test -- --watch=false --browsers=ChromeHeadless --reporters=dots
npm run build
```

Relatórios backend: `target/surefire-reports`. A cobertura por módulo/serviço não representa um percentual de linhas: não há um relatório JaCoCo configurado. Só afirmar testes aprovados após verificar o resultado da execução correspondente.

## Documentação da API

`ApiDocumentacaoIT` compara os endpoints de todos os controllers com o JSON OpenAPI. Verifica descrições, tags, permissões, respostas, CSRF e schemas. Login/logout dos filtros também devem estar publicados. A interface personalizada continua sem formulário de login próprio ou seção adicional de módulos.
