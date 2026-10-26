package br.com.pointmedia.auth;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("mysql")
@EnabledIfEnvironmentVariable(named = "POINT_MEDIA_MYSQL_TESTS", matches = "true")
class MySqlAuthTest {

    private static final String NAME = "Teste automatizado de autenticação";
    private static final String PASSWORD = "Senha-ficticia-integracao-2026!";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private PasswordEncoder encoder;
    @Autowired private ObjectMapper mapper;
    @Value("${local.server.port}") private int port;

    @Test
    // Confere cadastro, sessão e logout com um usuário fictício no MySQL
    void persistsRegistrationAuthenticatesAndInvalidatesSessionWithRealMysql() throws Exception {
        // Executa a preparação e limpeza do próprio usuário fictício
        admin("SELECT 1");
        String email = "pm-auth-" + UUID.randomUUID() + "@example.invalid";
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM usuarios WHERE email = ?", Long.class, email)).isZero();

        var cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        try (var client = HttpClient.newBuilder().cookieHandler(cookies).build()) {
            try {
                // Abre a sessão e confere as proteções do cookie
                var csrfResponse = send(client, "GET", "/csrf", null, null, null);
                assertThat(csrfResponse.statusCode()).isEqualTo(200);
                String cookieHeader = csrfResponse.headers().firstValue("set-cookie").orElseThrow();
                assertThat(cookieHeader).contains("HttpOnly", "SameSite=Lax");
                String sessionBefore = sessionId(cookies);
                Token before = parseToken(csrfResponse.body());
                String registration = mapper.writeValueAsString(Map.of("nome", NAME, "email", email, "senha", PASSWORD));

                // Recusa cadastro sem CSRF antes de salvar a conta
                assertThat(send(client, "POST", "/register", registration, "application/json", null).statusCode())
                        .isEqualTo(403);
                var registered = send(client, "POST", "/register", registration, "application/json", before);
                assertThat(registered.statusCode()).isEqualTo(201);
                assertThat(registered.body()).doesNotContain("senha", PASSWORD);
                assertThat(mapper.readTree(registered.body()).get("perfil").asString()).isEqualTo("USUARIO");
                // Confere o hash persistido sem mostrá-lo na saída
                String hash = jdbc.queryForObject("SELECT senha_hash FROM usuarios WHERE email = ?", String.class, email);
                assertThat(hash).startsWith("$2").isNotEqualTo(PASSWORD);
                assertThat(encoder.matches(PASSWORD, hash)).isTrue();
                assertThat(send(client, "POST", "/register", registration, "application/json", before).statusCode())
                        .isEqualTo(409);
                // Recusa a escolha de um perfil privilegiado
                String tampered = mapper.writeValueAsString(Map.of("nome", NAME, "email", email,
                        "senha", PASSWORD, "perfil", "GESTOR"));
                assertThat(send(client, "POST", "/register", tampered, "application/json", before).statusCode())
                        .isEqualTo(400);

                // Confere senha incorreta antes do login válido
                String form = form(email, "incorreta");
                assertThat(send(client, "POST", "/login", form, "application/x-www-form-urlencoded", before).statusCode())
                        .isEqualTo(401);
                var login = send(client, "POST", "/login", form(email, PASSWORD),
                        "application/x-www-form-urlencoded", before);
                assertThat(login.statusCode()).isEqualTo(204);
                assertThat(sessionId(cookies)).isNotEqualTo(sessionBefore);
                var me = send(client, "GET", "/me", null, null, null);
                assertThat(me.statusCode()).isEqualTo(200);
                assertThat(mapper.readTree(me.body()).get("email").asString()).isEqualTo(email);
                assertThat(me.body()).doesNotContain("senha", hash);
                assertThat(send(client, "POST", "/logout", null, null, before).statusCode()).isEqualTo(403);

                // Renova o CSRF e confirma o logout
                Token after = parseToken(send(client, "GET", "/csrf", null, null, null).body());
                assertThat(send(client, "POST", "/logout", null, null, after).statusCode()).isEqualTo(204);
                assertThat(send(client, "GET", "/me", null, null, null).statusCode()).isEqualTo(401);
                Token anonymous = parseToken(send(client, "GET", "/csrf", null, null, null).body());
                assertThat(send(client, "POST", "/login", form(email, PASSWORD),
                        "application/x-www-form-urlencoded", anonymous).statusCode()).isEqualTo(204);

                // Apenas a linha fictícia recém-criada é alterada e removida pelo login-path administrativo
                // Executa a preparação e limpeza do próprio usuário fictício
                admin("UPDATE point_media_dev.usuarios SET ativo = FALSE WHERE email = '" + email + "'");
                assertThat(send(client, "GET", "/me", null, null, null).statusCode()).isEqualTo(401);
                Token inactive = parseToken(send(client, "GET", "/csrf", null, null, null).body());
                assertThat(send(client, "POST", "/login", form(email, PASSWORD),
                        "application/x-www-form-urlencoded", inactive).statusCode()).isEqualTo(401);
            // Remove exclusivamente o usuário criado por este teste
            } finally {
                // Executa a preparação e limpeza do próprio usuário fictício
                admin("DELETE FROM point_media_dev.usuarios WHERE email = '" + email + "'");
                assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM usuarios WHERE email = ?", Long.class, email)).isZero();
            }
        }
    }

    // Envia a requisição HTTP com os cookies do teste
    private HttpResponse<String> send(HttpClient client, String method, String path, String body,
            String contentType, Token token) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/auth" + path))
                .timeout(Duration.ofSeconds(10));
        // Inclui o formato do corpo quando a operação o exige
        if (contentType != null) request.header("Content-Type", contentType);
        // Inclui o token CSRF nas operações protegidas
        if (token != null) request.header(token.header(), token.value());
        request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    // Lê o token CSRF recebido da API
    private Token parseToken(String body) throws Exception {
        var json = mapper.readTree(body);
        return new Token(json.get("headerName").asString(), json.get("token").asString());
    }

    // Prepara os campos de login no formato de formulário
    private String form(String email, String password) {
        return "email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                + "&senha=" + URLEncoder.encode(password, StandardCharsets.UTF_8);
    }

    // Localiza o identificador da sessão nos cookies
    private String sessionId(CookieManager cookies) {
        return cookies.getCookieStore().getCookies().stream()
                .filter(cookie -> cookie.getName().equals("JSESSIONID")).findFirst().orElseThrow().getValue();
    }

    // Executa a preparação e limpeza do próprio usuário fictício
    private void admin(String sql) throws Exception {
        String executable = System.getenv().getOrDefault("POINT_MEDIA_MYSQL_EXE", "mysql");
        var process = new ProcessBuilder(executable, "--login-path=point-media-admin", "--host=127.0.0.1",
                "--port=" + System.getenv().getOrDefault("POINT_MEDIA_DB_PORT", "3306"), "--ssl-mode=REQUIRED",
                "--connect-timeout=5", "--default-character-set=utf8mb4", "--batch", "--skip-column-names",
                "--execute", sql).redirectErrorStream(true).start();
        // Interrompe o cliente se a preparação ultrapassar o tempo previsto
        if (!process.waitFor(10, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException("Tempo excedido na preparação/limpeza do teste.");
        }
        // Interrompe o teste quando a preparação ou limpeza falha
        if (process.exitValue() != 0) throw new IllegalStateException("Falha na preparação/limpeza do teste MySQL.");
    }

    private record Token(String header, String value) {
    }
}
