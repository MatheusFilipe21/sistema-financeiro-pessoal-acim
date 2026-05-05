package br.com.sfpacim.backend.dtos.autenticacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO (Data Transfer Object) para encapsular os dados necessários para
 * efetivar a redefinição de senha.
 *
 * @author Matheus F. N. Pereira
 *
 * @param token O token JWT recebido por e-mail.
 * @param senha A nova senha desejada (mínimo 8 caracteres, 1 maiúscula, 1
 *              minúscula, 1 número).
 */
@Schema(description = "${autenticacao.descricao.schema.redefinicao}")
public record DadosRedefinicaoSenhaDTO(

        @Schema(description = "${autenticacao.descricao.token.recuperacao}", example = "${autenticacao.exemplo.token.recuperacao}") //
        @NotBlank(message = "{autenticacao.validacao.token.obrigatorio}") //
        String token,

        @Schema(description = "${usuario.descricao.senha}", example = "${usuario.exemplo.senha}") //
        @NotBlank(message = "{usuario.validacao.senha.obrigatoria}") //
        @Pattern(regexp = "^(?=.*[\\d])(?=.*[a-z])(?=.*[A-Z]).{8,}$", message = "{usuario.validacao.senha.padrao}") //
        String senha) {
}
