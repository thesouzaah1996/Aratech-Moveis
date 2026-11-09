# Estoque de manutencao

A tela utiliza o layout, os botoes alinhados, os modais e o historico do estoque do almoxarifado. Os dados sao independentes: nome, codigo, quantidade, unidade, localizacao e descricao de pecas de manutencao, sem copiar produtos, categorias ou fornecedores do almoxarifado.

A implementacao segue controller, DTO, model, repository, service/imp, validator e exception handler. O Flyway aplica db/migration/V7__manutencao_estoque.sql e cria manutencao_peca_estoque e manutencao_movimentacao_estoque. Nenhuma tabela do almoxarifado e alterada.

## API

Base: /aratech/manutencao/estoque.

| Metodo | Caminho | Uso |
| --- | --- | --- |
| GET | / | Pagina com pagina, tamanho e busca |
| POST | / | Cadastro com saldo inicial |
| GET | /{id} | Detalhes da peca |
| PUT | /{id} | Edicao cadastral sem quantidade |
| DELETE | /{id} | Arquivar somente quando o saldo for zero |
| POST | /{id}/entradas | Somar quantidade no servidor |
| POST | /{id}/saidas | Retirar quantidade no servidor |
| GET | /{id}/movimentacoes | Historico paginado |

Respostas seguem status, mensagem e peca; listagem usa pecas, pagina, tamanho e total; historico usa movimentacoes. Erros seguem mensagem e erros por campo, com os mesmos codigos HTTP dos demais cadastros.

Cadastro: nome de 2 a 150 caracteres, codigo de 1 a 100 (normalizado para maiusculas), unidade un/kg/L/m/cx/par, localizacao de 2 a 100, descricao opcional ate 2000 e quantidade inicial nao negativa. Quantidades aceitam ate 3 casas decimais, no maximo 999999999999.999. Codigo e unidade permanecem fixos na edicao para preservar a identidade e o significado dos saldos. Codigos de pecas arquivadas nao podem ser reutilizados.

Entrada/saida recebem quantidade positiva, chaveOperacao UUID e motivo de 3 a 500 caracteres. O servidor bloqueia o registro durante a transacao, calcula o saldo e grava o historico atomicamente. Repetir a chave com a mesma peca, tipo, quantidade e motivo nao aplica a movimentacao novamente. Reutilizar a chave com outros dados retorna conflito. Saldo insuficiente ou acima do limite e rejeitado.

O historico registra saldo inicial, entradas e saidas, quantidade, saldos anterior e posterior, horario do servidor, motivo e o campo de responsavel no padrao atual do almoxarifado. Arquivar preserva esse historico; nao ha exclusao fisica pela API. Solicitar pecas e concluir chamados nao baixam estoque automaticamente.

EstoqueManutencaoIT valida as operacoes no PostgreSQL de desenvolvimento, incluindo duas retiradas concorrentes. Os registros transacionais sao revertidos; o teste concorrente remove somente os registros que criou. A migracao do schema permanece aplicada.
