# Cadastro de funcionário

## Preparação

- Inicie o PostgreSQL do perfil escolhido e o MinIO configurado em `application.yml`.
- O bucket configurado em `minio.bucket-name` deve existir e as credenciais precisam permitir upload e remoção.
- A tarefa Flyway aplica `db/migration/V2__cadastro_funcionario.sql` antes de iniciar a aplicação, criando tabelas e índices quando ausentes. O processo HTTP apenas valida. Veja [operação e adoção de bancos existentes](migracoes-flyway.md).
- Se já houver CPF, e-mail ou PIS/NIT/NIS duplicados, resolva os conflitos antes de aplicar os índices.
- Reinicie o backend após atualizar as dependências.

## Requisição no Postman

Use `POST http://localhost:8082/aratech/rh/funcionarios` em dev, porta `8081` em homolog ou `8080` em prod.
Em **Body → form-data**, envie os nomes dos campos do DTO. Deixe o Postman gerar o cabeçalho `Content-Type`.

Campos de texto obrigatórios (exemplo):

| Campo | Valor |
| --- | --- |
| nomeCompleto | Ana Silva |
| cpf | 52998224725 |
| dataNascimento | 1990-01-01 |
| sexo | FEMININO |
| nomeMae | Maria Silva |
| cep | 12345678 |
| logradouro | Rua das Flores |
| numero | 10 |
| bairro | Centro |
| cidade | São Paulo |
| uf | SP |
| dataAdmissao | 2026-01-01 |
| tipoContrato | CLT |
| cargo | Analista |
| tipoFuncionario | ADMINISTRATIVO |
| setor | RH |
| salario | 3000.00 |
| email | ana@exemplo.com |
| banco | Banco Exemplo |
| agencia | 1234 |
| conta | 123456-7 |
| tipoConta | CORRENTE |

Envie como **File** os quatro documentos obrigatórios: `rgOuCnh`, `ctps`, `aso` e `comprovanteResidencia`.
Arquivos vazios são rejeitados. Datas usam `yyyy-MM-dd`; CPF, CEP, telefone e PIS são enviados sem máscara.

Campos opcionais: `rg`, `pisNitNis`, `estadoCivil`, `nomePai`, `complemento`, `telefoneFixo`,
`celularWhatsapp`, `jornadaSemanal`, `chavePix` e os arquivos `tituloEleitor`, `reservista`,
`diplomaCertificado`, `certidaoCasamento` e `certidaoNascimento`.
Omita campos opcionais sem valor. O CPF passa pela conferência dos dígitos verificadores;
isso não confirma a identidade do titular nem consulta a Receita Federal.
A admissão deve ser posterior ao nascimento. Datas de admissão futuras são permitidas.

Os documentos obrigatórios e opcionais aceitam apenas PDF, JPEG ou PNG, com até
5 MB por arquivo e 20 MB no conjunto do cadastro, validados no `addFuncionario`.
O servidor multipart aceita até 5 MB por arquivo e 21 MB por requisição (incluindo os campos e cabeçalhos). A validação de documentos mantém o limite de 20 MB de arquivos no total.
Extensão, assinatura inicial e tipo informado precisam ser compatíveis. Executáveis,
SVG, HTML, arquivos vazios e nomes com caminhos ou caracteres de controle são rejeitados.

## Resposta

HTTP **201 Created**:

```json
{
  "status": 201,
  "mensagem": "Funcionário cadastrado com sucesso.",
  "funcionarioId": 12,
  "nomeCompleto": "Ana Silva",
  "ativo": true
}
```

A resposta não expõe documentos, dados bancários nem dados de autenticação.
Campos nulos da classe `Response` não são serializados.

- **400**: dados inválidos ou documento obrigatório ausente/vazio; erros de validação incluem o mapa `erros`.
- **409**: CPF, e-mail ou PIS/NIT/NIS em uso. O e-mail é normalizado para minúsculas.
- **413**: arquivos acima do limite de upload.
- **502**: não foi possível enviar os documentos ao MinIO.
- **500**: falha inesperada, sem expor detalhes internos ao cliente.

Cada cadastro usa uma pasta com UUID no MinIO. As referências permanentes são persistidas em
`funcionario_documento`, sem URLs temporárias. Uploads parciais e uploads de transações canceladas
são removidos; se o MinIO também falhar na remoção, o erro fica registrado no log para limpeza posterior.

## Local das validações

As validações do cadastro são executadas exclusivamente pelo `addFuncionario`
da `FuncionarioServiceImp`, com métodos declarados na interface `FuncionarioService`
e implementados na própria classe. O método usa
as restrições já declaradas no DTO e confere CPF, datas, duplicidades e documentos
antes de persistir o funcionário ou enviar arquivos.
O controller encaminha os dados para a service. A `FuncionarioDocumentoService`
cuida somente do envio e da limpeza dos arquivos. O endpoint `/bucket` mantém
seu comportamento anterior e não executa as validações do cadastro de funcionário.

## Execução dos testes

Execute `.\mvnw.cmd test`. Os testes usam mocks para o banco e o MinIO e verificam cadastro,
duplicidades, validação multipart e limpeza dos documentos em falhas. Não substituem a validação
integrada com PostgreSQL e MinIO no ambiente de execução.
