package br.com.sfpacim.backend.exceptions;

import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.sfpacim.backend.dtos.erro.ErroPadraoDTO;
import br.com.sfpacim.backend.dtos.erro.ErroValidacaoDTO;
import br.com.sfpacim.backend.utils.MetodosUteis;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler Global de Exceções (@RestControllerAdvice).
 * 
 * <p>
 * Captura exceções específicas da aplicação e as transforma em
 * respostas HTTP (ResponseEntity) padronizadas, utilizando o MessageSource
 * para internacionalização dos títulos e mensagens.
 *
 * @author Matheus F. N. Pereira
 */
@Slf4j
@RestControllerAdvice
public class TratadorDeErrosGlobal {

    /**
     * Instância utilizada para buscar as traduções.
     */
    private final MessageSource messageSource;

    /**
     * Construtor para injeção do MessageSource.
     * 
     * @param messageSource Instância utilizada para buscar as traduções.
     */
    public TratadorDeErrosGlobal(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Manipula exceções de autenticação (lançadas pelo Spring Security).
     * Retorna HTTP 401 (Unauthorized) se o e-mail ou senha estiverem incorretos
     * durante a tentativa de login.
     *
     * @param excecao    A exceção {@link AuthenticationException} capturada.
     * @param requisicao A requisição HTTP (para obter a Rota/URI).
     * @return ResponseEntity (HTTP 401) com o {@link ErroPadraoDTO}.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroPadraoDTO> excecaoAutenticacao(AuthenticationException excecao,
            HttpServletRequest requisicao) {
        log.warn("Falha na autenticação: {}", excecao.getMessage());

        ErroPadraoDTO erroPadrao = new ErroPadraoDTO(
                HttpStatus.UNAUTHORIZED, // 401
                MetodosUteis.obterMensagem(messageSource, "erro.401.titulo"),
                MetodosUteis.obterMensagem(messageSource, "erro.401.mensagem"),
                requisicao.getRequestURI());

        return ResponseEntity.status(erroPadrao.status()).body(erroPadrao);
    }

    /**
     * Manipula exceções de recurso não encontrado (EntityNotFoundException).
     * Retorna HTTP 404 (Not Found).
     *
     * <p>
     * Utilizado quando uma busca por ID (ex: Pessoa, Conta) não retorna resultados
     * ou quando o registro pertence a outro usuário (Isolamento de Dados).
     *
     * @param excecao    A exceção
     *                   {@link EntityNotFoundException}
     *                   capturada.
     * @param requisicao A requisição HTTP (para obter a Rota/URI).
     * @return ResponseEntity (HTTP 404) com o {@link ErroPadraoDTO}.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErroPadraoDTO> excecaoEntidadeNaoEncontrada(
            EntityNotFoundException excecao,
            HttpServletRequest requisicao) {

        log.warn("Recurso não encontrado: {}", excecao.getMessage());

        ErroPadraoDTO erroPadrao = new ErroPadraoDTO(
                HttpStatus.NOT_FOUND, // 404
                MetodosUteis.obterMensagem(messageSource, "erro.404.titulo"),
                excecao.getMessage(),
                requisicao.getRequestURI());

        return ResponseEntity.status(erroPadrao.status()).body(erroPadrao);
    }

    /**
     * Manipula exceções de violação de integridade dos dados (E-mail duplicado).
     *
     * @param excecao    A exceção de violação de dados capturada.
     * @param requisicao A requisição HTTP (para obter a Rota/URI).
     * @return ResponseEntity (HTTP 409) com o {@link ErroPadraoDTO}.
     */
    @ExceptionHandler({ ViolacaoDadosException.class, DataIntegrityViolationException.class })
    public ResponseEntity<ErroPadraoDTO> excecaoViolacaoDados(Exception excecao, HttpServletRequest requisicao) {
        String mensagemErro;

        if (excecao instanceof ViolacaoDadosException) {
            mensagemErro = excecao.getMessage();
        } else {
            mensagemErro = MetodosUteis.obterMensagem(messageSource, "erro.409.mensagem.padrao");
        }

        ErroPadraoDTO erroPadrao = new ErroPadraoDTO(
                HttpStatus.CONFLICT, // 409
                MetodosUteis.obterMensagem(messageSource, "erro.409.titulo"),
                mensagemErro,
                requisicao.getRequestURI());

        return ResponseEntity.status(erroPadrao.status()).body(erroPadrao);
    }

    /**
     * Manipula exceções de validação (lançadas pelo @Valid no DTO).
     * Retorna HTTP 422 (Unprocessable Entity) e uma lista detalhada
     * dos campos que falharam na validação.
     *
     * @param excecao    A exceção {@link MethodArgumentNotValidException}
     *                   capturada.
     * @param requisicao A requisição HTTP (para obter a Rota/URI).
     * @return ResponseEntity contendo o DTO {@link ErroValidacaoDTO}.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroValidacaoDTO> excecaoValidacao(MethodArgumentNotValidException excecao,
            HttpServletRequest requisicao) {

        ErroValidacaoDTO erroValidacao = new ErroValidacaoDTO(
                HttpStatus.UNPROCESSABLE_CONTENT, // 422
                MetodosUteis.obterMensagem(messageSource, "erro.422.validacao.titulo"),
                MetodosUteis.obterMensagem(messageSource, "erro.422.validacao.mensagem"),
                requisicao.getRequestURI());

        excecao.getFieldErrors().forEach(erroValidacao::adicionarErro);

        return ResponseEntity.status(erroValidacao.erro().status()).body(erroValidacao);
    }

    /**
     * Manipula exceções de Regra de Negócio.
     * Retorna HTTP 422 (Unprocessable Entity) com a mensagem específica do erro.
     *
     * @param excecao    A exceção de regra de negócio capturada.
     * @param requisicao A requisição HTTP.
     * @return ResponseEntity (HTTP 422) com o {@link ErroPadraoDTO}.
     */
    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroPadraoDTO> excecaoRegraDeNegocio(RegraDeNegocioException excecao,
            HttpServletRequest requisicao) {

        log.warn("Regra de Negócio violada: {}", excecao.getMessage());

        ErroPadraoDTO erroPadrao = new ErroPadraoDTO(
                HttpStatus.UNPROCESSABLE_CONTENT, // 422
                MetodosUteis.obterMensagem(messageSource, "erro.422.negocio.titulo"),
                excecao.getMessage(),
                requisicao.getRequestURI());

        return ResponseEntity.status(erroPadrao.status()).body(erroPadrao);
    }

    /**
     * Manipula exceções genéricas (não esperadas) do servidor.
     * Retorna um HTTP 500 (Internal Server Error) padronizado.
     *
     * @param excecao    A exceção genérica capturada.
     * @param requisicao A requisição HTTP (para obter a Rota/URI).
     * @return ResponseEntity (HTTP 500) com o {@link ErroPadraoDTO}.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroPadraoDTO> excecaoGenerica(Exception excecao, HttpServletRequest requisicao) {
        log.error("Erro inesperado no servidor: {}", excecao.getMessage(), excecao);

        ErroPadraoDTO erroPadrao = new ErroPadraoDTO(
                HttpStatus.INTERNAL_SERVER_ERROR, // 500
                MetodosUteis.obterMensagem(messageSource, "erro.500.titulo"),
                MetodosUteis.obterMensagem(messageSource, "erro.500.mensagem"),
                requisicao.getRequestURI());

        return ResponseEntity.status(erroPadrao.status()).body(erroPadrao);
    }
}
