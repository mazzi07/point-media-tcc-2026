package br.com.pointmedia.vagas;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Repository
@Profile("mysql")
public class VagaRepository {

    private final JdbcTemplate jdbc;

    // Usa a mesma conexão gerenciada nas transações do serviço
    public VagaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Lista somente vagas publicadas em páginas de vinte itens
    public List<Vaga.ResumoPublico> publicadas(int pagina) {
        return jdbc.query("""
                SELECT id, titulo, area, modalidade, cidade, estado, publicada_em FROM vagas
                WHERE status = 'PUBLICADA' ORDER BY publicada_em DESC, id DESC LIMIT 20 OFFSET ?
                """, (rs, row) -> new Vaga.ResumoPublico(rs.getLong("id"), rs.getString("titulo"),
                rs.getString("area"), rs.getString("modalidade"), rs.getString("cidade"),
                rs.getString("estado"), rs.getObject("publicada_em", java.time.LocalDateTime.class)), pagina * 20);
    }

    // Lista todos os estados apenas no acesso administrativo
    public List<Vaga.ResumoAdmin> administrativas(int pagina) {
        return jdbc.query("""
                SELECT id, titulo, status, versao FROM vagas ORDER BY id DESC LIMIT 20 OFFSET ?
                """, (rs, row) -> new Vaga.ResumoAdmin(rs.getLong("id"), rs.getString("titulo"),
                rs.getString("status"), rs.getInt("versao")), pagina * 20);
    }

    // Restringe a busca pública no próprio SQL antes de carregar os requisitos
    public Optional<Vaga> publicada(long id) {
        return buscar(id, " AND status = 'PUBLICADA'");
    }

    // Lê os detalhes administrativos sem bloquear uma edição
    public Optional<Vaga> buscar(long id) {
        return buscar(id, "");
    }

    // Serializa as escritas da mesma vaga para preservar requisitos e histórico
    public Optional<Vaga> bloquear(long id) {
        return buscar(id, " FOR UPDATE");
    }

    // Os complementos SQL são fixos e nunca vêm de parâmetros HTTP
    private Optional<Vaga> buscar(long id, String complemento) {
        RowMapper<Vaga> mapper = (rs, row) -> new Vaga(rs.getLong("id"), rs.getString("titulo"),
                rs.getString("area"), rs.getString("descricao"), rs.getString("modalidade"),
                rs.getString("cidade"), rs.getString("estado"), rs.getString("status"),
                rs.getLong("criada_por"), rs.getInt("versao"),
                rs.getObject("criada_em", java.time.LocalDateTime.class),
                rs.getObject("atualizada_em", java.time.LocalDateTime.class),
                rs.getObject("publicada_em", java.time.LocalDateTime.class), List.of());
        var vaga = jdbc.query("SELECT * FROM vagas WHERE id = ?" + complemento, mapper, id).stream().findFirst();
        // Carrega os requisitos apenas quando a vaga pôde ser consultada
        return vaga.map(v -> new Vaga(v.id(), v.titulo(), v.area(), v.descricao(), v.modalidade(),
                v.cidade(), v.estado(), v.status(), v.criadaPor(), v.versao(), v.criadaEm(),
                v.atualizadaEm(), v.publicadaEm(), jdbc.query("""
                        SELECT descricao, tipo FROM vaga_requisitos WHERE vaga_id = ? ORDER BY ordem, id
                        """, (rs, row) -> new Vaga.Requisito(rs.getString("descricao"), rs.getString("tipo")), id)));
    }

    // Cria a vaga com os defaults de rascunho e versão do schema
    public long criar(Vaga.Dados dados, long autor) {
        var key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO vagas (titulo, area, descricao, modalidade, cidade, estado, criada_por)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, dados.titulo());
            statement.setString(2, dados.area());
            statement.setString(3, dados.descricao());
            statement.setString(4, dados.modalidade());
            statement.setString(5, dados.cidade());
            statement.setString(6, dados.estado());
            statement.setLong(7, autor);
            return statement;
        }, key);
        return key.getKey().longValue();
    }

    // Atualiza somente se a versão recebida ainda for a atual
    public int editar(long id, int versao, Vaga.Dados dados) {
        return jdbc.update("""
                UPDATE vagas SET titulo = ?, area = ?, descricao = ?, modalidade = ?, cidade = ?,
                estado = ?, versao = versao + 1 WHERE id = ? AND versao = ?
                """, dados.titulo(), dados.area(), dados.descricao(), dados.modalidade(),
                dados.cidade(), dados.estado(), id, versao);
    }

    // Muda o estado sem perder a data da primeira publicação
    public int alterarEstado(long id, int versao, String status) {
        return jdbc.update("""
                UPDATE vagas SET status = ?, versao = versao + 1,
                publicada_em = CASE WHEN ? = 'PUBLICADA' THEN CURRENT_TIMESTAMP(3) ELSE publicada_em END
                WHERE id = ? AND versao = ?
                """, status, status, id, versao);
    }

    // Substitui somente os requisitos da vaga editada dentro da transação
    public void requisitos(long id, List<Vaga.Requisito> requisitos) {
        // Não desfaz vínculos com habilidades que este formulário ainda não gerencia
        Integer vinculados = jdbc.queryForObject("SELECT COUNT(*) FROM vaga_requisitos WHERE vaga_id = ? AND habilidade_id IS NOT NULL", Integer.class, id);
        if (vinculados != null && vinculados > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Os requisitos desta vaga estão vinculados a habilidades e não podem ser substituídos neste formulário.");
        }
        jdbc.update("DELETE FROM vaga_requisitos WHERE vaga_id = ?", id);
        // A posição na lista define a ordem aprovada pelo formulário
        for (int ordem = 0; ordem < requisitos.size(); ordem++) {
            var requisito = requisitos.get(ordem);
            jdbc.update("INSERT INTO vaga_requisitos (vaga_id, descricao, tipo, ordem) VALUES (?, ?, ?, ?)",
                    id, requisito.descricao(), requisito.tipo(), ordem);
        }
    }

    // Inclui os vínculos e a ordem originais no snapshot histórico
    public List<Map<String, Object>> requisitosHistoricos(long id) {
        return jdbc.queryForList("SELECT id, habilidade_id, descricao, tipo, ordem FROM vaga_requisitos WHERE vaga_id = ? ORDER BY ordem, id", id);
    }

    // Guarda o conteúdo anterior sem permitir atualizar ou apagar versões históricas
    public void historico(Vaga anterior, long autor, String dados) {
        Integer numero = jdbc.queryForObject(
                "SELECT COALESCE(MAX(numero), 0) + 1 FROM vaga_versoes WHERE vaga_id = ?", Integer.class, anterior.id());
        jdbc.update("""
                INSERT INTO vaga_versoes (vaga_id, numero, versao_origem, dados, criada_por) VALUES (?, ?, ?, ?, ?)
                """, anterior.id(), numero, anterior.versao(), dados, autor);
    }
}
