# Dados de demonstração do ERP

Carga manual via INSERT, executada no banco **aratechmoveis_dev**, com prefixos `DEMO` e marcador `DEMO-ERP-V1`. Não é migração Flyway nem cadastro de operações reais. Homolog/prod e registros anteriores foram preservados.

## Conteúdo

| Área | Registros adicionados |
| --- | ---: |
| RH | 10 funcionários, 10 perfis de consulta sem contas de acesso, 10 férias, 10 solicitações, 50 dias de ponto e 50 escalas |
| Catálogo | 10 modelos e 10 variantes brancas com componentes, medidas e roteiros |
| Almoxarifado | 10 categorias, 10 fornecedores, 20 produtos, 40 movimentos, 10 requisições e 10 consumos confirmados |
| Comercial e PCP | 20 pedidos de venda, 10 linhas, 20 OPs e 20 lotes de produção |
| Ordens setoriais | 100 ordens: 20 para cada um de Corte, Furação, Montagem, Almoxarifado e Embalagem |
| Compras | 20 pedidos e 20 cotações; 10 recebidos e 10 aguardando aprovação |
| Financeiro | 10 títulos de compra a pagar, 20 títulos de venda a receber e 10 pagamentos de salários no caixa realizado |
| Corte e esteiras | 10 lotes de corte anteriores, 10 lotes de embalagem, 10 esteiras e 30 etapas |
| Portaria e carregamento | 10 recebimentos de materiais, 10 caminhões para coleta, 10 retornos e 10 OPs prontas para expedição |
| Manutenção | 10 mecânicos, 10 peças com saldo inicial, 10 chamados e 10 solicitações de peças ligadas a compras |
| Assistência | 10 lotes vinculados a retornos e 10 peças com saldo inicial |
| Suporte e Administração | 10 chamados de TI e 10 eventos identificando a carga na auditoria |

As tabelas de configuração mantêm suas quantidades funcionais: não são inventados setores, parâmetros ou permissões para atingir dez linhas. Não foram criados anexos/documentos/fotos fictícios nem contas ou senhas de usuários. Documentos pessoais são sintéticos com dígitos verificadores; e-mails usam `example.invalid`.

## Relações e estados

- Cada modelo possui uma variante branca e dois pedidos de 150 móveis, com OP, lote, ficha congelada e cinco ordens setoriais. Dez lotes estão em produção (ordens LIBERADA) e dez concluídos (todas as ordens CONCLUIDA).
- Cada unidade usa duas laterais e oito dobradiças: 300 laterais no Corte e 1.200 dobradiças na separação, com outros painéis especificados na ficha. Instruções e materiais seguem seus próprios setores.
- Consumos dos lotes concluídos possuem movimentação SAIDA com o mesmo UUID. Cada produto de dobradiça possui INICIAL 10.000 + ENTRADA 10.000 − SAIDA 1.200 = saldo 18.800. Os lotes apenas liberados ainda não consumiram estoque.
- Vendas e compras possuem os títulos financeiros correspondentes, com valores calculados. Dez salários demonstrativos, identificados pelo documento `DEMO-FOLHA-*` e referência ao funcionário, estão baixados e aparecem no caixa realizado do mês da carga.
- A chegada do fornecedor é referenciada no motivo da compra e no título. O estoque atual não possui FK de movimentação para chegada/compra: a carga mantém a rastreabilidade pelos campos existentes, sem criar relação artificial.
- O lote de assistência usa como origem a chegada de retorno. Dez caminhões em atendimento ficam disponíveis para carregar as dez OPs finalizadas. A expedição permanece pendente para ser executada pela interface.
- O fluxo anterior Corte → Esteiras é independente das novas ordens multissetoriais, conforme o projeto. Os lotes de embalagem preservam a ficha e a origem de corte próprias.
- SQL preenche explicitamente os dois lados das integrações síncronas. A carga não dispara chamadas HTTP nem simula publicação de eventos da aplicação.

## Execução e validação

Na pasta `moveis`, com PostgreSQL dev disponível e perfil `dev` ativo:

```powershell
.\scripts\popular-dados-dev.ps1 -Comando simular
.\scripts\popular-dados-dev.ps1 -Comando inserir
.\scripts\popular-dados-dev.ps1 -Comando validar
```

O padrão é `simular`, com ROLLBACK. `inserir` gera um backup e testa sua restauração integral antes de executar a transação. A carga usa IDs retornados pelo banco, bloqueio para concorrência, validação antes de COMMIT e marcador idempotente. A simulação pode deixar lacunas nas sequências de IDs, comportamento normal do PostgreSQL. Reexecuções não substituem cadastros nem recalculam lotes alterados posteriormente por usuários.

Arquivos:

- `scripts/dados-demonstracao-dev.sql`: inserts e montagem das fichas e vínculos.
- `scripts/validar-dados-demonstracao-dev.sql`: contagens mínimas e verificações de integridade, estoque, totais, revisão, ordens, financeiro e embalagem.
- `scripts/popular-dados-dev.ps1`: execução restrita ao dev, simulação e backup.

Na execução desta tarefa, a simulação e a carga passaram pela validação SQL. A leitura adicional com os repositórios Java validou os DTOs/Bean Validation, as 100 ordens e confirmou que o cálculo de todas as 20 fichas inseridas é idêntico ao cálculo de `LotesProducaoService.calcular`. Reexecutar a carga em simulação manteve as contagens.

Para localizar os dados na interface, pesquise por **DEMO** nos módulos. No PCP, há dez lotes em produção e dez concluídos. No financeiro, selecione o mês da carga para visualizar os salários no caixa; no ponto, selecione a semana anterior à carga. Relatórios e indicadores são derivados desses registros, sem inserts de relatórios fictícios.
