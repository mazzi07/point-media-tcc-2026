package br.com.pointmedia.vagas;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record Vaga(long id, String titulo, String area, String descricao, String modalidade,
        String cidade, String estado, String status, long criadaPor, int versao,
        LocalDateTime criadaEm, LocalDateTime atualizadaEm, LocalDateTime publicadaEm,
        List<Requisito> requisitos) {

    // Valida os campos que podem ser escritos pelo RH
    public record Dados(
            @NotBlank @Size(max = 150) String titulo,
            @Size(max = 100) String area,
            @Size(max = 10000) String descricao,
            @Pattern(regexp = "PRESENCIAL|HIBRIDO|REMOTO") String modalidade,
            @Size(max = 100) String cidade,
            @Pattern(regexp = "[A-Z]{2}") String estado,
            @NotNull @Size(max = 100) List<@NotNull @Valid Requisito> requisitos) {
    }

    // Aceita requisitos textuais sem criar habilidades ou regras de matching
    public record Requisito(@NotBlank @Size(max = 255) String descricao,
            @NotNull @Pattern(regexp = "OBRIGATORIO|DESEJAVEL") String tipo) {
    }

    // Exige a versão que o formulário recebeu antes da edição
    public record Edicao(@Min(1) int versao, @NotNull @Valid Dados dados) {
    }

    // Exige a versão atual também nas mudanças de estado
    public record AlteracaoEstado(@Min(1) int versao) {
    }

    // Limita a listagem pública aos campos próprios da divulgação
    public record ResumoPublico(long id, String titulo, String area, String modalidade,
            String cidade, String estado, LocalDateTime publicadaEm) {
    }

    // Mantém autoria, estado e controle de edição fora da resposta pública
    public record Publica(long id, String titulo, String area, String descricao, String modalidade,
            String cidade, String estado, LocalDateTime publicadaEm, List<Requisito> requisitos) {
    }

    // Apresenta os campos necessários para escolher uma vaga administrativa
    public record ResumoAdmin(long id, String titulo, String status, int versao) {
    }
}
