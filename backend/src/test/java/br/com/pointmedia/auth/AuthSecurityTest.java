package br.com.pointmedia.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import br.com.pointmedia.usuarios.Usuario;
import br.com.pointmedia.usuarios.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Import(AuthSecurityTest.Fixtures.class)
class AuthSecurityTest {

    private static final String PASSWORD = "Senha-ficticia-2026!";

    @Autowired private WebApplicationContext context;
    @Autowired private UsuarioRepository repository;
    @Autowired private PasswordEncoder encoder;
    @Autowired private ObjectMapper mapper;

    private MockMvc mvc;
    private Map<String, Usuario> users;

    @BeforeEach
    // Prepara contas fictícias e o repositório isolado dos testes
    void prepare() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        reset(repository);
        users = new HashMap<>();
        String hash = encoder.encode(PASSWORD);
        users.put("usuario@example.invalid", new Usuario(1, "Pessoa de teste",
                "usuario@example.invalid", hash, "USUARIO", true));
        users.put("inativo@example.invalid", new Usuario(2, "Pessoa inativa",
                "inativo@example.invalid", hash, "USUARIO", false));
        users.put("rh@example.invalid", new Usuario(3, "Pessoa RH", "rh@example.invalid", hash, "RH", true));
        users.put("gestor@example.invalid", new Usuario(4, "Pessoa gestora", "gestor@example.invalid", hash, "GESTOR", true));
        // Simula a busca sem acessar usuários do banco
        when(repository.findByEmail(anyString())).thenAnswer(
                invocation -> Optional.ofNullable(users.get(invocation.getArgument(0))));
        // Simula o cadastro com unicidade de e-mail
        when(repository.insert(anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            String email = invocation.getArgument(1);
            // Reproduz a restrição de e-mail único do banco
            if (users.containsKey(email)) throw new DuplicateKeyException("Duplicado de teste");
            var usuario = new Usuario(users.size() + 1, invocation.getArgument(0), email,
                    invocation.getArgument(2), "USUARIO", true);
            users.put(email, usuario);
            return usuario;
        });
    }

    @Test
    // Confere a normalização e a proteção da senha no cadastro
    void registersNormalizedUserWithBcryptWithoutReturningPassword() throws Exception {
        Token csrf = token(null);
        mvc.perform(secured(post("/api/auth/register"), csrf).contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("nome", " Pessoa nova ",
                                "email", "NOVO@example.invalid", "senha", PASSWORD))))
                .andExpect(status().isCreated()).andExpect(jsonPath("perfil").value("USUARIO"))
                .andExpect(jsonPath("nome").value("Pessoa nova"))
                .andExpect(jsonPath("email").value("novo@example.invalid"))
                .andExpect(jsonPath("senha_hash").doesNotExist())
                .andExpect(jsonPath("senhaHash").doesNotExist())
                .andExpect(jsonPath("senha").doesNotExist());
        assertThat(users.get("novo@example.invalid").senhaHash()).startsWith("$2").isNotEqualTo(PASSWORD);
        assertThat(encoder.matches(PASSWORD, users.get("novo@example.invalid").senhaHash())).isTrue();
    }

    @Test
    // Confere a resposta para e-mail já cadastrado
    void rejectsDuplicateEmail() throws Exception {
        Token csrf = token(null);
        mvc.perform(secured(post("/api/auth/register"), csrf).contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("nome", "Pessoa",
                                "email", "usuario@example.invalid", "senha", PASSWORD))))
                .andExpect(status().isConflict()).andExpect(jsonPath("code").value("EMAIL_DUPLICADO"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"RH", "GESTOR"})
    // Impede escolher um perfil privilegiado no cadastro
    void rejectsRoleSelection(String perfil) throws Exception {
        Token csrf = token(null);
        mvc.perform(secured(post("/api/auth/register"), csrf).contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("nome", "Pessoa",
                                "email", "novo@example.invalid", "senha", PASSWORD, "perfil", perfil))))
                .andExpect(status().isBadRequest());
        assertThat(users).doesNotContainKey("novo@example.invalid");
    }

    @Test
    // Recusa dados de cadastro inválidos
    void validatesRegistrationFields() throws Exception {
        Token csrf = token(null);
        mvc.perform(secured(post("/api/auth/register"), csrf).contentType("application/json")
                        .content("{\"nome\":\" \",\"email\":\"invalido\",\"senha\":\"curta\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("errors.nome").exists())
                .andExpect(jsonPath("errors.email").exists()).andExpect(jsonPath("errors.senha").exists());
    }

    @Test
    // Recusa senhas que ultrapassam o limite em bytes
    void rejectsPasswordExceedingBcryptByteLimit() throws Exception {
        Token csrf = token(null);
        mvc.perform(secured(post("/api/auth/register"), csrf).contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("nome", "Pessoa",
                                "email", "novo@example.invalid", "senha", "á".repeat(40)))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("errors.senha").exists());
    }

    @ParameterizedTest
    @ValueSource(strings = {"errada", "senha-muito-longa-que-nao-deve-autenticar-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"})
    // Recusa senhas incorretas sem revelar a conta
    void rejectsIncorrectPassword(String password) throws Exception {
        Token csrf = token(null);
        mvc.perform(secured(post("/api/auth/login"), csrf)
                        .param("email", "usuario@example.invalid").param("senha", password))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value("E-mail ou senha inválidos."));
    }

    @Test
    // Recusa o login de uma conta inativa
    void rejectsInactiveUser() throws Exception {
        Token csrf = token(null);
        mvc.perform(secured(post("/api/auth/login"), csrf)
                        .param("email", "inativo@example.invalid").param("senha", PASSWORD))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value("E-mail ou senha inválidos."));
    }

    @Test
    // Confere a troca de sessão, a renovação de CSRF e o logout
    void logsInChangesSessionIdRenewsCsrfReturnsUserAndLogsOut() throws Exception {
        Token before = token(null);
        String oldSessionId = before.session().getId();
        mvc.perform(secured(post("/api/auth/login"), before)
                        .param("email", "USUARIO@example.invalid").param("senha", PASSWORD))
                .andExpect(status().isNoContent());
        assertThat(before.session().getId()).isNotEqualTo(oldSessionId);
        mvc.perform(get("/api/auth/me").session(before.session()))
                .andExpect(status().isOk()).andExpect(jsonPath("email").value("usuario@example.invalid"))
                .andExpect(jsonPath("senhaHash").doesNotExist());
        mvc.perform(secured(post("/api/auth/logout"), before))
                .andExpect(status().isForbidden()).andExpect(jsonPath("code").value("CSRF_INVALIDO"));
        Token after = token(before.session());
        mvc.perform(secured(post("/api/auth/logout"), after)).andExpect(status().isNoContent());
        assertThat(after.session().isInvalid()).isTrue();
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(secured(post("/api/auth/login"), token(null))
                        .param("email", "usuario@example.invalid").param("senha", PASSWORD))
                .andExpect(status().isNoContent());
    }

    @Test
    // Invalida a sessão quando a conta fica inativa
    void invalidatesSessionWhenUserBecomesInactive() throws Exception {
        Token csrf = token(null);
        mvc.perform(secured(post("/api/auth/login"), csrf)
                .param("email", "usuario@example.invalid").param("senha", PASSWORD))
                .andExpect(status().isNoContent());
        Usuario user = users.get("usuario@example.invalid");
        users.put(user.email(), new Usuario(user.id(), user.nome(), user.email(), user.senhaHash(), user.perfil(), false));
        mvc.perform(get("/api/auth/me").session(csrf.session())).andExpect(status().isUnauthorized());
        assertThat(csrf.session().isInvalid()).isTrue();
    }

    @Test
    // Confere as permissões de visitante, usuário, RH e gestor
    void distinguishesAuthenticationFromAuthorizationAndAllowsPublicReads() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/public/probe")).andExpect(status().isOk());
        var usuario = login("usuario@example.invalid");
        mvc.perform(get("/api/gestao/probe").session(usuario))
                .andExpect(status().isForbidden()).andExpect(jsonPath("code").value("ACESSO_NEGADO"));
        var rh = login("rh@example.invalid");
        mvc.perform(get("/api/rh/probe").session(rh)).andExpect(status().isOk());
        mvc.perform(get("/api/gestao/probe").session(rh)).andExpect(status().isForbidden());
        var gestor = login("gestor@example.invalid");
        mvc.perform(get("/api/gestao/probe").session(gestor)).andExpect(status().isOk());
        mvc.perform(get("/api/rh/probe").session(gestor)).andExpect(status().isOk());
    }

    @Test
    // Bloqueia a conta inativa sem depender da consulta do frontend
    void rejectsInactiveUserOnProtectedRouteWithoutCallingMe() throws Exception {
        var session = login("gestor@example.invalid");
        var original = users.get("gestor@example.invalid");
        // Altera somente a conta fictícia para conferir a revogação
        users.put(original.email(), new Usuario(original.id(), original.nome(), original.email(),
                original.senhaHash(), original.perfil(), false));

        mvc.perform(get("/api/gestao/probe").session(session)).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("code").value("NAO_AUTENTICADO"));
        assertThat(session.isInvalid()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"RH", "USUARIO"})
    // Invalida as permissões antigas após mudança de perfil
    void invalidatesOldAuthoritiesAfterRoleChangeWithoutCallingMe(String newRole) throws Exception {
        var session = login("gestor@example.invalid");
        var original = users.get("gestor@example.invalid");
        // Altera somente a conta fictícia para conferir a revogação
        users.put(original.email(), new Usuario(original.id(), original.nome(), original.email(),
                original.senhaHash(), newRole, true));

        mvc.perform(get("/api/gestao/probe").session(session)).andExpect(status().isUnauthorized());
        assertThat(session.isInvalid()).isTrue();
        var renewed = login(original.email());
        mvc.perform(get("/api/gestao/probe").session(renewed)).andExpect(status().isForbidden());
    }

    @Test
    // Bloqueia o acesso na falha de banco e permite tentar novamente ou sair
    void deniesProtectedAccessDuringDatabaseFailureAndAllowsRetryAndLogout() throws Exception {
        var session = login("gestor@example.invalid");
        var csrf = token(session);
        // Simula a busca sem acessar usuários do banco
        when(repository.findByEmail(anyString())).thenThrow(new CannotGetJdbcConnectionException("Detalhe interno"));

        mvc.perform(get("/api/gestao/probe").session(session)).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("code").value("SERVICO_INDISPONIVEL"))
                .andExpect(jsonPath("message").value("Não foi possível verificar a sessão. Tente novamente."));
        assertThat(session.isInvalid()).isFalse();
        // Restaura o banco simulado antes de tentar novamente
        doAnswer(invocation -> Optional.ofNullable(users.get(invocation.getArgument(0))))
                .when(repository).findByEmail(anyString());
        mvc.perform(get("/api/gestao/probe").session(session)).andExpect(status().isOk());

        // Simula a busca sem acessar usuários do banco
        when(repository.findByEmail(anyString())).thenThrow(new CannotGetJdbcConnectionException("Detalhe interno"));
        mvc.perform(secured(post("/api/auth/logout"), csrf)).andExpect(status().isNoContent());
        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    // Evita leitura duplicada e invalida a sessão de uma conta removida
    void meReusesAuthorizationLookupAndDeletedUserCannotKeepSession() throws Exception {
        var session = login("usuario@example.invalid");
        clearInvocations(repository);
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk());
        // Confere que a resposta não consultou o usuário duas vezes
        verify(repository, times(1)).findByEmail("usuario@example.invalid");

        // Simula a remoção da conta depois do login
        users.remove("usuario@example.invalid");
        mvc.perform(get("/api/private/probe").session(session)).andExpect(status().isUnauthorized());
        assertThat(session.isInvalid()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"register", "login", "logout"})
    // Exige CSRF também no cadastro, login e logout
    void requiresCsrfIncludingPublicAuthenticationActions(String action) throws Exception {
        mvc.perform(post("/api/auth/" + action)).andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("CSRF_INVALIDO"));
    }

    @Test
    // Recusa um token CSRF inválido
    void rejectsInvalidCsrf() throws Exception {
        Token csrf = token(null);
        mvc.perform(post("/api/auth/login").session(csrf.session()).header(csrf.header(), "invalido"))
                .andExpect(status().isForbidden());
    }

    @Test
    // Oculta detalhes do banco durante o login
    void hidesDatabaseErrors() throws Exception {
        // Simula a busca sem acessar usuários do banco
        when(repository.findByEmail(anyString())).thenThrow(new CannotGetJdbcConnectionException("Detalhe interno"));
        Token csrf = token(null);
        mvc.perform(secured(post("/api/auth/login"), csrf).param("email", "usuario@example.invalid")
                        .param("senha", PASSWORD))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("message").value("Não foi possível entrar. Tente novamente."));
    }

    // Abre uma sessão real de autenticação no teste
    private MockHttpSession login(String email) throws Exception {
        var csrf = token(null);
        mvc.perform(secured(post("/api/auth/login"), csrf).param("email", email).param("senha", PASSWORD))
                .andExpect(status().isNoContent());
        return csrf.session();
    }

    // Obtém o token CSRF e a sessão usados no teste
    private Token token(MockHttpSession session) throws Exception {
        var request = get("/api/auth/csrf");
        if (session != null) request.session(session);
        var result = mvc.perform(request).andExpect(status().isOk()).andReturn();
        var json = mapper.readTree(result.getResponse().getContentAsString());
        return new Token((MockHttpSession) result.getRequest().getSession(false),
                json.get("headerName").asString(), json.get("token").asString());
    }

    // Envia o token e simula um cookie de sessão válido
    private MockHttpServletRequestBuilder secured(MockHttpServletRequestBuilder request, Token csrf) {
        return request.session(csrf.session()).header(csrf.header(), csrf.value()).with(mockRequest -> {
            mockRequest.setRequestedSessionId(csrf.session().getId());
            mockRequest.setRequestedSessionIdValid(true);
            return mockRequest;
        });
    }

    private record Token(MockHttpSession session, String header, String value) {
    }

    @TestConfiguration(proxyBeanMethods = false)
    @Import(PolicyProbe.class)
    static class Fixtures {
        @Bean UsuarioRepository usuarioRepository() { return mock(UsuarioRepository.class); }
    }

    @RestController
    static class PolicyProbe {
        @GetMapping({"/api/public/probe", "/api/rh/probe", "/api/gestao/probe", "/api/private/probe"})
        // Responde somente nas rotas isoladas do teste
        String probe() { return "ok"; }
    }
}
