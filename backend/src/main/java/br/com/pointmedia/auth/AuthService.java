package br.com.pointmedia.auth;

import java.util.Locale;

import br.com.pointmedia.usuarios.Usuario;
import br.com.pointmedia.usuarios.UsuarioRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {

    private final ObjectProvider<UsuarioRepository> repositories;
    private final PasswordEncoder passwordEncoder;

    // Recebe o acesso aos usuários e o codificador de senhas
    public AuthService(ObjectProvider<UsuarioRepository> repositories, PasswordEncoder passwordEncoder) {
        this.repositories = repositories;
        this.passwordEncoder = passwordEncoder;
    }

    // Exige o repositório disponível no ambiente MySQL
    private UsuarioRepository repository() {
        // Impede autenticar sem um repositório de usuários disponível
        return repositories.getIfAvailable(() -> {
            throw new DataAccessResourceFailureException("Autenticação indisponível sem MySQL.");
        });
    }

    // Normaliza os dados e salva a senha com BCrypt
    public Usuario register(String nome, String email, String senha) {
        return repository().insert(nome.strip(), email.strip().toLowerCase(Locale.ROOT),
                passwordEncoder.encode(senha));
    }

    // Busca o usuário ativo para conferir a sessão
    public Usuario currentUser(String email) {
        return repository().findByEmail(email).filter(Usuario::ativo)
                .orElseThrow(() -> new UsernameNotFoundException("Sessão inválida."));
    }

    @Override
    // Carrega a conta e seu perfil para o login
    public UserDetails loadUserByUsername(String email) {
        Usuario usuario = repository().findByEmail(email.strip().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas."));
        // Usa o perfil e a situação da conta conferidos no banco
        return User.withUsername(usuario.email()).password(usuario.senhaHash())
                .roles(usuario.perfil()).disabled(!usuario.ativo()).build();
    }
}
