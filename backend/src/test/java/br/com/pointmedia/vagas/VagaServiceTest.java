package br.com.pointmedia.vagas;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VagaServiceTest {

    @Mock private VagaRepository repository;
    @Mock private ObjectMapper mapper;
    private VagaService service;

    // Isola as regras sem substituir os testes transacionais reais
    @BeforeEach
    void preparar() {
        service = new VagaService(repository, mapper);
    }

    // Impede usar uma versão antiga antes de modificar dados ou requisitos
    @Test
    void recusaVersaoAntiga() {
        when(repository.bloquear(1)).thenReturn(Optional.of(vaga("PUBLICADA", 3)));
        assertThatThrownBy(() -> service.editar(1, new Vaga.Edicao(2, dados()), 9))
                .isInstanceOfSatisfying(ResponseStatusException.class, error -> assertThat(error.getStatusCode().value()).isEqualTo(409));
        verify(repository, never()).requisitos(anyLong(), any());
        verify(repository, never()).historico(any(), anyLong(), any());
    }

    // Recusa publicação incompleta mesmo quando o pedido tem a versão correta
    @Test
    void recusaPublicacaoIncompleta() {
        var incompleta = new Vaga(1, "Rascunho", null, null, null, null, null, "RASCUNHO", 9, 1, null, null, null, List.of());
        when(repository.bloquear(1)).thenReturn(Optional.of(incompleta));
        assertThatThrownBy(() -> service.alterarEstado(1, 1, "PUBLICADA", 9))
                .isInstanceOfSatisfying(ResponseStatusException.class, error -> assertThat(error.getStatusCode().value()).isEqualTo(400));
    }

    // Não permite reabrir a edição de uma vaga encerrada
    @Test
    void recusaEdicaoEncerrada() {
        when(repository.bloquear(1)).thenReturn(Optional.of(vaga("ENCERRADA", 2)));
        assertThatThrownBy(() -> service.editar(1, new Vaga.Edicao(2, dados()), 9)).isInstanceOf(ResponseStatusException.class);
    }

    // Um envio sem mudanças não consome versão nem cria histórico duplicado
    @Test
    void preservaVersaoSemMudancas() {
        var anterior = vaga("PUBLICADA", 2);
        when(repository.bloquear(1)).thenReturn(Optional.of(anterior));
        assertThat(service.editar(1, new Vaga.Edicao(2, dados()), 9)).isEqualTo(anterior);
        verify(repository, never()).historico(any(), anyLong(), any());
    }

    // A resposta pública não contém os campos administrativos do registro
    @Test
    void projecaoPublica() {
        when(repository.publicada(1)).thenReturn(Optional.of(vaga("PUBLICADA", 5)));
        assertThat(service.publica(1)).isEqualTo(new Vaga.Publica(1, "Título", "Tecnologia", "Descrição", "REMOTO", null, null,
                LocalDateTime.of(2026, 1, 1, 10, 0), List.of()));
    }

    // A busca pública não distingue rascunho de um identificador inexistente
    @Test
    void recusaVagaNaoDivulgada() {
        when(repository.publicada(1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.publica(1)).isInstanceOfSatisfying(ResponseStatusException.class,
                error -> assertThat(error.getStatusCode().value()).isEqualTo(404));
    }

    // Impede uma página inválida antes de fazer consultas ao banco
    @Test
    void validaPaginacao() {
        assertThatThrownBy(() -> service.publicadas(-1)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> service.administrativas(Integer.MAX_VALUE)).isInstanceOf(ResponseStatusException.class);
    }

    // Prepara os mesmos dados editáveis do registro fictício
    private Vaga.Dados dados() {
        return new Vaga.Dados("Título", "Tecnologia", "Descrição", "REMOTO", null, null, List.of());
    }

    // Cria o estado usado em cada cenário sem consultar dados existentes
    private Vaga vaga(String status, int versao) {
        return new Vaga(1, "Título", "Tecnologia", "Descrição", "REMOTO", null, null, status, 9, versao,
                null, null, LocalDateTime.of(2026, 1, 1, 10, 0), List.of());
    }
}
