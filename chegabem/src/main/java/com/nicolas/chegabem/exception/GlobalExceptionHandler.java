package com.nicolas.chegabem.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException e,
                                                      HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage(), req);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponse> regraDeNegocio(RegraDeNegocioException e,
                                                       HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, e.getMessage(), req);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResponse> conflito(ConflitoException e, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, e.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException e,
                                                  HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(ErroResponse.deValidacao(req.getRequestURI(), campos));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> jsonInvalido(HttpMessageNotReadableException e,
                                                     HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Corpo da requisicao invalido ou mal formatado.", req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tipoInvalido(MethodArgumentTypeMismatchException e,
                                                     HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST,
                "O parametro '" + e.getName() + "' recebeu um valor invalido.", req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponse> naoAutenticado(AuthenticationException e,
                                                       HttpServletRequest req) {
        return resposta(HttpStatus.UNAUTHORIZED, "Credenciais invalidas.", req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponse> acessoNegado(AccessDeniedException e,
                                                     HttpServletRequest req) {
        return resposta(HttpStatus.FORBIDDEN,
                "Seu perfil nao tem permissao para esta operacao.", req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException e,
                                                    HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT,
                "A operacao viola uma restricao de integridade do banco de dados.", req);
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErroResponse> rotaInexistente(Exception e, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, "Endpoint nao encontrado.", req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodoNaoSuportado(HttpRequestMethodNotSupportedException e,
                                                           HttpServletRequest req) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED,
                "O metodo " + e.getMethod() + " nao e suportado neste endpoint.", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception e, HttpServletRequest req) {
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno no servidor. Tente novamente mais tarde.", req);
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem,
                                                  HttpServletRequest req) {
        return ResponseEntity.status(status).body(
                ErroResponse.de(status.value(), status.getReasonPhrase(),
                        mensagem, req.getRequestURI()));
    }
}
