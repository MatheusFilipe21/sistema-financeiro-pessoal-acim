package br.com.sfpacim.backend.dtos.autenticacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO (Data Transfer Object) para encapsular os dados de entrada para a
 * autenticação (login) de um usuário.
 *
 * @author Matheus F. N. Pereira
 *
 * @param email O e-mail cadastrado do usuário.
 * @param senha A senha de acesso (em texto puro).
 */
@Schema(description = "${autenticacao.descricao.schema.login}")
public record DadosAutenticacaoDTO(

        @Schema(description = "${autenticacao.descricao.email}", example = "${usuario.exemplo.email}") //
        @NotBlank(message = "{usuario.validacao.email.obrigatorio}") //
        @Email(message = "{usuario.validacao.email.invalido}") //
        String email,

        @Schema(description = "${autenticacao.descricao.senha}", example = "${usuario.exemplo.senha}") //
        @NotBlank(message = "{usuario.validacao.senha.obrigatoria}") //
        String senha) {
}
