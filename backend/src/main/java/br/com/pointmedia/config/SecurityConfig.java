package br.com.pointmedia.config;

import java.io.IOException;

import br.com.pointmedia.auth.SessionAuthorizationManager;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.csrf.CsrfException;
import tools.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    @Bean
    // Define o custo usado para proteger as senhas
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    // Configura as rotas, sessões e proteções da API
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper mapper,
            SessionAuthorizationManager sessionAuthorization) throws Exception {
        // Confere a conta antes de avaliar os perfis exigidos
        AuthorizationManager<RequestAuthorizationContext> validSession = AuthorizationManagers.allOf(
                AuthenticatedAuthorizationManager.authenticated(), sessionAuthorization);
        // Separa as leituras públicas das rotas protegidas
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/api/health", "/api/auth/csrf", "/api/public/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/api/gestao/**").access(AuthorizationManagers.allOf(
                                validSession, AuthorityAuthorizationManager.hasRole("GESTOR")))
                        .requestMatchers("/api/rh/**").access(AuthorizationManagers.allOf(
                                validSession, AuthorityAuthorizationManager.hasAnyRole("RH", "GESTOR")))
                        .anyRequest().access(validSession))
                // Mantém CSRF ativo nas operações de escrita
                .csrf(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                // Troca o identificador da sessão quando o login é concluído
                .sessionManagement(session -> session.sessionFixation(fixation -> fixation.changeSessionId()))
                // Usa o login nativo com respostas próprias para a API
                .formLogin(form -> form.loginPage("/api/auth/login")
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("email").passwordParameter("senha")
                        .successHandler((request, response, authentication) -> response.setStatus(204))
                        .failureHandler((request, response, exception) -> {
                            // Distingue indisponibilidade do serviço de credenciais inválidas
                            if (exception instanceof InternalAuthenticationServiceException) {
                                // Devolve um erro de segurança sem detalhes internos
                                writeError(response, mapper, HttpStatus.SERVICE_UNAVAILABLE,
                                        "SERVICO_INDISPONIVEL", "Não foi possível entrar. Tente novamente.");
                            } else {
                                // Devolve um erro de segurança sem detalhes internos
                                writeError(response, mapper, HttpStatus.UNAUTHORIZED,
                                        "CREDENCIAIS_INVALIDAS", "E-mail ou senha inválidos.");
                            }
                        }))
                // Invalida a sessão e remove o cookie ao sair
                .logout(logout -> logout.logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(204)))
                // Devolve erros de autenticação e autorização em JSON
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> {
                            // Distingue indisponibilidade do serviço de credenciais inválidas
                            if (exception instanceof InternalAuthenticationServiceException) {
                                // Devolve um erro de segurança sem detalhes internos
                                writeError(response, mapper, HttpStatus.SERVICE_UNAVAILABLE,
                                        "SERVICO_INDISPONIVEL", "Não foi possível verificar a sessão. Tente novamente.");
                            } else {
                                // Devolve um erro de segurança sem detalhes internos
                                writeError(response, mapper, HttpStatus.UNAUTHORIZED,
                                        "NAO_AUTENTICADO", "Entre para continuar.");
                            }
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            // Diferencia token CSRF inválido de falta de permissão
                            if (exception instanceof CsrfException) {
                                // Devolve um erro de segurança sem detalhes internos
                                writeError(response, mapper, HttpStatus.FORBIDDEN,
                                        "CSRF_INVALIDO", "Atualize a sessão e tente novamente.");
                            } else {
                                // Devolve um erro de segurança sem detalhes internos
                                writeError(response, mapper, HttpStatus.FORBIDDEN,
                                        "ACESSO_NEGADO", "Você não tem permissão para esta ação.");
                            }
                        }));
        return http.build();
    }

    // Devolve um erro de segurança sem detalhes internos
    private static void writeError(HttpServletResponse response, ObjectMapper mapper,
            HttpStatus status, String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getOutputStream(), ApiExceptionHandler.error(status, code, message));
    }
}
