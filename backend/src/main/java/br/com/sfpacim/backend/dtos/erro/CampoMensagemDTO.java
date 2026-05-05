package br.com.sfpacim.backend.dtos.erro;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) para detalhar um erro de validação
 * específico em um campo.
 *
 * @author Matheus F. N. Pereira
 *
 * @param campo    Nome do campo onde ocorreu o erro.
 * @param mensagem Mensagem de erro associada ao campo.
 */
@Schema(description = "${erro.descricao.schema.campo-mensagem}")
public record CampoMensagemDTO(

        @Schema(description = "${erro.descricao.campo.nome}", example = "${erro.exemplo.campo.nome}") //
        String campo,

        @Schema(description = "${erro.descricao.campo.mensagem}", example = "${erro.exemplo.campo.mensagem}") //
        String mensagem) {
}
