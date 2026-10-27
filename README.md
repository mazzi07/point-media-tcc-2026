# Point Media — Banco de Talentos

Base do TCC com React, Spring Boot, MySQL e autenticação por sessão HTTP. Cadastro, login, consulta da sessão e logout estão publicados. O módulo de vagas está implementado localmente para revisão; currículos, candidaturas e matching ficam fora desta etapa.

## Organização

```text
frontend/
  src/
    app/theme.ts
    features/auth/AuthPanel.tsx
    features/auth/api.ts
    features/auth/session.ts
    features/auth/useSession.ts
    features/vagas/api.ts
    features/vagas/VagasPanel.tsx
    features/vagas/VagaForm.tsx
    features/health/HealthPage.tsx
    main.tsx
  tests/auth-session.test.ts
  tests/vagas-api.test.ts
  tests/vagas-ui.test.mjs
backend/
  pom.xml
  src/main/java/br/com/pointmedia/
    PointMediaApplication.java
    database/MySqlConnectionCheck.java
    auth/AuthController.java
    auth/AuthService.java
    auth/SessionAuthorizationManager.java
    usuarios/Usuario.java
    usuarios/UsuarioRepository.java
    vagas/Vaga.java
    vagas/VagaController.java
    vagas/VagaService.java
    vagas/VagaRepository.java
    config/SecurityConfig.java
    config/ApiExceptionHandler.java
    health/HealthController.java
  src/main/resources/application.properties
  src/main/resources/application-mysql.properties
  src/test/java/br/com/pointmedia/health/HealthEndpointTest.java
  src/test/java/br/com/pointmedia/health/HealthControllerTest.java
  src/test/java/br/com/pointmedia/database/MySqlConnectionTest.java
  src/test/java/br/com/pointmedia/auth/AuthSecurityTest.java
  src/test/java/br/com/pointmedia/auth/MySqlAuthTest.java
  src/test/java/br/com/pointmedia/vagas/VagaServiceTest.java
  src/test/java/br/com/pointmedia/vagas/MySqlVagaTest.java
database/schema.sql
docs/validacao.md
.gitattributes
.gitignore
README.md
```

`database/schema.sql` é a fonte oficial do schema candidato para MySQL 8 e permanece intacto. A execução padrão não exige banco; a conexão é ativada somente pelo profile `mysql`. A inicialização automática de SQL está explicitamente desativada (`spring.sql.init.mode=never`). Não são utilizados JPA, Hibernate ORM, Flyway ou Liquibase.

`src/app` concentra a configuração da aplicação. A tela inicial reúne conexão, autenticação e vagas, com cada funcionalidade em sua pasta `features`. O backend mantém os pacotes por funcionalidade abaixo de `br.com.pointmedia`.

## Versões e dependências

O código escrito manualmente usa comentários curtos em português brasileiro, imediatamente acima de métodos, ações, decisões, validações, consultas, chamadas HTTP, mudanças de estado, tratamentos de erro e cenários de teste. Os comentários explicam a finalidade do bloco, sem ponto final, cabeçalhos decorativos ou anotações em declarações triviais. Arquivos gerados e JSON não recebem comentários; o SQL aprovado permanece byte a byte idêntico.

| Tecnologia | Versão utilizada |
| --- | --- |
| Node.js / npm | 24.20.0 / 11.19.0 |
| React / React DOM | 19.3.1 |
| Vite / plugin React | 8.3.5 / 6.1.2 |
| TypeScript | 6.0.3, modo estrito |
| Chakra UI / Emotion | 3.37.0 / 11.14.0 |
| Java | Temurin JDK 21.0.12.1+1 |
| Spring Boot | 4.1.2, estável |
| Spring Security | 7.1.2, gerenciado pelo Boot |
| Maven | 3.9.16 |
| Git | 2.56.0.windows.2 |
| Banco de desenvolvimento | MySQL 8 local, profile `mysql`, banco isolado `point_media_dev` |

Frontend: React e React DOM renderizam a interface; Chakra UI oferece os componentes e o sistema de temas; Emotion é sua dependência de estilos. Vite e o plugin React fornecem o servidor de desenvolvimento e o build; TypeScript e os pacotes `@types/node` 24.19.2, `@types/react` 19.3.0 e `@types/react-dom` 19.3.0 fornecem a checagem de tipos. As versões diretas estão fixadas e `package-lock.json` registra as transitivas.

O tema usa `createSystem`, `defaultConfig` e `defineConfig`, fornecidos a `ChakraProvider`. Os tokens `brand.pink` (`#EB6099`) e `brand.blue` (`#6CABDE`) ficam em `src/app/theme.ts`. A tela utiliza apenas tema claro, sem adicionar um gerenciador de alternância de cores.

Backend: `spring-boot-starter-webmvc` fornece HTTP, JSON e Tomcat; `spring-boot-starter-jdbc` fornece JDBC/HikariCP; `mysql-connector-j` fornece o driver em runtime. `spring-boot-starter-security` fornece sessões, autenticação, autorização, BCrypt e CSRF; `spring-boot-starter-validation` valida os dados recebidos. `spring-boot-starter-test` e `spring-security-test` ficam restritos aos testes. O parent Boot 4.1.2 gerencia as versões, incluindo Security 7.1.2, Spring JDBC 7.0.10, HikariCP 7.0.2 e Connector/J 9.7.0. Nenhuma dependência nova foi necessária no frontend.

Referências oficiais consultadas: [Spring Boot estável e requisitos](https://docs.spring.io/spring-boot/system-requirements.html), [Chakra UI com Vite](https://chakra-ui.com/docs/get-started/frameworks/vite), [sistema de temas do Chakra](https://chakra-ui.com/docs/theming/overview) e [Vite](https://vite.dev/guide/).

## Iniciar no Windows com PowerShell

Requisitos: Node.js 22.12+ ou 24+, npm, JDK 21 e Maven 3.6.3+. Recomenda-se reproduzir as versões da tabela. As portas locais 8080 e 5173 precisam estar livres. Os servidores escutam somente em `127.0.0.1`.

Instale as ferramentas no Windows antes de executar o projeto. Abra um novo PowerShell após a instalação e confira:

```powershell
java -version
javac -version
mvn.cmd -version
node -v
npm.cmd -v
git --version
```

`java`, `javac` e o Java informado por Maven devem usar o JDK 21 instalado no sistema. `JAVA_HOME` deve apontar para a pasta desse JDK, e seu diretório `bin` precisa estar no `Path`. A instalação das dependências exige acesso aos registros npm e Maven Central. O projeto usa as configurações e os caches padrão das ferramentas.

Terminal 1 — backend neste workspace:

```powershell
Set-Location C:\Projetos\point-media-novo\backend
mvn.cmd clean verify
mvn.cmd spring-boot:run
```

Execute Maven a partir de `backend`. Para executar o artefato compilado, substitua o último comando por `java -jar target\point-media-0.0.1-SNAPSHOT.jar`.

Terminal 2 — frontend:

```powershell
Set-Location C:\Projetos\point-media-novo\frontend
npm.cmd ci
npm.cmd run dev
```

Abra [Point Media local](http://127.0.0.1:5173). O estado deve mostrar **Backend conectado**. A tela usa `fetch('/api/health')`, sem URL de backend embutida no código React. O Vite encaminha `/api` para `http://127.0.0.1:8080`, preservando o caminho. Isso permite uma mesma origem no navegador sem configuração CORS adicional.

O endpoint `GET http://127.0.0.1:8080/api/health` responde HTTP 200, `Content-Type: application/json` e `{"status":"UP"}`. Sem o profile `mysql`, verifica apenas a aplicação. Com esse profile, verifica também o banco de destino e a leitura da tabela `usuarios`; uma falha resulta em HTTP 503 e `{"status":"DOWN"}`, sem detalhes internos na resposta. Se o banco estiver indisponível na inicialização com esse profile, a aplicação encerra com erro.

Para interromper cada servidor, pressione `Ctrl+C` no respectivo terminal.

## MySQL local de desenvolvimento

Use exclusivamente um MySQL 8 local. O destino da aplicação é fixado em `127.0.0.1:3306/point_media_dev`; `POINT_MEDIA_DB_PORT` permite ajustar apenas a porta, se necessário. Não use um banco existente para esta preparação. Se `point_media_dev` já existir, pare e obtenha autorização antes de aplicar ou modificar o schema.

Um administrador deve preparar o banco uma única vez, usando seu próprio login-path MySQL, sem transferir sua credencial para a aplicação. Para o login-path `point-media-admin`, confira primeiro:

```powershell
mysql --login-path=point-media-admin --host=127.0.0.1 --ssl-mode=REQUIRED -e "SELECT VERSION(); SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = 'point_media_dev';"
```

Somente se o destino não existir, uma cópia em memória do SQL oficial pode ser adaptada e executada assim, a partir de `backend`:

```powershell
$schema = Get-Content ..\database\schema.sql -Raw -Encoding utf8
$schema = $schema.Replace('CREATE DATABASE IF NOT EXISTS point_media', 'CREATE DATABASE point_media_dev')
$schema = $schema.Replace('USE point_media;', 'USE point_media_dev;')
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$schema | mysql --login-path=point-media-admin --host=127.0.0.1 --ssl-mode=REQUIRED --default-character-set=utf8mb4
if ($LASTEXITCODE -ne 0) { throw 'Preparação interrompida; não reaplique sem revisar o estado do banco.' }
```

A retirada de `IF NOT EXISTS` faz a execução parar se o banco surgir entre a conferência e a criação. Não use `--force`. Não execute o SQL oficial sem adaptação, pois ele nomeia outro banco.

Crie uma conta exclusiva, como `point_media_app` restrita a `localhost`, com senha forte e `REQUIRE SSL`. Para autenticação, os privilégios são `SELECT ON point_media_dev.usuarios` e `INSERT (nome, email, senha_hash)` nessa mesma tabela. Não conceda UPDATE ou DELETE em `usuarios`, DDL ou privilégios globais. Os privilégios adicionais de vagas estão descritos abaixo. O cadastro não pode inserir `perfil` ou `ativo`: os defaults do schema aprovado fixam `USUARIO` e ativo. Se a conta já existir, não a sobrescreva. O administrador pode usar `CREATE USER ... IDENTIFIED BY RANDOM PASSWORD REQUIRE SSL` e guardar a senha de aplicação de forma privada; a credencial administrativa não deve ser reutilizada.

Neste ambiente, a ampliação aprovada foi feita somente com:

```sql
GRANT INSERT (nome, email, senha_hash) ON point_media_dev.usuarios
TO 'point_media_app'@'localhost';
```

A conexão JDBC exige TLS com `sslMode=REQUIRED`, sem habilitar `allowPublicKeyRetrieval`. Nesse modo o tráfego é cifrado, mas a identidade do certificado não é validada; a configuração é restrita a desenvolvimento em loopback. Para outro ambiente, é necessário configurar confiança e validação de identidade do servidor.

Neste computador, o banco novo já foi preparado com as 26 tabelas oficiais e a conta `point_media_app@localhost`. Não reaplique o schema. A credencial de aplicação foi guardada em `.env.mysql-app.xml`, na raiz, criptografada pelo Windows e ignorada pelo Git. Ela só pode ser importada pelo mesmo usuário no mesmo computador; não contém a credencial administrativa e não deve ser compartilhada.

Para iniciar com essa credencial local, configure apenas o ambiente do terminal:

```powershell
Set-Location C:\Projetos\point-media-novo\backend
$credential = Import-Clixml ..\.env.mysql-app.xml
$env:POINT_MEDIA_DB_USER = $credential.UserName
$env:POINT_MEDIA_DB_PASSWORD = $credential.GetNetworkCredential().Password
$env:SPRING_PROFILES_ACTIVE = 'mysql'
Remove-Variable credential
mvn.cmd spring-boot:run
```

Em outro computador, prepare seu próprio banco novo e conta local. Para fornecer a senha sem gravá-la no histórico, em `backend`:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'mysql'
$env:POINT_MEDIA_DB_USER = 'point_media_app'
$password = Read-Host 'Senha da conta de aplicação MySQL' -AsSecureString
$env:POINT_MEDIA_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $password).Password
mvn.cmd spring-boot:run
```

Não escreva senhas em comandos com valores literais, arquivos compartilhados ou histórico Git. Para voltar à execução sem banco, remova as variáveis do processo ou abra outro terminal:

```powershell
Remove-Item Env:SPRING_PROFILES_ACTIVE, Env:POINT_MEDIA_DB_USER, Env:POINT_MEDIA_DB_PASSWORD -ErrorAction SilentlyContinue
```

O profile `mysql` consulta `SELECT DATABASE()` e verifica a tabela `usuarios` na inicialização e na saúde. As consultas não retornam dados pessoais e não criam ou modificam tabelas. Não existem endpoints de administração do banco ou das credenciais.

## Cadastro, login e sessão

Inicie o backend com o profile `mysql` e o frontend pelo Vite, usando os comandos acima. Sem MySQL, a saúde continua disponível, mas as ações de autenticação retornam 503.

Na página local, abaixo da verificação de conexão:

1. Clique em **Criar conta** e informe nome, e-mail válido e senha de pelo menos 12 caracteres, limitada a 72 bytes em UTF-8 pelo BCrypt.
2. Após a confirmação, use **Entrar** com esse e-mail e senha. O nome, e-mail e perfil exibidos são confirmados por `/api/auth/me`.
3. Recarregue a página para confirmar a sessão. **Conferir sessão** consulta novamente o backend; a página também reconfirma ao recuperar o foco da janela.
4. Clique em **Sair da conta** para invalidar a sessão. Um novo acesso a `/me` retorna 401.

O cadastro público aceita somente nome, e-mail e senha. Campos extras como `perfil`, `ativo` ou `id` são rejeitados; não é possível escolher RH ou GESTOR. E-mails são normalizados e sua unicidade é garantida pelo banco. Senhas usam BCrypt com custo 12 e nunca são retornadas nas respostas. Usuários inativos não entram.

Cada requisição a uma rota protegida consulta o usuário atual antes de avaliar as permissões, por um `AuthorizationManager` integrado ao Spring Security. Usuário removido, inativo ou com perfil diferente do registrado na autenticação tem a sessão invalidada e recebe 401, exigindo novo login. Não há atualização silenciosa de autoridades nem dependência de uma consulta prévia a `/me`. A consulta de `/me` reutiliza o usuário lido nessa mesma requisição, sem uma segunda consulta JDBC. Falha de banco retorna 503 e bloqueia o acesso, preservando a possibilidade de tentar novamente. O logout nativo continua disponível sem consultar o banco.

O React distingue sessão confirmada, inválida e não verificável. Timeout, falha de rede e 503 preservam o último usuário confirmado, identificado como tal, e oferecem **Tentar novamente** e **Sair da conta**. Se a consulta falhar após login bem-sucedido, o painel apresenta o estado não verificável mesmo sem os dados do usuário. Somente 401 ou logout confirmado removem o usuário. Um erro de comunicação durante o logout não confirma a saída.

| Endpoint | Contrato |
| --- | --- |
| `GET /api/auth/csrf` | Público; devolve `headerName`, `parameterName` e `token` para a sessão. |
| `POST /api/auth/register` | Público com CSRF; JSON `{nome, email, senha}`; responde 201 com `{id, nome, email, perfil}`. |
| `POST /api/auth/login` | Público com CSRF; formulário `application/x-www-form-urlencoded`, campos `email` e `senha`; responde 204. |
| `POST /api/auth/logout` | CSRF obrigatório; invalida a sessão, limpa o cookie e responde 204. |
| `GET /api/auth/me` | Autenticado; responde `{id, nome, email, perfil}` ou 401. |

O login utiliza o filtro nativo do Spring Security. O frontend obtém CSRF antes de operações de escrita e envia o token mascarado em `X-CSRF-TOKEN`. Após login/logout, descarta o token anterior; a próxima escrita obtém o token da nova sessão. Um token expirado gera 403 com código `CSRF_INVALIDO`; o cliente renova o token e repete essa ação no máximo uma vez. Não são repetidos erros de autorização, timeout ou 503. Sessão e CSRF não são armazenados em `localStorage` ou `sessionStorage`.

O cookie `JSESSIONID` é HttpOnly, SameSite=Lax, restrito a cookies e com inatividade máxima de 30 minutos. O identificador muda no login para prevenir fixação de sessão. No desenvolvimento HTTP local, Secure fica desativado. Em produção, sirva frontend e API pela mesma origem com HTTPS e defina `POINT_MEDIA_COOKIE_SECURE=true`. TLS do MySQL também deve validar a identidade do servidor em outro ambiente.

Erros seguem `{status, code, message, errors}`. Credenciais inválidas e sessão ausente retornam 401; falta de permissão ou CSRF inválido retornam 403; dados inválidos retornam 400; e-mail duplicado retorna 409; falha do banco retorna 503. As respostas não incluem hashes, SQL ou detalhes internos.

GETs de `/api/health`, `/api/auth/csrf` e do namespace `/api/public/**` são públicos. `/api/rh/**` exige RH ou GESTOR; `/api/gestao/**` exige GESTOR. As demais rotas exigem autenticação. O namespace `/api/public/vagas` oferece a divulgação pública e `/api/rh/vagas` reúne a gestão de vagas; `/api/gestao/**` continua reservado para uso futuro. Não foi habilitado CORS geral; a SPA continua usando `/api` pelo proxy do Vite.

## Vagas — implementação local

O módulo usa somente `vagas`, `vaga_requisitos` e `vaga_versoes` do schema existente. Não há inicialização ou alteração automática de tabelas. A autenticação foi publicada no commit `be7c23c8e325e2c1ef52b6f3f99662d6c057167f`; as mudanças de vagas permanecem exclusivamente locais para revisão humana.

Visitantes e USUARIO consultam somente vagas PUBLICADAS. RH e GESTOR podem gerenciar todos os estados pelo namespace `/api/rh`; a política existente do Spring Security confere a conta e o perfil antes de cada acesso protegido. Não existe mecanismo paralelo de autorização.

| Método e caminho | Operação |
| --- | --- |
| `GET /api/public/vagas?pagina=0` | Lista pública, vinte itens por página |
| `GET /api/public/vagas/{id}` | Detalhes de vaga publicada; demais estados respondem 404 |
| `GET /api/rh/vagas?pagina=0` | Lista administrativa, vinte itens por página |
| `GET /api/rh/vagas/{id}` | Detalhes completos e versão para edição |
| `POST /api/rh/vagas` | Cria RASCUNHO com versão 1 e autoria da sessão |
| `PUT /api/rh/vagas/{id}` | Edita com `{versao, dados}` |
| `POST /api/rh/vagas/{id}/publicar` | Publica com `{versao}` |
| `POST /api/rh/vagas/{id}/encerrar` | Encerra com `{versao}` |
| `POST /api/rh/vagas/{id}/arquivar` | Arquiva com `{versao}` |

O objeto `dados` contém `titulo`, `area`, `descricao`, `modalidade`, `cidade`, `estado` e `requisitos`. Cada requisito contém `descricao` e `tipo` (`OBRIGATORIO` ou `DESEJAVEL`); a ordem da lista define `ordem`. O título é obrigatório mesmo em rascunho. Os limites seguem o schema, com descrição limitada a 10 mil caracteres e até cem requisitos por operação. Modalidades: `PRESENCIAL`, `HIBRIDO` e `REMOTO`.

Para publicar, são obrigatórios área, descrição e modalidade; presencial/híbrido também exigem cidade e UF. A edição de uma vaga publicada mantém essas validações. Somente RASCUNHO pode ser publicado, somente PUBLICADA pode ser encerrada, e qualquer estado não arquivado pode ser arquivado. ENCERRADA e ARQUIVADA não aceitam edição nesta versão. Não há reabertura ou exclusão definitiva; a ausência de DELETE preserva também vagas com candidaturas.

As escritas usam transação. A versão enviada precisa corresponder à atual; divergência retorna 409. O bloqueio da linha da vaga serializa a atualização de requisitos e a numeração do histórico; o UPDATE também exige a versão esperada. Toda alteração relevante em uma vaga que já foi publicada salva o estado anterior em `vaga_versoes`, com `versao_origem`, autor da alteração, requisitos, ordem e vínculos originais. Um envio sem mudanças não consome versão nem cria histórico. Falhas desfazem vaga, requisitos e histórico juntos.

Esta interface edita requisitos textuais. Se uma vaga já tiver requisitos vinculados a `habilidades`, alterar apenas os demais dados preserva esses vínculos. A troca dessa lista é recusada com 400 para evitar apagar relações que este formulário ainda não gerencia. Nenhuma habilidade é criada ou alterada pela aplicação nesta etapa.

Os DTOs públicos não incluem autoria, versão, estado interno ou datas administrativas. Os endpoints administrativos fornecem esses dados somente à equipe autorizada. Erros SQL continuam ocultos; erros de entrada retornam 400, ausência pública retorna 404 e conflitos de edição retornam 409.

### Privilégios MySQL de vagas

Neste ambiente, o administrador ampliou somente os privilégios abaixo, mantendo a conta de aplicação sem DDL, DELETE em vagas, alterações de histórico ou acesso global:

```sql
-- Permite criar e atualizar os dados gerenciados da vaga
GRANT SELECT, INSERT (titulo, area, descricao, modalidade, cidade, estado, criada_por),
UPDATE (titulo, area, descricao, modalidade, cidade, estado, status, versao, publicada_em)
ON point_media_dev.vagas TO 'point_media_app'@'localhost';
-- Permite substituir os requisitos da vaga editada dentro da transação
GRANT SELECT, INSERT (vaga_id, descricao, tipo, ordem), DELETE
ON point_media_dev.vaga_requisitos TO 'point_media_app'@'localhost';
-- Permite consultar e acrescentar snapshots sem alterar versões existentes
GRANT SELECT, INSERT (vaga_id, numero, versao_origem, dados, criada_por)
ON point_media_dev.vaga_versoes TO 'point_media_app'@'localhost';
```

### Utilizar no navegador

1. Inicie o backend com `mysql` e as variáveis da conta de aplicação, conforme os comandos anteriores
2. Em outro PowerShell, execute `npm.cmd run dev` em `frontend` e abra `http://127.0.0.1:5173`
3. Use a seção **Vagas** para listar e abrir as publicadas sem login
4. Entre com uma conta da aplicação que tenha perfil RH ou GESTOR e escolha **Gerenciar vagas** → **Nova vaga**
5. Salve o rascunho, abra **Editar vaga** para ajustar os dados e requisitos, e use **Publicar vaga**, **Encerrar vaga** ou **Arquivar vaga** conforme o estado

O cadastro público continua criando apenas USUARIO. A atribuição de RH/GESTOR é uma operação administrativa que exige autorização para uma conta específica; não há promoção automática, conta permanente de demonstração nem endpoint de promoção. As contas fictícias usadas na validação foram removidas.

A sessão compartilhada entre conta e vagas tem uma única origem na tela que reúne os painéis. Um 401 atual recebido em vagas remove a autenticação dos dois painéis. 403 bloqueia a operação sem encerrar a conta; 503, timeout e falha de rede também não confirmam logout. Chamadas antigas não podem limpar uma sessão criada por um novo login nem restaurar dados administrativos após a saída. A verificação por foco preserva as respostas do formulário da mesma sessão.

Erros 400 aproveitam as mensagens por campo do backend, exibidas junto aos campos e requisitos correspondentes, tanto na criação quanto na edição. Os dados preenchidos são preservados. Conflitos preservam o formulário e oferecem **Recarregar vaga e descartar alterações**. Timeout e 503 não repetem escritas; atualize a consulta para confirmar o resultado antes de tentar novamente. Estados de carregamento, lista vazia, falha e acesso negado aparecem no painel.

## Verificação

```powershell
# Frontend
Set-Location C:\Projetos\point-media-novo\frontend
npm.cmd ci
npm.cmd test
npm.cmd run build

# Backend
Set-Location C:\Projetos\point-media-novo\backend
mvn.cmd clean verify

# Com os dois servidores iniciados
Invoke-RestMethod http://127.0.0.1:8080/api/health
Invoke-RestMethod http://127.0.0.1:5173/api/health
```

O build do frontend inclui TypeScript e gera `frontend/dist`. O build do backend executa os testes e gera o JAR em `backend/target`. O proxy pertence ao servidor de desenvolvimento; `vite preview` não substitui essa configuração. Uma implantação futura deverá encaminhar `/api` para o backend no servidor responsável pela aplicação.

Os testes padrão permanecem independentes de MySQL: o teste de conexão real só é habilitado explicitamente. Com o banco novo preparado e as variáveis de aplicação configuradas, execute:

```powershell
Set-Location C:\Projetos\point-media-novo\backend
$env:POINT_MEDIA_MYSQL_TESTS = 'true'
try { mvn.cmd verify } finally { Remove-Item Env:POINT_MEDIA_MYSQL_TESTS -ErrorAction SilentlyContinue }
```

O teste de conexão anterior continua somente de leitura. O novo teste real de autenticação cadastra uma única pessoa fictícia com e-mail UUID em `example.invalid`, verifica a persistência/hash e o fluxo HTTP com cookies reais, e limpa exclusivamente essa linha em `finally`. O login-path `point-media-admin` é usado somente no teste para controlar o fixture inativo e removê-lo; a aplicação não recebe UPDATE ou DELETE. O cliente `mysql` deve estar no Path, ou ser indicado por `POINT_MEDIA_MYSQL_EXE`. Não habilite o teste real sem acesso de limpeza ao banco de desenvolvimento. Nenhuma tabela é criada/recriada pelos testes. Os testes padrão usam um repositório simulado e não exigem MySQL ou login-path administrativo.

`AuthSecurityTest` cobre cadastro, validação, duplicidade, BCrypt, login, logout, renovação de CSRF, fixação de sessão, usuário inativo e políticas de perfil. Também verifica inativação/rebaixamento sem consultar `/me`, rejeição de autoridades antigas, falha/recuperação de banco, logout durante a indisponibilidade e uma única consulta para autorização e `/me`. Os endpoints de prova de autorização existem somente nessa classe de teste, não no artefato da aplicação.

`npm.cmd test` usa o executor nativo do Node e a remoção de tipos TypeScript, sem dependências adicionais. Os testes exercitam a lógica de sessão usada pelo painel e os clientes HTTP com `fetch` controlado: timeout, rede, 503, 401, falha após login, recuperação, logout, versão na edição, campos permitidos e renovação de CSRF. Não conectam ao MySQL nem substituem a regressão HTTP real do backend.

`VagaServiceTest` cobre as regras sem banco. `MySqlVagaTest`, habilitado pela mesma variável `POINT_MEDIA_MYSQL_TESTS=true`, cobre os fluxos HTTP reais, concorrência com duas escritas simultâneas, snapshots e rollback após falha de CHECK no MySQL. O cenário de vaga publicada mantém a primeira transação aberta até a segunda tentar acessar a mesma vaga, confirma um 200 e um 409 e verifica conteúdo, requisitos, autoria e o único histórico resultante. As rotas reais também são verificadas com visitante/USUARIO e escritas de RH sem CSRF ou com token inválido. Cria suas próprias contas/vagas e, em um cenário, uma habilidade fictícia para conferir preservação de vínculos. O login-path administrativo é usado somente na preparação/limpeza desses fixtures, por IDs próprios; não altera dados existentes nem o schema. Sem a variável, os testes de MySQL são ignorados.

Para executar somente os testes de vagas com MySQL, configure as mesmas variáveis de conexão e `POINT_MEDIA_MYSQL_TESTS=true`, e use `mvn.cmd verify '-Dtest=VagaServiceTest,MySqlVagaTest'` em `backend`. A limpeza continua restrita aos dados fictícios preparados pelos próprios testes.

`tests/vagas-ui.test.mjs` contém os testes dos componentes no Edge, com respostas HTTP controladas, sem banco. Eles usam uma instalação de Playwright já disponível fora do projeto, sem adicionar pacotes ao frontend. Com o Vite iniciado, execute em outro PowerShell:

```powershell
Set-Location C:\Projetos\point-media-novo\frontend
# Informe o index.mjs de uma instalação de Playwright já disponível
$env:POINT_MEDIA_PLAYWRIGHT_MODULE = 'C:\caminho\existente\playwright\index.mjs'
# Use a porta do Vite que está em execução
$env:POINT_MEDIA_FRONTEND_URL = 'http://127.0.0.1:5173'
try {
    node --test tests/vagas-ui.test.mjs
} finally {
    Remove-Item Env:POINT_MEDIA_PLAYWRIGHT_MODULE, Env:POINT_MEDIA_FRONTEND_URL -ErrorAction SilentlyContinue
}
```

O teste de navegador é separado de `npm.cmd test`. Sem `POINT_MEDIA_PLAYWRIGHT_MODULE`, os cenários são ignorados e isso não equivale a uma aprovação. Ele cobre 401 compartilhado, 403/503/rede/timeout sem logout, respostas antigas após saída e novo login da mesma conta, resposta antiga de `/me`, falha durante logout e erros por campo com preservação dos dados durante revalidação por foco.

Os resultados efetivamente executados estão no registro de validação; um teste MySQL ignorado por padrão não significa aprovação de uma conexão real.

## Problemas comuns

- **Java 17 ainda aparece após instalar o 21:** feche e reabra o terminal e o aplicativo que o inicia, como VS Code ou Codex. Processos abertos antes da instalação mantêm as variáveis antigas. Confira `$env:JAVA_HOME` e `Get-Command java,javac`. Se um terminal novo ainda estiver incorreto, revise manualmente `JAVA_HOME` e a ordem do `Path` nas configurações de ambiente do Windows; não aponte `JAVA_HOME` para `bin`.
- **Maven usa outro Java:** `mvn.cmd -version` mostra o runtime efetivamente utilizado. `JAVA_HOME` precisa indicar o JDK 21, mesmo que `java -version` já esteja correto. Um erro como `release version 21 not supported` indica compilador incompatível.
- **Comando não encontrado:** verifique a instalação da ferramenta e sua presença no `Path`, depois abra outro terminal. `npm.cmd` evita depender da política de execução de scripts PowerShell.
- **Porta ocupada:** encerre uma execução anterior do projeto com `Ctrl+C`. As portas de desenvolvimento são 8080 e 5173; o Vite não escolhe outra porta automaticamente.
- **Backend indisponível na página:** inicie os dois servidores, confira `http://127.0.0.1:8080/api/health` e clique em **Verificar novamente**. O frontend deve ser aberto pelo Vite na porta 5173.
- **Falha com o profile `mysql`:** confira MySQL 8 ativo, banco e tabela preparados, variáveis de ambiente da conta de aplicação, porta e suporte a TLS. Não use root, não desative TLS e não reaplique o schema para tentar corrigir uma falha de conexão.
- **Falha ao baixar dependências:** confira acesso à rede, npm e Maven Central. Se houver proxy corporativo, use a configuração padrão da organização, sem salvar credenciais no repositório.
- **Aviso de agente do Mockito nos testes:** o starter de testes inclui Mockito transitivamente. O aviso do Java 21 não representa falha se o resultado final for `BUILD SUCCESS` com zero erros e falhas.

## Git e arquivos locais

Não versionar `.env`, credenciais, configurações pessoais, caches, JDKs locais, temporários, `node_modules`, `dist` ou `target`. Essas pastas de dependências e build são artefatos normais e podem permanecer no computador. A fundação foi publicada no commit `186375092844b0395432e81b6cf0c4a077d49f01` e a integração MySQL no commit `e93550eb5eed594831e4c7af04d8a9ccf30ce800`. A autenticação inclui cadastro, sessões HTTP e revogação de permissões, com os testes descritos neste documento.

As sessões ficam na memória desta instância e são perdidas ao reiniciar o backend. JWT, OAuth e recuperação de senha não fazem parte desta etapa.

## Pendências antes da exposição pública

O HTTP 409 para e-mail duplicado foi mantido por decisão explícita somente no ambiente privado de desenvolvimento. A diferença em relação ao cadastro novo permite enumeração de contas: um visitante consegue verificar se um e-mail está cadastrado. CSRF não impede esse uso pelo próprio solicitante. Antes da exposição pública, esse canal exige mitigação e revisão do comportamento de cadastro; uma mensagem genérica isolada não elimina todas as possibilidades de enumeração.

- Limitar tentativas de login e cadastro, incluindo abuso automatizado.
- Limitar o corpo JSON efetivamente lido. `server.tomcat.max-http-form-post-size` limita formulários e não fornece o limite do cadastro JSON; `@Size` valida campos depois da leitura.
- Servir frontend/API com HTTPS e configurar cookie Secure (`POINT_MEDIA_COOKIE_SECURE=true`).
- Validar a identidade do certificado TLS do MySQL. `sslMode=REQUIRED` exige cifra, mas não verifica essa identidade.

Essas medidas continuam pendentes; não foram implementadas nas correções locais de sessão e revogação.

`.gitattributes` impede conversões de quebra de linha em `database/schema.sql`, preservando os bytes do schema aprovado no Git.

Os resultados executados, limitações e o inventário de arquivos estão em [docs/validacao.md](docs/validacao.md).
