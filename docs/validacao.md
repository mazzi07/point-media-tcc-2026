# Registro da inicialização — 26/10/2026

Escopo: somente `C:\Projetos\point-media-novo`. O projeto antigo não foi acessado. Nenhuma funcionalidade de vagas, currículos, candidaturas, autenticação ou RH foi implementada.

## Resultados executados

| Verificação | Resultado |
| --- | --- |
| Template React + TypeScript | Gerado com `create-vite` 9.3.1; exemplos e imagens do template removidos |
| `npm.cmd install` | Concluído; auditoria inicial sem vulnerabilidades |
| `npm.cmd ci` | Instalação limpa pelo lockfile concluída; 175 pacotes adicionados e 176 auditados; nenhuma vulnerabilidade reportada |
| `npm.cmd run build` | TypeScript e build de produção concluídos |
| `mvn.cmd clean verify` com Java 21 | Compilação, teste e empacotamento concluídos; `BUILD SUCCESS` |
| Teste de integração Spring Boot | 1 executado, 0 falhas, 0 erros e 0 ignorados; servidor real em porta aleatória, valida HTTP 200, JSON e corpo do endpoint |
| Inicialização do JAR | Tomcat iniciou em `127.0.0.1:8080` com Spring Boot 4.1.2 e Java 21.0.12.1 |
| `mvn.cmd spring-boot:run` | Comando de desenvolvimento também executado; aplicação iniciou em `127.0.0.1:8080` |
| Inicialização do Vite | Servidor iniciou em `127.0.0.1:5173` |
| HTTP direto ao backend | `/api/health` retornou 200 e `{"status":"UP"}` |
| HTTP pelo proxy do Vite | `http://127.0.0.1:5173/api/health` retornou 200, JSON e `{"status":"UP"}` |
| HTML servido pelo Vite | `/` retornou HTTP 200 e `text/html` |
| React no navegador | A página exibiu **Backend conectado**, resultado do `fetch('/api/health')` real |
| Encerramento | Os servidores temporários foram encerrados; conferido que as portas 8080 e 5173 ficaram sem listeners |
| Schema | Original e cópia com o mesmo SHA-256; nenhum SQL executado |
| Git | Inicializado localmente, sem commits e sem remotos; sem push ou merge |
| Arquivos ignorados | Dependências, builds, caches, JDK portátil, `.env` e configuração local do Spring conferidos com `git check-ignore` |
| Segredos | Busca por padrões de chaves privadas, tokens e credenciais nos arquivos candidatos ao Git não encontrou ocorrências |

SHA-256 da origem `PointMedia_schema_aprovado.sql` e de `database/schema.sql`:

```text
37D548EDA30E9DD6042EC6ABF4C4FFCCA27F4CDD0D0B66F6DA9DA495CB8E2253
```

## Ocorrências e limites

- O Java configurado no Windows era o 17. Foi utilizado um JDK 21 portátil oficial, com checksum conferido, dentro de `.tools`, sem instalação global ou alteração persistente de `JAVA_HOME`/`Path`. O diretório é local e não entra no Git; outro computador precisa de seu próprio JDK 21.
- A execução inicialmente restrita não conseguia acessar os registros de pacotes ou estabelecer algumas conexões locais. As operações foram repetidas com a permissão apropriada e concluídas. Não houve mudança em firewall ou configuração do Windows.
- A primeira compilação do teste identificou que `LocalServerPort` não estava nas dependências mínimas escolhidas. O teste passou a obter a porta por `@Value("${local.server.port}")`; o build posterior concluiu com sucesso.
- O starter de testes traz Mockito transitivamente e produziu um aviso de carregamento dinâmico de agente no Java 21. O teste passou; a aplicação não utiliza mocks nem depende desse agente em execução normal.
- MySQL 8 é o banco previsto, sem conexão ou validação do servidor nesta etapa. Não foram instalados driver JDBC, JPA ou ferramentas de migração. A aplicação inicia independentemente do banco.
- A busca por segredos cobre padrões conhecidos; não constitui auditoria de segurança completa. Nenhum arquivo de credenciais foi criado.
- O schema fornecido é tratado como artefato de dados aprovado, sem executar seu conteúdo ou utilizá-lo como instrução para alterar o ambiente.
- Não há implantação de produção; o build do frontend exige um encaminhamento de `/api` no futuro ambiente de hospedagem.

## Inventário dos arquivos entregues

Todos os caminhos abaixo são relativos ao workspace autorizado. Foram criados:

```text
.gitignore
README.md
backend/.mvn/maven.config
backend/pom.xml
backend/src/main/java/br/com/pointmedia/PointMediaApplication.java
backend/src/main/java/br/com/pointmedia/health/HealthController.java
backend/src/main/resources/application.properties
backend/src/test/java/br/com/pointmedia/health/HealthEndpointTest.java
database/schema.sql
docs/validacao.md
frontend/.npmrc
frontend/index.html
frontend/package.json
frontend/package-lock.json
frontend/src/app/theme.ts
frontend/src/features/health/HealthPage.tsx
frontend/src/main.tsx
frontend/tsconfig.json
frontend/tsconfig.app.json
frontend/tsconfig.node.json
frontend/vite.config.ts
```

O arquivo preexistente `PointMedia_schema_aprovado.sql` permaneceu intacto. Os arquivos de exemplo do Vite foram gerados e removidos durante a preparação, sem fazer parte da entrega.

Artefatos locais ignorados gerados pelas verificações: `.tools/` (JDK portátil), `.cache/` (caches npm/Maven e temporários), `frontend/node_modules/`, `frontend/dist/` e `backend/target/`. O Git mantém seus metadados em `.git/`. Não houve modificação em arquivos de projeto fora do workspace nem instalação global de ferramentas.

## Padronização e limpeza — 26/10/2026

Esta seção registra a segunda etapa. O registro da inicialização acima foi preservado como histórico; as referências anteriores ao JDK portátil, caches personalizados e SQL na raiz descrevem o estado daquela validação, não a execução atual.

### Ambiente utilizado

| Ferramenta | Versão conferida | Caminho utilizado |
| --- | --- | --- |
| Java | Temurin 21.0.12.1+1 | `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe` |
| javac | 21.0.12.1 | `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\javac.exe` |
| Maven | 3.9.16 | `%USERPROFILE%\AppData\Local\Programs\Apache\apache-maven-3.9.16\bin\mvn.cmd` |
| Node.js | 24.20.0 | `C:\Program Files\nodejs\node.exe` |
| npm | 11.19.0 | `C:\Program Files\nodejs\npm.cmd` |
| Git | 2.56.0.windows.2 | `C:\Program Files\Git\cmd\git.exe` |

`mvn.cmd -version` confirmou Java 21.0.12.1, runtime em `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot`. O backend foi compilado e executado com esse JDK instalado no Windows.

A sessão já aberta do Codex ainda herdava `JAVA_HOME` e `Path` do Java 17. As variáveis permanentes do Windows já estavam corretas para o JDK 21. Os processos de validação receberam os valores permanentes atuais somente em seu próprio ambiente; nenhuma configuração global foi modificada. Para terminais futuros, reabrir o terminal e o aplicativo que o inicia evita herdar os valores antigos. Não há necessidade de alterar as configurações permanentes já verificadas.

Caches padrão confirmados: npm em `%USERPROFILE%\AppData\Local\npm-cache` e Maven em `%USERPROFILE%\.m2\repository`. As ferramentas gerenciam seus próprios caches fora da pasta do projeto; não houve edição manual de configurações fora do workspace.

### Alterações e justificativas

| Arquivo ou diretório | Ação e motivo |
| --- | --- |
| `backend/.mvn/maven.config` | Removido: continha apenas o redirecionamento do repositório Maven ao cache temporário do workspace. |
| `backend/.mvn/` | Removida após conferir que ficou vazia; não continha Maven Wrapper ou outras configurações. |
| `frontend/.npmrc` | Removido: continha apenas o redirecionamento do cache npm e a desativação do aviso de atualização. |
| `.tools/` | Removido depois dos builds com o JDK instalado; continha somente o JDK 21 portátil substituído pelo sistema. |
| `.cache/` | Removido depois dos builds com os caches padrão; continha downloads npm/Maven e temporários Java, Tomcat e Node da inicialização. |
| `PointMedia_schema_aprovado.sql` | Duplicata da raiz removida após nova comparação SHA-256. `database/schema.sql` passou a ser a fonte oficial. |
| `.gitignore` | Mantidas as exclusões de dependências, builds, caches e JDK portátil; adicionadas exclusões para caches locais npm/Maven, temporários, dumps Java e configurações pessoais com possíveis credenciais. |
| `README.md` | Comandos simplificados para o JDK do sistema e configurações padrão; estrutura atualizada e seção de problemas comuns adicionada. |
| `docs/validacao.md` | Acrescentado este registro, sem reescrever a validação original. |

Antes das exclusões, foram lidos os arquivos de configuração e pesquisadas referências aos caminhos temporários. O conteúdo dos diretórios foi inspecionado; os destinos absolutos foram conferidos como internos ao workspace e sem links de redirecionamento antes da remoção recursiva. As referências restantes aos diretórios removidos estão no histórico e nas regras de exclusão do Git.

`frontend/node_modules`, `frontend/dist` e `backend/target` permanecem como artefatos normais. A instalação limpa e o build regeneraram seus arquivos pelos comandos padrão solicitados; essas pastas não foram excluídas manualmente como limpeza. Não foram criados scripts auxiliares, instaladas ferramentas globais ou implementadas funcionalidades.

Os hashes de `frontend/package.json`, `frontend/package-lock.json` e `backend/pom.xml` foram comparados antes e depois e permaneceram iguais. Nenhuma dependência foi atualizada. O código de aplicação, a interface e a arquitetura não foram alterados.

O SHA-256 do schema oficial permanece:

```text
37D548EDA30E9DD6042EC6ABF4C4FFCCA27F4CDD0D0B66F6DA9DA495CB8E2253
```

Nenhum SQL foi executado e nenhum banco foi modificado.

### Validações executadas após retirar as configurações personalizadas

| Verificação | Resultado real |
| --- | --- |
| `java -version`, `javac -version`, `mvn.cmd -version` | JDK 21 do Windows confirmado nos processos com ambiente atualizado; sem dependência de `.tools`. |
| `node -v`, `npm.cmd -v`, `git --version` | Versões conferidas na tabela acima. |
| `npm.cmd config get cache` | Confirmou o cache padrão no perfil do usuário. |
| Maven `help:evaluate` para `settings.localRepository` | Confirmou o repositório padrão no perfil do usuário. |
| `npm.cmd ci` | 175 pacotes adicionados, 176 auditados, 0 vulnerabilidades reportadas. |
| `npm.cmd run build` | TypeScript estrito e Vite concluídos com sucesso; build Vite em 396 ms. |
| `mvn.cmd clean verify` | `BUILD SUCCESS`; 1 teste, 0 falhas, 0 erros, 0 ignorados. |
| Inicialização depois de excluir `.tools/` e `.cache/` | `mvn.cmd spring-boot:run` e `npm.cmd run dev` iniciaram nas portas 8080 e 5173. |
| HTTP direto: `http://127.0.0.1:8080/api/health` | HTTP 200, `application/json`, corpo `{"status":"UP"}`. |
| Proxy: `http://127.0.0.1:5173/api/health` | HTTP 200, `application/json`, corpo `{"status":"UP"}`. |
| Página React no navegador | Exibiu **Backend conectado** após a requisição real. |
| Encerramento | Ambos os servidores encerrados; portas 8080 e 5173 sem listeners. |
| `git status --short --untracked-files=all` | 19 arquivos candidatos, todos não rastreados, coerentes com o repositório ainda sem primeiro commit. |
| `git check-ignore -v` | Confirmou as exclusões de `node_modules`, `dist`, `target`, `.env`, caches, JDK portátil, temporários e configurações pessoais. |
| Segredos | Busca por padrões conhecidos nos arquivos candidatos sem ocorrências; isso não substitui auditoria de segurança. |
| Git | 0 commits, índice vazio e nenhum remoto; sem commit, push, merge ou integração remota. |

O aviso de carregamento de agente Mockito do registro original continuou aparecendo no teste; não houve falha e não foi feita alteração de dependências para suprimir o aviso.

### Estrutura final

```text
C:\Projetos\point-media-novo\
├── .git/
├── .gitignore
├── README.md
├── backend/
│   ├── pom.xml
│   ├── src/main/java/br/com/pointmedia/PointMediaApplication.java
│   ├── src/main/java/br/com/pointmedia/health/HealthController.java
│   ├── src/main/resources/application.properties
│   ├── src/test/java/br/com/pointmedia/health/HealthEndpointTest.java
│   └── target/                          (local, ignorado)
├── database/schema.sql
├── docs/validacao.md
└── frontend/
    ├── index.html
    ├── package.json
    ├── package-lock.json
    ├── tsconfig.json
    ├── tsconfig.app.json
    ├── tsconfig.node.json
    ├── vite.config.ts
    ├── src/main.tsx
    ├── src/app/theme.ts
    ├── src/features/health/HealthPage.tsx
    ├── node_modules/                    (local, ignorado)
    └── dist/                            (local, ignorado)
```

Os comandos definitivos de instalação, compilação, execução e testes estão no `README.md`. Não há pendência funcional identificada nesta padronização. A sessão antiga do aplicativo deve ser reaberta para passar a herdar automaticamente o JDK 21 já configurado no Windows. MySQL continua fora do escopo desta base.

## Auditoria Git e preparação do primeiro commit — 26/10/2026

A raiz do repositório foi confirmada como o workspace autorizado. A verificação inicial encontrou 19 arquivos candidatos, índice vazio, zero commits e nenhum remoto. A branch local foi renomeada de `master` para `main`. O remoto previsto não foi acessado nem configurado. Não foi executado commit, push, pull ou merge.

### Revisão individual

| Arquivo candidato | Resultado da inspeção |
| --- | --- |
| `.gitignore` | Exclusões de dependências, builds, credenciais, caches, ferramentas locais e temporários conferidas com `git check-ignore -v`. |
| `README.md` | Estrutura, versões, comandos PowerShell, portas e limitações coerentes com os arquivos atuais; caminhos do workspace são exemplos documentais, não configurações da aplicação. |
| `backend/pom.xml` | Java 21 e Boot 4.1.2; dependências mínimas, testes restritos ao escopo de teste, sem banco ou credenciais. |
| `backend/src/main/java/br/com/pointmedia/PointMediaApplication.java` | Inicialização convencional do Spring Boot. |
| `backend/src/main/java/br/com/pointmedia/health/HealthController.java` | Endpoint de saúde retorna apenas o estado HTTP da aplicação. |
| `backend/src/main/resources/application.properties` | Somente nome da aplicação, endereço de loopback e porta; sem caminhos de máquina ou credenciais. |
| `backend/src/test/java/br/com/pointmedia/health/HealthEndpointTest.java` | Teste usa servidor real em porta aleatória e valida status 200, tipo JSON e corpo. |
| `database/schema.sql` | Apenas definição de estrutura, sem carga de dados pessoais ou credenciais; SHA-256 original preservado, sem execução de SQL. |
| `docs/validacao.md` | Histórico revisado; referências ao nome do perfil do Windows substituídas por `%USERPROFILE%`, mantendo os resultados históricos. |
| `frontend/index.html` | Documento em português, título e ponto de montagem coerentes. |
| `frontend/package.json` | Versões fixadas, scripts convencionais, requisito de Node e ausência de comandos ou configurações pessoais. |
| `frontend/package-lock.json` | JSON válido, lockfile v3, dependências diretas iguais ao manifesto. Todos os registros de pacotes inspecionados por estrutura: URLs HTTPS do registro npm, sem autenticação embutida, com versão e integridade. Instalação limpa reproduzida. |
| `frontend/src/app/theme.ts` | Provider utiliza sistema Chakra 3 com os dois tokens institucionais centralizados. |
| `frontend/src/features/health/HealthPage.tsx` | Requisição relativa `/api/health`, validação da resposta, timeout e cancelamento; sem dados pessoais ou regras de negócio. |
| `frontend/src/main.tsx` | Ponto de entrada com StrictMode e ChakraProvider. |
| `frontend/tsconfig.app.json` | Checagem estrita, configuração de bundler e artefatos intermediários em diretório ignorado. |
| `frontend/tsconfig.json` | Referências locais aos dois projetos TypeScript. |
| `frontend/tsconfig.node.json` | Configuração estrita para o Vite, sem caminhos específicos do computador. |
| `frontend/vite.config.ts` | Proxy de desenvolvimento `/api` para loopback na porta 8080; porta 5173 fixa, sem credenciais ou caminhos de máquina. |
| `.gitattributes` | Adicionado nesta auditoria para impedir conversões de quebra de linha no SQL aprovado e preservar seus bytes no Git. |

O Git local utiliza `core.autocrlf=true`. A regra específica `database/schema.sql -text` permite preservar o hash original tanto no índice quanto em futuros checkouts, sem alterar o arquivo SQL ou a configuração global do Git. Com esse arquivo adicional, o inventário aprovado contém 20 arquivos.

Não foram encontrados erros aparentes que exigissem alterações de código, versões de dependências ou arquitetura. Os únicos ajustes nos arquivos compartilhados foram a anonimização do perfil local neste histórico e a proteção do schema em `.gitattributes`.

### Validação técnica repetida

- `npm.cmd ci`: concluído; 175 pacotes adicionados, 176 auditados, nenhuma vulnerabilidade reportada.
- `npm.cmd run build`: TypeScript e Vite concluídos, build Vite em 434 ms.
- `mvn.cmd clean verify`: `BUILD SUCCESS`, 1 teste, 0 falhas, 0 erros e 0 ignorados. Maven confirmou o JDK 21 instalado no Windows.
- A sessão antiga ainda herdava Java 17; os processos Maven receberam somente em seu ambiente os valores permanentes já corretos do Windows, sem alterar configurações globais.
- O próprio teste fez HTTP real para `/api/health` e validou HTTP 200, `application/json` e `{"status":"UP"}`. Não foi necessário iniciar servidores de desenvolvimento adicionais nesta auditoria; a execução de testes terminou normalmente.
- O aviso já documentado de agente Mockito permaneceu informativo; não houve alteração de dependências para suprimi-lo.

A revisão de segurança abrangeu leitura dos arquivos, estrutura do lockfile, URLs de download, padrões de credenciais, chaves privadas, tokens, caminhos locais e presença de dados pessoais. Nenhum segredo ou dado pessoal desnecessário foi identificado no inventário aprovado após a anonimização documental. Uma busca por padrões conhecidos não equivale a uma garantia absoluta de ausência de segredos.

O schema aprovado mantém SHA-256 `37D548EDA30E9DD6042EC6ABF4C4FFCCA27F4CDD0D0B66F6DA9DA495CB8E2253`. Nenhum SQL foi executado, nenhum banco foi alterado e o projeto legado não foi acessado.

Mensagem reservada para um futuro commit, que não foi criado: `chore: inicializa base do projeto Point Media`.

### Preparação do índice

Somente os 20 arquivos aprovados foram adicionados por lista explícita. `git diff --cached --check` não encontrou problemas de whitespace; `git diff --cached --stat`, `git diff --cached --name-only` e `git status --short` confirmaram o inventário integral como adições. A releitura dos blobs do índice não encontrou segredos, artefatos indevidos ou caminhos pessoais; seus bytes corresponderam aos arquivos revisados no workspace, inclusive o SHA-256 do schema. O README também passou a listar e explicar `.gitattributes`.

Estado preparado para revisão humana: branch `main`, 20 arquivos staged, nenhuma alteração aprovada pendente fora do índice, zero commits e nenhum remoto configurado. Nenhum arquivo foi publicado.

## Publicação da fundação e integração JDBC local — 26/10/2026

### Publicação autorizada

O usuário confirmou como correto o commit raiz já existente `186375092844b0395432e81b6cf0c4a077d49f01`, mensagem `chore: inicializa base do projeto`, e autorizou sua publicação sem recriá-lo ou alterar sua mensagem. O comando `git push -u origin main` criou a branch remota. `git ls-remote origin refs/heads/main` confirmou o mesmo identificador no GitHub, em `mazzi07/point-media-tcc-2026`, privado.

Não houve repetição dos builds da fundação antes desse push. Não foram feitos novos commits ou pushes para a integração MySQL descrita a seguir; ela permanece local para revisão humana.

### Implementação local

- Dependências adicionadas: `spring-boot-starter-jdbc` e `com.mysql:mysql-connector-j` em runtime, com versões gerenciadas pelo Boot 4.1.2. Artefato compilado contém Spring JDBC 7.0.10, HikariCP 7.0.2 e Connector/J 9.7.0.
- O profile `mysql` habilita a conexão; sem esse profile a auto-configuração de DataSource fica excluída, preservando a execução padrão sem banco. O teste HTTP original utiliza o profile `test`, independente do MySQL.
- Inicialização automática de SQL explicitamente desativada com `spring.sql.init.mode=never`. Nenhum ORM ou ferramenta de migração foi introduzido.
- Destino JDBC restrito ao loopback e ao banco `point_media_dev`; a porta é a única parte opcional configurável, por `POINT_MEDIA_DB_PORT`. Usuário e senha são fornecidos por variáveis de ambiente sem valores padrão.
- TLS obrigatório, pool pequeno em modo de leitura e timeouts limitados. `sslMode=REQUIRED` cifra o tráfego, mas não valida a identidade do certificado; esta configuração é exclusivamente local e exigiria validação de identidade para outro ambiente.
- `MySqlConnectionCheck` confirma o banco selecionado e a existência/leitura da tabela `usuarios`, sem retornar dados pessoais ou escrever no banco. É executado na inicialização com o profile `mysql` e nas verificações de saúde desse profile.
- Falha de inicialização com banco indisponível encerra o processo com erro. Falha posterior na verificação de saúde retorna apenas HTTP 503 e `{"status":"DOWN"}`. Sem o profile MySQL, o contrato anterior HTTP 200 e `{"status":"UP"}` é preservado.
- O teste real `MySqlConnectionTest` é habilitado somente por `POINT_MEDIA_MYSQL_TESTS=true`. Verifica leitura da tabela, banco correto, conta diferente de root, TLS e o endpoint HTTP, sem criar dados ou schema. O teste unitário do tratamento de erro não substitui essa validação real.

### Verificações concluídas nesta fase

| Verificação | Resultado real |
| --- | --- |
| Servidor MySQL local | Serviço `MySQL80` ativo; cliente MySQL Community 8.0.46 instalado. |
| `mvn.cmd clean verify` | `BUILD SUCCESS`; 2 testes executados com sucesso e 1 teste real MySQL ignorado por padrão, sem falhas ou erros. |
| Execução padrão do JAR | Iniciou em `127.0.0.1:8080` sem banco; HTTP 200 e `{"status":"UP"}`. Servidor encerrado após verificar. |
| Banco indisponível na inicialização | Profile `mysql` apontado para uma porta local previamente conferida sem servidor. Falha real de conexão detectada; processo terminou com código 1. O MySQL compartilhado não foi parado ou alterado. Log local ignorado em `backend/target/mysql-unavailable.log`. |
| Tratamento de falha HTTP | Teste unitário confirmou HTTP 503 e corpo `{"status":"DOWN"}`, sem detalhes internos. |

A compilação inicial do novo teste revelou uma inferência ambígua na asserção do cipher TLS; foi corrigida com uma variável local de tipo explícito e o build posterior passou. A sessão antiga continua exigindo que os processos de validação herdem os valores permanentes atuais de Java 21; não houve modificação de configuração global do Windows.

Nesse ponto da preparação, a criação do banco e o teste real ainda dependiam de autenticação administrativa funcional. O login-path `point-media-admin` havia sido informado pelo usuário, mas o cliente desta sessão não encontrava essa seção. Nenhuma credencial administrativa foi copiada, impressa ou armazenada no workspace. A resolução e os resultados finais estão registrados abaixo.

### Diagnóstico da diferença entre os clientes MySQL

O usuário confirmou autenticação funcional no PowerShell externo e forneceu somente metadados de ambiente: mesma identidade Windows, mesmo APPDATA e ausência de `MYSQL_TEST_LOGIN_FILE`, mas arquivo de login com 172 bytes. Tanto a execução de comandos quanto o runtime JavaScript desta sessão enxergaram 24 bytes no caminho padrão, com a mesma data de alteração.

Foi localizada uma cópia privada de 24 bytes em `%LOCALAPPDATA%\Packages\OpenAI.Codex_2p2nqsd0c76g0\LocalCache\Roaming\MySQL\.mylogin.cnf`, com metadados iguais aos vistos pelo cliente desta sessão. Isso identifica a divergência de visibilidade do arquivo de configuração, sem indicar falha na senha. O diagnóstico utilizou metadados; nenhum conteúdo de credencial foi exibido ou copiado.

A remoção exclusiva dessa cópia privada foi proposta, condicionada a autorização adicional, pois o arquivo está fora do workspace. O arquivo original de 172 bytes e as credenciais existentes devem ser preservados. Nenhuma alteração externa foi realizada durante esse diagnóstico.

### Resolução autorizada e preparação do banco

O usuário autorizou expressamente a remoção exclusiva da cópia privada de 24 bytes. Antes da remoção, seu tamanho e a ausência de redirecionamento por link foram novamente conferidos. Foi removido somente esse arquivo; o cliente passou a enxergar o original de 172 bytes e autenticou pelo login-path, usando TLS. O original manteve o mesmo tamanho e a mesma data de alteração ao final. Nenhuma senha foi redefinida, nenhuma credencial administrativa foi copiada ou armazenada no workspace e nenhuma configuração global do Windows foi alterada.

Com autenticação funcional, confirmou-se MySQL Community 8.0.46 em `127.0.0.1:3306`. Antes de qualquer criação, consultas somente de leitura comprovaram que `point_media_dev` e `point_media_app@localhost` não existiam. O destino foi confirmado novamente imediatamente antes de aplicar o SQL.

O schema oficial foi validado pelo SHA-256 aprovado. Uma cópia exclusivamente em memória teve apenas `CREATE DATABASE IF NOT EXISTS point_media` substituído por `CREATE DATABASE point_media_dev` e `USE point_media;` por `USE point_media_dev;`. O comando não usou `--force`; uma criação concorrente do banco faria a execução abortar, sem reutilizar um banco existente. Foram criadas as 26 tabelas previstas. Não houve DROP, TRUNCATE, carga de dados ou alterações em outros bancos.

A conta nova `point_media_app@localhost` exige SSL e recebeu somente `SELECT ON point_media_dev.usuarios`. `SHOW GRANTS` confirmou esse privilégio e apenas `USAGE` global, que não concede acesso adicional; o atributo SSL da conta também foi verificado. A aplicação e os testes reais utilizaram essa conta, sem root.

A senha de aplicação foi gerada aleatoriamente e salva como `PSCredential` criptografada pelo Windows em `.env.mysql-app.xml`, na raiz do workspace. Essa é uma credencial nova e exclusiva da aplicação, diferente da credencial administrativa. O arquivo é ignorado por `.env.*`, não contém senha em texto aberto, é restrito ao mesmo usuário/computador pela proteção do Windows e não foi adicionado ao Git. Sua importação forneceu as variáveis de ambiente somente aos processos de execução e testes. O README documenta esses comandos e a alternativa de entrada segura da senha em outros computadores.

### Resultados finais com MySQL real

| Verificação | Resultado real |
| --- | --- |
| `mvn.cmd verify` com `POINT_MEDIA_MYSQL_TESTS=true` | `BUILD SUCCESS`; 3 testes, 0 falhas, 0 erros e 0 ignorados. |
| Teste `MySqlConnectionTest` | Banco `point_media_dev`, leitura de `usuarios`, conta diferente de root, cipher TLS não vazio e endpoint HTTP 200 confirmados. |
| Testes existentes e tratamento de erro | Teste HTTP sem banco e teste de resposta 503 passaram na mesma suíte. |
| `mvn.cmd spring-boot:run`, profile `mysql` | Inicialização real com o JDK 21 do sistema e a conta de aplicação, pool JDBC conectado ao MySQL. |
| HTTP direto ao backend | `http://127.0.0.1:8080/api/health`: 200, `application/json`, `{"status":"UP"}`. |
| HTTP pelo proxy do Vite | `http://127.0.0.1:5173/api/health`: 200, `application/json`, `{"status":"UP"}`. |
| Página no navegador | Exibiu **Backend conectado** com o profile MySQL ativo. |
| Banco indisponível | A verificação real anterior contra porta local sem servidor terminou com código 1; nenhum servidor compartilhado foi desligado. O teste unitário adicional confirmou o contrato 503 sem detalhes internos. |
| Dados | `usuarios` permaneceu com 0 registros; os testes usam somente consultas de leitura e não preparam ou removem dados. |
| Encerramento | Frontend e backend encerrados; portas 5173 e 8080 sem listeners. O serviço MySQL existente permaneceu ativo. |
| Schema oficial | SHA-256 permanece `37D548EDA30E9DD6042EC6ABF4C4FFCCA27F4CDD0D0B66F6DA9DA495CB8E2253`. |
| Git | HEAD e `origin/main` permanecem em `186375092844b0395432e81b6cf0c4a077d49f01`, com um único commit. Índice vazio; integração somente no workspace, sem novo commit ou push. |

Não foram repetidos builds do frontend, pois seu código e dependências não mudaram. O Vite foi apenas iniciado para verificar a comunicação com o backend conectado ao banco real.

### Inventário local após o primeiro push

Modificados:

```text
README.md
backend/pom.xml
backend/src/main/java/br/com/pointmedia/health/HealthController.java
backend/src/main/resources/application.properties
backend/src/test/java/br/com/pointmedia/health/HealthEndpointTest.java
docs/validacao.md
```

Criados, ainda não rastreados:

```text
backend/src/main/java/br/com/pointmedia/database/MySqlConnectionCheck.java
backend/src/main/resources/application-mysql.properties
backend/src/test/java/br/com/pointmedia/database/MySqlConnectionTest.java
backend/src/test/java/br/com/pointmedia/health/HealthControllerTest.java
```

O arquivo criptografado `.env.mysql-app.xml`, relatórios e logs em `backend/target` e os artefatos normais do frontend ficam locais e ignorados. A única alteração externa ao workspace foi a remoção pontual da cópia privada expressamente autorizada pelo usuário.

A pendência de autenticação foi resolvida e a integração local passou nos testes reais. Permanecem os limites intencionais: TLS local sem validação da identidade do certificado, conta com leitura somente da tabela usada nesta etapa e credencial criptografada não portável entre usuários/computadores. Não há JPA, Hibernate, Flyway, Liquibase, autenticação ou funcionalidades de negócio. Nenhum arquivo dessa integração foi publicado.
