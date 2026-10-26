package br.com.pointmedia.auth;

import java.nio.charset.StandardCharsets;

import br.com.pointmedia.usuarios.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // Recebe o serviço de autenticação
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/csrf")
    // Entrega o token CSRF da sessão
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    // Valida o cadastro e devolve somente os dados públicos da conta
    public CurrentUser register(@Valid @RequestBody RegisterRequest request) {
        return CurrentUser.from(authService.register(request.nome(), request.email(), request.senha()));
    }

    @GetMapping("/me")
    // Reutiliza o usuário conferido na autorização desta requisição
    public CurrentUser me(@RequestAttribute(SessionAuthorizationManager.CURRENT_USER_ATTRIBUTE) Usuario usuario) {
        return CurrentUser.from(usuario);
    }

    public record CurrentUser(long id, String nome, String email, String perfil) {
        // Monta a resposta sem incluir a senha
        static CurrentUser from(Usuario usuario) {
            return new CurrentUser(usuario.id(), usuario.nome(), usuario.email(), usuario.perfil());
        }
    }

    // Valida os únicos campos aceitos no cadastro público
    public record RegisterRequest(
            @NotBlank(message = "Informe seu nome.")
            @Size(max = 120, message = "Use até 120 caracteres.") String nome,
            @NotBlank(message = "Informe seu e-mail.")
            @Email(message = "Informe um e-mail válido.")
            @Size(max = 254, message = "Use até 254 caracteres.") String email,
            @NotBlank(message = "Informe uma senha.")
            @Size(min = 12, max = 72, message = "Use de 12 a 72 caracteres.") String senha) {

        @JsonIgnore
        @AssertTrue(message = "A senha deve ocupar no máximo 72 bytes em UTF-8.")
        // Confere o limite de bytes aceito pelo BCrypt
        public boolean isSenhaCompativel() {
            return senha == null || senha.getBytes(StandardCharsets.UTF_8).length <= 72;
        }
    }
}
