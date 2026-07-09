package br.com.maranatamusic.presentation.exception;

import br.com.maranatamusic.domain.exception.AcessoNaoAutorizadoException;
import br.com.maranatamusic.domain.exception.CultoNaoEncontradoException;
import br.com.maranatamusic.domain.exception.EmailJaCadastradoException;
import br.com.maranatamusic.domain.exception.EscalaNaoEncontradaException;
import br.com.maranatamusic.domain.exception.EstadoEscalaInvalidoException;
import br.com.maranatamusic.domain.exception.InstrumentoEmUsoException;
import br.com.maranatamusic.domain.exception.InstrumentoJaCadastradoException;
import br.com.maranatamusic.domain.exception.InstrumentoJaEscaladoException;
import br.com.maranatamusic.domain.exception.InstrumentoNaoEncontradoException;
import br.com.maranatamusic.domain.exception.MusicoInstrumentoNaoEncontradoException;
import br.com.maranatamusic.domain.exception.MusicoJaEscaladoEmOutroCultoException;
import br.com.maranatamusic.domain.exception.MusicoInstrumentoAmbiguoException;
import br.com.maranatamusic.domain.exception.MusicoNaoTocaInstrumentoException;
import br.com.maranatamusic.domain.exception.PrimeiroAcessoInvalidoException;
import br.com.maranatamusic.domain.exception.PrimeiroAcessoNecessarioException;
import br.com.maranatamusic.domain.exception.EstadoSolicitacaoInvalidoException;
import br.com.maranatamusic.domain.exception.SenhasNaoConferemException;
import br.com.maranatamusic.domain.exception.SolicitacaoJaExisteException;
import br.com.maranatamusic.domain.exception.SolicitacaoSubstituicaoNaoEncontradaException;
import br.com.maranatamusic.domain.exception.SubstitutoNaoElegivelException;
import br.com.maranatamusic.domain.exception.SubstitutoObrigatorioException;
import br.com.maranatamusic.domain.exception.UltimoLiderException;
import br.com.maranatamusic.domain.exception.UsuarioComEscalaFuturaException;
import br.com.maranatamusic.domain.exception.UsuarioInativoException;
import br.com.maranatamusic.domain.exception.UsuarioNaoEncontradoException;
import br.com.maranatamusic.domain.exception.VinculoComEscalaFuturaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ErroResponse("Dados inválidos", campos));
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErroResponse> handleEmailJaCadastrado(EmailJaCadastradoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroResponse.simples("Email já cadastrado"));
    }

    // BadCredentials e UsernameNotFound retornam a MESMA mensagem — evita enumeração de emails
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErroResponse> handleCredenciaisInvalidas(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErroResponse.simples("Credenciais inválidas"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponse> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErroResponse.simples("Credenciais inválidas"));
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErroResponse.simples("Usuário não encontrado"));
    }

    // Sem isso, @PreAuthorize bloqueando um acesso cai no handler genérico de 500 do Spring Boot
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErroResponse.simples("Acesso negado"));
    }

    @ExceptionHandler(CultoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleCultoNaoEncontrado(CultoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErroResponse.simples("Culto não encontrado"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> handleParametroInvalido(MethodArgumentTypeMismatchException ex) {
        Map<String, String> campos = Map.of(ex.getName(), "formato esperado YYYY-MM");
        return ResponseEntity.badRequest().body(new ErroResponse("Parâmetro inválido", campos));
    }

    @ExceptionHandler(InstrumentoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleInstrumentoNaoEncontrado(InstrumentoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErroResponse.simples("Instrumento não encontrado"));
    }

    @ExceptionHandler(UsuarioInativoException.class)
    public ResponseEntity<ErroResponse> handleUsuarioInativo(UsuarioInativoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroResponse.simples("Usuário inativo"));
    }

    @ExceptionHandler(MusicoNaoTocaInstrumentoException.class)
    public ResponseEntity<ErroResponse> handleMusicoNaoTocaInstrumento(MusicoNaoTocaInstrumentoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(InstrumentoJaEscaladoException.class)
    public ResponseEntity<ErroResponse> handleInstrumentoJaEscalado(InstrumentoJaEscaladoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(MusicoJaEscaladoEmOutroCultoException.class)
    public ResponseEntity<ErroResponse> handleMusicoJaEscaladoEmOutroCulto(MusicoJaEscaladoEmOutroCultoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(InstrumentoJaCadastradoException.class)
    public ResponseEntity<ErroResponse> handleInstrumentoJaCadastrado(InstrumentoJaCadastradoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(InstrumentoEmUsoException.class)
    public ResponseEntity<ErroResponse> handleInstrumentoEmUso(InstrumentoEmUsoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(UltimoLiderException.class)
    public ResponseEntity<ErroResponse> handleUltimoLider(UltimoLiderException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(UsuarioComEscalaFuturaException.class)
    public ResponseEntity<ErroResponse> handleUsuarioComEscalaFutura(UsuarioComEscalaFuturaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(EscalaNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> handleEscalaNaoEncontrada(EscalaNaoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(AcessoNaoAutorizadoException.class)
    public ResponseEntity<ErroResponse> handleAcessoNaoAutorizado(AcessoNaoAutorizadoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(EstadoEscalaInvalidoException.class)
    public ResponseEntity<ErroResponse> handleEstadoEscalaInvalido(EstadoEscalaInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(VinculoComEscalaFuturaException.class)
    public ResponseEntity<ErroResponse> handleVinculoComEscalaFutura(VinculoComEscalaFuturaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(MusicoInstrumentoAmbiguoException.class)
    public ResponseEntity<ErroResponse> handleMusicoInstrumentoAmbiguo(MusicoInstrumentoAmbiguoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(MusicoInstrumentoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleMusicoInstrumentoNaoEncontrado(MusicoInstrumentoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(SenhasNaoConferemException.class)
    public ResponseEntity<ErroResponse> handleSenhasNaoConferem(SenhasNaoConferemException ex) {
        return ResponseEntity.badRequest().body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(PrimeiroAcessoInvalidoException.class)
    public ResponseEntity<ErroResponse> handlePrimeiroAcessoInvalido(PrimeiroAcessoInvalidoException ex) {
        return ResponseEntity.badRequest().body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(PrimeiroAcessoNecessarioException.class)
    public ResponseEntity<ErroResponse> handlePrimeiroAcessoNecessario(PrimeiroAcessoNecessarioException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(SolicitacaoSubstituicaoNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> handleSolicitacaoSubstituicaoNaoEncontrada(SolicitacaoSubstituicaoNaoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(SolicitacaoJaExisteException.class)
    public ResponseEntity<ErroResponse> handleSolicitacaoJaExiste(SolicitacaoJaExisteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(SubstitutoNaoElegivelException.class)
    public ResponseEntity<ErroResponse> handleSubstitutoNaoElegivel(SubstitutoNaoElegivelException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(EstadoSolicitacaoInvalidoException.class)
    public ResponseEntity<ErroResponse> handleEstadoSolicitacaoInvalido(EstadoSolicitacaoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(SubstitutoObrigatorioException.class)
    public ResponseEntity<ErroResponse> handleSubstitutoObrigatorio(SubstitutoObrigatorioException ex) {
        return ResponseEntity.badRequest().body(ErroResponse.simples(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleGenerico(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErroResponse.simples("Erro interno"));
    }
}
