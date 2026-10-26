package br.com.pointmedia.health;

import br.com.pointmedia.database.MySqlConnectionCheck;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class HealthControllerTest {

    @Test
    // Confere a resposta de falha sem expor detalhes do banco
    void reportsDatabaseFailureWithoutExposingInternalDetails() {
        var databaseCheck = mock(MySqlConnectionCheck.class);
        doThrow(new CannotGetJdbcConnectionException("Detalhe interno de teste"))
                .when(databaseCheck).verify();
        var beans = new StaticListableBeanFactory();
        beans.addBean("databaseCheck", databaseCheck);
        var controller = new HealthController(beans.getBeanProvider(MySqlConnectionCheck.class));

        var response = controller.health();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isEqualTo(new HealthController.HealthResponse("DOWN"));
    }
}
