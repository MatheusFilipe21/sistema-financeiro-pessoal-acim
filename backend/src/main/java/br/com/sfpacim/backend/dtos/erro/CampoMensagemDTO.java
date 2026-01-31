package br.com.sfpacim.backend.dtos.erro;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Detalha um erro de validação específico em um campo.
 *
 * @author Matheus F. N. Pereira
 */
@Schema(description = "Detalha um erro de validação específico em um campo.")
public record CampoMensagemDTO(

        @Schema(description = "Nome do campo onde ocorreu o erro.", example = "email") //
        String campo,

        @Schema(description = "Mensagem de erro associada ao campo.", example = "Campo obrigatório") //
        String mensagem) {
}
