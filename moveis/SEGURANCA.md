# Autenticação e autorização

O Angular usa o mesmo domínio da API (`/aratech` por proxy). O Spring Security mantém a autenticação na sessão do servidor. Nenhum JWT, senha ou perfil autorizado é persistido no armazenamento do navegador.

## Primeiro administrador

Também há suporte a uma conta técnica inicial, separada do quadro do RH. Sua criação é uma operação local explícita, pelo modo `aratech.security.admin-temporario`, com usuário e senha recebidos pela entrada privada do processo. A aplicação nunca cria essa conta automaticamente. A credencial temporária permite somente consultar a sessão, sair e definir uma nova senha; os módulos ficam bloqueados até a troca por uma senha que atenda à política. A troca revoga a sessão temporária e exige novo login. O cadastro local recusa sobrescrever uma conta técnica existente.

Escolha explicitamente um funcionário ativo já cadastrado. No PowerShell, dentro de `moveis`, execute `./configurar-administrador.ps1 -FuncionarioId ID_ESCOLHIDO`, substituindo `ID_ESCOLHIDO` pelo número do funcionário.

O script compila o JAR e pede a senha duas vezes com entrada oculta. A senha segue por um pipe privado para um processo Java sem servidor HTTP; não vai na linha de comando, em arquivo ou em variável de ambiente. O banco recebe apenas o hash. A inicialização é recusada quando já existe administrador. Não existe conta ou senha padrão. O administrador é um atributo de credencial configurado localmente: criar um perfil chamado ADMIN ou Administrador não concede esse atributo.

Depois inicie o backend normalmente e entre com o e-mail cadastrado do funcionário. Para os demais usuários:

1. Em **Criar Perfis de Acesso**, cadastre os nomes e use a ação de escudo para definir as permissões.
2. Em **Atribuir Perfil**, localize o funcionário e atribua um perfil existente.
3. Na mesma janela, **Criar acesso com senha temporária** cria a credencial e exibe uma senha aleatória. Entregue-a somente à pessoa selecionada. O login usa o e-mail cadastrado; os módulos ficam bloqueados até a troca obrigatória da senha. Contas existentes não são sobrescritas.

Perfis existentes e seus vínculos foram preservados. Começam sem permissões até que elas sejam concedidas explicitamente. A permissão `ACESSOS.GERENCIAR` permite administrar perfis, atribuições e credenciais temporárias, dentro das permissões que o próprio gestor possui. A API impede conceder privilégios superiores e criar credenciais para assumir contas de usuários com mais privilégios. Alterar o e-mail de login também exige essa capacidade. O último administrador ativo não pode ser desativado pela API.

## Sessões e senhas

- Cookie `ARATECHSESSION`: `HttpOnly`, `SameSite=Strict`, caminho `/`, sem persistência além da sessão do navegador e com `Secure` fora do perfil local `dev`.
- Sessão expira após 30 minutos sem uso e, no máximo, 8 horas após a autenticação. O identificador é trocado no login. Logout invalida a sessão no servidor.
- A cada requisição, a API consulta usuário ativo, versão de credencial e permissões atuais. Desativação, reativação, alteração de e-mail e troca de senha revogam sessões anteriores. Mudanças de permissões valem na próxima requisição.
- CSRF é obrigatório inclusive no login e logout. O Angular busca `/aratech/auth/csrf` e envia o token somente em requisições de escrita à própria API. O token é renovado após login/logout; operações de escrita não são repetidas automaticamente.
- Hash PBKDF2-HMAC-SHA256 com 600.000 iterações, sal aleatório de 16 bytes e identificação de algoritmo para migrações futuras.
- Novas senhas: 8 a 128 caracteres, frases e espaços permitidos. O backend usa zxcvbn com estimativa mínima de 100 milhões de tentativas e considera também termos previsíveis da aplicação. O limite usa a estimativa diretamente, pois a classificação arredondada da biblioteca pode dar nível 2 até mesmo a uma senha aleatória de oito caracteres. Senhas comuns, sequências, repetições e substituições previsíveis são avaliadas localmente, sem enviar a senha para serviços externos. Esta política foi adaptada para permitir senhas menores; sem MFA, a recomendação de comprimento da OWASP continua sendo 15 caracteres. A avaliação de força é heurística, não equivale à verificação de toda a base de senhas comprometidas nem substitui MFA.
- Limite persistido no PostgreSQL: 10 tentativas por identificador e 50 por IP em uma janela de 15 minutos. Respostas de erro de login são iguais para conta inexistente, inativa e senha incorreta. Contadores expiram e são removidos.
- Logs de autenticação registram ID em sucesso e hashes de identificador/origem em falha; nunca senha, hash de senha, ID da sessão.

## Implantação

Produção exige HTTPS no endereço público, no proxy de borda e nas conexões expostas. Configure `SPRING_PROFILES_ACTIVE=prod` (ou `homolog`). O cookie seguro não funciona em HTTP de produção.

Configure o proxy para encaminhar `/aratech` ao backend do ambiente correto. O `nginx.conf` do frontend inclui cabeçalhos de proteção para o navegador. Não habilite logs de corpos de requisição no proxy de borda. Não aceite `X-Forwarded-For` enviado diretamente por clientes: a limitação usa o endereço remoto do servidor. Se houver proxy externo, configure explicitamente a confiança somente nesse proxy antes de usar endereços encaminhados.



Sessões são mantidas na memória do único backend e terminam ao reiniciá-lo. Uma implantação com várias instâncias deve usar armazenamento compartilhado de sessões antes de distribuir as requisições entre servidores.

## Validação

`./mvnw.cmd '-Dtest=SegurancaSessaoIT,FuncionarioControllerTest,PerfilServiceImpTest,AtribuicaoPerfilIT' test` valida a autenticação com o PostgreSQL local, incluindo isolamento de permissões, CSRF, logout, revogação, throttling e flags reais do cookie no servidor HTTP. As contas de teste usam transações revertidas. Os contadores utilizam somente IP de teste reservado e identificadores aleatórios.

No frontend: testes de `auth.service.spec.ts` e dos serviços/telas de funcionários e perfis, além de `npm run build`.

Referências: [OWASP Authentication](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html), [OWASP Session Management](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html), [OWASP Password Storage](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html), [Spring Security CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html).

A aplicação não oferece recuperação de senha por e-mail. A troca de senha exige uma sessão autenticada e a senha atual. A senha temporária de novas contas deve ser trocada no primeiro login.

Em Funcionários, a ação de envelope permite a gestores com `ACESSOS.GERENCIAR` cadastrar um e-mail corporativo como login e gerar senha temporária aleatória. O e-mail pessoal é preservado. A credencial fica armazenada apenas como hash; o funcionário precisa trocar a senha antes de acessar módulos. Contas existentes não são sobrescritas. Essa ação cria o acesso à aplicação, sem provisionar uma caixa postal em um provedor externo.
