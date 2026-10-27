package br.com.pointmedia.vagas;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
@Profile("mysql")
public class VagaService {

    private final VagaRepository repository;
    private final ObjectMapper mapper;

    // Recebe o repositório e o conversor usado nos snapshots
    public VagaService(VagaRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // Limita a paginação sem permitir deslocamentos inválidos
    private void pagina(int pagina) {
        if (pagina < 0 || pagina > 100000) throw erro(HttpStatus.BAD_REQUEST, "Página inválida.");
    }

    // Lista a divulgação pública sem carregar dados administrativos
    public List<Vaga.ResumoPublico> publicadas(int pagina) {
        pagina(pagina);
        return repository.publicadas(pagina);
    }

    // Devolve o mesmo 404 para vaga inexistente ou não publicada
    @Transactional(readOnly = true)
    public Vaga.Publica publica(long id) {
        var v = repository.publicada(id).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Vaga não encontrada."));
        return new Vaga.Publica(v.id(), v.titulo(), v.area(), v.descricao(), v.modalidade(),
                v.cidade(), v.estado(), v.publicadaEm(), v.requisitos());
    }

    // Lista as vagas de todos os estados para a equipe autorizada
    public List<Vaga.ResumoAdmin> administrativas(int pagina) {
        pagina(pagina);
        return repository.administrativas(pagina);
    }

    // Mantém os detalhes e requisitos na mesma leitura transacional
    @Transactional(readOnly = true)
    public Vaga administrativa(long id) {
        return repository.buscar(id).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Vaga não encontrada."));
    }

    // Cria o rascunho e seus requisitos como uma única operação
    @Transactional
    public Vaga criar(Vaga.Dados pedido, long autor) {
        var dados = normalizar(pedido);
        long id = repository.criar(dados, autor);
        repository.requisitos(id, dados.requisitos());
        return administrativa(id);
    }

    // Preserva o conteúdo publicado antes de substituir dados e requisitos
    @Transactional
    public Vaga editar(long id, Vaga.Edicao pedido, long autor) {
        var anterior = atual(id, pedido.versao());
        // Vagas encerradas e arquivadas ficam disponíveis somente para consulta
        if (!List.of("RASCUNHO", "PUBLICADA").contains(anterior.status())) {
            throw erro(HttpStatus.BAD_REQUEST, "Somente rascunhos e vagas publicadas podem ser editados.");
        }
        var dados = normalizar(pedido.dados());
        if (anterior.status().equals("PUBLICADA")) validarPublicacao(dados);
        // Evita criar histórico para um formulário que não alterou o conteúdo
        if (dados.equals(dados(anterior))) return anterior;
        preservar(anterior, autor);
        conferirAtualizacao(repository.editar(id, pedido.versao(), dados));
        // Mantém os requisitos e seus vínculos quando somente o texto da vaga mudou
        if (!anterior.requisitos().equals(dados.requisitos())) repository.requisitos(id, dados.requisitos());
        return administrativa(id);
    }

    // Confere a transição antes de alterar o estado e a versão
    @Transactional
    public Vaga alterarEstado(long id, int versao, String destino, long autor) {
        var anterior = atual(id, versao);
        boolean permitido = switch (destino) {
            case "PUBLICADA" -> anterior.status().equals("RASCUNHO");
            case "ENCERRADA" -> anterior.status().equals("PUBLICADA");
            case "ARQUIVADA" -> !anterior.status().equals("ARQUIVADA");
            default -> false;
        };
        if (!permitido) throw erro(HttpStatus.BAD_REQUEST, "A mudança de estado não é permitida.");
        // Exige os campos de divulgação antes da primeira publicação
        if (destino.equals("PUBLICADA")) validarPublicacao(dados(anterior));
        preservar(anterior, autor);
        conferirAtualizacao(repository.alterarEstado(id, versao, destino));
        return administrativa(id);
    }

    // Lê a versão atual com bloqueio antes de escrever filhos ou histórico
    private Vaga atual(long id, int versao) {
        var vaga = repository.bloquear(id).orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Vaga não encontrada."));
        if (vaga.versao() != versao) throw erro(HttpStatus.CONFLICT, "A vaga foi alterada. Recarregue antes de salvar.");
        return vaga;
    }

    // Recusa qualquer escrita que não encontrou a versão esperada
    private void conferirAtualizacao(int linhas) {
        if (linhas != 1) throw erro(HttpStatus.CONFLICT, "A vaga foi alterada. Recarregue antes de salvar.");
    }

    // Registra o estado anterior de toda vaga que já esteve publicada
    private void preservar(Vaga anterior, long autor) {
        if (anterior.publicadaEm() != null) {
            ObjectNode snapshot = mapper.valueToTree(anterior);
            snapshot.set("requisitos", mapper.valueToTree(repository.requisitosHistoricos(anterior.id())));
            repository.historico(anterior, autor, mapper.writeValueAsString(snapshot));
        }
    }

    // Define os campos obrigatórios para divulgar uma vaga
    private void validarPublicacao(Vaga.Dados dados) {
        if (dados.area() == null || dados.descricao() == null || dados.modalidade() == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Para publicar, informe área, descrição e modalidade.");
        }
        if (!dados.modalidade().equals("REMOTO") && (dados.cidade() == null || dados.estado() == null)) {
            throw erro(HttpStatus.BAD_REQUEST, "Vagas presenciais ou híbridas precisam de cidade e UF.");
        }
    }

    // Remove espaços externos sem mudar a ordem dos requisitos
    private Vaga.Dados normalizar(Vaga.Dados d) {
        return new Vaga.Dados(d.titulo().strip(), texto(d.area()), texto(d.descricao()), d.modalidade(),
                texto(d.cidade()), d.estado(), d.requisitos().stream()
                        .map(r -> new Vaga.Requisito(r.descricao().strip(), r.tipo())).toList());
    }

    // Converte campos opcionais vazios em ausência de valor
    private String texto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.strip();
    }

    // Compara o conteúdo editável sem incluir dados administrativos
    private Vaga.Dados dados(Vaga v) {
        return new Vaga.Dados(v.titulo(), v.area(), v.descricao(), v.modalidade(), v.cidade(), v.estado(), v.requisitos());
    }

    // Usa mensagens controladas para erros esperados do módulo
    private ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }
}
