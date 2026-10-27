package br.com.pointmedia.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    // Recusa identificadores e parâmetros que não podem ser convertidos
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> invalidParameter() {
        return ResponseEntity.badRequest().body(error(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS",
                "Informe identificadores e parâmetros válidos."));
    }

    // Devolve os erros esperados das operações de vagas sem detalhes de SQL
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ErrorResponse> operation(ResponseStatusException exception) {
        var status = HttpStatus.valueOf(exception.getStatusCode().value());
        String code = status == HttpStatus.CONFLICT ? "CONFLITO_VERSAO" :
                status == HttpStatus.NOT_FOUND ? "NAO_ENCONTRADO" : "DADOS_INVALIDOS";
        return ResponseEntity.status(status).body(error(status, code, exception.getReason()));
    }

    public record ErrorResponse(int status, String code, String message, Map<String, String> errors) {
    }

    // Monta o formato comum das respostas de erro
    public static ErrorResponse error(HttpStatus status, String code, String message) {
        return new ErrorResponse(status.value(), code, message, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    // Devolve os campos que precisam de correção
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        // Apresenta mensagens por campo sem incluir o valor enviado
        exception.getBindingResult().getFieldErrors().forEach(error -> errors.putIfAbsent(
                error.getField().equals("senhaCompativel") ? "senha" : error.getField(),
                error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ErrorResponse(400, "DADOS_INVALIDOS",
                "Confira os campos informados.", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    // Recusa JSON inválido e campos não esperados
    ResponseEntity<ErrorResponse> invalidJson() {
        return ResponseEntity.badRequest().body(error(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS",
                "Envie somente os campos esperados, em um JSON válido."));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    // Informa a duplicidade permitida no desenvolvimento privado
    ResponseEntity<ErrorResponse> duplicateEmail() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409,
                "EMAIL_DUPLICADO", "Este e-mail já está cadastrado.",
                Map.of("email", "Este e-mail já está cadastrado.")));
    }

    @ExceptionHandler(DataAccessException.class)
    // Oculta os detalhes da falha de banco
    ResponseEntity<ErrorResponse> databaseUnavailable() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error(
                HttpStatus.SERVICE_UNAVAILABLE, "SERVICO_INDISPONIVEL",
                "Não foi possível concluir a solicitação. Tente novamente."));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    // Pede novo login quando a conta não é mais válida
    ResponseEntity<ErrorResponse> invalidSession() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error(
                HttpStatus.UNAUTHORIZED, "NAO_AUTENTICADO", "Entre novamente para continuar."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    // Informa que o recurso não foi encontrado
    ResponseEntity<ErrorResponse> notFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(
                HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", "Recurso não encontrado."));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    // Informa que o método HTTP não é aceito
    ResponseEntity<ErrorResponse> invalidMethod() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error(
                HttpStatus.METHOD_NOT_ALLOWED, "METODO_INVALIDO", "Método HTTP não permitido."));
    }

    @ExceptionHandler(Exception.class)
    // Oculta os detalhes de falhas inesperadas
    ResponseEntity<ErrorResponse> unexpectedFailure() {
        return ResponseEntity.internalServerError().body(error(
                HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO",
                "Não foi possível concluir a solicitação."));
    }
}
