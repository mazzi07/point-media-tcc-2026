package br.com.pointmedia.vagas;

import java.util.List;

import br.com.pointmedia.auth.SessionAuthorizationManager;
import br.com.pointmedia.usuarios.Usuario;
import jakarta.validation.Valid;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class VagaController {

    private final ObjectProvider<VagaService> services;

    // Mantém o backend sem banco disponível para os testes padrão
    public VagaController(ObjectProvider<VagaService> services) {
        this.services = services;
    }

    // Exige o serviço transacional quando uma rota de vagas é chamada
    private VagaService service() {
        return services.getIfAvailable(() -> { throw new DataAccessResourceFailureException("MySQL indisponível."); });
    }

    // Lista somente as vagas publicadas para visitantes e usuários
    @GetMapping("/public/vagas")
    public List<Vaga.ResumoPublico> publicadas(@RequestParam(defaultValue = "0") int pagina) {
        return service().publicadas(pagina);
    }

    // Consulta a divulgação de uma vaga sem incluir os campos administrativos
    @GetMapping("/public/vagas/{id}")
    public Vaga.Publica publica(@PathVariable long id) {
        return service().publica(id);
    }

    // Usa a política existente de acesso para RH e GESTOR
    @GetMapping("/rh/vagas")
    public List<Vaga.ResumoAdmin> administrativas(@RequestParam(defaultValue = "0") int pagina) {
        return service().administrativas(pagina);
    }

    // Abre os dados completos da vaga para edição autorizada
    @GetMapping("/rh/vagas/{id}")
    public Vaga administrativa(@PathVariable long id) {
        return service().administrativa(id);
    }

    // Cria o rascunho com a autoria conferida pelo servidor
    @PostMapping("/rh/vagas")
    @ResponseStatus(HttpStatus.CREATED)
    public Vaga criar(@Valid @RequestBody Vaga.Dados dados,
            @RequestAttribute(SessionAuthorizationManager.CURRENT_USER_ATTRIBUTE) Usuario autor) {
        return service().criar(dados, autor.id());
    }

    // Salva os dados e requisitos usando a versão recebida do formulário
    @PutMapping("/rh/vagas/{id}")
    public Vaga editar(@PathVariable long id, @Valid @RequestBody Vaga.Edicao pedido,
            @RequestAttribute(SessionAuthorizationManager.CURRENT_USER_ATTRIBUTE) Usuario autor) {
        return service().editar(id, pedido, autor.id());
    }

    // Publica somente após conferir os campos obrigatórios
    @PostMapping("/rh/vagas/{id}/publicar")
    public Vaga publicar(@PathVariable long id, @Valid @RequestBody Vaga.AlteracaoEstado pedido,
            @RequestAttribute(SessionAuthorizationManager.CURRENT_USER_ATTRIBUTE) Usuario autor) {
        return service().alterarEstado(id, pedido.versao(), "PUBLICADA", autor.id());
    }

    // Encerra a divulgação sem excluir a vaga ou suas referências
    @PostMapping("/rh/vagas/{id}/encerrar")
    public Vaga encerrar(@PathVariable long id, @Valid @RequestBody Vaga.AlteracaoEstado pedido,
            @RequestAttribute(SessionAuthorizationManager.CURRENT_USER_ATTRIBUTE) Usuario autor) {
        return service().alterarEstado(id, pedido.versao(), "ENCERRADA", autor.id());
    }

    // Arquiva a vaga sem oferecer exclusão definitiva
    @PostMapping("/rh/vagas/{id}/arquivar")
    public Vaga arquivar(@PathVariable long id, @Valid @RequestBody Vaga.AlteracaoEstado pedido,
            @RequestAttribute(SessionAuthorizationManager.CURRENT_USER_ATTRIBUTE) Usuario autor) {
        return service().alterarEstado(id, pedido.versao(), "ARQUIVADA", autor.id());
    }
}
