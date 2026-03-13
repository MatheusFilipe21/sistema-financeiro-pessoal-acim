package br.com.sfpacim.backend.dtos.autenticacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO (Record) que representa os dados de entrada (JSON)
 * para o endpoint de solicitação de recuperação de senha (POST
 * /autenticacao/recuperar-senha).
 *
 * @author Matheus F. N. Pereira
 *
 * @param email O e-mail cadastrado do usuário.
 */
public record DadosRecuperacaoSenhaDTO(

        /**
         * O e-mail deve ter um formato válido.
         * O e-mail é obrigatório.
         */
        @Schema(description = "E-mail cadastrado.", example = "matheusfnpereira@gmail.com") //
        @NotBlank(message = "O e-mail é obrigatório.") //
        @Email(message = "O formato do e-mail é inválido.") //
        String email) {
}
