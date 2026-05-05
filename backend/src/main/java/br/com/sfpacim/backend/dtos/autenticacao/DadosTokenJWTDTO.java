package br.com.sfpacim.backend.dtos.autenticacao;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) para encapsular a resposta enviada após
 * um login bem-sucedido.
 *
 * @author Matheus F. N. Pereira
 *
 * @param token O token JWT gerado.
 */
@Schema(description = "${autenticacao.descricao.schema.token}")
public record DadosTokenJWTDTO(

        @Schema(description = "${autenticacao.descricao.token.jwt}", example = "${autenticacao.exemplo.token.jwt}") //
        String token) {
}