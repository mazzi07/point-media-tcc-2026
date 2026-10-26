package br.com.pointmedia.usuarios;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Profile("mysql")
public class UsuarioRepository {

    private final JdbcTemplate jdbcTemplate;

    // Recebe a conexão JDBC da aplicação
    public UsuarioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Busca o usuário pelo e-mail com parâmetro SQL
    public Optional<Usuario> findByEmail(String email) {
        return jdbcTemplate.query("""
                SELECT id, nome, email, senha_hash, perfil, ativo
                FROM usuarios WHERE email = ?
                """, (resultSet, rowNumber) -> new Usuario(
                resultSet.getLong("id"), resultSet.getString("nome"), resultSet.getString("email"),
                resultSet.getString("senha_hash"), resultSet.getString("perfil"),
                resultSet.getBoolean("ativo")), email).stream().findFirst();
    }

    // Salva a conta sem permitir a escolha de perfil
    public Usuario insert(String nome, String email, String senhaHash) {
        // Os defaults aprovados fixam USUARIO/ativo; a conta SQL não pode inserir esses campos
        jdbcTemplate.update("INSERT INTO usuarios (nome, email, senha_hash) VALUES (?, ?, ?)",
                nome, email, senhaHash);
        return findByEmail(email).orElseThrow();
    }
}
