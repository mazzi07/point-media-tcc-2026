package br.com.pointmedia.health;

import br.com.pointmedia.database.MySqlConnectionCheck;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final ObjectProvider<MySqlConnectionCheck> databaseCheck;

    // Recebe a verificação opcional do banco
    public HealthController(ObjectProvider<MySqlConnectionCheck> databaseCheck) {
        this.databaseCheck = databaseCheck;
    }

    @GetMapping("/api/health")
    // Informa se a aplicação consegue responder às consultas
    public ResponseEntity<HealthResponse> health() {
        try {
            // Inclui a verificação do banco apenas quando o profile está ativo
            databaseCheck.ifAvailable(MySqlConnectionCheck::verify);
            return ResponseEntity.ok(new HealthResponse("UP"));
        // Responde indisponível sem revelar o erro de conexão
        } catch (DataAccessException | IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new HealthResponse("DOWN"));
        }
    }

    public record HealthResponse(String status) {
    }
}
