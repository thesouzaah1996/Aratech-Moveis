# Perfis de acesso do RH

Os perfis definem permissões efetivas e são atribuídos aos funcionários. O nome do perfil não concede privilégios automaticamente. Credenciais e permissões são verificadas no backend em cada requisição.

A tela em `/recursos-humanos/perfil` permite criar/editar perfis e abrir sua matriz de permissões. O diálogo filtra por setor para manter a tela curta. Atribuição de perfis é feita na opção Atribuir Perfis de Acesso. Todas as operações de gestão exigem `ACESSOS.GERENCIAR` e impedem concessão superior à do gestor.

API: `GET /aratech/rh/perfis?pagina=0&tamanho=8&busca=Operador`, `POST /aratech/rh/perfis` e `PUT /aratech/rh/perfis/{id}`.

Corpo de cadastro/edicao: `{"nome":"Operador"}`. Resposta: `{"id":1,"nome":"OPERADOR"}`.

Nomes devem conter de 3 a 100 caracteres: letras, numeros, espacos, hifen e sublinhado. Espacos extras sao normalizados e os nomes sao salvos em maiusculas. Nomes repetidos, sem diferenciar maiusculas/minusculas, retornam 409. Dados/paginacao invalidos retornam 400, ID inexistente retorna 404.

A API pagina a partir de 0; a tela mostra paginas a partir de 1. O total vem do servidor. A tabela é criada pelo Flyway em `db/migration/V3__perfis.sql`, sem cargas de perfis padrão.

O catálogo está em `GET /aratech/auth/usuarios/permissoes`; permissões de um perfil são consultadas e substituídas em `GET/PUT /aratech/auth/usuarios/perfis/{id}/permissoes`. Ações exigem também a consulta correspondente. Atualizações passam a valer na próxima requisição do usuário.

Permissões detalhadas como `RH_PONTO.CONSULTAR`, `RH_FUNCIONARIOS.ALTERAR` e `PCP_ORDENS.FINALIZAR` permitem restringir acesso por tela. As permissões antigas `RH.CONSULTAR`, por exemplo, continuam amplas: liberam todas as telas daquele setor. Remova a ampla se pretende restringir a pessoa às telas específicas; considere a união dos perfis atribuídos.

Menus, cards e botões não autorizados são removidos da interface; rotas e endpoints também bloqueiam acesso direto. Usuários não recebem novas permissões automaticamente. Não execute scripts antigos de rollback de autorização em um ambiente que usa o controle atual. Veja [operação integrada](operacao-fabrica.md) e [implantação](implantacao-producao.md).
