package br.com.pointmedia.auth;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

@Component
public class SessionAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    public static final String CURRENT_USER_ATTRIBUTE = "br.com.pointmedia.auth.currentUser";

    private final AuthService authService;

    // Recebe o serviço usado para conferir a sessão
    public SessionAuthorizationManager(AuthService authService) {
        this.authService = authService;
    }

    @Override
    // Confere o usuário no banco antes de autorizar a requisição
    public AuthorizationDecision authorize(Supplier<? extends Authentication> authentication,
            RequestAuthorizationContext context) {
        Authentication current = authentication.get();
        HttpServletRequest request = context.getRequest();
        try {
            // Confere a conta atual antes de usar as permissões da sessão
            var usuario = authService.currentUser(current.getName());
            var roles = current.getAuthorities().stream().map(authority -> authority.getAuthority())
                    .filter(authority -> authority.startsWith("ROLE_")).collect(Collectors.toSet());
            // Exige novo login quando o perfil armazenado ficou desatualizado
            if (!roles.equals(Set.of("ROLE_" + usuario.perfil()))) {
                throw new UsernameNotFoundException("Perfil da sessão desatualizado.");
            }
            // /me reutiliza a leitura desta requisição, nunca um cache da sessão
            request.setAttribute(CURRENT_USER_ATTRIBUTE, usuario);
            return new AuthorizationDecision(true);
        // Encerra a sessão que não representa mais uma conta válida
        } catch (UsernameNotFoundException exception) {
            var session = request.getSession(false);
            if (session != null) session.invalidate();
            SecurityContextHolder.clearContext();
            throw new CredentialsExpiredException("Sessão inválida.");
        // Bloqueia a autorização quando não é possível consultar o banco
        } catch (DataAccessException exception) {
            throw new InternalAuthenticationServiceException("Não foi possível verificar a sessão.", exception);
        }
    }
}
