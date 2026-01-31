package br.com.sfpacim.backend.dtos.autenticacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO (Record) que representa os dados de entrada (JSON)
 * para o endpoint de login (POST /autenticacao/login).
 *
 * @author Matheus F. N. Pereira
 *
 * @param email O e-mail do usuário.
 * @param senha A senha (em texto puro) do usuário.
 */
@Schema(description = "Dados de entrada para a autenticação (login) de um usuário.")
public record DadosAutenticacaoDTO(

        @Schema(description = "E-mail cadastrado.", example = "matheusfnpereira@gmail.com") //
        @NotBlank(message = "O e-mail é obrigatório.") //
        @Email(message = "O formato do e-mail é inválido.") //
        String email,

        @Schema(description = "Senha de acesso.", example = "Ab123456") //
        @NotBlank(message = "A senha é obrigatória.") //
        String senha) {
}