# Scripts legados e cargas manuais

A atualização automática do banco é feita pelo **Flyway**, usando os arquivos de `src/main/resources/db/migration`.

Os scripts desta pasta são materiais legados e cargas manuais, inclusive demonstrações. Não são executados pela aplicação. Não os use em paralelo às migrações para atualizar ambientes; novas mudanças de estrutura devem ser criadas como uma nova versão Flyway.

Veja [instalação, adoção de banco existente e manutenção](../docs/migracoes-flyway.md).
