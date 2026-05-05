package br.com.sfpacim.backend.dtos.erro;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) com a estrutura padrão para
 * representar erros na API.
 *
 * @author Matheus F. N. Pereira
 *
 * @param status   Código de status HTTP do erro.
 * @param titulo   Título descritivo do erro.
 * @param mensagem Mensagem detalhada sobre o problema.
 * @param dataHora Data e hora em que o erro ocorreu.
 * @param rota     Caminho do endpoint que gerou a falha.
 */
@Schema(description = "${erro.descricao.schema.padrao}")
public record ErroPadraoDTO(

        @Schema(description = "${erro.descricao.padrao.status}", example = "${erro.exemplo.padrao.status}") //
        Integer status,

        @Schema(description = "${erro.descricao.padrao.titulo}", example = "${erro.exemplo.padrao.titulo}") //
        String titulo,

        @Schema(description = "${erro.descricao.padrao.mensagem}", example = "${erro.exemplo.padrao.mensagem}") //
        String mensagem,

        @Schema(description = "${erro.descricao.padrao.data-hora}", example = "${erro.exemplo.padrao.data-hora}") //
        String dataHora,

        @Schema(description = "${erro.descricao.padrao.rota}", example = "${erro.exemplo.padrao.rota}") //
        String rota) {

    /**
     * Construtor customizado para inicializar todos os atributos
     * e formatar a data/hora automaticamente.
     *
     * @param statusHTTP Status HTTP.
     * @param titulo     Título do erro.
     * @param mensagem   Mensagem detalhada do erro.
     * @param rota       Rota da requisição.
     */
    public ErroPadraoDTO(HttpStatus statusHTTP, String titulo, String mensagem, String rota) {
        this(
                statusHTTP.value(),
                titulo,
                mensagem,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                rota);
    }
}
