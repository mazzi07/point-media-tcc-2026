package br.com.pointmedia.database;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("mysql")
@EnabledIfEnvironmentVariable(named = "POINT_MEDIA_MYSQL_TESTS", matches = "true")
class MySqlConnectionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${local.server.port}")
    private int port;

    @Test
    void readsApprovedSchemaThroughAnEncryptedConnection() throws Exception {
        assertThat(jdbcTemplate.queryForObject("SELECT DATABASE()", String.class))
                .isEqualTo("point_media_dev");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM usuarios", Long.class)).isNotNegative();
        assertThat(jdbcTemplate.queryForObject("SELECT CURRENT_USER()", String.class))
                .doesNotStartWith("root@");
        String cipher = jdbcTemplate.queryForObject("SHOW SESSION STATUS LIKE 'Ssl_cipher'",
                (resultSet, rowNumber) -> resultSet.getString("Value"));
        assertThat(cipher).isNotBlank();

        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + port + "/api/health"))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isEqualTo("{\"status\":\"UP\"}");
        }
    }
}
