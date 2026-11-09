# PCP: lotes de produção e ordens por setor

## Inspeção e decisões

- `fabricacao_modelo` já guarda `FichaGuardaRoupa` e componentes com medidas finais, materiais, acabamento, quantidades e unidades. A variante reutiliza esse contrato completo, sem inventar dimensões a partir da medida externa do móvel.
- `comercial_pedido.produto` e `pcp_ordem.produto` são descrições livres. Confirmar um pedido já cria uma OP vinculada. O novo lote pode selecionar essa OP como origem e controla seu saldo sob bloqueio. A correspondência com modelo/variante é explicitamente escolhida pelo PCP; não há associação pelo nome.
- O lote anterior (`fabricacao_lote`) tem estados exclusivos de corte e é usado pelas esteiras de embalagem. Foi preservado como fluxo anterior. `pcp_lote_producao` é o lote principal multissetorial; `pcp_ordem_setor` são suas ordens. Nenhum dado anterior é convertido silenciosamente.
- Não existia variante/revisão ou cadastro de setores de fabricação. V17 adiciona variantes com ficha completa e roteiro por UUID de componente, além de destinos Corte, Almoxarifado, Furação, Montagem e Embalagem. O roteamento usa IDs da ficha; as regras de destino e permissões estão em `fabricacao_setor`, sem inferir destino por nome/tipo do item.
- Cada componente tem um responsável pelo consumo e zero ou mais operações, com instruções próprias. `consomeMaterial` marca uma única linha por componente. Se consumo e primeira operação pertencem ao mesmo setor, aparecem juntos em uma linha (ex.: 300 laterais), sem duplicar a necessidade de material. As operações seguintes representam trabalho sobre a mesma peça.
- Setores com `exigeMedidas` recebem especificação técnica também nas linhas de consumo direto. Medidas, material e acabamento são validados nesses destinos mesmo sem operação cadastrada; peças marcadas para corte continuam exigindo uma operação de corte. A edição da variante exibe os campos de medidas conforme os destinos escolhidos, além da marcação de corte. Instruções de outros setores não são copiadas.
- Todos os componentes da ficha são obrigatórios. Consumo sem setor, peça de corte sem operação com medidas, medidas/material/cor ausentes e dados insuficientes para a separação bloqueiam a liberação e aparecem na prévia.
- O planejamento gera uma cópia para prévia. Antes de emitir qualquer ordem, o PCP pode corrigir a variante e usar **Atualizar prévia da variante**, sem reservar quantidade adicional da OP. A primeira liberação congela a revisão definitivamente para os setores restantes. Revisar o cadastro não altera automaticamente o lote. Não há substituição de ordens emitidas.
- `produto.quantidade` e o serviço de estoque existente são inteiros. Ficha e ordens calculam com `BigDecimal`, preservando até três casas e a unidade original. `fatorEstoque` é uma conversão explícita: unidades do produto de estoque por unidade da ficha. Por exemplo, se a cola é estocada em gramas, 0,125 KG por móvel × 150 × fator 1000 = 18.750 unidades de estoque. A prévia mostra 18,750 KG; a confirmação informa também 18.750 unidades de estoque. Nenhum arredondamento é permitido. O fator padrão é 1; a equipe deve conferir a unidade física do produto selecionado.
- A ordem de Almoxarifado é a tarefa de separação. Ela reutiliza `ProdutoService.saida` na confirmação individual de consumo, com validações e movimentações existentes. Não duplica `pcp_requisicao`, que depende de uma OP e aceita apenas inteiros. Liberar/iniciar não baixa saldo. Sem saldo, a confirmação inteira é revertida.
- A integração existente é síncrona, dentro do monólito. Não foi criado mensageiro ou publicação assíncrona artificial. Lote bloqueado com `FOR UPDATE`, `UNIQUE(lote_id,setor_id)` e transação cobrem reenvios e concorrência. Confirmação de consumo tem unicidade por ordem/componente e UUID de movimentação.
- Não existe otimizador de corte no projeto inspecionado. A saída é a lista de peças e medidas, sem estimativa simplificada de chapas, encaixe ou programa de máquina.
- As novas ordens da Embalagem são acessadas em **Embalagem → Ordens do PCP**. O controle existente de esteiras e recebimento de lotes anteriores continua próprio; não se presume que corte concluído significa móvel montado ou pronto para embalar.

## Reproduzir pela interface

1. Em **Administração → Guarda-roupas**, cadastre o modelo X. Adicione a lateral com quantidade 2, medidas reais, material e acabamento, e a dobradiça com quantidade 8 UN. Fotos continuam no catálogo existente.
2. No mesmo catálogo, abra **Variantes e roteiros por setor → Nova variante**. Selecione o modelo, copie a ficha e informe código `BRANCO` e nome `Branco`.
3. Na lateral: responsável pelo consumo **Corte**; operação **Corte / Cortar laterais**, com instruções próprias. Se necessário, adicione **Furação / Furar laterais**, com suas instruções. Na dobradiça: responsável **Almoxarifado**, produto de estoque correspondente (selecionado pelo SKU/nome), fator 1; não adicione operação de corte. Salve a revisão.
4. Em **PCP → Lotes → Novo lote**, selecione a variante branca, quantidade 150 e previsão futura. Opcionalmente selecione a OP criada pelo pedido de 150 móveis. O servidor impede planejar além do saldo da OP.
5. Clique **Planejar e visualizar prévia**. Se houver pendências, corrija a variante e use **Atualizar prévia da variante** antes de liberar. Corte mostra 300 laterais com especificação técnica; Almoxarifado mostra 1.200 dobradiças, sem instruções de corte.
6. Marque apenas **Corte** e clique **Liberar selecionados**. O PCP mantém Almoxarifado (e outros destinos calculados) pendente. Corte vê sua OS em **Corte → Lotes**, com código `LP-n` de origem.
7. Volte ao lote no PCP, marque Almoxarifado e libere. A ordem de Corte mantém seu ID; surge uma OS independente em **Almoxarifado → Separação de lotes**. Repetir a requisição não duplica as ordens.
8. Almoxarifado inicia sua tarefa e usa **Conferir e consumir** por insumo. A confirmação mostra quantidade da ficha e unidades de estoque, exige motivo e só então baixa o saldo. Conclua a separação após confirmar todos os itens.
9. Corte/Furação/Montagem/Embalagem iniciam e concluem suas próprias ordens. PCP mostra `LIBERACAO_PARCIAL` enquanto houver destino calculado não liberado. Só chega a `CONCLUIDO` com todos os destinos liberados e todas as ordens concluídas.
10. Altere a variante depois da primeira liberação. O lote e as liberações posteriores continuam com revisão e quantidades originais.

## API

Autenticação e CSRF são os existentes: cookie de sessão, GET `/aratech/auth/csrf`, token em `X-XSRF-TOKEN` nas escritas. Os contratos completos, limites e erros estão no Swagger.

Catálogo administrativo, usando `ADMINISTRACAO_GUARDA_ROUPAS`:

- GET/POST `/aratech/administracao/guarda-roupas/variantes`
- GET/PUT `/aratech/administracao/guarda-roupas/variantes/{id}`
- GET `/aratech/administracao/guarda-roupas/variantes/setores` e `/materiais`
- GET `/aratech/administracao/guarda-roupas/variantes/modelo/{id}`

O corpo `FichaVariante` contém `modeloId`, `codigo`, `nome`, `ativo`, `versao`, a `ficha` completa e o `roteiro`:

```json
[
  {"itemChave":"UUID-DA-LATERAL","consumoSetorId":1,"produtoEstoqueId":null,"fatorEstoque":1,
   "operacoes":[{"setorId":1,"nome":"Cortar laterais","instrucoes":"Conferir veio e acabamento branco"},
                {"setorId":3,"nome":"Furar laterais","instrucoes":"Conforme desenho de furação"}]},
  {"itemChave":"UUID-DA-DOBRADICA","consumoSetorId":2,"produtoEstoqueId":42,"fatorEstoque":1,"operacoes":[]}
]
```

IDs acima são exemplos; consulte os setores e produtos reais.

PCP, usando `PCP_LOTES`:

- GET/POST `/aratech/pcp/lotes/producao`
- GET `/aratech/pcp/lotes/producao/{id}` (prévia e ordens)
- GET `/aratech/pcp/lotes/producao/setores`, `/variantes` e `/origens`
- POST `/aratech/pcp/lotes/producao/{id}/revisar` exige ALTERAR e recebe `{"versao":1}` (revisão atual da prévia). Somente antes de emitir qualquer ordem; depois retorna 409.
- POST `/aratech/pcp/lotes/producao/{id}/liberar` exige ALTERAR, corpo `{"revisao":1,"setores":[1]}`. Posteriormente envie `{"revisao":1,"setores":[2]}`. Não precisa reenviar Corte; reenviá-lo também é seguro.

Criação:

```json
{"varianteId":10,"ordemOrigemId":25,"quantidade":150,"previsao":"2030-12-15","observacoes":"Pedido de móveis brancos","chaveOperacao":"00000000-0000-4000-8000-000000000150"}
```

Filas setoriais (GET lista, GET `/{id}`, POST `/{id}/iniciar`, `/{id}/concluir` e `/{id}/confirmar`):

| Base | Recurso |
| --- | --- |
| `/aratech/corte/ordens` | `CORTE_LOTES` |
| `/aratech/almoxarifado/separacoes` | `ALMOXARIFADO_REQUISICOES` |
| `/aratech/producao/furacao` | `PRODUCAO_FURACAO` |
| `/aratech/producao/montagem` | `PRODUCAO_MONTAGEM` |
| `/aratech/embalagem/ordens` | `EMBALAGEM_LOTES` |

CONSULTAR lê apenas o setor do caminho; ALTERAR inicia; FINALIZAR conclui/confirma consumo. ID de ordem de outro setor retorna 404. Iniciar/concluir recebe `{"versao":1}`. Confirmar é exclusivo de separação e recebe `itemChave`, UUID `chaveOperacao` e `motivo`. Estados: `LIBERADA → EM_ANDAMENTO → CONCLUIDA`.

Ordens PCP vinculadas não podem mudar de produto ou ser reduzidas abaixo dos lotes planejados. Não podem ser canceladas (nem pelo pedido comercial) quando há lote vinculado. Conclusão exige todos os lotes vinculados concluídos. Expedição e atualização comercial seguem seu fluxo já existente, sem conclusão automática pelo novo lote.

## Arquivos principais

- Migração: `src/main/resources/db/migration/V17__pcp_lotes_setoriais.sql`.
- Backend: `src/main/java/com/aratech/moveis/pcp/lotes/` (contratos, repositório, serviço e três controllers).
- Integrações: `pcp/producao/service/imp/ProducaoServiceImp.java`, `pcp/producao/controller/PcpController.java`, `comercial/pedidos/controller/ComercialController.java` e `security/authorization/Permissoes.java`.
- OpenAPI: `documentacao/ApiDocumentacaoConfig.java`, `API.md` e anotações nos controllers/DTOs.
- Frontend: `core/services/lotes-producao.service.ts`; `componente/fabricacao/lotes-producao.component.*`, `variantes.component.*`, `ordens-setor.component.*`; rotas/permissões; links no catálogo, PCP, Corte, Almoxarifado, Dashboard e Embalagem.
- Validação: `LotesProducaoIT`, `LotesProducaoTest`, `LotesConcorrenciaIT`, `ApiDocumentacaoIT`, testes de migração e testes Angular do fluxo.

## Validação

Validação executada em 09/11/2026: 9 testes unitários e 20 testes de integração aprovados, incluindo os cenários de aceite em `LotesProducaoIT`, concorrência em `LotesConcorrenciaIT`, `ApiDocumentacaoIT` e migrações/permissões Flyway. Os 7 testes Angular do PCP passaram em Chrome Headless. O build final Angular passou com avisos de orçamento do bundle e seletores CSS já observados antes dos ajustes.

O fluxo multissetorial e a V17 já estavam presentes no workspace ao iniciar esta revisão. Foram preservados e inspecionados antes das alterações. Esta revisão corrigiu a especificação/validação do consumo direto em destinos que exigem medidas e a exibição dos campos técnicos na edição da variante. Arquivos efetivamente ajustados nesta revisão:

- `src/main/java/com/aratech/moveis/pcp/lotes/LotesProducaoService.java`
- `src/main/java/com/aratech/moveis/pcp/lotes/ProducaoDtos.java`
- `src/main/java/com/aratech/moveis/pcp/lotes/PcpProducaoController.java`
- `src/main/java/com/aratech/moveis/documentacao/ApiDocumentacaoConfig.java`
- `src/test/java/com/aratech/moveis/fabricacao/LotesProducaoTest.java`
- `API.md` e `docs/pcp-lotes-setoriais.md`
- `../aratechFrontend/aratech-movies-frontend/src/app/componente/fabricacao/variantes.component.ts`
- `../aratechFrontend/aratech-movies-frontend/src/app/componente/fabricacao/variantes.component.html`

A V17 existente foi validada em instalação e atualização de schemas isolados. Não houve nova migração nem alteração no histórico aplicado. Para executar os testes, foi iniciado o Docker Desktop e disponibilizado o container PostgreSQL dev. As imagens da aplicação não foram reconstruídas nesta revisão.

Backend: `./mvnw.cmd "-Dtest=LotesProducaoIT,LotesProducaoTest,LotesConcorrenciaIT,ApiDocumentacaoIT,FabricacaoIT,FabricacaoTest,EmbalagemIT,FlywayMigrationIT,FlywayPermissoesIT,MigracoesImutaveisTest" test`.

Frontend: `npm run build` e testes direcionados de `lotes-producao.component.spec.ts`.

A migração V17 é aditiva e não reescreve migrações aplicadas. Aplicar pelos scripts Flyway existentes, com backup. Não altera o formulário ou autenticação da interface personalizada do Swagger.

## Relação de arquivos desta implementação

Caminhos relativos à raiz `C:/AratechMoveis` (não inclui artefatos temporários em `target`):

```text
moveis/src/main/resources/db/migration/V17__pcp_lotes_setoriais.sql
moveis/src/main/java/com/aratech/moveis/pcp/lotes/ProducaoDtos.java
moveis/src/main/java/com/aratech/moveis/pcp/lotes/repository/LotesProducaoRepository.java
moveis/src/main/java/com/aratech/moveis/pcp/lotes/LotesProducaoService.java
moveis/src/main/java/com/aratech/moveis/pcp/lotes/imp/LotesProducaoServiceImp.java
moveis/src/main/java/com/aratech/moveis/pcp/lotes/PcpProducaoController.java
moveis/src/main/java/com/aratech/moveis/pcp/lotes/VariantesController.java
moveis/src/main/java/com/aratech/moveis/pcp/lotes/OrdensSetorController.java
moveis/src/main/java/com/aratech/moveis/pcp/producao/service/imp/ProducaoServiceImp.java
moveis/src/main/java/com/aratech/moveis/pcp/producao/controller/PcpController.java
moveis/src/main/java/com/aratech/moveis/comercial/pedidos/controller/ComercialController.java
moveis/src/main/java/com/aratech/moveis/security/authorization/Permissoes.java
moveis/src/main/java/com/aratech/moveis/documentacao/ApiDocumentacaoConfig.java
moveis/API.md
moveis/docs/pcp-lotes-setoriais.md
moveis/src/test/java/com/aratech/moveis/fabricacao/LotesProducaoIT.java
moveis/src/test/java/com/aratech/moveis/fabricacao/LotesProducaoTest.java
moveis/src/test/java/com/aratech/moveis/fabricacao/LotesConcorrenciaIT.java
moveis/src/test/java/com/aratech/moveis/documentacao/ApiDocumentacaoIT.java
moveis/src/test/java/com/aratech/moveis/migracao/FlywayMigrationIT.java
moveis/src/test/java/com/aratech/moveis/migracao/FlywayPermissoesIT.java
aratechFrontend/aratech-movies-frontend/src/app/core/services/lotes-producao.service.ts
aratechFrontend/aratech-movies-frontend/src/app/componente/fabricacao/lotes-producao.component.ts
aratechFrontend/aratech-movies-frontend/src/app/componente/fabricacao/lotes-producao.component.html
aratechFrontend/aratech-movies-frontend/src/app/componente/fabricacao/lotes-producao.component.spec.ts
aratechFrontend/aratech-movies-frontend/src/app/componente/fabricacao/variantes.component.ts
aratechFrontend/aratech-movies-frontend/src/app/componente/fabricacao/variantes.component.html
aratechFrontend/aratech-movies-frontend/src/app/componente/fabricacao/ordens-setor.component.ts
aratechFrontend/aratech-movies-frontend/src/app/componente/fabricacao/ordens-setor.component.html
aratechFrontend/aratech-movies-frontend/src/app/app.routes.ts
aratechFrontend/aratech-movies-frontend/src/app/core/permissoes.ts
aratechFrontend/aratech-movies-frontend/src/app/componente/dashboard/dashboard.component.html
aratechFrontend/aratech-movies-frontend/src/app/componente/almoxarifado/almoxarifado.component.html
aratechFrontend/aratech-movies-frontend/src/app/componente/pcp/pcp.component.html
aratechFrontend/aratech-movies-frontend/src/app/componente/fabricacao/corte.component.ts
aratechFrontend/aratech-movies-frontend/src/app/componente/fabricacao/guarda-roupas.component.ts
aratechFrontend/aratech-movies-frontend/src/app/componente/fabricacao/guarda-roupas.component.html
aratechFrontend/aratech-movies-frontend/src/app/componente/embalagem/embalagem.component.ts
aratechFrontend/aratech-movies-frontend/src/app/componente/documentacao/guia-sistema.ts
```
