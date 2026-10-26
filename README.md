# Point Media — Banco de Talentos

Base inicial do TCC, com uma página React e um endpoint de saúde Spring Boot. Não há funcionalidades de negócio, autenticação ou conexão com banco nesta etapa.

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
    health/HealthController.java
  src/main/resources/application.properties
  src/test/java/br/com/pointmedia/health/HealthEndpointTest.java
database/schema.sql
docs/validacao.md
.gitattributes
.gitignore
README.md
```

`database/schema.sql` é a fonte oficial do schema candidato para MySQL 8. A duplicata da raiz foi removida após confirmar que os arquivos tinham SHA-256 idêntico. Nenhum SQL foi executado. A aplicação inicia sem MySQL; não inclui JDBC, JPA, Flyway ou Liquibase.

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
| Banco previsto | MySQL 8, sem integração nesta etapa |

Frontend: React e React DOM renderizam a interface; Chakra UI oferece os componentes e o sistema de temas; Emotion é sua dependência de estilos. Vite e o plugin React fornecem o servidor de desenvolvimento e o build; TypeScript e os pacotes `@types/node` 24.19.2, `@types/react` 19.3.0 e `@types/react-dom` 19.3.0 fornecem a checagem de tipos. As versões diretas estão fixadas e `package-lock.json` registra as transitivas.

O tema usa `createSystem`, `defaultConfig` e `defineConfig`, fornecidos a `ChakraProvider`. Os tokens `brand.pink` (`#EB6099`) e `brand.blue` (`#6CABDE`) ficam em `src/app/theme.ts`. A tela utiliza apenas tema claro, sem adicionar um gerenciador de alternância de cores.

Backend: `spring-boot-starter-webmvc` fornece HTTP, JSON e Tomcat embarcado; `spring-boot-starter-test`, restrito aos testes, fornece Spring Test, JUnit e AssertJ. O parent do Boot gerencia suas versões. O plugin Maven do Boot gera o JAR executável e permite execução em desenvolvimento. O teste faz uma chamada HTTP real usando o cliente do próprio Java 21.

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

O endpoint `GET http://127.0.0.1:8080/api/health` responde HTTP 200, `Content-Type: application/json` e `{"status":"UP"}`. Esse estado confirma que o processo HTTP responde; não verifica banco ou serviços externos.

Para interromper cada servidor, pressione `Ctrl+C` no respectivo terminal.

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

## Problemas comuns

- **Java 17 ainda aparece após instalar o 21:** feche e reabra o terminal e o aplicativo que o inicia, como VS Code ou Codex. Processos abertos antes da instalação mantêm as variáveis antigas. Confira `$env:JAVA_HOME` e `Get-Command java,javac`. Se um terminal novo ainda estiver incorreto, revise manualmente `JAVA_HOME` e a ordem do `Path` nas configurações de ambiente do Windows; não aponte `JAVA_HOME` para `bin`.
- **Maven usa outro Java:** `mvn.cmd -version` mostra o runtime efetivamente utilizado. `JAVA_HOME` precisa indicar o JDK 21, mesmo que `java -version` já esteja correto. Um erro como `release version 21 not supported` indica compilador incompatível.
- **Comando não encontrado:** verifique a instalação da ferramenta e sua presença no `Path`, depois abra outro terminal. `npm.cmd` evita depender da política de execução de scripts PowerShell.
- **Porta ocupada:** encerre uma execução anterior do projeto com `Ctrl+C`. As portas de desenvolvimento são 8080 e 5173; o Vite não escolhe outra porta automaticamente.
- **Backend indisponível na página:** inicie os dois servidores, confira `http://127.0.0.1:8080/api/health` e clique em **Verificar novamente**. O frontend deve ser aberto pelo Vite na porta 5173.
- **Falha ao baixar dependências:** confira acesso à rede, npm e Maven Central. Se houver proxy corporativo, use a configuração padrão da organização, sem salvar credenciais no repositório.
- **Aviso de agente do Mockito nos testes:** o starter de testes inclui Mockito transitivamente. O aviso do Java 21 não representa falha se o resultado final for `BUILD SUCCESS` com zero erros e falhas.

## Git e arquivos locais

Não versionar `.env`, credenciais, configurações pessoais, caches, JDKs locais, temporários, `node_modules`, `dist` ou `target`. Essas pastas de dependências e build são artefatos normais e podem permanecer no computador. Nenhum commit, push, merge ou remoto foi criado nesta preparação.

`.gitattributes` impede conversões de quebra de linha em `database/schema.sql`, preservando os bytes do schema aprovado no Git.

Os resultados executados, limitações e o inventário de arquivos estão em [docs/validacao.md](docs/validacao.md).
