package br.com.pointmedia.health;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HealthEndpointTest {

    @Value("${local.server.port}")
    private int port;

    @Test
    void returnsApplicationHealthOverHttp() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:" + port + "/api/health"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("content-type")).hasValueSatisfying(
                    contentType -> assertThat(contentType).startsWith("application/json"));
            assertThat(response.body()).isEqualTo("{\"status\":\"UP\"}");
        }
    }
}
