package br.com.pointmedia.database;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("mysql")
public class MySqlConnectionCheck implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    // Recebe a conexão usada na verificação do banco
    public MySqlConnectionCheck(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Confere o banco de destino e a tabela esperada
    public void verify() {
        String database = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        // Interrompe o uso de um banco diferente do desenvolvimento
        if (!"point_media_dev".equals(database)) {
            throw new IllegalStateException("Banco de desenvolvimento inesperado.");
        }

        // Confere a tabela sem carregar registros de usuários
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios WHERE 1 = 0", Integer.class);
    }

    @Override
    // Verifica o MySQL antes de concluir a inicialização
    public void run(ApplicationArguments args) {
        verify();
    }
}
