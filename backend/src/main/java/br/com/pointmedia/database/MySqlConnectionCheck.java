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

    public MySqlConnectionCheck(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void verify() {
        String database = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (!"point_media_dev".equals(database)) {
            throw new IllegalStateException("Banco de desenvolvimento inesperado.");
        }

        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios WHERE 1 = 0", Integer.class);
    }

    @Override
    public void run(ApplicationArguments args) {
        verify();
    }
}
