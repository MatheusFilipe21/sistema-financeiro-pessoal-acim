package br.com.sfpacim.backend.dtos.autenticacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO (Data Transfer Object) para encapsular os dados de entrada para a
 * solicitação de recuperação de senha.
 *
 * @author Matheus F. N. Pereira
 *
 * @param email O e-mail cadastrado do usuário (obrigatório e formato válido).
 */
@Schema(description = "${autenticacao.descricao.schema.recuperacao}")
public record DadosRecuperacaoSenhaDTO(

        @Schema(description = "${autenticacao.descricao.email}", example = "${usuario.exemplo.email}") //
        @NotBlank(message = "{usuario.validacao.email.obrigatorio}") //
        @Email(message = "{usuario.validacao.email.invalido}") //
        String email) {
}
