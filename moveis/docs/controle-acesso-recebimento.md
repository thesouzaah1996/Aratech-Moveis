# Portaria e recebimento do almoxarifado

## Como usar

1. Na Portaria > Controle de Acesso, informe nota fiscal (somente numeros), empresa, motorista, placa, descricao da carga e Almoxarifado como setor.
2. Registrar Chegada coloca o veiculo na fila com status Aguardando.
3. Em Almoxarifado > Recebimento, a carga aparece automaticamente. O aviso de nova chegada usa a area de mensagens ja existente, inclusive quando a nova carga esta em outra pagina.
4. O almoxarifado usa o cadeado para autorizar o recebimento.
5. A portaria ve Liberada pelo setor e confirma a entrada pelo cadeado. O caminhao permanece na fila como Entrada liberada. No almoxarifado ele fica Em recebimento. Tentar liberar enquanto Aguardando mostra uma mensagem e nao permite a entrada.
6. Depois de liberar a entrada, a portaria usa Finalizar e confirma para enviar o registro aos historicos. No historico da portaria aparece Entrada autorizada, seguida da data e hora de entrada. No historico do almoxarifado aparece Finalizado. O almoxarifado tambem pode concluir um recebimento iniciado.

As telas consultam o servidor a cada 5 segundos. Uma nova autorizacao do almoxarifado gera aviso na portaria e permite liberar a entrada. Se o historico estiver aberto, as entregas finalizadas tambem sao atualizadas automaticamente. A fila fica salva no banco e continua disponivel ao fechar/reabrir o navegador. Os avisos sao internos a tela de recebimento; nao dependem de permissao de notificacao do navegador.

Carregamento tambem esta integrado: selecione Carregamento na portaria e acompanhe em Carregamento > Aguardando Descarga. O setor recebe avisos a cada 5 segundos, autoriza a entrada e finaliza a descarga depois da confirmacao da portaria. Sua fila, resumo e historico sao exclusivos do setor, pela API /aratech/carregamento/chegadas (mesmas operacoes de recebimentos). A portaria acompanha os dois setores. PCP permanece desabilitado. Recebimento nao altera o saldo de produtos automaticamente.

## Regras

- Campos obrigatorios; numero da nota fiscal com 1 a 9 digitos e diferente de zero, ou chave de acesso de 44 digitos com digito verificador modulo 11 valido; empresa com 2 a 150 caracteres; motorista com 3 a 150; descricao com 3 a 1000.
- Placa brasileira tradicional ou Mercosul, salva em maiusculas e sem hifen.
- A mesma nota nao pode ser registrada duas vezes para a mesma empresa, inclusive depois de finalizada. Empresas diferentes podem ter notas com o mesmo numero.
- Uma placa so pode ter uma chegada em andamento. Uma nova chegada e permitida depois da finalizacao, com outra nota fiscal.
- Edicao na Portaria e no Recebimento apenas enquanto Aguardando. Ambas alteram o mesmo registro; a outra tela recebe os dados atualizados automaticamente. Horarios, identidade e status sao definidos pelo servidor.
- Nao e permitido confirmar entrada antes da autorizacao, finalizar antes da entrada, nem reabrir registros finalizados.
- Repetir a mesma transicao nao duplica nem altera o horario ja gravado. Cadastro tem UUID para repetir com seguranca apos falha de rede.
- Atualizacoes de etapa bloqueiam o registro durante a transacao; o banco reforca duplicidades e ordem dos horarios.
- As operacoes usam o ID da chegada, nao a nota fiscal, para nao confundir empresas diferentes.

## API

| Metodo | Caminho | Uso |
| --- | --- | --- |
| GET | /aratech/portaria/chegadas | Fila paginada |
| POST | /aratech/portaria/chegadas | Registrar chegada |
| PUT | /aratech/portaria/chegadas/{id} | Editar chegada aguardando |
| POST | /aratech/portaria/chegadas/{id}/entrada | Confirmar entrada autorizada |
| POST | /aratech/portaria/chegadas/{id}/finalizar | Finalizar depois da entrada e enviar aos historicos |
| GET | /aratech/portaria/chegadas/historico | Historico com filtros notaFiscal e data |
| GET | /aratech/portaria/chegadas/resumo | Contadores globais e ultima chegada |
| GET | /aratech/almoxarifado/recebimentos | Cargas aguardando, autorizadas ou em recebimento |
| PUT | /aratech/almoxarifado/recebimentos/{id} | Editar carga aguardando autorizacao |
| POST | /aratech/almoxarifado/recebimentos/{id}/autorizar | Autorizar carga aguardando |
| POST | /aratech/almoxarifado/recebimentos/{id}/finalizar | Finalizar recebimento iniciado |
| GET | /aratech/almoxarifado/recebimentos/historico | Recebimentos finalizados |
| GET | /aratech/almoxarifado/recebimentos/resumo | Contadores e dados para o aviso |

Listagens recebem pagina (a partir de 0) e tamanho (1 a 100, padrao 8). O frontend usa totalElements e nao recorta novamente os resultados. A fila segue a ordem da chegada. O historico da portaria mostra somente registros finalizados com o rotulo Entrada autorizada, ordenadas pela entrada, e seu filtro data considera a data da entrada em America/Sao_Paulo. O historico do almoxarifado mostra apenas Finalizados, pela data da finalizacao.

Exemplo de cadastro no Postman, Body > raw > JSON:

```json
{
  "notaFiscal": "12345",
  "empresa": "Fornecedor Exemplo",
  "nomeMotorista": "Motorista Exemplo",
  "placa": "ABC1D23",
  "descricaoCarga": "Chapas de MDF",
  "setorResponsavel": "ALMOXARIFADO",
  "chaveOperacao": "e1db31a9-ac2b-44c4-9188-88cece7f43a9"
}
```

Use outro UUID para outra chegada; repita o mesmo UUID e os mesmos dados ao tentar novamente uma operacao sem resposta. Edicao nao exige a chave. Erros de dados retornam 400, ID ausente 404, duplicidade ou etapa invalida 409.

A tarefa de migração aplica db/migration/V8__controle_acesso.sql antes da inicialização HTTP. Nao existe sincronizacao por copia: ambos os modulos consultam o mesmo registro persistido, de modo que chegada e recebimento nao podem divergir.

## Dados de demonstracao

`db/controle-acesso-demonstracao.sql` insere 5 chegadas ficticias, compartilhadas entre as duas telas: 2 aguardando, 2 autorizadas e 1 em recebimento. Assim, cada tela mostra os mesmos 5 registros integrados. A carga e transacional, pode ser repetida sem duplicar os registros e nao sobrescreve dados existentes.

Os layouts originais das telas, botoes e modais foram preservados. As filas continuam paginadas no servidor; os historicos percorrem todas as paginas filtradas para manter a tabela original sem adicionar novos controles visuais.

A fila de espera da portaria contem apenas Aguardando e Autorizado. Uma entrada ja liberada nao permanece nessa fila com o cadeado desabilitado; ela e consultada no historico enquanto o almoxarifado continua o recebimento.
