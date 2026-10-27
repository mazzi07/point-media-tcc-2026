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

## Publicação MySQL e autenticação local — 26/10/2026

Esta seção registra a etapa seguinte. As afirmações de publicação e permissões nas seções anteriores representam o estado histórico de cada validação.

### Integração MySQL publicada

O usuário aprovou a publicação dos dez arquivos da integração JDBC. Foi conferido apenas que o inventário e o histórico continuavam correspondendo ao aprovado; não foram repetidos os builds anteriores antes da publicação. O commit `feat: integra MySQL ao backend`, identificador `e93550eb5eed594831e4c7af04d8a9ccf30ce800`, foi criado em `main` e enviado a `origin/main`. `git ls-remote` confirmou o mesmo identificador no GitHub. O commit inicial foi preservado.

Nenhum commit ou push foi feito para a autenticação descrita abaixo.

### Implementação e segurança

- Dependências novas, gerenciadas pelo Boot 4.1.2: `spring-boot-starter-security` (Security 7.1.2), `spring-boot-starter-validation` e `spring-security-test` somente nos testes. Nenhuma dependência frontend foi acrescentada. Hibernate Validator é o provedor de Bean Validation, sem Hibernate ORM/JPA.
- `usuarios` utiliza somente a tabela aprovada. O repositório JDBC parametriza consultas e insere exclusivamente `nome`, `email` e `senha_hash`. Os defaults existentes fixam `USUARIO` e ativo; os campos de perfil e atividade não podem ser inseridos pela conta SQL da aplicação.
- O administrador concedeu apenas `INSERT (nome, email, senha_hash)` sobre `point_media_dev.usuarios`, mantendo o SELECT anterior. A conta continua exigindo SSL e não recebeu UPDATE, DELETE, DDL ou acesso a outras tabelas. O pool deixou de ser somente leitura para permitir o cadastro.
- Nome, e-mail e senha são validados. Nome e e-mail são aparados, e-mail é normalizado para minúsculas e duplicidade retorna 409. Campos JSON desconhecidos são rejeitados: tentativas de informar RH/GESTOR retornam 400. Senhas usam BCrypt com custo 12, mínimo de 12 caracteres e máximo de 72 bytes UTF-8. Hash e senha nunca integram os DTOs de resposta.
- Login e logout usam os filtros nativos do Spring Security, sem filtros de autenticação próprios, JWT, OAuth ou HTTP Basic. Login recebe formulário URL-encoded com `email` e `senha` e retorna 204. Usuário inativo e senha incorreta recebem a mesma resposta 401 genérica.
- Sessões usam JSESSIONID HttpOnly, SameSite=Lax, somente cookie, inatividade de 30 minutos e troca do identificador no login. Secure fica desativado apenas para o HTTP local, configurável por `POINT_MEDIA_COOKIE_SECURE=true` para HTTPS em produção.
- CSRF permaneceu ativo no cadastro, login e logout, com armazenamento nativo na sessão e token mascarado do Spring. `GET /api/auth/csrf` entrega o token ao cliente. A SPA mantém o token somente em memória e renova após login/logout ou, uma única vez, após um 403 de CSRF. Não utiliza localStorage/sessionStorage nem armazena token de autenticação acessível ao JavaScript.
- `/api/auth/me` consulta o usuário no banco e retorna somente id, nome, e-mail e perfil. Inexistência/inatividade invalida a sessão. O React confirma o estado pelo backend ao carregar, após login e ao recuperar foco; também oferece uma consulta explícita à sessão.
- Respostas de erro usam `{status, code, message, errors}`. 401 indica ausência de autenticação ou credenciais inválidas; 403 indica falta de permissão ou CSRF inválido. Erros de banco retornam 503 sem SQL ou detalhes internos.
- GETs de saúde, CSRF e do namespace futuro `/api/public/**` são públicos. `/api/rh/**` exige RH/GESTOR; `/api/gestao/**` exige GESTOR; demais rotas exigem autenticação. Não foram criados endpoints de RH/gestão: os endpoints de prova dessas políticas existem apenas na classe de teste e não são empacotados na aplicação.
- Não foi liberado CORS geral. O proxy `/api`, os tokens rosa/azul e o conteúdo da homepage foram preservados. Um painel de acesso foi acrescentado abaixo da verificação de saúde, com cadastro, login, estado autenticado, logout, carregamento, mensagens de campo, falha de conexão e sessão invalidada.

### Testes executados

| Verificação | Resultado real |
| --- | --- |
| `AuthSecurityTest`, sem banco | 17 casos passaram, cobrindo cadastro/normalização, duplicidade, BCrypt, validação, limite UTF-8, login, logout, fixação de sessão, CSRF, usuário inativo, consulta da sessão, perfis e falha do banco. |
| `mvn.cmd verify` com `POINT_MEDIA_MYSQL_TESTS=true` | `BUILD SUCCESS`: 21 testes, 0 falhas, 0 erros, 0 ignorados. Inclui todos os testes anteriores, a suíte de segurança e o novo teste HTTP com persistência MySQL real. Log local em `backend/target/auth-verification.log`. |
| Teste real `MySqlAuthTest` | Cadastro persistido, senha com BCrypt, duplicidade, tentativa de perfil, CSRF obrigatório/renovado, login inválido/válido, cookie HttpOnly/SameSite, troca real do ID da sessão, `/me`, logout e usuário inativo confirmados. |
| Modo padrão sem credenciais | Execução direcionada de `MySqlAuthTest,MySqlConnectionTest,HealthEndpointTest`: 1 teste HTTP passou e os 2 testes opt-in foram ignorados, sem conexão externa obrigatória. Log local em `backend/target/auth-default-verification.log`. |
| `npm.cmd run build` | TypeScript estrito e Vite concluídos; 1963 módulos, build Vite em 597 ms. Nenhum pacote foi atualizado. |
| Navegador via Vite | Cadastro real de pessoa fictícia, mensagem de senha incorreta, login, usuário/perfil confirmado, sessão mantida após recarregar e logout passaram. |
| Sessão invalidada no navegador | Apenas o fixture recém-criado foi temporariamente desativado pelo administrador. `/me` invalidou a sessão e a SPA exibiu aviso. Depois da reativação do mesmo fixture, novo login funcionou com renovação automática do CSRF antigo. |
| Limpeza | O teste real removeu sua linha em `finally`. O usuário fictício do navegador foi removido por e-mail UUID e nome exatos. A contagem de usuários era 0 antes e voltou a 0 ao final. Nenhum usuário existente foi alterado. |
| Encerramento | Backend e frontend de validação encerrados; portas 8080 e 5173 sem listeners. MySQL permaneceu ativo. |

A primeira rodada identificou dois ajustes: as propriedades de `AuthError` precisavam ser explícitas para o `erasableSyntaxOnly` do TypeScript, e o teste MockMvc precisava informar a validade do cookie recebido para reproduzir a troca de ID de sessão do container. Ambos foram corrigidos. A troca de ID também foi comprovada pelo teste HTTP com cookies reais. A configuração foi simplificada para usar a detecção nativa de UserDetailsService/PasswordEncoder, sem provider redundante.

### Isolamento dos testes MySQL

O teste opt-in gera e-mail UUID em `example.invalid`, confirma que ele não existe e cria exclusivamente esse usuário pelo endpoint público. A conta de aplicação permanece sem UPDATE/DELETE. O login-path administrativo é usado somente no teste para desativar seu próprio fixture e remover exatamente o e-mail criado em `finally`. Não há reset de tabela, DELETE geral, DROP, TRUNCATE ou preparação de schema. O teste de navegador usou outro UUID, com verificação e limpeza específicas. Credenciais administrativas não foram copiadas, impressas ou adicionadas ao workspace.

O teste padrão de segurança usa um repositório simulado somente no contexto `test`; isso não substituiu a validação real. Os endpoints de prova de autorização são exclusivos desse contexto de teste.

### Inventário da autenticação, ainda local

Modificados:

```text
README.md
docs/validacao.md
backend/pom.xml
backend/src/main/resources/application.properties
backend/src/main/resources/application-mysql.properties
frontend/src/features/health/HealthPage.tsx
```

Criados:

```text
backend/src/main/java/br/com/pointmedia/auth/AuthController.java
backend/src/main/java/br/com/pointmedia/auth/AuthService.java
backend/src/main/java/br/com/pointmedia/config/ApiExceptionHandler.java
backend/src/main/java/br/com/pointmedia/config/SecurityConfig.java
backend/src/main/java/br/com/pointmedia/usuarios/Usuario.java
backend/src/main/java/br/com/pointmedia/usuarios/UsuarioRepository.java
backend/src/test/java/br/com/pointmedia/auth/AuthSecurityTest.java
backend/src/test/java/br/com/pointmedia/auth/MySqlAuthTest.java
frontend/src/features/auth/AuthPanel.tsx
frontend/src/features/auth/api.ts
```

O schema oficial continua com SHA-256 `37D548EDA30E9DD6042EC6ABF4C4FFCCA27F4CDD0D0B66F6DA9DA495CB8E2253`. Manifesto e lockfile do frontend também mantiveram os hashes anteriores. Credencial criptografada, logs e captura de validação (`backend/target/auth-ui.png`) permanecem ignorados pelo Git.

Limites intencionais: sessões em memória de uma instância, sem persistência entre reinícios; HTTPS/Secure e validação de identidade TLS do banco necessários para produção; controle de tentativas abusivas deve ser tratado antes de exposição pública. Recuperação de senha, JWT, OAuth e funcionalidades de RH ficam fora do escopo. O índice Git permaneceu vazio e nenhuma alteração da autenticação foi publicada.

## Correções após auditoria de segurança — 26/10/2026

Esta seção complementa o histórico anterior. A autenticação continua exclusivamente local, sem commit ou push.

### Sessão no React

O painel passou a distinguir sessão autenticada, anônima confirmada e não verificável. Timeout, erro de rede e HTTP 503 preservam o último usuário confirmado. A interface informa que os dados são do último acesso confirmado, mantém **Tentar novamente** e **Sair da conta** e não exibe simultaneamente o formulário de login. Somente 401 ou logout confirmado removem o usuário.

Se o login retornar sucesso e a consulta seguinte falhar, o painel apresenta sessão não verificável sem inventar dados do usuário ou concluir que o login falhou. O mesmo cuidado vale para resposta de login perdida por erro de rede. Logout com falha de comunicação mantém a incerteza até uma resposta que confirme a saída ou sessão inválida.

A lógica de transição fica em `frontend/src/features/auth/session.ts`, usada diretamente pelo painel e pelos testes nativos do Node. O cliente HTTP existente foi preservado: CSRF é obtido antes das escritas, descartado após login/logout e renovado na próxima escrita. Apenas rejeição explícita de CSRF permite uma repetição; timeout e 503 não repetem operações automaticamente. Não foram adicionadas dependências.

### Revogação no servidor

`SessionAuthorizationManager` usa a extensão `AuthorizationManager` do Spring Security, sem filtro próprio. Cada requisição protegida verifica no banco se o usuário existe, está ativo e mantém exatamente o mesmo perfil da autenticação. A verificação antecede as regras de RH/GESTOR e não depende de `/api/auth/me`. Divergência invalida a sessão, limpa o contexto e retorna 401, exigindo novo login. Não são atualizadas autoridades silenciosamente.

Indisponibilidade do banco retorna 503 e impede a execução da rota protegida, sem autorizar com as autoridades antigas. A sessão pode ser verificada novamente quando o banco voltar. O logout nativo não exige essa leitura e continua funcionando durante a indisponibilidade. `/me` reutiliza o usuário consultado na autorização da própria requisição, sem segunda consulta JDBC ou cache de permissões entre requisições.

O repositório, schema e privilégios SQL não foram modificados. A conta da aplicação mantém SELECT na tabela `usuarios` e INSERT restrito a `nome`, `email` e `senha_hash`; os testes não concedem novos privilégios.

### Resultados executados

| Verificação | Resultado real |
| --- | --- |
| `npm.cmd test` | 11 testes passaram, sem falhas: timeout por AbortSignal, erro de rede, 503, 401, consulta inicial, falha após login confirmado, resposta de login perdida, recuperação, logout e renovação de CSRF. Usa Node e fetch controlado, sem pacotes de teste adicionais. |
| `npm.cmd run build` | TypeScript e Vite concluídos; 1964 módulos. |
| `mvn.cmd -B -ntp verify`, sem MySQL | BUILD SUCCESS: 26 casos contabilizados, 24 executados com sucesso e 2 testes MySQL opt-in ignorados. A suíte de segurança passou em 22 casos. Log local: `backend/target/security-corrections-verify.log`. |
| Novos testes de autorização | Inativação e rebaixamento GESTOR→RH/USUARIO sem `/me` prévio retornam 401 e invalidam a sessão. Novo login já com perfil rebaixado recebe 403 em gestão. Usuário removido também perde a sessão. |
| Banco indisponível no teste de segurança | A rota protegida retorna 503, sem acesso; a mesma sessão volta a funcionar após recuperação. Logout funciona mesmo com o repositório ainda indisponível. `/me` faz somente uma consulta ao repositório. |
| `mvn.cmd -B -ntp test '-Dtest=MySqlAuthTest'`, com MySQL | BUILD SUCCESS: 1 teste real passou, sem falhas ou skips. Regressão de cadastro, BCrypt, login, cookies, CSRF, `/me`, logout e inatividade com a conta da aplicação. O fixture UUID do próprio teste foi removido em finally e sua ausência conferida. Log local: `backend/target/security-corrections-mysql.log`. |
| Interface em navegador Edge temporário | Cinco cenários passaram usando a interface real e respostas HTTP controladas: 503 após login confirmado; recuperação sem novo login; 503 na consulta/logout preservando o usuário; logout confirmado; timeout real de 10 segundos seguido de logout. Nenhum erro React capturado. Sem acesso ao MySQL nesse teste de interface. |
| Verificação final da interface por foco | Após uma confirmação positiva, um 503 na verificação ao recuperar foco remove o aviso anterior de sessão ativa, preserva o usuário e a opção de sair. Logout confirmado funciona. Vite e navegador temporários encerrados pelo teste. |

A primeira compilação identificou que o nome do atributo usado na anotação `@RequestAttribute` precisava ser uma constante literal; foi corrigido. O teste de recuperação do banco precisou usar `doAnswer` para restabelecer o mock que lançava exceção, sem invocar esse comportamento durante a preparação. As execuções finais acima passaram. A execução inicial do Mockito foi bloqueada na comunicação de anexação do agente pelo ambiente restrito; foi encerrada e repetida em contexto autorizado, sem alteração de configuração do Windows ou dependências.

### Decisões e pendências

O HTTP 409 para e-mail duplicado permanece por decisão explícita no desenvolvimento privado. Esse comportamento permite enumeração de contas e exige mitigação/revisão antes da exposição pública. CSRF não impede um visitante de consultar esse comportamento usando sua própria sessão.

Continuam pendentes, sem implementação nesta tarefa:

- Limites de tentativas de login e cadastro.
- Limite real de corpo JSON; o limite Tomcat de formulário e as validações de campos não fornecem esse limite.
- HTTPS e cookie Secure no ambiente de publicação.
- Verificação da identidade do certificado TLS do MySQL.

### Arquivos desta correção

Modificados: `README.md`, `docs/validacao.md`, `backend/src/main/java/br/com/pointmedia/auth/AuthController.java`, `backend/src/main/java/br/com/pointmedia/config/SecurityConfig.java`, `backend/src/test/java/br/com/pointmedia/auth/AuthSecurityTest.java`, `frontend/src/features/auth/AuthPanel.tsx` e `frontend/package.json`.

Criados: `backend/src/main/java/br/com/pointmedia/auth/SessionAuthorizationManager.java`, `frontend/src/features/auth/session.ts` e `frontend/tests/auth-session.test.ts`.

Não foram alterados schema, versões de dependências, lockfile, credenciais ou o projeto antigo. Não foram adicionadas tabelas, endpoints de produção ou funcionalidades de negócio. As rotas de prova de autorização continuam restritas aos testes.

O hash do SQL oficial permaneceu `37D548EDA30E9DD6042EC6ABF4C4FFCCA27F4CDD0D0B66F6DA9DA495CB8E2253`. `git diff --check` passou e o índice Git permaneceu vazio. Os servidores temporários de validação foram encerrados. Nenhum commit ou push foi realizado.

## Comentários e preparação da publicação da autenticação — 26/10/2026

Os arquivos manuais de Java, TypeScript, HTML, Maven e Properties receberam comentários curtos em português, acima de métodos, ações, decisões, segurança, consultas e testes, sem pontuação final. JSON, lockfiles, artefatos gerados e o schema aprovado não foram comentados. A comparação dos 28 arquivos examinados, desconsiderando somente linhas de comentário, confirmou conteúdo funcional idêntico ao anterior.

A validação única antes da publicação passou: `npm.cmd test` (11 testes), `npm.cmd run build` (TypeScript e Vite) e `mvn.cmd -B -ntp verify` (24 testes executados, 2 MySQL opt-in ignorados, zero falhas/erros). A regressão real MySQL anterior foi preservada e não repetida para comentários. `git ls-remote` confirmou `origin/main` em `e93550eb5eed594831e4c7af04d8a9ccf30ce800` antes da publicação. O SQL mantém o hash aprovado.

## Publicação da autenticação e módulo de vagas local — 26/10/2026

### Publicação autorizada

A autenticação e os comentários foram publicados em main no commit be7c23c8e325e2c1ef52b6f3f99662d6c057167f, com a mensagem “feat: implementa autenticação e controle de acesso”. O push foi confirmado por git ls-remote, que devolveu o mesmo identificador em origin/main. O commit anterior e93550eb5eed594831e4c7af04d8a9ccf30ce800 foi preservado. O commit contém 30 arquivos; nenhum arquivo de vagas existia nessa publicação.

A passagem de comentários alterou 26 dos 28 arquivos manuais examinados, usando português brasileiro curto, sem ponto final, acima de métodos, blocos, decisões, validações, SQL, HTTP, estados, segurança e cenários. A comparação contra os arquivos anteriores, removendo somente linhas de comentário, confirmou ausência de alteração funcional nessa fase. Nenhum JSON, lockfile, arquivo gerado ou SQL aprovado foi comentado.

Arquivos que receberam comentários:

```text
backend/src/main/java/br/com/pointmedia/auth/AuthController.java
backend/src/main/java/br/com/pointmedia/auth/AuthService.java
backend/src/main/java/br/com/pointmedia/auth/SessionAuthorizationManager.java
backend/src/main/java/br/com/pointmedia/config/ApiExceptionHandler.java
backend/src/main/java/br/com/pointmedia/config/SecurityConfig.java
backend/src/main/java/br/com/pointmedia/database/MySqlConnectionCheck.java
backend/src/main/java/br/com/pointmedia/health/HealthController.java
backend/src/main/java/br/com/pointmedia/PointMediaApplication.java
backend/src/main/java/br/com/pointmedia/usuarios/UsuarioRepository.java
backend/src/main/resources/application-mysql.properties
backend/src/main/resources/application.properties
backend/src/test/java/br/com/pointmedia/auth/AuthSecurityTest.java
backend/src/test/java/br/com/pointmedia/auth/MySqlAuthTest.java
backend/src/test/java/br/com/pointmedia/database/MySqlConnectionTest.java
backend/src/test/java/br/com/pointmedia/health/HealthControllerTest.java
backend/src/test/java/br/com/pointmedia/health/HealthEndpointTest.java
frontend/src/app/theme.ts
frontend/src/features/auth/api.ts
frontend/src/features/auth/AuthPanel.tsx
frontend/src/features/auth/session.ts
frontend/src/features/health/HealthPage.tsx
frontend/src/main.tsx
frontend/tests/auth-session.test.ts
frontend/vite.config.ts
frontend/index.html
backend/pom.xml
```

### Implementação de vagas

O módulo usa somente as estruturas aprovadas vagas, vaga_requisitos e vaga_versoes. As leituras públicas filtram PUBLICADA no SQL e retornam DTOs sem autoria, versão, estado interno ou datas administrativas. Rascunhos, encerradas e arquivadas respondem 404 publicamente. As rotas /api/rh/vagas usam a política existente de RH/GESTOR e a revalidação da sessão; USUARIO não escreve nem consulta dados administrativos.

Rascunhos começam com versão 1. A publicação exige título válido, área, descrição e modalidade; presencial/híbrido também exigem cidade e UF. A edição publicada mantém esses requisitos. ENCERRADA e ARQUIVADA ficam somente para consulta. Arquivar nunca exclui dados. A API não fornece DELETE de vagas e a conta SQL não recebeu esse privilégio, preservando vagas mesmo quando tiverem candidaturas.

As escritas são transacionais. A linha da vaga é bloqueada para serializar alterações de requisitos/histórico; a versão enviada é conferida e o UPDATE também a exige. Conflitos retornam 409. O histórico guarda o estado anterior de uma vaga que já foi publicada, incluindo versão de origem, autoria da alteração, requisitos, ordem e habilidade_id original. Envio sem mudanças não incrementa versão. Edição só de dados da vaga mantém os requisitos intactos; substituição de requisitos vinculados a habilidades é recusada para não apagar relações que a interface textual ainda não gerencia.

A interface foi acrescentada à tela existente com os tokens institucionais. Visitantes consultam a lista e os detalhes. RH/GESTOR têm formulário de rascunho/edição, requisitos obrigatórios/desejáveis e ações de estado. São tratados carregamento, lista vazia, erros e acesso negado. Um conflito preserva o formulário até uma recarga explícita. O cliente envia somente os campos editáveis; timeout/503 não repetem escritas. Resultados administrativos de chamadas antigas são descartados ao mudar a sessão. Nenhuma homepage definitiva, candidatura, currículo ou matching foi implementado.

### Operações no MySQL

O destino point_media_dev foi confirmado antes dos GRANTs. Foram acrescentados somente:

- vagas: SELECT, INSERT em titulo/area/descricao/modalidade/cidade/estado/criada_por e UPDATE em titulo/area/descricao/modalidade/cidade/estado/status/versao/publicada_em
- vaga_requisitos: SELECT, INSERT em vaga_id/descricao/tipo/ordem e DELETE para substituir os requisitos da vaga editada
- vaga_versoes: SELECT e INSERT em vaga_id/numero/versao_origem/dados/criada_por

SHOW GRANTS confirmou esses privilégios. Não houve DDL, privilégios globais, DELETE de vagas, UPDATE/DELETE de histórico, alteração de credenciais ou uso de root pelo Spring. A conta não recebeu acesso novo a habilidades; apenas um fixture dessa tabela foi preparado pelo administrador no teste de integridade.

Os testes criaram suas próprias contas com UUID em example.invalid e vagas próprias. A limpeza usa os IDs criados, remove primeiro históricos/requisitos das próprias vagas e depois as vagas/contas. O teste de vínculo também remove sua própria habilidade fictícia. Nenhum dado existente foi usado como fixture.

### Resultados reais

| Verificação | Resultado |
| --- | --- |
| Preparação da publicação | npm test: 11 aprovados; build TypeScript/Vite aprovado; Maven verify: 24 aprovados, 2 MySQL opcionais ignorados; nenhuma alteração funcional na passagem de comentários |
| Suíte integrada com MySQL após o módulo inicial | 37 testes, zero falhas, zero erros e zero ignorados; BUILD SUCCESS; inclui todos os testes anteriores, sete unitários de vagas e quatro cenários MySQL de vagas; log local backend/target/vagas-verify.log |
| Verificação final direcionada de vagas | 12 testes aprovados: sete unitários e cinco cenários MySQL, incluindo a proteção de vínculos; zero falhas/erros/skips; BUILD SUCCESS e JAR gerado; log backend/target/vagas-integridade-final.log |
| Concorrência real | Duas edições HTTP simultâneas da mesma versão resultaram em um 200 e um 409; somente uma atualização persistida |
| Rollback real | Falha de CHECK ao inserir requisito depois da atualização/histórico reverteu vaga, requisitos e histórico; a versão e o conteúdo anteriores permaneceram iguais |
| Visibilidade e autorização | Visitante lê publicada, rascunho fica oculto, RH cria, USUARIO não cria/edita, GESTOR administra, publicação incompleta é recusada e respostas públicas não contêm campos administrativos |
| Histórico | Conteúdo e requisitos anteriores preservados, versao_origem correta; encerramento/arquivamento preservam o histórico; vínculo habilidade_id incluído no snapshot |
| Entrada e exclusão | Parâmetro/identificador inválido retorna 400; DELETE administrativo de vaga retorna 405; edição de arquivada e transições inválidas recusadas |
| Frontend final | 17 testes nativos do Node aprovados; TypeScript e Vite aprovados; nenhum pacote adicionado ou atualizado |
| Navegador com MySQL real pelo proxy | Criação, publicação inválida/válida, edição com requisitos, detalhes públicos, conflito 409 com formulário preservado, recarga explícita, histórico no MySQL, encerramento, arquivamento e logout passaram; nenhum erro React capturado |
| Limpeza | Fixtures dos testes e do navegador removidos; servidores temporários encerrados |

As falhas intermediárias foram investigadas: o cliente administrativo de limpeza precisava selecionar explicitamente point_media_dev; os fixtures da primeira execução foram identificados e removidos pelos IDs exatos 6 a 17, sem remover outros usuários. O CHECK do MySQL foi traduzido como UncategorizedSQLException, que continua sendo DataAccessException e provoca rollback; a asserção passou a aceitar essa base sem depender do tradutor do driver. No navegador, a primeira edição enviava campos administrativos junto com os dados e recebeu 400; o cliente passou a projetar somente os campos editáveis, com teste de regressão. As execuções finais acima passaram.

### Inventário local de vagas

Criados:

```text
backend/src/main/java/br/com/pointmedia/vagas/Vaga.java
backend/src/main/java/br/com/pointmedia/vagas/VagaController.java
backend/src/main/java/br/com/pointmedia/vagas/VagaRepository.java
backend/src/main/java/br/com/pointmedia/vagas/VagaService.java
backend/src/test/java/br/com/pointmedia/vagas/MySqlVagaTest.java
backend/src/test/java/br/com/pointmedia/vagas/VagaServiceTest.java
frontend/src/features/vagas/api.ts
frontend/src/features/vagas/VagaForm.tsx
frontend/src/features/vagas/VagasPanel.tsx
frontend/tests/vagas-api.test.ts
```

Modificados após o push: README.md, docs/validacao.md, backend/src/main/java/br/com/pointmedia/config/ApiExceptionHandler.java, frontend/package.json, frontend/src/features/auth/AuthPanel.tsx e frontend/src/features/health/HealthPage.tsx. AuthPanel comunica o estado da sessão à tela para mostrar as ações adequadas; não substitui a autorização do backend.

### Limites para revisão

O build Vite passou com aviso de chunk principal pouco acima de 500 kB, sem bloqueio de compilação. A divisão do bundle pode ser avaliada antes da publicação. Não há conta RH permanente de demonstração: o administrador precisa autorizar o perfil de uma conta específica para utilização da gestão; nenhum usuário existente foi promovido nesta tarefa. A interface atual gerencia requisitos textuais e impede substituir listas vinculadas a habilidades.

Permanecem as pendências de pré-publicação já registradas: mitigação da enumeração de e-mails por 409, limites de tentativas, limite real de corpo JSON, HTTPS/cookie Secure e validação da identidade TLS do MySQL. Não foram implementadas nesta etapa. Sessões continuam em memória. Nenhuma tabela, versão de dependência, lockfile, credencial ou arquivo do projeto antigo foi alterado.

O schema oficial conserva o SHA-256 37D548EDA30E9DD6042EC6ABF4C4FFCCA27F4CDD0D0B66F6DA9DA495CB8E2253. As alterações de vagas permanecem locais e fora da staging area; nenhum commit ou push foi feito após a publicação da autenticação.

A conferência final confirmou zero contas com os prefixos exclusivos dos testes de vagas e zero habilidades fictícias desses testes. Não há listeners nas portas 8080/5173; o MySQL permaneceu ativo. HEAD e origin/main continuam em be7c23c8e325e2c1ef52b6f3f99662d6c057167f, com seis arquivos modificados e dez novos apenas no workspace. O índice está vazio e git diff --check passou.


## Ajustes finais após a revisão de vagas — 26/10/2026

### Sessão compartilhada e formulário

A tela que reúne conta e vagas passou a manter uma única instância de `useSession`. `AuthPanel` recebe esse controle e não mantém outra cópia editável da sessão. `VagasPanel` comunica um 401 atual ao mesmo controle; a conta e os comandos administrativos são atualizados juntos. 403, 503, timeout e rede não confirmam logout. O aviso anterior de sessão ativa deixa de aparecer quando a sessão não está autenticada.

As ações de autenticação e o 401 confirmado invalidam marcas de chamadas anteriores. As respostas de vagas verificam essa marca e a seleção atual antes de alterar a tela. Um 401 já em trânsito não encerra um novo login da mesma conta; uma resposta administrativa antiga também não reaparece após logout. Consultas antigas de `/me` não restauram uma sessão invalidada depois por vagas. A revalidação por foco tem seu próprio controle de ordem e não descarta respostas do formulário da mesma sessão.

As mensagens de validação já fornecidas em `errors` são exibidas em `Field.ErrorText`, ao lado de título, área, descrição, modalidade, cidade, UF e de cada descrição/classificação de requisito. O formulário aceita os nomes usados na criação e o prefixo `dados.` usado na edição. A resposta 400 preserva o conteúdo digitado. Incluir/remover requisitos limpa mensagens que apontavam posições anteriores da lista.

Foram completados comentários dos blocos de ação relacionados em português simples, sem pontuação final. O README deixou de apresentar os namespaces de vagas como futuros e passou a explicar a sessão compartilhada, os erros por campo e os testes direcionados.

### Testes novos de integridade e segurança

`MySqlVagaTest.concorrenciaPublicadaPreservaRequisitosEUmUnicoHistorico` cria sua própria vaga, publica e envia duas edições HTTP com a mesma versão. Um observador de repositório exclusivo do teste mantém a primeira transação aberta depois do bloqueio SQL real até a segunda tentativa entrar no repositório. As duas chamadas executam o código SQL original; não há simulação do banco nem mudança na aplicação.

O cenário confirma um 200 e um 409 com `CONFLITO_VERSAO`, incremento de uma única versão, conteúdo e dois requisitos apenas da tentativa vencedora, ordem/classificação corretas e exatamente um histórico com número 1, versão de origem e autor da alteração vencedora. O snapshot contém título, estado, versão e os requisitos anteriores com seus IDs, ordem e vínculo original. A tentativa perdedora não deixa histórico nem alterações parciais.

`MySqlVagaTest.rotasReaisExigemPerfilECsrfSemAlterarAVaga` verifica leitura administrativa e transições negadas a visitante/USUARIO, além de criação, edição e transições de RH sem CSRF e com token inválido. Confere a resposta `CSRF_INVALIDO`, a manutenção de vaga/requisitos/versão e a ausência de histórico dessas tentativas. Nenhuma rota artificial foi adicionada.

O mecanismo de limpeza existente continuou removendo somente os dados ligados aos IDs fictícios criados em cada cenário. Não foram alterados schema, credenciais ou privilégios.

### Execuções finais

| Verificação | Resultado real |
| --- | --- |
| `mvn.cmd -B -ntp verify '-Dtest=VagaServiceTest,MySqlVagaTest'`, com MySQL opt-in | BUILD SUCCESS, 14 testes aprovados: 7 unitários e 7 cenários reais de MySQL, zero falhas/erros/skips; JAR gerado; log local `backend/target/vagas-ajustes-final.log` |
| `npm.cmd test` | 17 testes aprovados, zero falhas ou ignorados |
| `npm.cmd run build` | TypeScript e Vite aprovados, 1968 módulos; bundle principal 502,75 kB / gzip 140,70 kB, com o aviso não bloqueante de tamanho já conhecido |
| `node --test tests/vagas-ui.test.mjs`, com Playwright disponível no ambiente e Edge | 11 testes aprovados, zero falhas ou ignorados, sem erros React capturados |
| Cenários de interface | 401 compartilhado; 403/503/rede/timeout preservando a conta e permitindo logout; `/me` antigo após invalidação; 401 e 200 antigos após novo login da mesma conta; 401 antigo durante logout que responde 503; 400 na criação e edição com mensagens por campo e valores preservados mesmo durante revalidação por foco |
| Leitura final no MySQL | Destino `point_media_dev`; zero contas `pm-vaga-%@example.invalid` e zero habilidades `pm-vaga-habilidade-%` restantes |

A primeira execução do novo teste de snapshot falhou na igualdade entre árvores JSON por tipos numéricos diferentes após a releitura do MySQL. A comparação passou a conferir os valores de cada campo, incluindo IDs e ordem, sem depender do tipo de nó numérico. A execução final dos 14 testes passou.

O primeiro acesso do navegador ao Vite iniciado no ambiente restrito não conseguiu conectar. O servidor temporário foi reiniciado em contexto acessível ao Edge. A porta 5173 já estava ocupada; a validação utilizou 5174 sem interromper o processo existente. A execução final dos 11 testes passou, o navegador e o Vite temporários foram encerrados, e não restou listener em 5174. O backend dos testes usou porta aleatória e foi encerrado ao concluir o processo Maven.

Os testes de interface ficam separados de `npm.cmd test` e requerem `POINT_MEDIA_PLAYWRIGHT_MODULE` apontando para uma instalação já disponível. Não houve instalação nem dependência adicionada ao projeto. Sem essa variável, eles são ignorados; o README distingue esse caso de uma execução aprovada. As respostas HTTP desses testes são controladas; a integridade transacional foi verificada separadamente no MySQL real.

### Arquivos desta etapa

Modificados em relação ao início destes ajustes:

- `frontend/src/features/auth/AuthPanel.tsx`
- `frontend/src/features/health/HealthPage.tsx`
- `frontend/src/features/vagas/VagasPanel.tsx`
- `frontend/src/features/vagas/VagaForm.tsx`
- `backend/src/test/java/br/com/pointmedia/vagas/MySqlVagaTest.java`
- `README.md`
- `docs/validacao.md`

Criados:

- `frontend/src/features/auth/useSession.ts`
- `frontend/tests/vagas-ui.test.mjs`

As demais alterações locais do módulo de vagas anteriores a esta tarefa foram preservadas. Não houve alteração de lógica de produção do backend, dependências, versões ou lockfile nesta etapa. O SQL oficial conserva o SHA-256 `37D548EDA30E9DD6042EC6ABF4C4FFCCA27F4CDD0D0B66F6DA9DA495CB8E2253`.

Permanecem as pendências já registradas para exposição pública: enumeração de contas por 409, limites de tentativas e de corpo JSON, HTTPS/cookie Secure e identidade TLS do MySQL. A edição textual continua recusando substituição de requisitos vinculados a habilidades. O aviso de tamanho do bundle permanece para avaliação posterior.

Nenhum commit, push ou alteração da staging area foi realizado. Os ajustes continuam locais para revisão humana.
