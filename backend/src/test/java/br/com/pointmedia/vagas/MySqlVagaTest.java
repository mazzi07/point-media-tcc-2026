package br.com.pointmedia.vagas;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("mysql")
@EnabledIfEnvironmentVariable(named = "POINT_MEDIA_MYSQL_TESTS", matches = "true")
class MySqlVagaTest {

    private static final String PASSWORD = "Senha-ficticia-vagas-2026!";
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PasswordEncoder encoder;
    @Autowired private ObjectMapper mapper;
    @Autowired private VagaService service;
    @MockitoSpyBean private VagaRepository repository;
    @Value("${local.server.port}") private int port;
    private final Map<String, Long> autores = new HashMap<>();
    private final Map<String, HttpClient> clientes = new HashMap<>();
    private final List<HttpClient> abertos = new ArrayList<>();

    // Cria apenas contas fictícias exclusivas deste cenário
    @BeforeEach
    void preparar() throws Exception {
        admin("SELECT 1");
        String hash = encoder.encode(PASSWORD);
        for (String perfil : List.of("RH", "GESTOR", "USUARIO")) {
            String email = "pm-vaga-" + UUID.randomUUID() + "@example.invalid";
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM usuarios WHERE email = ?", Long.class, email)).isZero();
            jdbc.update("INSERT INTO usuarios (nome, email, senha_hash) VALUES (?, ?, ?)", "Teste fictício de vagas", email, hash);
            long id = jdbc.queryForObject("SELECT id FROM usuarios WHERE email = ?", Long.class, email);
            autores.put(perfil, id);
            // O administrador altera o perfil somente da conta recém-criada
            admin("UPDATE point_media_dev.usuarios SET perfil = '" + perfil + "' WHERE id = " + id);
            var client = cliente();
            var response = enviar(client, "POST", "/api/auth/login", "email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                    + "&senha=" + URLEncoder.encode(PASSWORD, StandardCharsets.UTF_8), true);
            assertThat(response.statusCode()).isEqualTo(204);
            clientes.put(perfil, client);
        }
    }

    // Remove somente as vagas e contas pertencentes aos IDs criados neste cenário
    @AfterEach
    void limpar() throws Exception {
        try {
            if (!autores.isEmpty()) {
                String ids = String.join(",", autores.values().stream().map(String::valueOf).toList());
                admin("DELETE h FROM point_media_dev.vaga_versoes h JOIN point_media_dev.vagas v ON v.id=h.vaga_id WHERE v.criada_por IN (" + ids + ");"
                        + " DELETE r FROM point_media_dev.vaga_requisitos r JOIN point_media_dev.vagas v ON v.id=r.vaga_id WHERE v.criada_por IN (" + ids + ");"
                        + " DELETE FROM point_media_dev.vagas WHERE criada_por IN (" + ids + ");"
                        + " DELETE FROM point_media_dev.habilidades WHERE criada_por IN (" + ids + ") AND nome_normalizado LIKE 'pm-vaga-habilidade-%';"
                        + " DELETE FROM point_media_dev.usuarios WHERE id IN (" + ids + ") AND email LIKE 'pm-vaga-%@example.invalid';");
                assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM vagas WHERE criada_por IN (" + ids + ")", Long.class)).isZero();
            }
        } finally {
            for (var client : abertos) client.close();
        }
    }

    // Confere o ciclo completo, a proteção dos dados e o histórico de requisitos
    @Test
    void cicloPublicoAdministrativoEHistorico() throws Exception {
        var rh = clientes.get("RH");
        var gestor = clientes.get("GESTOR");
        var usuario = clientes.get("USUARIO");
        var visitante = cliente();
        var vaga = criar(rh, new Vaga.Dados("Rascunho fictício", null, null, null, null, null, List.of()));
        assertThat(vaga.status()).isEqualTo("RASCUNHO");
        assertThat(enviar(visitante, "GET", "/api/public/vagas/" + vaga.id(), null, false).statusCode()).isEqualTo(404);
        assertThat(enviar(rh, "POST", "/api/rh/vagas/" + vaga.id() + "/publicar", new Vaga.AlteracaoEstado(1), false).statusCode()).isEqualTo(400);
        // USUARIO não ganha permissão mesmo enviando CSRF e dados válidos
        assertThat(enviar(usuario, "POST", "/api/rh/vagas", dados("Negada"), false).statusCode()).isEqualTo(403);
        assertThat(enviar(usuario, "PUT", "/api/rh/vagas/" + vaga.id(), new Vaga.Edicao(1, dados("Negada")), false).statusCode()).isEqualTo(403);
        assertThat(enviar(gestor, "GET", "/api/rh/vagas", null, false).statusCode()).isEqualTo(200);
        vaga = ler(enviar(rh, "PUT", "/api/rh/vagas/" + vaga.id(), new Vaga.Edicao(1, dados("Vaga fictícia publicada")), false), 200);
        vaga = ler(enviar(gestor, "POST", "/api/rh/vagas/" + vaga.id() + "/publicar", new Vaga.AlteracaoEstado(vaga.versao()), false), 200);
        var publica = enviar(visitante, "GET", "/api/public/vagas/" + vaga.id(), null, false);
        assertThat(publica.statusCode()).isEqualTo(200);
        // Nenhum campo de autoria, versão ou estado administrativo sai na resposta pública
        for (String campo : List.of("criadaPor", "versao", "status", "criadaEm", "atualizadaEm")) {
            assertThat(mapper.readTree(publica.body()).has(campo)).isFalse();
        }
        var lista = enviar(visitante, "GET", "/api/public/vagas", null, false);
        assertThat(lista.statusCode()).isEqualTo(200);
        assertThat(lista.body()).contains("Vaga fictícia publicada").doesNotContain("criadaPor", "versao", "RASCUNHO");
        assertThat(enviar(usuario, "GET", "/api/public/vagas/" + vaga.id(), null, false).statusCode()).isEqualTo(200);
        // Guarda o conteúdo anterior ao alterar texto e requisitos já publicados
        int origem = vaga.versao();
        var novos = new Vaga.Dados("Vaga fictícia revisada", "Tecnologia", "Descrição revisada", "REMOTO", null, null,
                List.of(new Vaga.Requisito("Experiência com Java", "OBRIGATORIO"), new Vaga.Requisito("Conhecer testes", "DESEJAVEL")));
        vaga = ler(enviar(rh, "PUT", "/api/rh/vagas/" + vaga.id(), new Vaga.Edicao(origem, novos), false), 200);
        String snapshot = jdbc.queryForObject("SELECT dados FROM vaga_versoes WHERE vaga_id = ? AND numero = 1", String.class, vaga.id());
        assertThat(mapper.readTree(snapshot).get("titulo").asString()).isEqualTo("Vaga fictícia publicada");
        assertThat(mapper.readTree(snapshot).get("requisitos").get(0).get("descricao").asString()).isEqualTo("Requisito original");
        assertThat(jdbc.queryForObject("SELECT versao_origem FROM vaga_versoes WHERE vaga_id = ? AND numero = 1", Integer.class, vaga.id())).isEqualTo(origem);
        vaga = ler(enviar(gestor, "POST", "/api/rh/vagas/" + vaga.id() + "/encerrar", new Vaga.AlteracaoEstado(vaga.versao()), false), 200);
        assertThat(vaga.status()).isEqualTo("ENCERRADA");
        assertThat(enviar(visitante, "GET", "/api/public/vagas/" + vaga.id(), null, false).statusCode()).isEqualTo(404);
        vaga = ler(enviar(rh, "POST", "/api/rh/vagas/" + vaga.id() + "/arquivar", new Vaga.AlteracaoEstado(vaga.versao()), false), 200);
        assertThat(vaga.status()).isEqualTo("ARQUIVADA");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM vaga_versoes WHERE vaga_id = ?", Long.class, vaga.id())).isEqualTo(3L);
    }

    // Duas edições simultâneas não podem salvar usando a mesma versão
    @Test
    void concorrenciaReal() throws Exception {
        var vaga = criar(clientes.get("GESTOR"), dados("Concorrência fictícia"));
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var primeira = executor.submit(() -> enviar(clientes.get("RH"), "PUT", "/api/rh/vagas/" + vaga.id(), new Vaga.Edicao(1, dados("Primeira edição")), false));
            var segunda = executor.submit(() -> enviar(clientes.get("GESTOR"), "PUT", "/api/rh/vagas/" + vaga.id(), new Vaga.Edicao(1, dados("Segunda edição")), false));
            assertThat(List.of(primeira.get().statusCode(), segunda.get().statusCode())).containsExactlyInAnyOrder(200, 409);
        }
        assertThat(service.administrativa(vaga.id()).versao()).isEqualTo(2);
    }

    // Mantém a primeira transação aberta até a segunda tentar bloquear a mesma vaga publicada
    @Test
    void concorrenciaPublicadaPreservaRequisitosEUmUnicoHistorico() throws Exception {
        var rascunho = criar(clientes.get("RH"), dados("Publicada antes da disputa"));
        var publicada = service.alterarEstado(rascunho.id(), 1, "PUBLICADA", autores.get("RH"));
        var requisitosAnteriores = jdbc.queryForList(
                "SELECT id, habilidade_id, descricao, tipo, ordem FROM vaga_requisitos WHERE vaga_id = ? ORDER BY ordem, id", publicada.id());
        var segundaEntrou = new CountDownLatch(1);
        var primeiraBloqueou = new CountDownLatch(1);
        var tentativas = new AtomicInteger();
        // O observador só sincroniza o teste e executa o bloqueio SQL verdadeiro nas duas chamadas
        doAnswer(invocation -> {
            int tentativa = tentativas.incrementAndGet();
            // A segunda tentativa só avança quando a primeira já segura a linha
            if (tentativa == 2) {
                assertThat(primeiraBloqueou.await(5, TimeUnit.SECONDS)).isTrue();
                segundaEntrou.countDown();
            }
            var resultado = invocation.callRealMethod();
            if (tentativa == 1) {
                primeiraBloqueou.countDown();
                assertThat(segundaEntrou.await(5, TimeUnit.SECONDS)).isTrue();
            }
            return resultado;
        }).when(repository).bloquear(publicada.id());
        var primeiraEdicao = new Vaga.Dados("Conteúdo da primeira tentativa", "Tecnologia", "Descrição primeira", "REMOTO", null, null,
                List.of(new Vaga.Requisito("Obrigatório primeiro", "OBRIGATORIO"), new Vaga.Requisito("Desejável primeiro", "DESEJAVEL")));
        var segundaEdicao = new Vaga.Dados("Conteúdo da segunda tentativa", "Tecnologia", "Descrição segunda", "REMOTO", null, null,
                List.of(new Vaga.Requisito("Obrigatório segundo", "OBRIGATORIO"), new Vaga.Requisito("Desejável segundo", "DESEJAVEL")));
        // Dispara duas requisições com a mesma versão e espera a conclusão das duas antes da limpeza
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var primeira = executor.submit(() -> enviar(clientes.get("RH"), "PUT", "/api/rh/vagas/" + publicada.id(),
                    new Vaga.Edicao(publicada.versao(), primeiraEdicao), false));
            var segunda = executor.submit(() -> enviar(clientes.get("GESTOR"), "PUT", "/api/rh/vagas/" + publicada.id(),
                    new Vaga.Edicao(publicada.versao(), segundaEdicao), false));
            var respostaPrimeira = primeira.get(15, TimeUnit.SECONDS);
            var respostaSegunda = segunda.get(15, TimeUnit.SECONDS);
            assertThat(List.of(respostaPrimeira.statusCode(), respostaSegunda.statusCode())).containsExactlyInAnyOrder(200, 409);
            var venceuPrimeira = respostaPrimeira.statusCode() == 200;
            var vencedora = ler(venceuPrimeira ? respostaPrimeira : respostaSegunda, 200);
            var perdedora = venceuPrimeira ? respostaSegunda : respostaPrimeira;
            assertThat(mapper.readTree(perdedora.body()).get("code").asString()).isEqualTo("CONFLITO_VERSAO");
            // Confere todo o conteúdo vencedor sem requisitos ou campos da tentativa perdedora
            assertThat(service.administrativa(publicada.id())).isEqualTo(vencedora);
            assertThat(vencedora.versao()).isEqualTo(publicada.versao() + 1);
            var esperados = venceuPrimeira ? primeiraEdicao : segundaEdicao;
            assertThat(vencedora.titulo()).isEqualTo(esperados.titulo());
            assertThat(vencedora.descricao()).isEqualTo(esperados.descricao());
            assertThat(vencedora.requisitos()).containsExactlyElementsOf(esperados.requisitos());
            var requisitosAtuais = jdbc.queryForList("SELECT tipo, ordem FROM vaga_requisitos WHERE vaga_id = ? ORDER BY ordem, id", publicada.id());
            assertThat(requisitosAtuais).hasSize(2);
            assertThat(requisitosAtuais.get(0)).containsEntry("tipo", "OBRIGATORIO").containsEntry("ordem", 0);
            assertThat(requisitosAtuais.get(1)).containsEntry("tipo", "DESEJAVEL").containsEntry("ordem", 1);
            // O único snapshot guarda a versão e os vínculos anteriores à disputa
            var historicos = jdbc.queryForList("SELECT numero, versao_origem, criada_por, dados FROM vaga_versoes WHERE vaga_id = ?", publicada.id());
            assertThat(historicos).hasSize(1);
            var historico = historicos.getFirst();
            assertThat(historico).containsEntry("numero", 1).containsEntry("versao_origem", publicada.versao())
                    .containsEntry("criada_por", autores.get(venceuPrimeira ? "RH" : "GESTOR"));
            var snapshot = mapper.readTree(historico.get("dados").toString());
            assertThat(snapshot.get("titulo").asString()).isEqualTo(publicada.titulo());
            assertThat(snapshot.get("versao").asInt()).isEqualTo(publicada.versao());
            assertThat(snapshot.get("status").asString()).isEqualTo("PUBLICADA");
            assertThat(snapshot.get("requisitos").size()).isEqualTo(requisitosAnteriores.size());
            // Compara os valores sem depender do tipo numérico escolhido ao reler o JSON do MySQL
            for (int index = 0; index < requisitosAnteriores.size(); index++) {
                var esperado = requisitosAnteriores.get(index);
                var requisito = snapshot.get("requisitos").get(index);
                assertThat(requisito.get("id").asLong()).isEqualTo(((Number) esperado.get("id")).longValue());
                assertThat(requisito.get("habilidade_id").isNull()).isTrue();
                assertThat(requisito.get("descricao").asString()).isEqualTo(esperado.get("descricao"));
                assertThat(requisito.get("tipo").asString()).isEqualTo(esperado.get("tipo"));
                assertThat(requisito.get("ordem").asInt()).isEqualTo(((Number) esperado.get("ordem")).intValue());
            }
        }
        assertThat(tentativas.get()).isEqualTo(2);
    }

    // Confere autorização e CSRF nos próprios caminhos de vagas sem criar rotas artificiais
    @Test
    void rotasReaisExigemPerfilECsrfSemAlterarAVaga() throws Exception {
        var rh = clientes.get("RH");
        var vaga = criar(rh, dados("Proteção das rotas fictícias"));
        // Visitante e usuário comum não leem nem executam as operações administrativas
        for (var client : List.of(cliente(), clientes.get("USUARIO"))) {
            int esperado = client == clientes.get("USUARIO") ? 403 : 401;
            assertThat(enviar(client, "GET", "/api/rh/vagas", null, false).statusCode()).isEqualTo(esperado);
            assertThat(enviar(client, "GET", "/api/rh/vagas/" + vaga.id(), null, false).statusCode()).isEqualTo(esperado);
            for (String acao : List.of("publicar", "encerrar", "arquivar")) {
                assertThat(enviar(client, "POST", "/api/rh/vagas/" + vaga.id() + "/" + acao,
                        new Vaga.AlteracaoEstado(vaga.versao()), false).statusCode()).isEqualTo(esperado);
            }
        }
        // Mesmo RH autenticado precisa de CSRF válido em cada escrita
        var caminhos = List.of("/api/rh/vagas", "/api/rh/vagas/" + vaga.id(), "/api/rh/vagas/" + vaga.id() + "/publicar",
                "/api/rh/vagas/" + vaga.id() + "/encerrar", "/api/rh/vagas/" + vaga.id() + "/arquivar");
        for (String caminho : caminhos) {
            String metodo = caminho.equals("/api/rh/vagas/" + vaga.id()) ? "PUT" : "POST";
            for (boolean tokenInvalido : List.of(false, true)) {
                var pedido = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + caminho))
                        .timeout(Duration.ofSeconds(10)).header("Content-Type", "application/json");
                if (tokenInvalido) pedido.header("X-CSRF-TOKEN", "token-invalido");
                var resposta = rh.send(pedido.method(metodo, HttpRequest.BodyPublishers.ofString("{}")).build(), HttpResponse.BodyHandlers.ofString());
                assertThat(resposta.statusCode()).isEqualTo(403);
                assertThat(mapper.readTree(resposta.body()).get("code").asString()).isEqualTo("CSRF_INVALIDO");
            }
        }
        // As recusas não mudam versão, conteúdo, requisitos ou histórico
        assertThat(service.administrativa(vaga.id())).isEqualTo(vaga);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM vaga_versoes WHERE vaga_id = ?", Long.class, vaga.id())).isZero();
    }

    // Uma falha SQL depois do histórico e da atualização desfaz toda a operação
    @Test
    void rollbackRealDeVagaRequisitosEHistorico() throws Exception {
        var vaga = criar(clientes.get("RH"), dados("Rollback fictício"));
        var publicada = service.alterarEstado(vaga.id(), 1, "PUBLICADA", autores.get("RH"));
        // A chamada direta simula uma falha interna que a validação HTTP normalmente impediria
        var invalido = new Vaga.Dados("Não deve persistir", "Tecnologia", "Descrição válida", "REMOTO", null, null,
                List.of(new Vaga.Requisito("Tipo recusado pelo CHECK do MySQL", "INVALIDO")));
        assertThatThrownBy(() -> service.editar(vaga.id(), new Vaga.Edicao(publicada.versao(), invalido), autores.get("RH")))
                .isInstanceOf(DataAccessException.class);
        assertThat(service.administrativa(vaga.id())).isEqualTo(publicada);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM vaga_versoes WHERE vaga_id = ?", Long.class, vaga.id())).isZero();
    }

    // Recusa transições e campos inválidos sem aceitar autoria ou estado enviados pelo cliente
    @Test
    void validacoesEEstados() throws Exception {
        var rh = clientes.get("RH");
        // Parâmetros inválidos são erros de entrada e não falhas internas
        assertThat(enviar(cliente(), "GET", "/api/public/vagas/abc", null, false).statusCode()).isEqualTo(400);
        assertThat(enviar(cliente(), "GET", "/api/public/vagas?pagina=-1", null, false).statusCode()).isEqualTo(400);
        assertThat(enviar(rh, "POST", "/api/rh/vagas", Map.of("titulo", "Teste", "requisitos", List.of(), "status", "PUBLICADA"), false).statusCode()).isEqualTo(400);
        var vaga = criar(rh, dados("Estados fictícios"));
        // A API não oferece exclusão definitiva de vagas
        assertThat(enviar(rh, "DELETE", "/api/rh/vagas/" + vaga.id(), null, false).statusCode()).isEqualTo(405);
        assertThat(enviar(rh, "POST", "/api/rh/vagas/" + vaga.id() + "/encerrar", new Vaga.AlteracaoEstado(1), false).statusCode()).isEqualTo(400);
        var presencial = new Vaga.Dados("Sem local", "Tecnologia", "Descrição", "PRESENCIAL", null, null, List.of());
        vaga = ler(enviar(rh, "PUT", "/api/rh/vagas/" + vaga.id(), new Vaga.Edicao(1, presencial), false), 200);
        assertThat(enviar(rh, "POST", "/api/rh/vagas/" + vaga.id() + "/publicar", new Vaga.AlteracaoEstado(vaga.versao()), false).statusCode()).isEqualTo(400);
        vaga = ler(enviar(rh, "POST", "/api/rh/vagas/" + vaga.id() + "/arquivar", new Vaga.AlteracaoEstado(vaga.versao()), false), 200);
        assertThat(enviar(rh, "PUT", "/api/rh/vagas/" + vaga.id(), new Vaga.Edicao(vaga.versao(), dados("Recusada")), false).statusCode()).isEqualTo(400);
    }

    // Mantém vínculos existentes sem implementar edição de habilidades
    @Test
    void preservaRequisitoVinculadoEHistoricoCompleto() throws Exception {
        var rh = clientes.get("RH");
        var vaga = criar(rh, dados("Vínculo fictício"));
        String habilidade = "pm-vaga-habilidade-" + UUID.randomUUID();
        admin("INSERT INTO habilidades (nome, nome_normalizado, criada_por) VALUES ('Habilidade fictícia', '" + habilidade + "', " + autores.get("RH") + ");"
                + " UPDATE vaga_requisitos SET habilidade_id = (SELECT id FROM habilidades WHERE nome_normalizado = '" + habilidade + "') WHERE vaga_id = " + vaga.id());
        vaga = service.alterarEstado(vaga.id(), 1, "PUBLICADA", autores.get("RH"));
        // Alterar somente o título mantém os requisitos e o vínculo original
        var titulo = new Vaga.Dados("Título revisado", vaga.area(), vaga.descricao(), vaga.modalidade(), vaga.cidade(), vaga.estado(), vaga.requisitos());
        vaga = service.editar(vaga.id(), new Vaga.Edicao(vaga.versao(), titulo), autores.get("RH"));
        String snapshot = jdbc.queryForObject("SELECT dados FROM vaga_versoes WHERE vaga_id = ? AND numero = 1", String.class, vaga.id());
        assertThat(mapper.readTree(snapshot).get("requisitos").get(0).get("habilidade_id").isNull()).isFalse();
        // Trocar requisitos vinculados é recusado e não deixa alterações parciais
        var anterior = vaga;
        var novos = new Vaga.Dados(vaga.titulo(), vaga.area(), vaga.descricao(), vaga.modalidade(), null, null,
                List.of(new Vaga.Requisito("Outro requisito", "DESEJAVEL")));
        assertThat(enviar(rh, "PUT", "/api/rh/vagas/" + vaga.id(), new Vaga.Edicao(vaga.versao(), novos), false).statusCode()).isEqualTo(400);
        assertThat(service.administrativa(vaga.id())).isEqualTo(anterior);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM vaga_versoes WHERE vaga_id = ?", Long.class, vaga.id())).isEqualTo(1L);
    }

    // Prepara dados completos de divulgação sem usar registros existentes
    private Vaga.Dados dados(String titulo) {
        return new Vaga.Dados(titulo, "Tecnologia", "Descrição fictícia", "REMOTO", null, null,
                List.of(new Vaga.Requisito("Requisito original", "OBRIGATORIO")));
    }

    // Confere a criação antes de usar o identificador devolvido
    private Vaga criar(HttpClient client, Vaga.Dados dados) throws Exception {
        return ler(enviar(client, "POST", "/api/rh/vagas", dados, false), 201);
    }

    // Lê a vaga sem ocultar uma resposta inesperada do teste
    private Vaga ler(HttpResponse<String> response, int status) {
        assertThat(response.statusCode()).withFailMessage("Status inesperado: %s", response.body()).isEqualTo(status);
        return mapper.readValue(response.body(), Vaga.class);
    }

    // Mantém os cookies de cada perfil separados durante os testes
    private HttpClient cliente() {
        var client = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();
        abertos.add(client);
        return client;
    }

    // Envia a escrita com CSRF real da sessão correspondente
    private HttpResponse<String> enviar(HttpClient client, String method, String path, Object body, boolean form) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).timeout(Duration.ofSeconds(15));
        if (!method.equals("GET")) {
            var csrf = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/auth/csrf")).GET().build(), HttpResponse.BodyHandlers.ofString());
            var token = mapper.readTree(csrf.body());
            request.header(token.get("headerName").asString(), token.get("token").asString());
            request.header("Content-Type", form ? "application/x-www-form-urlencoded" : "application/json");
        }
        String payload = body == null ? "" : form ? (String) body : mapper.writeValueAsString(body);
        request.method(method, method.equals("GET") ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(payload));
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    // Usa o login-path somente na preparação e limpeza dos próprios fixtures
    private void admin(String sql) throws Exception {
        var process = new ProcessBuilder(System.getenv().getOrDefault("POINT_MEDIA_MYSQL_EXE", "mysql"),
                "--login-path=point-media-admin", "--host=127.0.0.1", "--port=" + System.getenv().getOrDefault("POINT_MEDIA_DB_PORT", "3306"),
                "--database=point_media_dev", "--ssl-mode=REQUIRED", "--connect-timeout=5", "--execute", sql).redirectErrorStream(true).start();
        if (!process.waitFor(10, TimeUnit.SECONDS)) { process.destroyForcibly(); throw new IllegalStateException("Tempo excedido na limpeza do teste."); }
        if (process.exitValue() != 0) throw new IllegalStateException("Falha na preparação ou limpeza do teste.");
    }
}
