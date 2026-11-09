# Operacoes de funcionarios

Base local: `http://localhost:8082/aratech/rh/funcionarios`.

| Operacao | Requisicao | Resultado |
|---|---|---|
| Listar | `GET ?pagina=0&tamanho=20` | Resumos, pagina, tamanho e total |
| Consultar | `GET /{id}` | Resumo do funcionario |
| Atualizar | `PUT /{id}`, Body form-data | Dados atualizados |
| Ativar | `PATCH /{id}/ativar` | Funcionario ativo |
| Desativar | `PATCH /{id}/desativar` | Funcionario inativo |

## Regras

- O ID deve ser positivo. ID invalido retorna 400; funcionario inexistente retorna 404.
- A consulta aceita paginas a partir de zero e de 1 a 100 itens por pagina; o padrao e 20.
- Consultas retornam resumos, sem CPF, salario, dados bancarios, chaves do MinIO ou documentos pessoais.
- A atualizacao e completa para os dados cadastrais: envie os mesmos campos de texto obrigatorios do cadastro. Campos opcionais omitidos sao limpos.
- Nome, CPF, nascimento, sexo, nome da mae, endereco, admissao, contrato, cargo, tipo de funcionario, setor, salario, email e dados bancarios continuam sendo validados.
- O CPF enviado deve ser o mesmo do registro. Nao e permitido trocar o CPF.
- Email e PIS nao podem pertencer a outro funcionario. O proprio registro nao conta como duplicado.
- O status nao pode ser alterado pelo formulario de atualizacao. Use os endpoints de ativacao/desativacao. A atualizacao de um funcionario inativo preserva esse status.
- Documentos omitidos na atualizacao sao preservados. Arquivos enviados substituem apenas os tipos correspondentes. Ao final, os quatro documentos obrigatorios devem existir.
- Mantidas as regras de arquivos do cadastro: PDF/JPEG/PNG, nome permitido, assinatura e conteudo de imagens, ate 5 MB por arquivo e 20 MB por envio.
- Novos uploads sao compensados se houver rollback. Documentos substituidos so sao removidos apos o commit; se essa remocao falhar, o erro e registrado para acompanhamento.
- Ativar quem ja esta ativo ou desativar quem ja esta inativo retorna 200, sem nova gravacao. Status nulo no registro retorna 409.
- Atualizacoes e mudancas de status bloqueiam o registro no banco durante a transacao para evitar sobrescrita simultanea.

## Limite de seguranca

O controle de autenticacao e permissoes foi retirado desta etapa.
