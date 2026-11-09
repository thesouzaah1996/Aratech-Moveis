# Suporte de TI

As telas existentes de abertura e acompanhamento usam `/aratech/suporte/chamados`. O esquema PostgreSQL é aplicado pelo Flyway na migração `db/migration/V13__suporte_ti.sql`.

## Endpoints

- `POST /aratech/suporte/chamados`: abre chamado (HTTP 201).
- `GET /aratech/suporte/chamados?pagina=0&tamanho=8&status=Aberto&busca=monitor`: acompanhamento paginado.
- `GET /aratech/suporte/chamados/{id}`: consulta um chamado completo; inexistente retorna 404.

O POST recebe `tipo`, `prioridade`, `moduloAfetado`, `titulo`, `solicitante`, `setor`, `contato`, `descricao`, `tentativasSolucao` e `chaveOperacao` (UUID). Os valores dos seletores são os mesmos textos das telas existentes. Módulo, contato e tentativas de solução são opcionais. Contato aceita ramal de 2 a 6 dígitos ou telefone com DDD de 10/11 dígitos; a gravação remove a máscara.

O servidor define status inicial `Aberto`, data de abertura e número `TI-AAAA-NNNN`, com sequência do banco e ano em America/Sao_Paulo. O cliente não escolhe número, data ou status. A resposta contém `status`, `mensagem` e `chamado` com os dados persistidos.

Repetir uma abertura com a mesma chave e os mesmos dados retorna o chamado original, inclusive após eventual mudança de status. Reutilizar a chave para outros dados retorna 409. O frontend preserva os dados e a chave quando o resultado do envio é incerto, permitindo nova tentativa sem duplicar o chamado.

A listagem retorna `status`, `chamados`, `pagina`, `tamanho` e `total`. A página começa em zero, tamanho entre 1 e 100. Busca por número, título, solicitante, setor, módulo e tipo, sem distinção de caixa; `%` e `_` são literais. A ordem é prioridade (Crítica, Alta, Média, Baixa), data mais antiga e ID. Filtros: `Aberto`, `Em Atendimento`, `Concluído`, `Cancelado`; omitir status lista todos.

O acompanhamento consulta novamente a cada 5 segundos e aplica debounce de 300 ms na busca. Falhas mantêm os dados anteriores com aviso. Ao sair, as consultas e os temporizadores são cancelados. Esta entrega conecta a abertura e a consulta; não adiciona atribuição nem transições administrativas de status e não envia e-mails.

## Validação

Backend: `./mvnw.cmd -q -Dtest=ChamadoTiIT,ChamadoTiValidatorTest,ChamadoTiControllerTest test`. Os testes de integração usam PostgreSQL do perfil dev e revertem seus registros ao terminar.

Frontend: testes direcionados de `chamado-ti-form`, `chamado-ti.service`, `chamado-ti.component` e `acompanhamento-ti.component`, além de `npm run build`.
