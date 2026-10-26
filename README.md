# Point Media — Banco de Talentos

Base inicial do TCC, com uma página React, endpoint de saúde Spring Boot e conexão MySQL de desenvolvimento opcional. Não há funcionalidades de negócio ou autenticação nesta etapa.

## Organização

```text
frontend/
  src/
    app/theme.ts
    features/health/HealthPage.tsx
    main.tsx
backend/
  pom.xml
  src/main/java/br/com/pointmedia/
    PointMediaApplication.java
    database/MySqlConnectionCheck.java
    health/HealthController.java
  src/main/resources/application.properties
  src/main/resources/application-mysql.properties
  src/test/java/br/com/pointmedia/health/HealthEndpointTest.java
  src/test/java/br/com/pointmedia/health/HealthControllerTest.java
  src/test/java/br/com/pointmedia/database/MySqlConnectionTest.java
database/schema.sql
docs/validacao.md
.gitattributes
.gitignore
README.md
```

`database/schema.sql` é a fonte oficial do schema candidato para MySQL 8 e permanece intacto. A execução padrão não exige banco; a conexão é ativada somente pelo profile `mysql`. A inicialização automática de SQL está explicitamente desativada (`spring.sql.init.mode=never`). Não são utilizados JPA, Hibernate, Flyway ou Liquibase.

`src/app` concentra a configuração da aplicação. `src/features/health` contém a única tela atual. Os próximos módulos podem ocupar pastas próprias em `features` e pacotes próprios abaixo de `br.com.pointmedia`, sem criar camadas vazias antecipadamente.

## Versões e dependências

| Tecnologia | Versão utilizada |
| --- | --- |
| Node.js / npm | 24.20.0 / 11.19.0 |
| React / React DOM | 19.3.1 |
| Vite / plugin React | 8.3.5 / 6.1.2 |
| TypeScript | 6.0.3, modo estrito |
| Chakra UI / Emotion | 3.37.0 / 11.14.0 |
| Java | Temurin JDK 21.0.12.1+1 |
| Spring Boot | 4.1.2, estável |
| Maven | 3.9.16 |
| Git | 2.56.0.windows.2 |
| Banco de desenvolvimento | MySQL 8 local, profile `mysql`, banco isolado `point_media_dev` |

Frontend: React e React DOM renderizam a interface; Chakra UI oferece os componentes e o sistema de temas; Emotion é sua dependência de estilos. Vite e o plugin React fornecem o servidor de desenvolvimento e o build; TypeScript e os pacotes `@types/node` 24.19.2, `@types/react` 19.3.0 e `@types/react-dom` 19.3.0 fornecem a checagem de tipos. As versões diretas estão fixadas e `package-lock.json` registra as transitivas.

O tema usa `createSystem`, `defaultConfig` e `defineConfig`, fornecidos a `ChakraProvider`. Os tokens `brand.pink` (`#EB6099`) e `brand.blue` (`#6CABDE`) ficam em `src/app/theme.ts`. A tela utiliza apenas tema claro, sem adicionar um gerenciador de alternância de cores.

Backend: `spring-boot-starter-webmvc` fornece HTTP, JSON e Tomcat embarcado; `spring-boot-starter-jdbc` fornece Spring JDBC e o pool HikariCP; `mysql-connector-j` fornece o driver em runtime. `spring-boot-starter-test`, restrito aos testes, fornece Spring Test, JUnit e AssertJ. O parent Boot 4.1.2 gerencia as versões: Spring JDBC 7.0.10, HikariCP 7.0.2 e Connector/J 9.7.0. O plugin Maven do Boot gera o JAR executável e permite execução em desenvolvimento.

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

Crie uma conta exclusiva, como `point_media_app` restrita a `localhost`, com senha forte e `REQUIRE SSL`. Os únicos privilégios necessários nesta etapa são `SELECT ON point_media_dev.usuarios`, sem privilégios globais, de escrita ou administração. Se a conta já existir, não a sobrescreva. O administrador pode usar `CREATE USER ... IDENTIFIED BY RANDOM PASSWORD REQUIRE SSL` e guardar a senha de aplicação de forma privada; a credencial administrativa não deve ser reutilizada.

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

O profile `mysql` consulta `SELECT DATABASE()` e verifica a tabela `usuarios` na inicialização e na saúde. As consultas não retornam dados pessoais e não criam ou modificam tabelas. Não existem endpoints de administração.

## Verificação

```powershell
# Frontend
Set-Location C:\Projetos\point-media-novo\frontend
npm.cmd ci
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

O teste real confirma o banco correto, uma leitura em `usuarios`, uma conta diferente de root, TLS ativo e o endpoint HTTP. Ele utiliza apenas leitura e não prepara dados ou schema. Os resultados efetivamente executados estão no registro de validação; um teste ignorado não significa integração aprovada.

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

Não versionar `.env`, credenciais, configurações pessoais, caches, JDKs locais, temporários, `node_modules`, `dist` ou `target`. Essas pastas de dependências e build são artefatos normais e podem permanecer no computador. A fundação foi publicada no commit `186375092844b0395432e81b6cf0c4a077d49f01`; as alterações da integração MySQL permanecem locais para revisão.

`.gitattributes` impede conversões de quebra de linha em `database/schema.sql`, preservando os bytes do schema aprovado no Git.

Os resultados executados, limitações e o inventário de arquivos estão em [docs/validacao.md](docs/validacao.md).
