# Guia de uso na aplicação

No menu da conta, abra **Documentação do Sistema**. A rota do frontend é `/documentacao` e exige uma sessão válida. A tela não consulta dados pessoais ou operacionais e não concede acesso aos setores descritos.

O guia tem introdução para os primeiros acessos, três caminhos entre setores, busca sem distinção de maiúsculas/acentos, filtro por setor, orientações em etapas, cuidados antes da confirmação, exemplos e dúvidas frequentes. Inclui Comercial, PCP, Almoxarifado, Portaria, Carregamento, Compras, Financeiro, Manutenção, Assistência, RH, Administração e Suporte.

O conteúdo acompanha os serviços e as telas reais. Explica, por exemplo, que confirmar uma encomenda cria produção e cobrança; autorizar caminhão não movimenta estoque; receber compra cria conta a pagar; registrar baixa não executa pagamento bancário; aprovar afastamento não calcula folha.

O conteúdo fica em `aratechFrontend/aratech-movies-frontend/src/app/componente/documentacao/guia-sistema.ts`. Ao alterar uma regra de negócio, atualize a orientação correspondente e o Swagger afetado. O guia de uso e a documentação técnica da API são telas distintas; o Swagger permanece com autenticação real e renovação automática de CSRF.

Validação: build do frontend concluído e três testes aprovados no Chrome Headless, cobrindo apresentação dos 12 setores, busca sem acentos e filtros com recuperação de resultados vazios. O build mantém os avisos de tamanho do bundle inicial e seletores do Bootstrap.
